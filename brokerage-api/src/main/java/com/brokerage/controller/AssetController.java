package com.brokerage.controller;

import com.brokerage.dto.AssetResponse;
import com.brokerage.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Asset management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AssetController {
    private final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "List customer assets", description = "List all assets for a customer with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<List<AssetResponse>> listAssets(
            @Parameter(description = "Customer ID") @RequestParam Long customerId,
            @Parameter(description = "Filter by asset name (partial match)") @RequestParam(required = false) String assetName) {
        List<AssetResponse> assets = assetService.listAssets(customerId, assetName);
        return ResponseEntity.ok(assets);
    }
}