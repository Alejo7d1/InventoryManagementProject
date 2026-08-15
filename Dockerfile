# ============================================================
# DCAVA Backend - Dockerfile
# Build multi-etapa: compila con Maven + JDK 21 y ejecuta con JRE 21
# ============================================================

# ---- Etapa 1: Compilación ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar solo el pom primero para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests

# Copiar el código fuente y compilar el JAR
COPY src ./src
RUN mvn -B package -DskipTests

# ---- Etapa 2: Ejecución ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Directorio para archivos estáticos locales (uploads)
RUN mkdir -p /app/uploads

# Directorio para los archivos de log (retención de 30 días gestionada por Logback)
RUN mkdir -p /app/logs

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /app/target/dcava-backend-*.jar app.jar

# Puerto que expone la aplicación (Spring Boot)
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
