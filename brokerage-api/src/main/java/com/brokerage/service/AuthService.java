package com.brokerage.service;

import com.brokerage.dto.LoginRequest;
import com.brokerage.dto.LoginResponse;
import com.brokerage.entity.Customer;
import com.brokerage.exception.ResourceNotFoundException;
import com.brokerage.repository.CustomerRepository;
import com.brokerage.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = tokenProvider.generateToken(customer);
        
        return LoginResponse.builder()
                .token(token)
                .username(customer.getUsername())
                .role(customer.getRole())
                .customerId(customer.getId())
                .build();
    }

    public Customer registerCustomer(String username, String password) {
        if (customerRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setRole("CUSTOMER");
        
        return customerRepository.save(customer);
    }
}