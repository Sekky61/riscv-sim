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

import { Reference } from '@/lib/types/cpuApi';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Reference is valid if it is present and has non-negative value. A type guard.
 *
 * @param ref  Reference to check
 * @returns True if reference present and valid, false otherwise
 */
export function isValidReference(ref?: Reference): ref is Reference {
  return typeof ref == 'number' && ref >= 0;
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
  return '0x' + (hex.length % 2 ? `0${hex}` : hex);
}
