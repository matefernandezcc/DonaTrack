package com.donatrack.logistica.application.ports.in;

import java.util.List;
import java.util.UUID;

public interface ConfirmarEntregaUseCase {
    void confirmarEntrega(UUID idEntrega, List<String> fotos, String patenteCamion);
}
