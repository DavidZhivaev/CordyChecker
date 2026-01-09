package me.zhivaevda.utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ResourceExtractor {
    private static final String RESOURCE_DIRECTORY = "resources/binary/";
    private static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    // Список файлов для копирования
    private static final List<String> FILES_TO_COPY = Arrays.asList(
            "libgcc_s_seh-1.dll",
            "libstdc++-6.dll",
            "libwinpthread-1.dll",
            "PECmd.exe",
            "scannerExplorer.exe",
            "scannerJavaw.exe",
            "UsnParser.exe"
    );

    // Метод для копирования файлов во временную директорию
    public static JSONObject copyFilesToTemp() throws IOException {
        JSONObject result = new JSONObject();
        result.put("tempdir", TEMP_DIRECTORY);
        try {
            deleteFilesFromTemp();
            for (String fileName : FILES_TO_COPY) {
                URL resourceUrl = ResourceExtractor.class.getClassLoader().getResource("binary/" + fileName);

                if (resourceUrl != null) {
                    try (InputStream in = resourceUrl.openStream()) {
                        File destFile = new File(TEMP_DIRECTORY + fileName);

                        try (OutputStream out = new FileOutputStream(destFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = in.read(buffer)) > 0) {
                                out.write(buffer, 0, length);
                            }
                            if (fileName.equals("PECmd.exe")) {
                                result.put("pecmd", destFile.getAbsolutePath());
                            } else if (fileName.equals("scannerExplorer.exe")) {
                                result.put("explorer", destFile.getAbsolutePath());
                            } else if (fileName.equals("scannerJavaw.exe")) {
                                result.put("javaw", destFile.getAbsolutePath());
                            } else if (fileName.equals("UsnParser.exe")) {
                                result.put("usn", destFile.getAbsolutePath());
                            }
                        }
                    }
                } else {
                    System.out.println("Resource not found: " + fileName);
                }
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        System.out.println(result.toString(4));
        return result;
    }

    public static boolean deleteFilesFromTemp() {
        try {
            for (String fileName : FILES_TO_COPY) {
                File file = new File(TEMP_DIRECTORY + fileName);
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("File deleted: " + fileName);
                    } else {
                        System.out.println("Failed to delete file: " + fileName);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            copyFilesToTemp();
            deleteFilesFromTemp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
