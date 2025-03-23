const std = @import("std");
const Allocator = std.mem.Allocator;

/// Enum representing the predictor type.
pub const PredictorType = enum {
    ZERO_BIT_PREDICTOR,
    ONE_BIT_PREDICTOR,
    TWO_BIT_PREDICTOR,
};

/// A functional unit capability.
pub const Capability = struct {
    /// The name of the capability.
    capabilityName: []const u8,
    /// The latency in cycles.
    latency: i32,
};

/// Enum representing the type of a functional unit.
pub const FunctionalUnitType = enum {
    FX,
    FP,
    L_S,
    Branch,
    Memory,
};

/// A description of a functional unit.
pub const FunctionalUnitDescription = struct {
    /// A unique id for the functional unit.
    id: i32 = 0,

    /// The type of the functional unit.
    unitType: FunctionalUnitType,

    /// Two modes:
    /// - detailed: list of capabilities.
    /// - simple: count value for functional units.
    detail: union(enum) {
        /// Detailed description as an array of capabilities.
        capabilities: []const Capability,
        /// A simple count of functional units.
        count: i32,
    },

    /// Display name of the functional unit.
    displayName: []const u8,
};

/// Configuration for a CPU.
///
/// - The field `name` is provided for organizational purposes and is not used in simulation.
/// - Other fields determine the CPU simulation parameters.
pub const CpuConfig = struct {
    /// Provided for organizational purposes.
    /// Not used in the simulation. Displayed on frontend.
    name: ?[]const u8 = null,

    /// Maximum number of instructions that can be in the ROB.
    robSize: i32 = 0,

    /// Number of instructions that can be committed in one cycle - commitLimit on the ROB.
    commitWidth: i32 = 0,

    /// Number of clock cycles the CPU will take to flush the pipeline.
    /// For example, if a branch is mispredicted, the CPU will take this many cycles to clear the fetch, decode, and ROB.
    flushPenalty: i32 = 0,

    /// Number of instructions that can be fetched in one cycle.
    /// Also determines the decode width.
    /// Fetch unit can _evaluate_ one branch instruction per cycle. This means the fetch will stop before a second branch.
    fetchWidth: i32 = 0,

    /// Number of branch instructions that can be evaluated in one cycle.
    branchFollowLimit: i32 = 0,

    /// Branch target buffer size.
    btbSize: i32 = 0,

    /// Pattern history table size.
    phtSize: i32 = 0,

    /// Type of the predictor held in the PHT.
    /// One of ZERO_BIT_PREDICTOR, ONE_BIT_PREDICTOR, TWO_BIT_PREDICTOR.
    predictorType: PredictorType = .TWO_BIT_PREDICTOR,

    /// The initial state of all predictors in the PHT.
    /// For zero bit it is either 1 ("Taken"), or 0 ("Not Taken").
    /// For one bit it is either 1 ("Taken"), or 0 ("Not Taken").
    /// For two bit one of 0 ("Strongly Not Taken"), 1 ("Weakly Not Taken"), 2 ("Weakly Taken"),
    /// 3 ("Strongly Taken").
    predictorDefaultState: i32 = 0,

    /// Use global history vector in the PHT.
    /// The GHV is a register that holds the last N branches. It affects addressing of the PHT.
    useGlobalHistory: bool = false,

    /// Defined functional units.
    fUnits: []const FunctionalUnitDescription = &.{},

    /// Use single level cache.
    useCache: bool = false,

    /// Number of cache lines. Must be a power of two multiple of cacheAssoc (example 8*assoc, 16*assoc).
    cacheLines: i32 = 0,

    /// Size of one cache line in bytes.
    cacheLineSize: i32 = 0,

    /// Cache associativity.
    cacheAssoc: i32 = 0,

    /// Cache replacement policy.
    cacheReplacement: CacheReplacement = .FIFO,

    /// One of write-back, write-through.
    storeBehavior: StoreBehavior = .@"write-back",

    /// Number of cycles that it takes to replace a cache line.
    /// New line is loaded into the cache after this many cycles.
    laneReplacementDelay: i32 = 0,

    /// Cache access delay in cycles.
    cacheAccessDelay: i32 = 0,

    /// Load buffer size.
    lbSize: i32 = 0,

    /// Store buffer size.
    sbSize: i32 = 0,

    /// Main memory latency for store.
    storeLatency: i32 = 0,

    /// Main memory latency for load.
    loadLatency: i32 = 0,

    /// Call stack size in bytes.
    /// Amount of memory allocated for the call stack.
    callStackSize: i32 = 0,

    /// Number of speculative registers.
    /// This is in addition to the 32 integer and 32 floating point architectural registers.
    speculativeRegisters: i32 = 0,

    /// Core clock frequency in Hz.
    coreClockFrequency: i32 = 0,

    /// Cache clock frequency in Hz.
    cacheClockFrequency: i32 = 0,

    /// Creates a default configuration.
    pub fn getDefaultConfiguration() CpuConfig {
        return .{
            // Organizational name can be provided.
            .name = "Default Configuration",
            // ROB parameters.
            .robSize = 256,
            .fetchWidth = 3,
            .branchFollowLimit = 1,
            .commitWidth = 4,
            .flushPenalty = 1,
            // Prediction.
            .btbSize = 1024,
            .phtSize = 100, // test small PHT (there might be issue with false sharing)
            .predictorType = PredictorType.TWO_BIT_PREDICTOR,
            // For "TWO_BIT_PREDICTOR", default state: Weakly Taken (2).
            .predictorDefaultState = 2,
            .useGlobalHistory = false,
            // Functional Units.
            .fUnits = &[_]FunctionalUnitDescription{
                .{
                    .id = 0,
                    .unitType = FunctionalUnitType.FX,
                    .detail = .{ .capabilities = &.{
                        .{ .capabilityName = "addition", .latency = 1 },
                        .{ .capabilityName = "bitwise", .latency = 1 },
                        .{ .capabilityName = "multiplication", .latency = 2 },
                        .{ .capabilityName = "division", .latency = 10 },
                        .{ .capabilityName = "special", .latency = 2 },
                    } },
                    .displayName = "FX",
                },
                .{
                    .id = 1,
                    .unitType = FunctionalUnitType.FP,
                    .detail = .{ .capabilities = &.{
                        .{ .capabilityName = "addition", .latency = 1 },
                        .{ .capabilityName = "bitwise", .latency = 1 },
                        .{ .capabilityName = "multiplication", .latency = 2 },
                        .{ .capabilityName = "division", .latency = 2 },
                        .{ .capabilityName = "special", .latency = 2 },
                    } },
                    .displayName = "FP",
                },
                .{
                    .id = 2,
                    .unitType = FunctionalUnitType.L_S,
                    .detail = .{ .count = 1 },
                    .displayName = "L/S",
                },
                .{
                    .id = 3,
                    .unitType = FunctionalUnitType.Branch,
                    .detail = .{ .count = 2 },
                    .displayName = "Branch",
                },
                .{
                    .id = 4,
                    .unitType = FunctionalUnitType.Memory,
                    .detail = .{ .count = 1 },
                    .displayName = "Memory",
                },
            },
            // Cache parameters.
            .useCache = true,
            .cacheLines = 16,
            .cacheLineSize = 32,
            .cacheAssoc = 2,
            .cacheReplacement = .LRU,
            .storeBehavior = .@"write-back",
            .cacheAccessDelay = 1,
            .laneReplacementDelay = 10,
            // Memory.
            .lbSize = 64,
            .sbSize = 64,
            .storeLatency = 1,
            .loadLatency = 1,
            .callStackSize = 512,
            // Misc.
            .speculativeRegisters = 620,
            .coreClockFrequency = 100000000,
            .cacheClockFrequency = 100000000,
        };
    }

    pub fn toJson(self: @This(), allocator: Allocator) ![]const u8 {
        var string: std.ArrayListUnmanaged(u8) = .{};
        try std.json.stringify(self, .{}, string.writer(allocator));
        return string.items;
    }
};

/// One of Random, LRU, FIFO.
pub const CacheReplacement = enum { Random, LRU, FIFO };

/// One of write-back, write-through.
pub const StoreBehavior = enum { @"write-back", @"write-through" };

test "default CpuConfig" {
    const config = CpuConfig.getDefaultConfiguration();
    std.debug.print("Default config name: {s}\n", .{config.name.?});
}
