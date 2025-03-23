//! Just a wrapper for root_indirect.zig

const api = @import("api.zig");
const glue = @import("wasm/glue.zig");
const allocator = @import("allocator.zig");
const Serialized = glue.Serialized;
const root = @import("root_indirect.zig");

// todo reduce boilerplate

pub export fn add(a: i32, b: i32) Serialized(i32) {
    return root.add(a, b);
}

/// Allow client to allocate space for serialized request.
/// Returns pointer to allocated slice (ptr, len).
/// Calling `allocRequestSpace` again invalids any previous allocation.
pub export fn allocRequestSpace(length: i32) Serialized(i32) {
    return glue.allocRequestSpace(@intCast(length), allocator.getAllocator());
}

// TODO: include only in debug build
pub export fn returnError(allocationError: bool) Serialized(i32) {
    if (allocationError) {
        return glue.getAllocationError();
    }
    return glue.respondErrorMessage("Test error", allocator.getAllocator());
}

pub export fn parseAsm(request: Serialized(api.parse_asm.ParseAsmRequest)) Serialized(api.parse_asm.ParseAsmResponse) {
    return root.parseAsm(request);
}

pub export fn checkConfig(request: Serialized(api.check_config.CheckConfigRequest)) Serialized(api.check_config.CheckConfigResponse) {
    return root.checkConfig(request);
}

pub export fn simulate(request: Serialized(api.simulate.SimulateRequest)) Serialized(api.simulate.SimulateResponse) {
    return root.simulate(request);
}

pub export fn getDefaultCpuConfig() Serialized(api.CpuConfig) {
    return root.getDefaultCpuConfig();
}
