/* eslint-disable @typescript-eslint/ban-ts-comment */
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

import type { RootState } from '@/lib/redux/store';

type ShortcutsState = {
  scopes: string | string[];
};

// Define the initial state using that type
const initialState: ShortcutsState = {
  scopes: '*',
};

export const shortcutsSlice = createSlice({
  name: 'shortcuts',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    // Use the PayloadAction type to declare the contents of `action.payload`
    setScopes: (state, action: PayloadAction<string | string[]>) => {
      state.scopes = action.payload;
    },
  },
});

export const { setScopes } = shortcutsSlice.actions;

// Selectors
export const selectScopes = (state: RootState) => state.shortcuts.scopes;

export default shortcutsSlice.reducer;
