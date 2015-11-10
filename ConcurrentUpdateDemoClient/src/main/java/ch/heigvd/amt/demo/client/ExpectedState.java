package ch.heigvd.amt.demo.client;

import ch.heigvd.amt.demo.client.dto.TransactionDTO;
import ch.heigvd.amt.demo.client.dto.AccountDTO;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Olivier Liechti
 */
public class ExpectedState {
  
  
  private final Map<Long, AccountDTO> accounts = new HashMap<>();
  
  public synchronized void logTransactionIntoAccount(TransactionDTO transaction) {
    AccountDTO account = accounts.get(transaction.getAccountId());
    if (account == null) {
      account = new AccountDTO();
      account.setId(transaction.getAccountId());
      account.setNumberOfTransactions(0);
      account.setBalance(0);
      accounts.put(account.getId(), account);
    }
    account.setBalance(account.getBalance() + transaction.getAmount());
    account.setNumberOfTransactions(account.getNumberOfTransactions() + 1);
  }

  public Map<Long, AccountDTO> getAccounts() {
    return accounts;
  }


  @Override
  public String toString() {
    return "ExpectedState{" + "accounts=" + accounts + '}';
  }


}
