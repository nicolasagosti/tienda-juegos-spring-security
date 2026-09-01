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

# ---- Etapa 2: imagen final, liviana, solo el JRE ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
