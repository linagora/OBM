package org.obm.annotations.transactional;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TransactionalBinder implements ITransactionAttributeBinder {
	private final TransactionManager transactionManager;
	private Map<Transaction, Transactional> transactionAttributeCache;

	@Inject
	public TransactionalBinder(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		WeakHashMap<Transaction, Transactional> weakHashMap = new WeakHashMap<Transaction, Transactional>();
		this.transactionAttributeCache = Collections.synchronizedMap(weakHashMap);
	}

	@Override
	public Transactional getTransactionalInCurrentTransaction() throws TransactionException {
		Transaction transaction = getCurrentTransaction();
		Transactional transactional = transactionAttributeCache.get(transaction);
		if(transactional == null){
			throw new TransactionException(
					"Nothing is linked to the current transaction");
		}
		return transactional;
	}

	@Override
	public void bindTransactionalToCurrentTransaction(Transactional transactional)
			throws TransactionException {
		Transaction transaction = getCurrentTransaction();
		transactionAttributeCache.put(transaction, transactional);
	}

	@Override
	public void invalidateTransactionalInCurrentTransaction() throws TransactionException {
		Transaction transaction = getCurrentTransaction();
		transactionAttributeCache.remove(transaction);
	}

	private Transaction getCurrentTransaction() throws TransactionException {
		try {
			Transaction transaction = transactionManager.getTransaction();
			if (transaction == null) {
				throw new TransactionException(
						"No active transaction have been found");
			}
			return transaction;
		} catch (SystemException e) {
			throw new TransactionException(e);
		}

	}
}
