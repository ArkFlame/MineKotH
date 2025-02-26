package com.arkflame.minekoth.lang;

import java.util.Map;

public class Lang {

    private final String code;
    private final Map<String, String> messages;

    public Lang(String code, Map<String, String> messages) {
        this.code = code;
        this.messages = messages;
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }

    public String getMessage(String key, Object... args) {
        String message = getMessage(key);
        for (int i = 0; i < args.length; i+=2) {
            if (i + 1 >= args.length) {
                break;
            }
            message = message.replace(args[i].toString(), args[i + 1].toString());
        }
        return message;
    }

    public String getCode() {
        return code;
    }
}
