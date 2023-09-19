import { ReactChildren } from '@/lib/reactTypes';

type ExpandBubbleProps = {
  children: ReactChildren;
};

/**
 * A bubble that expands to the right of the parent element.
 */
const ExpandBubble = ({ children }: ExpandBubbleProps) => {
  return (
    <div className='absolute left-full top-0 z-10'>
      <div className='dropdown-bubble ml-1'>{children}</div>
    </div>
  );
};

export default ExpandBubble;
