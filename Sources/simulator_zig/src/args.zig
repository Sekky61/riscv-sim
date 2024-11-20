// This declares long options for double hyphen
@"gcc-path": ?[]const u8 = null,
@"log-level": ?[]const u8 = null,
@"log-file": ?[]const u8 = null,

// This declares short-hand options for single hyphen
pub const shorthands = .{};

pub const Command = union(enum) { server: struct {
    host: []const u8 = "0.0.0.0",
    port: []const u8 = "8000",
    @"timeout-ms": []const u8 = "5000",
}, cli: struct {
    cpu: ?[]const u8 = null,
    memory: ?[]const u8 = null,
    program: ?[]const u8 = null,
}, version, help };

pub const meta = .{
    .option_docs = .{
        .server = "Run as server",
        .@"gcc-path" = "Path to gcc executable",
        .@"log-level" = "Log level",
        .@"log-file" = "Log file",
        .host = "Host to bind to",
        .port = "Port to listen on",
        .@"timeout-ms" = "Request timeout in milliseconds",
        .cpu = "CPU description file path",
        .memory = "Memory description file path",
        .program = "Assembly program file path",
    },
    //.usage_summary = "prints after exe path"
    .full_text =
    \\RISC-V superscalar CPU simulator
    \\
    \\Commands: server, cli, version, help
};
