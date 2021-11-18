package azure.javaee.liberty.samples.servlet;

import java.io.IOException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.springframework.transaction.jta.JtaTransactionManager;

@WebServlet(urlPatterns="/app")
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    	response.setContentType("text/plain");
		ServletOutputStream out = response.getOutputStream();
		
		try {
			// java:comp/TransactionManager         javax.naming.NameNotFoundException:
			// java:/TransactionManager             javax.naming.NameNotFoundException:
			// java:appserver/TransactionManager    javax.naming.NameNotFoundException:
			// java:pm/TransactionManager           javax.naming.NameNotFoundException:
			// java:comp/UserTransaction            java.lang.ClassCastException: class com.ibm.ws.transaction.services.UserTransactionService 
			//                                      cannot be cast to class javax.transaction.TransactionManager
			
			// ISSUE: https://github.com/OpenLiberty/open-liberty/issues/1487
			// OpenLiberty: java:comp/TransactionManager is not a spec-defined JNDI name			
			// TransactionManager transactionManager = (TransactionManager) new InitialContext().lookup("java:comp/TransactionManager");
			
			
			// Workable UserTransaction -> JtaTransactionManager -> TransactionManager
			UserTransaction userTransaction  = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			// User org.springframework.transaction.jta.JtaTransactionManager to get the javax.transaction.TransactionManager
			// https://docs.spring.io/spring-framework/docs/1.1.0/javadoc-api/org/springframework/transaction/jta/JtaTransactionManager.html
 			JtaTransactionManager jtaTransManager = new JtaTransactionManager(userTransaction);
 			TransactionManager transactionManager = jtaTransManager.getTransactionManager();
			out.println("Access transaction manager");
		} catch (NamingException e) {
			out.println("Can not access transaction manager");
			e.printStackTrace();
		}
    }
}
