/**
 * @file    ApiResult.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Result type of WASM API call
 *
 * @date    23 March 2025, 19:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2025  Michal Majer
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

/**
 * The serialized response has this type.
 * WASM call actually returns a pointer to a pair [pointer, length].
 * This pair points at utf-8 serialized JSON that has this format
 * The values are generated in `Sources/simulator_zig/src/wasm/glue.zig`
 */
export type ApiResult<Res> = ApiResultError | ApiResultResponse<Res>;

type ApiResultError = {
  type: 'error';
  message: string;
};
type ApiResultResponse<Res> = {
  type: 'response';
  data: Res;
};

export function isResponse<Res>(
  result: ApiResult<Res>,
): result is ApiResultResponse<Res> {
  return result.type === 'response';
}

export function isError<Res>(result: ApiResult<Res>): result is ApiResultError {
  return result.type === 'error';
}
