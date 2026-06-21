package com.donatrack.common.events;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaFallidaEvent {
    // Equivale al ID de la donación original para mantener trazabilidad entre módulos
    private UUID idEntrega;
    private String motivo;
}
