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

import com.tuantu.salesapp.config.AppConfig;
import com.tuantu.salesapp.config.DatabaseMigrator;
import com.tuantu.salesapp.config.DataSourceFactory;

import com.zaxxer.hikari.HikariDataSource;

// JavaFX application class. Owns the lifecycle only: builds the object graph,
// runs database migration and shows the first window. No business logic here.
// Launched by Launcher, never run directly (see CLAUDE.md section 14).
public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    // Smallest window the layouts are designed for — below this, tables start to
    // break.
    private static final double MIN_WIDTH = 1024;
    private static final double MIN_HEIGHT = 700;

    // Held so stop() can close the pool — a pool left open keeps non-daemon threads
    // alive and the JVM never exits.
    private HikariDataSource dataSource;

    // Runs on the JavaFX-Launcher thread, before start(). Everything slow and
    // everything touching the database belongs here: start() runs on the FX
    // Application Thread, where a database call would freeze the interface
    // (CLAUDE.md section 7). Nothing in init() may touch Stage or Scene.
    @Override
    public void init() {
        AppConfig config = AppConfig.load();
        dataSource = DataSourceFactory.create(config);
        DatabaseMigrator.migrate(dataSource);
        log.info("Database ready");
    }

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
        if (dataSource != null) {
            dataSource.close();
        }
        // M4 adds the automatic backup here; M2 adds the shared executor shutdown.
    }
}
