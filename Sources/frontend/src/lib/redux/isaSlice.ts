import { createSelector, createSlice, PayloadAction } from '@reduxjs/toolkit';

// Import as type to avoid circular dependency
import type { RootState } from '@/lib/redux/store';

import { isaFormDefaultValues, IsaNamedConfig, isaSchema } from '../forms/Isa';

// Define a type for the slice state
interface IsaState {
  isas: Array<IsaNamedConfig>;
  activeIsaName: string;
}

export type IsaSaveChecked = IsaNamedConfig & {
  valid: boolean;
};

// Define the initial state using that type
const initialState: IsaState = {
  isas: [isaFormDefaultValues],
  activeIsaName: 'Default',
};

function findIsaByName(
  isas: Array<IsaNamedConfig>,
  name: string,
): IsaNamedConfig | undefined {
  return isas.find((isa) => isa.name == name);
}

export const isaSlice = createSlice({
  name: 'isaSl',
  // `createSlice` will infer the state type from the `initialState` argument
  initialState,
  reducers: {
    newActiveIsa: (state, action: PayloadAction<string>) => {
      if (findIsaByName(state.isas, action.payload) === undefined)
        throw new Error('ISA not found');
      state.activeIsaName = action.payload;
    },
    // Create a new ISA, make it active
    createIsa: (state, action: PayloadAction<IsaNamedConfig>) => {
      if (findIsaByName(state.isas, action.payload.name) !== undefined)
        throw new Error('ISA already exists');
      state.isas.push(action.payload);
      state.activeIsaName = action.payload.name;
    },
    updateIsa: (
      state,
      action: PayloadAction<{ oldName: string; isa: IsaNamedConfig }>,
    ) => {
      if (action.payload.oldName == 'Default') {
        throw new Error('Cannot edit the default ISA');
      }
      // Update the ISA
      state.isas = state.isas.map((isa) => {
        if (isa.name == action.payload.oldName) {
          // Found the ISA to update
          // If it is active, rename the active ISA field
          if (state.activeIsaName == action.payload.oldName) {
            state.activeIsaName = action.payload.isa.name;
          }
          return action.payload.isa;
        }
        return isa;
      });
    },
    removeIsa: (state, action: PayloadAction<string>) => {
      // Do not allow to remove the first ISA (called "Default")
      if (action.payload == 'Default') {
        throw new Error('Cannot remove the default ISA');
      }
      state.isas = state.isas.filter((isa) => isa.name != action.payload);
      // If the active ISA was removed, make the first one active
      if (state.activeIsaName == action.payload) {
        state.activeIsaName = state.isas[0].name;
      }
    },
  },
});
export type IsaReducer = ReturnType<typeof isaSlice.reducer>;

export const { newActiveIsa, createIsa, updateIsa, removeIsa } =
  isaSlice.actions;

export const selectActiveIsaName = (state: RootState) =>
  state.isa.activeIsaName;
export const selectIsas = (state: RootState) => state.isa.isas;

export const selectActiveIsa = createSelector(
  [selectIsas, selectActiveIsaName],
  (isas, name) => {
    const isa = isas.find((isa) => isa.name == name);
    if (isa == undefined) throw new Error('Active ISA not found');
    return isa;
  },
);

export const selectValidatedIsas = createSelector([selectIsas], (isas) => {
  const checkedIsas: Array<IsaSaveChecked> = [];
  for (const isaSave of isas) {
    // Use zod to validate the isa config
    const parseErrors = isaSchema.safeParse(isaSave);

    checkedIsas.push({ ...isaSave, valid: parseErrors.success });
  }
  return checkedIsas;
});

export default isaSlice.reducer;
