import { Head, router, Link, useForm, usePage } from '@inertiajs/react';
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';
import { Copy, RefreshCw, ExternalLink, Check } from 'lucide-react';

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
    return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
};

export default function Show({ command, devices = [] }: Props) {
    const { flash } = usePage().props as any;
    const [copied, setCopied] = useState(false);

    const { data, setData, patch, processing, errors } = useForm({
        name: command.name || '',
        device_id: command.device_id || '',
        is_public: command.is_public || false,
        permissions: command.permissions || 'view',
        expires_at: formatForInput(command.expires_at),
    });

    const breadcrumbs = [
        { title: 'Commands', href: '/commands' },
        { title: command.name || `Command #${command.id}`, href: `/commands/${command.uuid}` },
    ];

    const isExpired = command.expires_at ? new Date(command.expires_at) < new Date() : false;
    
    
    const publicUrl = `${window.location.origin}/s/${command.access_token}`;

    const submitUpdate = (e: React.FormEvent) => {
        e.preventDefault();
        patch(`/commands/${command.uuid}`, {
            preserveScroll: true,
        });
    };

    const rotateToken = () => {
        if (!confirm('Warning: This will invalidate the current public link. Anyone currently using it will lose access immediately.')) return;
        router.post(`/commands/${command.uuid}/rotate`, {}, {
            preserveScroll: true,
        });
    };

    const copyToClipboard = () => {
        navigator.clipboard.writeText(publicUrl);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    const destroy = () => {
        if (!confirm('Are you sure you want to delete this access link? This action is permanent.')) return;
        router.delete(`/commands/${command.uuid}`);
    };

    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-6xl">
            <Head title={`Manage ${command.name}`} />

            <Breadcrumbs breadcrumbs={breadcrumbs} />

            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <Heading title={command.name} description="Configure access rules and security for this remote session." />
                    <div className="mt-2 flex items-center gap-2">
                        <Badge variant={command.is_public ? (isExpired ? "destructive" : "default") : "secondary"}>
                            {isExpired ? 'Expired' : (command.is_public ? 'Public Access Live' : 'Private / Disabled')}
                        </Badge>
                        <Badge variant="outline" className="capitalize border-primary/30">{command.permissions} Access</Badge>
                        <span className="text-xs text-muted-foreground font-mono ml-2">ID: {command.uuid}</span>
                    </div>
                </div>
            </div>

            {flash?.success && (
                <div className="rounded-md bg-green-50 p-3 text-sm text-green-700 border border-green-200 dark:bg-green-900/20 dark:text-green-400 dark:border-green-800">
                    {flash.success}
                </div>
            )}

            <div className="grid gap-6 lg:grid-cols-3">
                {/* Main Configuration */}
                <div className="lg:col-span-2 space-y-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Access Control Settings</CardTitle>
                            <CardDescription>Define who can use this link and what they are allowed to do.</CardDescription>
                        </CardHeader>
                        <form onSubmit={submitUpdate}>
                            <CardContent className="space-y-5">
                                <div className="grid gap-4 sm:grid-cols-2">
                                    <div className="space-y-2">
                                        <Label htmlFor="name">Session Alias</Label>
                                        <Input
                                            id="name"
                                            value={data.name}
                                            onChange={(e) => setData('name', e.target.value)}
                                            placeholder="e.g., Guest Technician Access"
                                            required
                                        />
                                        {errors.name && <p className="text-xs text-red-500">{errors.name}</p>}
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="device_id">Target Device</Label>
                                        <select
                                            id="device_id"
                                            value={data.device_id}
                                            onChange={(e) => setData('device_id', parseInt(e.target.value))}
                                            className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-ring"
                                            required
                                        >
                                            {devices.map((device) => (
                                                <option key={device.id} value={device.id}>{device.name}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                <div className="grid gap-4 sm:grid-cols-2">
                                    <div className="space-y-2">
                                        <Label htmlFor="permissions">Permission Level</Label>
                                        <select
                                            id="permissions"
                                            value={data.permissions}
                                            onChange={(e) => setData('permissions', e.target.value)}
                                            className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-ring"
                                        >
                                            <option value="view">View Only (Monitor)</option>
                                            <option value="control">Remote Control (Execute)</option>
                                            <option value="admin">Full Admin (Reconfigure)</option>
                                        </select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="expires_at">Automatic Expiration</Label>
                                        <Input
                                            id="expires_at"
                                            type="datetime-local"
                                            value={data.expires_at}
                                            onChange={(e) => setData('expires_at', e.target.value)}
                                        />
                                        <p className="text-[10px] text-muted-foreground italic">Link will stop working after this date.</p>
                                    </div>
                                </div>

                                <Separator />

                                <div className="flex items-start space-x-3 rounded-md border p-4 bg-muted/30">
                                    <input
                                        id="is_public"
                                        type="checkbox"
                                        checked={data.is_public}
                                        onChange={(e) => setData('is_public', e.target.checked)}
                                        className="mt-1 h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                                    />
                                    <div className="space-y-1 leading-none">
                                        <Label htmlFor="is_public" className="text-sm font-medium leading-none">
                                            Enable Public Access
                                        </Label>
                                        <p className="text-xs text-muted-foreground">
                                            When enabled, anyone with the secret token can access this device without an account.
                                        </p>
                                    </div>
                                </div>
                            </CardContent>
                            <CardFooter className="bg-muted/20 border-t px-6 py-4">
                                <Button type="submit" disabled={processing} className="w-full sm:w-auto">
                                    {processing ? 'Saving Changes...' : 'Save Configuration'}
                                </Button>
                            </CardFooter>
                        </form>
                    </Card>
                </div>

                {/* Sidebar: Public Link & Status */}
                <div className="space-y-6">
                    {/* The Secret Link Card */}
                    <Card className={command.is_public ? "border-primary/50 shadow-sm" : "opacity-60"}>
                        <CardHeader className="pb-3">
                            <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground">Shareable Link</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="relative">
                                <Input 
                                    readOnly 
                                    value={command.is_public ? publicUrl : "Link Disabled"} 
                                    className="pr-24 font-mono text-[11px] bg-muted/50"
                                />
                                <div className="absolute right-1 top-1 flex gap-1">
                                    <Button 
                                        size="sm" 
                                        variant="ghost" 
                                        className="h-7 px-2" 
                                        onClick={copyToClipboard}
                                        disabled={!command.is_public}
                                    >
                                        {copied ? <Check className="h-3.5 w-3.5 text-green-500" /> : <Copy className="h-3.5 w-3.5" />}
                                    </Button>
                                    <Button 
                                        size="sm" 
                                        variant="ghost" 
                                        className="h-7 px-2"
                                        asChild
                                        disabled={!command.is_public || isExpired}
                                    >
                                        <a href={publicUrl} target="_blank" rel="noreferrer">
                                            <ExternalLink className="h-3.5 w-3.5" />
                                        </a>
                                    </Button>
                                </div>
                            </div>

                            <Button 
                                variant="outline" 
                                size="sm" 
                                className="w-full text-xs gap-2" 
                                onClick={rotateToken}
                                disabled={processing}
                            >
                                <RefreshCw className={`h-3 w-3 ${processing ? 'animate-spin' : ''}`} />
                                Regenerate Secret Token
                            </Button>
                            
                            {!command.is_public && (
                                <p className="text-[10px] text-center text-amber-600 font-medium">
                                    Enable "Public Access" to activate this link.
                                </p>
                            )}
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="pb-3">
                            <CardTitle className="text-sm font-bold uppercase tracking-wider text-muted-foreground">Session Info</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-3 text-sm">
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Parent Device</span>
                                <Link href={`/devices/${command.device.uuid}`} className="font-medium text-primary hover:underline">
                                    {command.device.name}
                                </Link>
                            </div>
                            <div className="flex justify-between">
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

                    <Card className="border-red-100 dark:border-red-900/30">
                        <CardHeader className="pb-3">
                            <CardTitle className="text-sm font-bold text-red-600 uppercase tracking-wider">Danger Zone</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <Button variant="destructive" size="sm" className="w-full" onClick={destroy}>
                                Delete Access Link
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}