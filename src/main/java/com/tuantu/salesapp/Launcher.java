// src/main/java/com/tuantu/salesapp/Launcher.java
package com.tuantu.salesapp;

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
        // Step 3 will add the log-directory system property as the first line here.
        Application.launch(App.class, args);
    }
}
