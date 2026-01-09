package me.zhivaevda.modules;

import org.json.JSONArray;
import java.io.*;
import java.util.regex.*;

public class PrefetchView {
    public static JSONArray getPrefetchView(String exePath, String pfPath) {
        File exe = new File(exePath);
        String newExePath = exe.getAbsolutePath();
        File pf = new File(pfPath);
        String newPfPath = pf.getAbsolutePath();
        JSONArray result = new JSONArray();
        String command = "\"" + newExePath + "\" -f \"" + newPfPath + "\"";

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            String line;
            String regex = "[^\\\\/]+(?:\\.[a-zA-Z0-9]+)$";
            Pattern pattern = Pattern.compile(regex);
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(".jar") || line.toLowerCase().contains(".exe") || line.toLowerCase().contains(".bat")) {
                    if (line.toLowerCase().contains("\\volume")) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            result.put(matcher.group(0));
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            result.put("error: " + e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) {
        String exePath = "C:\\Users\\11180\\OneDrive\\Рабочий стол\\бинарники чекера\\PECmd.exe";
        String pfPath = "C:\\Windows\\Prefetch\\JAVAW.EXE-FE8EBDCA.pf";

        JSONArray output = getPrefetchView(exePath, pfPath);
        System.out.println(output.toString(4));
    }
}
