import React, { useState, useEffect, useRef } from 'react';
import { Head, router, useForm } from '@inertiajs/react';
import axios from 'axios';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { MonitorPlay, Settings, AlertCircle, RefreshCw, Trash2 } from 'lucide-react';
import RemoteControlPanel from '@/components/my-components/remote-control-panel';

interface Command {
    uuid: string;
    name: string;
    screenshot_path: string | null;
    has_client_request: boolean;
    has_host_response: boolean;
    updated_at: string;
}

interface Props {
    activeCommand?: Command;
    errors: any;
}

export default function Index({ activeCommand, errors }: Props) {
    const [activeTab, setActiveTab] = useState('viewer');
    
    // Auth Form State
    const { data, setData, post: postAuth, processing: authProcessing } = useForm({
        uuid: '',
        token: '',
    });

    // Polling State
    const [liveCommand, setLiveCommand] = useState<Command | undefined>(activeCommand);
    const [timestamp, setTimestamp] = useState(Date.now());
    const [pollIntervalMs, setPollIntervalMs] = useState(1000);
    
    const lastActiveTime = useRef(Date.now());
    const lastUpdateRef = useRef(activeCommand?.updated_at);

    // Update liveCommand if Inertia provides a new activeCommand (e.g., successful login)
    useEffect(() => {
        setLiveCommand(activeCommand);
        if (activeCommand) lastUpdateRef.current = activeCommand.updated_at;
    }, [activeCommand]);

    // SEQUENTIAL ASYNC POLLING LOGIC
    useEffect(() => {
        if (!liveCommand) return; 

        let isMounted = true;
        let timeoutId: ReturnType<typeof setTimeout>;
        let currentInterval = 1000;

    const poll = async () => {
            if (!isMounted) return;

            try {
                const res = await axios.get(`/client/commands/${liveCommand.uuid}/status?_t=${Date.now()}`);
                const statusData = res.data;
                
                if (statusData.updated_at !== lastUpdateRef.current) {
                    const nextTimestamp = Date.now();
                    const path = statusData.screenshot_path.startsWith('screenshots/') 
                        ? statusData.screenshot_path 
                        : `screenshots/${statusData.screenshot_path}`;
                    const nextUrl = `/storage/${path}?t=${nextTimestamp}`;

                    // --- PRELOADER FEATURE ---
                    const img = new Image();
                    img.onload = () => {
                        if (!isMounted) return;
                        lastUpdateRef.current = statusData.updated_at;
                        setLiveCommand(prev => prev ? { ...prev, ...statusData } : prev);
                        setTimestamp(nextTimestamp);
                    };
                    img.onerror = () => {
                        lastUpdateRef.current = statusData.updated_at;
                        setLiveCommand(prev => prev ? { ...prev, ...statusData } : prev);
                    };
                    img.src = nextUrl;
                }
            } catch (err: any) {
                 if(err.response?.status === 429) console.warn("Polling rate limited!");
            } finally {
                if (isMounted) {
                    const inactiveDurationMs = Date.now() - lastActiveTime.current;
                    currentInterval = inactiveDurationMs >= 60000 ? Math.min(currentInterval + 2000, 10000) : 1000;
                    setPollIntervalMs(currentInterval);
                    timeoutId = setTimeout(poll, currentInterval);
                }
            }
        };

        poll();

        return () => {
            isMounted = false;
            clearTimeout(timeoutId);
        };
    // CRITICAL: Do NOT put pollIntervalMs or liveCommand object here.
    }, [liveCommand?.uuid]); 

    const resetPollingTimer = () => {
        lastActiveTime.current = Date.now();
        setPollIntervalMs(1000);
    };

    const submitAuth = (e: React.FormEvent) => {
        e.preventDefault();
        postAuth('/client/authenticate');
    };

    const getImageUrl = () => {
        if (!liveCommand?.screenshot_path) return null;
        
        const path = liveCommand.screenshot_path.startsWith('screenshots/') 
            ? liveCommand.screenshot_path 
            : `screenshots/${liveCommand.screenshot_path}`;
            
        return `/storage/${path}?t=${timestamp}`;
    };

    return (
        <div className="flex flex-col h-screen max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <Head title="Client Portal" />
            
            <div className="flex justify-between items-center mb-6">
                <Heading title="Client Portal" description="Access shared remote sessions." />
                
                {liveCommand && (
                    <Badge 
                        variant={pollIntervalMs <= 1000 ? "default" : "outline"} 
                        className="cursor-pointer gap-2 font-mono" 
                        onClick={resetPollingTimer}
                    >
                        <RefreshCw className={`h-3 w-3 ${pollIntervalMs <= 1000 ? 'animate-spin' : ''}`} /> 
                        {pollIntervalMs <= 1000 ? <span>Polling (1.0s)</span> : <span>Idle ({pollIntervalMs / 1000}s)</span>}
                    </Badge>
                )}
            </div>

            <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col min-h-0">
                <TabsList className="grid w-[400px] grid-cols-2 mb-4">
                    <TabsTrigger value="viewer" disabled={!liveCommand}><MonitorPlay className="h-4 w-4 mr-2" /> Remote Viewer</TabsTrigger>
                    <TabsTrigger value="settings"><Settings className="h-4 w-4 mr-2" /> Connection</TabsTrigger>
                </TabsList>

                <TabsContent value="viewer" className="flex-1 mt-0 outline-none h-full flex flex-col min-h-0">
                    {liveCommand ? (
                         <RemoteControlPanel 
                            postEndpoint={`/client/commands/${liveCommand.uuid}/payload`}
                            imageUrl={getImageUrl()}
                            onResetPolling={resetPollingTimer}
                         />
                    ) : (
                        <Card className="flex-1 flex items-center justify-center border-dashed bg-muted/20">
                            <div className="text-center text-muted-foreground space-y-4">
                                <AlertCircle className="h-12 w-12 mx-auto opacity-50" />
                                <p>No active session. Please authenticate in the Connection tab.</p>
                                <Button variant="outline" onClick={() => setActiveTab('settings')}>Go to Connection</Button>
                            </div>
                        </Card>
                    )}
                </TabsContent>

                <TabsContent value="settings" className="mt-0 outline-none">
                    <Card className="max-w-md">
                        <CardHeader>
                            <CardTitle>Authenticate Session</CardTitle>
                            <CardDescription>Enter the UUID and token provided by the host.</CardDescription>
                        </CardHeader>
                        <form onSubmit={submitAuth}>
                            <CardContent className="space-y-4">
                                {errors.auth && <Alert variant="destructive"><AlertCircle className="h-4 w-4" /><AlertDescription>{errors.auth}</AlertDescription></Alert>}
                                <div className="space-y-2">
                                    <Label htmlFor="uuid">Command UUID</Label>
                                    <Input id="uuid" value={data.uuid} onChange={(e) => setData('uuid', e.target.value)} required />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="token">Access Token</Label>
                                    <Input id="token" type="password" value={data.token} onChange={(e) => setData('token', e.target.value)} required />
                                </div>
                            </CardContent>
                            <CardFooter className="flex justify-between">
                                <Button type="button" variant="ghost" onClick={() => router.post('/client/logout')} className="text-red-500"><Trash2 className="h-4 w-4 mr-2" /> Disconnect</Button>
                                <Button type="submit" disabled={authProcessing}>Save and Connect</Button>
                            </CardFooter>
                        </form>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
}