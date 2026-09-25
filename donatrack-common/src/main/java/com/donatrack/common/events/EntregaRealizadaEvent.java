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
public class EntregaRealizadaEvent {
  private UUID idDonacion;
  private List<String> fotos;
  private String patenteCamion;
  private LocalDateTime fechaHora;
}
