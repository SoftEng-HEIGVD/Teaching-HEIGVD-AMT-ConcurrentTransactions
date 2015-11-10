package ch.heigvd.amt.demo.api;

import ch.heigvd.amt.demo.services.dao.AccountDAOLocal;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * This RESTful endpoint is used to expose an administration interface. At
 * the moment, there is only one command: reset. It is used to delete all
 * accounts. It would not make any sense in a real application (do you really
 * want to delete all accounts?), but it is useful during development as it
 * facilitates testing and validation.
 *
 * @author Olivier Liechti
 */
@Stateless
@Path("operations")
public class AdminOperationResource {

  @EJB
  AccountDAOLocal accountDAO;

  public AdminOperationResource() {
  }
  
  /**
   * This endpoint processes a Reset Operation. Note that in this implementation,
   * we don't even look at the payload (this is not very good).
   * @param operation 
   */
  @POST
  @Path("resetOperation")
  @Consumes("application/json")
  public void postResetOperation(Object operation) {
    accountDAO.deleteAll();
  }
  

}
