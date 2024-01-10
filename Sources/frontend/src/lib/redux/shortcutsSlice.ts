/**
 * @file    shortcutsSlice.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Redux state for shortcuts
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

/* eslint-disable @typescript-eslint/ban-ts-comment */
import { PayloadAction, createSlice } from '@reduxjs/toolkit';

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
