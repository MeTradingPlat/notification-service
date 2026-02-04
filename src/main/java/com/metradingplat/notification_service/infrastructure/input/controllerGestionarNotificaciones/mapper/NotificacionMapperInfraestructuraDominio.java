package com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.DTOAnswer.NotificacionDTORespuesta;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificacionMapperInfraestructuraDominio {
    NotificacionDTORespuesta mappearDeNotificacionARespuesta(Notificacion notificacion);
}
