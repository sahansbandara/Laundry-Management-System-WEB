package com.laundry.lms.controller;

import com.laundry.lms.dto.StatusUpdateRequest;
import com.laundry.lms.model.*;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Staff Order Controller - for laundry staff to manage orders.
 */
@RestController
@RequestMapping("/api/staff/orders")
@CrossOrigin(origins = "*")
public class StaffOrderController {

    private final OrderService orderService;
    private final CustomUserDetailsService userDetailsService;

    public StaffOrderController(OrderService orderService,
            CustomUserDetailsService userDetailsService) {
        this.orderService = orderService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get all orders with optional status filter.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('LAUNDRY_STAFF', 'ADMIN')")
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Long customerId) {
        try {
            List<LaundryOrder> orders;

            if (status != null && !status.isBlank()) {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getOrdersByStatus(orderStatus);
            } else if (customerId != null) {
                orders = orderService.getCustomerOrders(customerId);
            } else {
                orders = orderService.getAllOrders();
            }

            // Filter by serviceType if provided
            if (serviceType != null && !serviceType.isBlank()) {
                orders = orders.stream()
                        .filter(o -> o.getServiceType() != null &&
                                o.getServiceType().toLowerCase().contains(serviceType.toLowerCase()))
                        .toList();
            }

            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
        }
    }

    /**
     * Get a specific order.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAUNDRY_STAFF', 'ADMIN')")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found")));
    }

    /**
     * Update order status with workflow validation.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LAUNDRY_STAFF', 'ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        try {
            User staff = userDetailsService.loadUserEntityByEmail(authentication.getName());
            LaundryOrder updated = orderService.updateOrderStatus(id, request.getStatus(), staff.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated to " + updated.getStatus(),
                    "order", updated));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get order counts by status for dashboard.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('LAUNDRY_STAFF', 'ADMIN')")
    public ResponseEntity<?> getOrderStats() {
        List<LaundryOrder> allOrders = orderService.getAllOrders();

        long pending = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long inProgress = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.IN_PROGRESS).count();
        long ready = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.READY).count();
        long delivered = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        return ResponseEntity.ok(Map.of(
                "total", allOrders.size(),
                "pending", pending,
                "inProgress", inProgress,
                "ready", ready,
                "delivered", delivered,
                "cancelled", cancelled));
    }
}
