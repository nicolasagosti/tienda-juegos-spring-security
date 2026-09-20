# syntax=docker/dockerfile:1
# ==========================================================
# Dockerfile compartido por los servicios. Se elige cual construir con
# el build-arg SERVICE (lo pasa docker-compose.yml):
#
#   docker build --build-arg SERVICE=auth-service -t gamestore/auth ./backend
#
# Multi-stage: etapa 1 compila el modulo (y sus dependencias del reactor,
# -am) con Maven; etapa 2 deja solo el JRE + el fat-jar, corriendo como
# usuario sin privilegios.
# ==========================================================

# ---- Etapa 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiamos primero solo los pom.xml para cachear la resolucion de dependencias.
COPY pom.xml .
COPY common-security/pom.xml common-security/
COPY common-web/pom.xml common-web/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY negocio-service/pom.xml negocio-service/
# El cache mount de ~/.m2 persiste entre builds: la 2da build ya no
# vuelve a descargar dependencias.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -N dependency:go-offline || true

COPY . .
ARG SERVICE
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl ${SERVICE} -am -DskipTests package

# ---- Etapa 2: runtime ----
# "jammy" (Debian) y no Alpine: el truststore TLS de Alpine dio problemas
# con Postgres gestionados (Neon) en el monolito; mismo criterio aca.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 1001 app \
    && useradd --system --uid 1001 --gid app --home-dir /app --shell /usr/sbin/nologin app \
    && mkdir -p /data/uploads \
    && chown -R app:app /app /data/uploads

ARG SERVICE
COPY --from=build --chown=app:app /build/${SERVICE}/target/${SERVICE}.jar app.jar

USER app
ENV SPRING_PROFILES_ACTIVE=docker
# El heap se ajusta al limite de memoria del contenedor (ver docker-compose*.yml).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "app.jar"]
