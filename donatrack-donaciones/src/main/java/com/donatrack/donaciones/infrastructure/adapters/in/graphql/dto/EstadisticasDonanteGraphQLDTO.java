package com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto;

import java.util.UUID;

public record EstadisticasDonanteGraphQLDTO(
    UUID idDonante,
    int totalDonaciones,
    int donacionesEntregadas) {}
