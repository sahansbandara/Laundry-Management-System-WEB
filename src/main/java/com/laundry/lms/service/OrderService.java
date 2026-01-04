package com.laundry.lms.service;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.AuditLogRepository;
import com.laundry.lms.repository.LaundryOrderRepository;
import com.laundry.lms.repository.OrderItemRepository;
import com.laundry.lms.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for order management operations.
 */
@Service
public class OrderService {

    private final LaundryOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PricingService pricingService;
    private final ObjectMapper objectMapper;

    public OrderService(LaundryOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            PricingService pricingService,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.pricingService = pricingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Get orders for a specific customer.
     */
    public List<LaundryOrder> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get all orders (for staff/admin).
     */
    public List<LaundryOrder> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get orders filtered by status.
     */
    public List<LaundryOrder> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get an order by ID.
     */
    public Optional<LaundryOrder> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Cancel an order (customer can only cancel PLACED/RECEIVED).
     */
    @Transactional
    public LaundryOrder cancelOrder(Long orderId, Long customerId, String reason, boolean isAdmin) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Check ownership (unless admin)
        if (!isAdmin && !order.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("You can only cancel your own orders");
        }

        // Customer can only cancel PENDING status
        if (!isAdmin) {
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException(
                        "Cannot cancel order. Only PENDING orders can be cancelled by customer.");
            }
        }

        // Create audit log for admin cancellations
        if (isAdmin) {
            User admin = userRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));
            createAuditLog(admin, "CANCEL_ORDER", "LaundryOrder", orderId,
                    order.getStatus().name(), OrderStatus.CANCELLED.name());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes(
                order.getNotes() != null ? order.getNotes() + " | Cancelled: " + reason : "Cancelled: " + reason);

        return orderRepository.save(order);
    }

    /**
     * Update order status with workflow validation.
     */
    @Transactional
    public LaundryOrder updateOrderStatus(Long orderId, OrderStatus newStatus, Long staffId) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        // Validate transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }

        // Audit log
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found"));
        createAuditLog(staff, "UPDATE_STATUS", "LaundryOrder", orderId,
                currentStatus.name(), newStatus.name());

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Validate order status transitions.
     * PENDING -> IN_PROGRESS -> READY -> DELIVERED
     * Any state -> CANCELLED (with appropriate permissions)
     */
    public boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next)
            return true;

        return switch (current) {
            case PENDING -> next == OrderStatus.IN_PROGRESS || next == OrderStatus.CANCELLED;
            case IN_PROGRESS -> next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY -> next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }

    /**
     * Create an audit log entry.
     */
    private void createAuditLog(User actor, String action, String entityType, Long entityId,
            String before, String after) {
        AuditLog log = AuditLog.create(actor, action, entityType, entityId, before, after);
        auditLogRepository.save(log);
    }

    /**
     * Validate order dates.
     */
    public void validateOrderDates(LocalDateTime pickupAt, LocalDateTime deliveryAt) {
        LocalDateTime now = LocalDateTime.now();

        if (pickupAt == null) {
            throw new IllegalArgumentException("Pickup date is required");
        }
        if (deliveryAt == null) {
            throw new IllegalArgumentException("Delivery date is required");
        }
        if (pickupAt.isBefore(now)) {
            throw new IllegalArgumentException("Pickup date must be in the future");
        }
        if (deliveryAt.isBefore(pickupAt) || deliveryAt.isEqual(pickupAt)) {
            throw new IllegalArgumentException("Delivery date must be after pickup date");
        }
    }
}
