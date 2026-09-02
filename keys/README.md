# Claves JWT (RS256)

| Archivo | Quién lo usa | Para qué |
|---|---|---|
| `jwt-private.pem` (PKCS#8) | **solo `auth-service`** | firmar los access tokens |
| `jwt-public.pem` (X.509 SPKI) | `api-gateway`, `usuarios-service`, `catalogo-service` | validar la firma de los tokens |

El monolito original firmaba con un secreto HMAC compartido (`app.jwt.secret`).
Al partirlo en servicios eso obligaría a repartir el mismo secreto por todos
lados. Con un par asimétrico, **solo auth-service tiene el poder de emitir
tokens**; el resto solo puede verificarlos.

## Las claves del repo son de demo

Están commiteadas a propósito para que `docker compose up` funcione sin pasos
previos, igual que el `demo-secret` del monolito. **No las uses en producción.**

## Regenerar

```bash
./keys/generate-keys.sh      # requiere openssl
```

En producción: generá el par fuera del repo, dejá `jwt-private.pem` solo en
auth-service (variable de entorno `APP_JWT_PRIVATE_KEY` o secret montado) y
distribuí `jwt-public.pem` como recurso de solo lectura en los demás.
