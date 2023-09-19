import { ArrowBigLeft, ArrowBigRight, SkipForward } from 'lucide-react';

import AnimatedButton from '@/components/AnimatedButton';

export type TimelineProps = Pick<
  React.HTMLAttributes<HTMLDivElement>,
  'className'
>;

// Control ticks of simulation
// Go forward, back, finish
export default function Timeline({ className = '' }: TimelineProps) {
  return (
    <div
      className={
        className +
        ' flex gap-2 rounded-full border bg-gray-100 p-1 drop-shadow'
      }
    >
      <AnimatedButton shortCut='left'>
        <ArrowBigLeft strokeWidth={1.5} />
      </AnimatedButton>
      <AnimatedButton shortCut='right'>
        <ArrowBigRight strokeWidth={1.5} />
      </AnimatedButton>
      <AnimatedButton shortCut='ctrl+enter'>
        <SkipForward />
      </AnimatedButton>
    </div>
  );
}
