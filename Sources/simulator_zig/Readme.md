# RISC-V Simulator

This application provides a simulator for the risc-v superscalar processor.
It operates in two modes: cli and http server.
A dockerfile is included for deployment convenience.

## pre-requisites

1. [zig](https://ziglang.org) 0.13.0 or later
2. gcc for risc-v (`gcc-riscv-none-elf`)
   gcc is only needed for the server mode.
   you can find installation details in the [risc-v toolchain repository](https://github.com/riscv-collab/riscv-gnu-toolchain).
   specify the gcc executable path using the `help` command if it isn't in your default path.

## installation

Run the `zig build` command to compile the application.

## Usage

The executable `riscvsim` is created in `zig-out/bin`.


