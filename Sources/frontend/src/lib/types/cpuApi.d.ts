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
  managerRegistry: ManagerRegistry;
  tick: number;
  instructionMemoryBlock: InstructionMemoryBlock;
  reorderBufferState: ReorderBufferState;
  statisticsCounter: StatisticsCounter;
  cacheStatisticsCounter: CacheStatisticsCounter;
  branchTargetBuffer: BranchTargetBuffer;
  globalHistoryRegister: GlobalHistoryRegister;
  patternHistoryTable: PatternHistoryTable;
  gShareUnit: GShareUnit;
  unifiedRegisterFileBlock: UnifiedRegisterFileBlock;
  renameMapTableBlock: RenameMapTableBlock;
  instructionFetchBlock: InstructionFetchBlock;
  decodeAndDispatchBlock: DecodeAndDispatchBlock;
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

export type Reference = number;

export interface ManagerRegistry {
  instructionFunctionManager: Record<string, InstructionFunctionModel>;
  inputCodeManager: Record<string, InputCodeModel>;
  simCodeManager: Record<string, SimCodeModel>;
}

export interface InstructionMemoryBlock {
  nop: Reference;
  code: Reference[];
  labels: {
    [k: string]: number;
  };
}

export interface InputCodeModel {
  codeId: number;
  instructionName: string;
  arguments: InputCodeArgument[];
  instructionTypeEnum?: 'kArithmetic' | 'kLoadstore' | 'kJumpbranch';
  instructionFunctionModel?: InstructionFunctionModel;
}

export interface InputCodeArgument {
  name: string;
  value: string;
}

export interface InstructionFunctionModel {
  name?: string;
  instructionType?: 'kArithmetic' | 'kLoadstore' | 'kJumpbranch';
  arguments?: Argument[];
  interpretableAs?: string;
  dataType?:
    | 'kInt'
    | 'kUInt'
    | 'kLong'
    | 'kULong'
    | 'kFloat'
    | 'kDouble'
    | 'kBool';
  unconditionalJump: boolean;
}
export interface Argument {
  name?: string;
  type?: 'kInt' | 'kUInt' | 'kLong' | 'kULong' | 'kFloat' | 'kDouble' | 'kBool';
  defaultValue?: string;
  writeBack: boolean;
  silent: boolean;
}
export interface ReorderBufferState {
  reorderQueue?: ReorderBufferItem[];
  commitLimit: number;
  commitId: number;
  speculativePulls: boolean;
  bufferSize: number;
}
export interface ReorderBufferItem {
  simCodeModel?: SimCodeModel;
  reorderFlags?: ReorderFlags;
}
export interface SimCodeModel {
  id: number;
  inputCodeModel?: InputCodeModel;
  renamedArguments?: InputCodeArgument[];
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
  finished: boolean;
}
export interface ReorderFlags {
  isValid: boolean;
  isBusy: boolean;
  isSpeculative: boolean;
  valid: boolean;
  speculative: boolean;
  readyToBeCommitted: boolean;
  readyToBeRemoved: boolean;
  busy: boolean;
}
export interface GShareUnit {
  patternHistoryTable?: PatternHistoryTable;
  globalHistoryRegister?: GlobalHistoryRegister;
  size: number;
}
export interface UnifiedRegisterFileBlock {
  registerMap?: {
    [k: string]: RegisterModel;
  };
  speculativeRegisterFile?: SpeculativeRegisterFile;
}
export interface RegisterModel {
  name?: string;
  isConstant: boolean;
  type?: 'kInt' | 'kFloat';
  value?: RegisterDataContainer;
  readiness?: 'kFree' | 'kAllocated' | 'kExecuted' | 'kAssigned';
  constant: boolean;
}
export interface RegisterDataContainer {
  bits: number;
  currentType?:
    | 'kInt'
    | 'kUInt'
    | 'kLong'
    | 'kULong'
    | 'kFloat'
    | 'kDouble'
    | 'kBool';
}

export interface RenameMapTableBlock {
  freeList?: string[];
  registerMap?: {
    [k: string]: RenameMapModel;
  };
  referenceMap?: {
    [k: string]: number;
  };
  registerFileBlock?: UnifiedRegisterFileBlock;
}
export interface RenameMapModel {
  architecturalRegister?: string;
  order: number;
}
export interface InstructionFetchBlock {
  simCodeModelFactory?: SimCodeModelFactory;
  gShareUnit?: GShareUnit;
  branchTargetBuffer?: BranchTargetBuffer;
  branchFollowLimit: number;
  instructionMemoryBlock?: InstructionMemoryBlock;
  fetchedCode?: SimCodeModel[];
  numberOfWays: number;
  pc: number;
  stallFlag: boolean;
  cycleId: number;
}
export interface SimCodeModelFactory {
  id: number;
  manager?: unknown;
}
export interface DecodeAndDispatchBlock {
  beforeRenameCodeList?: SimCodeModel[];
  afterRenameCodeList?: SimCodeModel[];
  instructionFetchBlock?: InstructionFetchBlock;
  renameMapTableBlock?: RenameMapTableBlock;
  globalHistoryRegister?: GlobalHistoryRegister;
  branchTargetBuffer?: BranchTargetBuffer;
  instructionMemoryBlock?: InstructionMemoryBlock;
  idCounter: number;
  flush: boolean;
  stallFlag: boolean;
  stalledPullCount: number;
  decodeBufferSize: number;
}
