FROM openjdk:21-jdk-slim AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle /app/
COPY src src

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
