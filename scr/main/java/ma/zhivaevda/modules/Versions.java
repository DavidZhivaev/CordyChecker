package me.zhivaevda.modules;

import java.io.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Versions {
    public static final JSONArray result = new JSONArray();
    public static final JSONArray normal = new JSONArray();

    public static JSONArray getVersions(String directory) {
        String logsDirectory = new File(directory, "versions").getAbsolutePath();
        File folder = new File(logsDirectory);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    getJarFilesInDirectory(file);
                }
            }
        }
        return result;
    }

    private static void getJarFilesInDirectory(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getJarFilesInDirectory(file);
                } else {
                    if (file.getName().endsWith(".jar")) {
                        long sizeInKb = file.length() / 1024;
                        JSONObject res = new JSONObject();
                        res.put("name", file.getName());
                        res.put("size", sizeInKb);
                        result.put(res);
                    }
                }
            }
        }
    }
}
