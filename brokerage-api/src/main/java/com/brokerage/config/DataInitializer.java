package com.brokerage.config;

import com.brokerage.entity.Asset;
import com.brokerage.entity.Customer;
import com.brokerage.repository.AssetRepository;
import com.brokerage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            Customer admin = new Customer();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            customerRepository.save(admin);

            Customer customer1 = new Customer();
            customer1.setUsername("customer1");
            customer1.setPassword(passwordEncoder.encode("password123"));
            customer1.setRole("CUSTOMER");
            Customer savedCustomer1 = customerRepository.save(customer1);

            Asset tryAsset1 = Asset.builder()
                    .customer(savedCustomer1)
                    .assetName("TRY")
                    .size(new BigDecimal("100000"))
                    .usableSize(new BigDecimal("100000"))
                    .build();
            assetRepository.save(tryAsset1);

            Customer customer2 = new Customer();
            customer2.setUsername("customer2");
            customer2.setPassword(passwordEncoder.encode("password123"));
            customer2.setRole("CUSTOMER");
            Customer savedCustomer2 = customerRepository.save(customer2);

            Asset tryAsset2 = Asset.builder()
                    .customer(savedCustomer2)
                    .assetName("TRY")
                    .size(new BigDecimal("50000"))
                    .usableSize(new BigDecimal("50000"))
                    .build();
            assetRepository.save(tryAsset2);

            Asset appleAsset = Asset.builder()
                    .customer(savedCustomer2)
                    .assetName("AAPL")
                    .size(new BigDecimal("100"))
                    .usableSize(new BigDecimal("100"))
                    .build();
            assetRepository.save(appleAsset);
        }
    }
}