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
 * Admin Delivery Controller - for admin to manage all deliveries.
 */
@RestController
@RequestMapping("/api/admin/deliveries")
@CrossOrigin(origins = "*")
public class AdminDeliveryController {

    private final DeliveryService deliveryService;
    private final CustomUserDetailsService userDetailsService;

    public AdminDeliveryController(DeliveryService deliveryService,
            CustomUserDetailsService userDetailsService) {
        this.deliveryService = deliveryService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Generate a delivery job from an order.
     */
    @PostMapping("/generate/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateDeliveryJob(@PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Long> body,
            Authentication authentication) {
        try {
            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            Long assignedToId = body != null ? body.get("assignedToId") : null;

            DeliveryJob job = deliveryService.generateDeliveryJob(orderId, assignedToId, admin);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Delivery job created",
                    "delivery", job));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all deliveries.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllDeliveries() {
        List<DeliveryJob> deliveries = deliveryService.getAllDeliveries();
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Reassign a delivery to another user.
     */
    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reassignDelivery(@PathVariable Long id,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        try {
            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            Long newAssigneeId = body.get("assignedToId");

            if (newAssigneeId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Assignee ID is required"));
            }

            DeliveryJob updated = deliveryService.reassignDelivery(id, newAssigneeId, admin);
            return ResponseEntity.ok(Map.of(
                    "message", "Delivery reassigned",
                    "delivery", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a delivery job.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDelivery(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            deliveryService.deleteDelivery(id, admin);
            return ResponseEntity.ok(Map.of("message", "Delivery deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
