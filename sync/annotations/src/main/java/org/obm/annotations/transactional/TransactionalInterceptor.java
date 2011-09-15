package org.obm.annotations.transactional;

import java.lang.reflect.Method;

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TransactionalInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(TransactionalInterceptor.class);
	
	@Inject
	private Provider<TransactionManager> transactionManagerProvider;

	public TransactionalInterceptor() {
		super();
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Transactional transactional = readTransactionMetadata(methodInvocation);
		if (transactional == null) {
			return methodInvocation.proceed();
		}
		try {
			return managerTransaction(methodInvocation, transactional);
		} finally {
			logger.debug("exiting interceptor, thread {}", Thread.currentThread().getId());
		}
	}

	private Object managerTransaction(MethodInvocation methodInvocation,
			Transactional transactional) throws Throwable {
		
		TransactionManager ut = transactionManagerProvider.get();
		boolean haveCreatedNewTransaction = false;
		
		try {
			haveCreatedNewTransaction = attachTransactionIfNeeded(ut, transactional.propagation());
			Object obj = methodInvocation.proceed();
			detachTransactionIfNeeded(ut, haveCreatedNewTransaction);
			return obj;
		} catch (RollbackException e) {
			throw e;
		} catch (Throwable t) {
			rollbackTransaction(transactional, ut, haveCreatedNewTransaction, t);
			throw t;
		}
	}

	private boolean attachTransactionIfNeeded(	TransactionManager ut, 
			Propagation propagation) throws NotSupportedException,
			SystemException {
		
		boolean transactionAlreadyAttached = isTransactionAssociated(ut);
		
		if (transactionAlreadyAttached && propagation == Propagation.REQUIRED) {
			logger.debug("reuse current transaction, thread {}", Thread.currentThread().getId());
			return false;
		} else {
			ut.begin();
			logger.debug("transaction was started, thread {}", Thread.currentThread().getId());
			return true;
		}
	}
	
	private void detachTransactionIfNeeded(TransactionManager ut,
			boolean haveCreatedNewTransaction) throws Throwable {
		
		if (isTransactionAssociated(ut) && haveCreatedNewTransaction) {
			logger.debug("transaction status {}, thread {}", ut.getStatus(), Thread.currentThread().getId());
			if (canCommitTransaction(ut)) {
				ut.commit();
				logger.debug("transaction was commited, thread {}", Thread.currentThread().getId());
			} else {
				ut.rollback();
				logger.debug("transaction was rollback, thread {}", Thread.currentThread().getId());
				throw new RollbackException();
			}
		} else {
			logger.info("no transaction associated, thread {}", Thread.currentThread().getId());
		}
	}

	private void rollbackTransaction(Transactional transactional, TransactionManager ut,
			boolean haveCreatedNewTransaction, Throwable e) {
		logger.error(e.getMessage(), e);		
		try {
			if (isTransactionAssociated(ut) && haveCreatedNewTransaction) {
				if (needRollback(transactional, e)) {
					logger.error("transaction was rollback", e);
					ut.rollback();
					return;
				} else {
					if (canCommitTransaction(ut)) {
						logger.info("transaction was commited, thread {}", Thread.currentThread().getId());
						ut.commit();
						return;
					}
				}
			}
			logger.warn("transaction may be leaked", e);
		} catch (Throwable t) {
			logger.error("transaction rollback failed", t);
		}
	}

	private boolean canCommitTransaction(TransactionManager ut) throws SystemException {
		return isTransactionActive(ut);
	}

	private boolean isTransactionAssociated(TransactionManager tm) throws SystemException {
		int status = tm.getStatus();
		return status != Status.STATUS_NO_TRANSACTION;
	}
	
	private boolean isTransactionActive(TransactionManager tm)	throws SystemException {
		int status = tm.getStatus();
		return Status.STATUS_ACTIVE == status
				|| Status.STATUS_PREPARING == status
				|| Status.STATUS_PREPARED == status;
	}

	private Transactional readTransactionMetadata(MethodInvocation methodInvocation) {
		
		Method method = methodInvocation.getMethod();

		Class<?> targetClass = methodInvocation.getThis().getClass().getSuperclass();

		if (method.isAnnotationPresent(Transactional.class)) {
			return method.getAnnotation(Transactional.class);
		} else if (targetClass.isAnnotationPresent(Transactional.class)) {
			return targetClass.getAnnotation(Transactional.class);
		}
		return null;
	}

	private boolean needRollback(Transactional transactional, Throwable e) {

		for (Class<? extends Exception> rollBackOn : transactional.noRollbackOn()) {
			if (rollBackOn.isInstance(e)) {
				return false;
			}
		}
		return true;
	}
}
