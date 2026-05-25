import { Head, router, useForm } from '@inertiajs/react';
import React, { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import Heading from '@/components/heading';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { TransformWrapper, TransformComponent } from "react-zoom-pan-pinch";
import { 
    MonitorPlay, Settings, AlertCircle, RefreshCw, Trash2, 
    Type, Send, X, CheckCircle2, XCircle, Maximize2, Minimize2, 
    TerminalSquare, Keyboard, Mouse, MoveVertical, Command as CmdIcon
} from 'lucide-react';

// --- TypeScript Definitions ---
type ActionPayload = 
    | { type: 'move_mouse'; x: number; y: number }
    | { type: 'click'; button: 'left' | 'middle' | 'right' }
    | { type: 'type_text'; text: string }
    | { type: 'key_press'; key: string }
    | { type: 'hotkey'; key: string; modifiers: string[] }
    | { type: 'scroll'; axis: 'vertical' | 'horizontal'; amount: number }
    | { type: 'key_down'; key: string }
    | { type: 'key_up'; key: string };

type Action = ActionPayload & { id: string };

interface Command {
    uuid: string;
    name: string;
    screenshot_path: string | null;
    has_client_request: boolean;
    has_host_response: boolean;
    updated_at: string;
}

interface Props {
    activeCommand?: Command; // Matches ClientController return variable
    errors: any;
}

const SPECIAL_KEYS = ['enter', 'escape', 'tab', 'backspace', 'space', 'up', 'down', 'left', 'right', 'f5'];

export default function Index({ activeCommand, errors }: Props) {
    const [activeTab, setActiveTab] = useState('viewer');
    
    // Auth Form State
    const { data, setData, post: postAuth, processing: authProcessing } = useForm({
        uuid: '',
        token: '',
    });

    // Remote Controller State
    const [actions, setActions] = useState<Action[]>([]);
    const [textInput, setTextInput] = useState('');
    const [isPanMode, setIsPanMode] = useState(false); 
    const [isFullscreen, setIsFullscreen] = useState(false);
    const [isComposerOpen, setIsComposerOpen] = useState(false);
    
    // Hotkey State
    const [hkKey, setHkKey] = useState('');
    const [hkModifiers, setHkModifiers] = useState<string[]>([]);

    const [sendStatus, setSendStatus] = useState<'idle' | 'sending' | 'success' | 'error'>('idle');
    const [timestamp, setTimestamp] = useState(Date.now());
    const [pollIntervalMs, setPollIntervalMs] = useState(1000);
    
    const lastActiveTime = useRef(Date.now());
    const fullscreenRef = useRef<HTMLDivElement>(null);

    const toggleFullscreen = () => {
        if (!document.fullscreenElement) {
            fullscreenRef.current?.requestFullscreen().catch(err => console.error(err));
        } else {
            document.exitFullscreen();
        }
    };

    useEffect(() => {
        const handler = () => setIsFullscreen(!!document.fullscreenElement);
        document.addEventListener('fullscreenchange', handler);
        return () => document.removeEventListener('fullscreenchange', handler);
    }, []);

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

    const queueAction = (payload: ActionPayload) => {
        resetPollingTimer();
        setActions(prev => [...prev, { ...payload, id: crypto.randomUUID() }]);
    };

    const toggleModifier = (mod: string) => {
        setHkModifiers(prev => prev.includes(mod) ? prev.filter(m => m !== mod) : [...prev, mod]);
    };

    const sendPayload = () => {
        resetPollingTimer();
        if (actions.length === 0 || !activeCommand) return;
        setSendStatus('sending');
        
        // Remove ID before sending to server
        const cleanPayload = actions.map(({ id, ...rest }) => rest);
        
        // POST to public client endpoint
        router.post(`/client/commands/${activeCommand.uuid}/payload`, { payload: cleanPayload }, {
            preserveScroll: true,
            onSuccess: () => {
                setSendStatus('success');
                setActions([]);
                setTimeout(() => setSendStatus('idle'), 2000);
            },
            onError: () => {
                setSendStatus('error');
                setTimeout(() => setSendStatus('idle'), 3000);
            }
        });
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
                        <div ref={fullscreenRef} className={`relative flex-1 group bg-black rounded-xl overflow-hidden border-2 shadow-2xl transition-all duration-300 ${isFullscreen ? 'fixed inset-0 z-[100] rounded-none w-screen h-screen' : 'min-h-[500px]'}`}>
                            {activeCommand.screenshot_path ? (
                                <TransformWrapper disabled={!isPanMode} initialScale={1} minScale={1} maxScale={5}>
                                    <TransformComponent wrapperClass="w-full h-full">
                                        <div className={`relative w-full h-full flex items-center justify-center ${isPanMode ? 'cursor-grab active:cursor-grabbing' : 'cursor-crosshair'}`}
                                            onClick={(e) => {
                                                if(isPanMode) return;
                                                const rect = e.currentTarget.getBoundingClientRect();
                                                queueAction({
                                                    type: 'move_mouse',
                                                    x: Number(((e.clientX - rect.left) / rect.width).toFixed(4)),
                                                    y: Number(((e.clientY - rect.top) / rect.height).toFixed(4))
                                                });
                                                if(!isComposerOpen) setIsComposerOpen(true);
                                            }}>
                                            <img src={getImageUrl() || ''} className="max-w-full max-h-full object-contain pointer-events-none select-none" alt="Remote Screen" />
                                            {actions.map((a, i) => a.type === 'move_mouse' && (
                                                <div key={a.id} className="absolute w-6 h-6 -ml-3 -mt-3 bg-red-500 rounded-full border-2 border-white text-[10px] text-white flex items-center justify-center font-bold shadow-lg" style={{ left: `${a.x * 100}%`, top: `${a.y * 100}%` }}>{i+1}</div>
                                            ))}
                                        </div>
                                    </TransformComponent>
                                </TransformWrapper>
                            ) : (
                                <div className="w-full h-full flex flex-col items-center justify-center text-muted-foreground opacity-50 gap-2">
                                    <MonitorPlay className="h-12 w-12" />
                                    <span>Waiting for device stream...</span>
                                </div>
                            )}

                            {/* Overlays */}
                            <div className="absolute top-4 left-4 right-4 flex justify-between items-center opacity-0 group-hover:opacity-100 transition-opacity z-40">
                                <Badge variant="secondary" className="bg-black/60 text-white backdrop-blur-md">{isPanMode ? 'PANNING MODE' : 'TARGETING MODE'}</Badge>
                                <div className="flex gap-2">
                                    <Button size="sm" variant="secondary" className="bg-black/60 text-white backdrop-blur-md" onClick={() => setIsPanMode(!isPanMode)}>{isPanMode ? 'Switch to Target' : 'Switch to Pan'}</Button>
                                    <Button size="sm" variant="secondary" className="bg-black/60 text-white backdrop-blur-md" onClick={toggleFullscreen}>
                                        {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
                                    </Button>
                                </div>
                            </div>

                            {/* Composer Toggle Button */}
                            <Button onClick={() => setIsComposerOpen(!isComposerOpen)} className="absolute bottom-6 right-6 w-14 h-14 rounded-full shadow-2xl z-50 transition-transform active:scale-95" variant={isComposerOpen ? "destructive" : "default"}>
                                {isComposerOpen ? <X className="h-6 w-6" /> : <TerminalSquare className="h-6 w-6" />}
                                {actions.length > 0 && !isComposerOpen && <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white border-2 border-background">{actions.length}</span>}
                            </Button>

                            {/* Full Composer Panel */}
                            {isComposerOpen && (
                                <div className="absolute bottom-24 right-6 w-[350px] max-h-[80%] bg-background/95 backdrop-blur-xl border rounded-2xl shadow-2xl flex flex-col z-50 overflow-hidden animate-in slide-in-from-bottom-5">
                                    <div className="p-4 border-b flex justify-between items-center bg-muted/30">
                                        <span className="text-sm font-bold flex items-center gap-2"><CmdIcon className="h-4 w-4 text-primary"/> Action Queue ({actions.length})</span>
                                        <Button variant="ghost" size="sm" onClick={() => setActions([])} className="h-7 text-[10px]"><Trash2 className="h-3 w-3 mr-1"/> Clear</Button>
                                    </div>
                                    
                                    <div className="flex-1 overflow-y-auto p-4 space-y-2 min-h-[100px] max-h-[200px] custom-scrollbar">
                                        {actions.length === 0 ? <div className="text-center py-6 text-xs text-muted-foreground italic border-2 border-dashed rounded-lg">Queue is empty.<br/>Click the screen or use tabs below.</div> : 
                                            actions.map((a, i) => (
                                                <div key={a.id} className="text-[10px] flex justify-between items-center bg-muted/50 border p-2 rounded">
                                                    <div className="flex items-center gap-2 truncate">
                                                        <Badge className="w-4 h-4 p-0 flex items-center justify-center text-[9px]">{i+1}</Badge>
                                                        <span className="font-bold uppercase text-primary">{a.type.replace('_', ' ')}:</span>
                                                        <span className="truncate">
                                                            {a.type === 'move_mouse' && `[${Math.round(a.x * 100)}%, ${Math.round(a.y * 100)}%]` }
                                                            {a.type === 'click' && `${a.button} button`}
                                                            {a.type === 'type_text' && `"${a.text}"`}
                                                            {a.type === 'key_press' && a.key.toUpperCase()}
                                                            {a.type === 'hotkey' && `${a.modifiers.join('+')}+${a.key}`.toUpperCase()}
                                                            {a.type === 'scroll' && `${a.axis} ${a.amount}`}
                                                            {a.type === 'key_down' && `Hold ${a.key}`}
                                                            {a.type === 'key_up' && `Release ${a.key}`}
                                                        </span>
                                                    </div>
                                                    <button onClick={() => setActions(actions.filter(x => x.id !== a.id))} className="text-muted-foreground hover:text-destructive"><X className="h-3.5 w-3.5"/></button>
                                                </div>
                                            ))
                                        }
                                    </div>

                                    <Tabs defaultValue="mouse" className="p-4 border-t bg-muted/20">
                                        <TabsList className="grid grid-cols-4 h-9 mb-4">
                                            <TabsTrigger value="mouse" className="text-[10px]"><Mouse className="h-3.5 w-3.5 mr-1"/> Mouse</TabsTrigger>
                                            <TabsTrigger value="keys" className="text-[10px]"><Keyboard className="h-3.5 w-3.5 mr-1"/> Keys</TabsTrigger>
                                            <TabsTrigger value="combo" className="text-[10px]"><CmdIcon className="h-3.5 w-3.5 mr-1"/> Combo</TabsTrigger>
                                            <TabsTrigger value="scroll" className="text-[10px]"><MoveVertical className="h-3.5 w-3.5 mr-1"/> Scroll</TabsTrigger>
                                        </TabsList>
                                        
                                        <TabsContent value="mouse" className="grid grid-cols-3 gap-2 mt-0">
                                            <Button size="sm" variant="outline" className="h-9 text-[10px]" onClick={() => queueAction({type:'click', button:'left'})}>Left Click</Button>
                                            <Button size="sm" variant="outline" className="h-9 text-[10px]" onClick={() => queueAction({type:'click', button:'middle'})}>Mid Click</Button>
                                            <Button size="sm" variant="outline" className="h-9 text-[10px]" onClick={() => queueAction({type:'click', button:'right'})}>Right Click</Button>
                                        </TabsContent>
                                        
                                        <TabsContent value="keys" className="space-y-3 mt-0">
                                             <div className="flex gap-2">
                                                <Input className="h-9 text-xs" placeholder="Text to type..." value={textInput} onChange={e => setTextInput(e.target.value)} onKeyDown={e => e.key === 'Enter' && textInput && (queueAction({type:'type_text', text: textInput}), setTextInput(''))} />
                                                <Button size="sm" className="h-9 px-3" onClick={() => { if(textInput) { queueAction({type:'type_text', text: textInput}); setTextInput(''); }}}><Type className="h-4 w-4"/></Button>
                                             </div>
                                             <div className="grid grid-cols-5 gap-1.5">
                                                {SPECIAL_KEYS.map(k => <Button key={k} variant="secondary" className="h-7 text-[9px] px-1 font-medium border" onClick={() => queueAction({type:'key_press', key: k})}>{k}</Button>)}
                                             </div>
                                        </TabsContent>
                                        
                                        <TabsContent value="combo" className="space-y-3 mt-0">
                                            <div className="flex gap-1.5 flex-wrap">
                                                {['ctrl', 'alt', 'shift', 'win'].map(m => <Badge key={m} variant={hkModifiers.includes(m) ? "default" : "outline"} className="cursor-pointer uppercase text-[9px] px-2 py-0.5" onClick={() => toggleModifier(m)}>{m}</Badge>)}
                                            </div>
                                            <div className="flex gap-2">
                                                <Input className="h-9 text-xs uppercase font-bold" placeholder="Key (e.g. C, V)" value={hkKey} onChange={e => setHkKey(e.target.value)} />
                                                <Button size="sm" className="h-9 text-[10px] font-bold px-4" disabled={!hkKey} onClick={() => { queueAction({type:'hotkey', key: hkKey.toLowerCase(), modifiers: hkModifiers}); setHkKey(''); }}>Add</Button>
                                            </div>
                                        </TabsContent>

                                        <TabsContent value="scroll" className="grid grid-cols-2 gap-2 mt-0">
                                            <Button size="sm" variant="outline" className="h-9 text-[10px] font-semibold" onClick={() => queueAction({ type: 'scroll', axis: 'vertical', amount: -3 })}>Scroll Up</Button>
                                            <Button size="sm" variant="outline" className="h-9 text-[10px] font-semibold" onClick={() => queueAction({ type: 'scroll', axis: 'vertical', amount: 3 })}>Scroll Down</Button>
                                        </TabsContent>

                                        <Button className={`w-full mt-5 h-11 font-bold shadow-lg transition-all duration-300 ${sendStatus === 'success' ? 'bg-green-600 hover:bg-green-700' : sendStatus === 'error' ? 'bg-red-600 hover:bg-red-700' : 'bg-primary hover:scale-[1.02]'}`} disabled={actions.length === 0 || sendStatus === 'sending'} onClick={sendPayload}>
                                            {sendStatus === 'idle' && <><Send className="h-4 w-4 mr-2" /> EXECUTE SEQUENCE</>}
                                            {sendStatus === 'sending' && <RefreshCw className="h-4 w-4 animate-spin mr-2" />}
                                            {sendStatus === 'success' && <><CheckCircle2 className="h-4 w-4 mr-2" /> SENT</>}
                                            {sendStatus === 'error' && <><XCircle className="h-4 w-4 mr-2" /> FAILED</>}
                                        </Button>
                                    </Tabs>
                                </div>
                            )}
                        </div>
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