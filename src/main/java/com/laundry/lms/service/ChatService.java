package com.laundry.lms.service;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for customer communication - conversations and messages.
 */
@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LaundryOrderRepository orderRepository;
    private final AuditLogRepository auditLogRepository;

    public ChatService(ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            LaundryOrderRepository orderRepository,
            AuditLogRepository auditLogRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.orderRepository = orderRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Create a new conversation.
     */
    @Transactional
    public Conversation createConversation(User customer, Long orderId) {
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(ConversationStatus.OPEN);

        if (orderId != null) {
            LaundryOrder order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            conversation.setOrder(order);
        }

        return conversationRepository.save(conversation);
    }

    /**
     * Get conversations for a customer.
     */
    public List<Conversation> getCustomerConversations(Long customerId) {
        return conversationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get all conversations (for customer service).
     */
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    /**
     * Get open conversations (for customer service).
     */
    public List<Conversation> getOpenConversations() {
        return conversationRepository.findByStatus(ConversationStatus.OPEN);
    }

    /**
     * Get conversation by ID.
     */
    public Optional<Conversation> getConversationById(Long id) {
        return conversationRepository.findById(id);
    }

    /**
     * Send a message in a conversation.
     */
    @Transactional
    public Message sendMessage(Long conversationId, User sender, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message message = new Message(sender, null, content);
        // Note: Message entity uses from/to, we'll need to adapt
        return messageRepository.save(message);
    }

    /**
     * Get messages for a conversation.
     * Returns messages between the customer and support.
     */
    public List<Message> getConversationMessages(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // For now, get recent messages for the customer
        return messageRepository.findByFromUserId(conversation.getCustomer().getId());
    }

    /**
     * Close a conversation.
     */
    @Transactional
    public Conversation closeConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    /**
     * Delete a conversation (admin only).
     */
    @Transactional
    public void deleteConversation(Long conversationId, User admin) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        createAuditLog(admin, "DELETE_CONVERSATION", "Conversation", conversationId,
                conversation.getCustomer().getName(), null);
        conversationRepository.delete(conversation);
    }

    /**
     * Get customer order history (for customer service sidebar).
     */
    public List<LaundryOrder> getCustomerOrderHistory(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    private void createAuditLog(User actor, String action, String entityType, Long entityId,
            String before, String after) {
        AuditLog log = AuditLog.create(actor, action, entityType, entityId, before, after);
        auditLogRepository.save(log);
    }
}
