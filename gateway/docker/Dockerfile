# Lights gateway - built on the NAS by Container Manager (Project), see README.txt
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY lights-gateway.jar ./
COPY site/ site/

ENV LIGHTS_PORT=8321 \
    LIGHTS_SITE_DIR=/app/site \
    LIGHTS_DATA_DIR=/data

EXPOSE 8321
CMD ["java", "-Xmx128m", "-XX:+UseSerialGC", "-jar", "/app/lights-gateway.jar"]
