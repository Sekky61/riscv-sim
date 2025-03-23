const std = @import("std");

pub const SimulationStatistics = struct {
    /// Static instruction mix
    staticInstructionMix: InstructionMix = .{},
    /// Dynamic instruction mix.
    /// Instructions are counted at the moment they are committed.
    dynamicInstructionMix: InstructionMix = .{},
    /// Cache statistics
    cache: CacheStatistics = .{},
    /// Functional unit statistics.
    /// Using an AutoHashMap with key type []const u8 (unit name) and value FUStats.
    fuStats: std.AutoHashMap([]const u8, FUStats),
    /// Per instruction statistics.
    /// Indexed as the instructions appear in the code (id in InputCodeModel).
    instructionStats: std.ArrayList(InstructionStats),
    /// Counter for committed instructions.
    /// A committed instruction is one that has successfully left ROB.
    committedInstructions: i64 = 0,
    /// Counter for clocks passed.
    clockCycles: i64 = 0,
    /// Counter for how many instructions have been discarded as a result of branch misprediction.
    /// Failed instructions are those that have been flushed from the pipeline.
    flushedInstructions: i64 = 0,
    /// Counter for how many ROB flushes occurred.
    robFlushes: i64 = 0,
    /// Clock frequency (Hz).
    /// Used for calculating time based statistics.
    clock: i32 = 0,
    /// Counter for correctly predicted branching instructions.
    correctlyPredictedBranches: i64 = 0,
    /// Counter for all conditional branches.
    /// Count of unconditional branches can be calculated as (dynamicInstructionMix.branch - conditionalBranches).
    conditionalBranches: i64 = 0,
    /// Number of taken branches.
    takenBranches: i64 = 0,
    /// Amount of data transferred from main memory.
    mainMemoryLoadedBytes: i64 = 0,
    /// Amount of data transferred to main memory.
    mainMemoryStoredBytes: i64 = 0,
    /// Maximal number of allocated speculative registers.
    maxAllocatedRegisters: i32 = 0,

    /// Allocate a new SimulationStatistics instance (constructor equivalent)
    /// with the given instructionCount and clockHz.
    /// Allocator is used for building the internal ArrayList and AutoHashMap.
    pub fn init(
        allocator: *std.mem.Allocator,
        instructionCount: i32,
        clockHz: i32,
    ) !SimulationStatistics {
        return SimulationStatistics{
            .staticInstructionMix = InstructionMix{ .intArithmetic = 0, .floatArithmetic = 0, .memory = 0, .branch = 0, .other = 0 },
            .dynamicInstructionMix = InstructionMix{ .intArithmetic = 0, .floatArithmetic = 0, .memory = 0, .branch = 0, .other = 0 },
            .cache = .{}.init(),
            .fuStats = try std.AutoHashMap([]const u8, FUStats).init(allocator),
            .instructionStats = try std.ArrayList(InstructionStats).init(allocator),
            .committedInstructions = 0,
            .clockCycles = 0,
            .flushedInstructions = 0,
            .robFlushes = 0,
            .clock = clockHz,
            .correctlyPredictedBranches = 0,
            .conditionalBranches = 0,
            .takenBranches = 0,
            .mainMemoryLoadedBytes = 0,
            .mainMemoryStoredBytes = 0,
            .maxAllocatedRegisters = 0,
        };
    }

    /// Constructor equivalent that also takes a list of functional units.
    /// fUnits is an array of strings that represent the names.
    pub fn initWithFUnits(
        allocator: *std.mem.Allocator,
        instructionCount: i32,
        clockHz: i32,
        fUnits: []const []const u8,
    ) !SimulationStatistics {
        var self = try SimulationStatistics.init(allocator, instructionCount, clockHz);
        for (fUnits) |fUnit| {
            // Insert new FUStats with key fUnit.
            try self.fuStats.put(fUnit, .{}.init());
        }
        return self;
    }

    /// Allocate new per instruction statistics.
    /// Used in tests.
    pub fn allocateInstructionStats(self: *SimulationStatistics, instructionCount: i32) !void {
        try self.instructionStats.ensureTotalCapacity(instructionCount);
        self.instructionStats.clear();
        var i: i32 = 0;
        while (i < instructionCount) : (i += 1) {
            try self.instructionStats.append(.{}.init());
        }
    }

    /// Increment busy cycles of FU with given name.
    /// Asserts that fuName is not null.
    pub fn incrementBusyCycles(self: *SimulationStatistics, fuName: []const u8) !void {
        // In Zig we assume that fuName is valid.
        // If key is not found, add a new FUStats.
        if (!self.fuStats.contains(fuName)) {
            try self.fuStats.put(fuName, .{}.init());
        }
        const mutable_entry = self.fuStats.get(fuName) orelse unreachable;
        mutable_entry.incrementBusyCycles();
    }

    /// Increment main memory traffic.
    /// If isStore is true, increment mainMemoryStoredBytes, else mainMemoryLoadedBytes.
    pub fn incrementMemoryTraffic(
        self: *SimulationStatistics,
        isStore: bool,
        bytes: i32,
    ) void {
        if (isStore) {
            self.mainMemoryStoredBytes += bytes;
        } else {
            self.mainMemoryLoadedBytes += bytes;
        }
    }

    /// Increments number of taken branches.
    pub fn incrementTakenBranches(self: *SimulationStatistics) void {
        self.takenBranches += 1;
    }

    /// Increments number of correctly predicted branching instructions.
    pub fn incrementCorrectlyPredictedBranches(self: *SimulationStatistics) void {
        self.correctlyPredictedBranches += 1;
    }

    /// Increments number of conditional branch instructions that were committed.
    pub fn incrementConditionalBranches(self: *SimulationStatistics) void {
        self.conditionalBranches += 1;
    }

    /// Report number of allocated registers.
    pub fn reportAllocatedRegisters(
        self: *SimulationStatistics,
        allocatedRegisters: i32,
    ) void {
        if (allocatedRegisters > self.maxAllocatedRegisters) {
            self.maxAllocatedRegisters = allocatedRegisters;
        }
    }

    /// Increment number of ROB flushes.
    /// TODO: can a flush be caused by memory forwarding?
    pub fn incrementRobFlushes(self: *SimulationStatistics) void {
        self.robFlushes += 1;
    }

    /// Increments number of simulate() calls (clock cycles).
    pub fn incrementClockCycles(self: *SimulationStatistics) void {
        self.clockCycles += 1;
    }

    /// Increment number of failed instructions.
    pub fn incrementFailedInstructions(self: *SimulationStatistics) void {
        self.flushedInstructions += 1;
    }

    /// Get number of committed instructions.
    pub fn getCommittedInstructions(self: *SimulationStatistics) i64 {
        return self.committedInstructions;
    }

    /// Get number of committed conditional branch instructions.
    pub fn getConditionalBranches(self: *SimulationStatistics) i64 {
        return self.conditionalBranches;
    }

    /// Get number of committed unconditional branch instructions.
    /// Calculated as (dynamicInstructionMix.branch - conditionalBranches).
    pub fn getUnconditionalBranches(self: *SimulationStatistics) i64 {
        return self.dynamicInstructionMix.branch - self.conditionalBranches;
    }

    /// Get number of correctly predicted branches.
    pub fn getCorrectlyPredictedBranches(self: *SimulationStatistics) i64 {
        return self.correctlyPredictedBranches;
    }

    /// Get number of taken branches.
    pub fn getTakenBranches(self: *SimulationStatistics) i64 {
        return self.takenBranches;
    }

    /// Get the arithmetic intensity of the code.
    pub fn getArithmeticIntensity(self: *SimulationStatistics) f64 {
        if (self.committedInstructions == 0) {
            return 0;
        }
        return (@floatFromInt(self.dynamicInstructionMix.intArithmetic +
            self.dynamicInstructionMix.floatArithmetic)) / (@floatFromInt(self.committedInstructions));
    }

    /// Get prediction accuracy.
    pub fn getPredictionAccuracy(self: *SimulationStatistics) f64 {
        if (self.conditionalBranches == 0) {
            return 0;
        }
        return (@intToFloat(f64, self.correctlyPredictedBranches)) /
            (@intToFloat(f64, self.dynamicInstructionMix.branch));
    }

    /// Get FLOPS.
    pub fn getFlops(self: *SimulationStatistics) f64 {
        if (self.clock == 0) {
            return 0;
        }
        return (@intToFloat(f64, self.dynamicInstructionMix.intArithmetic +
            self.dynamicInstructionMix.floatArithmetic)) / (@intToFloat(f64, self.clock));
    }

    /// Get IPC.
    pub fn getIpc(self: *SimulationStatistics) f64 {
        if (self.clockCycles == 0) {
            return 0;
        }
        return (@intToFloat(f64, self.committedInstructions)) /
            (@intToFloat(f64, self.clockCycles));
    }

    /// Get wall time.
    /// clock is the clock frequency (Hz)
    pub fn getWallTime(self: *SimulationStatistics) f64 {
        return (@intToFloat(f64, self.clockCycles)) /
            (@intToFloat(f64, self.clock));
    }

    /// Get memory throughput (bytes/s).
    pub fn getMemoryThroughput(self: *SimulationStatistics) f64 {
        return (@intToFloat(f64, self.mainMemoryLoadedBytes + self.mainMemoryStoredBytes)) /
            (@intToFloat(f64, self.clock));
    }
};

/// A structure that represents cache statistics.
pub const CacheStatistics = struct {
    /// Counter for how many times cache has been accessed for read.
    readAccesses: i32,
    /// Counter for how many times cache has been accessed for write.
    writeAccesses: i32,
    /// Counter for the number of cache hits.
    hits: i32,
    /// Counter for the number of cache misses.
    /// Misaligned access that causes to load 2 cache lines counts as a single miss.
    misses: i32,
    /// Counter for the total delay caused by cache accesses.
    /// TODO
    totalDelay: i32,
    /// Number of bytes written to cache.
    bytesWritten: i32,
    /// Number of bytes read from cache.
    bytesRead: i32,

    /// Initialize CacheStatistics.
    pub fn init(self: CacheStatistics) CacheStatistics {
        return CacheStatistics{
            .readAccesses = 0,
            .writeAccesses = 0,
            .hits = 0,
            .misses = 0,
            .totalDelay = 0,
            .bytesWritten = 0,
            .bytesRead = 0,
        };
    }

    /// Get cache hits.
    pub fn getHits(self: *CacheStatistics) i32 {
        return self.hits;
    }

    /// Get cache misses.
    pub fn getMisses(self: *CacheStatistics) i32 {
        return self.misses;
    }

    /// Get cache hit rate.
    pub fn getHitRate(self: *CacheStatistics) f64 {
        const all = self.hits + self.misses;
        if (all == 0) {
            return 0;
        }
        return (@intToFloat(f64, self.hits)) / (@intToFloat(f64, all));
    }

    pub fn getReadAccesses(self: *CacheStatistics) i32 {
        return self.readAccesses;
    }

    pub fn getWriteAccesses(self: *CacheStatistics) i32 {
        return self.writeAccesses;
    }

    pub fn getBytesWritten(self: *CacheStatistics) i32 {
        return self.bytesWritten;
    }

    pub fn getBytesRead(self: *CacheStatistics) i32 {
        return self.bytesRead;
    }

    /// Increment read accesses and add bytesRead.
    pub fn incrementReadAccesses(self: *CacheStatistics, bytesRead: i32) void {
        self.readAccesses += 1;
        self.bytesRead += bytesRead;
    }

    /// Increment write accesses and add bytesWritten.
    pub fn incrementWriteAccesses(self: *CacheStatistics, bytesWritten: i32) void {
        self.writeAccesses += 1;
        self.bytesWritten += bytesWritten;
    }

    /// Increment cache hits.
    pub fn incrementHits(self: *CacheStatistics) void {
        self.hits += 1;
    }

    /// Increment cache misses.
    pub fn incrementMisses(self: *CacheStatistics) void {
        self.misses += 1;
    }

    /// Increment total delay by delay.
    pub fn incrementTotalDelay(self: *CacheStatistics, delay: i32) void {
        self.totalDelay += delay;
    }
};

/// InstructionMix represents the instruction mix counters.
pub const InstructionMix = struct {
    intArithmetic: i32 = 0,
    floatArithmetic: i32 = 0,
    memory: i32 = 0,
    branch: i32 = 0,
    other: i32 = 0,

    /// Increment the appropriate counter based on the type.
    pub fn increment(self: *InstructionMix, typ: InstructionTypeEnum) void {
        switch (typ) {
            .kIntArithmetic => self.intArithmetic += 1,
            .kFloatArithmetic => self.floatArithmetic += 1,
            .kLoadstore => self.memory += 1,
            .kJumpbranch => self.branch += 1,
            else => {
                self.other += 1;
            },
        }
    }
};

/// FUStats represents functional unit statistics.
pub const FUStats = struct {
    /// The number of cycles that the FU was busy.
    busyCycles: i32 = 0,

    pub fn incrementBusyCycles(self: *FUStats) void {
        self.busyCycles += 1;
    }
};

/// InstructionStats represents per instruction statistics.
pub const InstructionStats = struct {
    /// The number of cycles that instruction was committed.
    committedCount: i32 = 0,
    /// The number of times that instruction was decoded.
    decoded: i32 = 0,
    /// The number of times that the (jump) instruction was correctly predicted.
    /// Zero for all other instructions.
    /// The number of times that the (jump) instruction was incorrectly predicted can be calculated
    /// as (committedCount - correctlyPredicted).
    correctlyPredicted: i32 = 0,
    /// Cache hits of this instruction. Zero for all non-memory instructions.
    /// Cache misses of this instruction can be calculated as (memoryAccesses - cacheHits).
    /// Misaligned access that causes to load 2 cache lines counts as a single miss.
    /// Null if the instruction is not a memory instruction.
    cacheHits: ?i32,
    /// Memory access count. Null for non-memory instructions.
    memoryAccesses: ?i32,

    /// Increment number of committed cycles.
    pub fn incrementCommittedCycles(self: *InstructionStats) void {
        self.committedCount += 1;
    }

    /// Increment number of times that instruction was decoded.
    pub fn incrementDecoded(self: *InstructionStats) void {
        self.decoded += 1;
    }

    /// Increment number of times that instruction was correctly predicted.
    pub fn incrementCorrectlyPredicted(self: *InstructionStats) void {
        self.correctlyPredicted += 1;
    }

    /// Increment memory accesses and cacheHits conditionally.
    pub fn incrementMemoryAccesses(self: *InstructionStats, isHit: bool) void {
        if (self.memoryAccesses == null) {
            self.memoryAccesses = 0;
        }
        if (self.cacheHits == null) {
            self.cacheHits = 0;
        }
        self.memoryAccesses.? += 1;
        if (isHit) {
            self.cacheHits.? += 1;
        }
    }

    /// Get cache misses.
    pub fn getCacheMisses(self: *InstructionStats) ?i32 {
        if (self.memoryAccesses == null or self.cacheHits == null) {
            return null;
        }
        return self.memoryAccesses.? - self.cacheHits.*;
    }
};

/// An example enumeration for instruction types.
pub const InstructionTypeEnum = enum {
    kIntArithmetic,
    kFloatArithmetic,
    kLoadstore,
    kJumpbranch,
};
