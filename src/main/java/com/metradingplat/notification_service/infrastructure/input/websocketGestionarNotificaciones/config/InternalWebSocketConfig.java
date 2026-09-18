package com.metradingplat.notification_service.infrastructure.input.websocketGestionarNotificaciones.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.metradingplat.notification_service.infrastructure.input.websocketGestionarNotificaciones.handler.EstadoEscanerWSHandler;
import com.metradingplat.notification_service.infrastructure.input.websocketGestionarNotificaciones.handler.LogNotificacionWSHandler;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints internos (servicio a servicio, sin pasar por el Gateway) que
 * reemplazan los @KafkaListener de "logs.notifications" y "scanner.state".
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class InternalWebSocketConfig implements WebSocketConfigurer {

    private final LogNotificacionWSHandler logNotificacionWSHandler;
    private final EstadoEscanerWSHandler estadoEscanerWSHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this.logNotificacionWSHandler, "/ws/internal/notificaciones/logs");
        registry.addHandler(this.estadoEscanerWSHandler, "/ws/internal/notificaciones/estado-escaner");
    }
}
