package com.laundry.lms.model;

/**
 * Unit types for service billing.
 */
public enum ServiceUnit {
    KG, // Price per kilogram
    ITEM, // Price per item
    CATEGORY_ITEM // Price per item based on category (e.g., pressing)
}
