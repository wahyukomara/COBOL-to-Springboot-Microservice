package com.wkom.banking.transaction.repository;

import com.wkom.banking.transaction.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    Optional<TransactionRecord> findByTransactionId(String transactionId);
}
