package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.payment.PaymentCreateDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentDTO;
import com.ecommerce.E_commerce.dto.payment.PaymentUpdateDTO;
import com.ecommerce.E_commerce.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPayment_ShouldReturnCreatedPayment() throws Exception {
        PaymentCreateDTO createDTO = new PaymentCreateDTO(
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "TXN-123",
                "Payment notes"
        );

        PaymentDTO paymentDTO = new PaymentDTO(
                1L,
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "PENDING",
                Instant.now(),
                "TXN-123",
                "Payment notes",
                Instant.now(),
                Instant.now(),
                true
        );

        Mockito.when(paymentService.create(any(PaymentCreateDTO.class))).thenReturn(paymentDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getPaymentById_ShouldReturnPayment() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO(
                1L,
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "PENDING",
                Instant.now(),
                "TXN-123",
                "Payment notes",
                Instant.now(),
                Instant.now(),
                true
        );

        Mockito.when(paymentService.getById(1L)).thenReturn(paymentDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getAllPayments_ShouldReturnPage() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO(
                1L,
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "PENDING",
                Instant.now(),
                "TXN-123",
                null,
                Instant.now(),
                Instant.now(),
                true
        );

        Page<PaymentDTO> page = new PageImpl<>(List.of(paymentDTO));
        Mockito.when(paymentService.findAll(any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/payments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPage() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO(
                1L,
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "PENDING",
                Instant.now(),
                "TXN-123",
                null,
                Instant.now(),
                Instant.now(),
                true
        );

        Page<PaymentDTO> page = new PageImpl<>(List.of(paymentDTO));
        Mockito.when(paymentService.findByOrderId(anyLong(), any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/payments/order/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getPaymentsByStatus_ShouldReturnPage() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO(
                1L,
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "COMPLETED",
                Instant.now(),
                "TXN-123",
                null,
                Instant.now(),
                Instant.now(),
                true
        );

        Page<PaymentDTO> page = new PageImpl<>(List.of(paymentDTO));
        Mockito.when(paymentService.findByStatus(anyString(), any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/payments/status/COMPLETED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updatePayment_ShouldReturnUpdatedPayment() throws Exception {
        PaymentUpdateDTO updateDTO = new PaymentUpdateDTO("COMPLETED", null, null, null);

        PaymentDTO paymentDTO = new PaymentDTO(
                1L,
                1L,
                new BigDecimal("199.98"),
                "CREDIT_CARD",
                "COMPLETED",
                Instant.now(),
                "TXN-123",
                null,
                Instant.now(),
                Instant.now(),
                true
        );

        Mockito.when(paymentService.update(eq(1L), any(PaymentUpdateDTO.class))).thenReturn(paymentDTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deletePayment_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/payments/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(paymentService).delete(1L);
    }

    @Test
    void getPaymentCount_ShouldReturnCount() throws Exception {
        Mockito.when(paymentService.countByStatus("PENDING")).thenReturn(3L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/payments/stats/count")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }
}

