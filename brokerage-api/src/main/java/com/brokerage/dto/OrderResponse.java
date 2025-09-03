package com.brokerage.dto;

import com.brokerage.entity.Side;
import com.brokerage.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long customerId;
    private String assetName;
    private Side orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private Status status;
    private LocalDateTime createDate;
}