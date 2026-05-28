import { Head, useForm, router, Link } from '@inertiajs/react';
import React from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import InputError from '@/components/input-error';
import { Breadcrumbs } from '@/components/breadcrumbs';
import DangerZone from '@/components/my-components/danger-zone';
import FlashMessages from '@/components/my-components/flash-messages';

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

export default function Show({ device }: Props) {
    const { data, setData, patch, processing, errors } = useForm({
        name: device.name,
    });

    const submit = (e: React.FormEvent) => {
        e.preventDefault();
        patch(`/devices/${device.uuid}`);
    };

    const destroy = () => {
        if (confirm('Are you sure you want to delete this device? This cannot be undone.')) {
            router.delete(`/devices/${device.uuid}`);
        }
    };

    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
            <Head title={`Manage Device: ${device.name}`} />

            <Breadcrumbs breadcrumbs={[
                { title: 'Devices', href: '/devices' },
                { title: device.name, href: `/devices/${device.uuid}` },
            ]} />

            <FlashMessages />

            <Heading title="Manage Device" description="Update device settings or view associated access links." />

            <div className="grid gap-6 md:grid-cols-3 lg:grid-cols-4 mt-2">
                <div className="md:col-span-2 lg:col-span-3 space-y-6">
                    {/* Settings Form */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Device Settings</CardTitle>
                            <CardDescription>Update the display name of your remote device.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={submit} className="space-y-4">
                                <div className="space-y-2">
                                    <Label htmlFor="name">Device Name</Label>
                                    <Input
                                        id="name"
                                        value={data.name}
                                        onChange={(e) => setData('name', e.target.value)}
                                        required
                                    />
                                    <InputError message={errors.name} />
                                </div>
                                <div className="pt-4 flex justify-end">
                                    <Button type="submit" disabled={processing}>Save Changes</Button>
                                </div>
                            </form>
                        </CardContent>
                    </Card>
                    
                    {/* Active Links */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Access Links</CardTitle>
                            <CardDescription>Remote sessions attached to this device.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            {device.commands && device.commands.length > 0 ? (
                                <ul className="space-y-3">
                                    {device.commands.map(cmd => (
                                        <li key={cmd.id} className="flex justify-between items-center p-3 rounded-lg border bg-muted/30">
                                            <div>
                                                <Link href={`/commands/${cmd.uuid}`} className="font-semibold text-primary hover:underline block">
                                                    {cmd.name}
                                                </Link>
                                                <span className="text-xs text-muted-foreground font-mono mt-1">{cmd.uuid}</span>
                                            </div>
                                            <Badge variant={cmd.is_public ? "default" : "secondary"}>
                                                {cmd.is_public ? 'Public' : 'Private'}
                                            </Badge>
                                        </li>
                                    ))}
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
                </div>

                {/* Sidebar */}
                <div className="space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-sm">Status Overview</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4 text-sm">
                            <div className="flex justify-between items-center">
                                <span className="text-muted-foreground">Status</span>
                                <Badge variant="secondary" className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
                                    Active
                                </Badge>
                            </div>
                            <div className="flex justify-between items-center pt-1 border-t">
                                <span className="text-muted-foreground">Last Seen</span>
                                <span className="font-medium">{device.last_seen_at || 'Never'}</span>
                            </div>
                            <div className="flex justify-between items-center pt-1 border-t">
                                <span className="text-muted-foreground">Added</span>
                                <span className="font-medium">{new Date(device.created_at).toLocaleDateString()}</span>
                            </div>
                        </CardContent>
                    </Card>

                    <DangerZone 
                        title="Danger Zone" 
                        description="Removing this device will invalidate all active sessions and stop all remote tracking."
                        buttonText="Delete Device Permanently"
                        onAction={destroy}
                    />
                </div>
            </div>
        </div>
    );
}