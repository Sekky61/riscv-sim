// array of tailwindcss classes of colors to highlight blocks
const code_colors = [
  'code_highlight_red',
  'code_highlight_green',
  'code_highlight_yellow',
  'code_highlight_blue',
  'code_highlight_orange',
  'code_highlight_pink',
];

export function c_line_to_color(c_line: number) {
  return code_colors[c_line % code_colors.length];
}
