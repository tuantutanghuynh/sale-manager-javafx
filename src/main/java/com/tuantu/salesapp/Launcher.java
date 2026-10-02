// src/main/java/com/tuantu/salesapp/Launcher.java
package com.tuantu.salesapp;

import com.tuantu.salesapp.config.AppPaths;

import javafx.application.Application;

// Entry point that does NOT extend Application.
// A class extending Application triggers JavaFX's module-path check before main() runs,
// which fails when JavaFX is on the classpath (ADR-4). Launching from a plain class
// skips that check — see CLAUDE.md section 14.
public final class Launcher {

    private Launcher() {
        // Utility class — never instantiated.
    }

    public static void main(String[] args) {
        // Order matters. Logback reads its configuration on the FIRST
        // LoggerFactory.getLogger(...) call and never re-reads it, so the log directory
        // has to be published as a system property before anything touches a logger.
        // Setting it later would silently write app.log into the working directory.
        // This is also why Launcher deliberately has no Logger field of its own.
        AppPaths.createDirectories();
        System.setProperty(AppPaths.LOG_DIR_PROPERTY, AppPaths.logDir().toString());

        Application.launch(App.class, args);
    }
}
