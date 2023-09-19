import { ChevronRight } from 'lucide-react';
import { useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import _examples from 'src/constant/codeExamples.json';

import {
  callCompiler,
  openExample,
  selectAsmManuallyEdited,
} from '@/lib/redux/compilerSlice';
import {
  enterEditorMode,
  selectEditorMode,
  selectOptimize,
  setOptimize,
} from '@/lib/redux/compilerSlice';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

import ExpandBubble from '@/components/ExpandBubble';
import Tooltip from '@/components/Tooltip';

import { CodeExample } from '@/constant/codeExamples';

import { RadioInput } from '../form/RadioInput';

const examples = _examples as unknown as Array<CodeExample>;

export default function CompileOptions() {
  const dispatch = useAppDispatch();
  const optimize = useAppSelector(selectOptimize);
  const mode = useAppSelector(selectEditorMode);
  const asmEdited = useAppSelector(selectAsmManuallyEdited);

  function handleCompile() {
    // Show warning if the user is about to lose data
    if (asmEdited) {
      const res = confirm(
        'You have manually edited the assembly code. Are you sure you want to compile?',
      );
      if (!res) {
        return;
      }
    }
    dispatch(callCompiler());
  }

  const editorModeChanged = (newVal: string) => {
    if (newVal != 'c' && newVal != 'asm') {
      console.error(`Unknown mode '${newVal}' while changing editor mode`);
      return;
    }
    dispatch(enterEditorMode(newVal));
  };

  // TODO: bug: The first render does not show the mode selected
  return (
    <div className='flex flex-col items-stretch gap-4'>
      <RadioInput
        choices={['c', 'asm']}
        texts={['C', 'ASM']}
        value={mode}
        onNewValue={editorModeChanged}
      />
      <ExamplesButton />
      <div className='rounded border flex flex-col'>
        <div className='ml-2 mt-1'>
          <input type='checkbox' name='' id='floats' />
          <label htmlFor='floats' className='ml-2 text-sm'>
            Float instructions
          </label>
        </div>
        <div className='ml-2 mb-1'>
          <input
            id='optimizeCheckbox'
            type='checkbox'
            checked={optimize}
            onChange={() => dispatch(setOptimize(!optimize))}
          />
          <label htmlFor='optimizeCheckbox' className='ml-2 text-sm'>
            Optimize
          </label>
        </div>
        <CompileButton handleCompile={handleCompile} />
      </div>
      <button className='button'>Load ASM to simulator</button>
    </div>
  );
}

function ExamplesButton() {
  const dispatch = useAppDispatch();
  const [showExamples, setShowExamples] = useState(false);

  return (
    <div
      onMouseEnter={() => setShowExamples(true)}
      onMouseLeave={() => setShowExamples(false)}
      className='relative button-shape text-center button-interactions rounded border'
    >
      <span>Load example</span>
      <div className='absolute inset-0 flex items-center justify-end pr-1'>
        <ChevronRight
          strokeWidth={1.5}
          className={'duration-100 ' + (showExamples ? 'rotate-180' : '')}
        />
      </div>
      {showExamples && (
        <ExpandBubble>
          {examples.map((example) => {
            // Wrapped in two divs, to not lose hover when the mouse moves over
            return (
              <button
                key={example.name}
                className='w-full p-1 hover:bg-gray-100 flex items-center justify-between'
                onClick={() => {
                  dispatch(openExample(example));
                }}
              >
                <div className=''>{example.name}</div>
                <div className='w-8 rounded px-0.5 mr-1 bg-amber-300 text-[#694848] text-xs'>
                  {example.type}
                </div>
              </button>
            );
          })}
        </ExpandBubble>
      )}
    </div>
  );
}

const COMPILE_SHORTCUT = 'ctrl+enter';

function CompileButton({ handleCompile }: { handleCompile: () => void }) {
  const compileStatus = useAppSelector((state) => state.compiler.compileStatus);
  const statusStyle = statusToClass(compileStatus);

  useHotkeys(
    COMPILE_SHORTCUT,
    () => {
      // TODO: Show warning if the user is about to lose data
      // https://www.reddit.com/r/reactjs/comments/5xnien/reactredux_and_displaying_a_confirmation_dialog/
      handleCompile();
    },
    undefined,
    [handleCompile],
  );

  return (
    <button
      className={
        'button-interactions border-t button-shape tooltip ' + statusStyle
      }
      onClick={handleCompile}
    >
      Compile
      <Tooltip text='Compile' shortcut={COMPILE_SHORTCUT} />
    </button>
  );
}

function statusToClass(status: 'idle' | 'success' | 'loading' | 'failed') {
  switch (status) {
    case 'idle':
      return 'ring-gray-200';
    case 'loading':
      return 'ring-yellow-200';
    case 'success':
      return 'ring-green-200';
    case 'failed':
      return 'ring-red-200';
    default:
      throw new Error(`Unknown status '${status}'`);
  }
}
