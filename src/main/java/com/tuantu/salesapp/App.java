// src/main/java/com/tuantu/salesapp/App.java
package com.tuantu.salesapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// JavaFX application class. Owns the lifecycle only: builds the object graph,
// runs database migration and shows the first window. No business logic here.
// Launched by Launcher, never run directly (see CLAUDE.md section 14).
public class App extends Application {

    // Smallest window the layouts are designed for — below this, tables start to
    // break.
    private static final double MIN_WIDTH = 1024;
    private static final double MIN_HEIGHT = 700;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setCenter(new Label("Sales Manager"));

        Scene scene = new Scene(root, 1280, 800);

        stage.setTitle("Sales Manager");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Step 5 wires the ServiceRegistry shutdown here (executor, connection pool,
        // backup).
        // Keeping the override now documents that this hook exists and must not be
        // forgotten.
    }
}
