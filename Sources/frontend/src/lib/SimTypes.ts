/**
 * Types for the simulation
 */

/**
 * @class SimCodeModel
 * @brief Instruction execution data (renaming)
 * Ids are zero if not yet processed
 */
export interface SimCodeModel {
  '@type': 'com.gradle.superscalarsim.models.SimCodeModel';
  /**
   * Reference to original code model
   */
  inputCodeModel: InputCodeModel;
  /**
   * Id of order of instructions processed by the decoder
   */
  id: number;
  /**
   * String representation of the renamed code line
   */
  renamedCodeLine: string;
  /**
   * Number marking bulk of instructions, which was fetched together
   */
  instructionBulkNumber: number;
  /**
   * Id, when was instructions accepted by the issue window
   */
  issueWindowId: number;
  /**
   * Id of the function block, which processed this instruction
   */
  functionUnitId: number;
  /**
   * Id marking when was result ready
   */
  readyId: number;
  /**
   * Id marking when was instruction committed from ROB
   */
  commitId: number;
  /**
   * Bit value marking failure due to wrong branch prediction
   */
  hasFailed: boolean;
}

/**
 * Represents a processed line of code
 */
export interface InputCodeModel {
  '@type': 'com.gradle.superscalarsim.models.InputCodeModel';
  /**
   * ID - the index of the instruction in the code
   */
  codeId: number;
  /**
   * Name of the parsed instruction, matches name from InstructionFunctionLoader
   * Example: "addi"
   */
  instructionName: string;
  /**
   * @brief Original line of code
   * Example: "addi x1, x1, 5"
   */
  codeLine: string;
  /**
   * Arguments of the instruction
   */
  arguments: Array<InputCodeArgument>;
  /**
   * Type of the instruction
   */
  instructionTypeEnum: InstructionTypeEnum;
  /**
   * Data type of the output
   */
  resultDataType: DataTypeEnum;
}

/**
 * - kArithmetic - Instruction is arithmetic
 * - kLoadstore - Instruction does load/store operation
 * - kJumpbranch - Instruction does un/conditional jump in code
 * - kLabel - Instruction is jump label
 */
export type InstructionTypeEnum = {
  name: 'kArithmetic' | 'kLoadstore' | 'kJumpbranch' | 'kLabel';
};

export type DataTypeEnum = {
  name: 'kInt' | 'kLong' | 'kFloat' | 'kDouble' | 'kSpeculative';
};

/**
 * Instruction argument
 */
export interface InputCodeArgument {
  '@type': 'com.gradle.superscalarsim.models.InputCodeArgument';
  /**
   * The name of the argument (e.g. "rd")
   */
  name: string;
  /**
   * The value of the argument (e.g. "x1")
   */
  value: string;
}
