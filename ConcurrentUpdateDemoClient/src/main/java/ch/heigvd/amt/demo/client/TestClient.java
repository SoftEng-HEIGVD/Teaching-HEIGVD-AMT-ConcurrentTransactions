package ch.heigvd.amt.demo.client;

import ch.heigvd.amt.demo.client.dto.AccountDTO;
import ch.heigvd.amt.demo.client.dto.TransactionDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author Olivier Liechti
 */
public class TestClient {

  private final long numberOfAccounts = 20;
  private final long numberOfTransactionsPerAccount = 20;
  private final int numberOfConcurrentThreads = 1;

  private static final Logger LOG = Logger.getLogger(TestClient.class.getName());
  private int numberOfResponses = 0;

  Client client;
  final ExpectedState expectedState = new ExpectedState();

  private synchronized void incCounter() {
    numberOfResponses++;
  }

  public TestClient() {
    client = ClientBuilder.newClient().register(JacksonFeature.class);
  }

  private void sendResetCommandToServer() {
    final WebTarget target = client.target("http://localhost:8080/ConcurrentTransactionsServer/api").path("operations/resetOperation");
    Response response = target.request().post(null);
  }
  
  private List<AccountDTO> getAccountsListFromServer() {
    final WebTarget target = client.target("http://localhost:8080/ConcurrentTransactionsServer/api").path("accounts");
    GenericType<List<AccountDTO>> accountsListType = new GenericType<List<AccountDTO>>(){};
    return target.request().get(accountsListType);
  }

  private void test() {
    
    
    sendResetCommandToServer();
    
    ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentThreads);
    final WebTarget target = client.target("http://localhost:8080/ConcurrentTransactionsServer/api").path("transactions");

    for (int account = 1; account <= numberOfAccounts; account++) {
      for (int transaction = 0; transaction < numberOfTransactionsPerAccount; transaction++) {
      LOG.log(Level.INFO, "Generating {0} transactions for account {1}", new Object[]{numberOfTransactionsPerAccount, "account" + "/" + numberOfAccounts});
        final String counter = account + ", " + transaction;
        final int accountId = account;
        Runnable task = new Runnable() {
          @Override
          public void run() {
            TransactionDTO transaction = new TransactionDTO(accountId, 1);
            Response response = target.request().post(Entity.json(transaction));
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
              LOG.log(Level.INFO, "The server was not able to process the transaction: {0}", new Object[]{response.getStatus() + " " + response.getStatusInfo()});
            } else {
              expectedState.logTransactionIntoAccount(transaction);
            }
          }
        };
        executor.execute(task);
      }
    }

    LOG.log(Level.INFO, "All tasks have been submitted to the executor and will be processed by {0} concurrent threads.", numberOfConcurrentThreads);
    try {
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.HOURS);
      LOG.log(Level.INFO, "Number of responses received: {0}", numberOfResponses);
      LOG.log(Level.INFO, "Expected state: {0}", expectedState);
      List<String> errors = validateExpectedAgainstActualState();
      LOG.info("Errors: " + errors.toString());

    } catch (InterruptedException ex) {
      Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
    }
    LOG.info("Done.");

  }
  
  private List<String> validateExpectedAgainstActualState() {

    List<String> errors = new ArrayList<>();
    
    List<AccountDTO> actualState = getAccountsListFromServer();
    LOG.log(Level.INFO, "Expected number of accounts: {0}", expectedState.getAccounts().size());
    LOG.log(Level.INFO, "Actual number of accounts: {0}", actualState.size());
    if (expectedState.getAccounts().size() != actualState.size()) {
      errors.add("The number of accounts on the server is not the one expected: " + actualState.size() + " vs " + expectedState.getAccounts().size());
    }
    
    for (AccountDTO actualAccount : actualState) {
      AccountDTO expectedAccount = expectedState.getAccounts().get(actualAccount.getId());
      LOG.log(Level.INFO, "Expected vs actual number of transactions for account {0}: {1}/{2}", new Object[]{actualAccount.getId(), expectedAccount.getNumberOfTransactions(), actualAccount.getNumberOfTransactions()});
      LOG.log(Level.INFO, "Expected vs actual balance for account {0}: {1}/{2}", new Object[]{actualAccount.getId(), expectedAccount.getBalance(), actualAccount.getNumberOfTransactions()});
      if (expectedAccount.getNumberOfTransactions() != actualAccount.getNumberOfTransactions()) {
        errors.add("The number of transactions for account " + actualAccount.getId() + " is not the one expected: " + actualAccount.getNumberOfTransactions() + " vs " + expectedAccount.getNumberOfTransactions());
      }
      if (expectedAccount.getBalance()!= actualAccount.getBalance()) {
        errors.add("The balance for account " + actualAccount.getId() + " is not the one expected: " + actualAccount.getBalance()+ " vs " + expectedAccount.getBalance());
      }
    }
    
    return errors;

  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
    
    TestClient client = new TestClient();
    client.test();
  }

}
