# GameStore - Demo de Spring Security + React

Proyecto de ejemplo con **Spring Boot 3 + Spring Security 6** en el backend
y **React (Vite)** en el frontend. Implementa login y **3 categorías de
usuario** sobre una tienda de juegos de PC:

| Rol | Puede |
|---|---|
| **COMPRADOR** | Ver el catálogo de juegos (solo lectura) |
| **VENDEDOR** | Ver el catálogo + publicar/editar/eliminar **sus propios** juegos (nombre, precio, imagen, sección) |
| **ADMIN** | Todo lo anterior + crear usuarios y asignarles categoría, editar/habilitar/deshabilitar/eliminar cualquier usuario, crear/eliminar secciones (categorías del catálogo) y moderar (editar/eliminar) cualquier juego |

## Seguridad implementada (más allá del login básico)

- **Bloqueo por intentos fallidos**: 5 intentos seguidos con contraseña incorrecta bloquean la cuenta 15 minutos, sin que el ADMIN tenga que hacer nada (`LoginAttemptListener`).
- **Refresh tokens con rotación**: el access token (JWT) dura 15 minutos; un refresh token de 7 días (guardado en la base, revocable) lo renueva solo. Cada uso rota el refresh token — reusar uno viejo lo invalida.
- **2FA (TOTP)**: cualquier usuario puede activar verificación en dos pasos desde "Mi cuenta" (compatible con Google Authenticator, Authy, etc.), implementado a mano según RFC 6238.
- **Login con Google (OAuth2/OIDC)**: opcional — funciona sin configurar nada (el botón simplemente queda inerte), se activa cargando credenciales de Google Cloud Console. Ver [DEPLOY.md](DEPLOY.md#6-login-con-google-opcional).

## Arquitectura

El backend expone **dos interfaces sobre las mismas reglas de seguridad**:

- **API REST en JSON** bajo `/api/**` — la consume el frontend React (la
  experiencia principal, se sirve en `/`).
- **UI clásica en Thymeleaf** bajo `/juegos`, `/admin`, `/login` — la
  versión original con formularios server-side, se mantiene funcionando
  en paralelo como referencia/comparación.

Ambas comparten el mismo `AuthenticationProvider` (mismas entidades,
mismo `PasswordEncoder`), pero **cada una autentica distinto**:

- La UI Thymeleaf usa sesión + cookie de siempre (`formLogin`).
- La API usa **JWT stateless**: el login devuelve un token firmado que el
  frontend guarda y reenvía en cada pedido como
  `Authorization: Bearer <token>`. No hay cookie de sesión ni CSRF de por
  medio en `/api/**` — es la razón por la que el mismo frontend funciona
  igual de bien corriendo pegado al backend (mismo proceso, ver abajo) o
  desplegado en un dominio totalmente distinto (Vercel hablándole a un
  backend en Render, ver [DEPLOY.md](DEPLOY.md)).

Por defecto (`npm run build` sin variables de entorno) el frontend se
compila directamente adentro de `src/main/resources/static`, así que en
local **todo corre en un solo proceso y un solo puerto**. Para desplegarlo
separado (React en Vercel + backend en Render/Railway/etc.), ver
**[DEPLOY.md](DEPLOY.md)** — ahí está la guía paso a paso completa,
incluyendo cómo armar el repo con ramas `main`/`develop` y el CI de
GitHub Actions.

## Cómo correrlo

Requiere Java 17+ y Node 18+ (se probó con Java 21 y Node 24).

```bash
# 1) Compilar el frontend (deja el resultado en src/main/resources/static)
cd frontend
npm install
npm run build
cd ..

# 2) Levantar el backend (sirve la API + el React ya compilado)
mvn spring-boot:run
```

Abrí `http://localhost:8080` (si ese puerto está ocupado en tu máquina, arrancalo
en otro: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`).

Usuarios de prueba (se recrean en cada reinicio, base H2 en memoria):

```
admin      / admin123      -> ADMIN
vendedor1  / vendedor123   -> VENDEDOR
vendedor2  / vendedor123   -> VENDEDOR
comprador1 / comprador123  -> COMPRADOR
```

En la pantalla de login de React podés hacer click en cualquiera de esos
usuarios para autocompletar el formulario.

### Modo desarrollo del frontend (hot-reload)

Si vas a tocar el React y querés ver los cambios al instante sin
recompilar cada vez:

```bash
# terminal 1: backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# terminal 2: frontend con hot-reload, proxyeando /api al backend
cd frontend
npm run dev
```

Y abrís `http://localhost:5173` (el puerto que imprime Vite). Cuando
termines, `npm run build` deja la versión final servida por el backend.

## Estructura del proyecto

```
frontend/                              # React + Vite
├── public/css/styles.css              # CSS de la UI clasica Thymeleaf (se copia tal cual al build)
├── src/
│   ├── api/client.js                  # instancia de axios (baseURL configurable, header Authorization: Bearer)
│   ├── context/AuthContext.jsx        # login/logout/me
│   ├── components/                    # LoginPage, CatalogPage, GameFormPage, Admin*, Navbar, Toast
│   ├── App.jsx                        # navegacion por estado (sin react-router)
│   └── styles.css                     # tema oscuro "gaming"
└── vite.config.js                     # build.outDir -> ../src/main/resources/static

src/main/java/com/example/tiendajuegos/
├── TiendaJuegosApplication.java
├── config/
│   ├── SecurityConfig.java            # el corazon: reglas de login/roles, CORS, JWT vs sesion, JSON vs redirect
│   ├── WebConfig.java                 # sirve /uploads/** como recurso estatico
│   ├── GeneradorPortadas.java         # genera las portadas de los juegos de ejemplo (Java2D)
│   └── DataInitializer.java           # usuarios/secciones/juegos de ejemplo
├── model/          # entidades JPA: Usuario, Rol (enum), Seccion, Juego
├── repository/     # interfaces Spring Data JPA
├── security/
│   ├── CustomUserDetails.java
│   ├── UsuarioDetailsServiceImpl.java
│   ├── JwtService.java                # genera/valida los JWT de la API
│   └── JwtAuthenticationFilter.java   # lee "Authorization: Bearer ..." y autentica el request
├── service/         # logica de negocio (usuarios, secciones, juegos, imagenes)
├── controller/       # UI clasica: HomeController, JuegoController, AdminController (Thymeleaf)
└── api/
    ├── dto/          # records: UsuarioDTO, JuegoDTO, SeccionDTO, Requests
    └── controller/    # REST: AuthApiController, JuegoApiController, SeccionApiController,
                       #        UsuarioApiController, AdminStatsApiController, GlobalApiExceptionHandler

src/main/resources/
├── application.properties
├── templates/        # vistas Thymeleaf (UI clasica)
└── static/           # generado por "npm run build" (NO editar a mano)
```

## Qué probar en la demo

1. Entrar como `comprador1`: ves el catálogo, pero no aparece el botón
   "Publicar juego" ni el menú de administración. Las llamadas a la API
   protegida (`/api/usuarios`, `/api/juegos` con POST/PUT/DELETE) devuelven
   **403 en JSON**.
2. Entrar como `vendedor1`: podés publicar un juego (nombre, descripción,
   precio, sección, imagen) y editar/eliminar solo los juegos que
   publicaste vos. Si el juego es de `vendedor2`, también da 403.
3. Entrar como `admin`: tenés el panel con estadísticas, gestión de
   usuarios (crear, asignar rol, editar perfil, habilitar/deshabilitar,
   eliminar) y gestión de secciones. Además podés editar o borrar
   **cualquier** juego de cualquier vendedor.
4. La UI clásica sigue viva en `/juegos`, `/admin`, `/login` (formularios
   Thymeleaf) para comparar los dos estilos de implementación sobre las
   mismas reglas de seguridad.

## Nota sobre seguridad (esto es una demo)

- La consola H2 (`/h2-console`) y su exención de CSRF están pensadas
  solamente para poder inspeccionar la base durante el desarrollo. **Nunca
  se debe exponer en un entorno real.**
- El JWT de la API se firma con `app.jwt.secret`. En local tiene un valor
  por defecto (cómodo para probar); en producción (`application-prod.properties`)
  **no tiene default** — si no configurás la variable de entorno `JWT_SECRET`
  la app no arranca, a propósito (fail-fast en vez de arrancar insegura).
- `spring.jpa.hibernate.ddl-auto=update` y una base en memoria son
  prácticas de demo; en un proyecto real se usaría una base persistente
  con migraciones versionadas (Flyway/Liquibase).
- Las imágenes se guardan tal cual se suben en el disco local
  (`app.upload.dir`); en producción convendría validar tipo/tamaño real de
  archivo y usar almacenamiento externo (S3, etc.).

## Ramas y CI/CD

- `main`: rama de producción.
- `develop`: rama de integración (features/PRs se mergean acá primero).
- `.github/workflows/ci.yml`: corre en cada push/PR a `main` o `develop`
  — compila y testea el backend, compila el frontend.
- El despliegue real (Vercel + Render) se conecta una sola vez desde sus
  dashboards y después queda automático en cada push a `main` (deploy de
  producción) o `develop`/PRs (preview deploys). Paso a paso completo en
  **[DEPLOY.md](DEPLOY.md)**.
