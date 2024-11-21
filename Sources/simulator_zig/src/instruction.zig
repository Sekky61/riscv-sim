//! RISC-V instruction with the same layout as spec (1.5 Base Instruction-Length Encoding)
//! Currently supports 32bit I and M extensions.

const std = @import("std");
const register = @import("register.zig");
const Allocator = std.mem.Allocator;
const assert = std.debug.assert;

/// Arguments can be extracted with knowledge about layout and the original word
pub const Instruction = struct {
    /// Original word
    word: Word,
    /// Cached layout
    layout: Layout,
    /// Cached opcode
    opcode: Opcode,

    const Self = @This();

    /// Get the immediate value, bit extended to 32 bits
    pub fn imm(self: Self) i32 {
        assert(self.layout != .R);
        return self.word.rest.imm(self.layout);
    }

    pub fn rd(self: Self) u5 {
        assert(self.layout != .S and self.layout != .B);
        return self.word.rest.r.rd;
    }

    pub fn rs1(self: Self) u5 {
        assert(self.layout != .U and self.layout != .J);
        return self.word.rest.r.rs1;
    }

    pub fn rs2(self: Self) u5 {
        assert(self.layout != .U and self.layout != .J and self.layout != .I);
        return self.word.rest.r.rs2;
    }
};

/// Has exactly the same layout as RISC-V instructions (u32).
pub const Word = packed struct {
    opcode: u7,
    rest: Fields,

    /// Casts to a Word
    pub fn cast_from(word: u32) @This() {
        comptime assert(@sizeOf(@This()) == @sizeOf(u32));
        return @bitCast(word);
    }

    /// Rest of the instruction needs to be interpreted. In this program
    /// a layout is computed from database of signatures.
    pub const Fields = packed union {
        const Self = @This();

        pub fn imm(self: Self, layout: Layout) i32 {
            return switch (layout) {
                .R => unreachable,
                .I => self.i.imm(),
                .S => self.s.imm(),
                .B => self.b.imm(),
                .U => self.u.imm(),
                .J => self.j.imm(),
            };
        }

        const R = packed struct {
            rd: u5,
            funct3: u3,
            rs1: u5,
            rs2: u5,
            funct7: u7,
        };

        const I = packed struct {
            rd: u5,
            funct3: u3,
            rs1: u5,
            imm0: u12,

            pub fn imm(self: @This()) i12 {
                return @bitCast(self.imm0);
            }
        };

        const S = packed struct {
            imm0: u5,
            funct3: u3,
            rs1: u5,
            rs2: u5,
            imm1: u7,

            pub fn imm(self: @This()) i12 {
                // The @as casts are necessary: without them the bits shifted to the left disappear
                const c = (@as(u12, self.imm1) << 5) | self.imm0;
                return @bitCast(c);
            }
        };

        const B = packed struct {
            imm2: u1,
            imm0: u4,
            funct3: u3,
            rs1: u5,
            rs2: u5,
            imm1: u6,
            imm3: u1,

            pub fn imm(self: @This()) i13 {
                const c: u13 = (@as(u13, self.imm3) << 12) | (@as(u13, self.imm2) << 11) | (@as(u13, self.imm1) << 5) | (@as(u13, self.imm0) << 1);
                return @bitCast(c);
            }
        };

        const U = packed struct {
            rd: u5,
            imm0: i20,

            pub fn imm(self: @This()) i20 {
                return self.imm0;
            }
        };

        const J = packed struct {
            rd: u5,
            imm2: u8,
            imm1: u1,
            imm0: u10,
            imm3: u1,

            pub fn imm(self: @This()) i21 {
                const c = (@as(u21, self.imm3) << 20) | (@as(u21, self.imm2) << 12) | (@as(u21, self.imm1) << 11) | (@as(u21, self.imm0) << 1);
                return @bitCast(c);
            }
        };

        r: R,
        i: I,
        s: S,
        b: B,
        u: U,
        j: J,
    };
};

pub const Layout = enum {
    R,
    I,
    S,
    B,
    U,
    J,
};

const InstructionPattern = struct {
    match: u32,
    instruction: Opcode,
};

const SoaPatterns = std.MultiArrayList(InstructionPattern);

fn DecodingTables(Storage: type) type {
    return struct {
        table: Storage,
        layout: Layout,
        mask: u32,
    };
}

/// Generated encodings (scripts/gen_instructions.py)
const encodings: []const DecodingTables([]const InstructionPattern) = &.{
    .{
        .mask = 0x7f,
        .layout = .U,
        .table = &.{
            .{ .match = 0x17, .instruction = .auipc },
            .{ .match = 0x37, .instruction = .lui },
        },
    },
    .{
        .mask = 0x7f,
        .layout = .J,
        .table = &.{
            .{ .match = 0x6f, .instruction = .jal },
        },
    },
    .{
        .mask = 0x707f,
        .layout = .I,
        .table = &.{
            .{ .match = 0x3, .instruction = .lb },
            .{ .match = 0x13, .instruction = .addi },
            .{ .match = 0x67, .instruction = .jalr },
            .{ .match = 0x1003, .instruction = .lh },
            .{ .match = 0x2003, .instruction = .lw },
            .{ .match = 0x2013, .instruction = .slti },
            .{ .match = 0x3013, .instruction = .sltiu },
            .{ .match = 0x4003, .instruction = .lbu },
            .{ .match = 0x4013, .instruction = .xori },
            .{ .match = 0x5003, .instruction = .lhu },
            .{ .match = 0x6013, .instruction = .ori },
            .{ .match = 0x7013, .instruction = .andi },
        },
    },
    .{
        .mask = 0x707f,
        .layout = .B,
        .table = &.{
            .{ .match = 0x63, .instruction = .beq },
            .{ .match = 0x1063, .instruction = .bne },
            .{ .match = 0x4063, .instruction = .blt },
            .{ .match = 0x5063, .instruction = .bge },
            .{ .match = 0x6063, .instruction = .bltu },
            .{ .match = 0x7063, .instruction = .bgeu },
        },
    },
    .{
        .mask = 0x707f,
        .layout = .S,
        .table = &.{
            .{ .match = 0x23, .instruction = .sb },
            .{ .match = 0x1023, .instruction = .sh },
            .{ .match = 0x2023, .instruction = .sw },
        },
    },
    .{
        .mask = 0xfe00707f,
        .layout = .R,
        .table = &.{
            .{ .match = 0x33, .instruction = .add },
            .{ .match = 0x1033, .instruction = .sll },
            .{ .match = 0x2033, .instruction = .slt },
            .{ .match = 0x3033, .instruction = .sltu },
            .{ .match = 0x4033, .instruction = .xor },
            .{ .match = 0x5033, .instruction = .srl },
            .{ .match = 0x6033, .instruction = .@"or" },
            .{ .match = 0x7033, .instruction = .@"and" },
            .{ .match = 0x2000033, .instruction = .mul },
            .{ .match = 0x2001033, .instruction = .mulh },
            .{ .match = 0x2002033, .instruction = .mulhsu },
            .{ .match = 0x2003033, .instruction = .mulhu },
            .{ .match = 0x2004033, .instruction = .div },
            .{ .match = 0x2005033, .instruction = .divu },
            .{ .match = 0x2006033, .instruction = .rem },
            .{ .match = 0x2007033, .instruction = .remu },
            .{ .match = 0x40000033, .instruction = .sub },
            .{ .match = 0x40005033, .instruction = .sra },
        },
    },
};

const Opcode = enum {
    add,
    addi,
    @"and",
    andi,
    auipc,
    beq,
    bge,
    bgeu,
    blt,
    bltu,
    bne,
    div,
    divu,
    jal,
    jalr,
    lb,
    lbu,
    lh,
    lhu,
    lui,
    lw,
    mul,
    mulh,
    mulhsu,
    mulhu,
    @"or",
    ori,
    rem,
    remu,
    sb,
    sh,
    sll,
    slt,
    slti,
    sltiu,
    sltu,
    sra,
    srl,
    sub,
    sw,
    xor,
    xori,
};

comptime {
    // Assure all instructions have an entry in encodings
    var set = std.enums.EnumSet(Opcode){};
    for (encodings) |table| {
        for (table.table) |encoding| {
            set.insert(encoding.instruction);
        }
    }
    // Check that all are found
    const fullSet = std.enums.EnumSet(Opcode).initFull();
    assert(fullSet.eql(set));
}

/// Decodes RISC-V instructions.
/// Instruction encoding requires instantiation, while some independent
/// methods do not.
/// Calling extractors like `rs1` does not check if the instruction uses the
/// bits as first source register.
pub const Decoder = struct {
    /// Efficient decoding tables
    tables: []DecodingTables(SoaPatterns),

    const Self = @This();

    /// Instantiate a decoder with the given allocator.
    /// Do not forger to call `deinit` to free the memory.
    pub fn init(allocator: Allocator) !Self {
        const tables = try makeDecodingTables(allocator);
        return Self{ .tables = tables };
    }

    pub fn deinit(self: *Self, allocator: Allocator) void {
        // * pointer needed to mutate with deinit()
        for (self.tables) |*table| {
            table.table.deinit(allocator);
        }
        allocator.free(self.tables);
    }

    /// Convert encodings to SoA
    fn makeDecodingTables(allocator: Allocator) ![]DecodingTables(SoaPatterns) {
        // TODO: sort and binary search
        const tabs = try allocator.alloc(DecodingTables(SoaPatterns), encodings.len);
        for (tabs, encodings) |*tab, enc| {
            var table = SoaPatterns{};
            try table.ensureTotalCapacity(allocator, enc.table.len);
            for (enc.table) |encoding| {
                table.appendAssumeCapacity(encoding);
            }
            tab.* = DecodingTables(SoaPatterns){ .table = table, .layout = enc.layout, .mask = enc.mask };
        }
        return tabs;
    }

    /// Takes a piece of memory (32 bits) and classifies it as an instance of
    /// an instruction
    fn decodeInner(self: Self, word: u32) ?struct { opcode: Opcode, layout: Layout } {
        for (self.tables) |table| {
            const masked = word & table.mask;
            const haystack = table.table.items(.match);
            const foundIdx = std.mem.indexOfScalar(u32, haystack, masked) orelse continue;
            const instructions = table.table.items(.instruction);
            return .{ .opcode = instructions[foundIdx], .layout = table.layout };
        }
        return null;
    }

    pub fn decode(self: Self, word: u32) ?Instruction {
        const decoded = self.decodeInner(word) orelse return null;
        return Instruction{
            .word = Word.cast_from(word),
            .layout = decoded.layout,
            .opcode = decoded.opcode,
        };
    }
};

/// Create an integer type with the number of bits as the difference of two numbers
fn Diff(comptime lo: u8, comptime hi: u8) type {
    assert(lo <= hi);
    const i = std.builtin.Type.Int{ .bits = hi - lo + 1, .signedness = .unsigned };
    const ti = std.builtin.Type{ .int = i };
    return @Type(ti);
}

/// Extract a bit range from an integer
fn extractBits(x: u32, comptime lo: u8, comptime hi: u8) Diff(lo, hi) {
    assert(lo <= hi);
    const mask: u32 = (1 << (hi - lo + 1)) - 1;
    const masked = (x >> lo) & mask;
    return @intCast(masked);
}

// === Tests ===
//
// Good source: https://luplab.gitlab.io/rvcodecjs/#q=jal&abi=false&isa=AUTO

const testing = std.testing;

fn decodesTo(decoder: Decoder, word: u32, opcode: Opcode, layout: Layout, imm: ?i32) !void {
    const decoded = decoder.decode(word).?;
    try testing.expectEqual(opcode, decoded.opcode);
    try testing.expectEqual(layout, decoded.layout);
    if (imm) |val| {
        const immediate = decoded.word.rest.imm(layout);
        try testing.expectEqual(val, immediate);
    }
}

fn hasRegisters(ins: Instruction, rd: ?register.IntArchName, rs1: ?register.IntArchName, rs2: ?register.IntArchName) !void {
    if (rd) |val| {
        try testing.expectEqual(@intFromEnum(val), ins.rd());
    }
    if (rs1) |val| {
        try testing.expectEqual(@intFromEnum(val), ins.rs1());
    }
    if (rs2) |val| {
        try testing.expectEqual(@intFromEnum(val), ins.rs2());
    }
}

test "Bit extracting" {
    //                                  Index -> 43210
    try testing.expectEqual(0b101, extractBits(0b11011, 1, 3));
    try testing.expectEqual(0b001, extractBits(1, 0, 7));
    try testing.expectEqual(0b1, extractBits(0b10000000000000000100000000110011, 31, 31));
    try testing.expectEqual(0b1110, extractBits(0b11111100000100001001_1110_11100011, 8, 11));
}

test "Diff util" {
    const E = Diff(0, 0);
    try testing.expectEqual(1, @typeInfo(E).int.bits);
    const D = Diff(0, 1);
    try testing.expectEqual(2, @typeInfo(D).int.bits);
}

test "Decode immediates" {
    var decoder = try Decoder.init(testing.allocator);
    defer decoder.deinit(testing.allocator);

    // addi x4, x5, 598
    try decodesTo(decoder, 0b00100101011000101000001000010011, .addi, .I, 598);

    // lui x10, 64910
    try decodesTo(decoder, 0b00001111110110001110010100110111, .lui, .U, 64910);

    // jal x0, 1096
    try decodesTo(decoder, 0b01000100100000000000000001101111, .jal, .J, 1096);

    // jalr x0, 2047(x1)
    try decodesTo(decoder, 0b01111111111100001000000001100111, .jalr, .I, 2047);

    // jalr x0, -2047(x1)
    try decodesTo(decoder, 0b10000000000100001000000001100111, .jalr, .I, -2047);

    // beq x1, x4, 3040
    try decodesTo(decoder, 0b00111110010000001000000011100011, .beq, .B, 3040);

    // bne x1, x1, -36
    try decodesTo(decoder, 0b11111100000100001001111011100011, .bne, .B, -36);

    // sb x7, 980(x9)
    try decodesTo(decoder, 0b00111100011101001000101000100011, .sb, .S, 980);
}

test "Instruction API" {
    var decoder = try Decoder.init(testing.allocator);
    defer decoder.deinit(testing.allocator);

    // Sections:                        |-s2||-s1|   |-rd|
    const xor = decoder.decode(0b00000001000001001100000100110011).?;
    try testing.expectEqual(0b00010, xor.rd());
    try testing.expectEqual(0b01001, xor.rs1());
    try testing.expectEqual(0b10000, xor.rs2());

    // sb x17, 0(x25)
    const sb = decoder.decode(0b00000001000111001000000000100011).?;
    try hasRegisters(sb, null, .x25, .x17);

    // ori x0, x4, 10
    const ori = decoder.decode(0b00000000101000100110000000010011).?;
    try hasRegisters(ori, .x0, .x4, null);
}
