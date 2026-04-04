# ... (Keep Stage 1 and Stage 2 the same) ...

# STAGE 3: Final Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=tailscale /app/tailscale /usr/local/bin/tailscale
COPY --from=tailscale /app/tailscaled /usr/local/bin/tailscaled
COPY --from=build /app/target/*.jar app.jar

# UPDATED: Use userspace networking and socks5 proxy if needed
RUN echo '#!/bin/sh\n\
tailscaled --tun=userspace-networking --socks5-server=localhost:1055 & \n\
until tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-java; do \n\
  echo "Waiting for tailscale to come up..." \n\
  sleep 2 \n\
done \n\
echo "Tailscale is up! Starting Java..." \n\
java -jar app.jar' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 8080
ENTRYPOINT ["/app/start.sh"]