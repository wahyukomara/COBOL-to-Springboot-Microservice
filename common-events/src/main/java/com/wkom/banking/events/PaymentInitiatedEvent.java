package com.wkom.banking.events;

import java.math.BigDecimal;

public record PaymentInitiatedEvent(
        String transactionId,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        String status // e.g., PENDING
) {}
