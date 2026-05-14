package com.works.repository;

import com.works.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByEmailEqualsOrPhoneEqualsAllIgnoreCase(String email, String phone);
    Optional<Customer> findByEnabledTrueAndEmailIgnoreCaseOrEnabledTrueAndPhoneIgnoreCase(String email, String phone);
}