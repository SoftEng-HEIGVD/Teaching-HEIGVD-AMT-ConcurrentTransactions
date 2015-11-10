package ch.heigvd.amt.demo.api;

import ch.heigvd.amt.demo.dto.TransactionDTO;
import ch.heigvd.amt.demo.services.business.TransactionProcessorLocal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * This RESTful endpoint is used to POST (create) new financial transactions. A
 * financial transaction 
 *
 * @author Olivier Liechti
 */
@Stateless
@Path("transactions")
public class TransactionResource {

  private static final Logger LOG = Logger.getLogger(TransactionResource.class.getName());

  @EJB
  TransactionProcessorLocal transactionProcessor;

  /**
   * Creates a new instance of TransactionResource
   */
  public TransactionResource() {
  }

  /**
   * POST method for creating an instance of TransactionResource
   *
   * @param transaction a DTO with the financial transaction details
   */
  @POST
  @Consumes("application/json")
  public void postTransaction(TransactionDTO transaction) {
    LOG.log(Level.INFO, "Received transaction for account: {0} {1}", new Object[]{transaction.getAccountId(), transaction.getAmount()});

    int numberOfAttempts = 0;
    int maxAttempts = 20;
    boolean success = false;
    while (!success && numberOfAttempts < maxAttempts) {
      try {
        numberOfAttempts++;
        transactionProcessor.processTransaction(transaction);
        success = true;
      } catch (Exception e) {
        LOG.log(Level.INFO, "Exception while processing tx : {0} attempt {1}/{2}", new Object[]{e.getMessage(), numberOfAttempts, maxAttempts});
        try {
          Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException ex) {
          Logger.getLogger(TransactionResource.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    if (!success) {
      throw new RuntimeException("Unable to process transaction after " + maxAttempts + " attempts.");
    }
  }
}
