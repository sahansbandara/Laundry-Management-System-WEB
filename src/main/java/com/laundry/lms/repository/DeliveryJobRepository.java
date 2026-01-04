package com.laundry.lms.repository;

import com.laundry.lms.model.DeliveryJob;
import com.laundry.lms.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryJobRepository extends JpaRepository<DeliveryJob, Long> {
    List<DeliveryJob> findByAssignedToId(Long userId);

    List<DeliveryJob> findByOrderId(Long orderId);

    List<DeliveryJob> findByStatus(DeliveryStatus status);

    List<DeliveryJob> findByAssignedToIdAndStatus(Long userId, DeliveryStatus status);
}
