package com.laundry.lms.repository;

import com.laundry.lms.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByActorId(Long actorId);

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
