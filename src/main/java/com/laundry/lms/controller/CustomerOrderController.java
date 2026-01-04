package com.laundry.lms.controller;

import com.laundry.lms.dto.CustomerOrderRequest;
import com.laundry.lms.model.*;
import com.laundry.lms.repository.LaundryOrderRepository;
import com.laundry.lms.repository.OrderItemRepository;
import com.laundry.lms.repository.UserRepository;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.OrderService;
import com.laundry.lms.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Customer Order Controller - handles customer order operations.
 */
@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerOrderController {

    private final LaundryOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final PricingService pricingService;
    private final CustomUserDetailsService userDetailsService;

    public CustomerOrderController(LaundryOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            OrderService orderService,
            PricingService pricingService,
            CustomUserDetailsService userDetailsService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.pricingService = pricingService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get active services and pressing category prices.
     */
    @GetMapping("/services")
    public ResponseEntity<?> getServices() {
        return ResponseEntity.ok(Map.of(
                "services", pricingService.getActiveServices(),
                "pressingPrices", pricingService.getActivePressingPrices()));
    }

    /**
     * Create a new order.
     * Backend recalculates all prices to prevent client tampering.
     */
    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CustomerOrderRequest request,
            Authentication authentication) {
        try {
            // Get current user
            User customer = userDetailsService.loadUserEntityByEmail(authentication.getName());

            // Validate dates
            orderService.validateOrderDates(request.getPickupAt(), request.getDeliveryAt());

            // Validate at least one service
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "At least one service must be selected"));
            }

            // Calculate prices server-side
            BigDecimal subtotal = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CustomerOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                BigDecimal lineTotal = calculateItemPrice(itemReq);
                subtotal = subtotal.add(lineTotal);

                OrderItem item = new OrderItem();
                item.setServiceType(itemReq.getServiceType());
                item.setQuantityKg(itemReq.getQuantityKg());
                item.setItemCount(itemReq.getItemCount());
                item.setUnitPrice(getUnitPrice(itemReq.getServiceType()));
                item.setLineTotal(lineTotal);
                item.setUnit(getServiceUnit(itemReq.getServiceType()));
                orderItems.add(item);
            }

            // Apply Express (+25%)
            if (request.isExpressService()) {
                subtotal = pricingService.applyExpress(subtotal);
            }

            // Apply Premium Care (+400/item)
            int totalItems = request.getTotalItemCount();
            if (request.isPremiumCare()) {
                if (totalItems <= 0) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Premium care requires at least one item-based service"));
                }
                subtotal = pricingService.applyPremiumCare(subtotal, totalItems);
            }

            // Create order
            LaundryOrder order = new LaundryOrder();
            order.setCustomer(customer);
            order.setServiceType(buildServiceTypeSummary(request));
            order.setQuantity((double) request.getItems().size());
            order.setUnit("order");
            order.setPrice(subtotal);
            order.setPickupDate(request.getPickupAt().toLocalDate());
            order.setDeliveryDate(request.getDeliveryAt().toLocalDate());
            order.setNotes(request.getNotes());
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PENDING.name());

            LaundryOrder savedOrder = orderRepository.save(order);

            // Save order items
            for (OrderItem item : orderItems) {
                item.setOrder(savedOrder);
                orderItemRepository.save(item);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Order created successfully",
                    "orderId", savedOrder.getId(),
                    "total", subtotal,
                    "redirectUrl", "/frontend/pay.html?orderId=" + savedOrder.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Get customer's orders.
     */
    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        User customer = userDetailsService.loadUserEntityByEmail(authentication.getName());
        List<LaundryOrder> orders = orderService.getCustomerOrders(customer.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * Get a specific order.
     */
    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getOrder(@PathVariable Long id, Authentication authentication) {
        User customer = userDetailsService.loadUserEntityByEmail(authentication.getName());

        return orderService.getOrderById(id)
                .filter(order -> order.getCustomer().getId().equals(customer.getId()) ||
                        customer.getRole() == UserRole.ADMIN)
                .map(order -> ResponseEntity.ok(Map.of(
                        "order", order,
                        "items", orderItemRepository.findByOrderId(order.getId()))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found")));
    }

    /**
     * Cancel an order (customer can only cancel PENDING orders).
     */
    @PostMapping("/orders/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        try {
            User customer = userDetailsService.loadUserEntityByEmail(authentication.getName());
            String reason = body != null ? body.get("reason") : "Customer requested cancellation";
            boolean isAdmin = customer.getRole() == UserRole.ADMIN;

            LaundryOrder cancelled = orderService.cancelOrder(id, customer.getId(), reason, isAdmin);
            return ResponseEntity.ok(Map.of(
                    "message", "Order cancelled successfully",
                    "order", cancelled));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get customer's payments.
     */
    @GetMapping("/payments/my")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getMyPayments(Authentication authentication) {
        User customer = userDetailsService.loadUserEntityByEmail(authentication.getName());
        List<LaundryOrder> orders = orderService.getCustomerOrders(customer.getId());
        // Return orders with payment info
        return ResponseEntity.ok(orders.stream()
                .map(o -> Map.of(
                        "orderId", o.getId(),
                        "amount", o.getPrice(),
                        "paymentStatus", o.getPaymentStatus(),
                        "paymentMethod", o.getPaymentMethod() != null ? o.getPaymentMethod() : "N/A"))
                .toList());
    }

    // Helper methods
    private BigDecimal calculateItemPrice(CustomerOrderRequest.OrderItemRequest item) {
        return switch (item.getServiceType()) {
            case LAUNDRY_WASH_ONLY -> pricingService.calculateWashOnly(item.getQuantityKg());
            case DRY_CLEANING -> pricingService.calculateDryCleaning(item.getQuantityKg());
            case PRESSING_IRON_ONLY -> pricingService.calculatePressing(item.getPressingItems());
            case WASH_AND_IRON -> pricingService.calculateWashAndIron(item.getQuantityKg(), item.getItemCount());
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal getUnitPrice(ServiceType serviceType) {
        return switch (serviceType) {
            case LAUNDRY_WASH_ONLY -> PricingService.WASH_ONLY_PER_KG;
            case DRY_CLEANING -> PricingService.DRY_CLEANING_PER_KG;
            case WASH_AND_IRON -> PricingService.WASH_ONLY_PER_KG;
            default -> BigDecimal.ZERO;
        };
    }

    private ServiceUnit getServiceUnit(ServiceType serviceType) {
        return switch (serviceType) {
            case LAUNDRY_WASH_ONLY, DRY_CLEANING -> ServiceUnit.KG;
            case PRESSING_IRON_ONLY -> ServiceUnit.CATEGORY_ITEM;
            case WASH_AND_IRON -> ServiceUnit.ITEM;
            default -> ServiceUnit.ITEM;
        };
    }

    private String buildServiceTypeSummary(CustomerOrderRequest request) {
        List<String> services = new ArrayList<>();
        for (CustomerOrderRequest.OrderItemRequest item : request.getItems()) {
            services.add(item.getServiceType().name());
        }
        if (request.isExpressService())
            services.add("EXPRESS");
        if (request.isPremiumCare())
            services.add("PREMIUM");
        return String.join(", ", services);
    }
}
