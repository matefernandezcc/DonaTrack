# Multi-stage build for Render & Docker
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl donatrack-auth -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/donatrack-auth/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]

