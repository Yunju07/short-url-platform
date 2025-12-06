FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :stats-service:clean :stats-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=builder /app/stats-service/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]