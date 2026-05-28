import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

interface DangerZoneProps {
    title?: string;
    description: string;
    buttonText: string;
    onAction: () => void;
}

export default function DangerZone({ 
    title = "Danger Zone", 
    description, 
    buttonText, 
    onAction 
}: DangerZoneProps) {
    return (
        <Card className="border-red-200 dark:border-red-900/50 bg-red-50/50 dark:bg-red-950/10">
            <CardHeader className="pb-3">
                <CardTitle className="text-sm font-bold text-red-600 uppercase tracking-wider">
                    {title}
                </CardTitle>
                <CardDescription className="text-red-800/80 dark:text-red-200/70">
                    {description}
                </CardDescription>
            </CardHeader>
            <CardContent>
                <Button variant="destructive" size="sm" className="w-full sm:w-auto font-semibold shadow-sm" onClick={onAction}>
                    {buttonText}
                </Button>
            </CardContent>
        </Card>
    );
}