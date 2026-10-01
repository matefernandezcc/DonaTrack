package com.donatrack.incentivos.application.ports.out;

import java.io.Serializable;

public record NotificacionRequest(String destinatario, String mensaje, String medio) implements Serializable {}
