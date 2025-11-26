package com.example.util;

import com.itextpdf.text.pdf.BaseFont;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FontResolver {

    public static BaseFont resolveCyrillicFont() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return BaseFont.createFont("C:/Windows/Fonts/arial.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }

        Path dejavu = Paths.get("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf");

        if (Files.exists(dejavu)) {
            return BaseFont.createFont(dejavu.toString(),
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }

        throw new RuntimeException("Не знайдено шрифт з підтримкою кирилиці.");
    }
}
