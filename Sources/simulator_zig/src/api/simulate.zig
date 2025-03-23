const SimulationConfig = @import("simulation_config.zig").SimulationConfig;
const CpuState = @import("cpu_state.zig").CpuState;

/// Parameters for the `simulate` request
pub const SimulateRequest = struct {
    /// The requested tick to get the state of.
    /// Tick 0 is the initial state of the simulation.
    /// If not specified, the state of the last tick is returned (the end of the simulation).
    tick: ?i32 = null,
    /// The configuration to use for the simulation
    /// Used for getting the initial state in case of a backwards simulation
    config: SimulationConfig = .{},
};

pub const SimulateResponse = struct {
    /// Delta of the executed steps
    executedSteps: i32 = 0,
    /// State of the CPU at the requested tick, or at the end of the simulation, whichever comes first
    state: CpuState = .{},
    stopReason: StopReason = .kNotStopped,
};

/// Reason for stopping the simulation. Either not stopped yet, or the simulation ended.
pub const StopReason = enum {
    /// Simulation is running.
    kNotStopped,
    /// Simulation stopped because of an exception in the code (not a Java exception).
    kException,
    /// Simulation stopped because the PC ran past the end of the code.
    kEndOfCode,
    /// Simulation stopped because the entry function returned.
    kCallStackHalt,
    /// Simulation stopped because the maximum number of cycles was reached (Protection against infinite loops).
    kMaxCycles,
    /// Simulation stopped because the maximum time was reached (Protection against infinite loops).
    kTimeOut,
    /// Simulation did not even start, because of a bad configuration.
    kBadConfig,

    pub fn simulationEnded(stopReason: @This()) bool {
        return stopReason != .kNotStopped;
    }
};
