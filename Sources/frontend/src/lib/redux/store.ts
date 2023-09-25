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
import { reducer as notificationsReducer } from 'reapop';
import {
  FLUSH,
  PAUSE,
  PERSIST,
  persistReducer,
  persistStore,
  PURGE,
  REGISTER,
  REHYDRATE,
} from 'redux-persist';
import hardSet from 'redux-persist/lib/stateReconciler/hardSet';
import storage from 'redux-persist/lib/storage';

import compilerReducer from '@/lib/redux/compilerSlice';
import isaReducer, { IsaReducer } from '@/lib/redux/isaSlice';
import modalsReducer from '@/lib/redux/modalSlice';
import shortcutsReducer from '@/lib/redux/shortcutsSlice';

// Persistance config
// https://blog.logrocket.com/persist-state-redux-persist-redux-toolkit-react/
// TODO: look at https://github.com/localForage/localForage
const persistIsaConfig = {
  key: 'root',
  version: 1,
  storage,
  stateReconciler: hardSet,
};

// https://stackoverflow.com/questions/69978434/persist-reducer-function-giving-type-error-to-my-reducer-in-typescript
const reducers = combineReducers({
  isa: persistReducer<IsaReducer>(persistIsaConfig, isaReducer),
  compiler: compilerReducer,
  notifications: notificationsReducer(),
  shortcuts: shortcutsReducer,
  modals: modalsReducer,
});

export const store = configureStore({
  reducer: reducers,
  middleware: (gDM) =>
    gDM({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    }),
});
export const persistor = persistStore(store);

// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>;
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch;
