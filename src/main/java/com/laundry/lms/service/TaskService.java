package com.laundry.lms.service;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.AuditLogRepository;
import com.laundry.lms.repository.TaskRepository;
import com.laundry.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for task management operations.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public TaskService(TaskRepository taskRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Get tasks assigned to a specific user.
     */
    public List<Task> getTasksForUser(String assignedTo) {
        return taskRepository.findByAssignedTo(assignedTo);
    }

    /**
     * Get all tasks.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Get tasks by status.
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Get task by ID.
     */
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    /**
     * Create a new task.
     */
    @Transactional
    public Task createTask(Task task, User creator) {
        Task saved = taskRepository.save(task);
        createAuditLog(creator, "CREATE_TASK", "Task", saved.getId(), null, task.getTitle());
        return saved;
    }

    /**
     * Update task status.
     */
    @Transactional
    public Task updateTaskStatus(Long taskId, TaskStatus newStatus, User updater) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        String oldStatus = task.getStatus().name();
        task.setStatus(newStatus);
        Task updated = taskRepository.save(task);

        createAuditLog(updater, "UPDATE_TASK_STATUS", "Task", taskId, oldStatus, newStatus.name());
        return updated;
    }

    /**
     * Reassign task to another user.
     */
    @Transactional
    public Task reassignTask(Long taskId, String newAssignee, User admin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        String oldAssignee = task.getAssignedTo();
        task.setAssignedTo(newAssignee);
        Task updated = taskRepository.save(task);

        createAuditLog(admin, "REASSIGN_TASK", "Task", taskId, oldAssignee, newAssignee);
        return updated;
    }

    /**
     * Delete a task.
     */
    @Transactional
    public void deleteTask(Long taskId, User admin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        createAuditLog(admin, "DELETE_TASK", "Task", taskId, task.getTitle(), null);
        taskRepository.delete(task);
    }

    private void createAuditLog(User actor, String action, String entityType, Long entityId,
            String before, String after) {
        AuditLog log = AuditLog.create(actor, action, entityType, entityId, before, after);
        auditLogRepository.save(log);
    }
}
