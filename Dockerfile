ARG BASE_IMAGE=ghcr.io/ministryofjustice/hmpps-eclipse-temurin:21-jre-jammy
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-jammy AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

WORKDIR /app
ADD . .
RUN ./gradlew --no-daemon assemble

FROM ${BASE_IMAGE}
LABEL maintainer="HMPPS Digital Studio <info@digital.justice.gov.uk>"

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

USER root
RUN apt-get update -yq && \
    apt-get install -yq software-properties-common && \
    add-apt-repository ppa:mozillateam/ppa && \
    apt-get update && \
    apt-get -y upgrade && \
    apt-get install -y firefox-esr && \
    rm -rf /var/lib/apt/lists/*
ENV AUTOMATION_FIREFOX_BINARY=/usr/bin/firefox-esr

WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /app/build/libs/hmpps-ppud-automation-api*.jar /app/app.jar
COPY --from=builder --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.json /app
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.dev.json /app

USER 2000

ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+AlwaysActAsServerClassMachine", "-javaagent:/app/agent.jar", "-jar", "/app/app.jar"]
