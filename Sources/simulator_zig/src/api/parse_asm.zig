const std = @import("std");
const MemoryLocation = @import("memory_location.zig").MemoryLocation;

pub const ParseAsmRequest = struct {
    /// The ASM code to parse
    code: []const u8 = "",
    /// The defined memory locations.
    /// This is not required, but if it is present, it will be used to inform the parser about the defined memory locations.
    memoryLocations: ?[]MemoryLocation,
};

pub const ParseAsmResponse = struct {
    /// True if the compilation was successful
    success: bool = false,
    /// Error messages from the compiler
    errors: std.ArrayListUnmanaged(SimpleParseError) = .empty,
};

pub const SimpleParseError = struct {
    /// 'error' | 'warning'
    kind: []const u8 = "",
    /// A verbose description of the error.
    /// Double quotes are escaped, otherwise it would break the JSON. So single quotes are preferred.
    message: []const u8 = "",
    /// The 1-based row index of the error
    line: i32 = 0,
    /// The 1-based column index of the start of the error
    columnStart: i32 = 0,
    /// The 1-based column index of the end of the error
    columnEnd: i32 = 0,
};
