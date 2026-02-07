package com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition.EstadoEscanerEventoDTOPeticion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition.LogNotificacionDTOPeticion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.mappers.NotificacionKafkaMapper;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionesKafkaListener {

    private final GestionarNotificacionesCUIntPort objGestionarNotificacionesCUInt;
    private final NotificacionKafkaMapper objMapper;

    @KafkaListener(topics = "logs.notifications", groupId = "notifications-group")
    public void recibirLogNotificacion(LogNotificacionDTOPeticion command) {
        log.debug("Recibida notificacion de log: [{}] {}", command.getNivel(), command.getMensaje());
        Notificacion objNotificacion = this.objMapper.deDTOADominio(command);
        this.objGestionarNotificacionesCUInt.procesarNotificacionLog(objNotificacion);
    }

    @KafkaListener(topics = "scanner.state", groupId = "notifications-group")
    public void recibirEstadoEscaner(EstadoEscanerEventoDTOPeticion evento) {
        log.info("Recibido evento de estado escaner: {} -> {}",
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
