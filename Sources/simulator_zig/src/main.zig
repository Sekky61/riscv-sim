const std = @import("std");
const zig_builtin = @import("builtin");

const argParse = @import("args");

const Args = @import("args.zig");
const register = @import("register.zig");
const instruction = @import("instruction.zig");

const log = std.log.scoped(.main);

pub const std_options: std.Options = .{
    // Always set this to debug to make std.log call into our handler, then control the runtime
    // value in logFn itself
    .log_level = .debug,
    .logFn = logFn,
};

var runtime_log_level: std.log.Level = if (zig_builtin.mode == .Debug) .debug else .info;
var log_file: ?std.fs.File = null;

fn logFn(
    comptime level: std.log.Level,
    comptime scope: @TypeOf(.enum_literal),
    comptime format: []const u8,
    args: anytype,
) void {
    if (@intFromEnum(level) > @intFromEnum(runtime_log_level)) return;

    const level_txt: []const u8 = switch (level) {
        .err => "error",
        .warn => "warn ",
        .info => "info ",
        .debug => "debug",
    };
    const scope_txt: []const u8 = comptime @tagName(scope);
    const trimmed_scope = if (comptime std.mem.startsWith(u8, scope_txt, "zls_")) scope_txt[4..] else scope_txt;

    var buffer: [4096]u8 = undefined;
    var fbs = std.io.fixedBufferStream(&buffer);
    const no_space_left = blk: {
        fbs.writer().print("{s} ({s:^6}): ", .{ level_txt, trimmed_scope }) catch break :blk true;
        fbs.writer().print(format, args) catch break :blk true;
        fbs.writer().writeByte('\n') catch break :blk true;
        break :blk false;
    };
    if (no_space_left) {
        buffer[buffer.len - 4 ..][0..4].* = "...\n".*;
    }

    std.debug.lockStdErr();
    defer std.debug.unlockStdErr();

    std.io.getStdErr().writeAll(fbs.getWritten()) catch {};

    if (log_file) |file| {
        file.seekFromEnd(0) catch {};
        file.writeAll(fbs.getWritten()) catch {};
    }
}

fn createLogFile(allocator: std.mem.Allocator, log_file_path: []const u8) ?std.fs.File {
    if (std.fs.path.dirname(log_file_path)) |dirname| {
        std.fs.cwd().makePath(dirname) catch {};
    }

    const file = std.fs.cwd().createFile(log_file_path, .{ .truncate = false }) catch {
        allocator.free(log_file_path);
        return null;
    };
    errdefer file.close();

    return file;
}

const stack_frames = switch (zig_builtin.mode) {
    .Debug => 10,
    else => 0,
};

pub fn main() !void {
    var allocator_state = std.heap.GeneralPurposeAllocator(.{ .stack_trace_frames = stack_frames }){};
    defer _ = allocator_state.deinit();
    const allocator: std.mem.Allocator = allocator_state.allocator();

    const parsed_env = try argParse.parseWithVerbForCurrentProcess(Args, Args.Command, std.heap.page_allocator, .print);
    defer parsed_env.deinit();
    const options = parsed_env.options;

    const log_path = options.@"log-file";
    if (log_path) |path| {
        log_file = createLogFile(allocator, path);
    }
    defer if (log_file) |file| {
        file.close();
        log_file = null;
    };

    runtime_log_level = options.@"log-level";

    try argParse.printHelp(Args, parsed_env.executable_name orelse "demo", std.io.getStdOut().writer());
    log.debug("Verb: {any}", .{parsed_env.verb});

    log.info("Log Level:        {s}", .{@tagName(runtime_log_level)});
    log.info("Log File:         {?s}", .{log_path});
}

comptime {
    _ = instruction;
    _ = register;
    _ = Args;
    // The tests need to be referenced to be run!
    std.testing.refAllDecls(@This());
}
