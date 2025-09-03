package com.brokerage.controller;

import com.brokerage.dto.CreateOrderRequest;
import com.brokerage.dto.OrderResponse;
import com.brokerage.entity.Side;
import com.brokerage.entity.Status;
import com.brokerage.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private CreateOrderRequest createOrderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        createOrderRequest = new CreateOrderRequest(
                1L, "AAPL", Side.BUY, new BigDecimal("10"), new BigDecimal("150")
        );

        orderResponse = OrderResponse.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(Side.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(Status.PENDING)
                .createDate(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("BUY"));

        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createOrder_ValidationError() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
                null, "", Side.BUY, null, new BigDecimal("-1")
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listOrders_Success() throws Exception {
        List<OrderResponse> orders = Arrays.asList(orderResponse);
        when(orderService.listOrders(anyLong(), any(), any(), any())).thenReturn(orders);

        mockMvc.perform(get("/api/orders")
                .param("customerId", "1")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].assetName").value("AAPL"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deleteOrder_Success() throws Exception {
        doNothing().when(orderService).deleteOrder(1L, 1L);

        mockMvc.perform(delete("/api/orders/1")
                .param("customerId", "1"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L, 1L);
    }

    @Test
    void createOrder_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isForbidden());
    }
}