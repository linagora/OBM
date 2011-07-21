package org.obm.annotations.transactional;

import java.lang.reflect.Method;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

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
	private Provider<UserTransaction> userTransactionProvider;

	public TransactionalInterceptor() {
		super();
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Transactional transactional = readTransactionMetadata(methodInvocation);
		UserTransaction ut = userTransactionProvider.get();

		if (transactional == null) {
			return methodInvocation.proceed();
		}

		boolean nestedTransaction = isTransactionActive(ut);
		
		try {
			if (!nestedTransaction) {
				logger.info("transaction was started");
				ut.begin();
			}
			
			Object obj = methodInvocation.proceed();
			
			if (!nestedTransaction) {
				logger.info("transaction was commited");
				ut.commit();
			}
			return obj;
		} catch (Exception e) {
			if (isTransactionActive(ut)) {
				if (needRollback(transactional, e)) {
					logger.error("transaction was rollback", e);
					ut.rollback();
				} else {
					if (!nestedTransaction) {
						logger.info("transaction was commited");
						ut.commit();
					}
				}
			}
			throw e;
		}
	}

	private boolean isTransactionActive(UserTransaction ut)	throws SystemException {
		int status = ut.getStatus();
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
