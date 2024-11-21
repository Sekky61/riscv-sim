//! RISC-V instruction with the same layout as spec (1.5 Base Instruction-Length Encoding)
//! Currently supports 32bit I and M extensions.

const std = @import("std");
const Allocator = std.mem.Allocator;
const assert = std.debug.assert;

const InstructionEncoding = struct {
    match: u32,
    instruction: Instruction,
};

const Table = std.MultiArrayList(InstructionEncoding);

fn DecodingTable(Storage: type) type {
    return struct {
        table: Storage,
        mask: u32,
    };
}

/// Generated encodings (scripts/gen_instructions.py)
const encodings: []const DecodingTable([]const InstructionEncoding) = &.{
    .{
        .mask = 0x7f,
        .table = &.{
            .{ .match = 0x17, .instruction = .auipc },
            .{ .match = 0x37, .instruction = .lui },
            .{ .match = 0x6f, .instruction = .jal },
        },
    },
    .{
        .mask = 0x707f,
        .table = &.{
            .{ .match = 0x3, .instruction = .lb },
            .{ .match = 0xf, .instruction = .fence },
            .{ .match = 0x13, .instruction = .addi },
            .{ .match = 0x23, .instruction = .sb },
            .{ .match = 0x63, .instruction = .beq },
            .{ .match = 0x67, .instruction = .jalr },
            .{ .match = 0x1003, .instruction = .lh },
            .{ .match = 0x1023, .instruction = .sh },
            .{ .match = 0x1063, .instruction = .bne },
            .{ .match = 0x2003, .instruction = .lw },
            .{ .match = 0x2013, .instruction = .slti },
            .{ .match = 0x2023, .instruction = .sw },
            .{ .match = 0x3013, .instruction = .sltiu },
            .{ .match = 0x4003, .instruction = .lbu },
            .{ .match = 0x4013, .instruction = .xori },
            .{ .match = 0x4063, .instruction = .blt },
            .{ .match = 0x5003, .instruction = .lhu },
            .{ .match = 0x5063, .instruction = .bge },
            .{ .match = 0x6013, .instruction = .ori },
            .{ .match = 0x6063, .instruction = .bltu },
            .{ .match = 0x7013, .instruction = .andi },
            .{ .match = 0x7063, .instruction = .bgeu },
        },
    },
    .{
        .mask = 0xfe00707f,
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
    .{
        .mask = 0xffffffff,
        .table = &.{
            .{ .match = 0x73, .instruction = .ecall },
            .{ .match = 0x100073, .instruction = .ebreak },
        },
    },
};

const Instruction = enum {
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
    ebreak,
    ecall,
    fence,
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

/// Decodes RISC-V instructions.
/// Instruction encoding requires instantiation, while some independent
/// methods do not.
/// Calling extractors like `rs1` does not check if the instruction uses the
/// bits as first source register.
pub const Decoder = struct {
    /// Efficient decoding tables
    tables: []DecodingTable(Table),

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
    fn makeDecodingTables(allocator: Allocator) ![]DecodingTable(Table) {
        // TODO: sort and binary search
        const tabs = try allocator.alloc(DecodingTable(Table), encodings.len);
        for (tabs, encodings) |*tab, enc| {
            var table = Table{};
            try table.ensureTotalCapacity(allocator, enc.table.len);
            for (enc.table) |encoding| {
                table.appendAssumeCapacity(encoding);
            }
            tab.* = DecodingTable(Table){ .table = table, .mask = enc.mask };
        }
        return tabs;
    }

    /// Takes a piece of memory (32 bits) and classifies it as an instance of
    /// an instruction
    pub fn decode(self: Self, word: u32) ?Instruction {
        for (self.tables) |table| {
            const masked = word & table.mask;
            const haystack = table.table.items(.match);
            const foundIdx = std.mem.indexOfScalar(u32, haystack, masked) orelse continue;
            const instructions = table.table.items(.instruction);
            return instructions[foundIdx];
        }
        return null;
    }

    /// Load the register destination field
    pub fn rd(word: u32) u5 {
        return extractBits(word, 7, 11);
    }

    /// Load the first source register field
    pub fn rs1(word: u32) u5 {
        return extractBits(word, 15, 19);
    }

    /// Load the second source register field
    pub fn rs2(word: u32) u5 {
        return extractBits(word, 20, 24);
    }

    /// Load the immediate field in U-Type instructions
    pub fn immU(word: u32) i20 {
        return @bitCast(extractBits(word, 12, 31));
    }

    /// Load the immediate field in I-Type instructions
    pub fn immI(word: u32) i12 {
        const imm = extractBits(word, 20, 31);
        return @bitCast(imm);
    }

    /// Load the immediate field in S-Type instructions
    pub fn immS(word: u32) i12 {
        // The @as casts are necessary: without them the bits shifted to the left disappear
        const imm = (@as(u12, extractBits(word, 25, 31)) << 5) | extractBits(word, 7, 11);
        return @bitCast(imm);
    }

    /// Load the immediate field in B-Type instructions
    /// B-Type has implicit bit 0 as 0, which _is_ added here.
    pub fn immB(word: u32) i13 {
        const imm: u13 = (@as(u13, extractBits(word, 31, 31)) << 12) | (@as(u13, extractBits(word, 7, 7)) << 11) | (@as(u13, extractBits(word, 25, 30)) << 5) | (@as(u13, extractBits(word, 8, 11)) << 1);
        return @bitCast(imm);
    }

    /// Load the immediate field in J-Type instructions
    pub fn immJ(word: u32) i21 {
        const imm = (@as(u21, extractBits(word, 20, 20)) << 20) | (@as(u21, extractBits(word, 12, 19)) << 12) | (@as(u21, extractBits(word, 20, 20)) << 11) | (@as(u21, extractBits(word, 25, 30)) << 5) | (@as(u21, extractBits(word, 21, 24)) << 1);
        return @bitCast(imm);
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

fn decodesTo(decoder: Decoder, word: u32, expected: Instruction) !void {
    const decoded = decoder.decode(word);
    try testing.expectEqual(expected, decoded);
}

test "Bit extracting" {
    //                                  Index -> 43210
    try testing.expectEqual(0b101, extractBits(0b11011, 1, 3));
    try testing.expectEqual(0b001, extractBits(1, 0, 7));
    try testing.expectEqual(0b1, extractBits(0b10000000000000000100000000110011, 31, 31));
    try testing.expectEqual(0b1110, extractBits(0b11111100000100001001_1110_11100011, 8, 11));
}

test "Utils" {
    const E = Diff(0, 0);
    try testing.expectEqual(1, @typeInfo(E).int.bits);
    const D = Diff(0, 1);
    try testing.expectEqual(2, @typeInfo(D).int.bits);
}

test "Decode instruction" {
    var decoder = try Decoder.init(testing.allocator);
    defer decoder.deinit(testing.allocator);
    //                 xor: "0000000----------100-----0110011"
    try decodesTo(decoder, 0b00000000000000000100000000110011, .xor);
    try decodesTo(decoder, 0x0000006f, .jal);
    // addi x8, x30, 127
    try decodesTo(decoder, 0b00000111111111110000010000010011, .addi);
}

test "Extract register ids" {
    var decoder = try Decoder.init(testing.allocator);
    defer decoder.deinit(testing.allocator);

    // Sections:         |-s2||-s1|   |-rd|
    const xor = 0b00000001000001001100000100110011;
    const rd = Decoder.rd(xor);
    const rs1 = Decoder.rs1(xor);
    const rs2 = Decoder.rs2(xor);
    try testing.expectEqual(0b00010, rd);
    try testing.expectEqual(0b01001, rs1);
    try testing.expectEqual(0b10000, rs2);
}

test "Extract immediates" {
    // addi x4, x5, 598
    const addi = 0b00100101011000101000001000010011;
    const immI = Decoder.immI(addi);
    try testing.expectEqual(598, immI);

    // lui x10, 64910
    const lui = 0b00001111110110001110010100110111;
    try testing.expectEqual(64910, Decoder.immU(lui));

    // jal x0, 1096
    const jal = 0b01000100100000000000000001101111;
    try testing.expectEqual(1096, Decoder.immJ(jal));

    // jalr x0, 2047(x1)
    const jalr = 0b01111111111100001000000001100111;
    try testing.expectEqual(2047, Decoder.immI(jalr));

    // jalr x0, -2047(x1)
    const jalrNegative = 0b10000000000100001000000001100111;
    try testing.expectEqual(-2047, Decoder.immI(jalrNegative));

    // beq x1, x4, 3040
    const beq = 0b00111110010000001000000011100011;
    try testing.expectEqual(3040, Decoder.immB(beq));

    // bne x1, x1, -36
    const bne = 0b11111100000100001001_1110_1110_0011;
    try testing.expectEqual(-36, Decoder.immB(bne));

    // sb x7, 980(x9)
    const sb = 0b00111100011101001000101000100011;
    try testing.expectEqual(980, Decoder.immS(sb));
}
