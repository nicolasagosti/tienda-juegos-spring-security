# GameStore — versión microservicios

Migración del monolito Spring Boot original a **3 servicios Spring Boot + 2
librerías compartidas**, manteniendo el mismo frontend React y la misma
funcionalidad (roles ADMIN / VENDEDOR / COMPRADOR, JWT + refresh tokens,
2FA TOTP, login con Google, bloqueo por intentos fallidos).

> El monolito original vivió en `legacy-monolith/` como referencia durante la
> migración; se eliminó una vez consolidada la versión microservicios. El
> historial sigue disponible en git (rama `feat/microservices` y anteriores).

Diseño detallado (por qué cada corte, flujos entre servicios, convención de
paquetes, manejo de errores y próximos pasos):
**[MICROSERVICES.md](MICROSERVICES.md)**.

## Los servicios

| Módulo | Puerto | Es dueño de | Base de datos |
|---|---|---|---|
| **api-gateway** | 8080 | punto de entrada único: rutea `/api/**` y `/uploads/**`, sirve el React, valida el JWT de primera línea | — |
| **auth-service** | 8081 | credenciales (hash BCrypt), **firma** de los JWT (RSA), refresh tokens, 2FA TOTP, lockout, OAuth2 Google | `authdb` |
| **negocio-service** | 8082 | perfil de usuario y ABM de admin, estadísticas, juegos, secciones, compras, subida de imágenes | `negociodb` |
| **common-security** | (lib) | `JwtService` + filtro JWT compartido: auth firma, el resto **valida** con la clave pública | — |
| **common-web** | (lib) | contrato de error HTTP compartido: `ErrorResponseDto` + `GlobalExceptionHandler` | — |

```
                    ┌───────────── api-gateway :8080 ──────────────┐
  navegador ───────▶│  React estático  +  ruteo /api/**  +  JWT    │
                    └────────┬────────────────────────┬────────────┘
                             ▼                        ▼
                       auth-service  ◀────────────▶  negocio-service
                          :8081         /internal/**      :8082
                             │                             │
                          authdb                       negociodb
```

Las dos llamadas entre servicios van por `/internal/**` (header de secreto
compartido, nunca expuesto por el gateway):

- **auth → negocio**, en el login: pedir rol y si la cuenta está habilitada.
- **negocio → auth**, en el ABM del admin: crear / cambiar password / borrar la
  credencial del usuario.

`negocio-service` unifica lo que antes eran `usuarios-service` y
`catalogo-service`: adentro siguen siendo dos subdominios (`usuario/` y
`catalogo/`) pero comparten JVM y base, y se hablan por una interfaz Java
(paquetes `*.spi`) en vez de HTTP. El porqué está en
[MICROSERVICES.md](MICROSERVICES.md).

## Correrlo en local (sin Docker)

Requiere **Java 21** y **Node 18+**. Cada servicio usa **H2 en memoria** por
defecto (perfil `dev`), igual que el monolito.

### Windows (scripts)

```cmd
scripts\build.bat      :: Maven + npm build (una vez)
scripts\run-dev.bat    :: levanta los 3 servicios en ventanas separadas
```

Abrí **http://localhost:18080** (`admin` / `admin123`). Los scripts usan los
puertos **18080–18082** para no chocar con Jenkins u otra cosa que ya esté en
8080/8081. Para frenar: cerrá las 3 ventanas.

### A mano (cualquier SO)

```bash
# 1) Compilar el reactor (fat-jar en backend/*/target) y el frontend
mvn -f backend/pom.xml -DskipTests package
cd frontend && npm install && npm run build && cd ..

# 2) Levantar los 3 servicios (3 terminales, o con &). El gateway
#    necesita saber dónde está cada uno si no usás los puertos por defecto.
java -jar backend/negocio-service/target/negocio-service.jar   # :8082
java -jar backend/auth-service/target/auth-service.jar         # :8081
java -jar backend/api-gateway/target/api-gateway.jar           # :8080
```

Abrí **http://localhost:8080**. Si el 8080 está ocupado, corré cada servicio en
otro puerto con `--server.port=...` y pasale al gateway
`--SERVICES_AUTH_URL=` y `--SERVICES_NEGOCIO_URL=` (es lo que hace
`scripts/run-dev.bat`).

Para hot-reload del frontend: `cd frontend && npm run dev` (Vite en :5173, con
proxy al gateway).

## Correrlo con Docker (Postgres por servicio)

```bash
cd frontend && npm install && npm run build && cd ..   # la primera vez
docker compose up --build
```

`docker-compose.yml` levanta 2 Postgres (uno por servicio, con volumen), los 3
servicios (perfil `docker`) y publica solo el gateway en **http://localhost:8080**.
El arranque está ordenado con healthchecks (`negocio-service` antes que
`auth-service`, gateway al final).

## Desplegarlo (frontend en Vercel + backend en un VPS)

Vercel no puede correr los servicios Spring Boot ni los Postgres. El frontend va
a Vercel como hasta ahora; el backend (los 3 servicios + 2 bases) se levanta en
un VPS con `docker-compose.prod.yml` (Caddy delante, HTTPS automático) y el
frontend apunta a esa URL con `VITE_API_BASE_URL`. Guía paso a paso:
**[DEPLOY.md](DEPLOY.md)**.

## Usuarios de prueba

Se recrean en cada arranque (bases en memoria / volúmenes nuevos):

```
admin                   / admin123      -> ADMIN
vendedor1  / vendedor2   / vendedor123   -> VENDEDOR
comprador1 .. comprador5 / comprador123  -> COMPRADOR
```

## Smoke test rápido

```bash
BASE=http://localhost:8080
TOKEN=$(curl -s -X POST $BASE/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

curl -s $BASE/api/auth/me       -H "Authorization: Bearer $TOKEN"   # perfil (auth -> negocio)
curl -s $BASE/api/juegos        -H "Authorization: Bearer $TOKEN"   # catálogo (negocio)
curl -s $BASE/api/admin/stats   -H "Authorization: Bearer $TOKEN"   # stats (negocio)
curl -s -o /dev/null -w '%{http_code}\n' $BASE/api/juegos           # 401: el gateway corta sin token
```

Todos los errores de la API salen con la misma forma, `{"mensaje": "..."}`, la
arme el `GlobalExceptionHandler` de `common-web` o el advice propio de cada
servicio.

## Estructura del código

Convención: **todo paquete Java va en singular** (`controller`, `service`,
`repository`, `model`, `dto`, `exception`, …); en el frontend, la carpeta que
agrupa módulos del mismo tipo va en plural (`components/`, `contexts/`). El
detalle y el árbol completo están en
[MICROSERVICES.md](MICROSERVICES.md).

## Claves JWT

`auth-service` firma con `keys/jwt-private.pem` (RS256); el gateway y los demás
servicios validan con `keys/jwt-public.pem`. Ambas están commiteadas **solo para
la demo** — ver [`keys/README.md`](keys/README.md) para regenerarlas.
