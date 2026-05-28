import { Head, Link } from '@inertiajs/react';
import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';
import FlashMessages from '@/components/my-components/flash-messages';

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

export default function Index({ commands }: Props) {
    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
            <Head title="Access Links" />

            <Breadcrumbs breadcrumbs={breadcrumbs} />

            <FlashMessages />

            <div className="flex items-center justify-between">
                <Heading title="Access Links" description="Manage shared sessions and remote commands for your devices." />
                <Button asChild>
                    <Link href="/commands/create">+ Create Link</Link>
                </Button>
            </div>

            {commands.length === 0 ? (
                <div className="flex flex-1 items-center justify-center rounded-lg border border-dashed shadow-sm p-8">
                    <p className="text-sm text-muted-foreground">No access links have been created.</p>
                </div>
            ) : (
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {commands.map((command) => {
                        const isExpired = command.expires_at ? new Date(command.expires_at) < new Date() : false;

                        return (
                            <Card key={command.uuid} className="flex flex-col">
                                <CardHeader className="flex flex-col space-y-1 pb-2">
                                    <div className="flex items-start justify-between">
                                        <CardTitle className="text-lg font-bold line-clamp-1" title={command.name}>
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