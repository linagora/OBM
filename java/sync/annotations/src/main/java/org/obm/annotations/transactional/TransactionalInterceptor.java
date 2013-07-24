/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.annotations.transactional;

import java.lang.reflect.Method;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
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

public class TransactionalInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(TransactionalInterceptor.class);
	
	@Inject
	private TransactionProvider transactionManagerProvider;
	
	@Inject
	private ITransactionAttributeBinder transactionAttributeBinder;
	
	
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
			haveCreatedNewTransaction = attachTransactionIfNeeded(ut, transactional);
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
			Transactional transactional) throws NotSupportedException,
			SystemException, TransactionException {
		
		boolean transactionAlreadyAttached = isTransactionAssociated(ut);
		
		if (transactionAlreadyAttached && transactional.propagation() == Propagation.REQUIRED) {
			logger.debug("reuse current transaction, thread {}", Thread.currentThread().getId());
			return false;
		} else {
			doBegin(ut, transactional);
			logger.debug("transaction was started, thread {}", Thread.currentThread().getId());
			return true;
		}
	}
	
	private void detachTransactionIfNeeded(TransactionManager ut,
			boolean haveCreatedNewTransaction) throws Throwable {
		
		if (isTransactionAssociated(ut) && haveCreatedNewTransaction) {
			logger.debug("transaction status {}, thread {}", ut.getStatus(), Thread.currentThread().getId());
			if (canCommitTransaction(ut)) {
				doCommit(ut);
				logger.debug("transaction was commited, thread {}", Thread.currentThread().getId());
			} else {
				doRollBack(ut);
				logger.debug("transaction was rollback, thread {}", Thread.currentThread().getId());
				throw new RollbackException();
			}
		} else {
			logger.info("no transaction associated, thread {}", Thread.currentThread().getId());
		}
	}

	private void doBegin(TransactionManager ut, Transactional transactional) throws TransactionException, NotSupportedException, SystemException{
		ut.begin();
		transactionAttributeBinder.bindTransactionalToCurrentTransaction(transactional);
	}
	
	private void doCommit(TransactionManager ut) throws SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException, TransactionException{
		transactionAttributeBinder.invalidateTransactionalInCurrentTransaction();
		ut.commit();
	}
	
	private void doRollBack(TransactionManager ut) throws IllegalStateException, SecurityException, SystemException, TransactionException{
		transactionAttributeBinder.invalidateTransactionalInCurrentTransaction();
		ut.rollback();
	}
	
	private void rollbackTransaction(Transactional transactional, TransactionManager ut,
			boolean haveCreatedNewTransaction, Throwable e) {
		try {
			if (isTransactionAssociated(ut) && haveCreatedNewTransaction) {
				if (needRollback(transactional, e)) {
					logger.error("transaction was rollback", e);
					doRollBack(ut);
					return;
				} else {
					if (canCommitTransaction(ut)) {
						logger.info("transaction was commited, thread {}", Thread.currentThread().getId());
						doCommit(ut);
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
