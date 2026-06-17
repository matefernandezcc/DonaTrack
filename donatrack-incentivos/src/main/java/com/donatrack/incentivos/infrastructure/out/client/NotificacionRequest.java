package com.donatrack.incentivos.infrastructure.out.client;

public record NotificacionRequest(String destinatario, String mensaje, String medio) {}
