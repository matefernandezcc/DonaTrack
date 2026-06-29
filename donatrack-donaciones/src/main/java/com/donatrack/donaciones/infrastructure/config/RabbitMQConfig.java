package com.donatrack.donaciones.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {

    public static final String DONACIONES_EXCHANGE = "donaciones.exchange";
    public static final String LOGISTICA_EXCHANGE = "logistica.exchange";
    public static final String RUTA_INICIADA_QUEUE = "donaciones.ruta_iniciada.queue";

    @Bean
    public TopicExchange donacionesExchange() {
        return new TopicExchange(DONACIONES_EXCHANGE);
    }

    @Bean
    public TopicExchange logisticaExchange() {
        return new TopicExchange(LOGISTICA_EXCHANGE);
    }

    @Bean
    public Queue rutaIniciadaQueue() {
        return new Queue(RUTA_INICIADA_QUEUE);
    }

    @Bean
    public Binding bindingRutaIniciada(Queue rutaIniciadaQueue, TopicExchange logisticaExchange) {
        return BindingBuilder.bind(rutaIniciadaQueue).to(logisticaExchange).with("ruta.iniciada");
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
