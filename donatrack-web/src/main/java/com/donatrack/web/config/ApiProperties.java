package com.donatrack.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "donatrack.api")
public record ApiProperties(
    String authUrl,
    String donacionesUrl,
    String incentivosUrl,
    String logisticaUrl,
    String notificacionesUrl,
    long timeoutSeconds) {}
