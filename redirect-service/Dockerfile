FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :redirect-service:clean :redirect-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=builder /app/redirect-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]