package com.metradingplat.notification_service.domain.usecases;

import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.application.output.EmitirNotificacionIntPort;
import com.metradingplat.notification_service.domain.models.Notificacion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Slf4j
public class GestionarNotificacionesCUAdapter implements GestionarNotificacionesCUIntPort {

    private final EmitirNotificacionIntPort objEmitirNotificacionIntPort;

    @Override
    public void procesarNotificacionLog(Notificacion objNotificacion) {
        log.debug("Procesando notificacion: [{}] {}", objNotificacion.getNivel(), objNotificacion.getMensaje());

        objNotificacion.setTipo("LOG");
        if (objNotificacion.getTitulo() == null || objNotificacion.getTitulo().isEmpty()) {
            objNotificacion.setTitulo(generarTitulo(objNotificacion));
        }

        this.objEmitirNotificacionIntPort.emitir(objNotificacion);
    }

    @Override
    public void procesarNotificacionEstadoEscaner(Notificacion objNotificacion) {
        log.info("Procesando notificacion de estado escaner: [{}] {}",
            objNotificacion.getIdEscaner(), objNotificacion.getMensaje());

        objNotificacion.setTipo("SCANNER_STATE");
        this.objEmitirNotificacionIntPort.emitir(objNotificacion);
    }

    @Override
    public Flux<Notificacion> obtenerStreamNotificaciones() {
        return this.objEmitirNotificacionIntPort.obtenerStream();
    }

    @Override
    public Flux<Notificacion> obtenerStreamNotificacionesPorEscaner(Long idEscaner) {
        return this.objEmitirNotificacionIntPort.obtenerStreamPorEscaner(idEscaner);
    }

    private String generarTitulo(Notificacion notificacion) {
        if (notificacion.getSymbol() != null && !notificacion.getSymbol().isEmpty()) {
            return String.format("[%s] %s", notificacion.getNivel(), notificacion.getSymbol());
        }
        return String.format("[%s] %s", notificacion.getNivel(), notificacion.getCategoria());
    }
}
