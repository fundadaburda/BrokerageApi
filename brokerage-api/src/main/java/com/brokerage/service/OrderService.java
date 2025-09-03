package com.brokerage.service;

import com.brokerage.dto.CreateOrderRequest;
import com.brokerage.dto.OrderResponse;
import com.brokerage.entity.*;
import com.brokerage.exception.InsufficientBalanceException;
import com.brokerage.exception.InvalidOrderStatusException;
import com.brokerage.exception.ResourceNotFoundException;
import com.brokerage.repository.CustomerRepository;
import com.brokerage.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AssetService assetService;

    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        BigDecimal requiredAmount;
        Asset assetToUpdate;

        if (request.getSide() == Side.BUY) {
            Asset tryAsset = assetService.getOrCreateAsset(customer, "TRY");
            requiredAmount = request.getSize().multiply(request.getPrice());
            
            if (tryAsset.getUsableSize().compareTo(requiredAmount) < 0) {
                throw new InsufficientBalanceException("Insufficient TRY balance");
            }
            
            assetService.updateAssetBalance(tryAsset, BigDecimal.ZERO, requiredAmount.negate());
        } else {
            Asset assetToSell = assetService.getOrCreateAsset(customer, request.getAssetName());
            
            if (assetToSell.getUsableSize().compareTo(request.getSize()) < 0) {
                throw new InsufficientBalanceException("Insufficient " + request.getAssetName() + " balance");
            }
            
            assetService.updateAssetBalance(assetToSell, BigDecimal.ZERO, request.getSize().negate());
        }

        Order order = Order.builder()
                .customer(customer)
                .assetName(request.getAssetName())
                .orderSide(request.getSide())
                .size(request.getSize())
                .price(request.getPrice())
                .status(Status.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        return toOrderResponse(savedOrder);
    }

    public List<OrderResponse> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate, Status status) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Order> orders = orderRepository.findByCustomerAndDateRangeAndStatus(customer, startDate, endDate, status);
        
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    public void deleteOrder(Long orderId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Order order = orderRepository.findByIdAndCustomer(orderId, customer)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != Status.PENDING) {
            throw new InvalidOrderStatusException("Only PENDING orders can be deleted");
        }

        if (order.getOrderSide() == Side.BUY) {
            Asset tryAsset = assetService.getOrCreateAsset(customer, "TRY");
            BigDecimal amountToReturn = order.getSize().multiply(order.getPrice());
            assetService.updateAssetBalance(tryAsset, BigDecimal.ZERO, amountToReturn);
        } else {
            Asset assetToReturn = assetService.getOrCreateAsset(customer, order.getAssetName());
            assetService.updateAssetBalance(assetToReturn, BigDecimal.ZERO, order.getSize());
        }

        order.setStatus(Status.CANCELED);
        orderRepository.save(order);
    }

    public void matchOrders(List<Long> orderIds) {
        for (Long orderId : orderIds) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
            
            if (order.getStatus() != Status.PENDING) {
                throw new InvalidOrderStatusException("Order " + orderId + " is not PENDING");
            }

            Customer customer = order.getCustomer();
            
            if (order.getOrderSide() == Side.BUY) {
                Asset boughtAsset = assetService.getOrCreateAsset(customer, order.getAssetName());
                assetService.updateAssetBalance(boughtAsset, order.getSize(), order.getSize());
            } else {
                Asset tryAsset = assetService.getOrCreateAsset(customer, "TRY");
                BigDecimal amount = order.getSize().multiply(order.getPrice());
                assetService.updateAssetBalance(tryAsset, amount, amount);
            }
            
            order.setStatus(Status.MATCHED);
            orderRepository.save(order);
        }
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .assetName(order.getAssetName())
                .orderSide(order.getOrderSide())
                .size(order.getSize())
                .price(order.getPrice())
                .status(order.getStatus())
                .createDate(order.getCreateDate())
                .build();
    }
}