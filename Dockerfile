FROM amazoncorretto:21-alpine-jdk

# Preparar directorios y permisos
WORKDIR /deployments
USER root
RUN chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments
USER 1001

# Copiar los artefactos generados por Gradle
# Nota: Asegúrate de que la ruta coincida con la estructura de directorios de tu proyecto
COPY --chown=1001:root build/quarkus-app/lib/ /deployments/lib/
COPY --chown=1001:root build/quarkus-app/*.jar /deployments/
COPY --chown=1001:root build/quarkus-app/app/ /deployments/app/
COPY --chown=1001:root build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8088
ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]