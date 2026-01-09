package me.zhivaevda.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class TextRead {
    public static String getText(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder fullText = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                fullText.append(line).append(System.lineSeparator());
            }

            return fullText.toString();

        } catch (IOException e) {
            return "";
        }
    }
}