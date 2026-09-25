package com.donatrack.common.events;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaNoSatisfactoriaEvent {
  private UUID idDonacion;
  private String motivo;
  private boolean puedeReplanificarse;
}
