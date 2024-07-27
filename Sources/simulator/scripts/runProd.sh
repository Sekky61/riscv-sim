#!/bin/sh

# Determine the directory where the script is located
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

cd "$SCRIPT_DIR/.."
./gradlew run --args="server" -Dconfig.profile=prod

