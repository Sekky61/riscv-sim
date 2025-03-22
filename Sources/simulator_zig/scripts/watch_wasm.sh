#!/usr/bin/env bash
set -euo pipefail

WASM_OUTPUT=${1:-"./zig-out"}

echo "Starting Zig wasm build in watch mode..."
# Does not work on Nix right now
zig build wasm --watch --prefix $WASM_OUTPUT 
#  --fetch
#--cache-dir $(pwd)/.zig-cache

