// src/main/java/com/tuantu/salesapp/config/DatabaseMigrator.java
package com.tuantu.salesapp.config;

import javax.sql.DataSource;

import com.tuantu.salesapp.exceptions.AppException;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Brings the database schema up to date before the first window is shown.
// Running it afterwards would leave a window open against a half-built schema, where
// any screen that queries early fails in a way that is hard to trace back to here.
public final class DatabaseMigrator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrator.class);

    private DatabaseMigrator() {
        // Utility class — never instantiated.
    }

    public static void migrate(DataSource dataSource) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load();

            MigrateResult result = flyway.migrate();
            log.info("Flyway applied {} migration(s)", result.migrationsExecuted);
        } catch (Exception e) {
            // Wrapped so the startup path has a single failure type to report on.
            throw new AppException("Database migration failed: " + e.getMessage(), e);
        }
    }
}
