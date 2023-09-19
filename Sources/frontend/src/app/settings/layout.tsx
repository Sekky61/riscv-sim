import CenterLayout from '@/components/CenterLayout';

export default function Layout({ children }: { children: React.ReactNode }) {
  return <CenterLayout>{children}</CenterLayout>;
}
