package org.obm.annotations.transactional;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import bitronix.tm.TransactionManagerServices;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class TransactionProvider implements Provider<TransactionManager> {
	
	private TransactionManager transactionManager;

	@Inject
	public TransactionProvider() throws SystemException {
		transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.setTransactionTimeout(3600);
	}
	
	@Override
	public TransactionManager get() {
		return transactionManager;
	}
}