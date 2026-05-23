import { Head, Link } from '@inertiajs/react';
import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';

type Device = {
    uuid: string;
    name: string;
};

type Command = {
    id: number;
    device_id: number;
    uuid: string;
    name: string;
    is_public: boolean;
    permissions: string;
    expires_at: string | null;
    device: Device;
};

type Props = {
    commands: Command[];
};

const breadcrumbs = [
    { title: 'Commands', href: '/commands' },
];

Index.layout = {
    breadcrumbs: [
        { title: 'Commands', href: '/commands' },
    ],
};

export default function Index({ commands }: Props) {
    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8">
            <Head title="Access Links" />

            <Breadcrumbs breadcrumbs={breadcrumbs} />

            <div className="flex items-center justify-between">
                <Heading title="Access Links" description="Manage shared sessions and remote commands for your devices." />
                <Button asChild>
                    <Link href="/commands/create">+ Create Link</Link>
                </Button>
            </div>

            {commands.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-lg border border-dashed p-12 text-center">
                    <p className="text-muted-foreground">No active access links found.</p>
                </div>
            ) : (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {commands.map((command) => {
                        const isExpired = command.expires_at ? new Date(command.expires_at) < new Date() : false;
                        
                        return (
                            <Card key={command.uuid} className="flex flex-col">
                                <CardHeader className="pb-2">
                                    <div className="flex justify-between items-start">
                                        <CardTitle className="text-base font-semibold truncate pr-2">
                                            {command.name}
                                        </CardTitle>
                                        <Badge variant={command.is_public ? (isExpired ? "destructive" : "default") : "secondary"}>
                                            {isExpired ? 'Expired' : (command.is_public ? 'Public' : 'Private')}
                                        </Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground font-mono truncate">{command.uuid}</p>
                                </CardHeader>
                                <CardContent className="flex-1 space-y-1 text-sm text-muted-foreground mt-2">
                                    <p><span className="font-medium text-foreground">Target Device:</span> {command.device.name}</p>
                                    <p><span className="font-medium text-foreground">Permissions:</span> <span className="capitalize">{command.permissions}</span></p>
                                </CardContent>
                                
                                <CardFooter>
                                    <Button variant="outline" asChild className="w-full">
                                        <Link href={`/commands/${command.uuid}`}>Manage Settings</Link>
                                    </Button>
                                </CardFooter>
                            </Card>
                        );
                    })}
                </div>
            )}
        </div>
    );
}