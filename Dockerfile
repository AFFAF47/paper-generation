# Stage 1: Build Tailscale binary
FROM alpine:latest as tailscale
WORKDIR /app
RUN apk add --no-cache curl tar
RUN curl -fsSL https://pkgs.tailscale.com/stable/tailscale_1.64.0_amd64.tgz | tar xzf - --strip-components=1

# Stage 2: Your Java App
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy Tailscale binaries from Stage 1
COPY --from=tailscale /app/tailscale /usr/local/bin/tailscale
COPY --from=tailscale /app/tailscaled /usr/local/bin/tailscaled

# Copy your Spring Boot JAR
COPY target/*.jar app.jar

# Create a startup script
RUN echo '#!/bin/sh\n\
tailscaled --state=mem: & \n\
tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-java && \n\
java -jar app.jar' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 8080
ENTRYPOINT ["/app/start.sh"]