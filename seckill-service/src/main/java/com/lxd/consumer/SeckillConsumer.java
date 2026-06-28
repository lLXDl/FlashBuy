package com.lxd.consumer;

import com.lxd.config.DelayQueueConfig;
import com.lxd.config.RabbitConfig;
import com.lxd.dto.SeckillMessage;
import com.lxd.service.DelayMessageService;
import com.lxd.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;      // ← RabbitMQ 的 Message
import com.rabbitmq.client.Channel;                 // ← RabbitMQ 原生 Channel

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class SeckillConsumer {

    private final SeckillService seckillService;
    private final DelayMessageService delayMessageService;

    @RabbitListener(queues = RabbitConfig.SECKILL_QUEUE)
    // 第一个第二个是固定写法，第三个才是传过来的
    public void handleSeckill(Message message, Channel channel,
                              @Payload SeckillMessage msg) throws IOException {

        // 拿到消息的标签
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 1. 创建订单（返回订单ID）
            Long orderId = seckillService.createOrder(msg);

            // 2. 发送延迟关单消息（15分钟 = 900000ms）
            delayMessageService.sendOrderDelayMessage(orderId, 30 * 1000L);

            channel.basicAck(deliveryTag, false);
            log.info("下单成功 userId={}, goodsId={}", msg.getUserId(), msg.getGoodsId());
        } catch (DuplicateKeyException e) {
            log.warn("用户重复下单，忽略 userId={}, goodsId={}", msg.getUserId(), msg.getGoodsId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("下单失败，进入重试 userId={}, goodsId={}", msg.getUserId(), msg.getGoodsId(), e);
            channel.basicNack(deliveryTag, false, true);
        }

    }
}
