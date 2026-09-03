# Arquitectura — versión microservicios

Esta es la primera iteración de la migración: **partir el monolito en servicios
con base de datos propia y un gateway**, sin cambiar funcionalidad y sin
introducir todavía un message broker. Toda la comunicación entre servicios es
**REST síncrono** con Resilience4j (circuit breaker + retry).

## 1. Qué se llevó cada servicio

El monolito tenía todo bajo `com.example.tiendajuegos`. El corte fue por los
paquetes que ya existían:

| En el monolito original | Ahora vive en |
|---|---|
| `security/JwtService`, `JwtAuthenticationFilter` | `common-security` (RS256) |
| `security/*` (TOTP, refresh, lockout, OAuth2), `api/controller/AuthApiController`, `TotpApiController` | **auth-service** |
| `model/Usuario` (parte perfil), `service/UsuarioService`, `api/controller/UsuarioApiController`, `AdminStatsApiController` | **usuarios-service** |
| `model/{Juego,Seccion}`, `service/{JuegoService,SeccionService,ImagenStorageService}`, `config/GeneradorPortadas`, `api/controller/{JuegoApiController,SeccionApiController}` | **catalogo-service** |
| `config/SecurityConfig` (parte ruteo + CORS) | **api-gateway** |
| `controller/*` (vistas Thymeleaf) | se descartaron: el frontend React las reemplaza |

## 2. El corte de `Usuario` (la decisión central)

La entidad `Usuario` del monolito mezclaba **identidad** (para autenticar) y
**perfil / autorización** (para el negocio). Se partió en dos:

| Campo del `Usuario` original | Ahora en | Por qué |
|---|---|---|
| `username` | **ambos** (clave natural compartida, inmutable) | es el "join key" entre servicios |
| `password` (hash BCrypt) | auth-service (`Credential`) | solo auth compara contraseñas |
| `totpSecret`, `totpHabilitado` | auth-service | segundo factor = autenticación |
| `intentosFallidos`, `bloqueadoHasta` | auth-service | lockout = autenticación |
| `email` | usuarios-service (canónico); **copia** en auth-service | auth lo necesita para casar la cuenta en el login con Google |
| `nombreCompleto` | usuarios-service | dato de perfil |
| `rol` | usuarios-service | autorización de negocio |
| `habilitado` | usuarios-service | lo prende/apaga el ADMIN |

Consecuencia: **el login es una operación entre dos servicios**. auth-service
valida la contraseña contra su tabla `Credential`, después le pregunta a
usuarios-service el `rol` y el `habilitado` (`GET /internal/usuarios/by-username/{u}`),
y recién ahí emite el JWT (que lleva `uid`, `sub`=username y `rol`).

`Juego` también perdió su FK: `@ManyToOne Usuario vendedor` pasó a
`String vendedorUsername`. El vendedor del DTO se resuelve contra
usuarios-service; `puedeEditar` se calcula con el `username`/`rol` del JWT.

## 3. Autenticación: JWT RS256

El monolito firmaba con un secreto HMAC (`app.jwt.secret`). Repartir ese secreto
por 4 servicios sería peligroso, así que se pasó a **par asimétrico RSA**:

- `auth-service` tiene `keys/jwt-private.pem` → es el **único** que puede emitir
  tokens (`JwtService.generar` tira excepción si no hay clave privada).
- gateway + usuarios-service + catalogo-service tienen solo `keys/jwt-public.pem`
  → **validan** (firma + `iss=gamestore-auth` + expiración).

Doble validación a propósito: el **gateway** rechaza rápido lo que no trae token
(`/api/**` salvo `/api/auth/**`), y **cada servicio vuelve a validar** por su
cuenta (defensa en profundidad; un servicio no confía en que "alguien" ya filtró).

El gateway además **borra de toda request entrante** los headers `X-Internal-Token`
y `X-Auth-*`, para que un cliente no pueda spoofearlos, y reinyecta `X-Auth-*` con
lo que dice el token validado (por si un servicio futuro quiere la identidad sin
re-parsear).

## 4. API interna entre servicios

Cada servicio expone, además de su API pública (`/api/**`, vía gateway),
endpoints `/internal/**` que **solo** consumen otros servicios:

| Endpoint | Lo expone | Lo llama |
|---|---|---|
| `GET /internal/usuarios/by-username/{u}` | usuarios | auth (login, refresh, /me) |
| `GET /internal/usuarios?usernames=a,b` | usuarios | catalogo (vendedores del catálogo) |
| `POST /internal/usuarios/google` | usuarios | auth (login con Google) |
| `POST /internal/credenciales`, `PUT .../password`, `DELETE .../{u}` | auth | usuarios (ABM del ADMIN) |
| `GET /internal/stats` | catalogo | usuarios (dashboard) |
| `GET /internal/juegos/count-by-vendedor/{u}` | catalogo | usuarios (chequeo previo al borrado) |

Protección: `InternalTokenFilter` exige el header `X-Internal-Token` con un
secreto compartido (`app.internal.secret`). Es deliberadamente simple — en un
cluster real esto se refuerza con network policies / mTLS y el token queda como
segunda barrera.

## 5. Flujos entre servicios

**Login** (`POST /api/auth/login`):
```
gateway → auth-service
            ├─ valida password contra Credential (BCrypt, + lockout por eventos)
            ├─ GET usuarios-service /internal/usuarios/by-username/{u}  → rol, habilitado
            ├─ si !habilitado → 401 ;  si 2FA → exige totpCode
            └─ firma JWT(uid, sub, rol) + crea refresh token (tabla propia)
```
Si usuarios-service no responde: Resilience4j reintenta y, si el circuito abre,
auth-service devuelve **503** (el login falla explícito, no cuelga).

**Listado del catálogo** (`GET /api/juegos`):
```
gateway → catalogo-service
            ├─ trae los juegos (base propia)
            ├─ GET usuarios-service /internal/usuarios?usernames=...  (UNA llamada, no N)
            └─ arma cada JuegoDTO: vendedor + puedeEditar (del JWT)
```
Si usuarios-service no responde: fallback del circuit breaker → el vendedor sale
"degradado" (solo el username) y el catálogo **no se rompe**.

**Alta de usuario** (`POST /api/usuarios`, ADMIN) — escritura distribuida:
```
gateway → usuarios-service
            ├─ valida unicidad (username/email) y guarda el PERFIL
            ├─ POST auth-service /internal/credenciales  (crea el hash)
            └─ si ese POST falla → BORRA el perfil (compensación) y devuelve error
```
**No hay saga ni outbox todavía**: la compensación es un `catch` a mano. Es
suficiente para la demo y deja el problema a la vista. El siguiente paso natural
es publicar un evento `UsuarioCreado` y que auth-service reaccione.

## 6. Bases de datos

Una por servicio. En `dev` cada una es **H2 en memoria** (se recrea en cada
arranque, con datos de ejemplo sembrados por servicio: auth siembra credenciales,
usuarios siembra perfiles, catalogo siembra secciones+juegos; coinciden por
`username`). En `docker` cada servicio tiene su **Postgres** (contenedor +
volumen). Ningún servicio puede leer la base de otro.

## 7. Compromisos conocidos de esta iteración

| Tema | Estado hoy | Próximo paso |
|---|---|---|
| Escrituras distribuidas (alta de usuario) | compensación en `catch` | evento `UsuarioCreado` + outbox |
| Comunicación | REST síncrono | broker (Kafka/RabbitMQ) para lo asíncrono |
| Imágenes de juegos | disco local de catalogo-service | media-service + S3/Cloudinary |
| `email` duplicado (auth + usuarios) | copia, se acepta | dueño único + propagación por evento |
| Secreto interno (`X-Internal-Token`) | header con secreto compartido | mTLS / service mesh |
| Observabilidad | `/actuator/health` por servicio | OpenTelemetry + trazas distribuidas |
| Esquema de BD | `ddl-auto=update` (como el monolito) | Flyway/Liquibase por servicio |
| Descubrimiento | URLs fijas por config/env | service discovery o DNS de Kubernetes |

## 8. Mapa de puertos

| Servicio | dev (local) | docker (interno) | Publicado |
|---|---|---|---|
| api-gateway | 8080 | 8080 | **8080** |
| auth-service | 8081 | 8081 | — |
| usuarios-service | 8082 | 8082 | — |
| catalogo-service | 8083 | 8083 | — |
| auth-db / usuarios-db / catalogo-db | — | 5432 (cada uno) | — |
