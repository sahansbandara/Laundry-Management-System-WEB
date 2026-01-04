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
 * Customer Chat Controller - for customers to manage their conversations.
 */
@RestController
@RequestMapping("/api/customer/chat")
@CrossOrigin(origins = "*")
public class CustomerChatController {

    private final ChatService chatService;
    private final CustomUserDetailsService userDetailsService;

    public CustomerChatController(ChatService chatService,
            CustomUserDetailsService userDetailsService) {
        this.chatService = chatService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Create a new conversation.
     */
    @PostMapping("/conversations")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> createConversation(@RequestBody(required = false) Map<String, Long> body,
            Authentication authentication) {
        try {
            User customer = userDetailsService.loadUserEntityByEmail(authentication.getName());
            Long orderId = body != null ? body.get("orderId") : null;

            Conversation conversation = chatService.createConversation(customer, orderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Conversation created",
                    "conversation", conversation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get my conversations.
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getMyConversations(Authentication authentication) {
        User customer = userDetailsService.loadUserEntityByEmail(authentication.getName());
        List<Conversation> conversations = chatService.getCustomerConversations(customer.getId());
        return ResponseEntity.ok(conversations);
    }
}
