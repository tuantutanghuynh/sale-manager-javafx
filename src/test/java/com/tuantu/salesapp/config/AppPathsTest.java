// src/test/java/com/tuantu/salesapp/config/AppPathsTest.java
package com.tuantu.salesapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Files;

import org.junit.jupiter.api.Test;

// Guards the one promise AppPaths makes: every user-owned file sits under a single
// folder, and creating that folder twice is harmless.
class AppPathsTest {

    @Test
    void shouldPutEveryFolderUnderOneApplicationDirectory() {
        assertThat(AppPaths.appDir()).hasFileName("SalesManager");
        assertThat(AppPaths.logDir()).startsWithRaw(AppPaths.appDir());
        assertThat(AppPaths.backupDir()).startsWithRaw(AppPaths.appDir());
        assertThat(AppPaths.attachmentsDir()).startsWithRaw(AppPaths.appDir());
        assertThat(AppPaths.configFile()).hasFileName("config.properties");
    }

    @Test
    void shouldCreateDirectoriesAndBeSafeToCallTwice() {
        AppPaths.createDirectories();
        assertThatCode(AppPaths::createDirectories).doesNotThrowAnyException();

        assertThat(Files.isDirectory(AppPaths.logDir())).isTrue();
        assertThat(Files.isDirectory(AppPaths.backupDir())).isTrue();
    }
}
