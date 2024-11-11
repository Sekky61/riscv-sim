/**
 * @file    env.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Environment variables
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

// This is a CommonJS module, not an ES6 module, because it is used in next.config.js
// It can, however, be used in the frontend code as well.

/**
 * True if the app is running in production mode.
 */
const isProd = process.env.NODE_ENV === 'production';

/**
 * True if the app is running in development mode.
 */
const isLocal = process.env.NODE_ENV === 'development';

/**
 * True if the logger should be shown.
 * TODO: use and document, along with improvements to logging
 */
const showLogger = isLocal
  ? true
  : process.env.NEXT_PUBLIC_SHOW_LOGGER === 'true' ?? false;

/**
 * The simulator API server address used by the client. For example `riscvsim.com`, `http://localhost:1234`, or just `/api/prefix`.
 * Is always defined - by default, it is empty, so that api calls go to the same host (`GET /api/something` - relative absolute path).
 */
const simApiExternalPrefix =
  process.env.EXTERNAL_SIM_API_PREFIX ?? 'http://localhost:8000';

/**
 * Prefix path of deployment
 */
const basePath = process.env.BASE_PATH ?? '';

/**
 * The simulator API server address used by the server. It may differ form the external based on deployment of the app.
 */
const simApiInternalPrefix =
  process.env.INTERNAL_SIM_API_PREFIX ??
  process.env.EXTERNAL_SIM_API_PREFIX ??
  'localhost:8000';

const env = {
  simApiExternalPrefix,
  simApiInternalPrefix,
  isLocal,
  isProd,
  showLogger,
  basePath,
};

module.exports = env;
