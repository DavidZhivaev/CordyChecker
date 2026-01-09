package me.zhivaevda.modules;

import me.zhivaevda.utils.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.*;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.json.JSONObject;
import org.json.JSONArray;

public class Executer {
    public static final JSONObject result = new JSONObject();
    public static HashSet<String> myparts = new HashSet<>();
    public static HashSet<String> nemyparts = new HashSet<>();

    public static JSONArray getLastRunFiles(List<String> every) {
        HashSet<JSONObject> files = new HashSet<>();

        String[] registryPaths = {
                "Software\\Classes\\Local Settings\\Software\\Microsoft\\Windows\\Shell\\MuiCache",
                "Software\\Microsoft\\Windows\\ShellNoRoam\\MUICache",
                "Software\\Microsoft\\Windows NT\\CurrentVersion\\AppCompatFlags\\Compatibility Assistant\\Persisted",
                "Software\\Microsoft\\Windows NT\\CurrentVersion\\AppCompatFlags\\Compatibility Assistant\\Store"
        };

        for (String path : registryPaths) {
            files.addAll(getRegistryData(WinReg.HKEY_CURRENT_USER, path, every));
            files.addAll(getRegistryData(WinReg.HKEY_LOCAL_MACHINE, path, every));
        }

        return new JSONArray(files);
    }

    private static List<JSONObject> getRegistryData(WinReg.HKEY root, String keyPath, List<String> every) {
        List<JSONObject> registryData = new ArrayList<>();
        try {
            if (Advapi32Util.registryKeyExists(root, keyPath)) {
                Map<String, Object> values = Advapi32Util.registryGetValues(root, keyPath);
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    String part = entry.getKey();
                    if (!part.contains("FriendlyAppName") && !part.contains("ApplicationCompany") && !part.contains("}") && part.contains(".exe")) {
                        if (!myparts.contains(part)) {
                            myparts.add(part);
                            JSONObject out = new JSONObject();
                            out.put("path", entry.getKey());
                            File my = new File(entry.getKey());
                            try {
                                if (my.exists()) {
                                    long lastModified = my.lastModified(); // получаем время в миллисекундах
                                    Date date = new Date(lastModified); // преобразуем в объект даты
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); // формат
                                    String formattedDate = sdf.format(date);
                                    out.put("data", formattedDate);
                                } else {
                                    out.put("data", "Файл не найден!");
                                }
                            } catch (Exception e) {
                                out.put("data", "Ошибка при доступе!");
                            }

                            registryData.add(out);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при чтении " + keyPath + ": " + e.getMessage());
        }
        return registryData;
    }

    public static JSONArray getExe() {
        JSONArray result = new JSONArray();
        try {
            Process process = Runtime.getRuntime().exec("powershell -Command \"Get-Process | Where-Object { $_.Path -like '*.exe' } | ForEach-Object { if ($_.StartTime) { '{0} {1}' -f [System.IO.Path]::GetFileName($_.Path), $_.StartTime.ToString('dd.MM.yyyy HH:mm:ss') } }\"\n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (!nemyparts.contains(parts[0])) {
                    JSONObject nn = new JSONObject();
                    nn.put("name", parts[0]);
                    nn.put("data", parts[1] + " " + parts[2]);
                    result.put(nn);
                    nemyparts.add(parts[0]);
                }
            }
            reader.close();
        } catch (Exception e) {
            return result;
        }
        return result;
    }

    public static JSONObject getResult(List<String> every) {
        JSONArray executables = getExe();
        result.put("exe", executables);
        JSONArray execut = getLastRunFiles(every);
        JSONArray jsonArray = execut;
        result.put("executer", jsonArray);
        return result;
    }
}
