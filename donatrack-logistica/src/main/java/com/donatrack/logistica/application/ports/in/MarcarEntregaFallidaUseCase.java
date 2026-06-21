package com.donatrack.logistica.application.ports.in;

import java.util.UUID;

public interface MarcarEntregaFallidaUseCase {
    void marcarEntregaFallida(UUID idEntrega, String motivo);
}
