package com.metradingplat.notification_service.infrastructure.output.sse;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.metradingplat.notification_service.application.output.EmitirNotificacionIntPort;
import com.metradingplat.notification_service.domain.models.EventoNotificado;
import com.metradingplat.notification_service.domain.models.Notificacion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class SseEmitterAdapter implements EmitirNotificacionIntPort {

    private static final int MAX_BUFFER_SIZE = 500;

    private final Sinks.Many<EventoNotificado> sink;
    private final AtomicLong eventIdCounter = new AtomicLong();
    private final ConcurrentLinkedDeque<EventoBuffered> buffer = new ConcurrentLinkedDeque<>();

    public SseEmitterAdapter() {
        // directBestEffort() tiene muy poco margen para absorber una rafaga
        // de emisiones (confirmado en vivo el 2026-08-22:
        // reactor.core.Exceptions$OverflowException en emitNext apenas
        // llegaban varias notificaciones casi juntas, ej. varios escaneres
        // cambiando de estado en milisegundos) -- y a diferencia de un
        // simple valor perdido, esa excepcion termina el sink entero con
        // error, dejando a CUALQUIER suscriptor (incluido uno que se acabe
        // de conectar) recibiendo el mismo error de inmediato hasta el
        // proximo reinicio del servicio. onBackpressureBuffer() absorbe
        // rafagas reales con una cola acotada (mismo tamano que el buffer de
        // reconexion de mas abajo) en vez de fallar por un pico momentaneo.
        this.sink = Sinks.many().multicast().onBackpressureBuffer(MAX_BUFFER_SIZE);
    }

    @Override
    public void emitir(Notificacion objNotificacion) {
        log.debug("Emitiendo notificacion SSE: [{}] {}", objNotificacion.getNivel(), objNotificacion.getTitulo());
        // El id y el buffer viven AQUI, en la emision, no en el .map() de cada
        // suscriptor del endpoint SSE: con el buffer en el map por-suscriptor,
        // un evento emitido sin ningun suscriptor activo (desconexion del
        // frontend, o la conexion ya cancelada) jamas se numeraba ni se
        // buffereaba, y el replay por Last-Event-Id no tenia nada que
        // reenviar -- las senales generadas durante el hueco se perdian para
        // siempre (confirmado en vivo el 2026-08-24: 270 FAIL_CANCELLED,
        // incluido el primer lote de 169 senales del escaner 'volumen test').
        // Ahora cada evento queda en el buffer de reconexion SIEMPRE, haya o
        // no suscriptores en ese momento.
        long eventId = this.eventIdCounter.incrementAndGet();
        this.bufferEvent(eventId, objNotificacion);
        this.sink.emitNext(new EventoNotificado(eventId, objNotificacion), (signalType, emitResult) -> {
            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                return true;
            }
            if (emitResult != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.warn("Fallo emitiendo notificacion SSE: [{}] {} -> {}",
                        objNotificacion.getNivel(), objNotificacion.getTitulo(), emitResult);
            }
            return false;
        });
    }

    public void bufferEvent(long eventId, Notificacion objNotificacion) {
        this.buffer.addLast(new EventoBuffered(eventId, objNotificacion));
        while (this.buffer.size() > MAX_BUFFER_SIZE) {
            this.buffer.pollFirst();
        }
    }

    @Override
    public Flux<EventoNotificado> obtenerStream() {
        return this.sink.asFlux();
    }

    @Override
    public Flux<EventoNotificado> obtenerStreamPorEscaner(Long idEscaner) {
        return this.sink.asFlux()
                .filter(evento -> idEscaner.equals(evento.getNotificacion().getIdEscaner()));
    }

    /**
     * Recupera eventos del buffer que ocurrieron después del eventId dado.
     * Usado para reconexiones con Last-Event-Id.
     */
    public List<EventoBuffered> obtenerEventosDesde(long fromEventId) {
        return this.buffer.stream()
                .filter(e -> e.getEventId() > fromEventId)
                .toList();
    }

    @Getter
    @AllArgsConstructor
    public static class EventoBuffered {
        private final long eventId;
        private final Notificacion notificacion;
    }
}
