const std = @import("std");
const DataType = @import("data_type.zig").DataType;

/// Represents one named memory location (constant, array, struct).
/// This schema disallows some of the accepted forms, but it is made so
/// that the web interface is simpler.
/// For more info, check out the API documentation.
pub const MemoryLocation = struct {
    /// names of the memory locations. the first name is the primary one.
    /// derived from the label in the assembly code.
    /// when multiple labels point to the same memory location, they are all stored here.
    /// this is a list because gcc sometimes generates such code, but it is not allowed to use in manually written code.
    name: []const u8 = "",
    /// alignment of the memory location in bytes.
    /// warning: this aligns with the .align directive in the assembly code, which is in log2.
    /// so alignment value 3 means 2^3 = 8 bytes. alignment of 0 means no alignment (2^0 = 1 byte).
    alignment: i32 = 0,
    /// TODO structs
    /// either a datatype for the whole memory location or a list of data types for each data chunk.
    /// must be sorted in ascending order of the start offset.
    /// example: [{0, int}] means that the whole memory location is an int.
    /// example: [{0, int}, {7, byte}] means that the first 7 items (data[0-6], not bytes) are int, the rest are bytes.
    dataType: DataType = DataType.kByte,
    /// The discriminated union for the data field.
    data: DataUnion = .{},
};

/// Discriminated union representing the data field.
/// The union is discriminated by the "kind" field in the JSON:
/// - If kind is "data", then the "data" variant applies.
/// - If kind is "constant", then the "constant" variant applies.
/// - If kind is "random", then the "random" variant applies.
/// TODO, custom parsing and enum
pub const DataUnion = struct {
    kind: []const u8 = "",
    /// For kind: "data"
    data: ?[]const []const u8 = null,
    /// For kind: "constant": string value for the constant.
    constant: ?[]const u8 = null,
    /// For kind: "constant" and "random": string value for the constant: must be at least 1.
    size: ?i32 = null,
    /// For kind: "random"
    /// minimum value.
    min: ?f64 = null,
    /// maximum value.
    max: ?f64 = null,
};
