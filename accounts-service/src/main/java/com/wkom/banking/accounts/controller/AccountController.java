package com.wkom.banking.accounts.controller;

import com.wkom.banking.accounts.entity.Account;
import com.wkom.banking.accounts.repository.AccountRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final StringRedisTemplate redisTemplate;

    public AccountController(AccountRepository accountRepository, StringRedisTemplate redisTemplate) {
        this.accountRepository = accountRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account request) {
        request.setAccountId("ACC-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        request.setBalance(BigDecimal.ZERO);
        if (request.getStatus() == null) request.setStatus("ACTIVE");
        
        Account saved = accountRepository.save(request);
        redisTemplate.opsForValue().set("balance:" + saved.getAccountId(), saved.getBalance().toString());
        
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountId) {
        String cachedBalance = redisTemplate.opsForValue().get("balance:" + accountId);
        if (cachedBalance != null) {
            return ResponseEntity.ok(new BigDecimal(cachedBalance));
        }

        return accountRepository.findByAccountId(accountId)
                .map(account -> {
                    redisTemplate.opsForValue().set("balance:" + accountId, account.getBalance().toString());
                    return ResponseEntity.ok(account.getBalance());
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
