package com.laundry.lms.model;

/**
 * User roles for the Laundry Management System.
 * Each role has specific access permissions across the 6 modules.
 */
public enum UserRole {
    CUSTOMER, // Place orders, view own orders/payments, chat with support
    LAUNDRY_STAFF, // Process orders, update order status, manage laundry tasks
    DELIVERY_STAFF, // Handle delivery jobs, update delivery status
    FINANCE_STAFF, // Manage payments, invoices, financial reports
    CUSTOMER_SERVICE, // Handle customer chats, view customer order history
    ADMIN // Full access to all modules and system settings
}
