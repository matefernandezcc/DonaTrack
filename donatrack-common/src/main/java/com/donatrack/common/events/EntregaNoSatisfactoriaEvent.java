package com.donatrack.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaNoSatisfactoriaEvent {
    private UUID idDonacion;
    private String motivo;
    private boolean puedeReplanificarse;
}
