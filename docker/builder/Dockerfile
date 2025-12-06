# =================================================================================================
# STAGE 1: Builder
# =================================================================================================
FROM ubuntu:24.04 AS builder

# --- Tool Versions ---
ARG GRADLE_VERSION=9.1.0
ARG MAVEN_VERSION=3.9.11
ARG JEXTRACT_VERSION=22-6
ARG CMAKE_VERSION=3.31.2
ARG NINJA_VERSION=1.12.1

# --- Install Dependencies for Downloading ---
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    tar \
    unzip \
    gnupg2 && \
    rm -rf /var/lib/apt/lists/*

# --- Download Oracle JDK 25 ---
RUN curl -L "https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.deb" -o /tmp/jdk-25.deb

# --- Download Gradle ---
ARG GRADLE_INSTALL_DIR=/opt/gradle
RUN mkdir -p ${GRADLE_INSTALL_DIR} && \
    curl -L "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-all.zip" -o /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /tmp && \
    mv /tmp/gradle-${GRADLE_VERSION}/* ${GRADLE_INSTALL_DIR}/

# --- Download Maven ---
ARG MAVEN_INSTALL_DIR=/opt/maven
RUN mkdir -p ${MAVEN_INSTALL_DIR} && \
    curl -L "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /tmp && \
    mv /tmp/apache-maven-${MAVEN_VERSION}/* ${MAVEN_INSTALL_DIR}/

# --- Download JExtract ---
ARG JEXTRACT_INSTALL_DIR=/opt/jextract
RUN mkdir -p ${JEXTRACT_INSTALL_DIR} && \
    curl -L "https://download.java.net/java/early_access/jextract/22/6/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz" -o /tmp/jextract.tar.gz && \
    tar -xzf /tmp/jextract.tar.gz -C /tmp && \
    mv /tmp/jextract-22/* ${JEXTRACT_INSTALL_DIR}/

# --- Download CMake ---
ARG CMAKE_INSTALL_DIR=/opt/cmake
RUN mkdir -p ${CMAKE_INSTALL_DIR} && \
    curl -L "https://github.com/Kitware/CMake/releases/download/v${CMAKE_VERSION}/cmake-${CMAKE_VERSION}-linux-x86_64.tar.gz" | \
    tar -xz -C ${CMAKE_INSTALL_DIR} --strip-components=1

# --- Download Ninja ---
ARG NINJA_INSTALL_DIR=/opt/ninja
RUN mkdir -p ${NINJA_INSTALL_DIR} && \
    curl -L "https://github.com/ninja-build/ninja/releases/download/v${NINJA_VERSION}/ninja-linux.zip" -o /tmp/ninja.zip && \
    unzip -q /tmp/ninja.zip -d ${NINJA_INSTALL_DIR}

# =================================================================================================
# STAGE 2: CI/CD Runner Image
# =================================================================================================
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Etc/UTC

# --- 1. Install System Build Tools ---
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ca-certificates \
    git \
    wget \
    curl \
    unzip \
    build-essential \
    gcc \
    g++ \
    gdb \
    clang \
    clang-format \
    clang-tidy \
    lldb \
    libc6-dev \
    libstdc++-14-dev \
    libcurl4-openssl-dev \
    valgrind \
    pkg-config \
    autoconf \
    automake \
    libtool \
    make && \
    rm -rf /var/lib/apt/lists/*

# --- 2. Install JDK 25 ---
COPY --from=builder /tmp/jdk-25.deb /tmp/jdk-25.deb
RUN dpkg -i /tmp/jdk-25.deb && \
    rm /tmp/jdk-25.deb && \
    ln -s "$(find /usr/lib/jvm -maxdepth 1 -name 'jdk-25.*' -type d | head -n 1)" /usr/lib/jvm/jdk-25

ENV JAVA_HOME=/usr/lib/jvm/jdk-25

# --- 3. Copy Tools from Builder ---
COPY --from=builder /opt/gradle /opt/gradle
COPY --from=builder /opt/maven /opt/maven
COPY --from=builder /opt/jextract /opt/jextract
COPY --from=builder /opt/cmake /opt/cmake
COPY --from=builder /opt/ninja /opt/ninja

# --- 4. Setup Environment Paths ---
ENV GRADLE_HOME=/opt/gradle
ENV MAVEN_HOME=/opt/maven
ENV M2_HOME=/opt/maven
ENV JEXTRACT_INSTALL_DIR=/opt/jextract
ENV CMAKE_HOME=/opt/cmake
ENV NINJA_HOME=/opt/ninja

ENV PATH="${JAVA_HOME}/bin:${GRADLE_HOME}/bin:${MAVEN_HOME}/bin:${JEXTRACT_INSTALL_DIR}/bin:${CMAKE_HOME}/bin:${NINJA_HOME}:${PATH}"

# --- 5. Runner Config ---

CMD ["/bin/bash"]