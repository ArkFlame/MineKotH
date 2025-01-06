package com.arkflame.minekoth.lang;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LangManager {

    private final Map<String, Lang> languages = new HashMap<>();
    private final Lang defaultLang;
    private final File langFolder;

    public LangManager(File dataFolder) {
        this.langFolder = new File(dataFolder, "lang");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            Bukkit.getLogger().log(Level.WARNING, "Could not create lang folder: " + langFolder.getAbsolutePath());
        }
        copyDefaultLanguages();
        loadLanguages();

        // Set default language (e.g., English)
        defaultLang = languages.getOrDefault("en", new Lang("en", new HashMap<>()));
    }

    private void copyDefaultLanguages() {
        String[] defaultLangFiles = {"en.yml", "es.yml"}; // List of default language files in resources
        for (String fileName : defaultLangFiles) {
            createFileFromResource("lang/" + fileName, new File(langFolder, fileName));
        }
    }

    private void createFileFromResource(String resourcePath, File targetFile) {
        if (!targetFile.exists()) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    Files.copy(inputStream, targetFile.toPath());
                    Bukkit.getLogger().log(Level.INFO, "Default language file created: " + targetFile.getName());
                } else {
                    Bukkit.getLogger().log(Level.WARNING, "Resource not found: " + resourcePath);
                }
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to create default language file: " + targetFile.getName(), e);
            }
        }
    }

    private void loadLanguages() {
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            Bukkit.getLogger().log(Level.WARNING, "No language files found in folder: " + langFolder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            String langCode = file.getName().replace(".yml", "");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
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
                messages.put(fullPath, config.getString(fullPath));
            }
        }
    }

    public Lang getLang(Player player) {
        String locale = getPlayerLocale(player);
        return languages.getOrDefault(locale, defaultLang);
    }

    private String getPlayerLocale(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object locale = handle.getClass().getField("locale").get(handle);
            return locale.toString().split("_")[0]; // e.g., "en_US" -> "en"
        } catch (Exception e) {
            return "en"; // Default to English if locale cannot be determined
        }
    }
}
