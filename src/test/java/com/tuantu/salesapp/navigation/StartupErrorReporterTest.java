// src/test/java/com/tuantu/salesapp/navigation/StartupErrorReporterTest.java
package com.tuantu.salesapp.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import com.tuantu.salesapp.config.AppPaths;
import com.tuantu.salesapp.exceptions.AppException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Verifies that error dialog text composition properly formats user-facing messages
// and log directory hints without initializing the JavaFX toolkit — ensuring reliable
// headless verification of error presentation contracts.
class StartupErrorReporterTest {

    private ResourceBundle bundle;
    private StartupErrorReporter reporter;

    @BeforeEach
    void setUp() {
        bundle = ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag("vi"));
        reporter = new StartupErrorReporter(bundle);
    }

    @Test
    void shouldShowExceptionMessageFollowedByLogHint() {
        String message = "Missing configuration key 'db.url'. Set it in X";
        AppException exception = new AppException(message);

        String content = reporter.startupContent(exception);

        assertThat(content).startsWith(message + "\n\n");
        assertThat(content).contains(AppPaths.logDir().toString());
    }

    @Test
    void shouldShowOnlyLogHintWhenMessageIsNull() {
        RuntimeException exception = new RuntimeException((String) null);
        String expectedHint = MessageFormat.format(
                bundle.getString("startup.error.log.hint"), AppPaths.logDir().toString());

        String content = reporter.startupContent(exception);

        assertThat(content).isEqualTo(expectedHint);
        assertThat(content).doesNotContain("RuntimeException", "null");
    }

    @Test
    void shouldShowOnlyLogHintWhenMessageIsBlank() {
        RuntimeException exception = new RuntimeException("   ");
        String expectedHint = MessageFormat.format(
                bundle.getString("startup.error.log.hint"), AppPaths.logDir().toString());

        String content = reporter.startupContent(exception);

        assertThat(content).isEqualTo(expectedHint);
        assertThat(content).doesNotContain("RuntimeException", "null");
    }

    @Test
    void shouldShowOnlyLogHintWhenThrowableIsNull() {
        String expectedHint = MessageFormat.format(
                bundle.getString("startup.error.log.hint"), AppPaths.logDir().toString());

        String content = reporter.startupContent(null);

        assertThat(content).isEqualTo(expectedHint);
    }

    @Test
    void shouldNotLeakClassNameForErrorsWithoutMessage() {
        NoClassDefFoundError error = new NoClassDefFoundError();

        String content = reporter.startupContent(error);

        assertThat(content).doesNotContain("NoClassDefFoundError");
    }

    @Test
    void shouldNeverShowRawTextForUnexpectedErrors() {
        String content = reporter.unexpectedContent();

        assertThat(content).contains(bundle.getString("unexpected.error.description"));
        assertThat(content).contains(AppPaths.logDir().toString());
        assertThat(content).doesNotContain("password=abc123");
    }

    @Test
    void shouldKeepLogDirVerbatimInHint() {
        String logDir = AppPaths.logDir().toString();

        assertThat(reporter.startupContent(null)).contains(logDir);
        assertThat(reporter.unexpectedContent()).contains(logDir);
    }
}
