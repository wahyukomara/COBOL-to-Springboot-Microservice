package com.wkom.banking.ledger.consumer;

import com.wkom.banking.events.LedgerUpdatedEvent;
import com.wkom.banking.events.PaymentInitiatedEvent;
import com.wkom.banking.ledger.entity.LedgerEntry;
import com.wkom.banking.ledger.repository.LedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final LedgerRepository ledgerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventConsumer(LedgerRepository ledgerRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.ledgerRepository = ledgerRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Listens for PaymentInitiatedEvent from payment-service.
     * Performs double-entry bookkeeping in a single ACID transaction.
     * Emits LedgerUpdatedEvent upon completion.
     */
    @KafkaListener(topics = "payments.initiated", groupId = "ledger-group",
            properties = {"spring.json.value.default.type=com.wkom.banking.events.PaymentInitiatedEvent"})
    @Transactional
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Ledger processing payment: transactionId={}, amount={}", event.transactionId(), event.amount());

        try {
            // Validate amount
            if (event.amount().compareTo(BigDecimal.ZERO) <= 0) {
                emitFailure(event, "Invalid amount");
                return;
            }

            // --- Double-Entry Bookkeeping (ACID Transaction) ---

            // 1. DEBIT entry: money leaves the source account
            LedgerEntry debitEntry = new LedgerEntry();
            debitEntry.setTransactionId(event.transactionId());
            debitEntry.setAccountId(event.sourceAccountId());
            debitEntry.setAmount(event.amount());
            debitEntry.setEntryType("DEBIT");
            debitEntry.setStatus("SUCCESS");
            ledgerRepository.save(debitEntry);

            // 2. CREDIT entry: money arrives at the destination account
            LedgerEntry creditEntry = new LedgerEntry();
            creditEntry.setTransactionId(event.transactionId());
            creditEntry.setAccountId(event.destinationAccountId());
            creditEntry.setAmount(event.amount());
            creditEntry.setEntryType("CREDIT");
            creditEntry.setStatus("SUCCESS");
            ledgerRepository.save(creditEntry);

            log.info("Ledger entries created for transaction {}", event.transactionId());

            // --- Emit LedgerUpdatedEvent for DEBIT ---
            LedgerUpdatedEvent debitEvent = new LedgerUpdatedEvent(
                    event.transactionId(),
                    event.sourceAccountId(),
                    event.amount(),
                    "DEBIT",
                    "SUCCESS"
            );
            kafkaTemplate.send("ledger.updated", event.transactionId(), debitEvent);

            // --- Emit LedgerUpdatedEvent for CREDIT ---
            LedgerUpdatedEvent creditEvent = new LedgerUpdatedEvent(
                    event.transactionId(),
                    event.destinationAccountId(),
                    event.amount(),
                    "CREDIT",
                    "SUCCESS"
            );
            kafkaTemplate.send("ledger.updated", event.transactionId(), creditEvent);

            log.info("LedgerUpdatedEvents emitted for transaction {}", event.transactionId());

        } catch (Exception e) {
            log.error("Ledger processing failed for transaction {}: {}", event.transactionId(), e.getMessage());
            emitFailure(event, e.getMessage());
        }
    }

    private void emitFailure(PaymentInitiatedEvent event, String reason) {
        log.warn("Emitting FAILED LedgerUpdatedEvent for transaction {}: {}", event.transactionId(), reason);
        LedgerUpdatedEvent failedEvent = new LedgerUpdatedEvent(
                event.transactionId(),
                event.sourceAccountId(),
                event.amount(),
                "DEBIT",
                "FAILED"
        );
        kafkaTemplate.send("ledger.updated", event.transactionId(), failedEvent);
    }
}
