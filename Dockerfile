# STAGE 1: Extract Tailscale
FROM alpine:latest AS tailscale_stage
WORKDIR /app
RUN apk add --no-cache curl tar
RUN curl -fsSL https://pkgs.tailscale.com/stable/tailscale_1.64.0_amd64.tgz | tar xzf - --strip-components=1

# STAGE 2: Build the Java JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build_stage
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# STAGE 3: Final Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy Tailscale from the first stage
COPY --from=tailscale_stage /app/tailscale /usr/local/bin/tailscale
COPY --from=tailscale_stage /app/tailscaled /usr/local/bin/tailscaled

# Copy the JAR from the build stage
COPY --from=build_stage /app/target/*.jar app.jar

# Startup Script with Userspace networking fix
# UPDATED: We use socks5h:// to ensure DNS is also handled by the tunnel
RUN echo '#!/bin/sh\n\
tailscaled --tun=userspace-networking --socks5-server=localhost:1055 & \n\
until tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-java; do \n\
  echo "Waiting for Tailscale..." \n\
  sleep 2 \n\
done \n\
echo "Tailscale is up! Starting Java..." \n\
# We pass the proxy settings directly to the JVM to avoid "Incompatible SOCKS version" errors
java -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1055 -jar app.jar' > /app/start.sh && chmod +x /app/start.sh

ENTRYPOINT ["/app/start.sh"]