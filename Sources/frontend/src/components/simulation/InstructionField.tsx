import { Instruction } from '@/lib/redux/compilerSlice';

export type InstructionFieldProps = {
  instruction?: Instruction;
};

export default function InstructionField({
  instruction,
}: InstructionFieldProps) {
  if (!instruction) {
    return (
      <div className='w-full rounded-sm border p-0.5'>
        <span className='text-gray-400'>empty</span>
      </div>
    );
  }
  return (
    <div className='w-full rounded-sm border p-0.5'>{instruction.mnemonic}</div>
  );
}
