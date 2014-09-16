/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014  Linagora
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
package org.obm.sync.transactional;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.annotations.transactional.Transactional;
import org.obm.servlet.filter.resource.ResourcesHolder;
import org.obm.sync.ConnectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class JdbcTransactionalInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(JdbcTransactionalInterceptor.class);

	@Inject
	private Provider<ResourcesHolder> resourcesHolderProvider;
	@Inject
	private ITransactionAttributeBinder transactionalBinder;

	public JdbcTransactionalInterceptor() {
	}

	@VisibleForTesting
	JdbcTransactionalInterceptor(Provider<ResourcesHolder> resourcesHolderProvider, ITransactionAttributeBinder transactionalBinder) {
		this.resourcesHolderProvider = resourcesHolderProvider;
		this.transactionalBinder = transactionalBinder;
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Transactional transactional = readTransactionMetadata(methodInvocation);

		if (transactional == null) {
			return methodInvocation.proceed();
		}

		try {
			return manageTransaction(methodInvocation, transactional);
		} finally {
			logger.debug("Exiting {}", JdbcTransactionalInterceptor.class.getSimpleName());
		}
	}

	private Object manageTransaction(MethodInvocation methodInvocation, Transactional transactional) throws Throwable {
		try {
			transactionalBinder.bindTransactionalToCurrentTransaction(transactional);

			Object obj = methodInvocation.proceed();

			commitTransaction();

			return obj;
		} catch (Throwable t) {
			rollbackOrCommitTransaction(transactional, t);

			throw t;
		} finally {
			transactionalBinder.invalidateTransactionalInCurrentTransaction();
		}
	}

	private void commitTransaction() throws SQLException {
		ConnectionResource resource = resourcesHolderProvider.get().get(ConnectionResource.class);

		if (resource != null) {
			resource.commit();
			logger.debug("Transaction was commited");
		}
	}

	private void rollbackOrCommitTransaction(Transactional transactional, Throwable e) throws SQLException {
		if (needRollback(transactional, e)) {
			rollbackTransaction();
		} else {
			commitTransaction();
		}
	}

	private void rollbackTransaction() throws SQLException {
		ConnectionResource resource = resourcesHolderProvider.get().get(ConnectionResource.class);

		if (resource != null) {
			resource.rollback();
			logger.debug("Transaction was rolled back");
		}
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
