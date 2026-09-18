package com.metradingplat.notification_service.infrastructure.input.websocketGestionarNotificaciones.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition.LogNotificacionDTOPeticion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.mappers.NotificacionKafkaMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reemplaza al @KafkaListener de topic "logs.notifications" -- log-service se
 * conecta aca como cliente WS y manda un mensaje de texto (JSON) por cada log
 * ya persistido que deba notificarse en tiempo real.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogNotificacionWSHandler extends TextWebSocketHandler {

    private final GestionarNotificacionesCUIntPort objGestionarNotificacionesCUInt;
    private final NotificacionKafkaMapper objMapper;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LogNotificacionDTOPeticion command = this.objectMapper.readValue(message.getPayload(),
                LogNotificacionDTOPeticion.class);
        log.debug("Recibida notificacion de log via WS: [{}] {}", command.getNivel(), command.getMensaje());
        Notificacion objNotificacion = this.objMapper.deDTOADominio(command);
        this.objGestionarNotificacionesCUInt.procesarNotificacionLog(objNotificacion);
    }
}
