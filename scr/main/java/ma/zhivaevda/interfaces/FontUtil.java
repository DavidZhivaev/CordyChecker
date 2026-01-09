package me.zhivaevda.interfaces;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontUtil {
    public static Font loadExo2(float size) {
        try {
            InputStream is = FontUtil.class.getResourceAsStream("/fonts/Exo2-VariableFont_wght.ttf");
            if (is == null) {
                throw new IOException("Font file not found in resources");
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new Font("Arial", Font.PLAIN, (int) size); // fallback
        }
    }
}
