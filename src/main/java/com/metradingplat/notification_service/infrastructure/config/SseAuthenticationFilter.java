package com.metradingplat.notification_service.infrastructure.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro de autenticación simple para endpoints SSE.
 * Verifica que el cliente envíe un token válido en el header "X-Auth-Token" o como query parameter "token".
 *
 * NOTA: Esta es una implementación básica. Para producción, considera usar:
 * - JWT (JSON Web Tokens) para autenticación stateless
 * - Spring Security con OAuth2/JWT
 * - Integración con tu sistema de autenticación existente
 */
@Component
public class SseAuthenticationFilter implements Filter {

    @Value("${sse.auth.enabled:true}")
    private boolean authEnabled;

    @Value("${sse.auth.shared-secret:CHANGE_THIS_IN_PRODUCTION}")
    private String sharedSecret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Solo aplicar autenticación a endpoints SSE
        String path = httpRequest.getRequestURI();
        if (!path.contains("/stream")) {
            chain.doFilter(request, response);
            return;
        }

        // Si autenticación está deshabilitada (solo para desarrollo), permitir todo
        if (!authEnabled) {
            chain.doFilter(request, response);
            return;
        }

        // Obtener token del header o query parameter
        String token = httpRequest.getHeader("X-Auth-Token");
        if (token == null || token.isEmpty()) {
            token = httpRequest.getParameter("token");
        }

        // Validar token
        if (isValidToken(token)) {
            // Token válido, permitir la petición
            chain.doFilter(request, response);
        } else {
            // Token inválido o ausente
            httpResponse.setStatus(401); // Unauthorized
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"Token de autenticación inválido o ausente. Envía el token en el header 'X-Auth-Token' o como query parameter 'token'.\"}"
            );
        }
    }

    /**
     * Valida el token.
     *
     * IMPLEMENTACIÓN BÁSICA: Compara con un secreto compartido.
     *
     * TODO: Reemplazar con validación JWT o integración con tu sistema de auth.
     * Ejemplo con JWT:
     * - Decodificar el token JWT
     * - Verificar firma
     * - Verificar expiración
     * - Verificar claims (permisos, usuario, etc.)
     */
    private boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Implementación básica: comparar con secreto compartido
        // TODO: Reemplazar con JWT validation o llamada a servicio de autenticación
        return sharedSecret.equals(token);
    }
}
