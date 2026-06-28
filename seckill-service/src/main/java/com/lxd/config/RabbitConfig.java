package com.lxd.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitConfig {
    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_ROUTING_KEY = "seckill.routing.key";


    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true); // 持久化
    }

    @Bean
    public TopicExchange seckillExchange() {
        return new TopicExchange(SECKILL_EXCHANGE);
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with(SECKILL_ROUTING_KEY);
    }

    // ⭐ 关键：配置 MessageConverter 为 JSON 格式
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ⭐ 关键：配置 RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    // ⭐ 关键：配置 RabbitListenerContainerFactory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }
}
