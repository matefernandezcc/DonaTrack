package com.donatrack;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DonatrackTests {

  // Mock del ConnectionFactory para evitar que RabbitMQ intente conectarse a un broker real
  @MockitoBean
  private ConnectionFactory connectionFactory;

  @Test
  void contextLoads() {
    // Este test verifica que el servidor de DonaTrack arranque sin errores
  }
}
