# Notification Service

Microservicio de notificaciones para la plataforma Metradingplat. Gestiona el envío y distribución de notificaciones en tiempo real a través de Server-Sent Events (SSE) y consume eventos desde Kafka.

## Tecnologías

- Java 21
- Spring Boot 3.5.0
- Spring Cloud 2025.0.0
- Apache Kafka
- Eureka Client (Service Discovery)
- MapStruct
- Lombok
- Docker

## Arquitectura

El servicio implementa arquitectura hexagonal:

```
src/main/java/com/metradingplat/notification_service/
├── application/          # Puertos de entrada y salida
├── domain/               # Modelos y casos de uso
└── infrastructure/       # Adaptadores (REST, Kafka, SSE)
```

## Requisitos

- JDK 21+
- Maven 3.9+
- Apache Kafka
- Eureka Server

## Configuración

El servicio se ejecuta en el puerto `8085` por defecto.

Variables de entorno principales:
- `SPRING_PROFILES_ACTIVE`: Perfil activo (`dev`, `prod`)
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: URL del servidor Eureka
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Servidores Kafka

## Ejecución

### Local

```bash
./mvnw spring-boot:run
```

### Docker

```bash
docker build -t notification-service .
docker run -p 8085:8085 notification-service
```

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/notificaciones/stream` | SSE stream de notificaciones |
| GET | `/actuator/health` | Health check |

## Funcionalidades

- **SSE (Server-Sent Events)**: Emisión de notificaciones en tiempo real
- **Kafka Consumer**: Escucha eventos de otros microservicios
- **Tipos de notificación**: INFO, WARNING, ERROR, SUCCESS
- **Niveles**: BAJO, MEDIO, ALTO, CRITICO
- **Internacionalización**: Soporte para ES/EN

## Testing

```bash
./mvnw test
```
