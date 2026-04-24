package com.wkom.banking.events;

import java.math.BigDecimal;

public record LedgerUpdatedEvent(
        String transactionId,
        String accountId,
        BigDecimal amount,
        String entryType, // e.g., CREDIT or DEBIT
        String status // e.g., SUCCESS or FAILED
) {}
