package com.donatrack.common.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;

import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQCommonConfig {

  @Bean
  @Primary
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
    DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
    typeMapper.setTrustedPackages("*");
    converter.setJavaTypeMapper(typeMapper);
    return converter;
  }
}
