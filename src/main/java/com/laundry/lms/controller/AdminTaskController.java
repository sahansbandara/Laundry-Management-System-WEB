package com.laundry.lms.controller;

import com.laundry.lms.dto.TaskRequest;
import com.laundry.lms.model.*;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Task Controller - for admin to create and manage tasks.
 */
@RestController
@RequestMapping("/api/admin/tasks")
@CrossOrigin(origins = "*")
public class AdminTaskController {

    private final TaskService taskService;
    private final CustomUserDetailsService userDetailsService;

    public AdminTaskController(TaskService taskService,
            CustomUserDetailsService userDetailsService) {
        this.taskService = taskService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Create a new task.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest request,
            Authentication authentication) {
        try {
            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());

            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setAssignedTo(request.getAssignedTo());
            task.setDueDate(request.getDueDate());
            task.setPrice(request.getPrice());
            task.setNotes(request.getNotes());
            task.setStatus(TaskStatus.PENDING);

            Task created = taskService.createTask(task, admin);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Task created successfully",
                    "task", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create task: " + e.getMessage()));
        }
    }

    /**
     * Get all tasks.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTasks(@RequestParam(required = false) String status) {
        try {
            List<Task> tasks;
            if (status != null && !status.isBlank()) {
                TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
                tasks = taskService.getTasksByStatus(taskStatus);
            } else {
                tasks = taskService.getAllTasks();
            }
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
    }

    /**
     * Reassign a task to another user.
     */
    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reassignTask(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            String newAssignee = body.get("assignedTo");

            if (newAssignee == null || newAssignee.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "New assignee is required"));
            }

            Task updated = taskService.reassignTask(id, newAssignee, admin);
            return ResponseEntity.ok(Map.of(
                    "message", "Task reassigned to " + newAssignee,
                    "task", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a task.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            taskService.deleteTask(id, admin);
            return ResponseEntity.ok(Map.of("message", "Task deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
