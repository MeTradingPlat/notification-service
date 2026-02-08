# Configuración de Seguridad - Notification Service

## Protecciones Implementadas

### 1. Rate Limiting (Bucket4j)

**¿Qué hace?**
- Limita a **10 conexiones SSE por IP cada minuto**
- Previene abuso y ataques de denegación de servicio
- Se aplica automáticamente a todos los endpoints `/stream/*`

**Archivo**: `RateLimitFilter.java`

**Configuración**:
```java
// Límite: 10 conexiones SSE por minuto
Bandwidth limit = Bandwidth.builder()
        .capacity(10)
        .refillIntervally(10, Duration.ofMinutes(1))
        .build();
```

**Respuesta cuando se excede el límite**:
```json
{
  "error": "Rate limit exceeded",
  "message": "Demasiadas conexiones SSE. Intenta de nuevo en 1 minuto."
}
```
HTTP Status: `429 Too Many Requests`

---

### 2. Autenticación con Token

**¿Qué hace?**
- Requiere un token de autenticación para conectarse a SSE
- El token puede enviarse de dos formas:
  - Header: `X-Auth-Token: tu_token_aqui`
  - Query parameter: `?token=tu_token_aqui`

**Archivo**: `SseAuthenticationFilter.java`

**Configuración en `application.yml`**:
```yaml
sse:
  auth:
    enabled: true  # false para deshabilitar (solo desarrollo)
    shared-secret: "tu_token_secreto_aqui"
```

**Variables de entorno (recomendado para producción)**:
```bash
export SSE_AUTH_ENABLED=true
export SSE_AUTH_TOKEN="token_super_secreto_production_12345"
```

**Respuesta cuando falta autenticación**:
```json
{
  "error": "Unauthorized",
  "message": "Token de autenticación inválido o ausente. Envía el token en el header 'X-Auth-Token' o como query parameter 'token'."
}
```
HTTP Status: `401 Unauthorized`

---

### 3. CORS Configurado

**Orígenes permitidos**:
- `https://metradingplat.com`
- `https://www.metradingplat.com`
- `https://sse.metradingplat.com` ⬅️ **Subdominio para SSE sin timeout de Cloudflare**
- `http://localhost:4200` (desarrollo)

**Archivo**: `NotificacionRestController.java`

---

## Orden de Ejecución de Filtros

Los filtros se ejecutan en este orden (configurado en `FilterConfiguration.java`):

1. **SseAuthenticationFilter** (Orden 1)
   - Valida autenticación
   - Si falla: retorna 401 Unauthorized

2. **RateLimitFilter** (Orden 2)
   - Valida rate limit
   - Si falla: retorna 429 Too Many Requests

3. **Controller** (NotificacionRestController)
   - Procesa la petición SSE

---

## Configuración en Frontend

### Environment Variables

**`environment.ts` (desarrollo)**:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8085/api',
  sseAuthToken: 'CHANGE_THIS_IN_PRODUCTION'
};
```

**`environment.prod.ts` (producción)**:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://sse.metradingplat.com/api',  // ⬅️ Usar subdominio SSE
  sseAuthToken: 'CHANGE_THIS_IN_PRODUCTION'     // ⬅️ Debe coincidir con backend
};
```

### Servicio SSE

El servicio `NotificacionSseService` automáticamente envía el token:

```typescript
// El token se agrega automáticamente como query parameter
this.eventSource = new EventSource(
  `${this.apiUrl}/stream?token=${encodeURIComponent(token)}`
);
```

---

## Setup de Producción

### Configurar Token (Backend y Frontend usan el mismo)

**GitHub Secrets (Recomendado para CI/CD)**

Crear estos 2 secrets en GitHub (Settings → Secrets → Actions):

1. **SSE_AUTH_ENABLED** = `true`
2. **SSE_AUTH_TOKEN** = `tu_token_secreto_aqui` (se usa automáticamente en backend y frontend)

**O Manual con Variables de Entorno**

```bash
export SSE_AUTH_ENABLED=true
export SSE_AUTH_TOKEN="token_super_secreto_production_12345"
```

**O en application.yml (Backend)**
```yaml
sse:
  auth:
    enabled: true
    shared-secret: "token_super_secreto_production_12345"
```

**Nota**: El frontend obtiene el token automáticamente durante el build de Docker usando el mismo secret `SSE_AUTH_TOKEN`.

### Paso 3: Usar Subdominio SSE en Cloudflare

1. Crear registro DNS en Cloudflare:
   - Type: `CNAME`
   - Name: `sse`
   - Target: `metradingplat.com`
   - **Proxy status: DNS only** ⛅ (nube gris)

2. Esto crea: `sse.metradingplat.com` sin timeout de Cloudflare

### Paso 4: Reiniciar Servicios

```bash
# Reiniciar notification-service
sudo systemctl restart notification-service

# O con Docker
docker restart notification-service
```

---

## Testing

### Variables para Testing

Primero, define el token como variable:
```bash
export SSE_TOKEN="tu_token_secreto_aqui"
```

### Test 1: Sin Token (debe fallar)
```bash
curl -N https://sse.metradingplat.com/api/notificaciones/stream/escaner/1
```
**Esperado**: 401 Unauthorized
```json
{"error":"Unauthorized","message":"Token de autenticación inválido o ausente..."}
```

### Test 2: Con Token Válido
```bash
curl -N "https://sse.metradingplat.com/api/notificaciones/stream/escaner/1?token=${SSE_TOKEN}"
```
**Esperado**: Stream SSE funcionando ✅

### Test 3: Rate Limit
Hacer 11 conexiones rápidas desde la misma IP:
```bash
for i in {1..11}; do
  curl -N "https://sse.metradingplat.com/api/notificaciones/stream/escaner/1?token=${SSE_TOKEN}" &
done
```
**Esperado**: La petición #11 debe retornar 429 Too Many Requests
```json
{"error":"Rate limit exceeded","message":"Demasiadas conexiones SSE. Intenta de nuevo en 1 minuto."}
```

---

## Mejoras Futuras (TODOs)

### 1. Migrar a JWT (JSON Web Tokens)

**Ventajas**:
- Tokens por usuario (no un token compartido)
- Expiración automática
- Claims personalizados (permisos, roles, etc.)

**Implementación**:
```java
// Reemplazar isValidToken() con:
private boolean isValidToken(String token) {
    try {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();

        // Verificar expiración, permisos, etc.
        return true;
    } catch (JwtException e) {
        return false;
    }
}
```

### 2. Integrar con Spring Security

**Ventajas**:
- Gestión centralizada de autenticación
- Soporte OAuth2, SAML, etc.
- Roles y permisos

**Implementación**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 3. Rate Limiting más sofisticado

**Opciones**:
- Rate limit por usuario (no solo por IP)
- Rate limits diferentes según plan (free, premium, enterprise)
- Rate limiting distribuido con Redis (para múltiples instancias)

---

## Troubleshooting

### Problema: 401 Unauthorized
- **Causa**: Token incorrecto o ausente
- **Solución**: Verificar que el token en frontend coincida con backend

### Problema: 429 Too Many Requests
- **Causa**: Demasiadas conexiones desde la misma IP
- **Solución**: Esperar 1 minuto o ajustar el límite en `RateLimitFilter.java`

### Problema: CORS errors
- **Causa**: Origen no permitido
- **Solución**: Agregar origen en `@CrossOrigin` del controlador

### Problema: EventSource error después de ~100 segundos
- **Causa**: Cloudflare timeout
- **Solución**: Usar subdominio `sse.metradingplat.com` con proxy deshabilitado

---

## Contacto

Para preguntas sobre esta configuración, contactar al equipo de desarrollo.
