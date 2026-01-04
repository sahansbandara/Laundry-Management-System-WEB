package com.laundry.lms.controller;

import com.laundry.lms.model.*;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Delivery Controller - for delivery staff to manage deliveries.
 */
@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final CustomUserDetailsService userDetailsService;

    public DeliveryController(DeliveryService deliveryService,
            CustomUserDetailsService userDetailsService) {
        this.deliveryService = deliveryService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get deliveries assigned to current user.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('DELIVERY_STAFF', 'ADMIN')")
    public ResponseEntity<?> getMyDeliveries(Authentication authentication) {
        User user = userDetailsService.loadUserEntityByEmail(authentication.getName());
        List<DeliveryJob> deliveries = deliveryService.getDeliveriesForUser(user.getId());
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Update delivery status.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DELIVERY_STAFF', 'ADMIN')")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User user = userDetailsService.loadUserEntityByEmail(authentication.getName());
            String statusStr = body.get("status");

            if (statusStr == null || statusStr.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }

            DeliveryStatus newStatus = DeliveryStatus.valueOf(statusStr.toUpperCase());
            DeliveryJob updated = deliveryService.updateDeliveryStatus(id, newStatus, user);

            return ResponseEntity.ok(Map.of(
                    "message", "Delivery status updated to " + newStatus,
                    "delivery", updated,
                    "isLate", updated.getLateFlag()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get a specific delivery.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DELIVERY_STAFF', 'ADMIN')")
    public ResponseEntity<?> getDelivery(@PathVariable Long id) {
        return deliveryService.getDeliveryById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Delivery not found")));
    }
}
