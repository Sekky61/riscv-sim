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
const isProd = process.env.NODE_ENV === "production";

/**
 * True if the app is running in development mode.
 */
const isLocal = process.env.NODE_ENV === "development";

/**
 * True if the logger should be shown.
 */
const showLogger = isLocal
	? true
	: process.env.NEXT_PUBLIC_SHOW_LOGGER === "true" ?? false;

/**
 * The host of the API server. Is always defined,
 */
const apiServerHost =
	process.env.NEXT_PUBLIC_SIMSERVER_HOST ?? "localhost";

/**
 * The port of the actual Java API server.
 * Is always defined.
 */
const apiServerPort = process.env.NEXT_PUBLIC_SIMSERVER_PORT ?? 8000;

/**
 * This is the base URL of the API server. It should not be used directly, this is only for debugging and logging.
 */
const apiBaseUrl = `http://${apiServerHost}:${apiServerPort}`;

module.exports = {
	apiBaseUrl,
	apiServerHost,
	apiServerPort,
	isLocal,
	isProd,
	showLogger,
};
