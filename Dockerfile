# Multi-stage Dockerfile for building an Android (Kotlin) app with Gradle
# Focused on caching: separates SDK install, dependency resolution, and build

ARG BASE_IMAGE=eclipse-temurin:17-jdk

# ---------- base: JDK and utilities ----------
FROM ${BASE_IMAGE} AS base
SHELL ["/bin/bash", "-lc"]
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget unzip ca-certificates git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# ---------- sdk: Android SDK + Build-Tools ----------
FROM base AS sdk

ARG ANDROID_SDK_ROOT=/opt/android-sdk
ARG ANDROID_HOME=/opt/android-sdk
ARG CMDLINE_TOOLS_VERSION=11076708
ARG ANDROID_PLATFORM=android-34
ARG ANDROID_BUILD_TOOLS=34.0.0

ENV ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT} \
    ANDROID_HOME=${ANDROID_HOME} \
    PATH=${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/build-tools/${ANDROID_BUILD_TOOLS}:$PATH

RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
 && cd /tmp \
 && wget -q https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip -O cmdline-tools.zip \
 && unzip -q cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
 && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
 && rm -f cmdline-tools.zip \
 && yes | sdkmanager --licenses >/dev/null \
 && sdkmanager \
       "platform-tools" \
       "platforms;${ANDROID_PLATFORM}" \
       "build-tools;${ANDROID_BUILD_TOOLS}" >/dev/null

# ---------- deps: Warm Gradle caches by resolving dependencies ----------
FROM sdk AS deps

# Copy only Gradle wrapper and build scripts for better layer caching
COPY gradle/ ./gradle/
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle.properties ./
COPY app/build.gradle.kts ./app/build.gradle.kts
COPY app/gradle.properties ./app/gradle.properties

RUN chmod +x ./gradlew

# Use BuildKit cache mounts to persist Gradle caches between builds
# Resolve plugin and project dependencies without compiling sources
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon -q help \
 && ./gradlew --no-daemon -q :app:dependencies || true

# ---------- build: Compile and assemble APK ----------
FROM deps AS build
COPY . ./
RUN chmod +x ./gradlew
RUN --mount=type=cache,target=/root/.gradle \
    bash -lc 'printf "sdk.dir=%s\n" "$ANDROID_SDK_ROOT" > local.properties' \
 && ./gradlew --no-daemon assembleDebug

# ---------- artifact: Export build artifacts only ----------
FROM alpine:3.20 AS artifact
WORKDIR /out
COPY --from=build /workspace/app/build/outputs/apk/debug/*.apk /out/

# By default, show the contents of /out when running
CMD ["/bin/sh", "-lc", "ls -lh /out && echo 'APK(s) are in /out' && sleep infinity"]
