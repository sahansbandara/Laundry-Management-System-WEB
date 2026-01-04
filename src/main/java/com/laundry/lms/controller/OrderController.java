package com.laundry.lms.controller;

import com.laundry.lms.dto.OrderCreateResponse;
import com.laundry.lms.dto.OrderUpdateRequest;
import com.laundry.lms.dto.StatusUpdateRequest;
import com.laundry.lms.model.LaundryOrder;
import com.laundry.lms.model.OrderStatus;
import com.laundry.lms.model.PaymentStatus;
import com.laundry.lms.model.User;
import com.laundry.lms.repository.LaundryOrderRepository;
import com.laundry.lms.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final LaundryOrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(LaundryOrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /** Create order and tell frontend where to go next (payment page). */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody LaundryOrder orderRequest) {
        try {
            if (orderRequest.getCustomer() == null || orderRequest.getCustomer().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Customer is required"));
            }

            Optional<User> customerOpt = userRepository.findById(orderRequest.getCustomer().getId());
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Customer not found"));
            }

            if (orderRequest.getPrice() == null || orderRequest.getPrice().signum() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order total must be greater than zero"));
            }

            if (orderRequest.getServiceType() == null || orderRequest.getServiceType().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Service type is required"));
            }

            if (orderRequest.getQuantity() == null || orderRequest.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be greater than zero"));
            }

            orderRequest.setId(null);
            orderRequest.setCustomer(customerOpt.get());
            orderRequest.setStatus(OrderStatus.PENDING);
            orderRequest.setPaymentStatus(PaymentStatus.PENDING.name());
            orderRequest.setPaymentMethod(null);
            orderRequest.setPaidAt(null);

            LaundryOrder saved = orderRepository.save(orderRequest);
            String next = "/frontend/pay.html?orderId=" + saved.getId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new OrderCreateResponse(saved.getId(), next));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order: " + ex.getMessage()));
        }
    }

    /** Get one order (avoid Optional generic clash). */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        var opt = orderRepository.findById(id);
        if (opt.isPresent())
            return ResponseEntity.ok(opt.get());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
    }

    /** List all orders, optionally filtered by userId or status. */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        try {
            List<LaundryOrder> orders;

            if (userId != null) {
                // Filter by customer ID
                orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId);
            } else if (status != null && !status.isBlank()) {
                // Filter by status
                try {
                    OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                    orders = orderRepository.findByStatus(orderStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
                }
            } else {
                // Return all orders sorted by newest first
                orders = orderRepository.findAllByOrderByCreatedAtDesc();
            }

            return ResponseEntity.ok(orders);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch orders: " + ex.getMessage()));
        }
    }

    /** Update an existing order. Only allowed for PENDING orders. */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateRequest request) {
        try {
            Optional<LaundryOrder> orderOpt = orderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
            }

            LaundryOrder order = orderOpt.get();

            // Only allow updates for PENDING orders
            if (order.getStatus() != OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cannot update order. Only PENDING orders can be modified."));
            }

            // Update fields if provided
            if (request.getServiceType() != null && !request.getServiceType().isBlank()) {
                order.setServiceType(request.getServiceType());
            }
            if (request.getQuantity() != null && request.getQuantity() > 0) {
                order.setQuantity(request.getQuantity());
            }
            if (request.getUnit() != null && !request.getUnit().isBlank()) {
                order.setUnit(request.getUnit());
            }
            if (request.getPrice() != null && request.getPrice().signum() > 0) {
                order.setPrice(request.getPrice());
            }
            if (request.getPickupDate() != null) {
                order.setPickupDate(request.getPickupDate());
            }
            if (request.getDeliveryDate() != null) {
                order.setDeliveryDate(request.getDeliveryDate());
            }
            if (request.getNotes() != null) {
                order.setNotes(request.getNotes());
            }

            LaundryOrder updated = orderRepository.save(order);
            return ResponseEntity.ok(Map.of(
                    "message", "Order updated successfully",
                    "order", updated));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update order: " + ex.getMessage()));
        }
    }

    /** Update order status. Validates status transitions. */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        try {
            if (request.getStatus() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }

            Optional<LaundryOrder> orderOpt = orderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
            }

            LaundryOrder order = orderOpt.get();
            OrderStatus currentStatus = order.getStatus();
            OrderStatus newStatus = request.getStatus();

            // Validate status transition
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error",
                                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)));
            }

            order.setStatus(newStatus);
            LaundryOrder updated = orderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated to " + newStatus,
                    "order", updated));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update status: " + ex.getMessage()));
        }
    }

    /**
     * Validates order status transitions.
     * Allowed transitions:
     * - PENDING -> IN_PROGRESS, CANCELLED
     * - IN_PROGRESS -> READY, CANCELLED
     * - READY -> DELIVERED, CANCELLED
     * - DELIVERED -> (no further transitions)
     * - CANCELLED -> (no further transitions)
     */
    private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next)
            return true; // No change is always valid

        return switch (current) {
            case PENDING -> next == OrderStatus.IN_PROGRESS || next == OrderStatus.CANCELLED;
            case IN_PROGRESS -> next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY -> next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }

    /** Delete an order. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
        }
        orderRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Order deleted"));
    }
}
