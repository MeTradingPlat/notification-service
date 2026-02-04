package com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.metradingplat.notification_service.domain.models.Notificacion;
import com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition.LogNotificacionDTOPeticion;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificacionKafkaMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(dto.getIdRegistroLog()))")
    @Mapping(target = "tipo", constant = "LOG")
    @Mapping(target = "titulo", ignore = true)
    @Mapping(target = "metadatos", ignore = true)
    Notificacion deDTOADominio(LogNotificacionDTOPeticion dto);
}
