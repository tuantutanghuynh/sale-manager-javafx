// src/main/java/com/tuantu/salesapp/models/entity/Customer.java
package com.tuantu.salesapp.models.entity;

import java.math.BigDecimal;
import java.time.Instant;

// Immutable entity record mapping one-to-one to the customer table.
// Represents a purchasing customer account in the system, linking pricing channels,
// geography, assigned sales representatives, credit terms, and debt limits.
public record Customer(
        // Primary key identifier; null before initial database insertion.
        Integer id,
        // Unique business code (e.g., KH0001); never null or blank.
        String code,
        // Customer legal or trading name; never null or blank.
        String name,
        // Customer group ID defining sales channel and baseline policies; nullable.
        Integer groupId,
        // Province ID for geographical classification; nullable.
        Integer provinceId,
        // Ward ID for geographical classification; nullable.
        Integer wardId,
        // Sales representative ID who owns and manages the customer account; nullable.
        Integer salesRepId,
        // Assigned customer priority rank (A, B, or C); nullable.
        CustomerRank rank,
        // Maximum allowed outstanding debt in VND (NUMERIC(15, 0)); null inherits channel limit.
        BigDecimal creditLimit,
        // Allowed payment term in days before invoice overdue; null inherits channel policy.
        Integer paymentTermDays,
        // Number of days before payment due date to issue reminder; null inherits channel policy.
        Integer reminderDaysBefore,
        // Contact phone number; nullable.
        String phone,
        // Delivery and billing street address; nullable.
        String address,
        // Internal notes and remarks; nullable.
        String note,
        // Active status flag indicating whether trading is permitted; defaults to true.
        boolean isActive,
        // Timestamp when record was created; null before initial database insertion.
        Instant createdAt,
        // Timestamp when record was last updated; managed by database trigger.
        Instant updatedAt) {

    // Compact constructor validating invariant field constraints.
    public Customer {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Customer code must not be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be null or blank.");
        }
        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Customer credit limit must be greater than or equal to zero.");
        }
        if (creditLimit != null && creditLimit.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Customer credit limit must be a whole number of VND.");
        }
        if (paymentTermDays != null && paymentTermDays < 0) {
            throw new IllegalArgumentException("Customer payment term days must be greater than or equal to zero.");
        }
        if (reminderDaysBefore != null && reminderDaysBefore < 0) {
            throw new IllegalArgumentException("Customer reminder days before must be greater than or equal to zero.");
        }
    }

    // Factory method constructing a new customer instance prior to database insertion.
    // Initializes id and timestamps to null, optional fields to null, and isActive to true.
    public static Customer newCustomer(String code, String name) {
        return new Customer(
                null, code, name, null, null, null, null, null, null, null, null, null, null, null, true, null, null);
    }
}
