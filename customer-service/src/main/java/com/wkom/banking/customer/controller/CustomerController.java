package com.wkom.banking.customer.controller;

import com.wkom.banking.customer.entity.Customer;
import com.wkom.banking.customer.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer request) {
        request.setCustomerId("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setKycStatus("PENDING");
        Customer saved = customerRepository.save(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
