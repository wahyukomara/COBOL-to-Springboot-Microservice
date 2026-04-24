package com.wkom.banking.payment.controller;

import com.wkom.banking.events.PaymentInitiatedEvent;
import com.wkom.banking.payment.dto.PaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public ResponseEntity<String> initiatePayment(@RequestBody PaymentRequest request) {
        String transactionId = "TXN-" + UUID.randomUUID().toString().toUpperCase();
        
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                transactionId,
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                "PENDING"
        );
        
        kafkaTemplate.send("payments.initiated", transactionId, event);
        
        return ResponseEntity.accepted().body("Payment accepted. Transaction ID: " + transactionId);
    }
    
    public ResponseEntity<String> paymentFallback(PaymentRequest request, Throwable t) {
        return ResponseEntity.status(503).body("Payment service is currently unavailable. Please try again later.");
    }
}
