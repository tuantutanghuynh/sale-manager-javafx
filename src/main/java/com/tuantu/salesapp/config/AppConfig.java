// src/main/java/com/tuantu/salesapp/config/AppConfig.java
package com.tuantu.salesapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.tuantu.salesapp.exceptions.AppException;

// Merged view of the two configuration sources: the defaults shipped inside the jar
// and the per-machine file in %APPDATA% that overrides them and holds the database
// password. Callers ask for a value and never learn there were two files.
//
// Deliberately NOT all-static: this object holds state read from disk, and a static
// holder of state is a singleton in disguise (CLAUDE.md section 6). It is built once
// in App.init() and passed on from there.
public final class AppConfig {

    private static final String DEFAULTS_RESOURCE = "/application.properties";
    private static final String TEMPLATE_RESOURCE = "/config.properties.template";

    private final Properties props;

    private AppConfig(Properties props) {
        this.props = props;
    }

    // Reads the bundled defaults, then overlays the user file. On a first run the
    // user
    // file does not exist yet: it is created from the template and the caller is
    // told
    // to fill it in, which is friendlier than failing on a missing password later.
    public static AppConfig load() {
        Properties merged = new Properties();
        merged.putAll(readResource(DEFAULTS_RESOURCE));

        Path userFile = AppPaths.configFile();
        if (!Files.exists(userFile)) {
            createUserConfigFromTemplate(userFile);
            throw new AppException("Created a configuration file at " + userFile
                    + ". Set db.password in it, then start the application again.");
        }
        merged.putAll(readFile(userFile));

        return new AppConfig(merged);
    }

    public String dbUrl() {
        return required("db.url");
    }

    public String dbUser() {
        return required("db.user");
    }

    // Never logged and never included in an exception message — see required().
    public String dbPassword() {
        return required("db.password");
    }

    public int dbPoolMaxSize() {
        return intValue("db.pool.maxSize", 5);
    }

    public int dbConnectionTimeoutMs() {
        return intValue("db.pool.connectionTimeoutMs", 10_000);
    }

    // Error messages name the missing key and the file to edit, never the value —
    // one of these keys is the database password (CLAUDE.md section 8).
    private String required(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new AppException(
                    "Missing configuration key '" + key + "'. Set it in " + AppPaths.configFile());
        }
        return value.trim();
    }

    private int intValue(String key, int fallback) {
        String raw = props.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new AppException("Configuration key '" + key + "' must be a whole number.", e);
        }
    }

    private static Properties readResource(String resource) {
        try (InputStream in = AppConfig.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new AppException("Missing bundled resource " + resource);
            }
            Properties p = new Properties();
            p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            return p;
        } catch (IOException e) {
            throw new AppException("Cannot read bundled resource " + resource, e);
        }
    }

    private static Properties readFile(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Properties p = new Properties();
            p.load(reader);
            return p;
        } catch (IOException e) {
            throw new AppException("Cannot read configuration file " + file, e);
        }
    }

    private static void createUserConfigFromTemplate(Path target) {
        try (InputStream in = AppConfig.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (in == null) {
                throw new AppException("Missing bundled resource " + TEMPLATE_RESOURCE);
            }
            Files.copy(in, target);
        } catch (IOException e) {
            throw new AppException("Cannot create configuration file " + target, e);
        }
    }
}
