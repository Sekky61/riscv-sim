import type { CpuConfig, SimulationConfig } from '@/lib/forms/Isa';

/**
 * The default assembly code. Shown in the editor when the page is first loaded.
 */
export const defaultAsmCode: string =
  '  addi x3, x0, 3\nloop:\n  beq  x3,x0,loopEnd\n  subi x3,x3,1\n  jal  x0,loop\nloopEnd:';

/**
 * Default configuration
 */
export const defaultCpuConfig: CpuConfig = {
  name: 'Realistic',
  robSize: 256,
  fetchWidth: 4,
  branchFollowLimit: 1,
  commitWidth: 4,
  flushPenalty: 3,
  btbSize: 1024,
  phtSize: 10,
  predictorType: 'TWO_BIT_PREDICTOR',
  predictorDefaultState: 2,
  useGlobalHistory: false,
  fUnits: [
    {
      id: 0,
      name: 'Adder',
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
          name: 'special',
          latency: 2,
        },
      ],
    },
    {
      id: 1,
      name: 'Mult',
      fuType: 'FX',
      latency: 2,
      operations: [
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
      id: 3,
      name: 'L_S',
      fuType: 'L_S',
      latency: 1,
    },
    {
      id: 4,
      name: 'Branch',
      fuType: 'Branch',
      latency: 2,
    },
    {
      id: 5,
      name: 'Memory',
      fuType: 'Memory',
      latency: 2,
    },
  ],
  useCache: true,
  cacheLines: 64,
  cacheLineSize: 64,
  cacheAssoc: 4,
  cacheReplacement: 'LRU',
  storeBehavior: 'write-back',
  cacheAccessDelay: 1,
  storeLatency: 20,
  loadLatency: 20,
  laneReplacementDelay: 10,
  lbSize: 64,
  sbSize: 64,
  callStackSize: 512,
  speculativeRegisters: 320,
  coreClockFrequency: 100000000,
  cacheClockFrequency: 100000000,
};

/**
 * A configuration with smaller buffers, one bit predictor.
 * Simpler to see the work of the cpu.
 */
export const educationalCpuConfig: CpuConfig = {
  name: 'Educational',
  robSize: 32,
  fetchWidth: 3,
  branchFollowLimit: 1,
  commitWidth: 4,
  flushPenalty: 1,
  btbSize: 32,
  phtSize: 10,
  predictorType: 'ONE_BIT_PREDICTOR',
  predictorDefaultState: 0,
  useGlobalHistory: false,
  fUnits: [
    {
      id: 0,
      name: 'FX',
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
  lbSize: 16,
  sbSize: 16,
  callStackSize: 512,
  speculativeRegisters: 128,
  coreClockFrequency: 100000000,
  cacheClockFrequency: 100000000,
};

/**
 * Configuration with two float arrays
 */
export const floatSimulationConfig: SimulationConfig = {
  cpuConfig: {
    ...defaultCpuConfig,
    name: 'Float Arrays',
  },
  code: defaultAsmCode,
  memoryLocations: [
    {
      name: 'a',
      alignment: 4,
      dataType: 'kFloat',
      data: {
        kind: 'constant',
        constant: '7',
        size: 100,
      },
    },
    {
      name: 'b',
      alignment: 4,
      dataType: 'kFloat',
      data: {
        kind: 'constant',
        constant: '1',
        size: 100,
      },
    },
  ],
  entryPoint: 0,
};

/**
 * Educational config
 */
export const educationalSimulationConfig: SimulationConfig = {
  cpuConfig: educationalCpuConfig,
  code: defaultAsmCode,
  memoryLocations: [],
  entryPoint: 0,
};
