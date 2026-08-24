package com.metradingplat.notification_service.application.output;

import com.metradingplat.notification_service.domain.models.EventoNotificado;
import com.metradingplat.notification_service.domain.models.Notificacion;

import reactor.core.publisher.Flux;

public interface EmitirNotificacionIntPort {

    void emitir(Notificacion objNotificacion);

    Flux<EventoNotificado> obtenerStream();

    Flux<EventoNotificado> obtenerStreamPorEscaner(Long idEscaner);
}
