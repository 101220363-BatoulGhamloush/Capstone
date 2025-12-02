package com.capstone.OpportuGrow.enums;

public enum TransactionStatus {
    PENDING,    // Transaction started
    COMPLETED,  // Money transferred successfully
    FAILED,     // Transaction failed
    CANCELLED,  // User cancelled
    REFUNDED    // Money returned
}
