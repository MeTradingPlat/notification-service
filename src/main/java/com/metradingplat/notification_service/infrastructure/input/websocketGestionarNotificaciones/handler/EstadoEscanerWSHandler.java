package com.metradingplat.notification_service.infrastructure.input.websocketGestionarNotificaciones.handler;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition.EstadoEscanerEventoDTOPeticion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reemplaza al @KafkaListener de topic "scanner.state" -- signal-processing-service
 * y scanner-management-service se conectan aca como clientes WS para avisar
 * cambios de estado de escaner.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EstadoEscanerWSHandler extends TextWebSocketHandler {

    private final GestionarNotificacionesCUIntPort objGestionarNotificacionesCUInt;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        EstadoEscanerEventoDTOPeticion evento = this.objectMapper.readValue(message.getPayload(),
                EstadoEscanerEventoDTOPeticion.class);
        log.info("Recibido evento de estado escaner via WS: {} -> {}",
                evento.getIdEscaner(), evento.getEstadoNuevo());

        Notificacion objNotificacion = Notificacion.builder()
                .id(String.format("state-%d-%s", evento.getIdEscaner(), evento.getTimestamp()))
                .tipo("SCANNER_STATE")
                .nivel("INFO")
                .titulo(String.format("Escaner %s: %s", evento.getNombreEscaner(), evento.getEstadoNuevo()))
                .mensaje(String.format("El escaner '%s' cambio de %s a %s. Razon: %s",
                        evento.getNombreEscaner(),
                        evento.getEstadoAnterior(),
                        evento.getEstadoNuevo(),
                        evento.getRazon()))
                .idEscaner(evento.getIdEscaner())
                .categoria("SCANNER")
                .timestamp(LocalDateTime.now())
                .metadatos(String.format("{\"estadoAnterior\":\"%s\",\"estadoNuevo\":\"%s\",\"razon\":\"%s\"}",
                        evento.getEstadoAnterior(), evento.getEstadoNuevo(), evento.getRazon()))
                .build();

        this.objGestionarNotificacionesCUInt.procesarNotificacionEstadoEscaner(objNotificacion);
    }
}
