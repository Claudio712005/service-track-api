FROM eclipse-temurin:21-jdk-jammy AS build

ENV GRADLE_USER_HOME=/root/.gradle

WORKDIR /workspace

COPY gradlew gradlew.bat ./
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY gradle/           gradle/
COPY settings.gradle.kts \
     build.gradle.kts \
     jacoco-config.gradle.kts \
     gradle.properties ./
COPY openapi.yaml ./
COPY openApi/          openApi/

COPY _domain/build.gradle.kts         _domain/
COPY _application/build.gradle.kts    _application/
COPY _infrastructure/build.gradle.kts _infrastructure/

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew dependencies -q

COPY _domain/src/         _domain/src/
COPY _application/src/    _application/src/
COPY _infrastructure/src/ _infrastructure/src/

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew :_infrastructure:build -x test --no-build-cache

FROM eclipse-temurin:21-jre-alpine

ENV TZ=UTC \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8"

WORKDIR /app

RUN addgroup -S servicetrack && adduser -S servicetrack -G servicetrack

COPY --from=build --chown=servicetrack:servicetrack \
    /workspace/_infrastructure/build/quarkus-app ./

USER servicetrack

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
    "-jar", "quarkus-run.jar"]
