const std = @import("std");

/// Bus with multiple listeners
pub fn Bus(comptime Payload: type) type {
    return struct {
        name: []const u8,
        payload: Payload,

        // Multiple listeners can connect to a bus
        listeners: std.ArrayList(*Port),

        // Methods for item transmission
        write: fn (self: *Bus, payload: Payload) void,

        // Optional arbitration for multiple writers
        arbitration_policy: ArbitrationPolicy = .priority,
    };
}

pub const ArbitrationPolicy = enum {
    /// Fixed priority order
    priority,
    /// Fair scheduling
    round_robin,
    /// Oldest request first
    age_based,
};

/// Port can connect to a bus
pub fn Port(comptime Payload: type) type {
    return struct {
        const Self = @This();
        name: []const u8,
        data_type: type,
        connected_bus: ?*Bus = null,

        // Methods to read/write through the port
        read: fn (self: *const Self) ?Payload,
        /// Returns success
        write: fn (self: *Self, payload: Payload) bool,
    };
}
