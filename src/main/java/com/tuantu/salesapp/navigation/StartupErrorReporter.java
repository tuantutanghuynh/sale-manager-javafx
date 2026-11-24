// src/main/java/com/tuantu/salesapp/navigation/StartupErrorReporter.java
package com.tuantu.salesapp.navigation;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.tuantu.salesapp.config.AppPaths;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

// Builds and displays user-friendly error dialogs for startup failures and unexpected
// exceptions — keeps JavaFX alert construction out of App lifecycle methods.
public final class StartupErrorReporter {

    private final ResourceBundle bundle;

    // Creates an error reporter backed by the application's localized string bundle.
    public StartupErrorReporter(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    // Displays a startup error alert with the error message or header text, and log hint.
    // Must be called on the FX Application Thread.
    public void showStartupError(Throwable e) {
        String title = bundle.getString("startup.error.title");
        String header = bundle.getString("startup.error.header");
        String content = startupContent(e);

        showAlert(title, header, content);
    }

    // Displays a generic error alert with a description and log hint, hiding raw error text.
    // Must be called on the FX Application Thread.
    public void showUnexpectedError(Throwable e) {
        String title = bundle.getString("unexpected.error.title");
        String header = bundle.getString("unexpected.error.header");
        String content = unexpectedContent();

        showAlert(title, header, content);
    }

    // Composes the dialog body for a startup failure without touching JavaFX — allows unit
    // testing of user-facing error text composition in headless CI environments.
    String startupContent(Throwable e) {
        String logHintPattern = bundle.getString("startup.error.log.hint");
        String logHint = MessageFormat.format(logHintPattern, AppPaths.logDir().toString());

        return (e != null && e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage() + "\n\n" + logHint
                : logHint;
    }

    // Composes the dialog body for unexpected errors without touching JavaFX — protects
    // internal implementation details and secrets from leaking into error dialogs.
    String unexpectedContent() {
        String description = bundle.getString("unexpected.error.description");
        String logHintPattern = bundle.getString("unexpected.error.log.hint");
        String logHint = MessageFormat.format(logHintPattern, AppPaths.logDir().toString());

        return description + "\n\n" + logHint;
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
