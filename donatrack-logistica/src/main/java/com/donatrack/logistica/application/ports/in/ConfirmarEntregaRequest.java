package com.donatrack.logistica.application.ports.in;

import java.util.List;

public record ConfirmarEntregaRequest(
    List<String> fotos,
    String patenteCamion
) {}
