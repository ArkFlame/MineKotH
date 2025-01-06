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

    public String getCode() {
        return code;
    }
}
