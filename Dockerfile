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

# UPDATED: Added a 3-second sleep and improved the login logic
RUN echo '#!/bin/sh\n\
# Start the daemon in the background
tailscaled --tun=userspace-networking & \n\
\n\
# WAIT for the daemon to initialize to avoid the "panic" error
sleep 3 \n\
\n\
echo "Attempting to connect to Tailscale..." \n\
until tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-java; do \n\
  echo "Tailscale not ready yet, retrying in 2s..." \n\
  sleep 2 \n\
done \n\
\n\
echo "Tailscale is up! Starting Java..." \n\
java -jar app.jar' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 8080
ENTRYPOINT ["/app/start.sh"]