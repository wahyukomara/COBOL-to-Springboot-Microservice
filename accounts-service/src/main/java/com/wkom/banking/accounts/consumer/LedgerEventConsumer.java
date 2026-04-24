package com.wkom.banking.accounts.consumer;

import com.wkom.banking.events.LedgerUpdatedEvent;
import com.wkom.banking.accounts.repository.AccountRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerEventConsumer {

    private final AccountRepository accountRepository;
    private final StringRedisTemplate redisTemplate;

    public LedgerEventConsumer(AccountRepository accountRepository, StringRedisTemplate redisTemplate) {
        this.accountRepository = accountRepository;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "ledger.updated", groupId = "accounts-group")
    @Transactional
    public void handleLedgerUpdated(LedgerUpdatedEvent event) {
        if ("SUCCESS".equals(event.status())) {
            accountRepository.findByAccountId(event.accountId()).ifPresent(account -> {
                if ("CREDIT".equals(event.entryType())) {
                    account.setBalance(account.getBalance().add(event.amount()));
                } else if ("DEBIT".equals(event.entryType())) {
                    account.setBalance(account.getBalance().subtract(event.amount()));
                }
                accountRepository.save(account);
                redisTemplate.opsForValue().set("balance:" + account.getAccountId(), account.getBalance().toString());
            });
        }
    }
}
