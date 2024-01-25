/**
 * @file    Isa.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Type definition and validation for ISA configuration
 *
 * @date    19 September 2023, 22:00 (created)
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

import { z } from 'zod';

export const predictorDefaults = {
  '0bit': ['Taken', 'Not Taken'],
  '1bit': ['Taken', 'Not Taken'],
  '2bit': [
    'Strongly Not Taken',
    'Weakly Not Taken',
    'Weakly Taken',
    'Strongly Taken',
  ],
} as const;

export const predictorTypes = ['0bit', '1bit', '2bit'] as const;
export type PredictorType = (typeof predictorTypes)[number];

export const predictorStates = [
  'Taken',
  'Not Taken',
  'Strongly Taken',
  'Strongly Not Taken',
  'Weakly Taken',
  'Weakly Not Taken',
] as const;
export type PredictorState = (typeof predictorStates)[number];

export const cacheReplacementTypes = ['LRU', 'FIFO', 'Random'] as const;
export type CacheReplacementType = (typeof cacheReplacementTypes)[number];

export const storeBehaviorTypes = ['write-back', 'write-through'] as const;
export type StoreBehaviorType = (typeof storeBehaviorTypes)[number];

/**
 * THe difference between byte and char is in display and memory definition.
 */
export const dataTypes = [
  'kByte',
  'kShort',
  'kInt',
  'kUInt',
  'kLong',
  'kULong',
  'kFloat',
  'kDouble',
  'kBool',
  'kChar',
  'kByte',
  'kShort',
] as const;
export const dataTypesText = [
  'Byte',
  'Short',
  'Integer',
  'Unsigned Integer',
  'Long',
  'Unsigned Long',
  'Float',
  'Double',
  'Boolean',
  'Char',
  'Byte',
  'Short',
] as const;

/**
 * Definition of memory location, as the API expects it
 */

export const dataChunk = z.object({
  dataType: z.enum(dataTypes),
  values: z.array(z.string()),
});
export type DataChunk = z.infer<typeof dataChunk>;

export const memoryLocation = z.object({
  name: z.string().min(1),
  alignment: z.number().min(1).max(16),
  dataChunks: z.array(dataChunk),
});
export type MemoryLocationApi = z.infer<typeof memoryLocation>;

export const memoryLocationDefaultValue: MemoryLocationApi = {
  name: 'Array',
  alignment: 4,
  dataChunks: [],
};

/**
 * This is the memory location with additional fields for the form.
 * These extra fields are kept in the app, but not sent to the backend.
 */
export const memoryLocationIsa = memoryLocation.extend({
  dataType: z.enum(dataTypes),
  dataSource: z.enum(['constant', 'random', 'file']),
});
export type MemoryLocationIsa = z.infer<typeof memoryLocationIsa>;

export const arithmeticUnits = ['FX', 'FP'] as const;
export const otherUnits = ['L_S', 'Branch', 'Memory'] as const;
export const fuTypes = [...arithmeticUnits, ...otherUnits] as const;
export type FuTypes = (typeof fuTypes)[number];

export const operations = [
  'bitwise',
  'addition',
  'multiplication',
  'division',
  'special',
] as const;
export type Operations = (typeof operations)[number];

export const capability = z.object({
  name: z.enum(operations),
  latency: z.number().min(0),
});
export type Capability = z.infer<typeof capability>;

export const lsUnitSchema = z.object({
  id: z.number(),
  name: z.string(),
  latency: z.number().min(1).max(16),
  fuType: z.enum(otherUnits),
});
export type LsUnitConfig = z.infer<typeof lsUnitSchema>;

export const arithmeticUnitSchema = lsUnitSchema.extend({
  fuType: z.enum(arithmeticUnits), // Field overwrite
  operations: z.array(capability),
});
export type ArithmeticUnitConfig = z.infer<typeof arithmeticUnitSchema>;

export const fUnitSchema = z.union([lsUnitSchema, arithmeticUnitSchema]);
export type FUnitConfig = z.infer<typeof fUnitSchema>;

export function isArithmeticUnitConfig(
  fUnit: FUnitConfig,
): fUnit is ArithmeticUnitConfig {
  return fUnit.fuType === 'FX' || fUnit.fuType === 'FP';
}

/**
 * Schema for form validation.
 * Contains all the fields that are used in the form, including name.
 */
export const isaFormSchema = z
  .object({
    // Name. Used for saving different configurations.
    name: z.string(),
    // Buffers
    robSize: z.number().min(1).max(1024),
    commitWidth: z.number().min(1).max(10),
    branchFollowLimit: z.number().min(1).max(10),
    flushPenalty: z.number().min(1).max(100),
    fetchWidth: z.number().min(1).max(10),
    // Branch
    btbSize: z.number().min(1).max(16384),
    phtSize: z.number().min(1).max(16384),
    predictorType: z.enum(predictorTypes),
    predictorDefault: z.enum(predictorStates),
    useGlobalHistory: z.boolean(),
    // Functional Units
    fUnits: z.array(fUnitSchema),
    // Cache
    useCache: z.boolean(),
    cacheLines: z.number().min(1).max(65536),
    cacheLineSize: z.number().min(1).max(512),
    cacheLoadLatency: z.number().min(1).max(1000),
    cacheStoreLatency: z.number().min(1).max(1000),
    cacheAssoc: z.number().min(1),
    cacheReplacement: z.enum(cacheReplacementTypes),
    storeBehavior: z.enum(storeBehaviorTypes),
    laneReplacementDelay: z.number().min(0).max(1000),
    cacheAccessDelay: z.number().min(0).max(1000),
    // Memory
    lbSize: z.number().min(1).max(1024),
    sbSize: z.number().min(1).max(1024),
    storeLatency: z.number().min(1),
    loadLatency: z.number().min(1),
    callStackSize: z.number().min(0).max(65536),
    speculativeRegisters: z.number().min(1).max(1024),
    coreClockFrequency: z.number().min(1),
    cacheClockFrequency: z.number().min(1),
  })
  .refine((data) => {
    // Check the predictor
    const predictorDefault = data.predictorDefault;
    const predictorType = data.predictorType;
    const predictorDefaultsForType = predictorDefaults[predictorType];
    if (
      !(predictorDefaultsForType as readonly PredictorState[]).includes(
        predictorDefault,
      )
    ) {
      return "Predictor default state doesn't match the predictor type";
    }

    // Check that cacheAssoc <= cacheLines
    if (data.cacheAssoc > data.cacheLines) {
      return 'Cache associativity must be less than or equal to cache lines';
    }
    // Config is correct
    return true;
  });
export type CpuConfig = z.infer<typeof isaFormSchema>;

/**
 * The configuration that is sent to the backend.
 */
export const simulationConfig = z.object({
  /**
   * CPU configuration. Buffer sizes, functional units, etc.
   */
  cpuConfig: isaFormSchema,
  /**
   * Assembly code to be simulated.
   */
  code: z.string(),
  /**
   * Memory locations to be allocated.
   */
  memoryLocations: z.array(memoryLocationIsa),
  /**
   * Entry point of the program. A label or address.
   */
  entryPoint: z.union([z.string(), z.number()]),
});
export type SimulationConfig = z.infer<typeof simulationConfig>;

/**
 * Default configuration
 */
export const defaultCpuConfig: CpuConfig = {
  name: 'Default',
  robSize: 256,
  fetchWidth: 3,
  branchFollowLimit: 1,
  commitWidth: 4,
  flushPenalty: 1,
  btbSize: 1024,
  phtSize: 10,
  predictorType: '2bit',
  predictorDefault: 'Weakly Taken',
  useGlobalHistory: false,
  fUnits: [
    {
      id: 0,
      name: 'FX Universal',
      fuType: 'FX',
      latency: 2,
      operations: [
        {
          name: 'bitwise',
          latency: 1,
        },
        {
          name: 'addition',
          latency: 1,
        },
        {
          name: 'multiplication',
          latency: 2,
        },
        {
          name: 'division',
          latency: 10,
        },
        {
          name: 'special',
          latency: 2,
        },
      ],
    },
    {
      id: 1,
      name: 'FP',
      fuType: 'FP',
      latency: 2,
      operations: [
        {
          name: 'bitwise',
          latency: 1,
        },
        {
          name: 'addition',
          latency: 1,
        },
        {
          name: 'multiplication',
          latency: 2,
        },
        {
          name: 'division',
          latency: 10,
        },
        {
          name: 'special',
          latency: 2,
        },
      ],
    },
    {
      id: 2,
      name: 'L_S',
      fuType: 'L_S',
      latency: 1,
    },
    {
      id: 3,
      name: 'Branch',
      fuType: 'Branch',
      latency: 2,
    },
    {
      id: 4,
      name: 'Memory',
      fuType: 'Memory',
      latency: 1,
    },
  ],
  useCache: true,
  cacheLines: 16,
  cacheLineSize: 32,
  cacheAssoc: 2,
  cacheReplacement: 'LRU',
  storeBehavior: 'write-back',
  cacheAccessDelay: 1,
  storeLatency: 1,
  loadLatency: 1,
  laneReplacementDelay: 10,
  lbSize: 64,
  sbSize: 64,
  callStackSize: 512,
  speculativeRegisters: 320,
  coreClockFrequency: 100000000,
  cacheClockFrequency: 100000000,
  cacheLoadLatency: 1,
  cacheStoreLatency: 1,
};

export const defaultSimulationConfig: SimulationConfig = {
  cpuConfig: defaultCpuConfig,
  code: '',
  memoryLocations: [],
  entryPoint: 0,
};
