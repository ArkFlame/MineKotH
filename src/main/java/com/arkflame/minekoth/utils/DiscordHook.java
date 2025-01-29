package com.arkflame.minekoth.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.arkflame.minekoth.MineKoth;

import java.net.HttpURLConnection;

public class DiscordHook {

    private static String webhookUrl;

    /**
     * Initializes the DiscordHook with configuration variables.
     *
     * @param webhookUrl The Discord webhook URL from the config.
     */
    public static void init(String webhookUrl) {
        DiscordHook.webhookUrl = webhookUrl;
    }

    /**
     * Sends a message when a KOTH event starts.
     *
     * @param kothName The name of the KOTH event.
     */
    public static void sendKothStart(String kothName) {
        String title = "🏁 KOTH Started!";
        String description = "**" + kothName + "** has begun! Rally your team and capture the hill!";
        int color = 0x1F8B4C; // A nice green color.

        sendDiscordMessage(title, description, color);
    }

    /**
     * Sends a message when a KOTH event ends due to time limit.
     *
     * @param kothName The name of the KOTH event.
     */
    public static void sendKothTimeLimit(String kothName) {
        String title = "⏰ KOTH Ended - Time's Up!";
        String description = "Unfortunately, no one captured **" + kothName + "** in time.";
        int color = 0xE67E22; // Orange color.

        sendDiscordMessage(title, description, color);
    }

    /**
     * Sends a message when a player captures the KOTH.
     *
     * @param kothName  The name of the KOTH event.
     * @param playerName The name of the player who captured the KOTH.
     */
    public static void sendKothCaptured(String kothName, String playerName) {
        String title = "🎉 KOTH Captured!";
        String description = "Congratulations to **" + playerName + "** for conquering **" + kothName + "**!";
        int color = 0x9B59B6; // Purple color.

        sendDiscordMessage(title, description, color);
    }

    /**
     * Sends a formatted Discord webhook message.
     *
     * @param title       The title of the embed.
     * @param description The description of the embed.
     * @param color       The color of the embed.
     */
// Add Apache HttpClient dependency to your project.

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


    /**
     * Builds the JSON payload for the Discord webhook message.
     *
     * @param title       The title of the embed.
     * @param description The description of the embed.
     * @param color       The color of the embed.
     * @return A String containing the JSON payload.
     */
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

    /**
     * Escapes special characters for JSON strings.
     *
     * @param text The text to escape.
     * @return The escaped text.
     */
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
