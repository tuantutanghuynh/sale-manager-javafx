// src/main/java/com/tuantu/salesapp/repositories/CustomerRepository.java
package com.tuantu.salesapp.repositories;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import com.tuantu.salesapp.exceptions.AppException;
import com.tuantu.salesapp.models.entity.Customer;
import com.tuantu.salesapp.models.entity.CustomerRank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Data access layer for customer entities.
// Manages plain JDBC persistence and queries for the customer table.
// Encapsulates SQL queries and error translation, insulating upper layers from JDBC details.
public class CustomerRepository {

    private static final Logger log = LoggerFactory.getLogger(CustomerRepository.class);

    // One column list for every SELECT and RETURNING, so mapRow always sees the same columns.
    private static final String SELECT_COLUMNS = "id, code, name, group_id, province_id, ward_id, sales_rep_id, "
            + "rank, credit_limit, payment_term_days, reminder_days_before, "
            + "phone, address, note, is_active, created_at, updated_at";

    // Connection pool data source for obtaining database connections.
    private final DataSource dataSource;

    // Constructs a customer repository backed by the given data source connection pool.
    public CustomerRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "Data source must not be null.");
    }

    // Retrieves all customer records ordered by customer name and code.
    // Returns an empty list if no customers exist in the database.
    // Throws AppException on database query failure.
    public List<Customer> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM customer ORDER BY name, code";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
            log.debug("Fetched {} customer(s)", customers.size());
            return customers;
        } catch (SQLException e) {
            throw new AppException("Could not fetch all customers", e);
        }
    }

    // Finds a customer record by its primary key ID.
    // Returns an Optional containing the matched customer, or Optional.empty() if not found.
    // Throws AppException on database query failure.
    public Optional<Customer> findById(int id) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM customer WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AppException("Could not find customer by id: " + id, e);
        }
    }

    // Finds a customer record by its unique customer code.
    // Returns an Optional containing the matched customer, or Optional.empty() if not found.
    // Throws AppException on database query failure.
    public Optional<Customer> findByCode(String code) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM customer WHERE code = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AppException("Could not find customer by code: " + code, e);
        }
    }

    // Inserts a new customer record into the database and returns the generated entity.
    // Requires customer.id() to be null; throws IllegalArgumentException otherwise.
    // Throws AppException on database query failure or if the customer code already exists.
    public Customer insert(Customer customer) {
        Objects.requireNonNull(customer, "Customer must not be null.");
        if (customer.id() != null) {
            throw new IllegalArgumentException("Customer id must be null for insert.");
        }

        String sql = "INSERT INTO customer ("
                + "code, name, group_id, province_id, ward_id, sales_rep_id, "
                + "rank, credit_limit, payment_term_days, reminder_days_before, "
                + "phone, address, note, is_active"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING " + SELECT_COLUMNS;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCustomerFields(ps, customer);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer inserted = mapRow(rs);
                    log.debug("Inserted customer with code: {}, id: {}", inserted.code(), inserted.id());
                    return inserted;
                }
                throw new AppException("Could not insert customer with code: " + customer.code() + ", no row returned");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new AppException("Customer code already exists: " + customer.code(), e);
            }
            throw new AppException("Could not insert customer with code: " + customer.code(), e);
        }
    }

    // Updates an existing customer record identified by its ID and returns the updated entity.
    // Requires customer.id() to be non-null; throws IllegalArgumentException otherwise.
    // Returns an Optional containing the updated customer, or Optional.empty() if not found.
    // Throws AppException on database query failure or if the customer code already exists.
    public Optional<Customer> update(Customer customer) {
        Objects.requireNonNull(customer, "Customer must not be null.");
        if (customer.id() == null) {
            throw new IllegalArgumentException("Customer id must not be null for update.");
        }

        String sql = "UPDATE customer SET "
                + "code = ?, name = ?, group_id = ?, province_id = ?, ward_id = ?, sales_rep_id = ?, "
                + "rank = ?, credit_limit = ?, payment_term_days = ?, reminder_days_before = ?, "
                + "phone = ?, address = ?, note = ?, is_active = ? "
                + "WHERE id = ? "
                + "RETURNING " + SELECT_COLUMNS;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCustomerFields(ps, customer);
            ps.setInt(15, customer.id());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer updated = mapRow(rs);
                    log.debug("Updated customer with code: {}, id: {}", updated.code(), updated.id());
                    return Optional.of(updated);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new AppException("Customer code already exists: " + customer.code(), e);
            }
            throw new AppException("Could not update customer with id: " + customer.id(), e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        String rankStr = rs.getString("rank");
        CustomerRank rank = (rankStr == null || rankStr.isBlank()) ? null : CustomerRank.valueOf(rankStr.trim());

        OffsetDateTime createdAtRaw = rs.getObject("created_at", OffsetDateTime.class);
        Instant createdAt = createdAtRaw != null ? createdAtRaw.toInstant() : null;

        OffsetDateTime updatedAtRaw = rs.getObject("updated_at", OffsetDateTime.class);
        Instant updatedAt = updatedAtRaw != null ? updatedAtRaw.toInstant() : null;

        return new Customer(
                rs.getObject("id", Integer.class),
                rs.getString("code"),
                rs.getString("name"),
                rs.getObject("group_id", Integer.class),
                rs.getObject("province_id", Integer.class),
                rs.getObject("ward_id", Integer.class),
                rs.getObject("sales_rep_id", Integer.class),
                rank,
                rs.getBigDecimal("credit_limit"),
                rs.getObject("payment_term_days", Integer.class),
                rs.getObject("reminder_days_before", Integer.class),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("note"),
                rs.getBoolean("is_active"),
                createdAt,
                updatedAt);
    }

    private void bindCustomerFields(PreparedStatement ps, Customer customer) throws SQLException {
        ps.setString(1, customer.code());
        ps.setString(2, customer.name());
        setNullableInt(ps, 3, customer.groupId());
        setNullableInt(ps, 4, customer.provinceId());
        setNullableInt(ps, 5, customer.wardId());
        setNullableInt(ps, 6, customer.salesRepId());
        ps.setString(7, customer.rank() == null ? null : customer.rank().name());
        setNullableBigDecimal(ps, 8, customer.creditLimit());
        setNullableInt(ps, 9, customer.paymentTermDays());
        setNullableInt(ps, 10, customer.reminderDaysBefore());
        ps.setString(11, customer.phone());
        ps.setString(12, customer.address());
        ps.setString(13, customer.note());
        ps.setBoolean(14, customer.isActive());
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setNullableBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setBigDecimal(index, value);
        }
    }
}
