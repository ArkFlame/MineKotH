package com.arkflame.minekoth.colorapi;

import net.md_5.bungee.api.chat.HoverEvent;

public enum HoverAction {
    SHOW_TEXT(HoverEvent.Action.SHOW_TEXT),
    SHOW_ITEM(HoverEvent.Action.SHOW_ITEM),
    SHOW_ENTITY(HoverEvent.Action.SHOW_ENTITY);
    
    private final HoverEvent.Action bungeeAction;

    HoverAction(HoverEvent.Action bungeeAction) {
        this.bungeeAction = bungeeAction;
    }

    public HoverEvent.Action toBungee() {
        return this.bungeeAction;
    }
}