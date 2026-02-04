package com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.mapper;

import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.DTOAnswer.NotificacionDTORespuesta;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-03T23:22:03-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class NotificacionMapperInfraestructuraDominioImpl implements NotificacionMapperInfraestructuraDominio {

    @Override
    public NotificacionDTORespuesta mappearDeNotificacionARespuesta(Notificacion notificacion) {
        if ( notificacion == null ) {
            return null;
        }

        NotificacionDTORespuesta notificacionDTORespuesta = new NotificacionDTORespuesta();

        notificacionDTORespuesta.setCategoria( notificacion.getCategoria() );
        notificacionDTORespuesta.setId( notificacion.getId() );
        notificacionDTORespuesta.setIdEscaner( notificacion.getIdEscaner() );
        notificacionDTORespuesta.setMensaje( notificacion.getMensaje() );
        notificacionDTORespuesta.setNivel( notificacion.getNivel() );
        notificacionDTORespuesta.setSymbol( notificacion.getSymbol() );
        notificacionDTORespuesta.setTimestamp( notificacion.getTimestamp() );
        notificacionDTORespuesta.setTipo( notificacion.getTipo() );
        notificacionDTORespuesta.setTitulo( notificacion.getTitulo() );

        return notificacionDTORespuesta;
    }
}
