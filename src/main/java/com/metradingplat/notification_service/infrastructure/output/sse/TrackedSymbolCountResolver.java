package com.metradingplat.notification_service.infrastructure.output.sse;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * El tope del buffer de reconexion SSE (SseEmitterAdapter) arranca en un
 * valor fijo generoso -- este lo reemplaza por el numero real de simbolos
 * que marketdata-service tiene rastreados, el techo real de cuantas señales
 * distintas puede haber a la vez (no puede haber mas señales simultaneas
 * que simbolos que existen). Reintenta en el fondo si marketdata-service
 * todavia no esta arriba cuando este servicio arranca.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TrackedSymbolCountResolver {

    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);

    private final SseEmitterAdapter sseEmitterAdapter;
    private final WebClient.Builder webClientBuilder;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "tracked-symbol-count-resolver");
        t.setDaemon(true);
        return t;
    });

    @Value("${app.marketdata-service.url}")
    private String marketdataServiceUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void resolveOnStartup() {
        this.scheduler.execute(this::attempt);
    }

    private void attempt() {
        try {
            List<?> symbols = this.webClientBuilder.build().get()
                    .uri(this.marketdataServiceUrl + "/marketdata/symbols")
                    .header("X-Gateway-Passed", "true")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block(Duration.ofSeconds(30));
            int total = symbols == null ? 0 : symbols.size();
            if (total > 0) {
                this.sseEmitterAdapter.setMaxBufferSize(total);
                log.info("Buffer de reconexion SSE: tope ajustado a {} simbolos rastreados", total);
                return;
            }
        } catch (Exception e) {
            log.warn("No se pudo resolver el universo de simbolos, reintentando en {}s: {}",
                    RETRY_DELAY.getSeconds(), e.getMessage());
        }
        this.scheduler.schedule(this::attempt, RETRY_DELAY.getSeconds(), TimeUnit.SECONDS);
    }
}
