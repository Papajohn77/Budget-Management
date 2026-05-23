# JVM-mode Dockerfile for any Quarkus service in this monorepo.
# Build context: repo root. Pass SERVICE as a build-arg, e.g.:
#   docker build --build-arg SERVICE=identity-service -t identity-service .
# Prerequisite: `mvn package` has produced services/<service>/target/quarkus-app/.

FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.24

ARG SERVICE
ENV LANGUAGE='en_US:en'


# Four distinct layers so library changes don't invalidate the app layer cache
COPY --chown=185 services/${SERVICE}/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 services/${SERVICE}/target/quarkus-app/*.jar /deployments/
COPY --chown=185 services/${SERVICE}/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 services/${SERVICE}/target/quarkus-app/quarkus/ /deployments/quarkus/

USER root
RUN mkdir -p /data && chown 185 /data
USER 185

EXPOSE 8080

ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
