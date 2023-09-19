// Mirroring the Java simulation types

// Enumeration definition of different types of instruction
export type InstructionType =
  | 'kArithmetic'
  | 'kLoadStore'
  | 'kJumpBranch'
  | 'kLabel';

// "kSpeculative" omitted
export type DataType = 'kInt' | 'kLong' | 'kFloat' | 'kDouble';

export type InstructionDescription = {
  name: string;
  /**
   * The type of instruction, or a label
   */
  instructionType: InstructionType;
  /**
   * The data type of the instruction arguments (int, long, float, double)
   */
  inputDataType: DataType;
  /**
   * The data type of the instruction output
   */
  outputDataType: DataType;
  /**
   * The syntax of the instruction
   * Example: "add rd rs1 rs2",
   */
  instructionSyntax: string;
  /**
   * The instruction's semantics
   * Example: "rd=rs1+rs2;"
   */
  interpretableAs: string;
};

/**
 * Dictionary of all supported instructions
 */
export type SupportedInstructions = {
  [key: string]: InstructionDescription;
};
