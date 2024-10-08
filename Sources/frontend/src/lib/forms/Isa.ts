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

import { defaultAsmCode, defaultCpuConfig } from '@/constant/defaults';
import { isPowerOfTwo } from '@/lib/utils';
import { ZodIssueCode, z } from 'zod';

export const predictorTypes = [
  'ZERO_BIT_PREDICTOR',
  'ONE_BIT_PREDICTOR',
  'TWO_BIT_PREDICTOR',
] as const;
export type PredictorType = (typeof predictorTypes)[number];

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
] as const;

/**
 * Definition of memory location, as the API expects it
 */
const spanTypeSchema = z.object({
  startOffset: z.number(),
  dataType: z.enum(dataTypes),
});
export type SpanType = z.infer<typeof spanTypeSchema>;

/**
 * This schema disallows some of the accepted forms, but it is made so
 * that the web interface is simpler.
 * Form more info, check out the API documentation.
 */
export const memoryLocationSchema = z
  .object({
    name: z.string().min(1),
    alignment: z.number().min(0).max(16),
    dataType: z.enum(dataTypes),
    data: z.discriminatedUnion('kind', [
      z.object({
        kind: z.literal('data'),
        data: z.array(z.string()),
      }),
      z.object({
        kind: z.literal('constant'),
        constant: z.string(),
        size: z.number().min(1),
      }),
      z.object({
        kind: z.literal('random'),
        min: z.number(),
        max: z.number(),
        size: z.number().min(1),
      }),
    ]),
  })
  .superRefine((val, ctx) => {
    // Runs just for the one discriminated variant. If no kind is present, it's not run.

    // Random
    if (val.data.kind === 'random') {
      if (val.data.min > val.data.max) {
        ctx.addIssue({
          code: ZodIssueCode.too_small,
          path: ['data', 'max'],
          type: 'number',
          minimum: val.data.min,
          inclusive: true,
          exact: false,
        });
      }
    }
  });
export type MemoryLocationApi = z.infer<typeof memoryLocationSchema>;

export const memoryLocationDefaultValue: MemoryLocationApi = {
  name: 'Array',
  alignment: 4,
  dataType: 'kInt',
  data: {
    kind: 'data',
    data: [],
  },
};

/**
 * A collection of Memory Locations
 */
export const memoryLocationsSchema = z.array(memoryLocationSchema);
export type MemoryLocations = z.infer<typeof memoryLocationsSchema>;

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
    flushPenalty: z.number().min(1).max(100),
    fetchWidth: z.number().min(1).max(10),
    branchFollowLimit: z.number().min(1).max(10),
    // Branch
    btbSize: z.number().min(1).max(16384),
    phtSize: z.number().min(1).max(16384),
    predictorType: z.enum(predictorTypes),
    predictorDefaultState: z.number().min(0).max(3), // The maximum for 2bit (4 states). Must be further validated.
    useGlobalHistory: z.boolean(),
    // Functional Units
    fUnits: z.array(fUnitSchema),
    // Cache
    useCache: z.boolean(),
    cacheLines: z.number().min(1).max(65536),
    cacheLineSize: z.number().min(1).max(512),
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
  .superRefine((data, ctx) => {
    // Check the predictor
    const predictorDefault = data.predictorDefaultState;
    const predictorType = data.predictorType;
    if (
      predictorType === 'ZERO_BIT_PREDICTOR' ||
      predictorType === 'ONE_BIT_PREDICTOR'
    ) {
      if (predictorDefault !== 0 && predictorDefault !== 1) {
        ctx.addIssue({
          code: ZodIssueCode.invalid_enum_value,
          path: ['predictorDefaultState'],
          message: 'Predictor default state must be Taken or Not Taken',
          options: [0, 1],
          received: predictorDefault,
        });
      }
    }

    // Check that all indexes exist in cache (cacheLine / cacheAssoc = number of sets)
    const dividesEvenly = data.cacheLines % data.cacheAssoc === 0;
    const sets = data.cacheLines / data.cacheAssoc;
    const setsIsPowerOfTwo = isPowerOfTwo(sets);
    if (!dividesEvenly) {
      ctx.addIssue({
        code: ZodIssueCode.custom,
        path: ['cacheAssoc'],
        message: 'Cache lines must be divisible by cache associativity',
      });
    }
    if (!setsIsPowerOfTwo) {
      ctx.addIssue({
        code: ZodIssueCode.custom,
        path: ['cacheLines'],
        message: 'Number of sets must be a power of two',
      });
    }

    // cacheLineSize must be a power of two
    if (!isPowerOfTwo(data.cacheLineSize)) {
      ctx.addIssue({
        code: ZodIssueCode.custom,
        path: ['cacheLineSize'],
        message: 'Cache line size must be a power of two',
      });
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
  memoryLocations: memoryLocationsSchema,
  /**
   * Entry point of the program. A label or address.
   */
  entryPoint: z.union([z.string(), z.number()]),
});
export type SimulationConfig = z.infer<typeof simulationConfig>;

export const defaultSimulationConfig: SimulationConfig = {
  cpuConfig: defaultCpuConfig,
  code: defaultAsmCode,
  memoryLocations: [],
  entryPoint: 0,
};

export { defaultCpuConfig };
