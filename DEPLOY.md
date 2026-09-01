# Guia de despliegue: React en Vercel + Spring Boot en Render

Esta app se despliega en **dos partes independientes**, en dos dominios
distintos: el frontend React (estatico) en Vercel, y el backend Spring
Boot (con Postgres) en un host que sí corre procesos Java, como Render.
Se comunican por HTTP: el React le pega a la API con un JWT en el header
`Authorization`, no con cookies (por eso no hace falta que compartan
dominio ni configurar nada raro de SameSite).

Todos los pasos de abajo requieren que hagas login vos mismo (son flujos
de navegador que yo no puedo completar): `gh auth login`, `vercel login`,
y crear cuenta/servicios en Render. Te dejo el comando o el click exacto
en cada paso.

## 0) Requisitos

- Cuenta de GitHub.
- Cuenta de Vercel (podés loguearte con la cuenta de GitHub).
- Cuenta de Render (idem).
- `gh` (GitHub CLI) instalado — o hacelo directo desde la web de GitHub.
- `git` (ya lo tenés).

## 1) Crear el repositorio y las ramas

Ya deje el repo local inicializado con dos ramas:

- `main` → rama de produccion (lo que dispara los deploys reales).
- `develop` → rama de integracion (donde mergeas features antes de pasarlas a main).

Para subirlo a GitHub:

```bash
# Instalar/loguear gh si todavia no lo hiciste
gh auth login

# Desde la carpeta del proyecto:
gh repo create tienda-juegos-spring-security --public --source=. --remote=origin --push
```

Eso crea el repo en tu cuenta, agrega el remote `origin` y pushea la rama
actual. Despues pusheá también `develop`:

```bash
git push -u origin develop
git push -u origin main
```

Si preferís hacerlo sin `gh` (a mano desde github.com/new), despues corré:

```bash
git remote add origin https://github.com/TU_USUARIO/tienda-juegos-spring-security.git
git push -u origin main
git push -u origin develop
```

### Flujo de trabajo sugerido

- Trabajás sobre `develop` (o ramas `feature/algo` que mergeas a `develop` via PR).
- Cada push/PR a `develop` o `main` dispara el workflow de CI
  (`.github/workflows/ci.yml`): compila el backend y corre sus tests,
  y compila el frontend. Si algo rompe, se ve en la pestaña "Actions" del
  repo y en el check del PR.
- Cuando `develop` esta estable, mergeas a `main` → eso disparara el
  deploy de produccion en Vercel y Render (una vez conectados, ver abajo).

## 2) Backend en Render (Java + Postgres)

1. Entra a [render.com](https://render.com) y logueate con GitHub.
2. **New +** → **PostgreSQL**. Nombre: `tienda-juegos-db`, plan Free.
   Cuando este listo, andá a la pestaña "Info" y anotá:
   - `Hostname`
   - `Port` (5432)
   - `Database`
   - `Username`
   - `Password`
3. **New +** → **Web Service** → conectá el repo `tienda-juegos-spring-security`.
   - **Branch**: `main` (asi solo se redeploya con lo que llega a produccion).
   - **Root Directory**: dejalo vacio (el `Dockerfile` esta en la raiz del repo).
   - **Runtime**: Docker (Render detecta el `Dockerfile` solo).
   - **Plan**: Free.
4. En la pestaña **Environment** del Web Service, agregá estas variables:

   | Variable | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DATABASE_URL` | `jdbc:postgresql://<Hostname>:5432/<Database>` (armala con los datos del paso 2) |
   | `DATABASE_USERNAME` | el `Username` de la base |
   | `DATABASE_PASSWORD` | el `Password` de la base |
   | `JWT_SECRET` | una cadena larga y random, por ejemplo la que te da `openssl rand -base64 48` |
   | `CORS_ALLOWED_ORIGINS` | por ahora poné `http://localhost:5173` (lo vas a actualizar en el paso 4 con la URL real de Vercel) |

5. **Create Web Service**. Render va a buildear la imagen Docker y
   levantar la app. Cuando termine, vas a tener una URL tipo
   `https://tienda-juegos-backend.onrender.com`. Probala:

   ```bash
   curl https://tienda-juegos-backend.onrender.com/api/juegos
   # deberia dar 401 (no autenticado) en JSON, eso confirma que esta viva
   ```

   > Nota sobre el free tier de Render: el servicio se "duerme" tras ~15
   > minutos sin trafico y el primer pedido después tarda unos 30-60s en
   > responder mientras arranca de nuevo. Es normal, no es un error.

## 3) Frontend en Vercel

1. Entra a [vercel.com](https://vercel.com), logueate con GitHub, o
   desde la terminal:
   ```bash
   npm i -g vercel
   vercel login
   ```
2. **Add New... → Project** → importá el repo `tienda-juegos-spring-security`.
3. Cuando te pida configurar el proyecto:
   - **Root Directory**: `frontend` (importante — el repo tiene el backend
     en la raiz, el frontend vive en esa subcarpeta).
   - Vercel detecta el framework Vite solo (o lo toma de `frontend/vercel.json`).
   - **Production Branch** (en Settings → Git, despues de crear el
     proyecto): dejala en `main`.
4. Antes de deployar, en **Settings → Environment Variables** agregá:

   | Variable | Valor | Environments |
   |---|---|---|
   | `VITE_API_BASE_URL` | `https://tienda-juegos-backend.onrender.com/api` (la URL de Render + `/api`) | Production, Preview, Development |
   | `VITE_STANDALONE_BUILD` | `true` | Production, Preview, Development |

5. **Deploy**. Al terminar te da una URL tipo
   `https://tienda-juegos-spring-security.vercel.app`.

   Como no usa CLI ni token de mi lado, esta parte la conectas vos
   siguiendo estos clicks — una vez conectado el repo, Vercel ya queda
   armando un deploy de produccion en cada push a `main`, y una URL de
   preview distinta para cada Pull Request y cada push a `develop`
   (comportamiento por defecto de Vercel, no hay que configurar nada mas).

## 4) Cerrar el circulo: avisarle al backend cual es el dominio del frontend

Con la URL real de Vercel en mano, volvé a Render → tu Web Service →
Environment → editá `CORS_ALLOWED_ORIGINS`:

```
https://tienda-juegos-spring-security.vercel.app,https://tienda-juegos-spring-security-*.vercel.app
```

(el segundo patron, con `-*`, es para que las Preview Deployments de
Vercel —que usan subdominios random tipo
`tienda-juegos-spring-security-git-develop-tuusuario.vercel.app`—
tambien puedan llamar a la API). Guardá: Render redeploya solo.

## 5) Probarlo

Abrí la URL de Vercel, logueate con `admin` / `admin123` (o cualquiera de
los usuarios de prueba) y deberia funcionar exactamente igual que en
local, solo que ahora el React vive en un dominio y la API en otro.

## Resumen de que es CI y que es CD ac​á

- **CI** (`.github/workflows/ci.yml`, lo corre GitHub Actions, ya
  configurado): en cada push/PR a `main` o `develop`, compila el backend
  y corre sus tests, y compila el frontend. Si falla, el PR queda
  marcado en rojo.
- **CD**: la hacen Vercel y Render de forma nativa una vez que conectás
  el repo (pasos 2 y 3): cada push a `main` dispara un deploy de
  produccion en ambos; cada push a `develop` (o cualquier otra rama, o
  PR) genera un preview deploy en Vercel automaticamente.

## Limitaciones a tener en cuenta (es una demo)

- **Imagenes subidas por vendedores**: en el free tier de Render el disco
  es efimero — se pierde en cada redeploy/restart. Las portadas de
  ejemplo se regeneran solas (`DataInitializer`), pero una imagen real
  que suba un vendedor se puede perder. Para produccion en serio
  convendria un storage externo (S3, Cloudinary, etc.).
- **Base de datos**: Postgres free de Render tiene un limite de
  almacenamiento chico y (en el plan gratuito) expira a los 90 dias si no
  lo actualizas — suficiente para una demo, no para produccion real.
- **Cold starts**: el free tier de Render duerme el servicio sin trafico;
  el primer request tarda en responder.
