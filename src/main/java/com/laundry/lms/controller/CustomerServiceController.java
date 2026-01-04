package com.laundry.lms.controller;

import com.laundry.lms.model.*;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Customer Service Controller - for support staff to handle conversations.
 */
@RestController
@RequestMapping("/api/customer-service")
@CrossOrigin(origins = "*")
public class CustomerServiceController {

    private final ChatService chatService;
    private final CustomUserDetailsService userDetailsService;

    public CustomerServiceController(ChatService chatService,
            CustomUserDetailsService userDetailsService) {
        this.chatService = chatService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get all conversations (open ones first).
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN')")
    public ResponseEntity<?> getAllConversations(@RequestParam(required = false) String status) {
        List<Conversation> conversations;
        if ("open".equalsIgnoreCase(status)) {
            conversations = chatService.getOpenConversations();
        } else {
            conversations = chatService.getAllConversations();
        }
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get a specific conversation with customer order history.
     */
    @GetMapping("/conversations/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN')")
    public ResponseEntity<?> getConversation(@PathVariable Long id) {
        return chatService.getConversationById(id)
                .map(conversation -> {
                    List<LaundryOrder> orderHistory = chatService.getCustomerOrderHistory(
                            conversation.getCustomer().getId());
                    return ResponseEntity.ok(Map.of(
                            "conversation", conversation,
                            "customerInfo", Map.of(
                                    "id", conversation.getCustomer().getId(),
                                    "name", conversation.getCustomer().getName(),
                                    "email", conversation.getCustomer().getEmail()),
                            "orderHistory", orderHistory));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Conversation not found")));
    }

    /**
     * Get customer order history for a conversation.
     */
    @GetMapping("/conversations/{id}/customer-order-history")
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN')")
    public ResponseEntity<?> getCustomerOrderHistory(@PathVariable Long id) {
        return chatService.getConversationById(id)
                .<ResponseEntity<?>>map(conversation -> {
                    List<LaundryOrder> orderHistory = chatService.getCustomerOrderHistory(
                            conversation.getCustomer().getId());
                    return ResponseEntity.ok(orderHistory);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Conversation not found")));
    }

    /**
     * Close a conversation.
     */
    @PatchMapping("/conversations/{id}/close")
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN')")
    public ResponseEntity<?> closeConversation(@PathVariable Long id) {
        try {
            Conversation closed = chatService.closeConversation(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Conversation closed",
                    "conversation", closed));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
