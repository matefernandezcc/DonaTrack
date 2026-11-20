package com.donatrack.web.dto;

public record PuntoMapaPublicoDTO(
    String tipo, // "blue" (recepción), "green" (ong), "orange" (entrega)
    double lat,
    double lng,
    String titulo,
    String descripcion,
    String organizacion,
    String subcategoria,
    String fecha,
    String estado,
    String fotoUrl) {}
