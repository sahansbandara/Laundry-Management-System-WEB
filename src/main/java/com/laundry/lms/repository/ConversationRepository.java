package com.laundry.lms.repository;

import com.laundry.lms.model.Conversation;
import com.laundry.lms.model.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByCustomerId(Long customerId);

    List<Conversation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Conversation> findByStatus(ConversationStatus status);

    List<Conversation> findByOrderId(Long orderId);
}
