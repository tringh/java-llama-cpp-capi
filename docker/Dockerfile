# =================================================================================================
# STAGE 1: Builder
#
# In this stage, we download and prepare IntelliJ IDEA, Java tools, and C/C++ tools.
# This keeps the final production image lean by not including temporary build tools.
# =================================================================================================
FROM ubuntu:24.04 AS builder

# Set versions using ARGs for easy updates
ARG IDEA_VERSION=2025.2.4
ARG GRADLE_VERSION=9.1.0
ARG MAVEN_VERSION=3.9.11
ARG JEXTRACT_VERSION=22-6
# --- Added C/C++ Tool Versions ---
ARG CMAKE_VERSION=3.31.2
ARG NINJA_VERSION=1.12.1
ARG CUDA_VERSION=13.0.2

ENV IDEA_VERSION=${IDEA_VERSION}
ENV GRADLE_VERSION=${GRADLE_VERSION}
ENV MAVEN_VERSION=${MAVEN_VERSION}
ENV CMAKE_VERSION=${CMAKE_VERSION}
ENV NINJA_VERSION=${NINJA_VERSION}

# Install build-time dependencies (merged from both files)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    tar \
    unzip \
    wget \
    gnupg2 && \
    rm -rf /var/lib/apt/lists/*

# --- Download and Extract IntelliJ IDEA ---
ARG IDEA_INSTALL_DIR=/opt/jetbrains/idea
RUN mkdir -p ${IDEA_INSTALL_DIR}
RUN curl -L "https://download.jetbrains.com/idea/ideaIU-${IDEA_VERSION}.tar.gz" | \
    tar -xz -C ${IDEA_INSTALL_DIR} --strip-components=1

# --- Download and Extract Gradle ---
ARG GRADLE_INSTALL_DIR=/opt/gradle
RUN mkdir -p ${GRADLE_INSTALL_DIR} && \
    curl -L "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-all.zip" -o /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /tmp && \
    mv /tmp/gradle-${GRADLE_VERSION}/* ${GRADLE_INSTALL_DIR}/ && \
    rm -rf /tmp/gradle.zip /tmp/gradle-${GRADLE_VERSION}

# --- Download and Extract Maven ---
ARG MAVEN_INSTALL_DIR=/opt/maven
RUN mkdir -p ${MAVEN_INSTALL_DIR} && \
    curl -L "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /tmp && \
    mv /tmp/apache-maven-${MAVEN_VERSION}/* ${MAVEN_INSTALL_DIR}/ && \
    rm -rf /tmp/maven.tar.gz /tmp/apache-maven-${MAVEN_VERSION}

# --- Download and Extract JExtract ---
ARG JEXTRACT_INSTALL_DIR=/opt/jextract
RUN mkdir -p ${JEXTRACT_INSTALL_DIR} && \
    curl -L "https://download.java.net/java/early_access/jextract/22/6/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz" -o /tmp/jextract.tar.gz && \
    tar -xzf /tmp/jextract.tar.gz -C /tmp && \
    mv /tmp/jextract-22/* ${JEXTRACT_INSTALL_DIR}/ && \
    rm -rf /tmp/jextract.tar.gz /tmp/jextract-22

# --- Download Oracle JDKs ---
RUN curl -L "https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.deb" -o /tmp/jdk-25.deb
RUN curl -L "https://download.oracle.com/java/22/archive/jdk-22.0.2_linux-x64_bin.deb" -o /tmp/jdk-22.deb
RUN curl -L "https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb" -o /tmp/jdk-21.deb

# --- Download and Extract CMake (from CLion file) ---
ARG CMAKE_INSTALL_DIR=/opt/cmake
RUN mkdir -p ${CMAKE_INSTALL_DIR} && \
    curl -L "https://github.com/Kitware/CMake/releases/download/v${CMAKE_VERSION}/cmake-${CMAKE_VERSION}-linux-x86_64.tar.gz" | \
    tar -xz -C ${CMAKE_INSTALL_DIR} --strip-components=1

# --- Download and Extract Ninja (from CLion file) ---
ARG NINJA_INSTALL_DIR=/opt/ninja
RUN mkdir -p ${NINJA_INSTALL_DIR} && \
    curl -L "https://github.com/ninja-build/ninja/releases/download/v${NINJA_VERSION}/ninja-linux.zip" -o /tmp/ninja.zip && \
    unzip -q /tmp/ninja.zip -d ${NINJA_INSTALL_DIR} && \
    rm /tmp/ninja.zip

# =================================================================================================
# STAGE 2: Final Production Image
#
# This is the final, optimized image. It includes Java, C/C++, CUDA, dev tools,
# and the IntelliJ IDEA server, ready for remote development.
# =================================================================================================
FROM ubuntu:24.04

# Set versions again for use in this stage
ARG IDEA_VERSION=2025.2.4
ARG CUDA_VERSION=13.0.2
ENV IDEA_VERSION=${IDEA_VERSION}
ENV CUDA_VERSION=${CUDA_VERSION}

# --- Environment Setup ---
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Etc/UTC

# --- Install System Dependencies & Essential Dev Tools (Merged List) ---
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    # SSH Server
    openssh-server \
    ca-certificates \
    # Java-specific tools
    graphviz \
    fontconfig \
    # Version Control & Utilities
    git \
    wget \
    curl \
    unzip \
    nano \
    sudo \
    # C/C++ Build Tools & Compilers
    build-essential \
    gcc \
    g++ \
    gdb \
    clang \
    clang-format \
    clang-tidy \
    lldb \
    # Development Libraries
    libc6-dev \
    libstdc++-14-dev \
    libcurl4-openssl-dev \
    # Memory & Performance Analysis Tools
    valgrind \
    # Additional C/C++ Utilities
    pkg-config \
    autoconf \
    automake \
    libtool \
    make \
    # System monitoring
    procps \
    htop \
    # Documentation & Help
    man-db \
    manpages-dev \
    # Required for CUDA installation
    gnupg2 \
    software-properties-common && \
    rm -rf /var/lib/apt/lists/*

# --- Install Oracle JDKs ---
COPY --from=builder /tmp/jdk-25.deb /tmp/jdk-25.deb
COPY --from=builder /tmp/jdk-22.deb /tmp/jdk-22.deb
COPY --from=builder /tmp/jdk-21.deb /tmp/jdk-21.deb
RUN dpkg -i /tmp/jdk-25.deb && \
    dpkg -i /tmp/jdk-22.deb && \
    dpkg -i /tmp/jdk-21.deb && \
    rm /tmp/jdk-*.deb && \
    ln -s "$(find /usr/lib/jvm -maxdepth 1 -name 'jdk-25.*' -type d | head -n 1)" /usr/lib/jvm/jdk-25 && \
    ln -s "$(find /usr/lib/jvm -maxdepth 1 -name 'jdk-22.*' -type d | head -n 1)" /usr/lib/jvm/jdk-22 && \
    ln -s "$(find /usr/lib/jvm -maxdepth 1 -name 'jdk-21.*' -type d | head -n 1)" /usr/lib/jvm/jdk-21 && \
    ls -ld /usr/lib/jvm/jdk-25/bin/java

# --- Set up Java environment variables (default to JDK 25) ---
ENV JAVA_HOME=/usr/lib/jvm/jdk-25

# --- Install NVIDIA CUDA Toolkit (from CLion file) ---
RUN wget https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2404/x86_64/cuda-keyring_1.1-1_all.deb && \
    dpkg -i cuda-keyring_1.1-1_all.deb && \
    rm cuda-keyring_1.1-1_all.deb

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    cuda-toolkit-13-0 \
    cuda-cudart-dev-13-0 \
    cuda-nvcc-13-0 \
    cuda-libraries-dev-13-0 \
    libcublas-dev-13-0 \
    libcurand-dev-13-0 && \
    rm -rf /var/lib/apt/lists/*

# --- Set up CUDA environment variables (from CLion file) ---
ENV CUDA_HOME=/usr/local/cuda-13.0
ENV CUDA_PATH=/usr/local/cuda-13.0
ENV LD_LIBRARY_PATH=${CUDA_HOME}/lib64:${LD_LIBRARY_PATH}

# --- Install Gradle ---
ARG GRADLE_INSTALL_DIR=/opt/gradle
COPY --from=builder ${GRADLE_INSTALL_DIR} ${GRADLE_INSTALL_DIR}
ENV GRADLE_HOME=${GRADLE_INSTALL_DIR}

# --- Install Maven ---
ARG MAVEN_INSTALL_DIR=/opt/maven
COPY --from=builder ${MAVEN_INSTALL_DIR} ${MAVEN_INSTALL_DIR}
ENV MAVEN_HOME=${MAVEN_INSTALL_DIR}
ENV M2_HOME=${MAVEN_INSTALL_DIR}

# --- Install JExtract ---
ARG JEXTRACT_INSTALL_DIR=/opt/jextract
COPY --from=builder ${JEXTRACT_INSTALL_DIR} ${JEXTRACT_INSTALL_DIR}
ENV JEXTRACT_INSTALL_DIR=${JEXTRACT_INSTALL_DIR}

# --- Install CMake (from CLion file) ---
ARG CMAKE_INSTALL_DIR=/opt/cmake
COPY --from=builder ${CMAKE_INSTALL_DIR} ${CMAKE_INSTALL_DIR}
ENV CMAKE_HOME=${CMAKE_INSTALL_DIR}

# --- Install Ninja (from CLion file) ---
ARG NINJA_INSTALL_DIR=/opt/ninja
COPY --from=builder ${NINJA_INSTALL_DIR} ${NINJA_INSTALL_DIR}
ENV NINJA_HOME=${NINJA_INSTALL_DIR}

# ========================================================================================
# >>>>>>>>>>>>>>>>>>>>>>>>>>>> MODIFIED SECTION START <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
# ========================================================================================
# --- Configure Environment PATH for all command-line tools (Merged) ---

# 1. Set the PATH for non-login shells (e.g., `docker exec`)
ENV PATH="${JAVA_HOME}/bin:${GRADLE_HOME}/bin:${MAVEN_HOME}/bin:${JEXTRACT_INSTALL_DIR}/bin:${CUDA_HOME}/bin:${CMAKE_HOME}/bin:${NINJA_HOME}:${PATH}"

# 2. For SSH login shells, write the DIRECT paths to the system-wide profile
RUN echo 'export PATH="/usr/lib/jvm/jdk-25/bin:/opt/gradle/bin:/opt/maven/bin:/opt/jextract/bin:/usr/local/cuda-13.0/bin:/opt/cmake/bin:/opt/ninja:${PATH}"' >> /etc/profile && \
    echo 'export LD_LIBRARY_PATH="/usr/local/cuda-13.0/lib64:${LD_LIBRARY_PATH}"' >> /etc/profile && \
    echo 'export CUDA_HOME=/usr/local/cuda-13.0' >> /etc/profile
# ========================================================================================
# >>>>>>>>>>>>>>>>>>>>>>>>>>>> MODIFIED SECTION END <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
# ========================================================================================

# --- Remove the conflicting default 'ubuntu' user ---
# This user often has UID 1000, which can conflict with the host user.
RUN if id "ubuntu" &>/dev/null; then userdel -r ubuntu; fi

# --- Create a non-root user 'developer' ---
RUN groupadd developer && \
    useradd --gid developer -m -s /bin/bash developer && \
    echo 'developer:password' | chpasswd && \
    usermod -aG sudo developer

# Allow passwordless sudo for convenience
RUN echo '%developer ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

# --- Configure and Prepare SSH Server ---
RUN sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config && \
    mkdir -p /var/run/sshd && \
    ssh-keygen -A

# --- Create Project Workspace inside the user's home directory ---
RUN mkdir -p /home/developer/workspace && \
    chown developer:developer /home/developer/workspace

# --- Create .hushlogin to disable MOTD on login ---
RUN touch /home/developer/.hushlogin && \
    chown developer:developer /home/developer/.hushlogin

# --- Install IntelliJ IDEA ---
ARG IDEA_INSTALL_DIR=/opt/jetbrains/idea
COPY --from=builder ${IDEA_INSTALL_DIR} ${IDEA_INSTALL_DIR}

# --- Switch to developer user to register the IDE ---
USER developer
WORKDIR /home/developer

# --- CRITICAL STEP: Register the IDE backend for Gateway ---
#RUN ${IDEA_INSTALL_DIR}/bin/remote-dev-server.sh registerBackendLocationForGateway

# [FIX] Prepend the export to .bashrc (Top of file strategy) and append to .profile
# This ensures CLion non-interactive shells still see the socket.
RUN echo "export SSH_AUTH_SOCK=/ssh-agent" > ~/.bashrc_temp && \
    ([ -f ~/.bashrc ] && cat ~/.bashrc >> ~/.bashrc_temp || true) && \
    mv ~/.bashrc_temp ~/.bashrc && \
    echo "export SSH_AUTH_SOCK=/ssh-agent" >> ~/.profile && \
    touch /home/developer/.hushlogin && \
    ${IDEA_INSTALL_DIR}/bin/remote-dev-server.sh registerBackendLocationForGateway

# Switch back to root for final setup
USER root

# --- Setup Runtime Entrypoint for Permission Handling ---
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Expose the SSH port
EXPOSE 22

# Use the entrypoint to handle user permissions and then start the SSH server
ENTRYPOINT ["entrypoint.sh"]
