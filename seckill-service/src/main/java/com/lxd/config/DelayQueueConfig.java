package com.lxd.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelayQueueConfig {
    public static final String DLX_QUEUE = "order.close.queue";
    public static final String DLX_EXCHANGE = "order.dlx.exchange";
    public static final String DLX_ROUTING_KEY = "order.close";
    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DELAY_EXCHANGE = "order.delay.exchange";
    public static final String DELAY_ROUTING_KEY = "order.delay";

    // 死信交换机
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // 死信队列（真正处理关单的队列）
    @Bean
    public Queue orderCloseQueue() {
        return new Queue(DLX_QUEUE, true);
    }

    // 绑定死信队列到死信交换机，routing key = "order.close"
    @Bean
    public Binding closeBinding() {
        return BindingBuilder.bind(orderCloseQueue())
                .to(orderDlxExchange())
                .with(DLX_ROUTING_KEY);
    }

    // 延迟队列（无消费者，仅设置TTL和DLX）
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE) // 持久化
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                // 消息级别 TTL 也可在发送时设置，这里可以给队列设置默认 TTL
                .withArgument("x-message-ttl", 30 * 1000) // 30秒
                .build();
    }

    // 延迟队列绑定
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderDelayExchange())
                .with(DELAY_ROUTING_KEY);
    }
}
