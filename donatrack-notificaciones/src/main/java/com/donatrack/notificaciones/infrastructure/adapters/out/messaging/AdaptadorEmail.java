package com.donatrack.notificaciones.infrastructure.adapters.out.messaging;

import com.donatrack.notificaciones.application.ports.out.NotificacionAdapter;
import com.donatrack.notificaciones.domain.entities.Notificacion;
import com.donatrack.notificaciones.infrastructure.adapters.out.client.N8nNotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("EMAIL")
public class AdaptadorEmail implements NotificacionAdapter {

  private static final Logger logger = LoggerFactory.getLogger(AdaptadorEmail.class);
  private final N8nNotificationClient n8nClient;

  public AdaptadorEmail(N8nNotificationClient n8nClient) {
    this.n8nClient = n8nClient;
  }

  @Override
  public void enviar(Notificacion notificacion) {
    logger.info(
        "Enviando Email a {}: {}", notificacion.getDestinatario(), notificacion.getMensaje());
    boolean enviado = n8nClient.enviar(notificacion.getDestinatario(), notificacion.getMensaje(), "EMAIL");
    notificacion.setCompletada(enviado);
  }
}
