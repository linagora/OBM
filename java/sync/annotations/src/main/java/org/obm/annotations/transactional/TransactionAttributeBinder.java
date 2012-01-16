package org.obm.annotations.transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TransactionAttributeBinder implements ITransactionAttributeBinder {
	private Cache<Transaction, Transactional> transactionAttributeCache;
	private final TransactionManager transactionManager;

	@Inject
	public TransactionAttributeBinder(TransactionManager transactionManager,
			TransactionConfiguration transactionConfiguration) {
		this.transactionManager = transactionManager;
		transactionAttributeCache = CacheBuilder
				.newBuilder()
				.expireAfterWrite(
						transactionConfiguration.getTimeOutInSecond(),
						TimeUnit.SECONDS)
				.build(new CacheLoader<Transaction, Transactional>() {
					@Override
					public Transactional load(Transaction key) throws Exception {
						return null;
					}
				});
	}

	@Override
	public Transactional getCurrentTransactional() throws TransactionException {
		try {
			Transaction transaction = getCurrentTransaction();
			return transactionAttributeCache.get(transaction);
		} catch (NullPointerException e) {
			throw new TransactionException(e);
		} catch (ExecutionException e) {
			throw new TransactionException(e);
		}
	}

	@Override
	public void bindTransactional(Transactional transactional)
			throws TransactionException {
		Transaction transaction = getCurrentTransaction();
		transactionAttributeCache.asMap().put(transaction, transactional);
	}

	@Override
	public void invalidateTransactional() throws TransactionException {
		Transaction transaction = getCurrentTransaction();
		transactionAttributeCache.invalidate(transaction);
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
