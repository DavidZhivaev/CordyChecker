package me.zhivaevda.utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class Searcher {
    public static void main(String[] args) {
        JSONArray array = new JSONArray();
        array.put("*.*");

        List<String> every = normalEvery(array);
        String result = search("C:\\\\Users\\\\11180\\\\AppData\\\\Local\\\\Temp\\\\gaargwrs.th1\\\\Microsoft.VisualStudio.Setup.ToastNotification.exe.config", every);
        System.out.println(result);
    }

    public static List<String> normalEvery(JSONArray every) {
        List<String> response = new ArrayList<>();

        for (int i = 0; i < every.length(); i++) {
            String normalPart = every.getString(i).toLowerCase();
            if (!normalPart.isEmpty()) {
                response.add(normalPart);
            }
        }

        return response;
    }

    public static String search(String text, Collection<String> every) {
        String lowerText = text.toLowerCase();

        for (String pattern : every) {
            String regex = wildcardToRegex(pattern.toLowerCase());
            if (Pattern.compile(regex).matcher(lowerText).find()) {
                return pattern;
            }
        }

        return "Не найдено!";
    }

    private static String wildcardToRegex(String wildcard) {
        StringBuilder sb = new StringBuilder();
        for (char c : wildcard.toCharArray()) {
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '?':
                    sb.append(".?");
                    break;
                case '.':
                    sb.append("\\.");
                    break;
                default:
                    sb.append(Pattern.quote(Character.toString(c)));
            }
        }
        return sb.toString();
    }
}
