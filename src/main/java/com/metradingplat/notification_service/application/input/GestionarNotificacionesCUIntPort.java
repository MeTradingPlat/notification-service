package com.metradingplat.notification_service.application.input;

import com.metradingplat.notification_service.domain.models.Notificacion;

public interface GestionarNotificacionesCUIntPort {

    void procesarNotificacionLog(Notificacion objNotificacion);

    void procesarNotificacionEstadoEscaner(Notificacion objNotificacion);
}
