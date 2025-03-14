package com.arkflame.minekoth.lang;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.arkflame.mineclans.modernlib.utils.Titles;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.ChatColors;
import com.arkflame.minekoth.utils.ConfigUtil;

import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LangManager {

    private final Map<String, Lang> languages = new HashMap<>();
    private final Lang defaultLang;
    private final File langFolder;
    private final ConfigUtil configUtil;

    public LangManager(File dataFolder, ConfigUtil configUtil) {
        this.configUtil = configUtil;
        this.langFolder = new File(dataFolder, "lang");
        this.configUtil.createDirectory(langFolder);

        copyDefaultLanguages();
        loadLanguages();
        defaultLang = languages.getOrDefault("en", new Lang("en", new HashMap<>()));
    }

    private void copyDefaultLanguages() {
        String[] defaultLangFiles = { "en.yml", "es.yml" };
        for (String fileName : defaultLangFiles) {
            File targetFile = new File(langFolder, fileName);
            configUtil.copyResource("lang/" + fileName, targetFile);
        }
    }

    private void loadLanguages() {
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File file : files) {
            FileConfiguration config = configUtil.loadConfig(file);
            if (config == null)
                continue;

            String langCode = file.getName().replace(".yml", "");
            Map<String, String> messages = new HashMap<>();
            loadMessages(config, "", messages);

            languages.put(langCode, new Lang(langCode, messages));
            Bukkit.getLogger().log(Level.INFO, "Loaded language: " + langCode);
        }
    }

    private void loadMessages(FileConfiguration config, String path, Map<String, String> messages) {
        for (String key : config.getConfigurationSection(path).getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (config.isConfigurationSection(fullPath)) {
                loadMessages(config, fullPath, messages);
            } else if (config.isString(fullPath)) {
                messages.put(fullPath, ChatColors.color(config.getString(fullPath)));
            }
        }
    }

    public Lang getLang(Player player) {
        String locale = getPlayerLocale(player);
        return languages.getOrDefault(locale, defaultLang);
    }

    private String getPlayerLocale(Player player) {
        if (player == null)
            return "en";
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object locale = handle.getClass().getField("locale").get(handle);
            return locale.toString().split("_")[0]; // e.g., "en_US" -> "en"
        } catch (Exception e) {
            return "en"; // Default to English if locale cannot be determined
        }
    }

    /**
     * Sends a localized message to a player with placeholders replaced.
     *
     * @param player       The player to receive the message
     * @param key          The language key for the message
     * @param placeholders Optional array of alternating placeholder names and
     *                     values
     * @throws IllegalArgumentException If placeholders array has odd length or
     *                                  contains null placeholder names
     */
    public void sendMessage(Player player, String key, Object... placeholders) {
        // Validate parameters
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Message key cannot be null or empty");
        }

        // Get the message from language manager
        String message = getLang(player).getMessage(key);

        // If no message found for this key, don't send anything
        if (message == null || message.isEmpty()) {
            return;
        }

        // Process placeholders if any exist
        if (placeholders != null && placeholders.length > 0) {
            // Check for odd length array
            if (placeholders.length % 2 != 0) {
                throw new IllegalArgumentException("Placeholders must be provided in pairs (name, value)");
            }

            // Use StringBuilder for better performance with multiple replacements
            StringBuilder messageBuilder = new StringBuilder(message);

            // Process each placeholder
            for (int i = 0; i < placeholders.length; i += 2) {
                // Validate placeholder name
                if (placeholders[i] == null) {
                    throw new IllegalArgumentException("Placeholder name at index " + i + " cannot be null");
                }

                String placeholder = String.valueOf(placeholders[i]);
                String replacement = placeholders[i + 1] == null ? "" : String.valueOf(placeholders[i + 1]);

                // Replace all occurrences
                int placeholderIndex;
                int lastIndex = 0;

                while ((placeholderIndex = messageBuilder.indexOf(placeholder, lastIndex)) != -1) {
                    messageBuilder.replace(placeholderIndex, placeholderIndex + placeholder.length(), replacement);
                    lastIndex = placeholderIndex + replacement.length();
                }
            }

            message = messageBuilder.toString();
        }

        // Apply external placeholders and send to player
        try {
            message = PlaceholderAPI.setPlaceholders(player, message);
            player.sendMessage(message);
        } catch (Exception e) {
            // Log the error instead of letting it crash the plugin
            MineKoth.getInstance().getLogger().warning("Error sending message to player: " + message);
            e.printStackTrace();
        }
    }

    /**
     * Sends a localized action bar message to a player with placeholders replaced.
     *
     * @param player       The player to receive the action bar message
     * @param key          The language key for the message
     * @param placeholders Optional array of placeholder objects to be processed by
     *                     the language manager
     * @throws IllegalArgumentException If player is null or key is null/empty
     */
    public void sendAction(Player player, String key, Object... placeholders) {
        // Validate parameters
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Message key cannot be null or empty");
        }

        try {
            // Get the message from language manager
            String message = null;
            if (getLang(player) != null) {
                message = getLang(player).getMessage(key, placeholders);
            }

            // Only send if we have a valid message
            if (message != null && !message.isEmpty()) {
                Titles.sendActionBar(player, message);
            }
        } catch (Exception e) {
            // Log the error instead of letting it crash the plugin
            MineKoth.getInstance().getLogger().warning("Error sending action bar to player: " + e.getMessage());
        }
    }
}
