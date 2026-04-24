package com.wkom.banking.payment.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount
) {}
