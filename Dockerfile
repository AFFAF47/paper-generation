# STAGE 1: Build the Java JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build_stage
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# STAGE 2: Final Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 1. Add the AWS Lambda Web Adapter extension
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter

# 2. Tell the adapter and Spring Boot which port to target
ENV PORT=8080
ENV AWS_LWA_READINESS_CHECK_PATH=/exams

# Copy the built JAR from Stage 1
COPY --from=build_stage /app/target/*.jar app.jar

EXPOSE 8080

# 3. Fast-boot JVM flags (reduces Spring Boot cold start time)
ENTRYPOINT ["java", "-XX:+TieredCompilation", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]