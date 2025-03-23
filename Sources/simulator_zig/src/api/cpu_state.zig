const std = @import("std");
const Instruction = @import("../instruction.zig").Instruction;
const RegId = @import("../register.zig").PhyRegister;
const MemoryLocation = @import("memory_location.zig").MemoryLocation;
pub const SimulationStatistics = @import("simulation_statistics.zig").SimulationStatistics;

pub const CpuState = struct {
    /// Current tick/clock of the CPU
    tick: i32 = 0,
    statistics: SimulationStatistics = .{},
    /// Parsed instructions of the program
    program: std.ArrayListUnmanaged(Instruction) = .empty,
    labels: std.ArrayListUnmanaged([]const u8) = .empty,
    blocks: std.ArrayListUnmanaged(i32) = .empty, // todo
    /// Pointer to actual memoory location. For WASM, host would read it itself
    memory: i32 = 0,
    /// Log of events
    log: std.ArrayListUnmanaged(LogEntry) = .empty,
};

pub const LogEntry = struct {
    message: []const u8 = "",
    /// cycle at which the message got produced
    timestamp: i32 = 0,
};

/// @brief Represents a symbol in the code.
/// Used for jumps, data pointers.
pub const Symbol = struct {
    /// Name of the symbol
    name: []const u8 = "",

    /// Value of the symbol. An instruction address, or a data address.
    /// This same object is also referenced by {@link com.gradle.superscalarsim.models.instruction.InputCodeArgument} in the instruction.
    /// So changing this value will change the value in the instruction.
    /// Can be null if the symbol is not yet resolved.
    value: i32,
    // todo value should be like
    // bits: number;
    // currentType: DataTypeEnum;
    // stringRepresentation: string;

    /// Memory location. Null for types other than data.
    memoryLocation: MemoryLocation = .{},

    /// Type of the symbol
    type: SymbolType,
};

pub const SymbolType = enum { LABEL, DATA };
