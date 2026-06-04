import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
    MonitorPlay, X, CheckCircle2, XCircle, Maximize2, Minimize2, 
    TerminalSquare, Type, Send, Keyboard, Mouse, MoveVertical, 
    Command as CmdIcon, Trash2, RefreshCw, ChevronUp, ChevronDown
} from 'lucide-react';
import { TransformWrapper, TransformComponent } from "react-zoom-pan-pinch";
import { Action, ActionPayload } from '@/types/remote';

const SPECIAL_KEYS = ['enter', 'escape', 'tab', 'backspace', 'space', 'up', 'down', 'left', 'right', 'f5'];

interface RemoteControlPanelProps {
    postEndpoint: string;
    imageUrl: string | null;
    onResetPolling: () => void;
}

export default function RemoteControlPanel({ postEndpoint, imageUrl, onResetPolling }: RemoteControlPanelProps) {
    const [actions, setActions] = useState<Action[]>([]);
    const [textInput, setTextInput] = useState('');
    const [isFullscreen, setIsFullscreen] = useState(false);
    const [isComposerOpen, setIsComposerOpen] = useState(false);
    
    // Hotkey State
    const [hkKey, setHkKey] = useState('');
    const [hkModifiers, setHkModifiers] = useState<string[]>([]);
    const [sendStatus, setSendStatus] = useState<'idle' | 'sending' | 'success' | 'error'>('idle');
    
    const fullscreenRef = useRef<HTMLDivElement>(null);
    
    // 🔥 NEW: Tracks mouse down time and coordinates to differentiate between a click and a pan/drag
    const pointerStartRef = useRef<{ x: number; y: number; time: number } | null>(null);

    const toggleFullscreen = () => {
        if (!document.fullscreenElement) {
            fullscreenRef.current?.requestFullscreen().catch(console.error);
        } else {
            document.exitFullscreen();
        }
    };

    useEffect(() => {
        const handler = () => setIsFullscreen(!!document.fullscreenElement);
        document.addEventListener('fullscreenchange', handler);
        return () => document.removeEventListener('fullscreenchange', handler);
    }, []);

    const queueAction = (payload: ActionPayload) => {
        onResetPolling();
        setActions(prev => [...prev, { ...payload, id: crypto.randomUUID() }]);
    };

    const toggleModifier = (mod: string) => {
        setHkModifiers(prev => prev.includes(mod) ? prev.filter(m => m !== mod) : [...prev, mod]);
    };

    // Function to move actions up or down in the queue
    const moveAction = (index: number, direction: 'up' | 'down') => {
        setActions(prev => {
            const newActions = [...prev];
            if (direction === 'up' && index > 0) {
                [newActions[index - 1], newActions[index]] = [newActions[index], newActions[index - 1]];
            } else if (direction === 'down' && index < newActions.length - 1) {
                [newActions[index], newActions[index + 1]] = [newActions[index + 1], newActions[index]];
            }
            return newActions;
        });
    };

    // 🔥 NEW: Sends a dummy payload to force Java Host to update the screenshot
    const forceRefreshScreenshot = () => {
        onResetPolling();
        axios.post(postEndpoint, { 
            payload: [{ type: 'scroll', axis: 'vertical', amount: 0 }] 
        }).catch(err => console.error("Failed to force refresh:", err));
    };

    const sendPayload = () => {
        onResetPolling();
        if (actions.length === 0) return;
        setSendStatus('sending');
        
        const cleanPayload = actions.map(({ id, ...rest }) => rest);
        
        axios.post(postEndpoint, { payload: cleanPayload })
            .then(() => {
                setSendStatus('success');
                setActions([]);
                setTimeout(() => setSendStatus('idle'), 2000);
            })
            .catch(() => {
                setSendStatus('error');
                setTimeout(() => setSendStatus('idle'), 3000);
            });
    };

    return (
        <div ref={fullscreenRef} className={`relative flex-1 group bg-black rounded-xl overflow-hidden border-2 shadow-2xl transition-all duration-300 ${isFullscreen ? 'fixed inset-0 z-[100] rounded-none w-screen h-screen' : 'min-h-[500px] aspect-video'}`}>
            {imageUrl ? (
                // TransformWrapper disabled prop removed to permanently enable panning
                <TransformWrapper initialScale={1} minScale={1} maxScale={5}>
                    <TransformComponent wrapperClass="w-full h-full">
                        <div className="relative w-full h-full flex items-center justify-center cursor-crosshair active:cursor-grabbing"
                            onPointerDown={(e) => {
                                // Record the exact starting coordinates and time
                                pointerStartRef.current = {
                                    x: e.clientX,
                                    y: e.clientY,
                                    time: Date.now()
                                };
                            }}
                            onPointerUp={(e) => {
                                if (!pointerStartRef.current) return;

                                const deltaX = e.clientX - pointerStartRef.current.x;
                                const deltaY = e.clientY - pointerStartRef.current.y;
                                const duration = Date.now() - pointerStartRef.current.time;
                                
                                // Calculate total physical pixel displacement
                                const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                                
                                // Clear the ref tracking immediately
                                pointerStartRef.current = null;

                                // Strict validation: Must be a quick tap (<300ms) with almost zero dragging (<5px)
                                if (distance > 5 || duration > 300) {
                                    return; // It's a drag/pan gesture! Ignore it.
                                }

                                // --- Otherwise, it's a valid targeted selection click! ---
                                const rect = e.currentTarget.getBoundingClientRect();
                                queueAction({
                                    type: 'move_mouse',
                                    x: Number(((e.clientX - rect.left) / rect.width).toFixed(4)),
                                    y: Number(((e.clientY - rect.top) / rect.height).toFixed(4))
                                });
                                
                                if (!isComposerOpen) setIsComposerOpen(true);
                            }}
                        >
                            <img src={imageUrl} className="max-w-full max-h-full object-contain pointer-events-none select-none" alt="Remote Screen" draggable="false" />
                            {actions.map((a, i) => a.type === 'move_mouse' && (
                                <div key={a.id} className="absolute w-6 h-6 -ml-3 -mt-3 bg-red-500 rounded-full border-2 border-white text-[10px] text-white flex items-center justify-center font-bold shadow-lg" style={{ left: `${a.x! * 100}%`, top: `${a.y! * 100}%` }}>{i+1}</div>
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
                <Badge variant="secondary" className="bg-black/60 text-white backdrop-blur-md flex items-center gap-2">
                    <CmdIcon className="h-3 w-3" /> 
                    <span>LIVE INTERACTION</span>
                </Badge>
                <div className="flex gap-2">
                    {/* NEW: Refresh Button */}
                    <Button size="sm" variant="secondary" className="bg-black/60 text-white backdrop-blur-md" onClick={forceRefreshScreenshot}>
                        <RefreshCw className="h-4 w-4 mr-2" /> Refresh
                    </Button>
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
                                            {a.type === 'move_mouse' && `[${Math.round(a.x! * 100)}%, ${Math.round(a.y! * 100)}%]` }
                                            {a.type === 'click' && `${a.button} button`}
                                            {a.type === 'type_text' && `"${a.text}"`}
                                            {a.type === 'key_press' && a.key?.toUpperCase()}
                                            {a.type === 'hotkey' && `${a.modifiers?.join('+')}+${a.key}`.toUpperCase()}
                                            {a.type === 'scroll' && `${a.axis} ${a.amount}`}
                                            {a.type === 'key_down' && `Hold ${a.key}`}
                                            {a.type === 'key_up' && `Release ${a.key}`}
                                        </span>
                                    </div>
                                    
                                    {/* Move Up, Move Down, and Delete buttons */}
                                    <div className="flex items-center gap-1">
                                        <button onClick={() => moveAction(i, 'up')} disabled={i === 0} className="text-muted-foreground hover:text-primary disabled:opacity-30 p-1">
                                            <ChevronUp className="h-3.5 w-3.5" />
                                        </button>
                                        <button onClick={() => moveAction(i, 'down')} disabled={i === actions.length - 1} className="text-muted-foreground hover:text-primary disabled:opacity-30 p-1">
                                            <ChevronDown className="h-3.5 w-3.5" />
                                        </button>
                                        <button onClick={() => setActions(actions.filter(x => x.id !== a.id))} className="text-muted-foreground hover:text-destructive p-1 ml-1 border-l pl-2">
                                            <X className="h-3.5 w-3.5" />
                                        </button>
                                    </div>
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
    );
}