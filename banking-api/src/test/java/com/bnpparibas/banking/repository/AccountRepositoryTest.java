package com.bnpparibas.banking.repository;

import com.bnpparibas.banking.model.Account;
import com.bnpparibas.banking.model.AccountStatus;
import com.bnpparibas.banking.model.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for AccountRepository
 * 
 * WHY @DataJpaTest?
 * - Configures in-memory H2 database
 * - Scans for @Entity classes
 * - Configures Spring Data JPA
 * - Rolls back after each test (clean state)
 * 
 * BANKING TESTING PRINCIPLE:
 * Test against real database (even if in-memory) to catch:
 * - SQL syntax errors
 * - Constraint violations
 * - Transaction issues
 * - Data type mismatches
 */
@DataJpaTest
@DisplayName("AccountRepository Integration Tests")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        accountRepository.deleteAll();

        // Create test data
        customerId = UUID.randomUUID();
        testAccount = new Account(
            "FR7630006000011234567890189",
            customerId,
            AccountType.CHECKING,
            "EUR"
        );
    }

    @Test
    @DisplayName("Should save and retrieve account")
    void shouldSaveAndRetrieveAccount() {
        // Act
        Account saved = accountRepository.save(testAccount);

        // Assert
        assertNotNull(saved.getId());
        
        Optional<Account> retrieved = accountRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(testAccount.getAccountNumber(), retrieved.get().getAccountNumber());
    }

    @Test
    @DisplayName("Should find account by account number")
    void shouldFindByAccountNumber() {
        // Arrange
        accountRepository.save(testAccount);

        // Act
        Optional<Account> found = accountRepository.findByAccountNumber("FR7630006000011234567890189");

        // Assert
        assertTrue(found.isPresent());
        assertEquals(customerId, found.get().getCustomerId());
    }

    @Test
    @DisplayName("Should return empty when account number not found")
    void shouldReturnEmptyWhenAccountNumberNotFound() {
        // Act
        Optional<Account> found = accountRepository.findByAccountNumber("NONEXISTENT");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all accounts for customer")
    void shouldFindAllAccountsForCustomer() {
        // Arrange - Create 3 accounts for same customer
        accountRepository.save(testAccount);
        
        Account savingsAccount = new Account(
            "FR7630006000019876543210123",
            customerId,
            AccountType.SAVINGS,
            "EUR"
        );
        accountRepository.save(savingsAccount);

        Account businessAccount = new Account(
            "FR7630006000015555555555555",
            customerId,
            AccountType.BUSINESS,
            "EUR"
        );
        accountRepository.save(businessAccount);

        // Act
        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        // Assert
        assertEquals(3, accounts.size());
    }

    @Test
    @DisplayName("Should find accounts by status")
    void shouldFindAccountsByStatus() {
        // Arrange
        accountRepository.save(testAccount);

        Account frozenAccount = new Account(
            "FR7630006000019876543210123",
            UUID.randomUUID(),
            AccountType.SAVINGS,
            "EUR"
        );
        frozenAccount.setStatus(AccountStatus.FROZEN);
        accountRepository.save(frozenAccount);

        // Act
        List<Account> activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE);
        List<Account> frozenAccounts = accountRepository.findByStatus(AccountStatus.FROZEN);

        // Assert
        assertEquals(1, activeAccounts.size());
        assertEquals(1, frozenAccounts.size());
        assertEquals(AccountStatus.ACTIVE, activeAccounts.get(0).getStatus());
        assertEquals(AccountStatus.FROZEN, frozenAccounts.get(0).getStatus());
    }

    @Test
    @DisplayName("Should check if account number exists")
    void shouldCheckIfAccountNumberExists() {
        // Arrange
        accountRepository.save(testAccount);

        // Act & Assert
        assertTrue(accountRepository.existsByAccountNumber("FR7630006000011234567890189"));
        assertFalse(accountRepository.existsByAccountNumber("NONEXISTENT"));
    }

    @Test
    @DisplayName("Should count accounts by status")
    void shouldCountAccountsByStatus() {
        // Arrange
        accountRepository.save(testAccount);

        Account frozenAccount = new Account(
            "FR7630006000019876543210123",
            UUID.randomUUID(),
            AccountType.SAVINGS,
            "EUR"
        );
        frozenAccount.setStatus(AccountStatus.FROZEN);
        accountRepository.save(frozenAccount);

        Account anotherFrozenAccount = new Account(
            "FR7630006000015555555555555",
            UUID.randomUUID(),
            AccountType.BUSINESS,
            "EUR"
        );
        anotherFrozenAccount.setStatus(AccountStatus.FROZEN);
        accountRepository.save(anotherFrozenAccount);

        // Act
        long activeCount = accountRepository.countByStatus(AccountStatus.ACTIVE);
        long frozenCount = accountRepository.countByStatus(AccountStatus.FROZEN);

        // Assert
        assertEquals(1, activeCount);
        assertEquals(2, frozenCount);
    }

    @Test
    @DisplayName("Should find accounts with balance above threshold")
    void shouldFindAccountsWithBalanceAbove() {
        // Arrange
        testAccount.deposit(new BigDecimal("1000.00"));
        accountRepository.save(testAccount);

        Account richAccount = new Account(
            "FR7630006000019876543210123",
            UUID.randomUUID(),
            AccountType.SAVINGS,
            "EUR"
        );
        richAccount.deposit(new BigDecimal("50000.00"));
        accountRepository.save(richAccount);

        Account poorAccount = new Account(
            "FR7630006000015555555555555",
            UUID.randomUUID(),
            AccountType.CHECKING,
            "EUR"
        );
        poorAccount.deposit(new BigDecimal("50.00"));
        accountRepository.save(poorAccount);

        // Act
        List<Account> highBalanceAccounts = accountRepository.findAccountsWithBalanceAbove(
            new BigDecimal("10000.00")
        );

        // Assert
        assertEquals(1, highBalanceAccounts.size());
        assertEquals(0, richAccount.getBalance().compareTo(highBalanceAccounts.get(0).getBalance()));
    }

    @Test
    @DisplayName("Should find customer accounts by currency")
    void shouldFindCustomerAccountsByCurrency() {
        // Arrange
        accountRepository.save(testAccount); // EUR

        Account usdAccount = new Account(
            "US1234567890123456789012345",
            customerId,
            AccountType.SAVINGS,
            "USD"
        );
        accountRepository.save(usdAccount);

        // Act
        List<Account> eurAccounts = accountRepository.findByCustomerIdAndCurrency(customerId, "EUR");
        List<Account> usdAccounts = accountRepository.findByCustomerIdAndCurrency(customerId, "USD");

        // Assert
        assertEquals(1, eurAccounts.size());
        assertEquals(1, usdAccounts.size());
        assertEquals("EUR", eurAccounts.get(0).getCurrency());
        assertEquals("USD", usdAccounts.get(0).getCurrency());
    }

    @Test
    @DisplayName("Should find customer accounts by type")
    void shouldFindCustomerAccountsByType() {
        // Arrange
        accountRepository.save(testAccount); // CHECKING

        Account savingsAccount = new Account(
            "FR7630006000019876543210123",
            customerId,
            AccountType.SAVINGS,
            "EUR"
        );
        accountRepository.save(savingsAccount);

        // Act
        List<Account> checkingAccounts = accountRepository.findByCustomerIdAndAccountType(
            customerId, 
            AccountType.CHECKING
        );
        List<Account> savingsAccounts = accountRepository.findByCustomerIdAndAccountType(
            customerId,
            AccountType.SAVINGS
        );

        // Assert
        assertEquals(1, checkingAccounts.size());
        assertEquals(1, savingsAccounts.size());
        assertEquals(AccountType.CHECKING, checkingAccounts.get(0).getAccountType());
        assertEquals(AccountType.SAVINGS, savingsAccounts.get(0).getAccountType());
    }

    @Test
    @DisplayName("Should persist balance changes")
    void shouldPersistBalanceChanges() {
        // Arrange
        testAccount.deposit(new BigDecimal("500.00"));
        Account saved = accountRepository.save(testAccount);
        UUID accountId = saved.getId();

        // Act - Clear persistence context to force reload from DB
        accountRepository.flush();
        Optional<Account> reloaded = accountRepository.findById(accountId);

        // Assert
        assertTrue(reloaded.isPresent());
        assertEquals(0, new BigDecimal("500.00").compareTo(reloaded.get().getBalance()));
    }
}
