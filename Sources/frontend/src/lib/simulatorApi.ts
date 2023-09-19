// Send a request to the server to compile the code
export type CompilerInput = {
  c_code: string;
  optimize: boolean;
};

export type CompilerOutput = {
  // Textual representation of the compiled code
  asm: string;
  // A mapping from C code to assembly code
  blocks: Block[];
};

export type Block = {
  // Line in the C code
  c_line: number;
  // Lines in the assembly code
  asm_lines: number[];
};

// State of the simulator
export type SimulatorState = {
  // PC - The current line of code being executed
  pc: number;
  // The current value of the registers
  registers: number[];
  // The current value of the memory
  memory: number[];
  // The current value of the flags
  flags: boolean[];
};

export type SimulationStartInput = {
  // Code to be simulated
  asm: string;
};
