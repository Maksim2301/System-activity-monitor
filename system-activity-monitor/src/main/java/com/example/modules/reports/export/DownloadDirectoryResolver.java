package com.example.modules.reports.export;

import java.nio.file.*;

public class DownloadDirectoryResolver {

    public static Path getDownloadDirectory() {

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return Paths.get(System.getProperty("user.home"), "Downloads");
        }

        return Paths.get(System.getProperty("user.home"));
    }
}
