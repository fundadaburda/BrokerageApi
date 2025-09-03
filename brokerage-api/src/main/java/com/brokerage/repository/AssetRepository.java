package com.brokerage.repository;

import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByCustomerAndAssetName(Customer customer, String assetName);
    List<Asset> findByCustomer(Customer customer);
    List<Asset> findByCustomerAndAssetNameContainingIgnoreCase(Customer customer, String assetName);
}