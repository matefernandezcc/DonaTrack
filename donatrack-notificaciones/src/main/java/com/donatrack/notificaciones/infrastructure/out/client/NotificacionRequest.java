package com.donatrack.notificaciones.infrastructure.out.client;

public record NotificacionRequest(String destinatario, String mensaje, String medio) {}
