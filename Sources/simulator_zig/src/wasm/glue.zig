const std = @import("std");
const testing = std.testing;
const builtin = @import("builtin");
const is_wasm = @import("is_wasm.zig").is_wasm;
const Allocator = std.mem.Allocator;

/// Find the pointer to serialized request at this address
var request: std.ArrayListUnmanaged(u8) = .empty;
const requestSlotAddress: [*]u8 = @ptrCast(&request.items);

/// Find the pointer to serialized response at this address
var result: std.ArrayListUnmanaged(u8) = .empty;
const resultSlotAddress: [*]u8 = @ptrCast(&result.items);

/// Use this preallocated message if allocation fails
const allocationFailedMessage: []const u8 = "{\"type\":\"error\",\"message\": \"Allocation failed\"}";

fn buildResponse(comptime T: type, data: T, gpa: Allocator) !i32 {
    result.clearAndFree(gpa);
    try result.appendSlice(gpa, "{\"type\":\"response\",\"data\":");
    try std.json.stringify(data, .{}, result.writer(gpa));
    try result.appendSlice(gpa, "}");
    return @intCast(@intFromPtr(resultSlotAddress));
}

// See how to write custom structures in [docs](https://ziglang.org/documentation/master/std/#std.json.stringify.WriteStream.write)
fn buildErrorWithMessage(errMsg: []const u8, gpa: Allocator) i32 {
    const allocationFailedMessageAddress: i32 = @intCast(@intFromPtr(&allocationFailedMessage));
    result.clearAndFree(gpa);
    result.appendSlice(gpa, "{\"type\":\"error\",\"message\":") catch return allocationFailedMessageAddress;
    std.json.stringify(errMsg, .{}, result.writer(gpa)) catch return allocationFailedMessageAddress;
    result.appendSlice(gpa, "}") catch return allocationFailedMessageAddress;
    return @intCast(@intFromPtr(resultSlotAddress));
}

/// Returns pointer that is meant to be passed to WASM host
fn slotData(comptime T: type, data: T, gpa: Allocator) i32 {
    return buildResponse(T, data, gpa) catch |err| respondError(err, gpa);
}

pub fn Serialized(comptime T: type) type {
    return if (is_wasm) i32 else T;
}

pub fn wrapResult(comptime T: type, data: T, allocator: Allocator) Serialized(T) {
    if (!is_wasm) {
        return data;
    }
    return slotData(T, data, allocator);
}

pub fn unwrapRequest(comptime T: type, data: Serialized(T), gpa: Allocator) !T {
    if (!is_wasm) {
        return data;
    }
    const parsed = try std.json.parseFromSlice(T, gpa, request.items, .{});
    return parsed.value;
}

pub fn getAllocationError() i32 {
    const allocationFailedMessageAddress: i32 = @intCast(@intFromPtr(&allocationFailedMessage));
    return allocationFailedMessageAddress;
}

pub fn respondErrorMessage(errMsg: []const u8, gpa: Allocator) i32 {
    return buildErrorWithMessage(errMsg, gpa);
}

pub fn respondError(err: anyerror, gpa: Allocator) i32 {
    return buildErrorWithMessage(@errorName(err), gpa);
}

/// Client asks for `length` bytes of memory for the serialized request
pub fn allocRequestSpace(length: usize, gpa: Allocator) Serialized(i32) {
    request.resize(gpa, length) catch |err|
        return respondError(err, gpa);
    return wrapResult(i32, @intCast(@intFromPtr(requestSlotAddress)), gpa);
}

test "wasm glue" {
    const T = Serialized(bool);
    if (is_wasm) {
        // TODO does not run tests as wasm
        try testing.expect(T == i32);
    } else {
        try testing.expect(T == bool);
    }
}
