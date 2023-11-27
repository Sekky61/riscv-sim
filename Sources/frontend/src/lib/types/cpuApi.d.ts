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

export interface ReorderBufferBlock {
  reorderQueue: ReorderBufferItem[];
  commitLimit: number;
  commitId: number;
  speculativePulls: boolean;
  bufferSize: number;
  renameMapTableBlock?: RenameMapTableBlock;
  decodeAndDispatchBlock?: DecodeAndDispatchBlock;
  statisticsCounter?: StatisticsCounter;
  gShareUnit?: GShareUnit;
  branchTargetBuffer?: BranchTargetBuffer;
  instructionFetchBlock?: InstructionFetchBlock;
  loadBufferBlock?: LoadBufferBlock;
  storeBufferBlock?: StoreBufferBlock;
}

export type Reference = number;
export type StringReference = string;

export interface ManagerRegistry {
  instructionFunctionManager: Record<string, InstructionFunctionModel>;
  inputCodeManager: Record<string, InputCodeModel>;
  simCodeManager: Record<string, SimCodeModel>;
  registerModelManager: Record<string, RegisterModel>;
}

export interface InstructionMemoryBlock {
  nop: Reference;
  code: Reference[];
  labels: {
    [k: string]: Label;
  };
}

export interface Label {
  name: string;
  address: number;
}

export type InstructionTypeEnum = 'kArithmetic' | 'kLoadstore' | 'kJumpbranch';

export type DataTypeEnum =
  | 'kInt'
  | 'kUInt'
  | 'kLong'
  | 'kULong'
  | 'kFloat'
  | 'kDouble'
  | 'kBool';

export type RegisterReadinessEnum =
  | 'kFree'
  | 'kAllocated'
  | 'kExecuted'
  | 'kAssigned';

export type RegisterTypeEnum = 'kInt' | 'kFloat';

export interface InputCodeModel {
  codeId: number;
  instructionName: string;
  arguments: InputCodeArgument[];
  instructionTypeEnum: InstructionTypeEnum;
  instructionFunctionModel: Reference;
}

export interface InputCodeArgument {
  name: string;
  value: string;
}

export interface InstructionFunctionModel {
  name: string;
  instructionType: InstructionTypeEnum;
  arguments: Argument[];
  interpretableAs: string;
  // dataType: never; // TODO
  unconditionalJump: boolean;
}

export interface Argument {
  name: string;
  type: DataTypeEnum;
  defaultValue?: string;
  writeBack?: boolean;
  silent?: boolean;
}

export interface ReorderBufferItem {
  simCodeModel: Reference;
  reorderFlags: ReorderFlags;
}
export interface SimCodeModel {
  id: number;
  inputCodeModel: Reference;
  renamedArguments: InputCodeArgument[];
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
  registerMap: {
    [k: string]: Reference;
  };
  speculativeRegisterFile?: SpeculativeRegisterFile;
}
export interface RegisterModel {
  name: string;
  isConstant: boolean;
  type: RegisterTypeEnum;
  value: RegisterDataContainer;
  readiness: RegisterReadinessEnum;
  constant: boolean;
}
export interface RegisterDataContainer {
  bits: number;
  currentType: DataTypeEnum;
}

export interface RenameMapTableBlock {
  freeList: string[];
  registerMap: {
    [k: string]: RenameMapModel;
  };
  referenceMap: {
    [k: string]: number;
  };
  registerFileBlock?: UnifiedRegisterFileBlock;
}
export interface RenameMapModel {
  architecturalRegister: StringReference;
  order: number;
}
export interface InstructionFetchBlock {
  simCodeModelFactory?: SimCodeModelFactory;
  gShareUnit?: GShareUnit;
  branchTargetBuffer?: BranchTargetBuffer;
  branchFollowLimit: number;
  instructionMemoryBlock?: InstructionMemoryBlock;
  fetchedCode: Reference[];
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
  beforeRenameCodeList: Reference[];
  afterRenameCodeList: Reference[];
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

// Issue window blocks

export interface IssueWindowBlock {
  issuedInstructions: Reference[];
  argumentValidityMap: Record<Reference, IssueItemModel[]>;
  registerFileBlock?: Reference;
  windowId: number;
  functionUnitBlockList?: Reference[];
}

export interface IssueItemModel {
  tag: string;
  value: number;
  validityBit: boolean;
}

export type AluIssueWindowBlock = IssueWindowBlock;
export type FpIssueWindowBlock = IssueWindowBlock;
export type BranchIssueWindowBlock = IssueWindowBlock;
export type LoadStoreIssueWindowBlock = IssueWindowBlock;

// Function unit blocks

export interface FunctionUnitBlock {
  reorderBufferBlock?: Reference;
  simCodeModel: Reference;
  functionUnitId: number;
  functionUnitCount: number;
  issueWindowBlock?: Reference;
  delay: number;
  counter: number;
  name?: string;
  allowedOperators: string[];
  arithmeticInterpreter?: Reference;
  registerFileBlock?: Reference;
  functionUnitEmpty: boolean;
}

export type ArithmeticFunctionUnitBlock = FunctionUnitBlock;
export type BranchFunctionUnitBlock = FunctionUnitBlock;
export type MemoryAccessUnit = FunctionUnitBlock;

// L/S

export interface LoadBufferBlock {
  loadQueue: LoadBufferItem[];
  bufferSize: number;
  commitId: number;
  memoryAccessUnitList?: MemoryAccessUnit[];
  storeBufferBlock?: StoreBufferBlock;
  registerFileBlock?: UnifiedRegisterFileBlock;
  reorderBufferBlock?: ReorderBufferBlock;
  instructionFetchBlock?: InstructionFetchBlock;
}
export interface LoadBufferItem {
  simCodeModel: Reference;
  destinationRegister: string;
  destinationReady: boolean;
  address: number;
  isAccessingMemory: boolean;
  accessingMemoryId: number;
  memoryAccessId: number;
  hasBypassed: boolean;
  memoryFailedId: number;
  accessingMemory: boolean;
}

export interface StoreBufferBlock {
  storeQueue: StoreBufferItem[];
  bufferSize: number;
  commitId: number;
  memoryAccessUnitList?: MemoryAccessUnit[];
  loadStoreInterpreter?: CodeLoadStoreInterpreter;
  registerFileBlock?: UnifiedRegisterFileBlock;
  reorderBufferBlock?: ReorderBufferBlock;
}

export interface StoreBufferItem {
  simCodeModel: Reference;
  sourceRegister: string;
  sourceResultId: number;
  sourceReady: boolean;
  address: number;
  isAccessingMemory: boolean;
  accessingMemoryId: number;
  memoryAccessId: number;
  memoryFailedId: number;
  accessingMemory: boolean;
}

// Branch predictor

export interface BranchTargetBuffer {
  buffer: {
    [address: number]: BranchTargetEntryModel;
  };
  size: number;
}

export interface BranchTargetEntryModel {
  pcTag: number;
  isConditional: boolean;
  target: number;
  instructionId: number;
  commitId: number;
  conditional: boolean;
}

export interface GlobalHistoryRegister {
  shiftRegister: boolean[];
  history: {
    [k: string]: boolean[];
  };
  size: number;
}
export interface PatternHistoryTable {
  predictorMap: {
    [address: number]: IBitPredictor;
  };
  size: number;
  defaultPredictorClass: '0' | '1' | '2';
  defaultTaken: boolean[];
}

export interface IBitPredictor {
  state: null | [boolean] | [boolean, boolean]; // todo, how does 0bit predictor look like?
}
