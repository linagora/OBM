package org.obm.sync.server.transactional;

import java.lang.reflect.Method;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TransactionalInterceptor implements MethodInterceptor {

	private static final Log logger = LogFactory.getLog(TransactionalInterceptor.class);
	
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
				ut.begin();
			}
			
			Object obj = methodInvocation.proceed();
			
			if (!nestedTransaction) {
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
