import { Head, useForm, usePage, router, Link } from '@inertiajs/react';
import React from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import Heading from '@/components/heading';
import InputError from '@/components/input-error';
import { Breadcrumbs } from '@/components/breadcrumbs';

type Command = {
    id: number;
    uuid: string;
    name: string;
    is_public: boolean;
    permissions: string;
    expires_at: string | null;
};

type Device = {
    uuid: string;
    user_id: number;
    name: string;
    last_seen_at: string;
    created_at: string;
    updated_at: string;
    commands: Command[];
};

type Props = {
    device: Device;
};

Show.layout = (page: React.ReactNode) => ({
    props: {
        breadcrumbs: [
            {
                title: 'Devices',
                href: '/devices',
            },
        ],
    },
    children: page,
});

export default function Show({ device }: Props) {
    const { flash } = usePage().props as any;

    const { data, setData, patch, processing, errors } = useForm({
        name: device.name,
    });

    // 2. Dynamic Inline Breadcrumbs (Page Level)
    const breadcrumbs = [
        { title: 'Devices', href: '/devices' },
        { title: device.name, href: `/devices/${device.uuid}` },
    ];

    const submit = (e: React.FormEvent) => {
        e.preventDefault();
        patch(`/devices/${device.uuid}`, {
            preserveScroll: true,
        });
    };

    const destroy = () => {
        if (!confirm('Are you sure you want to delete this device? This action cannot be undone and will sever all associated access links.')) return;
        router.delete(`/devices/${device.uuid}`);
    };

    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-6xl">
            <Head title={`Device - ${device.name}`} />

            <Breadcrumbs breadcrumbs={breadcrumbs} />

            <Heading title={device.name} description="Manage settings and view associated access sessions for this device." />

            {flash?.success && (
                <div className="rounded border border-green-200 bg-green-50 p-3 text-sm text-green-700 dark:bg-green-900/20 dark:text-green-400 dark:border-green-800">
                    {flash.success}
                </div>
            )}

            <div className="grid gap-6 md:grid-cols-3">
                {/* Sidebar Info */}
                <div className="space-y-6 md:col-span-1">
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">Device Status</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4 text-sm">
                            <div>
                                <Label className="text-muted-foreground text-xs uppercase tracking-wider">Device ID</Label>
                                <p className="font-mono text-xs break-all mt-1 bg-muted p-2 rounded">{device.uuid}</p>
                            </div>
                            <Separator />
                            <div>
                                <Label className="text-muted-foreground text-xs uppercase tracking-wider">Connection</Label>
                                <div className="mt-1 flex items-center gap-2">
                                    <span className="h-2 w-2 rounded-full bg-green-500 animate-pulse" />
                                    <Badge variant="secondary" className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">Online</Badge>
                                </div>
                            </div>
                            <Separator />
                            <div>
                                <Label className="text-muted-foreground text-xs uppercase tracking-wider">Registered</Label>
                                <p className="mt-1">{new Date(device.created_at).toLocaleString()}</p>
                            </div>
                        </CardContent>
                    </Card>
                </div>

                {/* Main Content */}
                <div className="space-y-6 md:col-span-2">
                    {/* General Settings */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Rename Device</CardTitle>
                            <CardDescription>Give this device a recognizable name for your dashboard.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={submit} className="space-y-4 max-w-md">
                                <div className="space-y-2">
                                    <Label htmlFor="name">Friendly Name</Label>
                                    <Input
                                        id="name"
                                        type="text"
                                        value={data.name}
                                        onChange={(e) => setData('name', e.target.value)}
                                    />
                                    <InputError message={errors.name} />
                                </div>

                                <Button type="submit" disabled={processing}>
                                    {processing ? 'Saving...' : 'Save Changes'}
                                </Button>
                            </form>
                        </CardContent>
                    </Card>

                    {/* Access Links / Sessions */}
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between">
                            <div className="space-y-1">
                                <CardTitle>Active Access Links</CardTitle>
                                <CardDescription>Manage keys that allow remote control of this device.</CardDescription>
                            </div>
                            <Button variant="outline" size="sm" asChild>
                                <Link href="/commands/create">+ New Link</Link>
                            </Button>
                        </CardHeader>
                        <CardContent>
                            {device.commands.length > 0 ? (
                                <ul className="divide-y divide-border rounded-md border">
                                    {device.commands.map((command) => {
                                        const isExpired = command.expires_at ? new Date(command.expires_at) < new Date() : false;
                                        return (
                                            <li key={command.uuid} className="flex items-center justify-between p-4 hover:bg-muted/50 transition-colors">
                                                <div className="flex flex-col gap-1">
                                                    <span className="text-sm font-semibold">{command.name}</span>
                                                    <div className="flex items-center gap-2">
                                                        <Badge variant={isExpired ? "destructive" : "outline"} className="text-[10px] h-5">
                                                            {isExpired ? 'Expired' : (command.is_public ? 'Public' : 'Private')}
                                                        </Badge>
                                                        <span className="text-[11px] text-muted-foreground font-mono">
                                                            {command.uuid.split('-')[0]}...
                                                        </span>
                                                    </div>
                                                </div>
                                                <Button variant="ghost" size="sm" asChild>
                                                    <Link href={`/commands/${command.uuid}`}>Manage Link</Link>
                                                </Button>
                                            </li>
                                        );
                                    })}
                                </ul>
                            ) : (
                                <div className="rounded-md border border-dashed p-8 text-center">
                                    <p className="text-sm text-muted-foreground">No access links have been created for this device yet.</p>
                                    <Button variant="link" asChild className="mt-2">
                                        <Link href="/commands/create">Create your first access link</Link>
                                    </Button>
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    {/* Danger Zone */}
                    <Card className="border-red-200 dark:border-red-900/50 bg-red-50/50 dark:bg-red-950/10">
                        <CardHeader>
                            <CardTitle className="text-red-600 dark:text-red-500">Danger Zone</CardTitle>
                            <CardDescription>Removing this device will invalidate all active sessions and stop all remote tracking.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <Button variant="destructive" onClick={destroy}>
                                Delete Device Permanently
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}