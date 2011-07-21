package org.obm.annotations.transactional;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class TransactionProvider implements Provider<TransactionManager> {
	
	private static final String TRANSACTION_MANAGER = "java:comp/UserTransaction";
	private TransactionManager transactionManager;

	@Inject
	public TransactionProvider() throws NamingException {
		
		InitialContext context = new InitialContext();
		transactionManager = (TransactionManager) context.lookup(TRANSACTION_MANAGER);

	}
	
	@Override
	public TransactionManager get() {
		return transactionManager;
	}
}