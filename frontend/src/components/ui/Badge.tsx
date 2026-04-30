import type { HTMLAttributes } from 'react';
import { cn } from './cn';

export default function Badge({
  className,
  variant = 'neutral',
  ...props
}: HTMLAttributes<HTMLSpanElement> & {
  variant?: 'neutral' | 'success' | 'warning' | 'danger' | 'info';
}) {
  const v =
    variant === 'success'
      ? 'bg-lime/30 text-olive-dark ring-1 ring-olive/20'
      : variant === 'warning'
        ? 'bg-amber/25 text-amber-dark ring-1 ring-amber/30'
        : variant === 'danger'
          ? 'bg-coral-light text-coral-dark ring-1 ring-coral/25'
          : variant === 'info'
            ? 'bg-blue-50 text-blue-700 ring-1 ring-blue-200'
            : 'bg-gray-50 text-gray-700 ring-1 ring-gray-200 dark:bg-slate-800 dark:text-slate-200 dark:ring-slate-700';

  return (
    <span
      {...props}
      className={cn('inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium', v, className)}
    />
  );
}

