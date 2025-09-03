package com.brokerage.service;

import com.brokerage.dto.AssetResponse;
import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;
import com.brokerage.exception.ResourceNotFoundException;
import com.brokerage.repository.AssetRepository;
import com.brokerage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;

    public List<AssetResponse> listAssets(Long customerId, String assetNameFilter) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Asset> assets;
        if (assetNameFilter != null && !assetNameFilter.isEmpty()) {
            assets = assetRepository.findByCustomerAndAssetNameContainingIgnoreCase(customer, assetNameFilter);
        } else {
            assets = assetRepository.findByCustomer(customer);
        }

        return assets.stream()
                .map(this::toAssetResponse)
                .collect(Collectors.toList());
    }

    public Asset getOrCreateAsset(Customer customer, String assetName) {
        return assetRepository.findByCustomerAndAssetName(customer, assetName)
                .orElseGet(() -> {
                    Asset newAsset = Asset.builder()
                            .customer(customer)
                            .assetName(assetName)
                            .size(BigDecimal.ZERO)
                            .usableSize(BigDecimal.ZERO)
                            .build();
                    return assetRepository.save(newAsset);
                });
    }

    public void updateAssetBalance(Asset asset, BigDecimal sizeChange, BigDecimal usableSizeChange) {
        asset.setSize(asset.getSize().add(sizeChange));
        asset.setUsableSize(asset.getUsableSize().add(usableSizeChange));
        assetRepository.save(asset);
    }

    private AssetResponse toAssetResponse(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .customerId(asset.getCustomer().getId())
                .assetName(asset.getAssetName())
                .size(asset.getSize())
                .usableSize(asset.getUsableSize())
                .build();
    }
}