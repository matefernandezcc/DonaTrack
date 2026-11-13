package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

public record SolicitudOngDTO(
    String id,
    String nombreOng,
    String cuit,
    String direccion,
    String email,
    String telefono,
    String responsable,
    String tipoEntidad,
    String necesidades,
    String estado,
    String fecha) {}
