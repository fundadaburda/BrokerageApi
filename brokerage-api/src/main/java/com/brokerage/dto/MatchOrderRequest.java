package com.brokerage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchOrderRequest {
    @NotNull(message = "Order IDs are required")
    private List<Long> orderIds;
}