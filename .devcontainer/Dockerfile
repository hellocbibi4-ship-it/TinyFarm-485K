FROM mcr.microsoft.com/devcontainers/base:ubuntu

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
  && apt-get install -y git-lfs \
    openjdk-17-jdk \
    maven \
    ca-certificates \
    wget \
    gnupg \
  && rm -rf /var/lib/apt/lists/*

USER vscode
