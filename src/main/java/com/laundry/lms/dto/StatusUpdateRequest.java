package com.laundry.lms.dto;

import com.laundry.lms.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating order status.
 */
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
