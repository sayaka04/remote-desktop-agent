import { Head, router, useForm } from '@inertiajs/react';
import React, { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { 
    MonitorPlay, Settings, Save, AlertCircle, RefreshCw, Trash2, 
    MousePointer2, Type, Send, X, CheckCircle2, XCircle, Maximize2, Minimize2, TerminalSquare
} from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { TransformWrapper, TransformComponent } from "react-zoom-pan-pinch";

type ClickType = 'left' | 'middle' | 'right';
type Action = 
    | { type: 'move_mouse'; x: number; y: number; id: string }
    | { type: 'click'; button: ClickType; id: string }
    | { type: 'type_text'; text: string; id: string };

interface Device { name: string; }
interface Command {
    uuid: string;
    name: string;
    screenshot_path: string | null;
    device: Device;
}

interface Props {
    activeCommand: Command | null;
    credentials?: { uuid: string | null; token: string | null; };
    errors: any;
}

export default function Index({ activeCommand, credentials, errors }: Props) {
    const { data, setData, post: postAuth, processing: authProcessing } = useForm<{uuid: string; token: string}>({
        uuid: credentials?.uuid || '',
        token: credentials?.token || ''
    });

    const [actions, setActions] = useState<Action[]>([]);
    const [textInput, setTextInput] = useState('');
    const [isPanMode, setIsPanMode] = useState(false); 
    const [isFullscreen, setIsFullscreen] = useState(false);
    
    // This state controls the floating composer
    const [isComposerOpen, setIsComposerOpen] = useState(false);

    const [sendStatus, setSendStatus] = useState<'idle' | 'sending' | 'success' | 'error'>('idle');
    const [sendErrorMsg, setSendErrorMsg] = useState<string>('');
    const [timestamp, setTimestamp] = useState(Date.now());
    const [pollIntervalMs, setPollIntervalMs] = useState(1000);
    
    const lastActiveTime = useRef(Date.now());
    const fullscreenRef = useRef<HTMLDivElement>(null);

    const toggleFullscreen = () => {
        if (!document.fullscreenElement) {
            fullscreenRef.current?.requestFullscreen().catch(err => {
                console.error(`Error attempting to enable full-screen mode: ${err.message}`);
            });
        } else {
            document.exitFullscreen();
        }
    };

    useEffect(() => {
        const handler = () => setIsFullscreen(!!document.fullscreenElement);
        document.addEventListener('fullscreenchange', handler);
        return () => document.removeEventListener('fullscreenchange', handler);
    }, []);

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

    const handleImageClick = (e: React.MouseEvent<HTMLDivElement>) => {
        if (isPanMode) return;
        resetPollingTimer();
        const rect = e.currentTarget.getBoundingClientRect();
        const percentX = (e.clientX - rect.left) / rect.width;
        const percentY = (e.clientY - rect.top) / rect.height;
        setActions(prev => [...prev, { type: 'move_mouse', x: Number(percentX.toFixed(4)), y: Number(percentY.toFixed(4)), id: crypto.randomUUID() }]);
        
        // Optionally auto-open the composer when they click the image so they know where to execute
        if (!isComposerOpen && actions.length === 0) setIsComposerOpen(true);
    };

    const addClick = (button: ClickType) => { 
        resetPollingTimer(); 
        setActions(prev => [...prev, { type: 'click', button, id: crypto.randomUUID() }]); 
    };

    const addText = () => {
        resetPollingTimer();
        if (!textInput.trim()) return;
        setActions(prev => [...prev, { type: 'type_text', text: textInput, id: crypto.randomUUID() }]);
        setTextInput('');
    };

    const sendPayload = () => {
        resetPollingTimer();
        if (actions.length === 0 || !activeCommand) return;
        setSendStatus('sending');
        setSendErrorMsg('');
        const sanitizedPayload = actions.map(({ id, ...rest }) => rest);
        
        router.post(`/client/commands/${activeCommand.uuid}/payload`, { payload: sanitizedPayload }, {
            preserveScroll: true,
            onSuccess: () => {
                setSendStatus('success');
                setActions([]); 
                setTimeout(() => setSendStatus('idle'), 2500);
            },
            onError: (errs) => {
                setSendStatus('error');
                const firstError = Object.values(errs)[0];
                setSendErrorMsg(typeof firstError === 'string' ? firstError : 'Failed');
                setTimeout(() => setSendStatus('idle'), 4000);
            }
        });
    };

    const ActionList = () => (
        <div className="flex-1 overflow-y-auto min-h-0 space-y-2 pr-1 custom-scrollbar">
            {actions.length === 0 ? (
                <div className="h-20 flex items-center justify-center text-muted-foreground text-xs border-2 border-dashed rounded-md">Queue empty</div>
            ) : (
                actions.map((action, index) => (
                    <div key={action.id} className="flex items-center justify-between p-2 rounded bg-background/50 border text-[11px] shadow-sm">
                        <div className="flex items-center gap-2">
                            <Badge className="w-4 h-4 p-0 flex items-center justify-center text-[9px]">{index + 1}</Badge>
                            <span className="truncate max-w-[120px]">
                                {action.type === 'move_mouse' ? `Move ${Math.round(action.x * 100)}%` : action.type === 'click' ? `Click ${action.button}` : `Type: ${action.text}`}
                            </span>
                        </div>
                        <button onClick={() => setActions(actions.filter(a => a.id !== action.id))} className="text-destructive"><X className="h-3 w-3" /></button>
                    </div>
                ))
            )}
        </div>
    );

    return (
        <div className="flex h-full flex-1 flex-col gap-6 p-4 md:p-6 lg:p-8 w-full max-w-7xl mx-auto">
            <Head title="Web Client Controller" />
            
            <div className="flex justify-between items-center">
                <Heading title="Web Controller" description="Interactive remote desktop agent." />
                {activeCommand && (
                    <Badge variant={pollIntervalMs === 1000 ? "default" : "outline"} className="gap-2">
                        <RefreshCw className={`h-3 w-3 ${pollIntervalMs === 1000 ? 'animate-spin' : ''}`} />
                        Polling: {pollIntervalMs / 1000}s
                    </Badge>
                )}
            </div>

            <Tabs defaultValue={activeCommand ? "controller" : "config"} className="w-full">
                <TabsList className="grid w-full max-w-md grid-cols-2 mb-6">
                    <TabsTrigger value="controller" className="gap-2"><MonitorPlay className="h-4 w-4" /> Controller</TabsTrigger>
                    <TabsTrigger value="config" className="gap-2"><Settings className="h-4 w-4" /> Config</TabsTrigger>
                </TabsList>

                <TabsContent value="controller">
                    {!activeCommand ? (
                        <Alert variant="destructive"><AlertCircle className="h-4 w-4" /><AlertTitle>Auth Required</AlertTitle></Alert>
                    ) : (
                        <div className="grid lg:grid-cols-3 gap-6 items-start">
                            
                            {/* --- MAIN VIEWER SECTION --- */}
                            <div ref={fullscreenRef} className={`lg:col-span-3 relative group overflow-hidden rounded-xl border-2 shadow-2xl bg-black ${isFullscreen ? 'w-screen h-screen rounded-none border-0' : 'aspect-video'}`}>
                                
                                <TransformWrapper initialScale={1} disabled={!isPanMode} minScale={1} maxScale={5}>
                                    <TransformComponent wrapperClass="w-full h-full">
                                        <div 
                                            className={`relative w-full h-full flex items-center justify-center ${!isPanMode ? 'cursor-crosshair' : 'cursor-grab active:cursor-grabbing'}`}
                                            onClick={handleImageClick}
                                        >
                                            <img src={getImageUrl() || ''} className="max-w-full max-h-full object-contain pointer-events-none select-none" alt="Remote Desktop" />
                                            
                                            {actions.map((action, index) => action.type === 'move_mouse' && (
                                                <div key={action.id} className="absolute w-6 h-6 -ml-3 -mt-3 bg-red-500 rounded-full border-2 border-white flex items-center justify-center text-white text-[10px] font-bold shadow-lg" style={{ left: `${action.x * 100}%`, top: `${action.y * 100}%` }}>
                                                    {index + 1}
                                                </div>
                                            ))}
                                        </div>
                                    </TransformComponent>
                                </TransformWrapper>

                                {/* Top Header Overlay */}
                                <div className="absolute top-4 left-4 right-4 flex justify-between items-center opacity-0 group-hover:opacity-100 transition-opacity z-40">
                                    <Badge variant="secondary" className="bg-black/50 text-white backdrop-blur-md px-3 py-1">
                                        {activeCommand.device.name} {isPanMode ? '(Panning)' : '(Targeting)'}
                                    </Badge>
                                    <div className="flex gap-2">
                                        <Button size="sm" variant="secondary" className="bg-black/50 backdrop-blur-md text-white" onClick={() => setIsPanMode(!isPanMode)}>
                                            {isPanMode ? 'Mode: Pan' : 'Mode: Target'}
                                        </Button>
                                        <Button size="sm" variant="secondary" className="bg-black/50 backdrop-blur-md text-white" onClick={toggleFullscreen}>
                                            {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
                                        </Button>
                                    </div>
                                </div>

                                {/* --- ALWAYS VISIBLE FLOATING COMPOSER --- */}
                                
                                {/* 1. The Toggle Circle */}
                                <Button 
                                    onClick={() => setIsComposerOpen(!isComposerOpen)}
                                    className="absolute bottom-6 right-6 w-14 h-14 rounded-full shadow-2xl z-50 transition-transform active:scale-95"
                                    variant={isComposerOpen ? "destructive" : "default"}
                                    title="Toggle Command Composer"
                                >
                                    {isComposerOpen ? <X className="h-6 w-6" /> : <TerminalSquare className="h-6 w-6" />}
                                    {actions.length > 0 && !isComposerOpen && (
                                        <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white border-2 border-background">
                                            {actions.length}
                                        </span>
                                    )}
                                </Button>

                                {/* 2. The Pop-out Panel */}
                                {isComposerOpen && (
                                    <div className="absolute bottom-24 right-6 w-[320px] bg-background/95 backdrop-blur-xl border border-border/50 rounded-2xl shadow-2xl flex flex-col p-5 z-50 animate-in slide-in-from-bottom-5 duration-200 fade-in">
                                        <div className="flex items-center justify-between mb-4 border-b pb-2">
                                            <span className="font-bold flex items-center gap-2 text-sm">
                                                <MousePointer2 className="h-4 w-4 text-primary" /> Composer
                                            </span>
                                            <Badge variant="outline" className="bg-muted/50">{actions.length} queued</Badge>
                                        </div>
                                        
                                        <div className="max-h-48 overflow-hidden flex flex-col gap-2 mb-4">
                                            <ActionList />
                                        </div>

                                        <div className="grid grid-cols-3 gap-2 mt-auto">
                                            <Button size="sm" variant="outline" className="h-8 text-xs font-medium" onClick={() => addClick('left')}>Left</Button>
                                            <Button size="sm" variant="outline" className="h-8 text-xs font-medium" onClick={() => addClick('middle')}>Mid</Button>
                                            <Button size="sm" variant="outline" className="h-8 text-xs font-medium" onClick={() => addClick('right')}>Right</Button>
                                        </div>

                                        <div className="flex gap-2 mt-3">
                                            <Input className="h-9 text-xs focus-visible:ring-1" placeholder="Type text..." value={textInput} onChange={e => setTextInput(e.target.value)} onKeyDown={e => e.key === 'Enter' && addText()} />
                                            <Button size="sm" className="h-9 px-3" onClick={addText}><Type className="h-4 w-4" /></Button>
                                        </div>

                                        <Button 
                                            className={`w-full mt-4 h-10 gap-2 font-semibold transition-all duration-300 ${sendStatus === 'success' ? 'bg-green-600 hover:bg-green-700' : sendStatus === 'error' ? 'bg-red-600 hover:bg-red-700' : ''}`}
                                            disabled={actions.length === 0 || sendStatus === 'sending'}
                                            onClick={sendPayload}
                                        >
                                            {sendStatus === 'idle' && <><Send className="h-4 w-4" /> Execute Sequence</>}
                                            {sendStatus === 'sending' && <RefreshCw className="h-4 w-4 animate-spin" />}
                                            {sendStatus === 'success' && <><CheckCircle2 className="h-4 w-4" /> Sequence Sent!</>}
                                            {sendStatus === 'error' && <><XCircle className="h-4 w-4" /> {sendErrorMsg || 'Failed'}</>}
                                        </Button>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </TabsContent>

                <TabsContent value="config">
                    <Card className="max-w-2xl">
                        <CardHeader>
                            <CardTitle>Session Configuration</CardTitle>
                        </CardHeader>
                        <form onSubmit={(e) => { e.preventDefault(); postAuth('/client/authenticate'); }}>
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