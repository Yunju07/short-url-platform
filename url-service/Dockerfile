FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :url-service:clean :url-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=builder /app/url-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]