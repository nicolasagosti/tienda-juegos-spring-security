# Despliegue — frontend en Vercel + microservicios en un VPS

Vercel hostea sitios estáticos y funciones serverless: **no puede correr los
servicios Spring Boot ni los Postgres**. El reparto queda así:

```
   navegador
      │
      ├──────────────▶  Vercel            (React estático, el build de frontend/)
      │                    │  fetch /api/**  y  /uploads/**
      ▼                    ▼
   https://api.tudominio.com  ──▶  VPS con Docker
                                     Caddy (443) ─▶ api-gateway ─▶ auth / usuarios / catalogo ─▶ 3× Postgres
```

El frontend solo necesita saber **una** URL: la del gateway. Todo lo demás
(auth-service, catalogo-service, las llamadas internas, las bases) queda privado
en la red interna de Docker.

---

## 1. Backend en el VPS

Requisitos: un VPS Linux con **Docker** y **Docker Compose v2**, y un dominio
(o subdominio) apuntando a la IP del VPS.

```bash
# En el VPS
git clone <tu-repo> gamestore && cd gamestore

# 1) Variables de entorno
cp .env.prod.example .env
nano .env                      # completar DOMAIN, TLS_EMAIL, passwords, URL de Vercel
#   INTERNAL_SECRET:            openssl rand -hex 32
#   *_DB_PASSWORD:              openssl rand -hex 16

# 2) Claves JWT reales (las de keys/ commiteadas son SOLO demo)
./keys/generate-keys.sh        # regenera keys/jwt-private.pem y keys/jwt-public.pem
#   -> NO commitear jwt-private.pem (ver keys/README.md)

# 3) DNS: registro A de  api.tudominio.com  ->  IP del VPS
#    (Caddy necesita que resuelva para sacar el certificado)

# 4) Levantar
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps        # esperar "healthy"
```

Caddy saca el certificado HTTPS solo la primera vez. Verificación:

```bash
curl -s https://api.tudominio.com/actuator/health           # {"status":"UP"...}
curl -s -X POST https://api.tudominio.com/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'            # devuelve token
```

Solo el puerto 443 (y 80 para el redirect) queda expuesto: el gateway y los
servicios no publican puertos, únicamente Caddy los alcanza por la red interna.

## 2. Frontend en Vercel

El proyecto de Vercel apunta a la carpeta `frontend/` (build command `npm run
build`, output `dist`, ya configurado en `frontend/vercel.json` y
`vite.config.js` en modo standalone).

En **Settings → Environment Variables** de Vercel:

| Variable | Valor |
|---|---|
| `VITE_API_BASE_URL` | `https://api.tudominio.com/api` |
| `VITE_STANDALONE_BUILD` | `true` |

Redeploy. El `frontend/src/api/client.js` ya usa `VITE_API_BASE_URL` para la API
y deriva de ahí el origen para las imágenes (`/uploads/**`), así que no hay más
nada que tocar en el código.

## 3. Login con Google (opcional)

Si vas a usarlo, en Google Cloud Console → Credenciales → tu OAuth client:

- **Authorized redirect URI**: `https://api.tudominio.com/login/oauth2/code/google`
- Cargá `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` en el `.env` del VPS.

El redirect post-login usa `FRONTEND_URL` (la URL de Vercel), que ya está en el `.env`.

## 4. Operación

```bash
docker compose -f docker-compose.prod.yml logs -f api-gateway
docker compose -f docker-compose.prod.yml up -d --build        # deploy de una versión nueva
docker compose -f docker-compose.prod.yml down                 # parar (los volúmenes quedan)
```

**Backups**: los datos viven en los volúmenes `auth-db-data`, `usuarios-db-data`,
`catalogo-db-data` y las imágenes en `catalogo-uploads`. Programá un
`pg_dump` de las 3 bases.

## Limitaciones de este setup

Heredadas del enfoque "un VPS, un `docker compose`":

- **Un solo host**: no hay alta disponibilidad ni escala horizontal. Si el VPS
  se cae, se cae todo.
- **Imágenes de juegos en un volumen local**: sirve con una instancia de
  catalogo-service; para escalar hay que mover esto a S3/Cloudinary (ver
  `MICROSERVICES.md` §7).
- **`ddl-auto=update`**: Hibernate crea/actualiza las tablas. Un proyecto real
  usaría Flyway/Liquibase por servicio.
- **`/actuator/health` del gateway queda público** detrás de Caddy. Si molesta,
  bloquealo en el `Caddyfile`.
- Escalar a más de un host = Kubernetes (u otro orquestador), fuera del alcance
  de esta guía.
