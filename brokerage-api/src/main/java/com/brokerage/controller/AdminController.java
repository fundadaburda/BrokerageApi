package com.brokerage.controller;

import com.brokerage.dto.MatchOrderRequest;
import com.brokerage.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final OrderService orderService;

    @PostMapping("/match-orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Match pending orders", description = "Match pending orders and update asset balances (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders matched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or order not in PENDING status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> matchOrders(@Valid @RequestBody MatchOrderRequest request) {
        orderService.matchOrders(request.getOrderIds());
        return ResponseEntity.ok().build();
    }
}