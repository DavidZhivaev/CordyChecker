package me.zhivaevda;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class Api {
    private static final String BASE_URL = "http://217.28.222.80:8000/api";
    private static final List<String> signature = new ArrayList<>();

    public static String checkCode(String code) {
        try {
            JSONObject checkCodeJson = new JSONObject();
            checkCodeJson.put("signature", getSystemHWID());
            JSONObject checkCodeResponse = sendPostRequest("/verifiedCode/" + code, checkCodeJson);
            if (checkCodeResponse.has("key")) {
                String signal = checkCodeResponse.get("key").toString();
                System.out.println(signal);
                signature.add(signal);
            } else if (checkCodeResponse.has("status")) {
                System.out.println("error code");
                return "Ключ не рабочий!";
            } else {
                System.out.println("error code");
                return "Ключ не рабочий!";
            }
            return signature.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return "Внутренняя ошибка!";
        }
    }

    public static boolean miniCheckCode(String code) {
        try { // сделать проверку на интеджер
            JSONObject checkCodeJson = new JSONObject();
            JSONObject checkCodeResponse = sendPostRequest("/miniVerifiedCode/" + code, checkCodeJson);
            if (checkCodeResponse.has("status")) {
                String status = checkCodeResponse.get("status").toString();
                if (status.equals("Ready")) {
                    System.out.println("Код " + code + " - рабочий!");
                    return true;
                } else {
                    System.out.println("Код " + code + " - не рабочий!");
                    return false;
                }
            } else {
                System.out.println("Ответ сервера не имеет заголовка Ready");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            return false;
        }
    }

    public static JSONObject getData(String code) {
        try {
            String signal = signature.get(0);
            JSONObject checkCodeJson = new JSONObject();
            checkCodeJson.put("signature", signal);
            System.out.println(signal);
            JSONObject checkCodeResponse = sendPostRequest("/getData/" + code, checkCodeJson);

            return checkCodeResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static JSONObject sendError(String code, String error) {
        try {
            String signal = signature.get(0);
            JSONObject checkCodeJson = new JSONObject();
            checkCodeJson.put("signature", signal);
            checkCodeJson.put("error", error);
            JSONObject checkCodeResponse = sendPostRequest("/sendError/" + code, checkCodeJson);

            return checkCodeResponse;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static JSONObject sendResults(JSONObject results, String code) {
        try {
            String signal = signature.get(0);
            JSONObject checkCodeJson = new JSONObject();
            checkCodeJson.put("signature", signal);
            for (String key : results.keySet()) {
                checkCodeJson.put(key, results.get(key));
            }
            JSONObject checkCodeResponse = sendPostRequest("/saveResults/" + code, checkCodeJson);
            return checkCodeResponse;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static JSONObject sendPostRequest(String endpoint, JSONObject json) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return new JSONObject(response.toString());
    }

    public static String getSystemHWID() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            String systemData = hal.getComputerSystem().getSerialNumber();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(systemData.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "Не найдено!";
        }
    }
}
