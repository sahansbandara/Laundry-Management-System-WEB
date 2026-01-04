package com.laundry.lms.controller;

import com.laundry.lms.model.*;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.FinanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Finance Controller - for finance staff to manage payments and invoices.
 */
@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "*")
public class FinanceController {

    private final FinanceService financeService;
    private final CustomUserDetailsService userDetailsService;

    public FinanceController(FinanceService financeService,
            CustomUserDetailsService userDetailsService) {
        this.financeService = financeService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Generate invoice for an order.
     */
    @PostMapping("/invoices/generate/{orderId}")
    @PreAuthorize("hasAnyRole('FINANCE_STAFF', 'ADMIN')")
    public ResponseEntity<?> generateInvoice(@PathVariable Long orderId, Authentication authentication) {
        try {
            User user = userDetailsService.loadUserEntityByEmail(authentication.getName());
            Invoice invoice = financeService.generateInvoice(orderId, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Invoice generated",
                    "invoice", invoice));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all payments.
     */
    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('FINANCE_STAFF', 'ADMIN')")
    public ResponseEntity<?> getAllPayments(@RequestParam(required = false) String status) {
        try {
            List<Payment> payments;
            if (status != null && !status.isBlank()) {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
                payments = financeService.getPaymentsByStatus(paymentStatus);
            } else {
                payments = financeService.getAllPayments();
            }
            return ResponseEntity.ok(payments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
    }

    /**
     * Update payment status (idempotent).
     */
    @PatchMapping("/payments/{id}/status")
    @PreAuthorize("hasAnyRole('FINANCE_STAFF', 'ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User user = userDetailsService.loadUserEntityByEmail(authentication.getName());
            String statusStr = body.get("status");

            if (statusStr == null || statusStr.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }

            PaymentStatus newStatus = PaymentStatus.valueOf(statusStr.toUpperCase());
            Payment updated = financeService.updatePaymentStatus(id, newStatus, user);

            return ResponseEntity.ok(Map.of(
                    "message", "Payment status updated to " + newStatus,
                    "payment", updated));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete payment record.
     */
    @DeleteMapping("/payments/{id}")
    @PreAuthorize("hasAnyRole('FINANCE_STAFF', 'ADMIN')")
    public ResponseEntity<?> deletePayment(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userDetailsService.loadUserEntityByEmail(authentication.getName());
            financeService.deletePayment(id, user);
            return ResponseEntity.ok(Map.of("message", "Payment deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get finance statistics.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('FINANCE_STAFF', 'ADMIN')")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(financeService.getFinanceStats());
    }

    /**
     * Get all invoices.
     */
    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('FINANCE_STAFF', 'ADMIN')")
    public ResponseEntity<?> getAllInvoices() {
        return ResponseEntity.ok(financeService.getAllInvoices());
    }
}
