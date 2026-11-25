FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace
COPY . .
RUN ./gradlew :app:bootJar --no-daemon --max-workers=1 \
    -Dkotlin.compiler.execution.strategy=in-process \
    -Dorg.gradle.jvmargs="-Xmx384m -XX:MaxMetaspaceSize=256m"

FROM eclipse-temurin:25-jre

RUN useradd --system --uid 10001 --create-home ctosfit

WORKDIR /app
COPY --from=build /workspace/app/build/libs/ctosfit.jar /app/ctosfit.jar

ENV JAVA_OPTS=""
EXPOSE 10000
USER ctosfit
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-10000} -jar /app/ctosfit.jar"]
