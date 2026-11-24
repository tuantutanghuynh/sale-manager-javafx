// src/main/java/com/tuantu/salesapp/exceptions/AppException.java
package com.tuantu.salesapp.exceptions;

// Infrastructure failures the user cannot fix by changing their input — database
// unreachable, data directory not writable, Excel file corrupt.
// Validation errors use IllegalArgumentException and business-state errors
// IllegalStateException instead (CLAUDE.md section 5).
// Exactly two constructors on purpose: a message, and a message wrapping the cause.
// Needing to branch on the kind of failure means adding a subclass, not a code field.
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
