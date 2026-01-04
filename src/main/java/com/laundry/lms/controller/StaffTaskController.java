package com.laundry.lms.controller;

import com.laundry.lms.model.*;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Staff Task Controller - for staff to view and update their tasks.
 */
@RestController
@RequestMapping("/api/staff/tasks")
@CrossOrigin(origins = "*")
public class StaffTaskController {

    private final TaskService taskService;
    private final CustomUserDetailsService userDetailsService;

    public StaffTaskController(TaskService taskService,
            CustomUserDetailsService userDetailsService) {
        this.taskService = taskService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get tasks assigned to current user.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('LAUNDRY_STAFF', 'DELIVERY_STAFF', 'ADMIN')")
    public ResponseEntity<?> getMyTasks(Authentication authentication) {
        User user = userDetailsService.loadUserEntityByEmail(authentication.getName());
        List<Task> tasks = taskService.getTasksForUser(user.getName());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Update task status.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LAUNDRY_STAFF', 'DELIVERY_STAFF', 'ADMIN')")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User user = userDetailsService.loadUserEntityByEmail(authentication.getName());
            String statusStr = body.get("status");

            if (statusStr == null || statusStr.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }

            TaskStatus newStatus = TaskStatus.valueOf(statusStr.toUpperCase());
            Task updated = taskService.updateTaskStatus(id, newStatus, user);

            return ResponseEntity.ok(Map.of(
                    "message", "Task status updated to " + newStatus,
                    "task", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get a specific task.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAUNDRY_STAFF', 'DELIVERY_STAFF', 'ADMIN')")
    public ResponseEntity<?> getTask(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Task not found")));
    }
}
