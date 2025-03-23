const api = @import("cpu_config.zig");

pub const CheckConfigRequest = struct {
    config: api.CpuConfig = .{},
};

pub const CheckConfigResponse = struct {
    valid: bool = false,
    messages: []const []const u8 = &.{},
};
