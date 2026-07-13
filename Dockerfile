# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -B package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S careeros && adduser -S careeros -G careeros
COPY --from=build /workspace/target/careeros.jar app.jar
USER careeros

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
