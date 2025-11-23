package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.order.OrderCreateDTO;
import com.ecommerce.E_commerce.dto.order.OrderDTO;
import com.ecommerce.E_commerce.dto.order.OrderUpdateDTO;
import com.ecommerce.E_commerce.dto.orderitem.OrderItemCreateDTO;
import com.ecommerce.E_commerce.service.OrderService;
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

@WebMvcTest(controllers = OrderController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        OrderCreateDTO createDTO = new OrderCreateDTO(
                1L,
                "NEW",
                List.of(new OrderItemCreateDTO(1L, 2))
        );

        OrderDTO orderDTO = new OrderDTO(
                1L,
                1L,
                null,
                "NEW",
                new BigDecimal("199.98"),
                List.of(),
                Instant.now(),
                Instant.now(),
                true
        );

        Mockito.when(orderService.create(anyLong(), any(OrderCreateDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        OrderDTO orderDTO = new OrderDTO(
                1L,
                1L,
                null,
                "NEW",
                new BigDecimal("199.98"),
                List.of(),
                Instant.now(),
                Instant.now(),
                true
        );

        Mockito.when(orderService.getById(1L)).thenReturn(orderDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void getAllOrders_ShouldReturnPage() throws Exception {
        OrderDTO orderDTO = new OrderDTO(
                1L,
                1L,
                null,
                "NEW",
                new BigDecimal("199.98"),
                List.of(),
                Instant.now(),
                Instant.now(),
                true
        );

        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        Mockito.when(orderService.findAll(any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getOrdersByUserId_ShouldReturnPage() throws Exception {
        OrderDTO orderDTO = new OrderDTO(
                1L,
                1L,
                null,
                "NEW",
                new BigDecimal("199.98"),
                List.of(),
                Instant.now(),
                Instant.now(),
                true
        );

        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        Mockito.when(orderService.findByUserId(anyLong(), any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/user/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getOrdersByStatus_ShouldReturnPage() throws Exception {
        OrderDTO orderDTO = new OrderDTO(
                1L,
                1L,
                null,
                "CONFIRMED",
                new BigDecimal("199.98"),
                List.of(),
                Instant.now(),
                Instant.now(),
                true
        );

        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        Mockito.when(orderService.findByStatus(anyString(), any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/status/CONFIRMED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void cancelOrder_ShouldReturnCancelledOrder() throws Exception {
        OrderDTO orderDTO = new OrderDTO(
                1L,
                1L,
                null,
                "CANCELLED",
                new BigDecimal("199.98"),
                List.of(),
                Instant.now(),
                Instant.now(),
                true
        );

        Mockito.when(orderService.cancelOrder(1L)).thenReturn(orderDTO);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void updateOrder_ShouldReturnUpdatedOrder() throws Exception {
        OrderUpdateDTO updateDTO = new OrderUpdateDTO("CONFIRMED", null);

        OrderDTO orderDTO = new OrderDTO(
                1L,
                1L,
                null,
                "CONFIRMED",
                new BigDecimal("199.98"),
                List.of(),
                Instant.now(),
                Instant.now(),
                true
        );

        Mockito.when(orderService.update(eq(1L), any(OrderUpdateDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(orderService).delete(1L);
    }

    @Test
    void getOrderCount_ShouldReturnCount() throws Exception {
        Mockito.when(orderService.countByStatus("NEW")).thenReturn(5L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/stats/count")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }
}

