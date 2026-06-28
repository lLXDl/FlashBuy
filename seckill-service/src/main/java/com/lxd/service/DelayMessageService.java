package com.lxd.service;

import com.lxd.config.DelayQueueConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DelayMessageService {
    private final RabbitTemplate rabbitTemplate;

    public void sendOrderDelayMessage(Long orderId, Long delayTime) {
        rabbitTemplate.convertAndSend(
                DelayQueueConfig.DELAY_EXCHANGE,
                DelayQueueConfig.DELAY_ROUTING_KEY,
                orderId,
                message -> {
                    message.getMessageProperties().setExpiration(delayTime.toString());
                    return message;
                }
        );
    }
}
