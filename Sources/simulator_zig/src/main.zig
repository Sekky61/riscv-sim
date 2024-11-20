const std = @import("std");
const allocator = std.heap.page_allocator;

const argParse = @import("args");

const Args = @import("args.zig");

pub fn main() !void {
    const options = try argParse.parseWithVerbForCurrentProcess(Args, Args.Command, allocator, .print);
    defer options.deinit();

    try argParse.printHelp(Args, options.executable_name orelse "demo", std.io.getStdOut().writer());
    std.log.debug("Verb: {any}", .{options.verb});
}
