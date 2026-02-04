package com.metradingplat.notification_service.infrastructure.output.exceptionsController.ownExceptions;

import lombok.Getter;

import com.metradingplat.notification_service.infrastructure.output.exceptionsController.exceptionStructure.CodigoError;

@Getter
public abstract class BaseException extends RuntimeException {
    private final CodigoError codigoError;
    private final Object[] args;

    protected BaseException(CodigoError codigoError, String llaveMensaje, Object... args) {
        super(llaveMensaje);
        this.codigoError = codigoError;
        this.args = (args == null) ? new Object[] {} : args;
    }
}
