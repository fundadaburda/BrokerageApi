package com.brokerage.dto;

import com.brokerage.entity.Side;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new order")
public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer ID placing the order", example = "1")
    private Long customerId;

    @NotBlank(message = "Asset name is required")
    @Schema(description = "Name of the asset to buy/sell", example = "AAPL")
    private String assetName;

    @NotNull(message = "Order side is required")
    @Schema(description = "Order side - BUY or SELL", example = "BUY")
    private Side side;

    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0")
    @Schema(description = "Number of shares to buy/sell", example = "10")
    private BigDecimal size;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Price per share in TRY", example = "150.50")
    private BigDecimal price;
}