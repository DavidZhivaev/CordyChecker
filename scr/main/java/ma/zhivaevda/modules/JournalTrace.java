package me.zhivaevda.modules;

import com.fasterxml.jackson.core.JsonParseException;
import me.zhivaevda.interfaces.Interface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import me.zhivaevda.utils.Searcher;
import me.zhivaevda.utils.TextRead;

public class JournalTrace {
    public static final List<String> KEYS_ADD = Arrays.asList("file", "rename", "delete", "basic_info", "execute", "extend", "change");

    public static final JSONArray output = new JSONArray();

    public static void main(String[] args) {
        JSONArray r = new JSONArray();
        r.put("\\users\\*.exe");
        System.out.println(getResult(r, "C:\\Users\\11180\\OneDrive\\Рабочий стол\\Чекер файлы\\UsnParser.exe").toString(4));
    }

    public static JSONArray getResult(JSONArray usnData, String exe) {
        List<String> searchUsnData = new ArrayList<>();
        for (int i = 0; i < usnData.length(); i++) {
            searchUsnData.add(usnData.getString(i));
        }
        File[] roots = File.listRoots();
        for (File root : roots) {
            String rootPath = root.toString();
            try {
                String tempDir = System.getProperty("java.io.tmpdir");
                String fileName = "usn_output-" + rootPath.replace(":", "").replace("\\", "") + ".txt";
                String filePath = tempDir + File.separator + fileName;

                String command = "\"" + exe + "\" read " + rootPath + " > \"" + filePath.replace("\\\\", "\\") + "\"";
                System.out.println(command);

                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.redirectErrorStream(true);
                Process process = builder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("You need system administrator")) {
                        JSONObject error = new JSONObject();
                        error.put("error", "You need system administrator");
                        output.put(error);
                        return output;
                    }
                }
                process.waitFor();

                String result = TextRead.getText(filePath);
                String[] newArray = result.split("(?m)^\\s*$");

                for (String a : newArray) {
                    String reason = extractValue(a, "Reason").toLowerCase();
                    String searchResMe = Searcher.search(reason, KEYS_ADD);
                    if (!searchResMe.equals("Не найдено!")) {
                        String path = extractValue(a, "Path");
                        String searchRes = Searcher.search(path, searchUsnData);
                        if (!searchRes.equals("Не найдено!")) {
                            output.put(parseUsnEntry(a, searchRes));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JSONArray merge = mergeUsnEntries(output);
        return merge;
    }

    public static JSONArray mergeUsnEntries(JSONArray original) {
        Map<String, JSONObject> mergedMap = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        for (int i = 0; i < original.length(); i++) {
            JSONObject current = original.getJSONObject(i);
            String path = current.getString("Path");
            String reason = current.getString("Reason");
            String timestampStr = current.getString("Timestamp");

            JSONObject existing = mergedMap.get(path);

            if (existing == null) {
                mergedMap.put(path, new JSONObject(current.toString()));
            } else {
                Set<String> reasons = new HashSet<>();
                reasons.addAll(Arrays.asList(existing.getString("Reason").split("\\|\\s*")));
                reasons.addAll(Arrays.asList(reason.split("\\|\\s*")));

                reasons.remove("CLOSE");

                String newReason = String.join("| ", reasons);
                existing.put("Reason", newReason.trim());

                String existingTimestampStr = existing.getString("Timestamp");

                LocalDateTime newTime = LocalDateTime.parse(timestampStr, formatter);
                LocalDateTime existingTime = LocalDateTime.parse(existingTimestampStr, formatter);

                if (newTime.isAfter(existingTime)) {
                    existing.put("Timestamp", timestampStr);
                }
            }
        }

        return new JSONArray(mergedMap.values());
    }

    private static JSONObject parseUsnEntry(String entry, String part) {
        JSONObject json = new JSONObject();
        json.put("Path", extractValue(entry, "Path"));
        json.put("Timestamp", extractValue(entry, "Timestamp").replace(" +03:00", ""));
        json.put("Type", extractValue(entry, "Type"));
        json.put("Reason", extractValue(entry, "Reason"));
        json.put("Triger", part);
        return json;
    }

    private static String extractValue(String text, String key) {
        String pattern = key + "\\s*:\\s*(.+)";
        for (String line : text.split("\n")) {
            if (line.startsWith(key)) {
                return line.replaceFirst(pattern, "$1").trim();
            }
        }
        return "Not found";
    }
}
