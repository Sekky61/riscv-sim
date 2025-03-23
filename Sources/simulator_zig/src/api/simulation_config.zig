const std = @import("std");
const CpuConfig = @import("cpu_config.zig").CpuConfig;
const MemoryLocation = @import("memory_location.zig").MemoryLocation;
pub const Allocator = std.mem.Allocator;

/// Represents an entry point, which can be a label (string) or an address (int).
pub const EntryPoint = union(enum) {
    /// Label string.
    label: []const u8,
    /// Address integer.
    address: i32,

    const default: EntryPoint = .{ .address = 0 };
};

/// Configuration for the simulation - code, memory, buffers, entry point etc.
pub const SimulationConfig = struct {
    /// Code to run.
    /// Must be part of the configuration because it is used to create the initial state
    code: []const u8 = "",

    /// Memory locations defined outside the code.
    memoryLocations: []MemoryLocation = &.{},

    /// Configuration of the CPU.
    cpuConfig: CpuConfig = CpuConfig.getDefaultConfiguration(),

    /// The address of the entry point of the code.
    /// Can be a label (string) or a number (int).
    /// Address 0 is the default entry point (does not need to be specified in JSON).
    entryPoint: EntryPoint = EntryPoint.default,
};
