package com.lxd.consumer;

import com.lxd.config.DelayQueueConfig;
import com.lxd.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCloseConsumer {
    private final OrderService orderService;

    @RabbitListener(queues = DelayQueueConfig.DLX_QUEUE, ackMode = "MANUAL")
    public void handleOrderClose(Message message, Channel channel, @Payload Long orderId) {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            boolean closed = orderService.closeOrderIfUnpaid(orderId);
            if (closed) {
                log.info("订单 {} 超时关闭成功", orderId);
                // 恢复库存等其他操作...
            } else {
                log.info("订单 {} 状态已变更，无需关闭", orderId);
            }
            // 处理成功，手动确认
            channel.basicAck(tag, false);
        } catch (Exception e) {
            // 根据异常类型决定是否重试，这里简化处理：记录日志并拒绝 requeue=false 避免死循环
            log.error("关单失败, orderId={}", orderId, e);
            try {
                channel.basicNack(tag, false, false); // 不重新入队，可转入死信或丢弃
            } catch (Exception ex) {
                log.error("nack失败", ex);
            }
        }
    }
}
