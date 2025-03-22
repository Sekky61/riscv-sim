//! The actual implementation of library api
//! The important difference is that the functions here
//! are not exported, which means it can be built for tests

const std = @import("std");
const builtin = @import("builtin");
const api = @import("api/cpu_state.zig");
const allocator = @import("allocator.zig");
const glue = @import("wasm/glue.zig");
const testing = std.testing;

pub fn add(a: i32, b: i32) glue.wrapResultType(i32) {
    return glue.wrapResult(i32, a + b, allocator.getAllocator());
}

/// returns
/// - Success: pointer to string, JSON format, type CpuConfig
/// - error: -1
pub fn getDefaultCpuConfig() glue.wrapResultType(api.CpuConfig) {
    const defaultConfig = api.CpuConfig.getDefaultConfiguration();
    return glue.wrapResult(api.CpuConfig, defaultConfig, allocator.getAllocator());
}

test "basic add functionality" {
    try testing.expect(add(3, 7) == 10);
}
