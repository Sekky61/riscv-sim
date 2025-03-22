const std = @import("std");
const testing = std.testing;
const builtin = @import("builtin");
const is_wasm = @import("is_wasm.zig").is_wasm;
const Allocator = std.mem.Allocator;

var result: std.ArrayListUnmanaged(u8) = .empty;

/// Find the pointer to serialized response at this address
const resultSlotAddress: [*]u8 = &result.items;

fn buildResponse(comptime T: type, data: T, gpa: Allocator) !void {
    result.clearAndFree(gpa);
    try result.appendSlice(gpa, "{\"type\":\"response\",\"data\":");
    try std.json.stringify(data, .{}, result.writer(gpa));
    try result.appendSlice(gpa, "}");
}

fn buildError(errMsg: []const u8, gpa: Allocator) !void {
    result.clearAndFree(gpa);
    try result.appendSlice(gpa, "{\"type\":\"error\",\"message\":");
    try std.json.stringify(errMsg, .{}, result.writer(gpa));
    try result.appendSlice(gpa, "}");
}

/// Returns pointer that is meant to be passed to WASM host
fn slotData(comptime T: type, data: T, allocator: Allocator) [*]u8 {
    buildResponse(T, data, allocator) catch {
        buildError("Failed to serialize response", allocator) catch @panic("OOM");
    };
    return @ptrCast(&result.items);
}

pub fn wrapResultType(comptime T: type) type {
    return if (is_wasm) i32 else T;
}

pub fn wrapResult(comptime T: type, data: T, allocator: Allocator) wrapResultType(T) {
    if (is_wasm) {
        return @intCast(@intFromPtr(slotData(T, data, allocator)));
    } else {
        return data;
    }
}

test "wasm glue" {
    const T = wrapResultType(bool);
    if (is_wasm) {
        // TODO does not run tests as wasm
        try testing.expect(T == i32);
    } else {
        try testing.expect(T == bool);
    }
}
