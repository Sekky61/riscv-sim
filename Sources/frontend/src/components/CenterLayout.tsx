export default function CenterLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className='small-container box-border h-full px-4 py-10'>
      <div className='h-full'>{children}</div>
    </div>
  );
}
