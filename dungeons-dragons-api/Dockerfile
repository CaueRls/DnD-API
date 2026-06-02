# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven descriptors first for better Docker cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd

# Use Maven from the image to avoid Maven Wrapper download/permission issues on Render
RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build the application
COPY src src
RUN mvn -DskipTests clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render provides PORT at runtime. Locally it falls back to 8080.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
