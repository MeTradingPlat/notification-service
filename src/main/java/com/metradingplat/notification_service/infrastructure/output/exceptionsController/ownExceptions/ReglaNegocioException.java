package com.metradingplat.notification_service.infrastructure.output.exceptionsController.ownExceptions;

import com.metradingplat.notification_service.infrastructure.output.exceptionsController.exceptionStructure.CodigoError;

public class ReglaNegocioException extends BaseException {
    public ReglaNegocioException(String llaveMensaje, Object... args) {
        super(CodigoError.VIOLACION_REGLA_DE_NEGOCIO, llaveMensaje, args);
    }
}
