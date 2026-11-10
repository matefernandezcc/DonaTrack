package com.donatrack.web.dto;

import java.util.UUID;

public record EntregaActivaDonanteDTO(
    UUID idDonacion,
    String codigoCorto,
    String estado,
    String patenteCamion,
    String choferNombre,
    String destinoComedor,
    double lat,
    double lng,
    String ultimaActualizacion
) {}
