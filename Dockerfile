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

RUN apk add --no-cache python3 py3-pip \
    && python3 -m venv /opt/careeros-venv \
    && /opt/careeros-venv/bin/pip install --no-cache-dir python-docx==1.2.0 \
    && addgroup -S careeros \
    && adduser -S careeros -G careeros
ENV PATH="/opt/careeros-venv/bin:${PATH}"
COPY --from=build /workspace/target/careeros.jar app.jar
COPY scripts/resume_docx.py scripts/resume_docx.py
RUN mkdir -p /data/resumes && chown -R careeros:careeros /data /app/scripts
USER careeros

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
