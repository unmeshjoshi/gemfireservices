FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 8080 1099 10334 40404
COPY archive/pivotal-gemfire-9.1.0.tar.gz /
RUN apk add bash
RUN tar -xvzf /pivotal-gemfire-9.1.0.tar.gz
RUN rm -f /pivotal-gemfire-9.1.0.tar.gz
COPY provisioning/* /pivotal-gemfire-9.1.0/config/