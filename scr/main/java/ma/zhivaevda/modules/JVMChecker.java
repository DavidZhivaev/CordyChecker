package me.zhivaevda.modules;

import me.zhivaevda.utils.Searcher;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import me.zhivaevda.utils.Searcher.*;

public class JVMChecker {
    public static final JSONArray parts = new JSONArray();
    public static final List<File> jcmds = new ArrayList<>();
    public static final List<File> ips = new ArrayList<>();

    public static JSONObject getResult(JSONArray search) {
        List<String> searcher = Searcher.normalEvery(search);
        JSONObject jsonOutput = new JSONObject();
        try {
            getFilesJCMDPath();
            if (jcmds.isEmpty()) {
                return getErrorJson("Не найден файл JCMD");
            } else if (ips.isEmpty()) {
                return getErrorJson("Не найден файл IPS");
            }

            String jcmdFile = getJcmdPath();
            String ipsFile = getIpsPath();

            String command = "\"" + ipsFile + "\"";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] pidParts = line.split(" ");
                    String pid = pidParts[0];
                    String commandJCMD = "\"" + jcmdFile + "\" " + pid + " VM.class_hierarchy";
                    Process processJCMD = Runtime.getRuntime().exec(commandJCMD);

                    BufferedReader readerJCMD = new BufferedReader(new InputStreamReader(processJCMD.getInputStream(), "Cp866"));
                    String lineJCMD;
                    while ((lineJCMD = readerJCMD.readLine()) != null) {
                        String myLine = lineJCMD.replaceAll("^\\|.*?--", "").toLowerCase();
                        String res = Searcher.search(myLine, searcher);
                        if (!res.equals("Не найдено!")) {
                            parts.put(myLine);
                        }
                    }
                    processJCMD.waitFor();
                } catch (Exception e) {
                    // poxui
                }
            }
            process.waitFor();

            jsonOutput.put("JVM", parts);

        } catch (Exception e) {
            return getErrorJson("Внутренняя ошибка");
        }
        return jsonOutput;
    }

    public static JSONObject getErrorJson(String error) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", error);
        return errorJson;
    }

    public static String getIpsPath() {
        return ips.get(0).getAbsolutePath();
    }

    public static String getJcmdPath() {
        return jcmds.get(0).getAbsolutePath();
    }

    public static void getFilesJCMDPath() {
        File[] roots = File.listRoots();

        for (File root : roots) {
            checkDirJcmd(root);
        }
    }

    private static void checkDirJcmd(File folder) {
        if (jcmds.isEmpty() || ips.isEmpty()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        checkDirJcmd(file);
                    } else {
                        if (file.getName().endsWith(".exe")) {
                            if (file.getAbsolutePath().contains("\\bin\\jcmd.exe")) {
                                jcmds.add(file);
                            } else if (file.getAbsolutePath().contains("\\bin\\jps.exe")) {
                                ips.add(file);
                            }
                        }
                    }
                }
            }
        }
    }
}
