import { Head, useForm, Link } from '@inertiajs/react';
import React from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import Heading from '@/components/heading';
import InputError from '@/components/input-error';
import { Breadcrumbs } from '@/components/breadcrumbs';
import FlashMessages from '@/components/my-components/flash-messages';

const breadcrumbs = [
    { title: 'Devices', href: '/devices' },
    { title: 'Create', href: '/devices/create' },
];

export default function Create() {
    const { data, setData, post, processing, reset, errors } = useForm({
        name: '',
    });

    const submit = (e: React.FormEvent) => {
        e.preventDefault();
        post('/devices', {
            onSuccess: () => reset(),
        });
    };

    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-2xl mx-auto w-full">
            <Head title="Create Device" />

            <Breadcrumbs breadcrumbs={breadcrumbs} />

            <Heading title="Register Device" description="Add a new remote device to your account." />

            <FlashMessages />

            <Card>
                <CardHeader>
                    <CardTitle>Device Details</CardTitle>
                    <CardDescription>Enter a recognizable name for your new device.</CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={submit} className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="name">Device Name</Label>
                            <Input
                                id="name"
                                type="text"
                                value={data.name}
                                onChange={(e) => setData('name', e.target.value)}
                                placeholder="e.g. Living Room PC"
                                required
                                autoFocus
                            />
                            <InputError message={errors.name} />
                        </div>

                        <div className="flex items-center gap-2 pt-4">
                            <Button type="submit" disabled={processing}>
                                {processing ? 'Creating...' : 'Register Device'}
                            </Button>
                            <Button variant="ghost" asChild>
                                <Link href="/devices">Cancel</Link>
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}