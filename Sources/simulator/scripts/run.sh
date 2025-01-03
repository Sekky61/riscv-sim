#!/bin/sh
# Usage: ./run.sh [args]

# Determine the directory where the script is located
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

# "$SCRIPT_DIR/../build/install/superscalar-simulator/bin/superscalar-simulator" $*
java -jar "$SCRIPT_DIR/../target/superscalar-simulator-1.1.0.jar" $*

# TODO: make not depend on version (like 1.1.0)

# Args could be: server --gcc-path=riscv64-unknown-linux-gnu-gcc

