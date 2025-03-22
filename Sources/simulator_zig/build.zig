const std = @import("std");

// Declarative build graph
pub fn build(b: *std.Build) void {
    // Options passed from cli `zig build`
    const target = b.standardTargetOptions(.{});
    // Debug, ReleaseSafe, ReleaseFast, and ReleaseSmall
    const optimize = b.standardOptimizeOption(.{});

    // dependencies
    const args_dependency = b.dependency("args", .{ .target = target, .optimize = optimize }).module("args");

    // lib module
    const lib_root_module = b.addModule("lib_root_module", .{
        .root_source_file = b.path("src/root.zig"),
        .target = target,
        .optimize = optimize,
    });

    // exe module
    const exe_root_module = b.addModule("exe_root_module", .{
        .root_source_file = b.path("src/main.zig"),
        .target = target,
        .optimize = optimize,
    });
    exe_root_module.addImport("args", args_dependency);

    // === LIB ===

    const lib = b.addStaticLibrary(.{
        .name = "riscvsim",
        .root_module = lib_root_module,
    });
    // Install step ~ Copying the files
    b.installArtifact(lib);

    // Creates a step for unit testing. This only builds the test executable
    // but does not run it.
    const lib_unit_tests = b.addTest(.{
        .name = "lib test",
        .root_module = lib_root_module,
        .test_runner = .{ .mode = .simple, .path = b.path("src/test_runner.zig") },
    });
    const run_lib_unit_tests = b.addRunArtifact(lib_unit_tests);

    // === EXE ===

    const exe = b.addExecutable(.{
        .name = "riscvsim",
        .root_module = exe_root_module,
    });
    b.installArtifact(exe);
    const run_cmd = b.addRunArtifact(exe);

    const exe_unit_tests = b.addTest(.{
        .name = "src test",
        .root_module = exe_root_module,
        .test_runner = .{ .mode = .simple, .path = b.path("src/test_runner.zig") },
    });
    const run_exe_unit_tests = b.addRunArtifact(exe_unit_tests);

    // === WASM LIB ===
    // Create a separate target for WebAssembly
    const wasm_target = b.resolveTargetQuery(.{
        .cpu_arch = .wasm32,
        .os_tag = .freestanding,
    });

    // Create a module for the WASM library
    const wasm_lib_root_module = b.addModule("wasm_lib_root_module", .{
        .root_source_file = b.path("src/root.zig"),
        .target = wasm_target,
        .optimize = optimize,
    });

    // Build the WASM library
    const wasm_lib = b.addExecutable(.{
        .name = "riscvsim-wasm",
        .root_module = wasm_lib_root_module,
    });

    const wasm_lib_unit_tests = b.addTest(.{
        .name = "wasm lib test",
        .root_module = wasm_lib_root_module,
        .test_runner = .{ .mode = .simple, .path = b.path("src/test_runner.zig") },
    });
    const run_wasm_lib_unit_tests = b.addRunArtifact(wasm_lib_unit_tests);

    // WASM-specific linking options
    wasm_lib.rdynamic = true;
    wasm_lib.entry = .disabled;
    b.installArtifact(wasm_lib);
    //
    // Create a custom step to rename and move the WASM file to a more convenient location
    const wasm_install_step = b.addInstallBinFile(
        wasm_lib.getEmittedBin(),
        "riscvsim.wasm",
    );

    // === STEPS ===

    // Run from the installation dir instead of cache
    run_cmd.step.dependOn(b.getInstallStep());

    // This allows the user to pass arguments to the application in the build
    // command itself, like this: `zig build run -- arg1 arg2 etc`
    if (b.args) |args| {
        run_cmd.addArgs(args);
    }

    //`zig build run` step (visible in the `zig build --help` menu)
    const run_step = b.step("run", "Run the app");
    run_step.dependOn(&run_cmd.step);

    // run the unit tests with `zig build test`
    const test_step = b.step("test", "Run unit tests");
    test_step.dependOn(&run_lib_unit_tests.step);
    test_step.dependOn(&run_wasm_lib_unit_tests.step);
    test_step.dependOn(&run_exe_unit_tests.step);

    // Add a step to build the WASM library specifically
    const wasm_step = b.step("wasm", "Build the library as WebAssembly");
    wasm_step.dependOn(&wasm_install_step.step);
}
