#!/usr/bin/env bash
# ==========================================================
# Regenera el par de claves RSA que usa auth-service para FIRMAR los JWT
# (clave privada) y que el gateway + el resto de los servicios usan para
# VALIDARLOS (clave publica).
#
# Las claves que vienen commiteadas en el repo son SOLO para la demo (mismo
# criterio que el "demo-secret" del monolito original). Para un despliegue
# real: corre este script, NO commitees jwt-private.pem, y cargalo por
# variable de entorno / secret manager (ver docker-compose.yml y los
# application-docker.properties de cada servicio).
#
#   Uso:  ./keys/generate-keys.sh
# ==========================================================
set -euo pipefail
cd "$(dirname "$0")"

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
openssl pkey -in jwt-private.pem -pubout -out jwt-public.pem

echo "OK - jwt-private.pem (firma, solo auth-service) y jwt-public.pem (validacion, todos) regenerados."
