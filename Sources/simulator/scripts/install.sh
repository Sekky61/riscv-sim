#!/bin/sh

# Determine the directory where the script is located
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

# Usage: ./install.sh
cd "$SCRIPT_DIR/.." # Needs to be in the root directory of the project
./gradlew assembleDist installDist

