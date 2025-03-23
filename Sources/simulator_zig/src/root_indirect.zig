//! The actual implementation of library api
//! The important difference is that the functions here
//! are not exported, which means it can be built for tests

const std = @import("std");
const builtin = @import("builtin");
const api = @import("api.zig");
const allocator = @import("allocator.zig");
const glue = @import("wasm/glue.zig");
const Serialized = glue.Serialized;
const testing = std.testing;

pub fn add(a: i32, b: i32) Serialized(i32) {
    return glue.wrapResult(i32, a + b, allocator.getAllocator());
}

/// returns
/// - Success: pointer to string, JSON format, type CpuConfig
/// - error: -1
pub fn getDefaultCpuConfig() Serialized(api.CpuConfig) {
    const defaultConfig = api.CpuConfig.getDefaultConfiguration();
    return glue.wrapResult(api.CpuConfig, defaultConfig, allocator.getAllocator());
}

pub fn parseAsm(request: Serialized(api.parse_asm.ParseAsmRequest)) Serialized(api.parse_asm.ParseAsmResponse) {
    const gpa = allocator.getAllocator();
    _ = request;
    // const req = glue.unwrapRequest(parse_asm.ParseAsmRequest, request, gpa) catch |err| return glue.respondError(err, gpa);
    // var errors = std.ArrayListUnmanaged(parse_asm.SimpleParseError).initCapacity(gpa, 1) catch |err| return glue.respondError(err, gpa);
    // errors.appendAssumeCapacity(.{ .line = 69, .message = req.code });

    const response: api.parse_asm.ParseAsmResponse = .{ .success = true };
    return glue.wrapResult(api.parse_asm.ParseAsmResponse, response, gpa);
}

pub fn checkConfig(request: Serialized(api.check_config.CheckConfigRequest)) Serialized(api.check_config.CheckConfigResponse) {
    _ = request;
    const gpa = allocator.getAllocator();
    return glue.wrapResult(api.check_config.CheckConfigResponse, .{}, gpa);
}

pub fn simulate(request: Serialized(api.simulate.SimulateRequest)) Serialized(api.check_config.CheckConfigResponse) {
    _ = request;
    const gpa = allocator.getAllocator();
    return glue.wrapResult(api.simulate.SimulateResponse, .{}, gpa);
}

test "basic add functionality" {
    try testing.expect(add(3, 7) == 10);
}
