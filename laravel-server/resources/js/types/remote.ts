// resources/js/types/remote.ts

export type ActionPayload = 
    | { type: 'move_mouse'; x: number; y: number }
    | { type: 'click'; button: 'left' | 'middle' | 'right' }
    | { type: 'type_text'; text: string }
    | { type: 'key_press'; key: string }
    | { type: 'hotkey'; key: string; modifiers: string[] }
    | { type: 'scroll'; axis: 'vertical' | 'horizontal'; amount: number }
    | { type: 'key_down'; key: string }
    | { type: 'key_up'; key: string };

export type Action = ActionPayload & { id: string };
