package com.donatrack.incentivos.infrastructure.client;

public record NotificacionRequest(String destinatario, String mensaje, String medio) {}
