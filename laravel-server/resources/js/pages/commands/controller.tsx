import { Head, router, Link } from '@inertiajs/react';
import React, { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';
import { ArrowLeft, RefreshCw } from 'lucide-react';
import RemoteControlPanel from '@/components/my-components/remote-control-panel';

interface Device {
    name: string;
}

interface Command {
    uuid: string;
    name: string;
    screenshot_path: string | null;
    has_client_request: boolean;
    has_host_response: boolean;
    updated_at: string;
    device: Device;
}

interface Props {
    command: Command;
}

export default function Controller({ command }: Props) {
    const [timestamp, setTimestamp] = useState(Date.now());
    const [pollIntervalMs, setPollIntervalMs] = useState(1000);
    const lastActiveTime = useRef(Date.now());

    // Polling Logic
    useEffect(() => {
        const timerId = setInterval(() => {
            const inactiveDurationMs = Date.now() - lastActiveTime.current;
            if (inactiveDurationMs >= 60000) {
                setPollIntervalMs(prev => Math.min(prev + 2000, 10000));
            } else {
                setPollIntervalMs(1000); 
            }
            
            setTimestamp(Date.now());
            router.reload({ only: ['command'] });
        }, pollIntervalMs);
        
        return () => clearInterval(timerId);
    }, [pollIntervalMs, command.uuid]);

    const resetPollingTimer = () => {
        lastActiveTime.current = Date.now();
        if (pollIntervalMs > 1000) setPollIntervalMs(1000);
    };

    const getImageUrl = () => {
        if (!command.screenshot_path) return null;
        const path = command.screenshot_path.startsWith('screenshots/') 
            ? command.screenshot_path 
            : `screenshots/${command.screenshot_path}`;
        return `/storage/${path}?t=${timestamp}`;
    };

    return (
        <div className="flex flex-col h-full flex-1 gap-6 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
            <Head title={`Controller: ${command.name}`} />

            <Breadcrumbs breadcrumbs={[
                { title: 'Commands', href: '/commands' },
                { title: command.name, href: `/commands/${command.uuid}` },
                { title: 'Remote Control', href: `/commands/${command.uuid}/controller` },
            ]} />
            
            <div className="flex flex-wrap justify-between items-center gap-4">
                <div className="flex items-center gap-4">
                    <Button variant="outline" size="icon" asChild>
                        <Link href={`/commands/${command.uuid}`}><ArrowLeft className="h-4 w-4" /></Link>
                    </Button>
                    <Heading title={`Remote: ${command.device.name}`} description={`Session: ${command.name}`} />
                </div>
                
                <Badge 
                    variant={pollIntervalMs <= 1000 ? "default" : "outline"} 
                    className="cursor-pointer gap-2 font-mono transition-all" 
                    onClick={resetPollingTimer}
                    title="Click to force refresh and reset timer"
                >
                    <RefreshCw className={`h-3 w-3 ${pollIntervalMs <= 1000 ? 'animate-spin' : ''}`} /> 
                    {pollIntervalMs <= 1000 ? <span>Polling (1.0s)</span> : <span>Idle Polling ({pollIntervalMs / 1000}s)</span>}
                </Badge>
            </div>

            <RemoteControlPanel 
                postEndpoint={`/commands/${command.uuid}/payload`}
                imageUrl={getImageUrl()}
                onResetPolling={resetPollingTimer}
            />
        </div>
    );
}