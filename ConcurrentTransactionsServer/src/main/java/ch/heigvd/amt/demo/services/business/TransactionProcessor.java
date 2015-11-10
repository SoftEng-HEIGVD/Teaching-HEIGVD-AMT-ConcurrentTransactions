package ch.heigvd.amt.demo.services.business;

import ch.heigvd.amt.demo.dto.TransactionDTO;
import ch.heigvd.amt.demo.model.Account;
import ch.heigvd.amt.demo.services.dao.AccountDAOLocal;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Olivier Liechti
 */
@Stateless
public class TransactionProcessor implements TransactionProcessorLocal {

  private static final Logger LOG = Logger.getLogger(TransactionProcessor.class.getName());

  @EJB
  AccountDAOLocal accountDAO;

  @EJB
  TransactionProcessorLocal selfViaContainer;

  @PersistenceContext
  EntityManager em;

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  public void processTransaction(TransactionDTO transaction) {

    /*
     * If this is the first financial transaction for this account, we need
     * to create it.
     */
    try {
      selfViaContainer.createAccountIfNotExists(transaction.getAccountId());
    } catch (Exception e) {
      LOG.info("*** An exception has occurred during account creation... maybe a DUPLICATE KEY that would not be a real problem..." + e.getMessage());
    }

    /*
     * Try the difference between findByIdForUpdate, which locks the account record
     * and findById, which does not.
     */
    Account account = accountDAO.findById(transaction.getAccountId());

    double bal = account.getBalance();
    bal = bal + transaction.getAmount();
    account.setBalance(bal);
    account.setNumberOfTransactions(account.getNumberOfTransactions() + 1);
    LOG.info("*** Updating account: " + account.getId() + " - " + account.getNumberOfTransactions());

  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  public void createAccountIfNotExists(long id) {
    Account account = accountDAO.findById(id);
    if (account == null) {
      account = new Account();
      account.setId(id);
      account.setBalance(0);
      account.setNumberOfTransactions(0);
      account.setHolderName(generateRandomHolderName());
      accountDAO.create(account);
    }
  }

  private String generateRandomHolderName() {
    String[] firstNames = {"John", "Carla", "Hans", "Yuki", "Sacha", "Tomoe", "Bernard", "Heinz", "Kurt", "Dani"};
    String[] lastNames = {"Smith", "Mueller", "Jones", "Dupond", "Martin", "Baecker", "Braig", "Ichikawa", "Nomura", "Simpson"};
    return pickRandomArrayItem(firstNames) + " " + pickRandomArrayItem(lastNames);
  }

  private Object pickRandomArrayItem(Object[] array) {
    int index = (int) (Math.random() * array.length);
    return array[index];
  }

}
