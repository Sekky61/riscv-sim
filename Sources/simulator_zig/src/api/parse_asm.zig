const std = @import("std");
const MemoryLocation = @import("memory_location.zig").MemoryLocation;

pub const ParseAsmRequest = struct { code: []const u8 = "", memoryLocations: []MemoryLocation };

pub const ParseAsmResponse = struct {
    success: bool = false,
    errors: std.ArrayListUnmanaged(SimpleParseError) = .empty,
};

pub const SimpleParseError = struct {
    /// 'error' | 'warning'
    kind: []const u8 = "",
    message: []const u8 = "",
    line: i32 = 0,
    columnStart: i32 = 0,
    columnEnd: i32 = 0,
};
