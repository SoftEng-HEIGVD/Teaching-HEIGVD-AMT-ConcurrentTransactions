package ch.heigvd.amt.demo.services.dao;

import ch.heigvd.amt.demo.model.Account;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Olivier Liechti
 */
@Stateless
public class AccountDAO implements AccountDAOLocal {

  private static final Logger LOG = Logger.getLogger(AccountDAO.class.getName());

  @PersistenceContext
  EntityManager em;
   
  @Override
  public void create(Account account) {
      em.persist(account);
      em.flush();
  }
       
  @Override
  public void update(Account account) {
    em.merge(account);
  }

  @Override
  public Account findById(long id) {
    return em.find(Account.class, id);
  }

  @Override
  public Account findByIdForUpdate(long id) {
    return em.find(Account.class, id, LockModeType.PESSIMISTIC_WRITE);
  }

  @Override
  public List<Account> findAll() {
    Query query = em.createNamedQuery("Account.findAll");
    return query.getResultList();
  }

  @Override
  public void deleteAll() {
    Query query = em.createNamedQuery("Account.deleteAll");
    query.executeUpdate();
  }

}
