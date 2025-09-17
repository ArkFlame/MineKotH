package com.arkflame.minekoth.colorapi;

import com.arkflame.minekoth.colorapi.util.ColorWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Internal representation of a single, styled piece of text.
 * This is not meant for direct use; it's a data carrier for the ColorAPI that
 * uses the version-agnostic ColorWrapper.
 */
class InternalTextComponent {
    private final String text;
    private final ColorWrapper color;
    private final boolean bold, italic, underlined, strikethrough, magic;

    private HoverEvent hoverEvent;
    private ClickEvent clickEvent;

    InternalTextComponent(String text, ColorWrapper color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean magic) {
        this.text = text;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.magic = magic;
    }

    // Getters and setters for internal use by ColorAPI
    String getText() { return text; }
    HoverEvent getHoverEvent() { return hoverEvent; }
    ClickEvent getClickEvent() { return clickEvent; }
    void setHoverEvent(HoverEvent hoverEvent) { this.hoverEvent = hoverEvent; }
    void setClickEvent(ClickEvent clickEvent) { this.clickEvent = clickEvent; }

    /**
     * Converts this internal component into a BungeeCord BaseComponent for sending to a player.
     * @return A BaseComponent representing this piece of text.
     */
    BaseComponent toBungee() {
        TextComponent component = new TextComponent(text);
        if (color != null) {
            // Use the wrapper to get the correct Bungee ChatColor for the current server version.
            component.setColor(color.toBungee());
        }
        component.setBold(bold);
        component.setItalic(italic);
        component.setUnderlined(underlined);
        component.setStrikethrough(strikethrough);
        component.setObfuscated(magic);
        
        component.setHoverEvent(hoverEvent);
        component.setClickEvent(clickEvent);
        return component;
    }

    /**
     * Converts this component into a legacy string with '§' color codes.
     * This is now version-agnostic for hex colors.
     * @return A plain string with legacy formatting.
     */
    String toLegacyText() {
        StringBuilder sb = new StringBuilder();
        if (color != null) {
            // Use the wrapper to get the correct legacy string representation.
            sb.append(color.toLegacyString());
        }
        if (bold) sb.append(ChatColor.BOLD);
        if (italic) sb.append(ChatColor.ITALIC);
        if (underlined) sb.append(ChatColor.UNDERLINE);
        if (strikethrough) sb.append(ChatColor.STRIKETHROUGH);
        if (magic) sb.append(ChatColor.MAGIC);
        
        sb.append(text);
        return sb.toString();
    }
}