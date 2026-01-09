package me.zhivaevda.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;

import org.json.JSONObject;
import org.json.JSONArray;

public class Servises {
    public static void main(String[] args) {
        System.out.println(getServises().toString(4));
    }

    public static JSONObject getServises() {
        try {
            JSONObject res = new JSONObject();
            String command = "powershell -Command \"get-service | findstr -i \"pcasvc\"; get-service | findstr -i \"DPS\"; get-service | findstr -i \"sysmain\"; get-service | findstr -i \"eventlog\"; get-service \"bam\";\"";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
            String line;
            while ((line = reader.readLine()) != null) {
                String regex = "(Running|Stopped)\\s+(\\S+)\\s+(.*)";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String status = matcher.group(1);
                    String serviceName = matcher.group(2);
                    String statusInRussian = status.equals("Running") ? "Работает" : "Отключен";
                    res.put(serviceName, statusInRussian);
                }
            }
            process.waitFor();
            return res;
        } catch (Exception e) {
            JSONObject res = new JSONObject();
            return res;
        }
    }
}
