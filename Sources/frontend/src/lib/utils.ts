/**
 * @file    utils.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Utils for ui components
 *
 * @date    10 November 2023, 16:00 (created)
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

import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

import {
  InputCodeModel,
  Reference,
  RegisterModel,
  StopReason,
} from '@/lib/types/cpuApi';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Reference is valid if it is present and has non-negative value. A type guard.
 *
 * @param ref  Reference to check
 * @returns True if reference present and valid, false otherwise
 */
export function isValidReference(ref: Reference | null): ref is Reference {
  return typeof ref === 'number' && ref >= 0;
}

/**
 * Converts InputCodeId to hex string representing the address.
 * Does not pad with zeros.
 */
export function inputCodeAddress(instructionId: number): string {
  return `0x${(instructionId * 4).toString(16)}`;
}

/**
 * Converts number to hex string padded to even number of characters.
 */
export function hexPadEven(num: number): string {
  const hex = num.toString(16);
  return `0x${hex.length % 2 ? `0${hex}` : hex}`;
}

/**
 * Converts number to hex string padded to 4B.
 */
export function hexPad(num: number): string {
  const hex = num.toString(16).padStart(8, '0');
  return `0x${hex}`;
}

/**
 * Converts number to binary string padded to 32 bits.
 */
export function binPad32(num: number): string {
  return `0b${num.toString(2).padStart(32, '0')}`;
}

/**
 * Returns true if a register has a valid value.
 */
export function isValidRegisterValue(register: RegisterModel): boolean {
  return (
    register.readiness === 'kExecuted' || register.readiness === 'kAssigned'
  );
}

/**
 * Return the name of the instruction type.
 */
export function instructionTypeName(inputCodeModel: InputCodeModel): string {
  switch (inputCodeModel.instructionTypeEnum) {
    case 'kIntArithmetic':
      return 'Arithmetic (int)';
    case 'kFloatArithmetic':
      return 'Arithmetic (float)';
    case 'kLoadstore':
      return 'Load/Store';
    case 'kJumpbranch':
      return 'Jump/Branch';
  }
}

/**
 * Format a number with a unit.
 * @param value the value to format
 * @param base base of the unit to divide by
 * @param units array of units to use. The first unit is used for values < base.
 * @returns formatted string
 */
export function formatNumberWithUnit(
  value: number,
  base = 1000,
  units: string[] = ['Hz', 'kHz', 'MHz', 'GHz', 'THz'],
): string {
  let unitIndex = 0;
  let val = value;

  while (val >= base && unitIndex < units.length - 1) {
    val /= base;
    unitIndex++;
  }

  // Cap the index at the last unit
  const unitFinalIndex = Math.min(unitIndex, units.length - 1);

  // If the value is less than 1 and has a decimal part, show one decimal place
  if (val % 1 !== 0) {
    return `${val.toFixed(1)} ${units[unitFinalIndex]}`;
  }

  // Otherwise show no decimal places
  const valInt = Math.round(val);
  return `${valInt} ${units[unitFinalIndex]}`;
}

/**
 * Convert stopReason to a short human-readable string.
 */
export function stopReasonToShortString(stopReason: StopReason): string {
  switch (stopReason) {
    case 'kBadConfig':
      return 'Bad Config';
    case 'kCallStackHalt':
      return 'Finished';
    case 'kEndOfCode':
      return 'Finished';
    case 'kException':
      return 'Exception';
    case 'kMaxCycles':
      return 'Timeout';
    case 'kTimeOut':
      return 'Timeout';
    case 'kNotStopped':
      return 'Running';
    default:
      return unreachable();
  }
}

function unreachable(): never {
  throw new Error('Unreachable');
}
