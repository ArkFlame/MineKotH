package com.arkflame.minekoth.lang;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.utils.ChatColors;
import com.arkflame.minekoth.utils.ConfigUtil;

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
        String[] defaultLangFiles = {"en.yml", "es.yml"};
        for (String fileName : defaultLangFiles) {
            File targetFile = new File(langFolder, fileName);
            configUtil.copyResource("lang/" + fileName, targetFile);
        }
    }

    private void loadLanguages() {
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            FileConfiguration config = configUtil.loadConfig(file);
            if (config == null) continue;

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
        if (player == null) return "en";
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object locale = handle.getClass().getField("locale").get(handle);
            return locale.toString().split("_")[0]; // e.g., "en_US" -> "en"
        } catch (Exception e) {
            return "en"; // Default to English if locale cannot be determined
        }
    }
}
