package com.metradingplat.notification_service.infrastructure.input.controllerGestionarNotificaciones.DTOAnswer;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionDTORespuesta {
    private String id;
    private String tipo;
    private String nivel;
    private String titulo;
    private String mensaje;
    private Long idEscaner;
    private String symbol;
    private String categoria;
    private LocalDateTime timestamp;
}
