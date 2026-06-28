package com.lxd.consumer;

import com.lxd.config.LikeRabbitConfig;
import com.lxd.entity.LikeMessage;
import com.lxd.entity.LikeRecord;
import com.lxd.mapper.LikeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeConsumer {
    private final LikeRecordMapper likeRecordMapper;

    @RabbitListener(queues = LikeRabbitConfig.LIKE_QUEUE)
    public void handleLike(LikeMessage message, Channel channel, Message rabbitMessage) throws IOException {
        long tag = rabbitMessage.getMessageProperties().getDeliveryTag();
        try {
            LikeRecord record = new LikeRecord();
            record.setUserId(message.getUserId());
            record.setTargetType(message.getTargetType());
            record.setTargetId(message.getTargetId());
            record.setStatus(message.getStatus());

            likeRecordMapper.upsert(record);
            channel.basicAck(tag, false);
            log.info("点赞记录落库成功: userId={}, targetId={}", message.getUserId(), message.getTargetId());
        } catch (Exception e) {
            log.error("点赞记录落库失败", e);
            channel.basicNack(tag, false, true);
        }
    }
}