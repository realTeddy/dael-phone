#!/usr/bin/env bash
# Installs the JDK needed to build Android apps from the command line.
set -euo pipefail
pacman -S --needed --noconfirm jdk17-openjdk
archlinux-java set java-17-openjdk
echo "Done. JDK: $(java -version 2>&1 | head -1)"
