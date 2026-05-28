import React, { useState, useEffect, useRef } from 'react';
import { Head, router, useForm } from '@inertiajs/react';
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

    const [timestamp, setTimestamp] = useState(Date.now());
    const [pollIntervalMs, setPollIntervalMs] = useState(1000);
    const lastActiveTime = useRef(Date.now());

    // Polling Logic (Only active when connected)
    useEffect(() => {
        if (!activeCommand) return;
        const timerId = setInterval(() => {
            const inactiveDurationMs = Date.now() - lastActiveTime.current;
            if (inactiveDurationMs >= 60000) {
                setPollIntervalMs(prev => Math.min(prev + 2000, 10000));
            } else {
                setPollIntervalMs(1000); 
            }
            
            setTimestamp(Date.now());
            router.reload({ only: ['activeCommand'] });
        }, pollIntervalMs);
        return () => clearInterval(timerId);
    }, [pollIntervalMs, activeCommand]);

    const resetPollingTimer = () => {
        lastActiveTime.current = Date.now();
        if (pollIntervalMs > 1000) setPollIntervalMs(1000);
    };

    const getImageUrl = () => {
        if (!activeCommand?.screenshot_path) return null;
        const path = activeCommand.screenshot_path.startsWith('screenshots/') 
            ? activeCommand.screenshot_path 
            : `screenshots/${activeCommand.screenshot_path}`;
        return `/storage/${path}?t=${timestamp}`;
    };

    return (
        <div className="flex flex-col h-full flex-1 gap-6 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
            <Head title="Client Web Controller" />
            
            <div className="flex justify-between items-center">
                <Heading 
                    title={activeCommand ? `Connected: ${activeCommand.name}` : "Client Access"} 
                    description={activeCommand ? "Remote control session active" : "Connect to a remote device using your access token"} 
                />
                {activeCommand && (
                    <Badge 
                        variant={pollIntervalMs <= 1000 ? "default" : "outline"} 
                        className="cursor-pointer gap-2 font-mono transition-all" 
                        onClick={resetPollingTimer}
                        title="Click to force refresh and reset timer"
                    >
                        <RefreshCw className={`h-3 w-3 ${pollIntervalMs <= 1000 ? 'animate-spin' : ''}`} /> 
                        {pollIntervalMs <= 1000 ? <span>Polling (1.0s)</span> : <span>Idle Polling ({pollIntervalMs / 1000}s)</span>}
                    </Badge>
                )}
            </div>

            <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col min-h-0">
                <TabsList className="grid w-full grid-cols-2 max-w-[400px]">
                    <TabsTrigger value="viewer" className="gap-2"><MonitorPlay className="h-4 w-4" /> Viewer</TabsTrigger>
                    <TabsTrigger value="settings" className="gap-2"><Settings className="h-4 w-4" /> Connection</TabsTrigger>
                </TabsList>
                
                <TabsContent value="viewer" className="flex-1 flex flex-col mt-4 min-h-0 data-[state=inactive]:hidden">
                    {!activeCommand ? (
                        <Card className="flex-1 flex flex-col items-center justify-center text-center p-8 border-dashed">
                            <MonitorPlay className="h-12 w-12 text-muted-foreground mb-4 opacity-50" />
                            <h3 className="text-lg font-semibold">Not Connected</h3>
                            <p className="text-muted-foreground max-w-sm mt-2 mb-6">You need to configure your connection settings before you can view the remote screen.</p>
                            <Button onClick={() => setActiveTab('settings')}>Configure Connection</Button>
                        </Card>
                    ) : (
                        <RemoteControlPanel 
                            postEndpoint={`/client/commands/${activeCommand.uuid}/payload`}
                            imageUrl={getImageUrl()}
                            onResetPolling={resetPollingTimer}
                        />
                    )}
                </TabsContent>
                
                <TabsContent value="settings" className="mt-4">
                    <Card className="max-w-md">
                        <CardHeader>
                            <CardTitle>Connection Status</CardTitle>
                            <CardDescription>Enter the credentials provided by the host.</CardDescription>
                        </CardHeader>
                        <form onSubmit={(e) => { e.preventDefault(); postAuth('/client/authenticate', { onSuccess: () => setActiveTab('viewer') }); }}>
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