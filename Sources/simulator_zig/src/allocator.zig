const std = @import("std");
const is_wasm = @import("wasm/is_wasm.zig").is_wasm;

pub fn getAllocator() std.mem.Allocator {
    return if (is_wasm)
        std.heap.wasm_allocator
    else blk: {
        var alloc = std.heap.GeneralPurposeAllocator(.{}).init;
        break :blk alloc.allocator();
    };
}
