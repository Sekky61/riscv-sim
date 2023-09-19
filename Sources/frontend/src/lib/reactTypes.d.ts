export type ReactChildren = React.ReactNode;
export type ReactClassName = Pick<
  React.HTMLAttributes<HTMLDivElement>,
  'className'
>;
export type EmptyObject = Record<string, never>;
