const std = @import("std");

pub const SimulationStatistics = struct {
    /// Static instruction mix
    staticInstructionMix: InstructionMix = .{},
    /// Dynamic instruction mix.
    /// Instructions are counted at the moment they are committed.
    dynamicInstructionMix: InstructionMix = .{},
    /// Cache statistics
    cache: CacheStatistics = .{},
    /// Functional unit statistics. Index it using id of functional unit
    fuStats: std.ArrayListUnmanaged(FUStats) = .empty,
    /// Per instruction statistics.
    /// Indexed as the instructions appear in the code (id in InputCodeModel).
    instructionStats: std.ArrayListUnmanaged(InstructionStats) = .empty,
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

    /// Increment busy cycles of FU with given id.
    pub fn incrementBusyCycles(self: *SimulationStatistics, fuId: i32) !void {
        if (fuId >= self.fuStats.items.len) {
            return error.UnknownIdError;
        }
        self.fuStats.items[fuId].incrementBusyCycles();
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
};

/// A structure that represents cache statistics.
pub const CacheStatistics = struct {
    /// Counter for how many times cache has been accessed for read.
    readAccesses: i32 = 0,
    /// Counter for how many times cache has been accessed for write.
    writeAccesses: i32 = 0,
    /// Counter for the number of cache hits.
    hits: i32 = 0,
    /// Counter for the number of cache misses.
    /// Misaligned access that causes to load 2 cache lines counts as a single miss.
    misses: i32 = 0,
    /// Counter for the total delay caused by cache accesses.
    /// TODO
    totalDelay: i32 = 0,
    /// Number of bytes written to cache.
    bytesWritten: i32 = 0,
    /// Number of bytes read from cache.
    bytesRead: i32 = 0,

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
    fuId: i32 = 0,
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
