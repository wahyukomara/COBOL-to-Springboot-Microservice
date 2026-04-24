package com.wkom.banking.accounts.repository;

import com.wkom.banking.accounts.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountId(String accountId);
}
