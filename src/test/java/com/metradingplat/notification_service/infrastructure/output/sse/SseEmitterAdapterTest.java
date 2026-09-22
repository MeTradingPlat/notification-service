package com.metradingplat.notification_service.infrastructure.output.sse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.metradingplat.notification_service.domain.models.Notificacion;

class SseEmitterAdapterTest {

    private static Notificacion notificacion(String symbol) {
        Notificacion n = new Notificacion();
        n.setSymbol(symbol);
        return n;
    }

    @Test
    void elBufferDeReconexionNoDescartaNadaMientrasQuepaDebajoDelTope() {
        SseEmitterAdapter adapter = new SseEmitterAdapter();
        adapter.setMaxBufferSize(3);

        adapter.bufferEvent(1, notificacion("AAPL"));
        adapter.bufferEvent(2, notificacion("MSFT"));
        adapter.bufferEvent(3, notificacion("SPY"));

        assertThat(adapter.obtenerEventosDesde(0)).hasSize(3);
    }

    @Test
    void elBufferDeReconexionDescartaLosMasViejosAlPasarseDelTope() {
        SseEmitterAdapter adapter = new SseEmitterAdapter();
        adapter.setMaxBufferSize(2);

        adapter.bufferEvent(1, notificacion("AAPL"));
        adapter.bufferEvent(2, notificacion("MSFT"));
        adapter.bufferEvent(3, notificacion("SPY"));

        var restantes = adapter.obtenerEventosDesde(0);
        assertThat(restantes).hasSize(2);
        assertThat(restantes.get(0).getEventId()).isEqualTo(2);
        assertThat(restantes.get(1).getEventId()).isEqualTo(3);
    }

    @Test
    void setMaxBufferSizeCambiaElTopeUsadoPorLosProximosEventos() {
        SseEmitterAdapter adapter = new SseEmitterAdapter();
        adapter.setMaxBufferSize(1);
        adapter.bufferEvent(1, notificacion("AAPL"));

        adapter.setMaxBufferSize(3);
        adapter.bufferEvent(2, notificacion("MSFT"));
        adapter.bufferEvent(3, notificacion("SPY"));

        assertThat(adapter.obtenerEventosDesde(0)).hasSize(3);
    }

    @Test
    void elTopeArrancaEnElValorDelSinkMientrasNoSeResuelveElUniversoReal() {
        SseEmitterAdapter adapter = new SseEmitterAdapter();

        for (int i = 1; i <= 5; i++) {
            adapter.bufferEvent(i, notificacion("SYM" + i));
        }

        assertThat(adapter.obtenerEventosDesde(0)).hasSize(5);
    }
}
