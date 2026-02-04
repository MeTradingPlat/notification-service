package com.metradingplat.notification_service.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metradingplat.notification_service.application.input.GestionarNotificacionesCUIntPort;
import com.metradingplat.notification_service.application.output.EmitirNotificacionIntPort;
import com.metradingplat.notification_service.domain.usecases.GestionarNotificacionesCUAdapter;

@Configuration
public class BeanConfigurations {

    @Bean
    public GestionarNotificacionesCUIntPort gestionarNotificacionesCUIntPort(
            EmitirNotificacionIntPort objEmitirNotificacionIntPort) {
        return new GestionarNotificacionesCUAdapter(objEmitirNotificacionIntPort);
    }
}
