# GameStore — versión microservicios

Migración del monolito Spring Boot original (`legacy-monolith/`) a **4 servicios
Spring Boot + 1 librería compartida**, manteniendo el mismo frontend React y la
misma funcionalidad (roles ADMIN / VENDEDOR / COMPRADOR, JWT + refresh tokens,
2FA TOTP, login con Google, bloqueo por intentos fallidos).

> El monolito sigue compilando y corriendo en `legacy-monolith/` como
> referencia — se elimina cuando la versión microservicios esté consolidada.
> Ver [`legacy-monolith/README.md`](legacy-monolith/README.md).

Diseño detallado (por qué cada corte, flujos entre servicios, compromisos y
próximos pasos): **[MICROSERVICES.md](MICROSERVICES.md)**.

## Los servicios

| Módulo | Puerto | Es dueño de | Base de datos |
|---|---|---|---|
| **api-gateway** | 8080 | punto de entrada único: rutea `/api/**` y `/uploads/**`, sirve el React, valida el JWT de primera línea | — |
| **auth-service** | 8081 | credenciales (hash BCrypt), **firma** de los JWT (RSA), refresh tokens, 2FA TOTP, lockout, OAuth2 Google | `authdb` |
| **usuarios-service** | 8082 | perfil de usuario (nombre, email, rol, habilitado), ABM de admin, estadísticas | `usuariosdb` |
| **catalogo-service** | 8083 | juegos, secciones, subida de imágenes | `catalogodb` |
| **common-security** | (lib) | `JwtService` + filtro JWT compartido: auth firma, el resto **valida** con la clave pública | — |

```
                    ┌───────────── api-gateway :8080 ──────────────┐
  navegador ───────▶│  React estático  +  ruteo /api/**  +  JWT    │
                    └──┬──────────────┬───────────────────┬────────┘
                       ▼              ▼                    ▼
                auth-service    usuarios-service     catalogo-service
                   :8081            :8082                 :8083
                     │  ▲             │  ▲                  │
      login: rol/estado │             │  │ credencial (alta) │ vendedor de cada juego
                        └─────────────┘  └───── /internal/** ─┘
                     authdb          usuariosdb            catalogodb
```

## Correrlo en local (sin Docker)

Requiere **Java 21** y **Node 18+**. Cada servicio usa **H2 en memoria** por
defecto (perfil `dev`), igual que el monolito.

```bash
# 1) Compilar todo el reactor (deja los fat-jar en services/*/target)
mvn -f services/pom.xml -DskipTests package

# 2) Compilar el frontend (se copia dentro del gateway)
cd frontend && npm install && npm run build && cd ..

# 3) Levantar los servicios — en 4 terminales, o con & — EN ESTE ORDEN:
java -jar services/usuarios-service/target/usuarios-service.jar   # :8082
java -jar services/catalogo-service/target/catalogo-service.jar   # :8083
java -jar services/auth-service/target/auth-service.jar           # :8081
java -jar services/api-gateway/target/api-gateway.jar             # :8080
```

Abrí **http://localhost:8080**.

Durante el desarrollo del backend también sirve `mvn -f services/<modulo> spring-boot:run`.
Para hot-reload del frontend: `cd frontend && npm run dev` (Vite en :5173, con
proxy al gateway).

## Correrlo con Docker (Postgres por servicio)

```bash
cd frontend && npm install && npm run build && cd ..   # la primera vez
docker compose up --build
```

`docker-compose.yml` levanta 3 Postgres (uno por servicio, con volumen), los 4
servicios (perfil `docker`) y publica solo el gateway en **http://localhost:8080**.
El arranque está ordenado con healthchecks (`usuarios-service` antes que
`auth-service`, gateway al final).

## Usuarios de prueba

Se recrean en cada arranque (bases en memoria / volúmenes nuevos):

```
admin      / admin123      -> ADMIN
vendedor1  / vendedor123   -> VENDEDOR
vendedor2  / vendedor123   -> VENDEDOR
comprador1 / comprador123  -> COMPRADOR
```

## Smoke test rápido

```bash
BASE=http://localhost:8080
TOKEN=$(curl -s -X POST $BASE/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

curl -s $BASE/api/auth/me       -H "Authorization: Bearer $TOKEN"   # perfil (auth -> usuarios)
curl -s $BASE/api/juegos        -H "Authorization: Bearer $TOKEN"   # catálogo (catalogo -> usuarios)
curl -s $BASE/api/admin/stats   -H "Authorization: Bearer $TOKEN"   # stats (usuarios + catalogo)
curl -s -o /dev/null -w '%{http_code}\n' $BASE/api/juegos           # 401: el gateway corta sin token
```

## Claves JWT

`auth-service` firma con `keys/jwt-private.pem` (RS256); el gateway y los demás
servicios validan con `keys/jwt-public.pem`. Ambas están commiteadas **solo para
la demo** — ver [`keys/README.md`](keys/README.md) para regenerarlas.
