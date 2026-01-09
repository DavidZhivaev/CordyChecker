package me.zhivaevda.modules;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

public class ASM {
    public static JSONArray getAsm(JSONArray netochno, JSONArray tochno, String exePath) {
        JSONArray result = new JSONArray();
        StringBuilder inputData = new StringBuilder();

        for (Object s : tochno) {
            String a = (String) s;
            inputData.append(a).append("\n");
        }
        inputData.append("END_EXACT\n");
        for (Object s : netochno) {
            String a = (String) s;
            inputData.append(a).append("\n");
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(exePath);
            Process process = processBuilder.start();

            OutputStreamWriter osw = new OutputStreamWriter(process.getOutputStream(), "UTF-8");
            BufferedWriter writer = new BufferedWriter(osw);
            writer.write(inputData.toString());
            writer.flush();
            writer.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("error process") || line.toLowerCase().contains("error open")) {
                    if (!line.toLowerCase().contains("::::::::::::::")) {
                        JSONObject out = new JSONObject();
                        out.put("error", line);
                        result.put(out);
                    }
                }
                try {
                    String[] parts = line.split("::::::::::::::");
                    if (parts.length == 2) {
                        String[] part = parts[0].split(" ");
                        if (parts[0].length() >= 20 && part[0].equals("ASM:")) {
                            JSONObject out = new JSONObject();
                            out.put("string", parts[0]);
                            out.put("detect", "ASM: & len>=20");
                            result.put(out);
                        }
                    }
                } catch (Exception e) {
                    // poxui
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            JSONObject out = new JSONObject();
            out.put("error", e.getMessage());
            result.put(out);
        }

        return result;
    }
}
