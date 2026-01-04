package com.laundry.lms.service;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for finance management - payments and invoices.
 */
@Service
public class FinanceService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final LaundryOrderRepository orderRepository;
    private final AuditLogRepository auditLogRepository;

    public FinanceService(PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            LaundryOrderRepository orderRepository,
            AuditLogRepository auditLogRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Generate an invoice for an order.
     */
    @Transactional
    public Invoice generateInvoice(Long orderId, User creator) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Check if invoice already exists
        if (invoiceRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Invoice already exists for this order");
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceNo("INV-" + System.currentTimeMillis());
        invoice.setAmount(order.getPrice());
        invoice.setIssuedAt(LocalDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);
        createAuditLog(creator, "GENERATE_INVOICE", "Invoice", saved.getId(), null, orderId.toString());
        return saved;
    }

    /**
     * Get all payments.
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get payments by status.
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    /**
     * Get payment by order ID.
     */
    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    /**
     * Update payment status (idempotent).
     */
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus, User updater) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        // Idempotent check - if already in the requested status, return as-is
        if (payment.getStatus() == newStatus) {
            return payment;
        }

        // Prevent changing from PAID back to other states
        if (payment.getStatus() == PaymentStatus.PAID && newStatus != PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot change payment status from PAID");
        }

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(newStatus);

        Payment updated = paymentRepository.save(payment);
        createAuditLog(updater, "UPDATE_PAYMENT_STATUS", "Payment", paymentId,
                oldStatus.name(), newStatus.name());
        return updated;
    }

    /**
     * Mark payment as PAID.
     */
    @Transactional
    public Payment markAsPaid(Long paymentId, User updater) {
        return updatePaymentStatus(paymentId, PaymentStatus.PAID, updater);
    }

    /**
     * Delete a payment record.
     */
    @Transactional
    public void deletePayment(Long paymentId, User admin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        createAuditLog(admin, "DELETE_PAYMENT", "Payment", paymentId,
                payment.getAmountLkr().toString(), null);
        paymentRepository.delete(payment);
    }

    /**
     * Get invoice for an order.
     */
    public Optional<Invoice> getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId);
    }

    /**
     * Get all invoices.
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Finance analytics.
     */
    public FinanceStats getFinanceStats() {
        List<Payment> allPayments = paymentRepository.findAll();

        BigDecimal totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmountLkr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingAmount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmountLkr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.PAID).count();
        long pendingCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();

        return new FinanceStats(totalRevenue, pendingAmount, paidCount, pendingCount);
    }

    public record FinanceStats(
            BigDecimal totalRevenue,
            BigDecimal pendingAmount,
            long paidCount,
            long pendingCount) {
    }

    private void createAuditLog(User actor, String action, String entityType, Long entityId,
            String before, String after) {
        AuditLog log = AuditLog.create(actor, action, entityType, entityId, before, after);
        auditLogRepository.save(log);
    }
}
