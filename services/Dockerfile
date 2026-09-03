# ==========================================================
# Dockerfile compartido por los 4 servicios. Se elige cual construir con
# el build-arg SERVICE (lo pasa docker-compose.yml):
#
#   docker build --build-arg SERVICE=auth-service -t gamestore/auth ./services
#
# Multi-stage: etapa 1 compila el modulo (y sus dependencias del reactor,
# -am) con Maven; etapa 2 deja solo el JRE + el fat-jar.
# ==========================================================

# ---- Etapa 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiamos primero solo los pom.xml para cachear la descarga de dependencias.
COPY pom.xml .
COPY common-security/pom.xml common-security/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY usuarios-service/pom.xml usuarios-service/
COPY catalogo-service/pom.xml catalogo-service/
RUN mvn -B -q -N dependency:go-offline || true

COPY . .
ARG SERVICE
RUN mvn -B -q -pl ${SERVICE} -am -DskipTests package

# ---- Etapa 2: runtime ----
# "jammy" (Debian) y no Alpine: el truststore TLS de Alpine dio problemas
# con Postgres gestionados (Neon) en el monolito; mismo criterio aca.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

ARG SERVICE
COPY --from=build /build/${SERVICE}/target/${SERVICE}.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]
