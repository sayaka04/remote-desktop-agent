import { Head, router, Link } from '@inertiajs/react';
import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { Breadcrumbs } from '@/components/breadcrumbs';
import { ArrowLeft, RefreshCw } from 'lucide-react';
import RemoteControlPanel from '@/components/my-components/remote-control-panel';

interface Device {
    uuid: string;
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
    const [liveCommand, setLiveCommand] = useState(command);
    const [timestamp, setTimestamp] = useState(Date.now());
    const [pollIntervalMs, setPollIntervalMs] = useState(1000);
    
    const lastActiveTime = useRef(Date.now());
    const lastUpdateRef = useRef(command.updated_at);

    // SEQUENTIAL ASYNC POLLING LOGIC
    useEffect(() => {
        let isMounted = true;
        let timeoutId: ReturnType<typeof setTimeout>;
        let currentInterval = 1000; // Local tracker to prevent closure staleness

        const poll = async () => {
            if (!isMounted) return;

            try {
                const res = await axios.get(`/commands/${command.uuid}/status?_t=${Date.now()}`);
                const data = res.data;
                
                if (data.updated_at !== lastUpdateRef.current) {
                    const nextTimestamp = Date.now();
                    
                    // Construct the URL of the new image
                    const path = data.screenshot_path.startsWith('screenshots/') 
                        ? data.screenshot_path 
                        : `screenshots/${data.screenshot_path}`;
                    const nextUrl = `/storage/${path}?t=${nextTimestamp}`;

                    // --- PRELOADER FEATURE ---
                    // Create a "virtual" image in memory to download the file before showing it
                    const img = new Image();
                    img.onload = () => {
                        // This block only runs once the browser has the full image ready
                        if (!isMounted) return;
                        lastUpdateRef.current = data.updated_at;
                        setLiveCommand(prev => ({ ...prev, ...data }));
                        setTimestamp(nextTimestamp); // This now triggers an instant swap with no flash
                    };
                    img.onerror = () => {
                        // If the image failed to load, we still update the timestamp so we don't get stuck
                        lastUpdateRef.current = data.updated_at;
                        setLiveCommand(prev => ({ ...prev, ...data }));
                    };
                    img.src = nextUrl; // This starts the background download
                }
            } catch (err: any) {
                if (err.response?.status === 429) console.warn("Polling rate limited!");
            } finally {
                // Same finally block as before...
                if (isMounted) {
                    const inactiveDurationMs = Date.now() - lastActiveTime.current;
                    currentInterval = inactiveDurationMs >= 60000 ? Math.min(currentInterval + 2000, 10000) : 1000;
                    setPollIntervalMs(currentInterval);
                    timeoutId = setTimeout(poll, currentInterval);
                }
            }
        };
        // Start the first request immediately
        poll();

        return () => {
            isMounted = false;
            clearTimeout(timeoutId);
        };
    // CRITICAL: We only want this effect to run once per command. 
    // Do NOT put pollIntervalMs in this array.
    }, [command.uuid]); 

    const resetPollingTimer = () => {
        lastActiveTime.current = Date.now();
        setPollIntervalMs(1000);
    };

    const getImageUrl = () => {
        if (!liveCommand.screenshot_path) return null;
        
        const path = liveCommand.screenshot_path.startsWith('screenshots/') 
            ? liveCommand.screenshot_path 
            : `screenshots/${liveCommand.screenshot_path}`;
            
        return `/storage/${path}?t=${timestamp}`;
    };

    return (
        <div className="flex flex-col h-[calc(100vh-theme(spacing.16))] space-y-4 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <Head title={`Control: ${command.device.name}`} />
            
            <Breadcrumbs breadcrumbs={[
                { title: 'Devices', href: '/devices' },
                { title: command.device.name, href: `/devices/${command.device.uuid}` },
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