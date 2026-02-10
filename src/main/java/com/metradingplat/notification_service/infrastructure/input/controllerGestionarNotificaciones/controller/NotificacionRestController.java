package com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.controller;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.DTOAnswer.NotificacionDTORespuesta;
import com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.mapper.NotificacionMapperInfraestructuraDominio;
import com.metradingplat.notification_service.infrastructure.output.sse.SseEmitterAdapter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionRestController {

    private final GestionarNotificacionesCUIntPort objGestionarNotificacionesCUInt;
    private final NotificacionMapperInfraestructuraDominio objMapper;
    private final SseEmitterAdapter objSseEmitter;

    /**
     * Stream SSE de todas las notificaciones en tiempo real.
     * Incluye heartbeat cada 30s para mantener la conexión viva con Cloudflare.
     * Soporta Last-Event-Id para recuperar notificaciones perdidas en reconexión.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<NotificacionDTORespuesta>> streamNotificaciones(
            @RequestHeader(value = "Last-Event-Id", required = false) String lastEventIdHeader,
            @RequestParam(value = "lastEventId", required = false) String lastEventIdParam) {

        // Header tiene prioridad (auto-reconnect del browser), luego query param (reconexión manual)
        String lastEventId = lastEventIdHeader != null ? lastEventIdHeader : lastEventIdParam;

        // Heartbeat: primero inmediato (evita timeout async de Tomcat), luego cada 30s
        Flux<ServerSentEvent<NotificacionDTORespuesta>> heartbeat = Flux.concat(
                Flux.just(ServerSentEvent.<NotificacionDTORespuesta>builder().comment("connected").build()),
                Flux.interval(Duration.ofSeconds(30))
                        .map(i -> ServerSentEvent.<NotificacionDTORespuesta>builder()
                                .comment("heartbeat")
                                .build()));

        // Eventos perdidos durante reconexión (si hay Last-Event-Id)
        Flux<ServerSentEvent<NotificacionDTORespuesta>> missed = Flux.empty();
        if (lastEventId != null && !lastEventId.isEmpty()) {
            try {
                long fromId = Long.parseLong(lastEventId);
                missed = Flux.fromIterable(this.objSseEmitter.obtenerEventosDesde(fromId))
                        .map(entry -> ServerSentEvent.<NotificacionDTORespuesta>builder(
                                this.objMapper.mappearDeNotificacionARespuesta(entry.getNotificacion()))
                                .id(String.valueOf(entry.getEventId()))
                                .build());
            } catch (NumberFormatException e) {
                // lastEventId inválido, ignorar
            }
        }

        // Stream en vivo de notificaciones con IDs
        Flux<ServerSentEvent<NotificacionDTORespuesta>> live = this.objGestionarNotificacionesCUInt
                .obtenerStreamNotificaciones()
                .map(this.objMapper::mappearDeNotificacionARespuesta)
                .map(n -> ServerSentEvent.<NotificacionDTORespuesta>builder(n)
                        .id(String.valueOf(this.objSseEmitter.getNextEventId()))
                        .build());

        // Primero envía eventos perdidos, luego mezcla live + heartbeat
        return Flux.concat(missed, Flux.merge(heartbeat, live));
    }

    /**
     * Stream SSE de notificaciones filtradas por escaner.
     * Incluye heartbeat cada 30s y soporte para Last-Event-Id.
     */
    @GetMapping(value = "/stream/escaner/{idEscaner}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<NotificacionDTORespuesta>> streamNotificacionesPorEscaner(
            @PathVariable("idEscaner") @NotNull @Positive Long idEscaner,
            @RequestHeader(value = "Last-Event-Id", required = false) String lastEventIdHeader,
            @RequestParam(value = "lastEventId", required = false) String lastEventIdParam) {

        String lastEventId = lastEventIdHeader != null ? lastEventIdHeader : lastEventIdParam;

        Flux<ServerSentEvent<NotificacionDTORespuesta>> heartbeat = Flux.concat(
                Flux.just(ServerSentEvent.<NotificacionDTORespuesta>builder().comment("connected").build()),
                Flux.interval(Duration.ofSeconds(30))
                        .map(i -> ServerSentEvent.<NotificacionDTORespuesta>builder()
                                .comment("heartbeat")
                                .build()));

        Flux<ServerSentEvent<NotificacionDTORespuesta>> missed = Flux.empty();
        if (lastEventId != null && !lastEventId.isEmpty()) {
            try {
                long fromId = Long.parseLong(lastEventId);
                missed = Flux.fromIterable(this.objSseEmitter.obtenerEventosDesde(fromId))
                        .filter(entry -> idEscaner.equals(entry.getNotificacion().getIdEscaner()))
                        .map(entry -> ServerSentEvent.<NotificacionDTORespuesta>builder(
                                this.objMapper.mappearDeNotificacionARespuesta(entry.getNotificacion()))
                                .id(String.valueOf(entry.getEventId()))
                                .build());
            } catch (NumberFormatException e) {
                // lastEventId inválido, ignorar
            }
        }

        Flux<ServerSentEvent<NotificacionDTORespuesta>> live = this.objGestionarNotificacionesCUInt
                .obtenerStreamNotificacionesPorEscaner(idEscaner)
                .map(this.objMapper::mappearDeNotificacionARespuesta)
                .map(n -> ServerSentEvent.<NotificacionDTORespuesta>builder(n)
                        .id(String.valueOf(this.objSseEmitter.getNextEventId()))
                        .build());

        return Flux.concat(missed, Flux.merge(heartbeat, live));
    }
}
