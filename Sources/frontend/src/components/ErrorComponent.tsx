import { useEffect } from 'react';
import { Button } from './base/ui/button';
import { clearLocalMemory } from '@/lib/utils';
import { Textarea } from './base/ui/textarea';
import { ExternalLink } from './ExternalLink';

const githubNewIssueLink = "https://github.com/Sekky61/riscv-sim/issues/new";

export default function ErrorComponent({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    // Log the error to an error reporting service
    console.error(error);
  }, [error]);

  return (
    <div>
      <h2>Something went wrong!</h2>
      <p>You may want to delete local data and reload the page.</p>
      <div className='flex gap-4 mb-12'>
        <Button
          onClick={
            // Attempt to recover by trying to re-render the segment
            () => reset()
          }
        >
          Reload Page
        </Button>
        <Button onClick={() => clearLocalMemory()}>Delete Local Data</Button>
      </div>
      <div>
        <p>Also, please consider <ExternalLink href={githubNewIssueLink} openInNewTab>reporting the issue on GitHub</ExternalLink>.</p>
        <p>Context of the error:</p>
        <p className="font-bold">{error.name}: {error.message}</p>
        <pre className="h-48 overflow-scroll">{error.stack}</pre>
      </div>
    </div>
  );
}
