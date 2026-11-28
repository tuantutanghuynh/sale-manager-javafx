// src/main/java/com/tuantu/salesapp/di/ServiceRegistry.java
package com.tuantu.salesapp.di;

import java.util.Objects;

import javax.sql.DataSource;

import com.tuantu.salesapp.repositories.CustomerRepository;

// Central dependency injection registry where the application object graph is built once (CLAUDE.md section 6 DI).
// Avoids hidden static singletons by instantiating and holding repositories and services in final fields.
// Controllers will receive dependencies from this registry via an FXMLLoader controller factory in M2.
public final class ServiceRegistry {

    // Repository managing customer data persistence and queries.
    private final CustomerRepository customerRepository;

    // Constructs the service registry and instantiates all repository and service components.
    public ServiceRegistry(DataSource dataSource) {
        Objects.requireNonNull(dataSource, "Data source must not be null.");
        this.customerRepository = new CustomerRepository(dataSource);
    }

    // Repository managing customer data persistence and queries.
    public CustomerRepository customerRepository() {
        return customerRepository;
    }
}
