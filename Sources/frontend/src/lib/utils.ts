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

import { MemoryLocationApi, dataTypes, dataTypesText } from '@/lib/forms/Isa';
import {
  DataTypeEnum,
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

/**
 * Save the string as a file.
 */
export function saveAsFile(content: string, filename: string): void {
  const blob = new Blob([content], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

/**
 * Save the passed object as a JSON file.
 */
// biome-ignore lint: any object can be serialized to JSON
export function saveAsJsonFile(object: any, filename: string): void {
  const content = JSON.stringify(object, null, 2);
  saveAsFile(content, filename);
}

/**
 * Show dialog to pick a file and calls the callback with file text contents once the file is picked.
 * TODO: Use this function in other places where file is loaded (config import), move to utils, generalize?
 */
export function loadFile(callback: (contents: string) => void) {
  // Show dialog
  const input = document.createElement('input');
  input.type = 'file';
  input.onchange = () => {
    if (!input.files) {
      console.warn('No file selected');
      return;
    }
    const file = input.files[0];
    if (file === undefined) {
      console.warn('No file selected');
      return;
    }
    const reader = new FileReader();
    reader.onload = (e) => {
      const contents = e.target?.result;
      if (typeof contents === 'string') {
        callback(contents);
      }
    };
    reader.readAsText(file);
  };
  input.click();
}

/**
 * Convert the data type to byte size.
 */
export function dataTypeToSize(dataType: DataTypeEnum): number {
  switch (dataType) {
    case 'kBool':
    case 'kChar':
    case 'kByte':
      return 1;
    case 'kShort':
      return 2;
    case 'kUInt':
    case 'kInt':
    case 'kFloat':
      return 4;
    case 'kDouble':
    case 'kULong':
    case 'kLong':
      return 8;
    default:
      return unreachable();
  }
}

/**
 * Convert the data type to human-readable string.
 */
export function dataTypeToText(dataType: DataTypeEnum): string {
  const i = dataTypes.indexOf(dataType);
  return dataTypesText[i] ?? dataType;
}

/**
 * Get the size of a memory location in elements
 */
export function memoryLocationSizeInElements(
  location: MemoryLocationApi,
): number {
  let dataLengthElements = 0;
  switch (location.data.kind) {
    case 'data':
      dataLengthElements = location.data.data.length;
      break;
    case 'constant': // fallthrough
    case 'random':
      dataLengthElements = location.data.size;
      break;
  }
  return dataLengthElements;
}

/**
 * Return the word in the plural form if the number is not 1
 */
export function pluralize(word: string, number: number): string {
  return number === 1 ? word : `${word}s`;
}

/**
 * Format the fraction as a percentage. Returns 0% for 0 denominator.
 */
export function formatFracPercentage(
  numerator: number,
  denominator: number,
): string {
  let frac = 0;
  if (denominator !== 0) {
    frac = (numerator / denominator) * 100;
  }
  return `${frac.toFixed(2)}%`;
}

/**
 * Return true if the number is a power of two
 */
export function isPowerOfTwo(n: number): boolean {
  return Math.log2(n) % 1 === 0;
}
