FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace
COPY . .
RUN ./gradlew :app:bootJar --no-daemon --max-workers=1 \
    -Dkotlin.compiler.execution.strategy=in-process \
    -Dorg.gradle.jvmargs="-Xmx384m -XX:MaxMetaspaceSize=256m"

FROM eclipse-temurin:25-jre

RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --uid 10001 --create-home ctosfit

WORKDIR /app
COPY --from=build /workspace/app/build/libs/ctosfit.jar /app/ctosfit.jar

ENV JAVA_OPTS=""
EXPOSE 10000
USER ctosfit
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD curl --fail --silent "http://127.0.0.1:${PORT:-10000}/actuator/health/readiness" || exit 1
ENTRYPOINT ["sh", "-c", "if [ -n \"${CTOSFIT_DATABASE_URL:-}\" ] && [ \"${CTOSFIT_DATABASE_URL#jdbc:}\" = \"$CTOSFIT_DATABASE_URL\" ]; then export CTOSFIT_DATABASE_URL=\"jdbc:${CTOSFIT_DATABASE_URL}\"; fi; exec java $JAVA_OPTS -Dserver.port=${PORT:-10000} -jar /app/ctosfit.jar"]
