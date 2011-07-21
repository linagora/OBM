package org.obm.annotations.transactional;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class TransactionProvider implements Provider<UserTransaction> {
	
	private static final String USER_TRANSACTION = "java:comp/UserTransaction";
	private UserTransaction userTransaction;

	@Inject
	public TransactionProvider() throws NamingException {
		
		InitialContext context = new InitialContext();
		userTransaction = (UserTransaction) context.lookup(USER_TRANSACTION);

	}
	
	@Override
	public UserTransaction get() {
		return userTransaction;
	}
}