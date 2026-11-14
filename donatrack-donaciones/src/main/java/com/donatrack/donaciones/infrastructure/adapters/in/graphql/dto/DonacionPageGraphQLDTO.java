package com.donatrack.donaciones.infrastructure.adapters.in.graphql.dto;

import java.util.List;

public record DonacionPageGraphQLDTO(
    List<DonacionGraphQLDTO> contenido,
    int totalElementos,
    int totalPaginas,
    int paginaActual) {}
