/**
 * @file    util.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Utility functions for CPU state
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

import type { ArrayList, Reference, WithId } from '@/lib/types/cpuApi';
import type { SimCodeModel } from '@/lib/types/cpuDeref';

/**
 * Type guard for Reference type.
 * Object is a reference if it has a '@ref' property
 */
export function isReference(obj: unknown): obj is Reference {
  if (typeof obj !== 'object' || obj === null) {
    return false;
  }
  return '@ref' in obj;
}

/**
 * Type guard for WithId interface.
 */
export function hasId(obj: unknown): obj is WithId {
  if (typeof obj !== 'object' || obj === null) {
    return false;
  }
  return '@id' in obj;
}

export type IdMap = { [id: number]: object };

/**
 * Collect all ids from an object, put the ids and objects in a map.
 * Adapted from https://github.com/jdereg/json-io/blob/master/jsonUtil.js
 * @param state any object
 * @returns map of ids to objects
 */
export function collectIds(state: object): IdMap {
  const idMap: IdMap = {};
  if (!state) return idMap;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const stack: Array<{ [key: string]: any }> = [state];
  while (stack.length > 0) {
    const currentObj = stack.pop();

    for (const field in currentObj) {
      const value = currentObj[field];

      if (!value) continue;

      if (field === '@id') {
        idMap[value] = currentObj;
      } else if (typeof value === 'object') {
        stack.push(value);
      }
    }
  }
  return idMap;
}

/**
 * Type that removes all (Reference | T) to a plain T.
 * This expresses the fact that a reference is resolved to the object it points to.
 */
export type Resolved<T> = ExcludeReference<T>;

export type ExcludeReference<T> = T extends Reference
  ? never
  : T extends object
  ? { [K in keyof T]: ExcludeReference<T[K]> }
  : T;

/**
 * A variant of resolveRefs that creates a copy of the object. This means no cycles can be present.
 * @param obj object to resolve
 * @param map map of ids to objects
 * @returns a copy of the object with all references resolved
 */
export function resolveRefsCopy<T>(obj: T, map: IdMap): Resolved<T> {
  if (typeof obj !== 'object' || obj === null || obj === undefined) {
    // Do nothing to primitives
    return obj as Resolved<T>;
  } else if (isReference(obj)) {
    // obj has a '@ref' property, resolve it
    const mapValue = map[obj['@ref']];
    if (!mapValue) {
      throw new Error(`Reference ${obj['@ref']} not found in map`);
    }
    return resolveRefsCopy(mapValue, map) as Resolved<T>;
  } else {
    // Not a reference, for all properties recursively resolve references (array or plain object)

    if (Array.isArray(obj)) {
      return obj.map((item) => resolveRefsCopy(item, map)) as Resolved<T>;
    }

    const resolved: Record<string, unknown> = {};
    // recursively visit all properties of the object
    for (const [key, value] of Object.entries(obj)) {
      resolved[key] = resolveRefsCopy(value, map);
    }

    return resolved as Resolved<T>;
  }
}

/**
 * Given an object and a map of ids, creates a copy of the object with all references resolved
 * TODO: Can we do it without changing object identity?
 * @warning Does not detect circular references
 *
 * Uses the 'as' operator to cast the result to the correct type. The safety is ensured by tests.
 */
export function resolveRefs<T extends object>(obj: T, map: IdMap): Resolved<T> {
  substitute(null, null, obj, map);
  return obj as Resolved<T>;
}

/**
 * Adapted from https://github.com/jdereg/json-io/blob/master/jsonUtil.js
 * @param parent
 * @param fieldName
 * @param jObj
 * @param idsToObjs
 */
function substitute(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  parent: { [key: string]: any } | null,
  fieldName: string | null,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  jObj: { [key: string]: any },
  idsToObjs: IdMap,
) {
  if (!jObj) return;

  for (const field in jObj) {
    const value = jObj[field];

    if (!value) continue;

    if (field === '@ref') {
      if (parent && fieldName) {
        // Recursively substitute the reference
        const ref = idsToObjs[value];

        if (!ref) throw new Error(`Reference ${value} not found in map`);

        // substitute(parent, fieldName, ref, idsToObjs);
        parent[fieldName] = ref;
      }
    } else if (typeof value === 'object') {
      substitute(jObj, field, value, idsToObjs);
    }
  }
}

/**
 * Type guard for SimCodeModel type.
 */
export function isSimCodeModel(obj: unknown): obj is SimCodeModel {
  if (typeof obj !== 'object' || obj === null) {
    return false;
  }
  return '@type' in obj && obj['@type'] === 'SimCodeModel';
}

export function getArrayItems<T>(arr: ArrayList<T>): Array<T> {
  return arr['@items'] ?? [];
}

export interface JavaHashMap<K, V> {
  '@type': 'java.util.HashMap';
  '@keys'?: Array<K>;
  '@items'?: Array<V>;
}
