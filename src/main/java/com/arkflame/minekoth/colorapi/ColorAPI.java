package com.arkflame.minekoth.colorapi;

import com.arkflame.minekoth.colorapi.util.ColorWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A self-contained, fluent API for creating and sending rich text messages.
 * This API is fully version-agnostic (1.8-1.21+).
 * Hex codes are supported on modern servers and gracefully degrade to the nearest
 * legacy color on older servers.
 */
public class ColorAPI {
    // Combined pattern for all special formats we handle.
    // Group 1: Gradient Start | Group 2: Gradient End | Group 3: Single Hex | Group 4: Legacy
    private static final Pattern FORMATTING_PATTERN = Pattern.compile(
            "<#([A-Fa-f0-9]{6})>|" +   // Gradient Start (Group 1)
            "</#([A-Fa-f0-9]{6})>|" +  // Gradient End (Group 2)
            "&#([A-Fa-f0-9]{6})|" +    // Single Hex (Group 3)
            "&[0-9A-FK-ORa-fk-or]"      // Legacy Code (Group 4)
    );

    private final List<InternalTextComponent> components = new ArrayList<>();

    private ColorAPI() {}

    public static ColorAPI create() {
        return new ColorAPI();
    }

    public static ColorAPI colorize(String text) {
        return create().append(text);
    }

    public ColorAPI append(String text) {
        if (text != null && !text.isEmpty()) {
            this.components.addAll(parse(text));
        }
        return this;
    }

    public ColorAPI append(ColorAPI other) {
        if (other != null) {
            this.components.addAll(other.components);
        }
        return this;
    }

    public ColorAPI onHover(HoverAction action, ColorAPI content) {
        if (!components.isEmpty()) {
            getLastAppendedGroup().forEach(c -> c.setHoverEvent(new HoverEvent(
                action.toBungee(),
                content.toBungeeComponents()
            )));
        }
        return this;
    }

    public ColorAPI onHover(String hoverText) {
        return onHover(HoverAction.SHOW_TEXT, ColorAPI.colorize(hoverText));
    }

    public ColorAPI onClick(ClickAction action, String value) {
        if (!components.isEmpty()) {
            getLastAppendedGroup().forEach(c -> c.setClickEvent(new ClickEvent(
                action.toBungee(),
                value
            )));
        }
        return this;
    }
    
    private List<InternalTextComponent> getLastAppendedGroup() {
        if (components.isEmpty()) {
            return new ArrayList<>();
        }
        int lastIndex = components.size() - 1;
        InternalTextComponent last = components.get(lastIndex);
        HoverEvent h = last.getHoverEvent();
        ClickEvent c = last.getClickEvent();
        
        List<InternalTextComponent> group = new ArrayList<>();
        for (int i = lastIndex; i >= 0; i--) {
            InternalTextComponent current = components.get(i);
            if (current.getHoverEvent() == h && current.getClickEvent() == c) {
                group.add(current);
                if (current.getText().contains(" ")) {
                    break;
                }
            } else {
                break;
            }
        }
        return group;
    }

    public void send(CommandSender sender) {
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(toBungeeComponents());
        } else {
            sender.sendMessage(toLegacyText());
        }
    }

    public String toLegacyText() {
        return components.stream().map(InternalTextComponent::toLegacyText).collect(Collectors.joining());
    }

    public BaseComponent[] toBungeeComponents() {
        return components.stream().map(InternalTextComponent::toBungee).toArray(BaseComponent[]::new);
    }

    private static List<InternalTextComponent> parse(String text) {
        List<InternalTextComponent> components = new ArrayList<>();
        Matcher matcher = FORMATTING_PATTERN.matcher(text);
        TextColorizer state = new TextColorizer();
        int lastMatchEnd = 0;

        while (matcher.find()) {
            int start = matcher.start();
            if (start > lastMatchEnd) {
                state.appendText(text.substring(lastMatchEnd, start), components);
            }

            String match = matcher.group();
            if (match.startsWith("<#") && !match.startsWith("</#")) {
                state.beginGradient(new ColorWrapper(new Color(Integer.parseInt(matcher.group(1), 16))));
            } else if (match.startsWith("</#")) {
                state.endGradient(new ColorWrapper(new Color(Integer.parseInt(matcher.group(2), 16))));
            } else if (match.startsWith("&#")) {
                state.setColor(new ColorWrapper(new Color(Integer.parseInt(matcher.group(3), 16))));
            } else {
                state.applyLegacyCode(ChatColor.getByChar(match.charAt(1)));
            }
            lastMatchEnd = matcher.end();
        }
        if (lastMatchEnd < text.length()) {
            state.appendText(text.substring(lastMatchEnd), components);
        }
        return components;
    }

    private static Color interpolate(Color color1, Color color2, float factor) {
        int r = (int) (color1.getRed() + factor * (color2.getRed() - color1.getRed()));
        int g = (int) (color1.getGreen() + factor * (color2.getGreen() - color1.getGreen()));
        int b = (int) (color1.getBlue() + factor * (color2.getBlue() - color1.getBlue()));
        return new Color(r, g, b);
    }

    private static class TextColorizer {
        private ColorWrapper color = new ColorWrapper(ChatColor.WHITE);
        private boolean bold, italic, underlined, strikethrough, magic;
        private ColorWrapper gradientStart, gradientEnd;
        private boolean inGradient = false;

        void setColor(ColorWrapper color) {
            if (!inGradient) {
                this.color = color;
                resetFormatting();
            }
        }

        /**
         * Correctly handles legacy codes based on their character, not their properties.
         * This version is compatible with ChatColor as a class.
         */
        void applyLegacyCode(ChatColor code) {
            if (code == null) return;
            
            // This is the definitive, safe way to check for a color code.
            char codeChar = code.toString().charAt(1);
            boolean isColor = (codeChar >= '0' && codeChar <= '9') || (codeChar >= 'a' && codeChar <= 'f');

            if (isColor) {
                setColor(new ColorWrapper(code));
            } else { // It's a formatting code.
                // We must use if/else if because ChatColor is not an enum.
                if (code.equals(ChatColor.BOLD)) {
                    bold = true;
                } else if (code.equals(ChatColor.ITALIC)) {
                    italic = true;
                } else if (code.equals(ChatColor.UNDERLINE)) {
                    underlined = true;
                } else if (code.equals(ChatColor.STRIKETHROUGH)) {
                    strikethrough = true;
                } else if (code.equals(ChatColor.MAGIC)) {
                    magic = true;
                } else if (code.equals(ChatColor.RESET)) {
                    this.color = new ColorWrapper(ChatColor.WHITE);
                    resetFormatting();
                    this.inGradient = false;
                }
            }
        }

        void beginGradient(ColorWrapper start) { this.inGradient = true; this.gradientStart = start; }
        void endGradient(ColorWrapper end) { this.gradientEnd = end; }

        void appendText(String text, List<InternalTextComponent> components) {
            if (inGradient) {
                ColorWrapper finalEndColor = (gradientEnd != null) ? gradientEnd : gradientStart;
                Color start = gradientStart.getColor();
                Color end = finalEndColor.getColor();
                String cleanText = ChatColor.stripColor(text);
                int len = cleanText.length();
                for (int i = 0; i < len; i++) {
                    float factor = (len > 1) ? (float) i / (len - 1) : 0;
                    Color interpolated = interpolate(start, end, factor);
                    components.add(new InternalTextComponent(
                        String.valueOf(cleanText.charAt(i)), new ColorWrapper(interpolated), 
                        bold, italic, underlined, strikethrough, magic)
                    );
                }
                inGradient = false;
                gradientStart = null;
                gradientEnd = null;
            } else {
                components.add(new InternalTextComponent(
                    text, color, bold, italic, underlined, strikethrough, magic));
            }
        }
        
        private void resetFormatting() {
            bold = italic = underlined = strikethrough = magic = false;
        }
    }
}