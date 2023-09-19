import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { ReactNode } from 'react';

import AnimatedButton from '@/components/AnimatedButton';
import Tooltip from '@/components/Tooltip';

export type SideMenuButtonProps = {
  Icon: ReactNode;
  href: string;
  shortcut?: string;
  hoverText: string;
};

export default function SideMenuButton({
  Icon,
  href,
  shortcut,
  hoverText,
}: SideMenuButtonProps) {
  const router = useRouter();
  const path = usePathname();
  const isActive = path === href;

  // Navigate to href
  const clickCallback = () => {
    router.push(href);
  };

  return (
    <Link href={href} className='tooltip'>
      <AnimatedButton
        active={isActive}
        clickCallback={clickCallback}
        shortCut={shortcut}
      >
        {Icon}
      </AnimatedButton>
      <Tooltip text={hoverText} shortcut={shortcut} />
    </Link>
  );
}
