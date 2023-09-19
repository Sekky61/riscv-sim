// Renders a svg line

export type LineProps = {
  length: number;
};

// https://thenewcode.com/1068/Making-Arrows-in-SVG

export default function Line({ length }: LineProps) {
  return (
    <svg
      viewBox={`0 0 ${length} 100`}
      xmlns='http://www.w3.org/2000/svg'
      style={{ width: `${length}px`, height: '100px' }}
    >
      <line x1='0' y1='0' x2={length} y2='0' className='schemaLine' />
    </svg>
  );
}
