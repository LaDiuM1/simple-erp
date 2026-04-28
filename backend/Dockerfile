# syntax=docker/dockerfile:1.7

# ===== build stage — 빌드는 러너 native arch 에서 (QEMU 우회로 속도 개선) =====
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./
# Windows 체크아웃 시 git index 의 executable bit 가 누락되는 케이스 방어
RUN chmod +x ./gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew --version

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon

# ===== runtime stage — target arch (linux/arm64) =====
FROM eclipse-temurin:25-jre AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends tzdata curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --home /app --shell /usr/sbin/nologin app

ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar

USER app
EXPOSE 8080

# Xmx512m: 컨테이너 768M 한도 안에서 heap + metaspace + native 합쳐 안전한 상한
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
