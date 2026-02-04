package com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition.LogNotificacionDTOPeticion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.mappers.NotificacionKafkaMapper;

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
}
