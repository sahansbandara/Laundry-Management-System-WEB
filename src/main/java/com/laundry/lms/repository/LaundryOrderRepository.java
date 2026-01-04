package com.laundry.lms.repository;

import com.laundry.lms.model.LaundryOrder;
import com.laundry.lms.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaundryOrderRepository extends JpaRepository<LaundryOrder, Long> {
    List<LaundryOrder> findByCustomerId(Long customerId);

    List<LaundryOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<LaundryOrder> findByStatus(OrderStatus status);

    List<LaundryOrder> findAllByOrderByCreatedAtDesc();
}
