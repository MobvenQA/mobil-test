package com.flick.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SlackReader {
    private static final String SLACK_TOKEN = System.getenv("SLACK_TOKEN");
    private static final String CHANNEL_ID = System.getenv("SLACK_CHANNEL_ID");
    public static String getSlackToken() {
        return SLACK_TOKEN;
    }
    public static String getChannelId() {
        return CHANNEL_ID;
    }
    public static String getLatestAppleCodeWithin(int waitSeconds) throws Exception {
        for (int i = 0; i < waitSeconds; i++) {
            String code = getAppleCodeFromRecentMessages();
            if (code != null && code.matches("\\d{6}")) return code;
            Thread.sleep(1000);
        }
        return null;
    }
    private static String getAppleCodeFromRecentMessages() throws Exception {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://slack.com/api/conversations.history?channel=" + getChannelId() + "&limit=5")
                .addHeader("Authorization", "Bearer " + getSlackToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new RuntimeException("Slack API Error: " + response.code());

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray messages = json.getAsJsonArray("messages");

            for (int i = 0; i < messages.size(); i++) {
                JsonObject message = messages.get(i).getAsJsonObject();
                if (!message.has("text")) continue;
                String text = message.get("text").getAsString();
                System.out.println("📩 Gelen Slack mesaj: " + text);
                String code = extractAppleCode(text);
                if (code != null) return code;
            }
        }
        return null;
    }
    private static String extractAppleCode(String text) {
        // Apple kod formatlarını denetle
        String[] lines = text.split("\\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("apple account code")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{6})").matcher(line);
                if (m.find()) {
                    System.out.println("🔐 Apple Kodu bulundu: " + m.group(1));
                    return m.group(1);
                }
            }
        }
        return null;
    }
}
