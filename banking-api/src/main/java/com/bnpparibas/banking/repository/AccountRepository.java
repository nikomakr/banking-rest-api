package com.bnpparibas.banking.repository;

import com.bnpparibas.banking.model.Account;
import com.bnpparibas.banking.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Account Repository - Data Access Layer
 * 
 * WHY INTERFACE, NOT CLASS?
 * Spring Data JPA automatically implements this interface at runtime.
 * You define method signatures, Spring generates the SQL!
 * 
 * BANKING BENEFIT:
 * - No boilerplate JDBC code
 * - Type-safe queries
 * - Automatic transaction management
 * - Built-in CRUD operations
 * 
 * EXTENDS JpaRepository:
 * Gives us for free:
 * - save(account)
 * - findById(id)
 * - findAll()
 * - delete(account)
 * - count()
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Find account by account number (IBAN)
     * 
     * WHY NEEDED?
     * Customers reference accounts by account number, not UUID.
     * Example: "Transfer from FR76... to GB29..."
     * 
     * METHOD NAMING CONVENTION:
     * Spring parses "findByAccountNumber" and generates SQL:
     * SELECT * FROM accounts WHERE account_number = ?
     * 
     * @param accountNumber IBAN format account number
     * @return Optional<Account> - empty if not found
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts for a specific customer
     * 
     * BANKING USE CASE:
     * Customer logs in → Show all their accounts
     * - Checking: €1,234.56
     * - Savings: €10,000.00
     * - Business: €5,678.90
     * 
     * @param customerId UUID of the customer
     * @return List of accounts (empty list if none found)
     */
    List<Account> findByCustomerId(UUID customerId);

    /**
     * Find accounts by status
     * 
     * BANKING USE CASES:
     * - Compliance: "Show all FROZEN accounts for review"
     * - Operations: "List all CLOSED accounts for archival"
     * - Fraud: "Find all PENDING accounts older than 30 days"
     * 
     * @param status Account status to filter by
     * @return List of matching accounts
     */
    List<Account> findByStatus(AccountStatus status);

    /**
     * Find customer's accounts by status
     * 
     * USE CASE:
     * "Show me all my ACTIVE accounts"
     * 
     * QUERY GENERATION:
     * Spring generates:
     * SELECT * FROM accounts 
     * WHERE customer_id = ? AND status = ?
     * 
     * @param customerId Customer UUID
     * @param status Account status
     * @return List of matching accounts
     */
    List<Account> findByCustomerIdAndStatus(UUID customerId, AccountStatus status);

    /**
     * Check if account number already exists
     * 
     * WHY CRITICAL?
     * Account numbers must be unique (database constraint).
     * Check before creating to give friendly error message.
     * 
     * BETTER THAN:
     * - Try to save → Database throws constraint violation
     * - User sees: "ERROR: duplicate key value violates unique constraint"
     * 
     * WITH THIS:
     * - Check first → User sees: "Account number already exists"
     * 
     * @param accountNumber IBAN to check
     * @return true if exists, false otherwise
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Count accounts by status
     * 
     * BANKING METRICS:
     * - Dashboard: "1,234 ACTIVE accounts"
     * - Compliance: "5 FROZEN accounts pending review"
     * - Operations: "10 PENDING accounts awaiting KYC"
     * 
     * @param status Status to count
     * @return Number of accounts with that status
     */
    long countByStatus(AccountStatus status);

    /**
     * Find accounts by customer and currency
     * 
     * USE CASE:
     * Multi-currency banking:
     * - "Show my EUR accounts"
     * - "Show my USD accounts"
     * - "Show my GBP accounts"
     * 
     * @param customerId Customer UUID
     * @param currency ISO 4217 currency code
     * @return List of accounts in that currency
     */
    List<Account> findByCustomerIdAndCurrency(UUID customerId, String currency);

    /**
     * Custom JPQL query - Find accounts with balance above threshold
     * 
     * WHY CUSTOM QUERY?
     * Can't express this with method naming convention.
     * 
     * BANKING USE CASES:
     * - Premium services: "Customers with €10,000+"
     * - Marketing: "High-value account holders"
     * - Risk: "Large balance accounts needing extra security"
     * 
     * JPQL vs SQL:
     * JPQL uses Java class/field names, not table/column names
     * More portable across databases
     * 
     * @param minBalance Minimum balance threshold
     * @return List of accounts with balance >= minBalance
     */
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance")
    List<Account> findAccountsWithBalanceAbove(@Param("minBalance") java.math.BigDecimal minBalance);

    /**
     * Find accounts by customer ID and account type
     * 
     * USE CASE:
     * "Show me all my SAVINGS accounts"
     * "Show me all my CHECKING accounts"
     * 
     * @param customerId Customer UUID
     * @param accountType Type of account
     * @return List of matching accounts
     */
    List<Account> findByCustomerIdAndAccountType(UUID customerId, com.bnpparibas.banking.model.AccountType accountType);
}
