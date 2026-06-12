package com.donatrack.notificaciones.infrastructure.controller;

public record NotificacionRequest(String destinatario, String mensaje, String medio) {}
