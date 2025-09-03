package com.brokerage.service;

import com.brokerage.dto.CreateOrderRequest;
import com.brokerage.dto.OrderResponse;
import com.brokerage.entity.*;
import com.brokerage.exception.InsufficientBalanceException;
import com.brokerage.exception.InvalidOrderStatusException;
import com.brokerage.exception.ResourceNotFoundException;
import com.brokerage.repository.CustomerRepository;
import com.brokerage.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Asset tryAsset;
    private Asset stockAsset;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setUsername("testuser");
        testCustomer.setRole("CUSTOMER");

        tryAsset = Asset.builder()
                .id(1L)
                .customer(testCustomer)
                .assetName("TRY")
                .size(new BigDecimal("10000"))
                .usableSize(new BigDecimal("10000"))
                .build();

        stockAsset = Asset.builder()
                .id(2L)
                .customer(testCustomer)
                .assetName("AAPL")
                .size(new BigDecimal("100"))
                .usableSize(new BigDecimal("100"))
                .build();
    }

    @Test
    void createBuyOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L, "AAPL", Side.BUY, new BigDecimal("10"), new BigDecimal("150")
        );

        Order savedOrder = Order.builder()
                .id(1L)
                .customer(testCustomer)
                .assetName("AAPL")
                .orderSide(Side.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(Status.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(assetService.getOrCreateAsset(testCustomer, "TRY")).thenReturn(tryAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("AAPL", response.getAssetName());
        assertEquals(Side.BUY, response.getOrderSide());
        verify(assetService).updateAssetBalance(tryAsset, BigDecimal.ZERO, new BigDecimal("-1500"));
    }

    @Test
    void createBuyOrder_InsufficientBalance() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L, "AAPL", Side.BUY, new BigDecimal("100"), new BigDecimal("150")
        );

        tryAsset.setUsableSize(new BigDecimal("1000"));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(assetService.getOrCreateAsset(testCustomer, "TRY")).thenReturn(tryAsset);

        assertThrows(InsufficientBalanceException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createSellOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L, "AAPL", Side.SELL, new BigDecimal("10"), new BigDecimal("150")
        );

        Order savedOrder = Order.builder()
                .id(1L)
                .customer(testCustomer)
                .assetName("AAPL")
                .orderSide(Side.SELL)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(Status.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(assetService.getOrCreateAsset(testCustomer, "AAPL")).thenReturn(stockAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(Side.SELL, response.getOrderSide());
        verify(assetService).updateAssetBalance(stockAsset, BigDecimal.ZERO, new BigDecimal("-10"));
    }

    @Test
    void deleteOrder_Success() {
        Order pendingOrder = Order.builder()
                .id(1L)
                .customer(testCustomer)
                .assetName("AAPL")
                .orderSide(Side.BUY)
                .size(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .status(Status.PENDING)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByIdAndCustomer(1L, testCustomer)).thenReturn(Optional.of(pendingOrder));
        when(assetService.getOrCreateAsset(testCustomer, "TRY")).thenReturn(tryAsset);

        orderService.deleteOrder(1L, 1L);

        verify(assetService).updateAssetBalance(tryAsset, BigDecimal.ZERO, new BigDecimal("1500"));
        verify(orderRepository).save(pendingOrder);
        assertEquals(Status.CANCELED, pendingOrder.getStatus());
    }

    @Test
    void deleteOrder_InvalidStatus() {
        Order matchedOrder = Order.builder()
                .id(1L)
                .customer(testCustomer)
                .status(Status.MATCHED)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByIdAndCustomer(1L, testCustomer)).thenReturn(Optional.of(matchedOrder));

        assertThrows(InvalidOrderStatusException.class, () -> orderService.deleteOrder(1L, 1L));
    }

    @Test
    void deleteOrder_OrderNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByIdAndCustomer(1L, testCustomer)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(1L, 1L));
    }
}