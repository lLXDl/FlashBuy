package com.lxd.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LikeRabbitConfig {
    public static final String LIKE_EXCHANGE = "like.exchange";
    public static final String LIKE_QUEUE = "like.queue";
    public static final String LIKE_ROUTING_KEY = "like.record";

    @Bean
    public Queue likeQueue() {
        return new Queue(LIKE_QUEUE, true);
    }

    @Bean
    public TopicExchange likeExchange() {
        return new TopicExchange(LIKE_EXCHANGE);
    }

    @Bean
    public Binding likeBinding() {
        return BindingBuilder.bind(likeQueue()).to(likeExchange()).with(LIKE_ROUTING_KEY);
    }
}
