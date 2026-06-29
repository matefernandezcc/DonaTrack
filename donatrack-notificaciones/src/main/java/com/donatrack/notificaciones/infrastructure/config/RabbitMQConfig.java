package com.donatrack.notificaciones.infrastructure.config;

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
    public static final String RUTA_INICIADA_NOTIF_QUEUE = "notificaciones.ruta_iniciada.queue";

    @Bean
    public TopicExchange donacionesExchange() {
        return new TopicExchange(DONACIONES_EXCHANGE);
    }

    @Bean
    public Queue rutaIniciadaNotifQueue() {
        return new Queue(RUTA_INICIADA_NOTIF_QUEUE);
    }

    @Bean
    public Binding bindingNotifRutaIniciada(Queue rutaIniciadaNotifQueue, TopicExchange donacionesExchange) {
        return BindingBuilder.bind(rutaIniciadaNotifQueue).to(donacionesExchange).with("notificacion.inicio.ruta");
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
