package com.donatrack.logistica.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration("logisticaRabbitMQConfig")
public class RabbitMQConfig {

    public static final String LOGISTICA_EXCHANGE = "logistica.exchange";

    @Bean
    public TopicExchange logisticaExchange() {
        return new TopicExchange(LOGISTICA_EXCHANGE);
    }

    @Bean("logisticaJsonMessageConverter")
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    public static final String DONACIONES_EXCHANGE = "donaciones.exchange";
    public static final String REPLANIFICADA_QUEUE = "logistica.replanificada.queue";

    @Bean
    public TopicExchange donacionesExchange() {
        return new TopicExchange(DONACIONES_EXCHANGE);
    }

    @Bean
    public Queue replanificadaQueue() {
        return new Queue(REPLANIFICADA_QUEUE);
    }

    @Bean
    public Binding bindingReplanificada(Queue replanificadaQueue, TopicExchange donacionesExchange) {
        return BindingBuilder.bind(replanificadaQueue).to(donacionesExchange).with("donacion.replanificada");
    }
}
