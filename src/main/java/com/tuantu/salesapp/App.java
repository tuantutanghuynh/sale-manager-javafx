// src/main/java/com/tuantu/salesapp/App.java
package com.tuantu.salesapp;

import com.tuantu.salesapp.config.AppPaths;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import atlantafx.base.theme.PrimerLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// JavaFX application class. Owns the lifecycle only: builds the object graph,
// runs database migration and shows the first window. No business logic here.
// Launched by Launcher, never run directly (see CLAUDE.md section 14).
public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    // Smallest window the layouts are designed for — below this, tables start to
    // break.
    private static final double MIN_WIDTH = 1024;
    private static final double MIN_HEIGHT = 700;

    @Override
    public void start(Stage stage) {
        // Replaces JavaFX's default Modena stylesheet for the whole application.
        // Must run before any Scene is shown. M2 adds a light/dark toggle.
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        Label lblTitle = new Label("Sales Manager");
        lblTitle.getStyleClass().add("app-title");

        BorderPane root = new BorderPane();
        root.setCenter(lblTitle);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets()
                .add(getClass()
                        .getResource("/com/tuantu/salesapp/ui/styles/main.css")
                        .toExternalForm());

        stage.setTitle("Sales Manager");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        stage.show();
        log.info("Application started, data directory: {}", AppPaths.appDir());
    }

    @Override
    public void stop() {
        // Step 5 wires the ServiceRegistry shutdown here (executor, connection pool,
        // backup).
        // Keeping the override now documents that this hook exists and must not be
        // forgotten.
    }
}
