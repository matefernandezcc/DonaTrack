package com.donatrack.notificaciones.infrastructure.adapters.out.client;

public record NotificacionRequest(String destinatario, String mensaje, String medio) {}
