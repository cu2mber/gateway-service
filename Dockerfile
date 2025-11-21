### 1. Builder Stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
COPY . .
RUN GITHUB_ACTOR=${GITHUB_ACTOR} GITHUB_TOKEN=${GITHUB_TOKEN} ./mvnw clean package -DskipTests

### 2. Runtime Stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]