# STAGE 1: Build Tailscale
FROM alpine:latest AS tailscale
WORKDIR /app
RUN apk add --no-cache curl tar
RUN curl -fsSL https://pkgs.tailscale.com/stable/tailscale_1.64.0_amd64.tgz | tar xzf - --strip-components=1

# STAGE 2: Build the Java JAR (The missing link)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# STAGE 3: Final Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy Tailscale
COPY --from=tailscale /app/tailscale /usr/local/bin/tailscale
COPY --from=tailscale /app/tailscaled /usr/local/bin/tailscaled

# Copy the JAR from the BUILD stage (not your local machine)
COPY --from=build /app/target/*.jar app.jar

# Startup Script
RUN echo '#!/bin/sh\n\
tailscaled --state=mem: & \n\
tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-java && \n\
java -jar app.jar' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 8080
ENTRYPOINT ["/app/start.sh"]