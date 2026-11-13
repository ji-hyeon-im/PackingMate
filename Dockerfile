FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /workspace
COPY backend/ ./
COPY --from=frontend-build /frontend/dist/ ./src/main/resources/static/
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system --gid 10001 packingmate \
    && useradd --system --uid 10001 --gid packingmate --home-dir /app --no-create-home packingmate
COPY --from=backend-build --chown=packingmate:packingmate /workspace/build/libs/backend-0.0.1-SNAPSHOT.jar ./app.jar
USER packingmate
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
