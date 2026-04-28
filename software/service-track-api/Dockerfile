FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY settings.gradle.kts build.gradle.kts jacoco-config.gradle.kts ./
COPY openapi.yaml ./
COPY openApi/ openApi/

COPY gradle.properties ./

COPY _domain/build.gradle.kts          _domain/
COPY _application/build.gradle.kts     _application/
COPY _infrastructure/build.gradle.kts  _infrastructure/

RUN chmod +x gradlew

RUN ./gradlew dependencies -q 2>&1 | tail -3 || true

COPY _domain/src/          _domain/src/
COPY _application/src/     _application/src/
COPY _infrastructure/src/  _infrastructure/src/

RUN ./gradlew :_infrastructure:build -x test

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