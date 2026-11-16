package com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto;

import java.util.List;
import java.util.UUID;

public record DonacionGraphQLDTO(
    UUID id,
    String descripcion,
    String estado,
    String fechaCreacion,
    List<ItemDonacionGraphQLDTO> items) {}
