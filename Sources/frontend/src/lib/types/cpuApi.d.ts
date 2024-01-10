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

export interface CpuState {
  managerRegistry: ManagerRegistry;
  tick: number;
  instructionMemoryBlock: InstructionMemoryBlock;
  simulationStatistics: StatisticsCounter;
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
}

export interface Cache {
  numberOfLines: number;
  associativity: number;
  lineSize: number;
  cache: CacheLineModel[][];
  writeBack: boolean;
  lastAccess?: CacheAccess[]; // todo ?
  storeDelay: number;
  loadDelay: number;
  lineReplacementDelay: number;
  addRemainingDelayToStore: boolean;
  cycleEndOfReplacement: number;
  replacementPolicyType: 'FIFO' | 'LRU' | 'RANDOM';
  replacementPolicy: ReplacementPolicyModel;
  memory?: SimulatedMemory;
  cacheStatisticsCounter?: CacheStatisticsCounter;
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
export interface CacheAccess {
  id: number;
  clockCycle: number;
  isHit?: boolean[];
  isStore: boolean;
  tag: number;
  index: number;
  offset: number;
  data: number;
  cacheIndex?: number[];
  lineOffset?: number[];
  delay: number;
  endOfReplacement: number;
  store: boolean;
}

export interface SimulatedMemory {
  /**
   * Memory encoded as a base64 string
   */
  memoryBase64: string | null;
  /**
   * Size of the memory in bytes
   */
  size: number;
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
  instructionName: string;
  arguments: InputCodeArgument[];
  instructionTypeEnum: InstructionTypeEnum;
  instructionFunctionModel: Reference;
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
  renamedCodeLine: string;
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
  stringRepresentation?: string;
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
  instructionType: InstructionTypeEnum;
  windowId: number;
  functionUnitBlockList?: Reference[];
  registerFileBlock?: UnifiedRegisterFileBlock;
}

// Function unit blocks

export interface FunctionUnitBlock {
  reorderBufferBlock?: Reference;
  simCodeModel?: Reference; // todo it is actually a Reference | null
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

export interface AbstractFunctionUnitBlock {
  functionUnitId: number;
  description: FunctionalUnitDescription;
  delay: number;
  counter: number;
  simCodeModel: Reference;
  functionUnitEmpty: boolean;
  functionUnitCount: number;
  reorderBufferBlock?: ReorderBufferBlock;
  issueWindowBlock?: IssueWindowBlock;
}

export interface FunctionalUnitDescription {
  id: number;
  name: string;
  latency: number;
  fuType: 'FX' | 'FP' | 'L_S' | 'Branch' | 'Memory';
  operations?: Capability[];
}

export type ArithmeticFunctionUnitBlock = AbstractFunctionUnitBlock;
export type BranchFunctionUnitBlock = AbstractFunctionUnitBlock;
export type MemoryAccessUnit = AbstractFunctionUnitBlock;

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
