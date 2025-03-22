const builtin = @import("builtin");

pub const is_wasm: bool = builtin.target.cpu.arch.isWasm();
