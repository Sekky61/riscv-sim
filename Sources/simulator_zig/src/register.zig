const std = @import("std");
const Allocator = std.mem.Allocator;

/// The physical CPU register. It stores current value and simulation/inspection flags
pub fn PhyRegister(comptime Storage: type) type {
    return struct {
        /// The register value
        data: Storage,
        /// Register value cannot be changed iff true
        constant: bool = false,

        const Self = @This();

        pub fn set_data(self: *Self, data: Storage) void {
            if (self.constant) {
                return;
            }
            self.data = data;
        }
    };
}

pub fn stringToEnum(comptime T: type, str: []const u8) ?T {
    inline for (@typeInfo(T).@"enum".fields) |enumField| {
        if (std.mem.eql(u8, str, enumField.name)) {
            return @field(T, enumField.name);
        }
    }
    return null;
}

/// Index of a register in a register field.
/// Understands aliases (sp). Tags the register type.
pub const RegId = packed struct {
    tag: Tag,
    val: packed union { intArchName: IntArchName, floatArchName: FloatArchName, intAliasName: IntAliasName, floatAliasName: FloatAliasName },

    const Tag = enum(u1) {
        integer,
        float,
    };

    pub fn idx(self: RegId) u8 {
        const res: u5 = @bitCast(self.val);
        return res;
    }

    pub fn from_int_arch_name(reg: IntArchName) RegId {
        return RegId{ .tag = .integer, .val = .{ .intArchName = reg } };
    }

    pub fn from_float_arch_name(reg: FloatArchName) RegId {
        return RegId{ .tag = .float, .val = .{ .floatArchName = reg } };
    }

    pub fn from_int_alias_name(reg: IntAliasName) RegId {
        return RegId{ .tag = .integer, .val = .{ .intAliasName = reg } };
    }

    pub fn from_float_alias_name(reg: FloatAliasName) RegId {
        return RegId{ .tag = .float, .val = .{ .floatAliasName = reg } };
    }

    /// Parse a register identifier from a string
    pub fn from_string(name: []const u8) ?RegId {
        if (stringToEnum(IntArchName, name)) |val| {
            return RegId.from_int_arch_name(val);
        }
        if (stringToEnum(FloatArchName, name)) |val| {
            return RegId.from_float_arch_name(val);
        }
        if (stringToEnum(IntAliasName, name)) |val| {
            return RegId.from_int_alias_name(val);
        }
        if (stringToEnum(FloatAliasName, name)) |val| {
            return RegId.from_float_alias_name(val);
        }
        if (std.mem.eql(u8, name, "fp")) {
            return RegId.from_int_alias_name(IntAliasName.fp);
        }

        return null;
    }
};

test "RegId layout" {
    const assert = @import("std").debug.assert;
    const T = @typeInfo(RegId);
    const Backing = T.@"struct".backing_integer.?;
    comptime assert(@typeInfo(Backing).int.bits <= 8);

    // TODO: Test that enums have no overlap (paranoid)
}

pub const IntArchName = enum(u5) {
    x0,
    x1,
    x2,
    x3,
    x4,
    x5,
    x6,
    x7,
    x8,
    x9,
    x10,
    x11,
    x12,
    x13,
    x14,
    x15,
    x16,
    x17,
    x18,
    x19,
    x20,
    x21,
    x22,
    x23,
    x24,
    x25,
    x26,
    x27,
    x28,
    x29,
    x30,
    x31,
};

pub const FloatArchName = enum(u5) {
    f0,
    f1,
    f2,
    f3,
    f4,
    f5,
    f6,
    f7,
    f8,
    f9,
    f10,
    f11,
    f12,
    f13,
    f14,
    f15,
    f16,
    f17,
    f18,
    f19,
    f20,
    f21,
    f22,
    f23,
    f24,
    f25,
    f26,
    f27,
    f28,
    f29,
    f30,
    f31,
};

pub const IntAliasName = enum(u5) {
    // Aliases, start at 0
    zero = 0,
    ra,
    sp,
    gp,
    tp,
    t0,
    t1,
    t2,
    s0,
    s1,
    a0,
    a1,
    a2,
    a3,
    a4,
    a5,
    a6,
    a7,
    s2,
    s3,
    s4,
    s5,
    s6,
    s7,
    s8,
    s9,
    s10,
    s11,
    t3,
    t4,
    t5,
    t6,

    // Unfortunate naming, x8 has two aliases
    pub const fp = .s0;
};

pub const FloatAliasName = enum(u5) {
    // Floating point
    ft0,
    ft1,
    ft2,
    ft3,
    ft4,
    ft5,
    ft6,
    ft7,
    fs0,
    fs1,
    fa0,
    fa1,
    fa2,
    fa3,
    fa4,
    fa5,
    fa6,
    fa7,
    fs2,
    fs3,
    fs4,
    fs5,
    fs6,
    fs7,
    fs8,
    fs9,
    fs10,
    fs11,
    ft8,
    ft9,
    ft10,
    ft11,
};

const testing = std.testing;

fn checkId(id: RegId, idx: u8, tag: RegId.Tag) !void {
    try testing.expectEqual(idx, id.idx());
    try testing.expectEqual(tag, id.tag);
}

fn isIdx(name: []const u8, idx: u8) !void {
    const id = RegId.from_string(name) orelse unreachable;
    try testing.expectEqual(idx, id.idx());
}

test "Constant register" {
    var reg = PhyRegister(u8){ .data = 42, .constant = true };
    (&reg).set_data(43);

    try std.testing.expectEqual(reg.data, 42);
}

test "Register id" {
    const id = RegId.from_int_arch_name(.x4);
    try checkId(id, 4, RegId.Tag.integer);

    const alias = RegId.from_int_alias_name(.s6);
    try checkId(alias, 22, RegId.Tag.integer);

    const fl = RegId.from_float_arch_name(.f31);
    try checkId(fl, 31, RegId.Tag.float);
}

test "String to register id" {
    try isIdx("x9", 9);
    try isIdx("s6", 22);
    try isIdx("f31", 31);
    try isIdx("fa7", 17);
    try isIdx("fp", 8);
}
