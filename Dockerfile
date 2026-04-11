# STAGE 1: Build the Java JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build_stage
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# STAGE 2: Final Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build_stage /app/target/*.jar app.jar

# Simple entrypoint - no sidecars needed!
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]