package org.obm.annotations.transactional;

import java.lang.reflect.Method;

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

	private static final Logger logger = LoggerFactory
			.getLogger(TransactionalInterceptor.class);
	
	@Inject
	private Provider<TransactionManager> transactionManagerProvider;

	public TransactionalInterceptor() {
		super();
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Transactional transactional = readTransactionMetadata(methodInvocation);
		TransactionManager ut = transactionManagerProvider.get();
		if (transactional == null) {
			return methodInvocation.proceed();
		}

		boolean isInSubTransaction = isTransactionActive(ut);
		boolean haveCreatedNewTransaction = false;
		
		try {
			if (canBeginNewTransaction(isInSubTransaction, transactional)) {
				logger.info("transaction was started");
				ut.begin();
				haveCreatedNewTransaction = true;
			}
			
			Object obj = methodInvocation.proceed();
			
			if (canCommitTransaction(ut, haveCreatedNewTransaction)) {
				logger.info("transaction was commited");
				ut.commit();
			}
			return obj;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (isTransactionActive(ut)) {
				if (needRollback(transactional, e)) {
					logger.error("transaction was rollback", e);
					ut.rollback();
				} else {
					if (canCommitTransaction(ut, haveCreatedNewTransaction)) {
						logger.info("transaction was commited");
						ut.commit();
					}
				}
			}
			throw e;
		}
	}

	private boolean canCommitTransaction(TransactionManager ut, boolean haveCreatedNewTransaction) throws SystemException {
		return isTransactionActive(ut) && haveCreatedNewTransaction;
	}

	private boolean canBeginNewTransaction(boolean isInSubTransaction,
			Transactional transactional) {
		return !isInSubTransaction || (isInSubTransaction && Propagation.NESTED.equals(transactional.propagation()));
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

	private boolean needRollback(Transactional transactional, Exception e) {

		for (Class<? extends Exception> rollBackOn : transactional.noRollbackOn()) {
			if (rollBackOn.isInstance(e)) {
				return false;
			}
		}
		return true;
	}
}
