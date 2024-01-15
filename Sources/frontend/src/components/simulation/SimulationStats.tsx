/**
 * @file    SimulationStats.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The simulation stats
 *
 * @date    14 January 2024, 11:00 (created)
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

'use client';

import {
  Card,
  CardContent,
  CardTitle,
  CardHeader,
  CardDescription,
} from '@/components/base/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/base/ui/table';
import { ProgramInstruction } from '@/components/simulation/Program';
import {
  selectProgramWithLabels,
  selectStatistics,
} from '@/lib/redux/cpustateSlice';
import { useAppSelector } from '@/lib/redux/hooks';
import {
  FUStats,
  InstructionMix,
  InstructionStats,
  SimulationStatistics,
} from '@/lib/types/cpuApi';
import Link from 'next/link';

/**
 * @return The ratio in percentage, formatted. Zero if the denominator is zero.
 */
function formatRatio(numerator: number, denominator: number) {
  if (denominator === 0) {
    return '0%';
  }
  return `${((numerator / denominator) * 100).toFixed(2)}%`;
}

/**
 * TODO does not show (due to the null check) after reloading the stats page
 */
export function SimulationStats() {
  const statistics = useAppSelector(selectStatistics);

  if (!statistics) {
    return (
      <div className='text-2xl'>
        No Simulation Running.{' '}
        <Link href='/' className='link'>
          Start Simulating
        </Link>{' '}
        and come back here later.
      </div>
    );
  }

  const branchAccuracy = `${(statistics.predictionAccuracy * 100).toFixed(2)}%`;

  return (
    <div className='grid gap-6'>
      <div className='grid md:grid-cols-3 gap-6'>
        <Card>
          <CardHeader>
            <CardTitle>IPC</CardTitle>
          </CardHeader>
          <CardContent>
            <span className='text-7xl'>{statistics.ipc.toFixed(2)}</span>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Clocks</CardTitle>
          </CardHeader>
          <CardContent>
            <span className='text-7xl'>{statistics.clockCycles}</span>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Branch Prediction Accuracy</CardTitle>
          </CardHeader>
          <CardContent>
            <span className='text-7xl'>{branchAccuracy}</span>
          </CardContent>
        </Card>
        <InstructionMixDash
          title='Static Instruction Mix'
          description='Instruction mix of the program'
          mix={statistics.staticInstructionMix}
        />
        <InstructionMixDash
          title='Dynamic Instruction Mix'
          description='Instruction mix of committed instructions'
          mix={statistics.dynamicInstructionMix}
        />
        <FuStatsDash
          totalCycles={statistics.clockCycles}
          stats={statistics.fuStats}
        />
        <div className=''>
          <DetailedSimulationStats stats={statistics} />
        </div>
        <CacheStatistics stats={statistics} />
        <InstructionStatsCard
          instructionStats={statistics.instructionStats}
          commitCount={statistics.committedInstructions}
        />
      </div>
    </div>
  );
}

interface InstructionStatsProps {
  instructionStats: InstructionStats[];
  commitCount: number;
}

/**
 * Color gradient from #0600f9 to #f90006, linearly interpolated.
 * @param value The value to get the color for, range [0, 1]
 */
function getHeatMapColor(value: number) {
  const r = 6 + Math.floor(value * 249);
  const g = 0;
  const b = 249 - Math.floor(value * 249);
  return `${r}, ${g}, ${b}`;
}

function InstructionStatsCard({
  instructionStats,
  commitCount,
}: InstructionStatsProps) {
  const codeOrder = useAppSelector(selectProgramWithLabels);

  if (!codeOrder) {
    return null;
  }

  const allDecoded = instructionStats.reduce(
    (acc, { decoded }) => acc + decoded,
    0,
  );

  // Calculate normalized instruction counts
  const norms = Array.from({ length: codeOrder.length }, () => 0);
  let maxVal = 0;
  for (const instruction of codeOrder) {
    if (typeof instruction === 'string') {
      continue;
    }
    const st = instructionStats[instruction];
    if (!st) {
      continue;
    }
    const c = st.committedCount / commitCount;
    norms[instruction] = c;
    if (c > maxVal) {
      maxVal = c;
    }
  }
  // Normalize
  if (maxVal === 0) {
    maxVal = 1;
  }
  for (let i = 0; i < norms.length; i++) {
    norms[i] /= maxVal;
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Instruction Statistics</CardTitle>
        <CardDescription>Heat Map of execution counts</CardDescription>
      </CardHeader>
      <CardContent>
        <div className='pt-4'>
          {codeOrder.map((instructionOrLabel) => {
            if (typeof instructionOrLabel === 'string') {
              return (
                <div
                  key={`lab-${instructionOrLabel}`}
                  className='font-bold text-sm'
                >
                  {instructionOrLabel}:
                </div>
              );
            }
            const st = instructionStats[instructionOrLabel];
            if (!st) {
              return null;
            }
            const { committedCount, decoded, correctlyPredicted } = st;
            const heatCoef = norms[instructionOrLabel] ?? 0;
            const percentage = formatRatio(committedCount, commitCount);
            return (
              <div
                className='flex'
                style={
                  {
                    '--heat': getHeatMapColor(heatCoef),
                    backgroundColor: 'rgba(var(--heat), 0.2)',
                  } as React.CSSProperties
                }
                key={`ins-${instructionOrLabel}`}
              >
                <div className='font-mono text-sm text-gray-800 w-14 mr-2'>
                  {percentage}
                </div>
                <ProgramInstruction
                  key={`ins-${instructionOrLabel}`}
                  instructionId={instructionOrLabel}
                  showAddress={false}
                />
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}

interface DetailedStatsProps {
  stats: SimulationStatistics;
}

type DetailedStatName = keyof typeof detailedStatNames;
const detailedStatNames = {
  predictionAccuracy: 'Prediction Accuracy',
  committedInstructions: 'Committed Instructions',
  clockCycles: 'Clock Cycles',
  flushedInstructions: 'Flushed Instructions',
  robFlushes: 'ROB Flushes',
  correctlyPredictedBranches: 'Correctly Predicted Branches',
  conditionalBranches: 'Conditional Branches',
  takenBranches: 'Taken Branches',
  memoryTraffic: 'Memory Traffic',
  maxAllocatedRegisters: 'Max Allocated Registers',
  arithmeticIntensity: 'Arithmetic Intensity',
  flops: 'FLOPS',
  ipc: 'IPC',
  wallTime: 'Wall Time',
  memoryThroughput: 'Memory Throughput',
} as const;

function DetailedSimulationStats({ stats }: DetailedStatsProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Detailed Statistics</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Attribute</TableHead>
              <TableHead>Value</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {Object.entries(detailedStatNames).map(([name, displayName]) => {
              return (
                <TableRow>
                  <TableCell className='font-medium'>{displayName}</TableCell>
                  <TableCell>{stats[name as DetailedStatName]}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}

type CacheStatName = keyof typeof cacheStatNames;
const cacheStatNames = {
  readAccesses: 'Read Accesses',
  writeAccesses: 'Write Accesses',
  hits: 'Hits',
  misses: 'Misses',
  totalDelay: 'Total Delay',
  bytesWritten: 'Bytes Written',
  bytesRead: 'Bytes Read',
} as const;

function CacheStatistics({ stats }: DetailedStatsProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Cache Statistics</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Attribute</TableHead>
              <TableHead>Value</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {Object.entries(cacheStatNames).map(([name, displayName]) => {
              return (
                <TableRow>
                  <TableCell className='font-medium'>{displayName}</TableCell>
                  <TableCell>{stats.cache[name as CacheStatName]}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}

interface FuStatsProps {
  totalCycles: number;
  stats: {
    [fuName: string]: FUStats;
  };
}

function FuStatsDash({ stats, totalCycles }: FuStatsProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Functional Units</CardTitle>
        <CardDescription>
          Amount of time a unit spends computing
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Busy Cycles</TableHead>
              <TableHead>Utilization</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {Object.entries(stats).map(([name, stat]) => {
              return (
                <TableRow>
                  <TableCell className='font-medium'>{name}</TableCell>
                  <TableCell>{stat.busyCycles}</TableCell>
                  <TableCell>
                    {formatRatio(stat.busyCycles, totalCycles)}
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}

interface InstructionMixProps {
  title: string;
  description?: string;
  mix: InstructionMix;
}

function InstructionMixDash({ mix, title, description }: InstructionMixProps) {
  const total =
    mix.intArithmetic +
    mix.floatArithmetic +
    mix.branch +
    mix.memory +
    mix.other;
  const percentages = {
    intArithmetic: formatRatio(mix.intArithmetic, total),
    floatArithmetic: formatRatio(mix.floatArithmetic, total),
    branch: formatRatio(mix.branch, total),
    memory: formatRatio(mix.memory, total),
    other: formatRatio(mix.other, total),
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Category</TableHead>
              <TableHead>Value</TableHead>
              <TableHead>Proportion</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow>
              <TableCell className='font-medium'>Integer</TableCell>
              <TableCell>{mix.intArithmetic}</TableCell>
              <TableCell className='text-right'>
                {percentages.intArithmetic}
              </TableCell>
            </TableRow>
            <TableRow>
              <TableCell className='font-medium'>Float</TableCell>
              <TableCell>{mix.floatArithmetic}</TableCell>
              <TableCell className='text-right'>
                {percentages.floatArithmetic}
              </TableCell>
            </TableRow>
            <TableRow>
              <TableCell className='font-medium'>Branch</TableCell>
              <TableCell>{mix.branch}</TableCell>
              <TableCell className='text-right'>{percentages.branch}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell className='font-medium'>Memory</TableCell>
              <TableCell>{mix.memory}</TableCell>
              <TableCell className='text-right'>{percentages.memory}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell className='font-medium'>Other</TableCell>
              <TableCell>{mix.other}</TableCell>
              <TableCell className='text-right'>{percentages.other}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}
