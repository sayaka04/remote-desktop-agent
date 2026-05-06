<?php

namespace App\Enums;

enum ActionType: string
{
    case MoveMouse = 'move_mouse';
    case Click = 'click';
    case Scroll = 'scroll';
    case TypeText = 'type_text';
}
