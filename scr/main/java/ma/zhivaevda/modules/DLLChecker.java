package me.zhivaevda.modules;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DLLChecker {
    public static JSONArray getDlls(String processName) {
        HashSet<String> my = new HashSet<>();
        JSONArray result = new JSONArray();
        try {
            String command = "powershell -NoProfile -Command \"(Get-Process -Name " + processName + ").Modules | " +
                    "Where-Object { $_.ModuleName -like '*.dll' -and " +
                    "($_.FileName -notlike 'C:\\\\Windows\\\\System32\\\\*' -and " +
                    "$_.FileName -notlike 'C:\\\\Windows\\\\SysWOW64\\\\*' -and " +
                    "$_.FileName -notlike 'C:\\\\Windows\\\\WinSxS\\\\*' -and " +
                    "$_.FileName -notlike 'C:\\\\Windows\\\\Microsoft.NET\\\\*') } | " +
                    "ForEach-Object { " +
                    "$item = Get-Item $_.FileName -ErrorAction SilentlyContinue; " +
                    "[PSCustomObject]@{ " +
                    "Path = $_.FileName; " +
                    "SizeKB = if ($item) { '{0:N2}' -f ($item.Length / 1KB) } else { 'N/A' }; " +
                    "Description = if ($item) { $item.VersionInfo.FileDescription } else { 'N/A' } " +
                    "} }\"";

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {

                String line;
                Pattern dllPattern = Pattern.compile(".*\\.dll", Pattern.CASE_INSENSITIVE);

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = dllPattern.matcher(line.trim());
                    if (matcher.matches() && !my.contains(line.trim())) {
                        my.add(line.trim());
                        JSONObject resTemp = new JSONObject();
                        File file = new File(line.trim());
                        String path = file.getAbsolutePath();
                        double sizeKB = file.length() / 1024.0;
                        double roundedSize = Math.round(sizeKB * 100.0) / 100.0;

                        resTemp.put("path", path);
                        resTemp.put("size", roundedSize + " KB");

                        result.put(resTemp);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        JSONArray result = getDlls("javaw");
        for (Object res : result) {
            System.out.println(res);
        }
    }
}