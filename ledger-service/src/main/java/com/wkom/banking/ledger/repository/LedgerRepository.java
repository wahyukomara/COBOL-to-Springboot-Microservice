package com.wkom.banking.ledger.repository;

import com.wkom.banking.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByTransactionId(String transactionId);
    List<LedgerEntry> findByAccountId(String accountId);
}
