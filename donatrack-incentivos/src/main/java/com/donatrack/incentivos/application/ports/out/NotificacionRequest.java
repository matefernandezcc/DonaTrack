package com.donatrack.incentivos.application.ports.out;

public record NotificacionRequest(String destinatario, String mensaje, String medio) {}
