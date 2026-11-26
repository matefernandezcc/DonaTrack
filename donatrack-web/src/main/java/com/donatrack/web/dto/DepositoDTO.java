package com.donatrack.web.dto;

public record DepositoDTO(
    String id,
    String nombre,
    String barrio,
    String direccion,
    String horarios,
    double lat,
    double lng,
    String acepta,
    String referencia,
    String descripcion,
    String estado) {}
