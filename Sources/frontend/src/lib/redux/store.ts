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
