//! Just a wrapper for root_indirect.zig

const api = @import("api/cpu_state.zig");
const glue = @import("wasm/glue.zig");
const root = @import("root_indirect.zig");

// todo reduce boilerplate

pub export fn add(a: i32, b: i32) glue.wrapResultType(i32) {
    return root.add(a, b);
}

pub export fn getDefaultCpuConfig() glue.wrapResultType(api.CpuConfig) {
    return root.getDefaultCpuConfig();
}
