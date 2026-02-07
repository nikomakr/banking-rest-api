package com.bnpparibas.banking.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Account Entity
 * 
 * WHY TEST AN ENTITY CLASS?
 * - Business logic in deposit/withdraw methods MUST be correct
 * - Financial calculations cannot have bugs
 * - Validates our BigDecimal precision assumptions
 * 
 * BANKING PRINCIPLE: Testing
 * Every line of code that touches money must be tested.
 * 
 * TEST STRUCTURE:
 * We use @Nested classes to organize related tests.
 */
@DisplayName("Account Entity Tests")
class AccountTest {

    private Account account;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        account = new Account(
            "FR7630006000011234567890189",
            customerId,
            AccountType.CHECKING,
            "EUR"
        );
    }

    @Nested
    @DisplayName("Account Creation Tests")
    class AccountCreationTests {

        @Test
        @DisplayName("Should create account with valid data")
        void shouldCreateAccountWithValidData() {
            assertNotNull(account.getAccountNumber());
            assertEquals(customerId, account.getCustomerId());
            assertEquals(AccountType.CHECKING, account.getAccountType());
            assertEquals("EUR", account.getCurrency());
            assertEquals(BigDecimal.ZERO, account.getBalance());
            assertEquals(AccountStatus.ACTIVE, account.getStatus());
            assertNotNull(account.getCreatedAt());
            assertNotNull(account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should initialize balance to zero")
        void shouldInitializeBalanceToZero() {
            assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO),
                "New account balance must be exactly 0.00");
        }

        @Test
        @DisplayName("Should set status to ACTIVE by default")
        void shouldSetStatusToActiveByDefault() {
            assertEquals(AccountStatus.ACTIVE, account.getStatus());
        }
    }

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {

        @Test
        @DisplayName("Should deposit positive amount successfully")
        void shouldDepositPositiveAmount() {
            BigDecimal depositAmount = new BigDecimal("100.50");
            
            account.deposit(depositAmount);
            
            assertEquals(0, account.getBalance().compareTo(depositAmount));
        }

        @Test
        @DisplayName("Should handle multiple deposits correctly")
        void shouldHandleMultipleDeposits() {
            BigDecimal deposit1 = new BigDecimal("100.10");
            BigDecimal deposit2 = new BigDecimal("200.20");
            BigDecimal deposit3 = new BigDecimal("300.30");
            BigDecimal expected = new BigDecimal("600.60");

            account.deposit(deposit1);
            account.deposit(deposit2);
            account.deposit(deposit3);

            assertEquals(0, account.getBalance().compareTo(expected),
                "Multiple deposits should maintain precision");
        }

        @Test
        @DisplayName("Should reject null deposit amount")
        void shouldRejectNullDepositAmount() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(null)
            );

            assertTrue(exception.getMessage().contains("positive"));
        }

        @Test
        @DisplayName("Should reject zero deposit amount")
        void shouldRejectZeroDepositAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> account.deposit(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should reject negative deposit amount")
        void shouldRejectNegativeDepositAmount() {
            BigDecimal negativeAmount = new BigDecimal("-50.00");
            
            assertThrows(IllegalArgumentException.class,
                () -> account.deposit(negativeAmount));
        }

        @Test
        @DisplayName("Should reject deposit when account is frozen")
        void shouldRejectDepositWhenAccountFrozen() {
            account.setStatus(AccountStatus.FROZEN);
            BigDecimal depositAmount = new BigDecimal("100.00");

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> account.deposit(depositAmount)
            );

            assertTrue(exception.getMessage().contains("FROZEN"));
        }

        @Test
        @DisplayName("Should update timestamp on deposit")
        void shouldUpdateTimestampOnDeposit() throws InterruptedException {
            var originalTimestamp = account.getUpdatedAt();
            Thread.sleep(10);

            account.deposit(new BigDecimal("50.00"));

            assertTrue(account.getUpdatedAt().isAfter(originalTimestamp));
        }
    }

    @Nested
    @DisplayName("Withdrawal Tests")
    class WithdrawalTests {

        @BeforeEach
        void depositInitialBalance() {
            account.deposit(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should withdraw within available balance")
        void shouldWithdrawWithinAvailableBalance() {
            BigDecimal withdrawalAmount = new BigDecimal("250.50");
            BigDecimal expectedBalance = new BigDecimal("749.50");

            account.withdraw(withdrawalAmount);

            assertEquals(0, account.getBalance().compareTo(expectedBalance));
        }

        @Test
        @DisplayName("Should maintain precision in withdrawal calculations")
        void shouldMaintainPrecisionInWithdrawals() {
            BigDecimal withdrawal1 = new BigDecimal("333.33");
            BigDecimal withdrawal2 = new BigDecimal("333.33");
            BigDecimal withdrawal3 = new BigDecimal("333.34");
            BigDecimal expectedBalance = BigDecimal.ZERO;

            account.withdraw(withdrawal1);
            account.withdraw(withdrawal2);
            account.withdraw(withdrawal3);

            assertEquals(0, account.getBalance().compareTo(expectedBalance));
        }

        @Test
        @DisplayName("Should reject withdrawal exceeding balance")
        void shouldRejectWithdrawalExceedingBalance() {
            BigDecimal excessiveAmount = new BigDecimal("1500.00");

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> account.withdraw(excessiveAmount)
            );

            assertTrue(exception.getMessage().contains("Insufficient funds"));
        }

        @Test
        @DisplayName("Should reject withdrawal of exact zero balance")
        void shouldRejectWithdrawalOfExactZeroBalance() {
            BigDecimal tinyExcess = new BigDecimal("1000.01");

            assertThrows(IllegalStateException.class,
                () -> account.withdraw(tinyExcess));
        }

        @Test
        @DisplayName("Should reject negative withdrawal amount")
        void shouldRejectNegativeWithdrawalAmount() {
            BigDecimal negativeAmount = new BigDecimal("-50.00");
            
            assertThrows(IllegalArgumentException.class,
                () -> account.withdraw(negativeAmount));
        }

        @Test
        @DisplayName("Should reject withdrawal when account is closed")
        void shouldRejectWithdrawalWhenAccountClosed() {
            account.setStatus(AccountStatus.CLOSED);

            assertThrows(IllegalStateException.class,
                () -> account.withdraw(new BigDecimal("50.00")));
        }

        @Test
        @DisplayName("Should reject null withdrawal amount")
        void shouldRejectNullWithdrawalAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> account.withdraw(null));
        }
    }

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should change account status")
        void shouldChangeAccountStatus() {
            account.setStatus(AccountStatus.FROZEN);

            assertEquals(AccountStatus.FROZEN, account.getStatus());
        }

        @Test
        @DisplayName("Should update timestamp when changing status")
        void shouldUpdateTimestampWhenChangingStatus() throws InterruptedException {
            var originalTimestamp = account.getUpdatedAt();
            Thread.sleep(10);

            account.setStatus(AccountStatus.FROZEN);

            assertTrue(account.getUpdatedAt().isAfter(originalTimestamp));
        }

        @Test
        @DisplayName("Should block transactions on pending account")
        void shouldBlockTransactionsOnPendingAccount() {
            account.setStatus(AccountStatus.PENDING);

            assertThrows(IllegalStateException.class,
                () -> account.deposit(new BigDecimal("100.00")));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Accounts with same account number should be equal")
        void accountsWithSameNumberShouldBeEqual() {
            Account account1 = new Account(
                "FR7630006000011234567890189",
                UUID.randomUUID(),
                AccountType.CHECKING,
                "EUR"
            );

            Account account2 = new Account(
                "FR7630006000011234567890189",
                UUID.randomUUID(),
                AccountType.SAVINGS,
                "EUR"
            );

            assertEquals(account1, account2);
            assertEquals(account1.hashCode(), account2.hashCode());
        }

        @Test
        @DisplayName("Accounts with different numbers should not be equal")
        void accountsWithDifferentNumbersShouldNotBeEqual() {
            Account account1 = new Account(
                "FR7630006000011234567890189",
                customerId,
                AccountType.CHECKING,
                "EUR"
            );

            Account account2 = new Account(
                "FR7630006000019876543210123",
                customerId,
                AccountType.CHECKING,
                "EUR"
            );

            assertNotEquals(account1, account2);
        }

        @Test
        @DisplayName("Account should equal itself")
        void accountShouldEqualItself() {
            assertEquals(account, account);
        }

        @Test
        @DisplayName("Account should not equal null")
        void accountShouldNotEqualNull() {
            assertNotEquals(null, account);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large deposit amounts")
        void shouldHandleVeryLargeDeposits() {
            BigDecimal largeAmount = new BigDecimal("999999999999999.99");
            
            account.deposit(largeAmount);
            
            assertEquals(0, account.getBalance().compareTo(largeAmount));
        }

        @Test
        @DisplayName("Should handle very small deposit amounts")
        void shouldHandleVerySmallDeposits() {
            BigDecimal smallAmount = new BigDecimal("0.01");
            
            account.deposit(smallAmount);
            
            assertEquals(0, account.getBalance().compareTo(smallAmount));
        }

        @Test
        @DisplayName("Should handle exact balance withdrawal")
        void shouldHandleExactBalanceWithdrawal() {
            BigDecimal amount = new BigDecimal("500.00");
            account.deposit(amount);
            
            account.withdraw(amount);
            
            assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
        }
    }
}
