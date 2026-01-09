package me.zhivaevda.modules;

import me.zhivaevda.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BrowsersDownloads {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String DOWNLOADS_PATH = USER_HOME + "\\Downloads\\browser_copy.db";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private static final Map<String, String> BROWSER_PATHS = new HashMap<>();

    static {
        BROWSER_PATHS.put("chrome", USER_HOME + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History");
        BROWSER_PATHS.put("firefox", USER_HOME + "\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles");
        BROWSER_PATHS.put("edge", USER_HOME + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\History");
        BROWSER_PATHS.put("opera", USER_HOME + "\\AppData\\Roaming\\Opera Software\\Opera Stable\\History");
        BROWSER_PATHS.put("brave", USER_HOME + "\\AppData\\Local\\BraveSoftware\\Brave-Browser\\User Data\\Default\\History");
        BROWSER_PATHS.put("vivaldi", USER_HOME + "\\AppData\\Local\\Vivaldi\\User Data\\Default\\History");
        BROWSER_PATHS.put("waterfox", USER_HOME + "\\AppData\\Roaming\\Waterfox\\Profiles");
        BROWSER_PATHS.put("ie", USER_HOME + "\\AppData\\Local\\Microsoft\\Internet Explorer");
        BROWSER_PATHS.put("safari", USER_HOME + "\\AppData\\Roaming\\Apple Computer\\Safari");
        BROWSER_PATHS.put("yandex", USER_HOME + "\\AppData\\Local\\Yandex\\YandexBrowser\\User Data\\Default\\History");
        BROWSER_PATHS.put("seamonkey", USER_HOME + "\\AppData\\Roaming\\Mozilla\\SeaMonkey\\Profiles");
        BROWSER_PATHS.put("pale_moon", USER_HOME + "\\AppData\\Roaming\\PaleMoon\\Profiles");
        BROWSER_PATHS.put("internet_explorer", USER_HOME + "\\AppData\\Local\\Microsoft\\Internet Explorer");
    }

    private final List<String> keywords;

    public BrowsersDownloads(List<String> keywords) {
        this.keywords = keywords;
    }

    private String copyBrowserHistory(String browserPath) {
        try {
            File source = new File(browserPath);
            File destination = new File(DOWNLOADS_PATH);

            if (source.exists()) {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return destination.getAbsolutePath();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private JSONArray getDownloads(String browserPath) {
        JSONArray downloads = new JSONArray();
        String copiedDbPath = copyBrowserHistory(browserPath);

        if (copiedDbPath == null) return downloads;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + copiedDbPath);
             PreparedStatement stmt = conn.prepareStatement("SELECT target_path, start_time FROM downloads ORDER BY start_time DESC");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String targetPath = rs.getString("target_path").toLowerCase();
                long startTime = rs.getLong("start_time");

                LocalDateTime downloadTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime / 1000), ZoneId.systemDefault());

                String res = Searcher.search(targetPath, keywords);
                if (!res.equals("Не найдено!")) {
                    JSONObject download = new JSONObject();
                    download.put("file", targetPath);
                    download.put("time", downloadTime.format(FORMATTER));
                    downloads.put(download);
                }
            }
        } catch (Exception ignored) {}

        return downloads;
    }

    public JSONObject getAllDownloads() {
        JSONObject browserDownloads = new JSONObject();
        boolean hasDownloads = false;

        for (Map.Entry<String, String> entry : BROWSER_PATHS.entrySet()) {
            JSONArray downloads = getDownloads(entry.getValue());
            if (!downloads.isEmpty()) {
                browserDownloads.put(entry.getKey(), downloads);
                hasDownloads = true;
            }
        }

        return hasDownloads ? browserDownloads : new JSONObject();
    }

    public static JSONObject getBrowsersDownloads(List<String> keywords) {
        BrowsersDownloads browsersDownloads = new BrowsersDownloads(keywords);
        return browsersDownloads.getAllDownloads();
    }
}
