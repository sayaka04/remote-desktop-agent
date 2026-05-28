import { Head, Link } from '@inertiajs/react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';
import FlashMessages from '@/components/my-components/flash-messages';

type Device = {
    uuid: string;
    user_id: number;
    name: string;
    last_seen_at: string;
    created_at: string;
    updated_at: string;
};

type Props = {
    devices: Device[];
};

const breadcrumbs = [
    { title: 'Devices', href: '/devices' },
];

export default function Index({ devices }: Props) {
    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
            <Head title="Devices" />
            
            <Breadcrumbs breadcrumbs={breadcrumbs} />

            <FlashMessages />

            <div className="flex items-center justify-between">
                <Heading title="Devices" description="Manage your remote devices." />
                <Button asChild>
                    <Link href="/devices/create">+ Add Device</Link>
                </Button>
            </div>

            {devices.length === 0 ? (
                <div className="flex flex-1 items-center justify-center rounded-lg border border-dashed shadow-sm p-8">
                    <p className="text-sm text-muted-foreground">No devices found.</p>
                </div>
            ) : (
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {devices.map((device) => (
                        <Card key={device.uuid} className="flex flex-col">
                            <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
                                <CardTitle className="text-lg font-bold">{device.name}</CardTitle>
                                <Badge variant="secondary" className="bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400">
                                    Active
                                </Badge>
                            </CardHeader>
                            <CardContent className="flex-1 space-y-1 text-sm text-muted-foreground mt-2">
                                <p><span className="font-medium text-foreground">Last seen:</span> {device.last_seen_at || 'Never'}</p>
                                <p><span className="font-medium text-foreground">Added:</span> {new Date(device.created_at).toLocaleDateString()}</p>
                            </CardContent>
                            <CardFooter>
                                <Button variant="outline" asChild className="w-full">
                                    <Link href={`/devices/${device.uuid}`}>View Details</Link>
                                </Button>
                            </CardFooter>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}