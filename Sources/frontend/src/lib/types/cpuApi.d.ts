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

import type { Capability } from '@/lib/forms/Isa';

/**
 * Part of the API is defined in schemas.
 * Otherwise, generated from JSON schema (using https://transform.tools/json-schema-to-typescript).
 * See backend endpoint `POST /schema`.
 */

export type Reference = number;
export type StringReference = string;

export interface CpuState {
  managerRegistry: ManagerRegistry;
  tick: number;
  instructionMemoryBlock: InstructionMemoryBlock;
  statistics: SimulationStatistics;
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
  aluIssueWindowBlock: IssueWindowBlock;
  fpIssueWindowBlock: IssueWindowBlock;
  branchInterpreter: CodeBranchInterpreter;
  branchFunctionUnitBlocks: BranchFunctionUnitBlock[];
  branchIssueWindowBlock: IssueWindowBlock;
  loadStoreFunctionUnits: LoadStoreFunctionUnit[];
  loadStoreIssueWindowBlock: IssueWindowBlock;
  memoryAccessUnits: MemoryAccessUnit[];
  simulatedMemory: SimulatedMemory;
  issueWindowSuperBlock: IssueWindowSuperBlock;
  reorderBufferBlock: ReorderBufferBlock;
  debugLog: DebugLog;
}

export type SimulationStatistics = {
  staticInstructionMix: InstructionMix;
  dynamicInstructionMix: InstructionMix;
  cache: CacheStatistics;
  fuStats: {
    [fuName: string]: FUStats;
  };
  instructionStats: InstructionStats[];
  committedInstructions: number;
  clockCycles: number;
  flushedInstructions: number;
  robFlushes: number;
  clock: number;
  correctlyPredictedBranches: number;
  conditionalBranches: number;
  takenBranches: number;
  mainMemoryLoadedBytes: number;
  mainMemoryStoredBytes: number;
  maxAllocatedRegisters: number;
  arithmeticIntensity: number;
  predictionAccuracy: number;
  flops: number;
  ipc: number;
  wallTime: number;
  memoryThroughput: number;
};

export type StopReason =
  | 'kNotStopped'
  | 'kException'
  | 'kEndOfCode'
  | 'kCallStackHalt'
  | 'kMaxCycles'
  | 'kTimeOut'
  | 'kBadConfig';

export interface InstructionMix {
  intArithmetic: number;
  floatArithmetic: number;
  memory: number;
  branch: number;
  other: number;
}

export interface CacheStatistics {
  readAccesses: number;
  writeAccesses: number;
  hits: number;
  misses: number;
  totalDelay: number;
  bytesWritten: number;
  bytesRead: number;
  hitRate: number;
}

export interface FUStats {
  busyCycles: number;
}

export interface InstructionStats {
  committedCount: number;
  decoded: number;
  correctlyPredicted: number;
  /**
   * NULL for non-load/store instructions
   */
  cacheHits: number | null;
  memoryAccesses: number | null;
}

export interface Cache {
  numberOfLines: number;
  associativity: number;
  lineSize: number;
  cache: CacheLineModel[][];
  writeBack: boolean;
  storeDelay: number;
  loadDelay: number;
  lineReplacementDelay: number;
  addRemainingDelayToStore: boolean;
  cacheAccessId: number;
  cycleEndOfReplacement: number;
  replacementPolicyType: 'FIFO' | 'LRU' | 'RANDOM';
  replacementPolicy: ReplacementPolicyModel;
  memory?: SimulatedMemory;
  statistics: Reference;
  offsetBits: number;
  indexBits: number;
}
export interface CacheLineModel {
  line: string; // base64 encoded
  valid: boolean;
  dirty: boolean;
  tag: number;
  lineSize: number;
  index: number;
  baseAddress: number;
}
export type ReplacementPolicyModel = object;

export interface SimulatedMemory {
  memoryBase64: string; // base64 encoded
  size: number;
  storeLatency: number;
  loadLatency: number;
  transactionId: number;
  operations: MemoryTransaction[];
  statistics: Reference;
}

export interface MemoryTransaction {
  data?: number[];
  handledBy: 'main_memory' | 'cache' | 'cache_with_miss' | null;
  mmuId: number;
  timestamp: number;
  address: number;
  size: number;
  isStore: boolean;
  isSigned: boolean;
  id: number;
  isFinished: boolean;
  latency: number;
  finished: boolean;
  signed: boolean;
  store: boolean;
}

export interface MemoryModel {
  cache: Cache;
  memory: SimulatedMemory;
  statistics: Reference;
}

export interface ReorderBufferBlock {
  /**
   * Reference to a SimCodeModel
   */
  reorderQueue: Reference[];
  stopReason: StopReason;
  haltTarget: number;
  commitLimit: number;
  commitId: number;
  bufferSize: number;
  renameMapTableBlock?: RenameMapTableBlock;
  decodeAndDispatchBlock?: DecodeAndDispatchBlock;
  simulationstatistics: Reference;
  gShareUnit?: GShareUnit;
  branchTargetBuffer?: BranchTargetBuffer;
  instructionFetchBlock?: InstructionFetchBlock;
  loadBufferBlock?: LoadBufferBlock;
  storeBufferBlock?: StoreBufferBlock;
  debugLog: DebugLog;
}

export interface DebugLog {
  entries: Entry[];
  registerFile: Reference;
}

export interface Entry {
  message: string;
  cycle: number;
}

export interface ManagerRegistry {
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
  address: RegisterDataContainer;
}

export type InstructionTypeEnum =
  | 'kIntArithmetic'
  | 'kFloatArithmetic'
  | 'kLoadstore'
  | 'kJumpbranch';

export type DataTypeEnum =
  | 'kByte'
  | 'kShort'
  | 'kInt'
  | 'kUInt'
  | 'kLong'
  | 'kULong'
  | 'kFloat'
  | 'kDouble'
  | 'kBool'
  | 'kChar';

export type RegisterReadinessEnum =
  | 'kFree'
  | 'kAllocated'
  | 'kExecuted'
  | 'kAssigned';

export type RegisterTypeEnum = 'kInt' | 'kFloat';

export interface InputCodeModel {
  codeId: number;
  instructionFunctionModel: Reference;
  arguments: InputCodeArgument[];
  debugInfo: DebugInfo;
}

export interface DebugInfo {
  formatString: string;
}

export interface InputCodeArgument {
  name: string;
  stringValue: string;
  constantValue: RegisterDataContainer | null;
  registerValue: StringReference | null;
}

export interface InstructionFunctionModel {
  name: string;
  instructionType: InstructionTypeEnum;
  arguments: InstructionArgument[];
  interpretableAs: string;
  unconditionalJump: boolean;
  /**
   * Tokens for syntax of the instruction
   */
  syntaxTemplate: string[];
}

export interface InstructionArgument {
  name: string;
  type: DataTypeEnum;
  defaultValue?: string;
  writeBack: boolean;
  silent: boolean;
  isOffset: boolean;
  register: boolean;
  immediate: boolean;
}

export interface SimCodeModel {
  id: number;
  inputCodeModel: Reference;
  renamedArguments: InputCodeArgument[];
  issueWindowId: number;
  fetchId: number;
  functionUnitId: number;
  readyId: number;
  commitId: number;
  isFinished: boolean;
  hasFailed: boolean;
  branchInfo: BranchInfo | null;
  isValid: boolean;
  isBusy: boolean;
  isSpeculative: boolean;
  exception: InstructionException | null;
  conditionalBranch: boolean;
  store: boolean;
  load: boolean;
  readyToExecute: boolean;
  readyToBeCommitted: boolean;
}

export interface BranchInfo {
  predictorVerdict: boolean;
  predictedTarget: number;
  branchCondition: boolean;
  branchTarget: number;
  branchComputedInDecode: boolean;
  predictorIndex: number;
  predictorStateBeforePrediction: number;
}

export interface InstructionException {
  exceptionKind: 'kNone' | 'kArithmetic' | 'kMemory';
  exceptionMessage: string;
  cycle: number;
}

export interface GShareUnit {
  size: number;
  useGlobalHistory: boolean;
  patternHistoryTable?: PatternHistoryTable;
  globalHistoryRegister?: GlobalHistoryRegister;
}

export interface UnifiedRegisterFileBlock {
  /**
   * Only architectural, but including aliases
   */
  registerMap: {
    [k: string]: Reference;
  };
  speculativeRegisterFile: SpeculativeRegisterFile;
}

export interface SpeculativeRegisterFile {
  name: string;
  numberOfRegisters: number;
  /**
   * Ref to RegisterModel
   */
  registers: {
    [k: string]: StringReference;
  };
}

export interface RegisterModel {
  name: string;
  isConstant: boolean;
  type: RegisterTypeEnum;
  value: RegisterDataContainer;
  readiness: RegisterReadinessEnum;
  referenceCount: number;
  /**
   * Ref to RegisterModel
   */
  renames: StringReference[];
  architecturalRegister: StringReference | null;
  speculative: boolean;
}

export interface RegisterDataContainer {
  bits: number;
  currentType: DataTypeEnum;
  stringRepresentation: string;
}

export interface RenameMapTableBlock {
  freeList: string[];
  registerFileBlock?: UnifiedRegisterFileBlock;
  allocatedSpeculativeRegistersCount: number;
}
export interface RenameMapModel {
  architecturalRegister: StringReference;
  order: number;
}
export interface InstructionFetchBlock {
  fetchedCode: Reference[];
  branchFollowLimit: number;
  numberOfWays: number;
  pc: number;
  stallFlag: boolean;
  instructionMemoryBlock?: InstructionMemoryBlock;
  branchTargetBuffer?: BranchTargetBuffer;
  gShareUnit?: GShareUnit;
  simCodeModelFactory?: SimCodeModelFactory;
}
export interface SimCodeModelFactory {
  id: number;
  manager?: unknown;
}

export interface DecodeAndDispatchBlock {
  codeBuffer: Reference[];
  stallFlag: boolean;
  decodeBufferSize: number;
  instructionFetchBlock?: InstructionFetchBlock;
  renameMapTableBlock?: RenameMapTableBlock;
  globalHistoryRegister?: GlobalHistoryRegister;
  branchTargetBuffer?: BranchTargetBuffer;
  statistics: Reference;
}

// Issue window blocks

export interface IssueWindowBlock {
  issuedInstructions: Reference[];
  instructionType: InstructionTypeEnum;
  windowId: number;
  functionUnitBlockList?: Reference[];
}

// Function unit blocks

export interface AbstractFunctionUnitBlock {
  functionUnitId: number;
  description: FunctionalUnitDescription;
  simCodeModel: Reference | null;
  delay: number;
  counter: number;
  functionUnitEmpty: boolean;
  functionUnitCount: number;
  issueWindowBlock?: IssueWindowBlock;
  statistics: Reference;
}

export interface FunctionalUnitDescription {
  id: number;
  name: string;
  latency: number;
  fuType: 'FX' | 'FP' | 'L_S' | 'Branch' | 'Memory';
  /**
   * Only for FX and FP
   */
  operations?: Capability[];
}

export type ArithmeticFunctionUnitBlock = AbstractFunctionUnitBlock;
export type BranchFunctionUnitBlock = AbstractFunctionUnitBlock;
export type LoadStoreFunctionUnit = AbstractFunctionUnitBlock;
export type MemoryAccessUnit = AbstractFunctionUnitBlock & {
  transaction: MemoryTransaction;
  handledBy: string | null;
};

// L/S

export interface LoadBufferBlock {
  loadQueue: LoadBufferItem[];
  bufferSize: number;
  memoryAccessUnitList?: MemoryAccessUnit[];
  storeBufferBlock?: StoreBufferBlock;
}

export interface LoadBufferItem {
  simCodeModel: Reference;
  destinationRegister: StringReference;
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
  memoryAccessUnitList?: MemoryAccessUnit[];
}

export interface StoreBufferItem {
  simCodeModel: Reference;
  sourceRegister: StringReference;
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
  conditional: boolean;
}

/**
 * Invariant: shiftRegisters.length >= 1
 */
export interface GlobalHistoryRegister {
  size: number;
  shiftRegisters: Register[];
}
export interface Register {
  shiftRegister: number;
  codeId: number;
}

export interface PatternHistoryTable {
  predictorMap: {
    [k: string]: BitPredictor;
  };
  size: number;
  defaultPredictor: BitPredictor;
}

export interface BitPredictor {
  state: number;
  bitWidth: number;
}
