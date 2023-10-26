/**
 * @file    cpuApi.d.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Types for the API response from simulator backend
 *
 * @date    23 October 2023, 23:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023  Michal Majer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

export interface CpuState {
  tick: number;
  instructionMemoryBlock: InstructionMemoryBlock;
  simCodeModelAllocator: SimCodeModelAllocator;
  reorderBufferState: ReorderBufferState;
  statisticsCounter: StatisticsCounter;
  cacheStatisticsCounter: CacheStatisticsCounter;
  branchTargetBuffer: BranchTargetBuffer;
  globalHistoryRegister: GlobalHistoryRegister;
  patternHistoryTable: PatternHistoryTable;
  gShareUnit: GShareUnit;
  unifiedRegisterFileBlock: UnifiedRegisterFileBlock;
  renameMapTableBlock: RenameMapTableBlock;
  instructionFetchBlock: InstructionFetchBlockRef;
  decodeAndDispatchBlock: DecodeAndDispatchBlockRef;
  cache: Cache;
  memoryModel: MemoryModel;
  loadStoreInterpreter: CodeLoadStoreInterpreter;
  storeBufferBlock: StoreBufferBlock;
  loadBufferBlock: LoadBufferBlock;
  arithmeticInterpreter: CodeArithmeticInterpreter;
  arithmeticFunctionUnitBlocks: ArithmeticFunctionUnitBlock[];
  fpFunctionUnitBlocks: ArithmeticFunctionUnitBlock[];
  aluIssueWindowBlock: AluIssueWindowBlock;
  fpIssueWindowBlock: FpIssueWindowBlock;
  branchInterpreter: CodeBranchInterpreter;
  branchFunctionUnitBlocks: BranchFunctionUnitBlock[];
  branchIssueWindowBlock: BranchIssueWindowBlock;
  loadStoreFunctionUnits: LoadStoreFunctionUnit[];
  loadStoreIssueWindowBlock: LoadStoreIssueWindowBlock;
  memoryAccessUnits: MemoryAccessUnit[];
  simulatedMemory: SimulatedMemory;
  issueWindowSuperBlock: IssueWindowSuperBlock;
  reorderBufferBlock: ReorderBufferBlock;
}

/**
 * If an object is a reference, it will have a property '@ref' with a number value.
 * Another object with the same '@id' value will be the original object.
 */
export interface Reference {
  '@ref': number;
}

export type MaybeReference<T> = T | Reference;

/**
 * '@items' not being present means that there are no items
 */
export interface ArrayList<T> {
  '@type': 'ArrayList';
  '@items'?: Array<T>;
}

export interface WithId {
  '@id': number;
}

export interface InstructionMemoryBlock {
  nop: InputCodeModel;
  code: ArrayList<InputCodeModel>;
  labels: Record<string, number>;
}

export interface InputCodeModel {
  codeId: number;
  instructionName: string;
  arguments: ArrayList<InputCodeArgument>;
  instructionTypeEnum: InstructionTypeEnum;
  instructionFunctionModel: InstructionFunctionModel;
}

export interface InputCodeArgument {
  name: string;
  value: string;
}

export interface InstructionFunctionModel {
  name: string;
  instructionType: {
    name: string;
  };
  arguments: InstructionFunctionModelArgument[];
  interpretableAs: string;
  dataType: string | null;
}

export interface InstructionFunctionModelArgument {
  name: string;
  type: {
    name: string;
  };
  defaultValue: string | null;
  writeBack: boolean;
  silent: boolean;
}

export interface InstructionTypeEnum {
  name: string;
}

export interface InstructionFetchBlockRef {
  fetchedCode: ArrayList<MaybeReference<SimCodeModelRef>>;
  numberOfWays: number;
  pc: number;
  stallFlag: boolean;
  cycleId: number;
}

export interface DecodeAndDispatchBlockRef {
  beforeRenameCodeList: ArrayList<MaybeReference<SimCodeModelRef>>;
  afterRenameCodeList: ArrayList<MaybeReference<SimCodeModelRef>>;
  idCounter: number;
  flush: boolean;
  stallFlag: boolean;
  stalledPullCount: number;
}

export interface SimCodeModelRef {
  inputCodeModel: MaybeReference<InputCodeModel>;
  id: number;
  renamedArguments: ArrayList<InputCodeArgument>;
  instructionBulkNumber: number;
  issueWindowId: number;
  functionUnitId: number;
  readyId: number;
  commitId: number;
  isFinished: boolean;
  hasFailed: boolean;
  savedPc: number;
  branchPredicted: boolean;
  branchLogicResult: boolean;
  branchTargetOffset: number;
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
