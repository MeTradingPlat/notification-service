package com.metradingplat.notification_service.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Filtro de Rate Limiting para endpoints SSE.
 * Limita a 10 conexiones SSE por IP cada minuto.
 */
@Component
public class RateLimitFilter implements Filter {

    // Cache de buckets por IP (expira después de 10 minutos de inactividad)
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Solo aplicar rate limiting a endpoints SSE
        String path = httpRequest.getRequestURI();
        if (!path.contains("/stream")) {
            chain.doFilter(request, response);
            return;
        }

        // Obtener IP del cliente
        String clientIp = getClientIP(httpRequest);

        // Obtener o crear bucket para esta IP
        Bucket bucket = cache.get(clientIp, k -> createNewBucket());

        // Verificar si hay tokens disponibles
        if (bucket.tryConsume(1)) {
            // Permitir la petición
            chain.doFilter(request, response);
        } else {
            // Rate limit excedido
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Demasiadas conexiones SSE. Intenta de nuevo en 1 minuto.\"}"
            );
        }
    }

    /**
     * Crea un nuevo bucket con límite de 10 peticiones por minuto.
     */
    private Bucket createNewBucket() {
        // Límite: 10 conexiones SSE por minuto
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
