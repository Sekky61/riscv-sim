# RISC-V Simulator

This application provides a simulator for the risc-v superscalar processor.
It operates in two modes: cli and http server.
A dockerfile is included for deployment convenience.

## pre-requisites

1. [zig](https://ziglang.org) master branch (Current specific commit is specified in Nix)
2. gcc for risc-v (`gcc-riscv-none-elf`)
   gcc is only needed for the server mode.
   you can find installation details in the [risc-v toolchain repository](https://github.com/riscv-collab/riscv-gnu-toolchain).
   specify the gcc executable path using the `help` command if it isn't in your default path.

## installation

Run the `zig build` command to compile the application.

## Usage

The executable `riscvsim` is created in `zig-out/bin`.

## Prompt

I am rewriting a RISC-V superscalar simulator in Zig with a focus on both flexibility and performance. The design goals are as follows:

1. **Modular and Parameterizable Architecture:**  
   - All simulation units (e.g., fetch unit, decode unit, cache) must be fully configurable and independent.  
   - Units should be connectable in arbitrary configurations without creating tight data dependencies.

2. **Tick-Based Simulation Model:**  
   - Each simulation tick represents a discrete step — for example, advancing a batch of 4 instructions from decode to the ROB.  
   - The order of operations is crucial (e.g., running decode before fetch to prevent an instruction from moving twice during a tick).

3. **Serializable System State:**  
   - As the final simulator runs in a WASM environment within a web app, the entire simulator state must be serializable.  
   - State includes registers, speculative registers, instruction buffers, statistics, and any other simulation-relevant data.

4. **Separation of Instruction Data and Metadata:**  
   - Instruction blocks should reference an internal instruction buffer.  
   - There must be a clear distinction between the instruction instance (e.g., `addi x1, x2, 5`) and its associated metadata.  
   - To enforce encapsulation, blocks should communicate only via a dedicated wiring or bus system without directly accessing each other’s data.

