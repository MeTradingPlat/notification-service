package com.metradingplat.notification_service.infrastructure.output.sse;

import org.springframework.stereotype.Service;

import com.metradingplat.notification_service.application.output.EmitirNotificacionIntPort;
import com.metradingplat.notification_service.domain.models.Notificacion;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class SseEmitterAdapter implements EmitirNotificacionIntPort {

    private final Sinks.Many<Notificacion> sink;

    public SseEmitterAdapter() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    public void emitir(Notificacion objNotificacion) {
        log.debug("Emitiendo notificacion SSE: [{}] {}", objNotificacion.getNivel(), objNotificacion.getTitulo());
        Sinks.EmitResult result = this.sink.tryEmitNext(objNotificacion);
        if (result.isFailure()) {
            log.warn("Fallo al emitir notificacion: {}", result);
        }
    }

    @Override
    public Flux<Notificacion> obtenerStream() {
        return this.sink.asFlux();
    }

    @Override
    public Flux<Notificacion> obtenerStreamPorEscaner(Long idEscaner) {
        return this.sink.asFlux()
                .filter(notificacion -> idEscaner.equals(notificacion.getIdEscaner()));
    }
}
