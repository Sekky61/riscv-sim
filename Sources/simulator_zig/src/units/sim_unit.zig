pub fn SimUnit(comptime config_type: type) type {
    return struct {
        const Self = @This();

        id: []const u8,
        config: config_type,

        // Single function that handles all phases
        simulate: fn (self: *Self, phase: SimulationPhase) void,

        // Lifecycle and serialization methods
        init: fn (self: *Self) void,
        deinit: fn (self: *Self) void,
        serialize: fn (self: *Self) SerializedState,
        deserialize: fn (self: *Self, state: SerializedState) void,
    };
}
