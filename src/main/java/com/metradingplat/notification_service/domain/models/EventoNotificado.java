package com.metradingplat.notification_service.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Evento ya numerado y encolado en el buffer de reconexion al momento de
// emitirse (ver SseEmitterAdapter.emitir). El id se asigna UNA sola vez por
// evento -- antes se asignaba dentro del .map() por-suscriptor del endpoint
// SSE, asi que un evento emitido sin ningun suscriptor activo jamas se
// numeraba ni se buffereaba, y el replay por Last-Event-Id no tenia nada que
// reenviar: las senales generadas durante una desconexion se perdian para
// siempre (confirmado en vivo: 270 FAIL_CANCELLED el 2026-08-24, las 169
// senales del primer ciclo del escaner 'volumen test' nunca llegaron al
// frontend hasta recargar la pagina).
@Getter
@AllArgsConstructor
public class EventoNotificado {
    private final long eventId;
    private final Notificacion notificacion;
}
