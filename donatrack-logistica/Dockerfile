# Multi-stage build for Render
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
# Copiamos todo el repositorio (asumiendo que el contexto de build es la raiz)
COPY . .
# Compilamos especificamente el modulo de logistica y sus dependencias (common, etc)
RUN mvn clean package -DskipTests -pl donatrack-logistica -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el jar generado en la etapa anterior
COPY --from=builder /app/donatrack-logistica/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
