ARG BASE_IMAGE=ghcr.io/ministryofjustice/hmpps-eclipse-temurin:21-jre-jammy
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-jammy AS builder

# BUILD_NUMBER is provided as a build argument by the build pipeline
ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

WORKDIR /app
ADD . .
RUN ./gradlew --no-daemon assemble

FROM ${BASE_IMAGE}

# BUILD_NUMBER is provided as a build argument by the build pipeline
ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

# the base image changes the user as its last command, but we need
# root access for the commands below. we switch it back at the end
USER root

RUN apt-get update -yq  && \
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

ENTRYPOINT ["java", "-XX:+AlwaysActAsServerClassMachine", "-javaagent:/app/agent.jar", "-jar", "/app/app.jar"]
