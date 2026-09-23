package com.donatrack.incentivos.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQIncentivosConfig {

    public static final String INCENTIVOS_EXCHANGE = "incentivos.exchange";
    public static final String NUEVA_INSIGNIA_QUEUE = "nueva_insignia_queue";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange incentivosExchange() {
        return new TopicExchange(INCENTIVOS_EXCHANGE);
    }

    @Bean
    public Queue nuevaInsigniaQueue() {
        return new Queue(NUEVA_INSIGNIA_QUEUE, true); // Durable
    }

    @Bean
    public Binding bindingNuevaInsignia(Queue nuevaInsigniaQueue, TopicExchange incentivosExchange) {
        return BindingBuilder.bind(nuevaInsigniaQueue).to(incentivosExchange).with("insignia.nueva");
    }
}
