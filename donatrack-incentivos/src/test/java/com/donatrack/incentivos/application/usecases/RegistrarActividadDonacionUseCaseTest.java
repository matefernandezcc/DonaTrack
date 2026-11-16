package com.donatrack.incentivos.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.donatrack.common.dto.ActividadDonacionDTO;
import com.donatrack.incentivos.application.ports.out.IncentivosNotificacionPort;
import com.donatrack.incentivos.application.ports.out.NotificacionRequest;
import com.donatrack.incentivos.application.ports.out.PerfilDonanteRepository;
import com.donatrack.incentivos.domain.entities.PerfilDonante;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

@DisplayName("RegistrarActividadDonacionUseCase — Resolución de Email Real")
class RegistrarActividadDonacionUseCaseTest {

  @Test
  @DisplayName(
      "Al completar la primera misión (2 donaciones) envía notificación al email real del donante")
  void testNotificaAlEmailRealDelDonante() {
    PerfilDonanteRepository repo = mock(PerfilDonanteRepository.class);
    ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    IncentivosNotificacionPort notifPort = mock(IncentivosNotificacionPort.class);

    UUID donanteId = UUID.randomUUID();
    PerfilDonante perfil = new PerfilDonante(donanteId);
    when(repo.findById(donanteId)).thenReturn(Optional.of(perfil));

    RegistrarActividadDonacionUseCase useCase =
        new RegistrarActividadDonacionUseCase(
            notifPort, publisher, repo, id -> "lucia.solidaria@ejemplo.org");

    useCase.ejecutar(
        donanteId,
        new ActividadDonacionDTO(
            UUID.randomUUID(),
            donanteId,
            5,
            List.of("ALIMENTOS"),
            UUID.randomUUID(),
            LocalDate.now()));
    useCase.ejecutar(
        donanteId,
        new ActividadDonacionDTO(
            UUID.randomUUID(),
            donanteId,
            3,
            List.of("ABRIGO"),
            UUID.randomUUID(),
            LocalDate.now()));

    ArgumentCaptor<NotificacionRequest> captor = ArgumentCaptor.forClass(NotificacionRequest.class);
    verify(notifPort, atLeastOnce()).enviarNotificacion(captor.capture());
    assertEquals("lucia.solidaria@ejemplo.org", captor.getValue().destinatario());
  }

  @Test
  @DisplayName(
      "Si el resolver falla o retorna blank, hace fallback al email técnico donante{id}@test.com")
  void testFallbackCuandoEmailNoDisponible() {
    PerfilDonanteRepository repo = mock(PerfilDonanteRepository.class);
    ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    IncentivosNotificacionPort notifPort = mock(IncentivosNotificacionPort.class);

    UUID donanteId = UUID.randomUUID();
    PerfilDonante perfil = new PerfilDonante(donanteId);
    when(repo.findById(donanteId)).thenReturn(Optional.of(perfil));

    RegistrarActividadDonacionUseCase useCase =
        new RegistrarActividadDonacionUseCase(notifPort, publisher, repo, id -> "");

    useCase.ejecutar(
        donanteId,
        new ActividadDonacionDTO(
            UUID.randomUUID(),
            donanteId,
            5,
            List.of("ALIMENTOS"),
            UUID.randomUUID(),
            LocalDate.now()));
    useCase.ejecutar(
        donanteId,
        new ActividadDonacionDTO(
            UUID.randomUUID(),
            donanteId,
            4,
            List.of("HIGIENE"),
            UUID.randomUUID(),
            LocalDate.now()));

    ArgumentCaptor<NotificacionRequest> captor = ArgumentCaptor.forClass(NotificacionRequest.class);
    verify(notifPort, atLeastOnce()).enviarNotificacion(captor.capture());
    assertEquals("donante" + donanteId + "@test.com", captor.getValue().destinatario());
  }
}
