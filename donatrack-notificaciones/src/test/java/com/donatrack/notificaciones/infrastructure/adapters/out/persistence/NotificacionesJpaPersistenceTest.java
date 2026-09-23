package com.donatrack.notificaciones.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.EventoNotificacionEntity;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.entities.NotificacionEntity;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.repositories.EventoNotificacionJpaRepository;
import com.donatrack.notificaciones.infrastructure.adapters.out.persistence.repositories.NotificacionJpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class NotificacionesJpaPersistenceTest {

  @Autowired private EventoNotificacionJpaRepository eventoRepository;

  @Autowired private NotificacionJpaRepository notificacionRepository;

  @Test
  @DisplayName("Debe persistir un Evento de Notificación y una Notificación en H2 in-memory")
  void debePersistirEventoYNotificacion() {
    EventoNotificacionEntity evento = new EventoNotificacionEntity();
    evento.setTipoEvento("ENTREGA_COMPLETADA");
    evento.setDescripcion("Notificar al donante que su donación fue entregada");
    evento.setFechaRecepcion(LocalDateTime.now());

    EventoNotificacionEntity savedEvento = eventoRepository.save(evento);
    assertNotNull(savedEvento.getId());

    NotificacionEntity notif = new NotificacionEntity();
    notif.setEventoNotificacion(savedEvento);
    notif.setMedio("EMAIL");
    notif.setDestinatario("donante@example.com");
    notif.setMensaje("Tu donación ha sido entregada con éxito.");
    notif.setCompletada(true);
    notif.setFechaEnvio(LocalDateTime.now());

    NotificacionEntity savedNotif = notificacionRepository.save(notif);

    assertNotNull(savedNotif.getId());
    Optional<NotificacionEntity> encontrada = notificacionRepository.findById(savedNotif.getId());
    assertEquals(true, encontrada.isPresent());
    assertEquals("EMAIL", encontrada.get().getMedio());
    assertEquals(savedEvento.getId(), encontrada.get().getEventoNotificacion().getId());
  }
}
