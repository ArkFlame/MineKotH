package com.arkflame.minekoth.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.lang.Lang;

import java.net.HttpURLConnection;

public class DiscordHook {

    private static String webhookUrl;

    public static void init(String webhookUrl) {
        DiscordHook.webhookUrl = webhookUrl;
    }

    public static void sendKothStart(String kothName) {
        Lang lang = MineKoth.getInstance().getLangManager().getLang(null); // Use null for global messages
        String title = lang.getMessage("discord.koth-started-title");
        String description = lang.getMessage("discord.koth-started-description")
                .replace("<koth_name>", kothName);
        int color = 0x1F8B4C; // A nice green color.

        sendDiscordMessage(title, description, color);
    }

    public static void sendKothTimeLimit(String kothName) {
        Lang lang = MineKoth.getInstance().getLangManager().getLang(null);
        String title = lang.getMessage("discord.koth-time-limit-title");
        String description = lang.getMessage("discord.koth-time-limit-description")
                .replace("<koth_name>", kothName);
        int color = 0xE67E22; // Orange color.

        sendDiscordMessage(title, description, color);
    }

    public static void sendKothCaptured(String kothName, String playerName) {
        Lang lang = MineKoth.getInstance().getLangManager().getLang(null);
        String title = lang.getMessage("discord.koth-captured-title");
        String description = lang.getMessage("discord.koth-captured-description")
                .replace("<koth_name>", kothName)
                .replace("<player_name>", playerName);
        int color = 0x9B59B6; // Purple color.

        sendDiscordMessage(title, description, color);
    }

    private static void sendDiscordMessage(String title, String description, int color) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            MineKoth.getInstance().getLogger().warning("Discord webhook URL is not configured.");
            return;
        }

        FoliaAPI.runTaskAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(webhookUrl);
                String jsonPayload = buildJsonPayload(title, description, color);
                StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);

                request.setEntity(entity);

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                        MineKoth.getInstance().getLogger().warning("Failed to send Discord webhook: HTTP " + responseCode);
                    }
                }
            } catch (Exception e) {
                MineKoth.getInstance().getLogger().severe("Error sending Discord webhook: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static String buildJsonPayload(String title, String description, int color) {
        String timestamp = java.time.Instant.now().toString();
        return "{"
                + "\"embeds\":[{"
                + "\"title\":\"" + escapeJson(title) + "\","
                + "\"description\":\"" + escapeJson(description) + "\","
                + "\"color\":" + color + ","
                + "\"timestamp\":\"" + timestamp + "\""
                + "}]"
                + "}";
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static void shutdown() {
        DiscordHook.webhookUrl = null;
    }
}