package me.zhivaevda.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import me.zhivaevda.utils.*;
import java.util.regex.*;

import org.json.JSONObject;
import org.json.JSONArray;

public class RecentPrefetch {
    public static final JSONObject result = new JSONObject();
    public static final JSONArray res = new JSONArray();

    public static JSONObject getRecentPrefetch(String exePath, List<String> every) {
        result.put("prefetch", getPrefetch(every));
        result.put("recent", getRecent());
        result.put("prefetchMiddle", getPrefetchMiddle(exePath));
        return result;
    }

    private static JSONArray getPrefetchMiddle(String exePath) {
        JSONArray nado = new JSONArray();
        nado.put("conhost.exe"); nado.put("consent.exe"); nado.put("javaw.exe"); nado.put("java.exe"); nado.put("cmd.exe");

        for (Object i : nado) {
            String part = (String) i;
            for (Object b : getPrograms(exePath, part)) {
                res.put(b);
            }
        }

        return res;
    }

    private static JSONArray getPrograms(String exePath, String query) {
        JSONArray output = new JSONArray();
        try {
            String prefetchPath = "C:\\Windows\\Prefetch";
            File prefetchDir = new File(prefetchPath);
            if (prefetchDir.exists() && prefetchDir.isDirectory()) {
                File[] files = prefetchDir.listFiles();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String realname = file.getName().toLowerCase().split("-")[0];
                            if (realname.equals(query)) {
                                System.out.println(file.getName().toLowerCase());
                                JSONObject a = new JSONObject();
                                a.put("name", file.getName());
                                a.put("files", PrefetchView.getPrefetchView(exePath, file.getAbsolutePath()));
                                Date lastModified = new Date(file.lastModified());
                                String formattedDate = sdf.format(lastModified);
                                a.put("data", formattedDate);
                                output.put(a);
                            }
                        }
                    }
                } else {
                    return output;
                }
            } else {
                return output;
            }
            return output;
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static JSONArray getRecent() {
        try {
            JSONArray res = new JSONArray();
            String command = "powershell -NoProfile -Command \"Get-ChildItem -Path \\\"$env:APPDATA\\Microsoft\\Windows\\Recent\\\" -Filter *.lnk | ForEach-Object { $s = New-Object -ComObject WScript.Shell; $shortcut = $s.CreateShortcut($_.FullName); $target = $shortcut.TargetPath; if (![string]::IsNullOrWhiteSpace($target) -and ($target -match '\\.(dll|json|cfg|exe|jar|zip|gz|pf|mp3|mp4)$')) { $time = $_.LastWriteTime.ToString('dd.MM.yyyy HH:mm:ss'); [PSCustomObject]@{Time=$time; Target=$target} } } | Format-Table -AutoSize\"";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".") && line.contains(":")) {
                    String pattern = "^(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2})\\s+(.+)$";
                    Pattern regex = Pattern.compile(pattern);

                    Matcher matcher = regex.matcher(line);
                    if (matcher.find()) {
                        JSONObject out = new JSONObject();

                        String dateTime = matcher.group(1);
                        String path = matcher.group(2).trim();

                        out.put("data", dateTime);
                        out.put("path", path);

                        res.put(out);
                    }
                }
            }
            process.waitFor();
            return res;
        } catch (Exception e) {
            JSONArray res = new JSONArray();
            return res;
        }
    }

    public static JSONArray getPrefetch(List<String> every) {
        try {
            JSONArray resPrefetch = new JSONArray();

            String prefetchPath = "C:\\Windows\\Prefetch";
            File prefetchDir = new File(prefetchPath);
            if (prefetchDir.exists() && prefetchDir.isDirectory()) {
                File[] files = prefetchDir.listFiles();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            JSONObject a = new JSONObject();
                            a.put("name", file.getName());
                            Date lastModified = new Date(file.lastModified());
                            String formattedDate = sdf.format(lastModified);
                            a.put("data", formattedDate);
                            resPrefetch.put(a);
                        }
                    }
                } else {
                    return resPrefetch;
                }
            } else {
                return resPrefetch;
            }
            return resPrefetch;
        } catch (Exception e) {
            return new JSONArray();
        }
    }
}
