package com.laundry.lms.dto;

import com.laundry.lms.model.PressingCategory;
import com.laundry.lms.model.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a customer order.
 */
public class CustomerOrderRequest {

    @NotNull(message = "Pickup date/time is required")
    private LocalDateTime pickupAt;

    @NotNull(message = "Delivery date/time is required")
    private LocalDateTime deliveryAt;

    // List of order items
    @NotNull(message = "At least one service must be selected")
    private List<OrderItemRequest> items;

    // Add-ons
    private boolean expressService;
    private boolean premiumCare;

    private String notes;

    // Nested class for order items
    public static class OrderItemRequest {
        @NotNull(message = "Service type is required")
        private ServiceType serviceType;

        // For KG-based services
        @Positive(message = "Weight must be greater than 0")
        private Double quantityKg;

        // For ITEM-based services
        @Positive(message = "Item count must be greater than 0")
        private Integer itemCount;

        // For pressing with categories
        private Map<PressingCategory, Integer> pressingItems;

        // Getters and Setters
        public ServiceType getServiceType() {
            return serviceType;
        }

        public void setServiceType(ServiceType serviceType) {
            this.serviceType = serviceType;
        }

        public Double getQuantityKg() {
            return quantityKg;
        }

        public void setQuantityKg(Double quantityKg) {
            this.quantityKg = quantityKg;
        }

        public Integer getItemCount() {
            return itemCount;
        }

        public void setItemCount(Integer itemCount) {
            this.itemCount = itemCount;
        }

        public Map<PressingCategory, Integer> getPressingItems() {
            return pressingItems;
        }

        public void setPressingItems(Map<PressingCategory, Integer> pressingItems) {
            this.pressingItems = pressingItems;
        }
    }

    // Getters and Setters
    public LocalDateTime getPickupAt() {
        return pickupAt;
    }

    public void setPickupAt(LocalDateTime pickupAt) {
        this.pickupAt = pickupAt;
    }

    public LocalDateTime getDeliveryAt() {
        return deliveryAt;
    }

    public void setDeliveryAt(LocalDateTime deliveryAt) {
        this.deliveryAt = deliveryAt;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public boolean isExpressService() {
        return expressService;
    }

    public void setExpressService(boolean expressService) {
        this.expressService = expressService;
    }

    public boolean isPremiumCare() {
        return premiumCare;
    }

    public void setPremiumCare(boolean premiumCare) {
        this.premiumCare = premiumCare;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Calculate total item count across all services.
     */
    public int getTotalItemCount() {
        if (items == null)
            return 0;
        int total = 0;
        for (OrderItemRequest item : items) {
            if (item.getItemCount() != null) {
                total += item.getItemCount();
            }
            if (item.getPressingItems() != null) {
                for (Integer count : item.getPressingItems().values()) {
                    if (count != null)
                        total += count;
                }
            }
        }
        return total;
    }
}
