import { SVG } from '@svgdotjs/svg.js';

export function renderLine(color: string) {
  const draw = SVG().size(300, 300);
  draw.line(0, 0, 100, 100).stroke({ width: 1, color });
  return draw;
}
