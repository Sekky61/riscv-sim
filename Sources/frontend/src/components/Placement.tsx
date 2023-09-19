// Place component on a canvas

export type PlacementProps = {
  children?: React.ReactNode;
  x: number;
  y: number;
};

export default function Placement({ children, x = 0, y = 0 }: PlacementProps) {
  return (
    <div className='absolute' style={{ left: `${x}px`, top: `${y}px` }}>
      {children}
    </div>
  );
}
