# --- Build Stage ---
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
RUN mkdir -p /app/attachments && chmod -R 777 /app/attachments

# --- Run Stage ---
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

RUN mkdir -p /app/logs && chmod -R 777 /app/logs

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
