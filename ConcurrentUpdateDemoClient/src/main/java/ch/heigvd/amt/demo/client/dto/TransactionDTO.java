package ch.heigvd.amt.demo.client.dto;

/**
 *
 * @author Olivier Liechti
 */
public class TransactionDTO {
  private final long accountId;
  private final double amount;

  public TransactionDTO(long accountId, double amount) {
    this.accountId = accountId;
    this.amount = amount;
  }

  public long getAccountId() {
    return accountId;
  }

  public double getAmount() {
    return amount;
  }

}
