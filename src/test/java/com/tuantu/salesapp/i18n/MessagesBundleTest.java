// src/test/java/com/tuantu/salesapp/i18n/MessagesBundleTest.java
package com.tuantu.salesapp.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Validates the Vietnamese resource bundle integrity, ensuring UTF-8 decoding,
// required key existence, and correct MessageFormat parameterization — preventing runtime
// MissingResourceException and garbled text.
class MessagesBundleTest {

    private ResourceBundle bundle;

    @BeforeEach
    void setUp() {
        bundle = ResourceBundle.getBundle("i18n.messages", Locale.forLanguageTag("vi"));
    }

    @Test
    void shouldLoadVietnameseBundle() {
        assertThat(bundle).isNotNull();
        assertThat(bundle.getString("app.title")).isEqualTo("Sales Manager");
    }

    @Test
    void shouldContainEveryKeyUsedByCode() {
        List<String> requiredKeys = List.of(
                "app.title",
                "startup.error.title",
                "startup.error.header",
                "startup.error.log.hint",
                "unexpected.error.title",
                "unexpected.error.header",
                "unexpected.error.description",
                "unexpected.error.log.hint");

        SoftAssertions.assertSoftly(softly -> {
            for (String key : requiredKeys) {
                softly.assertThat(bundle.containsKey(key))
                        .as("Bundle should contain key '%s'", key)
                        .isTrue();
                if (bundle.containsKey(key)) {
                    softly.assertThat(bundle.getString(key))
                            .as("Value for key '%s' must not be blank", key)
                            .isNotBlank();
                }
            }
        });
    }

    @Test
    void shouldDecodeVietnameseAsUtf8() {
        assertThat(bundle.getString("startup.error.title")).isEqualTo("Lỗi khởi động");
    }

    @Test
    void shouldFormatLogHintPatterns() {
        String samplePath = "C:\\Users\\a b\\AppData";
        List<String> hintKeys = List.of("startup.error.log.hint", "unexpected.error.log.hint");

        for (String key : hintKeys) {
            String pattern = bundle.getString(key);
            String formatted = MessageFormat.format(pattern, samplePath);

            assertThat(formatted)
                    .as("Formatted text for '%s' should contain the verbatim path", key)
                    .contains(samplePath)
                    .as("Formatted text for '%s' should not contain literal '{0}'", key)
                    .doesNotContain("{0}");
        }
    }
}
