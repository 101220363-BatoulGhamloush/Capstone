package com.capstone.OpportuGrow.enums;

public enum PostStatus {
    PENDING,    // Waiting for admin approval
    ACTIVE,     // Available for funding
    FUNDED,     // Fully funded/completed
    EXPIRED,    // Time ran out
    CANCELLED,  // User cancelled
    REJECTED    // Admin rejected
}