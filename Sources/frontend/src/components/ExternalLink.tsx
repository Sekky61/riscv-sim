export interface CenterLayoutProps {
  href: string;
  openInNewTab?: boolean;
  children: React.ReactNode;
}

export default function ExternalLink({
  href,
  children,
  openInNewTab = false,
}: CenterLayoutProps) {
  return (
    <a
      href={href}
      target={openInNewTab ? '_blank' : '_self'}
      className='inline-flex items-center font-medium text-blue-600 hover:cursor-pointer hover:underline'
    >
      {children}
      <svg
        className='ml-2 h-4 w-4'
        aria-hidden='true'
        xmlns='http://www.w3.org/2000/svg'
        fill='none'
        viewBox='0 0 14 10'
      >
        <path
          stroke='currentColor'
          strokeLinecap='round'
          strokeLinejoin='round'
          strokeWidth='2'
          d='M1 5h12m0 0L9 1m4 4L9 9'
        />
      </svg>
    </a>
  );
}
