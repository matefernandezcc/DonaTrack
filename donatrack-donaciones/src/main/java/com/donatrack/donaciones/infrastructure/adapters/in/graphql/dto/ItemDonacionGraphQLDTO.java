package com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto;

import java.util.UUID;

public record ItemDonacionGraphQLDTO(
    UUID id,
    String nombre,
    int cantidad,
    String categoria) {}
