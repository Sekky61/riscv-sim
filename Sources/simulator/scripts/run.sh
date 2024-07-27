#!/bin/sh
# Usage: ./run.sh [args]

# Determine the directory where the script is located
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

"$SCRIPT_DIR/../build/install/superscalar-simulator/bin/superscalar-simulator" $*

