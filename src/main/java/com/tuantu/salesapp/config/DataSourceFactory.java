// src/main/java/com/tuantu/salesapp/config/DataSourceFactory.java
package com.tuantu.salesapp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

// Builds the one connection pool the application uses. Opening a fresh connection per
// query would make the aggregate reports (sales by period, ageing, pace index) crawl,
// because each one costs a TCP handshake plus authentication.
public final class DataSourceFactory {

    private DataSourceFactory() {
        // Utility class — never instantiated.
    }

    public static HikariDataSource create(AppConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.dbUrl());
        hikari.setUsername(config.dbUser());
        hikari.setPassword(config.dbPassword());
        hikari.setMaximumPoolSize(config.dbPoolMaxSize());
        hikari.setConnectionTimeout(config.dbConnectionTimeoutMs());

        // Named so the pool's threads are identifiable in a thread dump and in logs.
        hikari.setPoolName("salesmanager-pool");

        return new HikariDataSource(hikari);
    }
}
