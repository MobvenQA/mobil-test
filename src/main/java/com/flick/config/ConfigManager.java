package com.flick.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private static JSONObject envConfig;

    private static final ThreadLocal<String> threadEnvironment = ThreadLocal.withInitial(() -> "local");
    private static final ThreadLocal<String> threadPlatform = ThreadLocal.withInitial(() -> "ios");
    private static final ThreadLocal<Integer> threadDeviceIndex = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<String> threadAppKey = new ThreadLocal<>();

    static {
        try (InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream("environment.json")) {
            if (stream == null) throw new RuntimeException("environment.json bulunamadı!");
            envConfig = new JSONObject(readStream(stream));
            System.out.println("[ConfigManager] environment.json yüklendi.");
        } catch (IOException e) {
            throw new RuntimeException("ConfigManager yüklenemedi: " + e.getMessage());
        }
    }

    private static String readStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
        }
        return sb.toString();
    }

    /** SuiteHooks / CLI tarafından environment & platform & device index & appKey set edilir */
    public static void setContext(String environment, String platform, int deviceIndex) {
        threadEnvironment.set(environment.toLowerCase());
        threadPlatform.set(platform.toLowerCase());
        threadDeviceIndex.set(deviceIndex);
    }

    public static void setAppKey(String appKey) {
        threadAppKey.set(appKey);
    }

    public static String getEnvironment() {
        return threadEnvironment.get();
    }

    public static String getPlatform() {
        return threadPlatform.get();
    }

    public static int getDeviceIndex() {
        return threadDeviceIndex.get();
    }

    public static String getAppKey() {
        return threadAppKey.get();
    }

    /** Device bilgileri */
    public static JSONObject getDevice() {
        String env = getEnvironment();
        String platform = getPlatform();

        JSONObject platformConfig = envConfig
                .getJSONObject("environments")
                .getJSONObject(env)
                .getJSONObject("platforms")
                .getJSONObject(platform);

        JSONArray devices = platformConfig.optJSONArray("devices");
        int index = getDeviceIndex();

        if (devices == null || index >= devices.length()) {
            throw new RuntimeException("Device index bulunamadı: env=" + env + ", platform=" + platform + ", index=" + index);
        }

        return devices.getJSONObject(index);
    }

    public static String getDeviceName() {
        return getDevice().optString("deviceName", "");
    }

    public static String getDeviceUdid() {
        return getDevice().optString("udid", "");
    }

    public static String getDeviceVersion() {
        return getDevice().optString("platformVersion", "");
    }

    public static int getDeviceGw() {
        return getDevice().optInt("gw", 0);
    }

    /** App bilgileri */
    public static JSONObject getApp() {
        String appKey = getAppKey();
        if (appKey == null) throw new RuntimeException("App key henüz set edilmedi!");
        return getApp(appKey);
    }
    private static JSONObject findKeyIgnoreCase(JSONObject obj, String key) {
        if (obj == null || key == null) return null;
        if (obj.has(key)) return obj.getJSONObject(key);          // birebir
        for (String k : obj.keySet()) {                           // case-insensitive
            if (k.equalsIgnoreCase(key)) return obj.getJSONObject(k);
        }
        return null;
    }
    public static JSONObject getApp(String appKey) {
        String env = getEnvironment();
        String platform = getPlatform();

        // 1) Yeni şema (env -> platforms -> platform -> apps)
        JSONObject envObj = envConfig.getJSONObject("environments").getJSONObject(env);
        JSONObject appsUnderPlatform = null;
        if (envObj.has("platforms")) {
            JSONObject platformConfig = envObj.getJSONObject("platforms").getJSONObject(platform);
            appsUnderPlatform = platformConfig.optJSONObject("apps");
        }
        JSONObject candidate = findKeyIgnoreCase(appsUnderPlatform, appKey);
        if (candidate != null) return candidate;

        // 2) GERİYE DÖNÜK: Top-level "apps"
        JSONObject globalApps = envConfig.optJSONObject("apps");
        candidate = findKeyIgnoreCase(globalApps, appKey);
        if (candidate != null) return candidate;

        throw new RuntimeException("App bulunamadı: " + appKey + " (env=" + env + ", platform=" + platform + ")");
    }
    public static String getAppBundleId() {
        return getApp().optString("bundleId", "");
    }

    public static String getAppCloudPath() {
        return getApp().optString("cloudAppPath", "");
    }

    public static String getAppLocalPath() {
        return getApp().optString("localAppPath", "");
    }

    public static String getAppName() {
        return getApp().optString("appName", "");
    }

    /** MomentumSuite (Cloud) için opsiyonel options objesi */
    public static JSONObject getMomentumOptions() {
        JSONObject creds = getCloudCredentials();
        JSONObject opts = new JSONObject();
        opts.put("gw", getDeviceGw());
        opts.put("user", creds.optString("user", ""));
        opts.put("token", creds.optString("token", ""));
        return opts;
    }

    public static JSONObject getCloudCredentials() {
        String env = getEnvironment();
        JSONObject envObj = envConfig.getJSONObject("environments").getJSONObject(env);
        return envObj.optJSONObject("momentum") != null ? envObj.getJSONObject("momentum") : new JSONObject();
    }

    public static String getCloudServerUrl() {
        JSONObject creds = getCloudCredentials();
        return creds.optString("serverUrl", "https://console.momentumsuite.com/gateway/wd/hub");
    }

    /** WDA ve Webkit için benzersiz port hesaplama */
    public static int getPortOffset(int baseOffset) {
        return getDeviceGw() + 4000 + baseOffset;
    }
    public static String getDefaultAppKey() {
        String env = getEnvironment();
        String platform = getPlatform();
        JSONObject envObj = envConfig.getJSONObject("environments").getJSONObject(env);
        if (envObj.has("platforms")) {
            JSONObject platformConfig = envObj.getJSONObject("platforms").getJSONObject(platform);
            JSONObject apps = platformConfig.optJSONObject("apps");
            if (apps != null && apps.length() > 0) {
                // İlk appKey'i döndür
                return apps.keys().next();
            }
        }
        return null;
    }
}
