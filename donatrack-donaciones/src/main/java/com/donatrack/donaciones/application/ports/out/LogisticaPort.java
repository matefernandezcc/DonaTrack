package com.donatrack.donaciones.application.ports.out;

import java.util.UUID;

public interface LogisticaPort {
    /**
     * Solicita al servicio de logística que planifique el retiro de una donación.
     */
    void solicitarRetiro(UUID idDonacionOriginal, double pesoTotal, double volumenTotal, String calle, String altura, String localidad);
}
