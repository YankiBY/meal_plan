import type { InputHTMLAttributes } from 'react';
import { cn } from './cn';

export default function Input({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      {...props}
      className={cn(
        'h-10 w-full rounded-md border border-gray-200 bg-white px-3 text-sm text-gray-900',
        'placeholder:text-gray-400',
        'dark:border-slate-800 dark:bg-slate-900 dark:text-slate-100 dark:placeholder:text-slate-500',
        'focus:outline-none focus:ring-2 focus:ring-olive focus:border-olive',
        className,
      )}
    />
  );
}

