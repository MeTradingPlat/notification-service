package com.metradingplat.notification_service.infrastructure.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de filtros para endpoints SSE.
 * Define el orden de ejecución:
 * 1. SseAuthenticationFilter (autenticación)
 * 2. RateLimitFilter (rate limiting)
 */
@Configuration
public class FilterConfiguration {

    /**
     * Registra el filtro de autenticación con prioridad 1 (ejecuta primero).
     */
    @Bean
    public FilterRegistrationBean<SseAuthenticationFilter> sseAuthenticationFilter(SseAuthenticationFilter filter) {
        FilterRegistrationBean<SseAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/notificaciones/stream/*");
        registration.setOrder(1); // Ejecuta primero
        registration.setName("SseAuthenticationFilter");
        return registration;
    }

    /**
     * Registra el filtro de rate limiting con prioridad 2 (ejecuta después de autenticación).
     */
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/notificaciones/stream/*");
        registration.setOrder(2); // Ejecuta después de autenticación
        registration.setName("RateLimitFilter");
        return registration;
    }
}
