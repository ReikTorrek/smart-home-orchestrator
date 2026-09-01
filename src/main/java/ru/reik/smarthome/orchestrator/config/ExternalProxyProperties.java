package ru.reik.smarthome.orchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external-proxy")
public record ExternalProxyProperties(
        boolean enabled,
        String host,
        int port
) {
}
