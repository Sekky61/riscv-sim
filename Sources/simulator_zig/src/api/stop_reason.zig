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
};
