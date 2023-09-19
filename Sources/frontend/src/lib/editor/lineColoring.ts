// array of tailwindcss classes of colors to highlight blocks
const codeColors = [
  'code_highlight_red',
  'code_highlight_green',
  'code_highlight_yellow',
  'code_highlight_blue',
  'code_highlight_orange',
  'code_highlight_pink',
];

export function cLineToColor(cLine: number) {
  return codeColors[cLine % codeColors.length];
}
