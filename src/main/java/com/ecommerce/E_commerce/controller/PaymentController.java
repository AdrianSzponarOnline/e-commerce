package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentCreateDTO dto) {
        PaymentDTO payment = paymentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PaymentDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateDTO dto) {
        PaymentDTO payment = paymentService.update(id, dto);
        return ResponseEntity.ok(payment);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @paymentService.isPaymentOwner(#id, authentication.name))")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        PaymentDTO payment = paymentService.getById(id);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<PaymentDTO>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.findAll(pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #orderId != null and @orderService.isOrderOwner(#orderId, authentication.name))")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsByOrderId(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.findByOrderId(orderId, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.findByStatus(status, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/order/{orderId}/status/{status}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #orderId != null and @orderService.isOrderOwner(#orderId, authentication.name))")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsByOrderIdAndStatus(
            @PathVariable Long orderId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.findByOrderIdAndStatus(orderId, status, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/filter")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<PaymentDTO>> filterPayments(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDTO> payments = paymentService.findByMultipleCriteria(orderId, status, method, isActive, startDate, endDate, pageable);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/stats/count")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #orderId != null and @orderService.isOrderOwner(#orderId, authentication.name))")
    public ResponseEntity<Long> getPaymentCount(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status) {
        long count;
        if (orderId != null && status != null) {
            count = paymentService.countByStatus(status);
        } else if (orderId != null) {
            count = paymentService.countByOrderId(orderId);
        } else if (status != null) {
            count = paymentService.countByStatus(status);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(count);
    }
}

