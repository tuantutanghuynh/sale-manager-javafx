// src/main/java/com/tuantu/salesapp/config/AppPaths.java
package com.tuantu.salesapp.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.tuantu.salesapp.exceptions.AppException;

// Single source of truth for where user-owned data lives on disk — config, logs,
// database backups, customer photos. Nothing goes next to the jar: the install
// directory can be read-only, and an upgrade wipes it (CLAUDE.md section 7).
// All-static with a private constructor, like every other class in config/.
public final class AppPaths {

    // Folder created inside the OS application-data directory.
    private static final String APP_DIR_NAME = "SalesManager";

    // System property that logback.xml reads to locate the log folder. Launcher
    // must
    // set it before the first LoggerFactory call — Logback configures itself once.
    public static final String LOG_DIR_PROPERTY = "salesmanager.logDir";

    private AppPaths() {
        // Utility class — never instantiated.
    }

    // Root folder for everything the user owns. Windows only today (ADR-5); keeping
    // the OS decision inside this method means adding macOS later touches no
    // caller.
    public static Path appDir() {
        String appData = System.getenv("APPDATA");
        Path base = (appData == null || appData.isBlank())
                ? Paths.get(System.getProperty("user.home"), "AppData", "Roaming")
                : Paths.get(appData);
        return base.resolve(APP_DIR_NAME);
    }

    // Rolling log files written by Logback.
    public static Path logDir() {
        return appDir().resolve("logs");
    }

    // pg_dump output — kept outside the install directory so an app upgrade cannot
    // delete it (spec section 10).
    public static Path backupDir() {
        return appDir().resolve("backup");
    }

    // Display-check photos attached to visits, from phase 2 on. The database stores
    // only the relative path, never the image bytes.
    public static Path attachmentsDir() {
        return appDir().resolve("attachments");
    }

    // Machine-specific settings including the database password — never in the jar.
    public static Path configFile() {
        return appDir().resolve("config.properties");
    }

    // Creates every folder the app needs. Called once from Launcher before logging
    // is
    // initialised, so a failure here cannot be reported through the log — it has to
    // surface as an exception.
    public static void createDirectories() {
        try {
            Files.createDirectories(logDir());
            Files.createDirectories(backupDir());
            Files.createDirectories(attachmentsDir());
        } catch (IOException e) {
            throw new AppException("Cannot create application data directory: " + appDir(), e);
        }
    }
}
