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

import com.ibm.tx.jta.TransactionManagerFactory;

@WebServlet(urlPatterns="/app")
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    	response.setContentType("text/plain");
		ServletOutputStream out = response.getOutputStream();
		
		try {
			// Test #1: lookup java:comp/TransactionManager
			// java:comp/TransactionManager         javax.naming.NameNotFoundException:
			// java:/TransactionManager             javax.naming.NameNotFoundException:
			// java:appserver/TransactionManager    javax.naming.NameNotFoundException:
			// java:pm/TransactionManager           javax.naming.NameNotFoundException:
			// java:comp/UserTransaction            java.lang.ClassCastException: class com.ibm.ws.transaction.services.UserTransactionService 
			//                                      cannot be cast to class javax.transaction.TransactionManager
			
			// ISSUE: https://github.com/OpenLiberty/open-liberty/issues/1487
			// OpenLiberty: java:comp/TransactionManager is not a spec-defined JNDI name			
//			 TransactionManager transactionManager0 = (TransactionManager) new InitialContext().lookup("java:comp/TransactionManager");
			
			// Test #2: use org.springframework.transaction.jta.JtaTransactionManager 
			// Un-workable UserTransaction -> JtaTransactionManager -> TransactionManager
			// https://docs.spring.io/spring-framework/docs/1.1.0/javadoc-api/org/springframework/transaction/jta/JtaTransactionManager.html
			UserTransaction userTransaction  = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");			
 			JtaTransactionManager jtaTransManager = new JtaTransactionManager(userTransaction);
 			TransactionManager transactionManager1 = jtaTransManager.getTransactionManager();
 			out.println("Access transaction manager using JtaTransactionManager    " + transactionManager1);
 			
 			
 			// Test #3: use com.ibm.ws.Transaction.TransactionManagerFactory
			TransactionManager transactionManager2 = TransactionManagerFactory.getTransactionManager();
 			
			out.println("Access transaction manager using TransactionManagerFactory    " + transactionManager2);
		} catch (NamingException e) {
			out.println("Can not access transaction manager");
			e.printStackTrace();
		}
    }
}
