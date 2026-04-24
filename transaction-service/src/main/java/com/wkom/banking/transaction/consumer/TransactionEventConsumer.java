package com.wkom.banking.transaction.consumer;

import com.wkom.banking.events.PaymentInitiatedEvent;
import com.wkom.banking.events.LedgerUpdatedEvent;
import com.wkom.banking.transaction.entity.TransactionRecord;
import com.wkom.banking.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final TransactionRepository transactionRepository;

    public TransactionEventConsumer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Listens for PaymentInitiatedEvent from payment-service.
     * Creates a TransactionRecord with status PENDING.
     */
    @KafkaListener(topics = "payments.initiated", groupId = "transaction-group",
            properties = {"spring.json.value.default.type=com.wkom.banking.events.PaymentInitiatedEvent"})
    @Transactional
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Received PaymentInitiatedEvent: transactionId={}", event.transactionId());
        
        TransactionRecord record = new TransactionRecord();
        record.setTransactionId(event.transactionId());
        record.setSourceAccountId(event.sourceAccountId());
        record.setDestinationAccountId(event.destinationAccountId());
        record.setAmount(event.amount());
        record.setStatus("PENDING");
        
        transactionRepository.save(record);
        log.info("Transaction {} saved with status PENDING", event.transactionId());
    }

    /**
     * Listens for LedgerUpdatedEvent from ledger-service.
     * Updates the TransactionRecord status to COMPLETED or FAILED.
     */
    @KafkaListener(topics = "ledger.updated", groupId = "transaction-group",
            properties = {"spring.json.value.default.type=com.wkom.banking.events.LedgerUpdatedEvent"})
    @Transactional
    public void handleLedgerUpdated(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent: transactionId={}, status={}", event.transactionId(), event.status());
        
        transactionRepository.findByTransactionId(event.transactionId()).ifPresent(record -> {
            if ("SUCCESS".equals(event.status())) {
                record.setStatus("COMPLETED");
            } else {
                record.setStatus("FAILED");
            }
            transactionRepository.save(record);
            log.info("Transaction {} updated to status {}", event.transactionId(), record.getStatus());
        });
    }
}
