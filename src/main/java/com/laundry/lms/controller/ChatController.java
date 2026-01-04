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
 * Chat Controller - shared endpoints for messaging.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final CustomUserDetailsService userDetailsService;

    public ChatController(ChatService chatService,
            CustomUserDetailsService userDetailsService) {
        this.chatService = chatService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Send a message in a conversation.
     */
    @PostMapping("/conversations/{id}/messages")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CUSTOMER_SERVICE', 'ADMIN')")
    public ResponseEntity<?> sendMessage(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User sender = userDetailsService.loadUserEntityByEmail(authentication.getName());
            String content = body.get("content");

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message content is required"));
            }

            Message message = chatService.sendMessage(id, sender, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Message sent",
                    "data", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get messages for a conversation.
     */
    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CUSTOMER_SERVICE', 'ADMIN')")
    public ResponseEntity<?> getMessages(@PathVariable Long id) {
        try {
            List<Message> messages = chatService.getConversationMessages(id);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
