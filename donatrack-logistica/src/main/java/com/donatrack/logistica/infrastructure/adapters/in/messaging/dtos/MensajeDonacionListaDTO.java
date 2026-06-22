package com.donatrack.logistica.infrastructure.adapters.in.messaging.dtos;

import java.util.UUID;

public record MensajeDonacionListaDTO(
                UUID idDonacion,
                Double peso,
                Double volumen,
                // Desarmamos la dirección en tipos primitivos que vienen del JSON
                String calleDestino,
                String alturaDestino,
                String localidadDestino) {
}