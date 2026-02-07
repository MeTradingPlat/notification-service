package com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoEscanerEventoDTOPeticion {
    private Long idEscaner;
    private String nombreEscaner;
    private String estadoAnterior;
    private String estadoNuevo;
    private String razon;
    private String timestamp;
}
