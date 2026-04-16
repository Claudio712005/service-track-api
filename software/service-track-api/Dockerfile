# Build stage para OS Mac
FROM --platform=linux/amd64 eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY settings.gradle.kts build.gradle.kts jacoco-config.gradle.kts ./
COPY openapi.yaml ./
COPY openApi/ openApi/

COPY _domain/build.gradle.kts          _domain/
COPY _application/build.gradle.kts     _application/
COPY _infrastructure/build.gradle.kts  _infrastructure/

RUN chmod +x gradlew

ENV GRADLE_OPTS="-Dorg.gradle.native=false"

RUN ./gradlew :_infrastructure:dependencies --no-daemon -q 2>&1 | tail -3 || true

COPY _domain/src/          _domain/src/
COPY _application/src/     _application/src/
COPY _infrastructure/src/  _infrastructure/src/

RUN ./gradlew :_infrastructure:build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S servicetrack && adduser -S servicetrack -G servicetrack

COPY --from=build --chown=servicetrack:servicetrack \
    /workspace/_infrastructure/build/quarkus-app ./

USER servicetrack

EXPOSE 8080

CMD ["java", \
     "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
     "-jar", "quarkus-run.jar"]