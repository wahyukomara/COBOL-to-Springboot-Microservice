package com.wkom.banking.transaction.controller;

import com.wkom.banking.transaction.entity.TransactionRecord;
import com.wkom.banking.transaction.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionRecord> getTransaction(@PathVariable String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
