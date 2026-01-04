package com.laundry.lms.model;

/**
 * Service types available in the laundry system.
 */
public enum ServiceType {
    LAUNDRY_WASH_ONLY, // 250 LKR per kg
    PRESSING_IRON_ONLY, // Per item based on category
    WASH_AND_IRON, // 250 LKR per kg + 25 LKR per item
    DRY_CLEANING, // 400 LKR per kg
    EXPRESS_SERVICE, // +25% of subtotal
    PREMIUM_DELICATE_CARE // +400 LKR per item
}
