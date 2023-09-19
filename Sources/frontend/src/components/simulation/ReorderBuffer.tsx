import Block from '@/components/simulation/Block';
import InstructionField from '@/components/simulation/InstructionField';

export default function ReorderBuffer() {
  const capacity = 128;
  const used = 2;

  return (
    <Block title='Reorder Buffer'>
      <div>
        <span>
          {used}/{capacity}
        </span>
      </div>
      <div className='flex flex-col gap-1'>
        <InstructionField
          instruction={{ mnemonic: 'add', args: ['x0', 'x1', '5'], id: 5 }}
        />
        <InstructionField
          instruction={{ mnemonic: 'add', args: ['x4', 'x4', 'x4'], id: 6 }}
        />
        <InstructionField />
        <InstructionField />
      </div>
    </Block>
  );
}
