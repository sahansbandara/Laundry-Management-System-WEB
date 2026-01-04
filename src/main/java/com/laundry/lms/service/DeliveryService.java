package com.laundry.lms.service;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.AuditLogRepository;
import com.laundry.lms.repository.DeliveryJobRepository;
import com.laundry.lms.repository.LaundryOrderRepository;
import com.laundry.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for delivery job management.
 */
@Service
public class DeliveryService {

    private final DeliveryJobRepository deliveryJobRepository;
    private final LaundryOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public DeliveryService(DeliveryJobRepository deliveryJobRepository,
            LaundryOrderRepository orderRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.deliveryJobRepository = deliveryJobRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Generate a delivery job from an order (when order is READY).
     */
    @Transactional
    public DeliveryJob generateDeliveryJob(Long orderId, Long assignedToId, User creator) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Check if delivery job already exists for this order
        List<DeliveryJob> existingJobs = deliveryJobRepository.findByOrderId(orderId);
        if (!existingJobs.isEmpty()) {
            throw new IllegalStateException("Delivery job already exists for this order");
        }

        User assignedTo = null;
        if (assignedToId != null) {
            assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
        }

        DeliveryJob job = new DeliveryJob();
        job.setOrder(order);
        job.setAssignedTo(assignedTo);
        job.setPickupAt(order.getPickupDate().atTime(9, 0)); // Default 9 AM pickup
        job.setDeliveryAt(order.getDeliveryDate().atTime(17, 0)); // Default 5 PM delivery
        job.setStatus(DeliveryStatus.SCHEDULED);
        job.setLateFlag(false);

        DeliveryJob saved = deliveryJobRepository.save(job);
        createAuditLog(creator, "CREATE_DELIVERY_JOB", "DeliveryJob", saved.getId(), null, orderId.toString());
        return saved;
    }

    /**
     * Get deliveries assigned to a specific user.
     */
    public List<DeliveryJob> getDeliveriesForUser(Long userId) {
        return deliveryJobRepository.findByAssignedToId(userId);
    }

    /**
     * Get all deliveries.
     */
    public List<DeliveryJob> getAllDeliveries() {
        return deliveryJobRepository.findAll();
    }

    /**
     * Get delivery by ID.
     */
    public Optional<DeliveryJob> getDeliveryById(Long id) {
        return deliveryJobRepository.findById(id);
    }

    /**
     * Update delivery status with late detection.
     */
    @Transactional
    public DeliveryJob updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, User updater) {
        DeliveryJob job = deliveryJobRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery job not found"));

        DeliveryStatus oldStatus = job.getStatus();

        // Check if late
        if (newStatus != DeliveryStatus.DELIVERED &&
                newStatus != DeliveryStatus.CANCELLED &&
                LocalDateTime.now().isAfter(job.getDeliveryAt())) {
            job.setLateFlag(true);
        }

        job.setStatus(newStatus);
        DeliveryJob updated = deliveryJobRepository.save(job);

        createAuditLog(updater, "UPDATE_DELIVERY_STATUS", "DeliveryJob", deliveryId,
                oldStatus.name(), newStatus.name());
        return updated;
    }

    /**
     * Reassign delivery to another user.
     */
    @Transactional
    public DeliveryJob reassignDelivery(Long deliveryId, Long newAssigneeId, User admin) {
        DeliveryJob job = deliveryJobRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery job not found"));

        String oldAssignee = job.getAssignedTo() != null ? job.getAssignedTo().getName() : "Unassigned";

        User newAssignee = userRepository.findById(newAssigneeId)
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));

        job.setAssignedTo(newAssignee);
        DeliveryJob updated = deliveryJobRepository.save(job);

        createAuditLog(admin, "REASSIGN_DELIVERY", "DeliveryJob", deliveryId,
                oldAssignee, newAssignee.getName());
        return updated;
    }

    /**
     * Delete a delivery job.
     */
    @Transactional
    public void deleteDelivery(Long deliveryId, User admin) {
        DeliveryJob job = deliveryJobRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery job not found"));

        createAuditLog(admin, "DELETE_DELIVERY", "DeliveryJob", deliveryId,
                job.getOrder().getId().toString(), null);
        deliveryJobRepository.delete(job);
    }

    /**
     * Check for late deliveries (scheduled job).
     */
    @Transactional
    public int markLateDeliveries() {
        List<DeliveryJob> active = deliveryJobRepository.findByStatus(DeliveryStatus.SCHEDULED);
        active.addAll(deliveryJobRepository.findByStatus(DeliveryStatus.PICKED_UP));
        active.addAll(deliveryJobRepository.findByStatus(DeliveryStatus.IN_TRANSIT));

        int lateCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (DeliveryJob job : active) {
            if (now.isAfter(job.getDeliveryAt()) && !job.getLateFlag()) {
                job.setLateFlag(true);
                deliveryJobRepository.save(job);
                lateCount++;
            }
        }
        return lateCount;
    }

    private void createAuditLog(User actor, String action, String entityType, Long entityId,
            String before, String after) {
        AuditLog log = AuditLog.create(actor, action, entityType, entityId, before, after);
        auditLogRepository.save(log);
    }
}
