package me.zhivaevda.modules;

import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class RequestsChecker {
    public static final Set<String> parts = new HashSet<>();

    public static void main(String[] args) {
        System.out.println(getResult().toString(4));
    }

    public static JSONArray getResult() {
        JSONArray output = new JSONArray();
        try {
            String command = "for /f \"tokens=5\" %a in ('netstat -ano ^| find \"ESTABLISHED\"') do @tasklist /FI \"PID eq %a\" /FO CSV /NH | for /f \"tokens=1 delims=,\" %b in ('more') do @echo %b";

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\"", "");
                parts.add(line.trim());
                output.put(line.trim());
            }

            process.waitFor();
        } catch (Exception e) {
            return output;
        }
        for (String part : parts) {
            output.put(part);
        }
        return output;
    }
}
