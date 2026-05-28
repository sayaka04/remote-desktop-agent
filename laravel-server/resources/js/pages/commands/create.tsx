import { Head, useForm, Link } from '@inertiajs/react';
import React from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';
import FlashMessages from '@/components/my-components/flash-messages';

type Device = {
    id: string;
    name: string;
};

type Props = {
    devices: Device[];
};

const breadcrumbs = [
    { title: 'Commands', href: '/commands' },
    { title: 'Create', href: '/commands/create' },
];

export default function Create({ devices }: Props) {
    const { data, setData, post, processing, reset, errors } = useForm({
        device_id: '',
        name: '',
        is_public: false,
        permissions: 'view',
        expires_at: '',
    });

    const submit = (e: React.FormEvent) => {
        e.preventDefault();
        post('/commands', {
            onSuccess: () => reset(),
        });
    };

    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-2xl mx-auto w-full">
            <Head title="Create Session" />

            <Breadcrumbs breadcrumbs={breadcrumbs} />

            <Heading title="Create Access Link" description="Generate a new session for remote device access." />

            <FlashMessages />

            <Card>
                <CardHeader>
                    <CardTitle>Session Details</CardTitle>
                    <CardDescription>Configure the target device and access rules.</CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={submit} className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="device_id">Target Device</Label>
                            <select
                                id="device_id"
                                value={data.device_id}
                                onChange={(e) => setData('device_id', e.target.value)}
                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                                required
                            >
                                <option value="" disabled>Select a device</option>
                                {devices.map((device) => (
                                    <option key={device.id} value={device.id}>
                                        {device.name}
                                    </option>
                                ))}
                            </select>
                            {errors.device_id && <p className="text-sm text-red-500">{errors.device_id}</p>}
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="name">Session Name</Label>
                            <Input
                                id="name"
                                type="text"
                                value={data.name}
                                onChange={(e) => setData('name', e.target.value)}
                                placeholder="e.g. Helpdesk Session"
                                required
                            />
                            {errors.name && <p className="text-sm text-red-500">{errors.name}</p>}
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="expires_at">Expiration Date (Optional)</Label>
                            <Input
                                id="expires_at"
                                type="datetime-local"
                                value={data.expires_at}
                                onChange={(e) => setData('expires_at', e.target.value)}
                            />
                            <p className="text-xs text-muted-foreground">Leave blank for a permanent link.</p>
                            {errors.expires_at && <p className="text-sm text-red-500">{errors.expires_at}</p>}
                        </div>

                        <div className="flex items-center space-x-2 pt-2">
                            <input
                                id="is_public"
                                type="checkbox"
                                checked={data.is_public}
                                onChange={(e) => setData('is_public', e.target.checked)}
                                className="h-4 w-4 rounded border-gray-300 text-primary"
                            />
                            <Label htmlFor="is_public" className="font-normal">
                                Make Public (Allow access via Client Portal)
                            </Label>
                        </div>
                        {errors.is_public && <p className="text-sm text-red-500">{errors.is_public}</p>}

                        <div className="pt-4 flex gap-2">
                            <Button type="submit" disabled={processing}>
                                {processing ? 'Creating...' : 'Create Access Link'}
                            </Button>
                            <Button variant="ghost" asChild>
                                <Link href="/commands">Cancel</Link>
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}