package com.metradingplat.notification_service.application.output;

import com.metradingplat.notification_service.domain.models.Notificacion;

import reactor.core.publisher.Flux;

public interface EmitirNotificacionIntPort {

    void emitir(Notificacion objNotificacion);

    Flux<Notificacion> obtenerStream();

    Flux<Notificacion> obtenerStreamPorEscaner(Long idEscaner);
}
