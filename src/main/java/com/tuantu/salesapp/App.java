// src/main/java/com/tuantu/salesapp/App.java
package com.tuantu.salesapp;

import java.util.Locale;
import java.util.ResourceBundle;

import com.tuantu.salesapp.config.AppConfig;
import com.tuantu.salesapp.config.AppPaths;
import com.tuantu.salesapp.config.DataSourceFactory;
import com.tuantu.salesapp.config.DatabaseMigrator;
import com.tuantu.salesapp.di.ServiceRegistry;
import com.tuantu.salesapp.navigation.StartupErrorReporter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import atlantafx.base.theme.PrimerLight;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// JavaFX application class. Owns the lifecycle only: builds the object graph,
// runs database migration and shows the first window. No business logic here.
// Launched by Launcher, never run directly (see CLAUDE.md section 14).
public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    // Smallest window the layouts are designed for — below this, tables start to break.
    private static final double MIN_WIDTH = 1024;
    private static final double MIN_HEIGHT = 700;

    // Held so stop() can close the pool — a pool left open keeps non-daemon threads
    // alive and the JVM never exits.
    private HikariDataSource dataSource;

    // Holds localized strings loaded in init() so UI titles and error messages share the same bundle.
    private ResourceBundle bundle;

    // Helper to display styled error dialogs for startup failures and uncaught exceptions.
    private StartupErrorReporter errorReporter;

    // Stores any failure during init() so start() can display an error alert on the FX thread.
    private Throwable startupError;

    // Dependency injection registry holding application services; not used in start() yet — M2 will use it.
    private ServiceRegistry registry;

    // Runs on the JavaFX-Launcher thread, before start(). Everything slow and
    // everything touching the database belongs here: start() runs on the FX
    // Application Thread, where a database call would freeze the interface
    // (CLAUDE.md section 7). Nothing in init() may touch Stage or Scene.
    @Override
    public void init() {
        bundle = ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag("vi"));
        errorReporter = new StartupErrorReporter(bundle);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                log.error("Uncaught exception on thread {}", thread.getName(), throwable);
                Platform.runLater(() -> {
                    try {
                        errorReporter.showUnexpectedError(throwable);
                    } catch (Throwable t) {
                        log.error("Failed to show unexpected error dialog", t);
                    }
                });
            } catch (Throwable t) {
                log.error("Failed to handle uncaught exception", t);
            }
        });

        try {
            AppConfig config = AppConfig.load();
            dataSource = DataSourceFactory.create(config);
            DatabaseMigrator.migrate(dataSource);
            registry = new ServiceRegistry(dataSource);
            log.info("Database ready");
        } catch (Throwable e) {
            log.error("Startup failed", e);
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (Exception closeEx) {
                    log.warn("Failed to close data source on startup failure", closeEx);
                }
                dataSource = null;
            }
            startupError = e;
        }
    }

    @Override
    public void start(Stage stage) {
        // Replaces JavaFX's default Modena stylesheet for the whole application.
        // Must run before any Scene or Alert is shown so dialogs are themed too.
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        if (startupError != null) {
            errorReporter.showStartupError(startupError);
            Platform.exit();
            return;
        }

        String appTitle = bundle.getString("app.title");
        Label lblTitle = new Label(appTitle);
        lblTitle.getStyleClass().add("app-title");

        BorderPane root = new BorderPane();
        root.setCenter(lblTitle);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets()
                .add(getClass()
                        .getResource("/com/tuantu/salesapp/ui/styles/main.css")
                        .toExternalForm());

        stage.setTitle(appTitle);
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
