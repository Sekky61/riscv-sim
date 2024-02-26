/**
 * @file    store.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Redux store
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

import { combineReducers, configureStore } from '@reduxjs/toolkit';
import {
  FLUSH,
  PAUSE,
  PERSIST,
  PURGE,
  PersistedState,
  REGISTER,
  REHYDRATE,
  createMigrate,
  persistReducer,
  persistStore,
} from 'redux-persist';
import hardSet from 'redux-persist/lib/stateReconciler/hardSet';
import storage from 'redux-persist/lib/storage';

import compilerReducer, { CompilerReducer } from '@/lib/redux/compilerSlice';
import cpuReducer, { cpuInitialState } from '@/lib/redux/cpustateSlice';
import isaReducer, { IsaReducer } from '@/lib/redux/isaSlice';
import shortcutsReducer from '@/lib/redux/shortcutsSlice';
import simConfigReducer, { SimConfigReducer } from '@/lib/redux/simConfigSlice';

/**
 * This is the root of the global state.
 * It is a combination of all the reducers defined in this directory.
 *
 * The configuration is persisted in the local storage.
 * If the schema changes, the version number _must be increased_, otherwise the wrong data will be loaded and the app will not work.
 *
 * List of reducers:
 * - isaSlice - for ISA configuration
 * - compilerSlice - for compiler tab
 * - cpustateSlice - for the simulator tab
 * - modalSlice - for modals (invocation)
 * - shortcutsSlice - for keyboard shortcuts
 */

/**
 * Migrations for the redux-persist.
 * The migrations are used when the version number is increased.
 * Returning undefined will result in the state being discarded.
 * https://github.com/rt2zz/redux-persist/blob/HEAD/docs/migrations.md
 */
const migrations = {
  2: (state: PersistedState) => {
    // Changed MemoryLocation, compile
    return undefined;
  },
  3: (state: PersistedState) => {
    return undefined;
  },
  10: (state: PersistedState) => {
    return state;
  },
  11: (state: PersistedState) => {
    return undefined;
  },
  12: (state: PersistedState) => {
    // Added instructionFunctionModels
    return undefined;
  },
  13: (state: PersistedState) => {
    // Changed MemoryLocation
    return undefined;
  },
};

// Persistance config
// https://blog.logrocket.com/persist-state-redux-persist-redux-toolkit-react/
// TODO: look at https://github.com/localForage/localForage
const persistIsaConfig = {
  // The key in localStorage
  key: 'isa',
  // Change the version when changing the schema
  version: 14,
  storage,
  stateReconciler: hardSet,
  // This migration is used when the version number is increased
  migrate: createMigrate(migrations),
};

const persistSimConfig = {
  key: 'simConfig',
  version: 3,
  storage,
  stateReconciler: hardSet,
  migrate: createMigrate(migrations),
};

const persistCompileConfig = {
  key: 'compiler',
  version: 2,
  storage,
  stateReconciler: hardSet,
  migrate: createMigrate(migrations),
};

// https://stackoverflow.com/questions/69978434/persist-reducer-function-giving-type-error-to-my-reducer-in-typescript
const reducers = combineReducers({
  isa: persistReducer<IsaReducer>(persistIsaConfig, isaReducer),
  simConfig: persistReducer<SimConfigReducer>(
    persistSimConfig,
    simConfigReducer,
  ),
  compiler: persistReducer<CompilerReducer>(
    persistCompileConfig,
    compilerReducer,
  ),
  shortcuts: shortcutsReducer,
  cpu: cpuReducer,
});

/* export const store = configureStore({
  reducer: reducers,
  middleware: (gDM) =>
    gDM({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    }),
}); */

/**
 * Create the store with the preloaded state, persistor and the reducers.
 *
 * Note: What is defined in preloadedState stays after the state is updated.
 */
export const makeStore = () => {
  const store = configureStore({
    reducer: reducers,
    middleware: (gDM) =>
      gDM({
        serializableCheck: {
          ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
        },
      }),
  });
  const persistor = persistStore(store);
  return { store, persistor };
};

export type AppPersistedStore = Awaited<ReturnType<typeof makeStore>>;
export type AppStore = AppPersistedStore['store'];
// Infer the `RootState` and `AppDispatch` types from the store itself
// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<AppStore['getState']>;
export type AppDispatch = AppStore['dispatch'];
