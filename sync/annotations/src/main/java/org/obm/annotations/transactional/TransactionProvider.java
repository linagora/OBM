package org.obm.annotations.transactional;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class TransactionProvider implements Provider<TransactionManager> {
	
	private TransactionManager transactionManager;

	@Inject
	public TransactionProvider() throws SystemException {
		transactionManager = new UserTransactionManager();
		transactionManager.setTransactionTimeout(3600);
	}
	
	@Override
	public TransactionManager get() {
		return transactionManager;
	}
}