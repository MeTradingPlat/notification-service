package com.metradingplat.notification_service.infrastructure.input.kafkaGestionarNotificaciones.DTOPetition;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogNotificacionDTOPeticion {
    private Long idRegistroLog;
    private String servicioOrigen;
    private String nivel;
    private String mensaje;
    private Long idEscaner;
    private String symbol;
    private String categoria;
    private LocalDateTime timestamp;
}
