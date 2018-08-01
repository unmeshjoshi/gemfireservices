FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 8080 1099 10334 40404
COPY libs/pivotal-gemfire-9.5.1.tgz /
RUN apk add bash
RUN tar -xvzf /pivotal-gemfire-9.5.1.tgz
