import { Head, router, Link, useForm } from '@inertiajs/react';
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';
import { Copy, RefreshCw, ExternalLink, Check, MonitorPlay } from 'lucide-react';
import DangerZone from '@/components/my-components/danger-zone';
import FlashMessages from '@/components/my-components/flash-messages';

type Device = {
    id: number;
    uuid: string;
    name: string;
};

type Command = {
    id: number;
    uuid: string;
    access_token: string;
    name: string;
    device_id: number;
    is_public: boolean;
    permissions: string;
    expires_at: string | null;
    device: Device;
    created_at?: string;
};

type Props = {
    command: Command;
    devices: Device[];
};

const formatForInput = (dateString: string | null) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Date(date.getTime() - date.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);
};

export default function Show({ command, devices = [] }: Props) {
    const [copiedToken, setCopiedToken] = useState(false);
    const [copiedLink, setCopiedLink] = useState(false);

    const { data, setData, patch, processing, errors } = useForm({
        name: command.name,
        device_id: command.device_id.toString(),
        is_public: command.is_public,
        permissions: command.permissions,
        expires_at: formatForInput(command.expires_at),
    });

    const submit = (e: React.FormEvent) => {
        e.preventDefault();
        patch(`/commands/${command.uuid}`);
    };

    const destroy = () => {
        if (confirm('Are you sure you want to delete this command link? This cannot be undone.')) {
            router.delete(`/commands/${command.uuid}`);
        }
    };

    const rotateToken = () => {
        if (confirm('Are you sure you want to rotate the access token? Anyone using the current token will be disconnected.')) {
            router.post(`/commands/${command.uuid}/rotate-token`);
        }
    };

    const copyToClipboard = (text: string, type: 'token' | 'link') => {
        navigator.clipboard.writeText(text);
        if (type === 'token') {
            setCopiedToken(true);
            setTimeout(() => setCopiedToken(false), 2000);
        } else {
            setCopiedLink(true);
            setTimeout(() => setCopiedLink(false), 2000);
        }
    };

    const isExpired = command.expires_at ? new Date(command.expires_at) < new Date() : false;
    const clientLink = `${window.location.origin}/client`;

    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
            <Head title={`Manage Link: ${command.name}`} />

            <Breadcrumbs breadcrumbs={[
                { title: 'Commands', href: '/commands' },
                { title: command.name, href: `/commands/${command.uuid}` },
            ]} />

            <FlashMessages />

            <div className="flex flex-wrap items-center justify-between gap-4">
                <Heading title="Manage Session" description="Update settings, revoke access, or rotate credentials." />
                <div className="flex gap-2">
                    <Button asChild variant="default" className="gap-2">
                        <Link href={`/commands/${command.uuid}/controller`}>
                            <MonitorPlay className="h-4 w-4" /> Open Private Controller
                        </Link>
                    </Button>
                </div>
            </div>

            <div className="grid gap-6 md:grid-cols-3 lg:grid-cols-4">
                <div className="md:col-span-2 lg:col-span-3 space-y-6">
                    {/* Credentials Card */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Connection Credentials</CardTitle>
                            <CardDescription>Share these details with authorized users so they can connect to this device.</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="space-y-2">
                                <Label>Client Access Portal</Label>
                                <div className="flex items-center gap-2">
                                    <Input value={clientLink} readOnly className="bg-muted font-mono text-xs" />
                                    <Button variant="outline" size="icon" onClick={() => copyToClipboard(clientLink, 'link')} title="Copy Link">
                                        {copiedLink ? <Check className="h-4 w-4 text-green-500" /> : <Copy className="h-4 w-4" />}
                                    </Button>
                                    <Button variant="outline" size="icon" asChild title="Open Client Portal">
                                        <a href={clientLink} target="_blank" rel="noopener noreferrer"><ExternalLink className="h-4 w-4" /></a>
                                    </Button>
                                </div>
                            </div>

                            <Separator />

                            <div className="grid sm:grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label>Session UUID</Label>
                                    <div className="flex items-center gap-2">
                                        <Input value={command.uuid} readOnly className="bg-muted font-mono text-xs" />
                                        <Button variant="ghost" size="icon" onClick={() => copyToClipboard(command.uuid, 'link')}>
                                            <Copy className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label>Access Token (Password)</Label>
                                    <div className="flex items-center gap-2">
                                        <Input value={command.access_token} readOnly className="bg-muted font-mono text-xs" type="password" />
                                        <Button variant="ghost" size="icon" onClick={() => copyToClipboard(command.access_token, 'token')}>
                                            {copiedToken ? <Check className="h-4 w-4 text-green-500" /> : <Copy className="h-4 w-4" />}
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </CardContent>
                        <CardFooter className="bg-muted/50 justify-between">
                            <p className="text-xs text-muted-foreground">If a token is compromised, rotate it immediately.</p>
                            <Button variant="outline" size="sm" onClick={rotateToken} className="gap-2">
                                <RefreshCw className="h-3 w-3" /> Rotate Token
                            </Button>
                        </CardFooter>
                    </Card>

                    {/* Settings Form */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Session Settings</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={submit} className="space-y-4">
                                <div className="grid md:grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="name">Session Name</Label>
                                        <Input id="name" value={data.name} onChange={e => setData('name', e.target.value)} required />
                                        {errors.name && <p className="text-sm text-red-500">{errors.name}</p>}
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="device_id">Target Device</Label>
                                        <select id="device_id" value={data.device_id} onChange={e => setData('device_id', e.target.value)}
                                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                                            {devices.map(device => (
                                                <option key={device.id} value={device.id}>{device.name}</option>
                                            ))}
                                        </select>
                                        {errors.device_id && <p className="text-sm text-red-500">{errors.device_id}</p>}
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="expires_at">Expiration Date (Optional)</Label>
                                    <Input id="expires_at" type="datetime-local" value={data.expires_at} onChange={e => setData('expires_at', e.target.value)} />
                                    {errors.expires_at && <p className="text-sm text-red-500">{errors.expires_at}</p>}
                                </div>

                                <div className="flex items-center space-x-2 pt-2">
                                    <input id="is_public" type="checkbox" checked={data.is_public} onChange={e => setData('is_public', e.target.checked)} className="h-4 w-4 rounded border-gray-300 text-primary" />
                                    <Label htmlFor="is_public" className="font-normal">Make Public (Allow Client Portal access)</Label>
                                </div>

                                <div className="pt-4 flex justify-end">
                                    <Button type="submit" disabled={processing}>Save Changes</Button>
                                </div>
                            </form>
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
                                <span className="text-muted-foreground">Access</span>
                                <Badge variant={command.is_public ? (isExpired ? "destructive" : "default") : "secondary"}>
                                    {isExpired ? 'Expired' : (command.is_public ? 'Public' : 'Private')}
                                </Badge>
                            </div>
                            <div className="flex justify-between items-center pt-1 border-t">
                                <span className="text-muted-foreground">Created</span>
                                <span className="font-medium">{command.created_at ? new Date(command.created_at).toLocaleDateString() : 'N/A'}</span>
                            </div>
                            <div className="flex justify-between items-center pt-1 border-t">
                                <span className="text-muted-foreground">Expires</span>
                                <span className={`font-medium ${isExpired ? 'text-red-500' : 'text-green-600'}`}>
                                    {command.expires_at ? (isExpired ? 'Expired' : new Date(command.expires_at).toLocaleDateString()) : 'Never'}
                                </span>
                            </div>
                        </CardContent>
                    </Card>

                    <DangerZone 
                        title="Danger Zone" 
                        description="Removing this session will instantly block all remote clients using these credentials."
                        buttonText="Delete Access Link"
                        onAction={destroy}
                    />
                </div>
            </div>
        </div>
    );
}