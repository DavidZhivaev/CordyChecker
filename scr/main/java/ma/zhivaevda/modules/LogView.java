package me.zhivaevda.modules;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.zip.*;
import org.json.JSONArray;

import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.json.JSONObject;

public class LogView {
    private static final Set<String> names = new LinkedHashSet<>();
    private static final List<String> bad_parts = new ArrayList<>();

    public static JSONObject getLogs(String directory, JSONArray data) {
        String logsDirectory = new File(directory, "logs").getAbsolutePath();
        File folder = new File(logsDirectory);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".log")) {
                            readTextFile(file, data);
                        } else if (file.getName().endsWith(".log.gz")) {
                            readGzippedLog(file, data);
                        } else if (file.getName().endsWith(".zip")) {
                            extractAndReadLogFromZip(file, data);
                        } else if (file.getName().endsWith(".tar") || file.getName().endsWith(".tar.gz")) {
                            extractAndReadLogFromTar(file, data);
                        }
                    }
                }
            }
        }

        JSONObject output = new JSONObject();
        JSONArray nicks = new JSONArray();
        for (String nick : names) {
            nicks.put(nick);
        }
        output.put("nicks", nicks);
        JSONArray bads = new JSONArray();
        for (String part : bad_parts) {
            bads.put(part);
        }
        output.put("lines", bads);
        return output;
    }

    private static void checkLine(String line, JSONArray data) {
        try {
            if (line.contains(" Setting user: ")) {
                String[] args = line.split(" Setting user: ");
                String nick = args[1];
                names.add(nick);
            }
            for (Object part : data) {
                if (line.toLowerCase().contains(part.toString().toLowerCase())) {
                    bad_parts.add(line);
                }
            }
        } catch (Exception e) {
            // poxui
        }
    }

    private static void readTextFile(File file, JSONArray data) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                checkLine(line, data);
            }
        } catch (IOException e) {
            System.out.println("Error reading text file: " + file.getName());
        }
    }

    private static void readGzippedLog(File file, JSONArray data) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GzipCompressorInputStream(new FileInputStream(file))))) {
            String line;
            while ((line = br.readLine()) != null) {
                checkLine(line, data);
            }
        } catch (IOException e) {
            System.out.println("Error reading gzipped log file: " + file.getName());
        }
    }

    private static void extractAndReadLogFromZip(File zipFile, JSONArray data) {
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".log")) {
                    System.out.println("Reading log file: " + entry.getName());
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            checkLine(line, data);
                        }
                    }
                    return; // Прерываем после нахождения первого .log файла
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading zip file: " + zipFile.getName());
        }
    }

    private static void extractAndReadLogFromTar(File tarFile, JSONArray data) {
        try (FileInputStream fis = new FileInputStream(tarFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             TarArchiveInputStream tarIn = tarFile.getName().endsWith(".gz") ?
                     new TarArchiveInputStream(new GzipCompressorInputStream(bis)) :
                     new TarArchiveInputStream(bis)) {

            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".log")) {
                    System.out.println("Reading log file: " + entry.getName());
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(tarIn))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            checkLine(line, data);
                        }
                    }
                    return;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading tar file: " + tarFile.getName());
        }
    }
}
