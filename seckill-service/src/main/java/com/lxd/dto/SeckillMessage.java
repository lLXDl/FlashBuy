package com.lxd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.messaging.handler.annotation.Payload;
import com.rabbitmq.client.Channel;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage implements Serializable {
    private Long userId;
    private Long goodsId;
}
