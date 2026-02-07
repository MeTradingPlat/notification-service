package com.metradingplat.notification_service.application.input;

import com.metradingplat.notification_service.domain.models.Notificacion;

import reactor.core.publisher.Flux;

public interface GestionarNotificacionesCUIntPort {

    void procesarNotificacionLog(Notificacion objNotificacion);

    void procesarNotificacionEstadoEscaner(Notificacion objNotificacion);

    Flux<Notificacion> obtenerStreamNotificaciones();

    Flux<Notificacion> obtenerStreamNotificacionesPorEscaner(Long idEscaner);
}
