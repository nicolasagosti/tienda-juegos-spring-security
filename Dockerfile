# ==========================================================
# Imagen del backend (Spring Boot) para desplegarlo separado del
# frontend -- pensada para Render/Railway/Fly.io o cualquier host que
# reciba un Dockerfile. El React de Vercel le habla por HTTP via /api.
#
# Build:  docker build -t tienda-juegos-backend .
# Run:    docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod \
#           -e DATABASE_URL=... -e DATABASE_USERNAME=... -e DATABASE_PASSWORD=... \
#           -e JWT_SECRET=... -e CORS_ALLOWED_ORIGINS=https://tu-app.vercel.app \
#           tienda-juegos-backend
# ==========================================================

# ---- Etapa 1: compilar con Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero solo el pom.xml para aprovechar la cache de capas de
# Docker: si no cambiaron las dependencias, no las vuelve a descargar en
# cada build.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Etapa 2: imagen final, solo el JRE ----
# OJO: se probo primero con "eclipse-temurin:21-jre-alpine" (mas liviana)
# pero su truststore de certificados TLS/SSL viene incompleto en algunos
# builds, y como Neon exige SSL (sslmode=require) la conexion a la base
# fallaba en produccion (aunque funcionaba perfecto fuera de Alpine). Se
# uso la variante "jammy" (basada en Debian/Ubuntu), sin ese problema.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
