const std = @import("std");
const ins = @import("../instruction.zig");

pub const SimulatorState = struct {
    // Global simulation state
    cycle_count: u64 = 0,
    running: bool = false,

    // Program and memory
    program: std.ArrayList(ins.Instruction),
    // memory: Memory,

    // Simulation components
    units: std.StringHashMap(*SimUnit),
    buses: std.StringHashMap(*Bus),

    // Configuration
    config: SimulatorConfig,

    // Statistics and monitoring
    stats: Statistics,

    // Methods
    pub fn init(config_json: []const u8) !SimulatorState {
        // Parse JSON config and construct simulator
        var config = try std.json.parse(SimulatorConfig, config_json);

        // Initialize state based on config
        var state = SimulatorState{
            .program = try Program.init(config.program_path),
            .memory = try Memory.init(config.memory_config),
            .units = std.StringHashMap(*SimUnit).init(allocator),
            .buses = std.StringHashMap(*Bus).init(allocator),
            .config = config,
            .stats = Statistics.init(),
        };

        // Create and connect units based on config
        try state.setupUnitsFromConfig();
        try state.connectBusesFromConfig();

        return state;
    }

    pub fn tick(self: *SimulatorState) !void {
        self.cycle_count += 1;

        // Run each phase in order
        for (self.config.phases) |phase| {
            var unit_iter = self.units.valueIterator();
            while (unit_iter.next()) |unit| {
                try unit.*.simulate(phase);
            }
        }

        // Update statistics
        self.stats.updateAfterTick(self);
    }

    pub fn serialize(self: *const SimulatorState) ![]const u8 {
        // Serialize entire state to JSON or binary format
        var serialized = std.ArrayList(u8).init(allocator);

        // Serialize units
        var unit_iter = self.units.iterator();
        while (unit_iter.next()) |entry| {
            const unit_state = entry.value_ptr.*.serialize();
            try serialized.appendSlice(unit_state);
        }

        // Serialize other state components
        // ...

        return serialized.toOwnedSlice();
    }

    pub fn deserialize(serialized: []const u8) !SimulatorState {
        // Reconstruct state from serialized data
        // ...
    }

    fn setupUnitsFromConfig(self: *SimulatorState) !void {
        // Create units based on config
        for (self.config.units) |unit_config| {
            const unit = try createUnit(unit_config);
            try self.units.put(unit.id, unit);
        }
    }

    fn connectBusesFromConfig(self: *SimulatorState) !void {
        // Create and connect buses based on config
        for (self.config.connections) |connection| {
            const bus = try createBus(connection.bus_config);
            try self.buses.put(bus.name, bus);

            // Connect units to bus
            for (connection.sources) |source| {
                const unit = self.units.get(source.unit_id) orelse
                    return error.UnitNotFound;
                try connectUnitToBus(unit, source.port, bus, .source);
            }

            for (connection.destinations) |dest| {
                const unit = self.units.get(dest.unit_id) orelse
                    return error.UnitNotFound;
                try connectUnitToBus(unit, dest.port, bus, .destination);
            }
        }
    }
};

// Configuration structure that maps to JSON
pub const SimulatorConfig = struct {
    program_path: []const u8,
    memory_config: MemoryConfig,
    units: []UnitConfig,
    connections: []ConnectionConfig,
    phases: []SimulationPhase,
};

pub const UnitConfig = struct {
    id: []const u8,
    type: []const u8, // e.g., "fetch", "decode", "alu"
    params: std.json.Value, // Unit-specific parameters
};

pub const ConnectionConfig = struct {
    bus_config: BusConfig,
    sources: []PortReference,
    destinations: []PortReference,
};

pub const PortReference = struct {
    unit_id: []const u8,
    port: []const u8,
};

pub const BusConfig = struct {
    name: []const u8,
    width: u8,
    data_type: []const u8, // String representation of type
};
