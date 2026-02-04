package com.metradingplat.notification_service.domain.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notificacion {
    private String id;
    private String tipo;           // "LOG", "SIGNAL", "ORDER", "ALERT"
    private String nivel;          // "INFO", "WARN", "ERROR", "DEBUG"
    private String titulo;
    private String mensaje;
    private Long idEscaner;
    private String symbol;
    private String categoria;
    private LocalDateTime timestamp;
    private String metadatos;
}
