# Build stage — compiles the application and runs tests
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY config ./config
COPY gradle ./gradle
# Cache dependencies by copying build files first
RUN gradle dependencies --no-daemon || true
COPY src ./src
RUN gradle build -x test --no-daemon

# Runtime stage — minimal image with just the JAR
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
