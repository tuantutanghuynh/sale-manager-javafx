// src/main/java/com/tuantu/salesapp/models/entity/CustomerRank.java
package com.tuantu.salesapp.models.entity;

// Priority rank assigned to a customer (A, B, or C) by the company, driving priority ordering —
// independent from the customer group / channel axis (ADR-11).
// An enum is used instead of a raw character or string so an invalid rank cannot reach the database.
public enum CustomerRank {
    A,
    B,
    C
}
