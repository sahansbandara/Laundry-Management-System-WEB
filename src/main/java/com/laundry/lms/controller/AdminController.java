package com.laundry.lms.controller;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.*;
import com.laundry.lms.security.CustomUserDetailsService;
import com.laundry.lms.service.FinanceService;
import com.laundry.lms.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin Controller - for admin to manage users, services, settings, and view
 * analytics.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final PressingCategoryPriceRepository pressingPriceRepository;
    private final AuditLogRepository auditLogRepository;
    private final OrderService orderService;
    private final FinanceService financeService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public AdminController(UserRepository userRepository,
            ServiceCatalogRepository serviceCatalogRepository,
            PressingCategoryPriceRepository pressingPriceRepository,
            AuditLogRepository auditLogRepository,
            OrderService orderService,
            FinanceService financeService,
            PasswordEncoder passwordEncoder,
            CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.pressingPriceRepository = pressingPriceRepository;
        this.auditLogRepository = auditLogRepository;
        this.orderService = orderService;
        this.financeService = financeService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Get all users.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream().map(u -> Map.of(
                "id", u.getId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "role", u.getRole().name())).toList());
    }

    /**
     * Create a new user.
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            String name = body.get("name");
            String email = body.get("email");
            String password = body.get("password");
            String roleStr = body.get("role");

            if (name == null || email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name, email, and password are required"));
            }

            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            UserRole role = roleStr != null ? UserRole.valueOf(roleStr.toUpperCase()) : UserRole.CUSTOMER;

            User user = new User(name, email, passwordEncoder.encode(password), role);
            User saved = userRepository.save(user);

            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            createAuditLog(admin, "CREATE_USER", "User", saved.getId(), null, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "User created",
                    "userId", saved.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }
    }

    /**
     * Update user role.
     */
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String roleStr = body.get("role");
            if (roleStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role is required"));
            }

            String oldRole = user.getRole().name();
            UserRole newRole = UserRole.valueOf(roleStr.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);

            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            createAuditLog(admin, "UPDATE_USER_ROLE", "User", id, oldRole, newRole.name());

            return ResponseEntity.ok(Map.of("message", "User role updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete user (soft delete by just removing - in production would be status
     * change).
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            createAuditLog(admin, "DELETE_USER", "User", id, user.getEmail(), null);

            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "User deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== SERVICE MANAGEMENT ====================

    /**
     * Get all services.
     */
    @GetMapping("/services")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllServices() {
        return ResponseEntity.ok(serviceCatalogRepository.findAll());
    }

    /**
     * Update service pricing.
     */
    @PatchMapping("/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateService(@PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        try {
            ServiceCatalog service = serviceCatalogRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found"));

            String oldPrice = service.getBasePrice().toString();

            if (body.containsKey("basePrice")) {
                service.setBasePrice(((Number) body.get("basePrice")).doubleValue());
            }
            if (body.containsKey("active")) {
                service.setActive((Boolean) body.get("active"));
            }
            if (body.containsKey("description")) {
                service.setDescription((String) body.get("description"));
            }

            serviceCatalogRepository.save(service);

            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            createAuditLog(admin, "UPDATE_SERVICE", "ServiceCatalog", id, oldPrice, service.getBasePrice().toString());

            return ResponseEntity.ok(Map.of("message", "Service updated", "service", service));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pressing category prices.
     */
    @GetMapping("/pressing-prices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPressingPrices() {
        return ResponseEntity.ok(pressingPriceRepository.findAll());
    }

    /**
     * Update pressing category price.
     */
    @PatchMapping("/pressing-prices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePressingPrice(@PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        try {
            PressingCategoryPrice price = pressingPriceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Pressing price not found"));

            String oldPrice = price.getPricePerItem().toString();

            if (body.containsKey("pricePerItem")) {
                price.setPricePerItem(((Number) body.get("pricePerItem")).doubleValue());
            }
            if (body.containsKey("active")) {
                price.setActive((Boolean) body.get("active"));
            }

            pressingPriceRepository.save(price);

            User admin = userDetailsService.loadUserEntityByEmail(authentication.getName());
            createAuditLog(admin, "UPDATE_PRESSING_PRICE", "PressingCategoryPrice", id,
                    oldPrice, price.getPricePerItem().toString());

            return ResponseEntity.ok(Map.of("message", "Pressing price updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== ANALYTICS ====================

    /**
     * Get analytics dashboard data.
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAnalytics() {
        List<LaundryOrder> allOrders = orderService.getAllOrders();
        FinanceService.FinanceStats financeStats = financeService.getFinanceStats();
        List<User> allUsers = userRepository.findAll();

        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long completedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long totalCustomers = allUsers.stream().filter(u -> u.getRole() == UserRole.CUSTOMER).count();
        long totalStaff = allUsers.stream()
                .filter(u -> u.getRole() != UserRole.CUSTOMER && u.getRole() != UserRole.ADMIN).count();

        return ResponseEntity.ok(Map.of(
                "orders", Map.of(
                        "total", totalOrders,
                        "pending", pendingOrders,
                        "completed", completedOrders),
                "finance", financeStats,
                "users", Map.of(
                        "totalCustomers", totalCustomers,
                        "totalStaff", totalStaff)));
    }

    // ==================== AUDIT LOGS ====================

    /**
     * Get audit logs.
     */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(logs);
    }

    private void createAuditLog(User actor, String action, String entityType, Long entityId,
            String before, String after) {
        AuditLog log = AuditLog.create(actor, action, entityType, entityId, before, after);
        auditLogRepository.save(log);
    }
}
