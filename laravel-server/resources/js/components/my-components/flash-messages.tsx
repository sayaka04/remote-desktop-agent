import React from 'react';
import { usePage } from '@inertiajs/react';
import { CheckCircle2, AlertCircle } from 'lucide-react';

export default function FlashMessages() {
    const { flash } = usePage().props as any;

    if (!flash?.success && !flash?.error) return null;

    return (
        <div className="flex flex-col gap-2 w-full">
            {flash?.success && (
                <div className="flex items-center gap-2 rounded-md bg-green-100 p-3 text-sm text-green-800 dark:bg-green-900/40 dark:text-green-300 border border-green-200 dark:border-green-800/60 shadow-sm animate-in fade-in slide-in-from-top-2">
                    <CheckCircle2 className="h-4 w-4 shrink-0" />
                    <p>{flash.success}</p>
                </div>
            )}
            {flash?.error && (
                <div className="flex items-center gap-2 rounded-md bg-red-100 p-3 text-sm text-red-800 dark:bg-red-900/40 dark:text-red-300 border border-red-200 dark:border-red-800/60 shadow-sm animate-in fade-in slide-in-from-top-2">
                    <AlertCircle className="h-4 w-4 shrink-0" />
                    <p>{flash.error}</p>
                </div>
            )}
        </div>
    );
}