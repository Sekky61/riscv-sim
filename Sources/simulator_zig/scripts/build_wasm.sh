#!/usr/bin/env bash
set -euo pipefail

WASM_OUTPUT=${1:-"./zig-out"}

echo "Building Zig wasm..."
zig build wasm --prefix $WASM_OUTPUT --cache-dir $(pwd)/.zig-cache
echo "Built and copied wasm file to $WASM_OUTPUT"
