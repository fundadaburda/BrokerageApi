package com.brokerage.repository;

import com.brokerage.entity.Customer;
import com.brokerage.entity.Order;
import com.brokerage.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerAndCreateDateBetween(Customer customer, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.customer = :customer " +
           "AND o.createDate BETWEEN :startDate AND :endDate " +
           "AND (:status IS NULL OR o.status = :status)")
    List<Order> findByCustomerAndDateRangeAndStatus(@Param("customer") Customer customer,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     @Param("status") Status status);
    
    Optional<Order> findByIdAndCustomer(Long id, Customer customer);
    
    List<Order> findByStatus(Status status);
}