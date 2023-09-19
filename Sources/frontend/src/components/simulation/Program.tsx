import { parsedInstructions } from '@/lib/redux/compilerSlice';
import { useAppSelector } from '@/lib/redux/hooks';

import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';

export default function Program() {
  const instructions = useAppSelector(parsedInstructions);

  return (
    <Block title='Program'>
      <div className='flex h-[600px] flex-col gap-1 overflow-y-scroll'>
        {instructions.map((instruction) => {
          return (
            <InstructionField instruction={instruction} key={instruction.id} />
          );
        })}
      </div>
    </Block>
  );
}
