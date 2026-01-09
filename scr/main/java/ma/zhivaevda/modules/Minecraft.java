package me.zhivaevda.modules;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

public class Minecraft {
    private static final Logger log = LoggerFactory.getLogger(Minecraft.class);

    public static void main(String[] args) {
        JSONArray logsData = new JSONArray();
        logsData.put("Dellarba");
        System.out.println(getResult(logsData).toString(4));
    }

    public static JSONObject getResult(JSONArray logsData) {
        String mineDir = getMinecraftDirectories();
        JSONObject logs = LogView.getLogs(mineDir, logsData);

        JSONObject outputJson = new JSONObject();
        outputJson.put("username", getMinecraftPlayerNames());
        outputJson.put("startProcess", getMinecraftStartTimes());
        outputJson.put("startExplorer", getExplorerStartTimes());
        outputJson.put("directory", mineDir);
        outputJson.put("server", getServer(mineDir));
        outputJson.put("logNames", logs.get("nicks"));
        outputJson.put("logLines", logs.get("lines"));
        outputJson.put("versions", Versions.getVersions(mineDir));
        outputJson.put("version", getNowVerion());
        outputJson.put("crashReports", getCrashReports());
        return outputJson;
    }

    public static JSONArray getCrashReports() {
        JSONArray out = new JSONArray();
        try {
            String command = "cmd /c forfiles /p \"%USERPROFILE%\\AppData\\Local\\CrashDumps\" /s /c \"cmd /c echo @fdate @ftime @path\"";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
            String line;
            while ((line = reader.readLine()) != null) {
                String real = line.replace("\"", "");
                if (real.contains(".") && real.contains(":")) {
                    String pattern = "^(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2})\\s+(.+)$";
                    Pattern regex = Pattern.compile(pattern);

                    Matcher matcher = regex.matcher(real);
                    if (matcher.find()) {
                        JSONObject outmy = new JSONObject();

                        String dateTime = matcher.group(1);
                        String path = matcher.group(2).trim();

                        outmy.put("data", dateTime);
                        outmy.put("path", path);

                        out.put(outmy);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            return out;
        }
        return out;
    }

    public static String getNowVerion() {
        List<String> result = new ArrayList<>();
        OperatingSystem os = new SystemInfo().getOperatingSystem();
        for (OSProcess process : os.getProcesses(0, null)) {
            if (process.getName().toLowerCase().contains("javaw")) {
                String[] arguments = process.getCommandLine().replace(" -", "-").split("--");
                for (int i = 0; i < arguments.length - 1; i++) {
                    String[] ver = arguments[i].split(" ");
                    if (ver[0].equals("version")) {
                        return arguments[i].replace("version ", "").replace("\"", "");
                    }
                }
            }
        }
        return "Не найдено!";
    }

    public static List<String> getMinecraftFlag(String flag) {
        List<String> result = new ArrayList<>();
        OperatingSystem os = new SystemInfo().getOperatingSystem();
        for (OSProcess process : os.getProcesses(0, null)) {
            if (process.getName().toLowerCase().contains("javaw")) {
                String[] arguments = process.getCommandLine().split(" ");
                for (int i = 0; i < arguments.length - 1; i++) {
                    if (flag.equals(arguments[i])) {
                        result.add(arguments[i + 1].replace("\"", ""));
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static String getServer(String mcDir) {
        File logFile = new File(mcDir, "logs/latest.log");
        List<String> result = new ArrayList<>();

        if (!logFile.exists()) {
            return "Сервер не найден";
        }

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains("Connected to a vanilla")) {
                    if (line.contains("Connecting to ")) {
                        String[] arguments = line.split("Connecting to ")[1].split(", ");
                        result.add(arguments[0] + ":" + arguments[1]);
                    }
                }
            }
            Collections.reverse(result);
            return result.get(0);
        } catch (Exception e) {
            return "Сервер не найден";
        }
    }

    public static String getMinecraftPlayerNames() {
        List<String> minecraftNames = getMinecraftFlag("--username");
        if (minecraftNames.isEmpty()) {
            minecraftNames.add("Не найдено!");
        }
        return minecraftNames.get(0);
    }

    public static String getMinecraftDirectories() {
        List<String> minecraftDir = getMinecraftFlag("--gameDir");
        if (minecraftDir.isEmpty()) {
            minecraftDir.add("Не найдено!");
        }
        return minecraftDir.get(0);
    }

    public static String getMinecraftStartTimes() {
        List<String> startTimes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        OperatingSystem os = new SystemInfo().getOperatingSystem();
        for (OSProcess process : os.getProcesses(0, null)) {
            if (process.getName().toLowerCase().contains("javaw")) {
                String[] arguments = process.getCommandLine().split(" ");
                for (String arg : arguments) {
                    if (arg.toLowerCase().contains("minecraft")) {
                        System.out.println(1);
                        String formattedDate = sdf.format(new Date(process.getStartTime()));
                        startTimes.add(formattedDate);
                        break;
                    }
                }
            }
        }
        if (startTimes.isEmpty()) {
            startTimes.add("Не найдено!");
        }
        return startTimes.get(0);
    }

    public static String getExplorerStartTimes() {
        List<String> startTimes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        OperatingSystem os = new SystemInfo().getOperatingSystem();
        for (OSProcess process : os.getProcesses(0, null)) {
            if (process.getName().toLowerCase().contains("explorer")) {
                String[] arguments = process.getCommandLine().split(" ");
                for (String arg : arguments) {
                    if (arg.toLowerCase().contains("explorer")) {
                        String formattedDate = sdf.format(new Date(process.getStartTime()));
                        startTimes.add(formattedDate);
                        break;
                    }
                }
            }
        }
        if (startTimes.isEmpty()) {
            startTimes.add("Не найдено!");
        }
        return startTimes.get(0);
    }
}
