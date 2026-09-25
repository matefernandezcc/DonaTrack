package com.donatrack.common.events;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEntregaExitosaEvent {
  private UUID idDonacion;
  private List<NotificacionInicioRutaEvent.ContactoInfo> contactosDonantes;
  private List<NotificacionInicioRutaEvent.ContactoInfo> contactosEntidades;
  private LocalDateTime fechaHora;
  private String patenteCamion;
  private List<String> enlacesFotos;
}
