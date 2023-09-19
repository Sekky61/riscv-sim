import {
  BarChart3,
  BookOpen,
  BrainCircuit,
  Code,
  Cpu,
  Info,
  Settings,
} from 'lucide-react';

import SideMenuButton from '@/components/SideMenuButton';

export default function SideBar() {
  return (
    <div className='flex flex-col justify-between border-r border-r-black p-1.5'>
      <div className='flex flex-col gap-3'>
        <SideMenuButton
          Icon={<BrainCircuit strokeWidth={1.5} />}
          href='/'
          hoverText='Simulation'
          shortcut='Digit1'
        />
        <SideMenuButton
          Icon={<Code strokeWidth={1.5} />}
          href='/compiler'
          hoverText='Code editor'
          shortcut='Digit2'
        />
        <SideMenuButton
          Icon={<Cpu strokeWidth={1.5} />}
          href='/isa'
          hoverText='Architecture settings'
          shortcut='Digit3'
        />
        <SideMenuButton
          Icon={<BarChart3 strokeWidth={1.5} />}
          href='/stats'
          hoverText='Runtime statistics'
          shortcut='Digit4'
        />
      </div>
      <div className='flex flex-col gap-3'>
        <SideMenuButton
          Icon={<BookOpen strokeWidth={1.5} />}
          href='/docs'
          hoverText='RISC-V documentation'
          shortcut='Digit5'
        />
        <SideMenuButton
          Icon={<Settings strokeWidth={1.5} />}
          href='/settings'
          hoverText='App settings'
          shortcut='Digit6'
        />
        <SideMenuButton
          Icon={<Info strokeWidth={1.5} />}
          href='/help'
          hoverText='About'
          shortcut='Digit7'
        />
      </div>
    </div>
  );
}
