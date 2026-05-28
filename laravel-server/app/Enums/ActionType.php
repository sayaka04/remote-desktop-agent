<?php

namespace App\Enums;

enum ActionType: string
{
    // Mouse Actions
    case MoveMouse = 'move_mouse';
    case Click = 'click';
    case DoubleClick = 'double_click';
    case Scroll = 'scroll';

        // Keyboard Actions
    case TypeText = 'type_text';
    case KeyPress = 'key_press';
    case KeyDown  = 'key_down';
    case KeyUp    = 'key_up';
    case Hotkey   = 'hotkey';
}
