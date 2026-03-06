FROM gradle:9.3.1-jdk17-jammy AS builder
WORKDIR /app

COPY settings.gradle build.gradle ./
COPY gradle ./gradle

COPY src ./src

RUN gradle --no-daemon clean bootJar -x check

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
