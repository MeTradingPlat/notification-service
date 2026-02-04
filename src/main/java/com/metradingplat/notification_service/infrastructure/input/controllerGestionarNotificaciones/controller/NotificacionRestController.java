package com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.DTOAnswer.NotificacionDTORespuesta;
import com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.mapper.NotificacionMapperInfraestructuraDominio;

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

    /**
     * Stream SSE de todas las notificaciones en tiempo real.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificacionDTORespuesta> streamNotificaciones() {
        return this.objGestionarNotificacionesCUInt.obtenerStreamNotificaciones()
                .map(this.objMapper::mappearDeNotificacionARespuesta);
    }

    /**
     * Stream SSE de notificaciones filtradas por escaner.
     */
    @GetMapping(value = "/stream/escaner/{idEscaner}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificacionDTORespuesta> streamNotificacionesPorEscaner(
            @PathVariable("idEscaner") @NotNull @Positive Long idEscaner) {
        return this.objGestionarNotificacionesCUInt.obtenerStreamNotificacionesPorEscaner(idEscaner)
                .map(this.objMapper::mappearDeNotificacionARespuesta);
    }
}
