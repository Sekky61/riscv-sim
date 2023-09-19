export type TooltipProps = {
  text: string;
  shortcut?: string;
};

export default function Tooltip({ text, shortcut }: TooltipProps) {
  return (
    <div className='tooltiptext show-tooltip-right rounded bg-black px-2 py-1 text-white duration-100 flex items-center gap-4'>
      <div>{text}</div>
      {shortcut && <div className='text-xs text-gray-100'>({shortcut})</div>}
    </div>
  );
}
