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

COPY --from=tailscale_stage /app/tailscale /usr/local/bin/tailscale
COPY --from=tailscale_stage /app/tailscaled /usr/local/bin/tailscaled
COPY --from=build_stage /app/target/*.jar app.jar

# UPDATED: Re-added SOCKS5 server and explicitly told Java to use it
RUN echo '#!/bin/sh\n\
tailscaled --tun=userspace-networking --socks5-server=localhost:1055 & \n\
until tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-java; do \n\
  echo "Waiting for Tailscale..." \n\
  sleep 2 \n\
done \n\
echo "Tailscale is up! Starting Java..." \n\
# -DsocksProxyHost/Port is the reliable way to bridge Java to Tailscale
java -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1055 -jar app.jar' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 8080
ENTRYPOINT ["/app/start.sh"]