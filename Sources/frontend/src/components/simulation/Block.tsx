// Universal block component - used for all blocks in the simulation
// Provides design of the box, content is substituted in

import clsx from 'clsx';
import { MoreVertical } from 'lucide-react';

import { ReactChildren, ReactClassName } from '@/lib/reactTypes';
import { useAppDispatch } from '@/lib/redux/hooks';
import { openModal } from '@/lib/redux/modalSlice';

export type BlockProps = {
  children: ReactChildren;
  className?: ReactClassName;
  title: string;
};

export default function Block({ children, className, title }: BlockProps) {
  const dispatch = useAppDispatch();
  const classes = clsx(className, 'w-[200px] rounded border bg-white p-2');

  const handleMore = () => {
    console.log('More');
    dispatch(
      openModal({
        modalType: 'ROB_DETAILS_MODAL',
        modalProps: {},
      }),
    );
  };

  return (
    <div className={classes}>
      <div className='flex justify-between'>
        <span>{title}</span>
        <button
          onClick={handleMore}
          className='iconHighlight h-6 w-6 rounded-full'
        >
          <MoreVertical strokeWidth={1.5} />
        </button>
      </div>
      {children}
    </div>
  );
}
