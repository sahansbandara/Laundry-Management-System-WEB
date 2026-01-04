package com.laundry.lms.model;

/**
 * Delivery status enum for tracking delivery jobs.
 */
public enum DeliveryStatus {
    SCHEDULED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    LATE,
    CANCELLED
}
