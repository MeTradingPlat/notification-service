package com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.mappers;

import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition.LogNotificacionDTOPeticion;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-03T23:22:04-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class NotificacionKafkaMapperImpl implements NotificacionKafkaMapper {

    @Override
    public Notificacion deDTOADominio(LogNotificacionDTOPeticion dto) {
        if ( dto == null ) {
            return null;
        }

        Notificacion.NotificacionBuilder notificacion = Notificacion.builder();

        notificacion.categoria( dto.getCategoria() );
        notificacion.idEscaner( dto.getIdEscaner() );
        notificacion.mensaje( dto.getMensaje() );
        notificacion.nivel( dto.getNivel() );
        notificacion.symbol( dto.getSymbol() );
        notificacion.timestamp( dto.getTimestamp() );

        notificacion.id( String.valueOf(dto.getIdRegistroLog()) );
        notificacion.tipo( "LOG" );

        return notificacion.build();
    }
}
