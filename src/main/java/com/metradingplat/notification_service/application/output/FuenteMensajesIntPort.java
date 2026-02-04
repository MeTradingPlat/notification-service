package com.metradingplat.notification_service.application.output;

import java.util.Locale;

public interface FuenteMensajesIntPort {
    String obtenerMensaje(String llaveMensaje, Locale locale, Object... args);

    String internacionalizarMensaje(String llaveMensaje, Object... args);
}
