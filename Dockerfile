FROM node:22-alpine AS frontend-builder
WORKDIR /app/frontend

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY frontend/ ./
RUN pnpm build

FROM eclipse-temurin:25-jdk AS backend-builder
WORKDIR /app/backend

COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./

# Strip Windows CRLF before running wrapper
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY backend/src/ src/
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends nginx curl && \
    rm -rf /var/lib/apt/lists/*

COPY --from=backend-builder /app/backend/target/pokemon-api-0.0.1-SNAPSHOT.jar /app/app.jar
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html

# Seed database so the container starts with initial data offline
COPY backend/data/pokemondb.mv.db /app/data/pokemondb.mv.db

COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN sed -i 's/\r$//' /app/entrypoint.sh && chmod +x /app/entrypoint.sh

# Single entrypoint port: Nginx serves the UI and proxies /api to Spring Boot
EXPOSE 80

ENTRYPOINT ["/app/entrypoint.sh"]
