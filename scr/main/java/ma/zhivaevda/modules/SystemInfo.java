package me.zhivaevda.modules;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SystemInfo {
    public static final List<String> systeminfo = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(getResult().toString(4));
    }

    public static JSONObject getResult() {
        try {
            String command = "systeminfo";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            String line;
            while ((line = reader.readLine()) != null) {
                systeminfo.add(line);
            }
            process.waitFor();
        } catch (Exception e) {
            // net user %USERNAME% | find "Членство в локальных группах"
        }

        JSONObject output = new JSONObject();
        output.put("osName", getSystem());
        output.put("musorReset", getResycle());
        output.put("osStart", getStart());
        output.put("userPriv", getUserPriv());
        output.put("osInstall", getInstall());
        return output;
    }

    public static String getSystem() {
        try {
            for (String line : systeminfo) {
                if (line.contains("Название ОС:")) {
                    String[] parts = line.split(" {22}");
                    return parts[1];
                } else if (line.contains("Имя ОС:")) {
                    String[] parts = line.split(" {34}");
                    return parts[1];
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Не найдено!";
        }
        return "Не найдено!";
    }

    public static String getStart() {
        try {
            for (String line : systeminfo) {
                if (line.contains("Время загрузки системы:")) {
                    String[] parts = line.split(" {18}");
                    return parts[1];
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Не найдено!";
        }
        return "Не найдено!";
    }

    public static String getInstall() {
        try {
            for (String line : systeminfo) {
                if (line.contains("Дата установки:")) {
                    String[] parts = line.split(" {19}");
                    return parts[1];
                } else if (line.contains("Дата исходной установки:")) {
                    String[] parts = line.split(" {17}");
                    return parts[1];
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Не найдено!";
        }
        return "Не найдено!";
    }

    public static String getResycle() {
        File[] folders = new File("C:\\$Recycle.Bin").listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            return "Не найдено!";
        }

        File newestFolder = null;
        long newestTime = Long.MIN_VALUE;

        for (File folder : folders) {
            long lastModified = folder.lastModified();
            if (lastModified > newestTime) {
                newestTime = lastModified;
                newestFolder = folder;
            }
        }

        if (newestFolder != null) {
            return getLastModifiedTime(newestFolder);
        } else {
            return "Не найдено!";
        }
    }

    public static String getUserPriv() {
        try {
            String command = "cmd /c net user %USERNAME% | find \"Членство в локальных группах\"";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" {11}");
                return parts[1].replace(" ", "").replace("*", "");
            }
            process.waitFor();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Не найдено!";
        }
        return "Не найдено!";
    }

    public static String getLastModifiedTime(File folder) {
        long lastModified = folder.lastModified();
        if (lastModified > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
            return sdf.format(lastModified);
        } else {
            return "Не найдено!";
        }
    }
}
