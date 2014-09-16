/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.transactional;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;

import org.aopalliance.intercept.MethodInvocation;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.annotations.transactional.Transactional;
import org.obm.servlet.filter.resource.ResourcesHolder;
import org.obm.sync.ConnectionResource;

import com.google.inject.util.Providers;

public class JdbcTransactionalInterceptorTest {

	private IMocksControl control;
	private ITransactionAttributeBinder binder;
	private ResourcesHolder holder;
	private MethodInvocation invocation;
	private ConnectionResource connectionResource;
	private TestObject testObject;

	private JdbcTransactionalInterceptor interceptor;

	@Before
	public void setUp() {
		control = createControl();

		testObject = new TestObject();

		holder = control.createMock(ResourcesHolder.class);
		binder = control.createMock(ITransactionAttributeBinder.class);
		invocation = control.createMock(MethodInvocation.class);
		connectionResource = control.createMock(ConnectionResource.class);

		interceptor = new JdbcTransactionalInterceptor(Providers.of(holder), binder);
	}

	@After
	public void tearDown() {
		control.verify();
	}

	@Test
	public void testInvokeOnNonTransactionalMethod() throws Throwable {
		expect(invocation.getMethod()).andReturn(TestObject.class.getDeclaredMethod("nonTransactionalMethod"));
		expect(invocation.getThis()).andReturn(testObject);
		expect(invocation.proceed()).andReturn(null);

		control.replay();

		assertThat(interceptor.invoke(invocation)).isNull();
	}

	@Test
	public void testInvokeOnTransactionalMethodCommitsTransaction() throws Throwable {
		expect(invocation.getMethod()).andReturn(TestObject.class.getDeclaredMethod("transactionalMethod"));
		expect(invocation.getThis()).andReturn(testObject);
		expect(invocation.proceed()).andReturn(null);

		binder.bindTransactionalToCurrentTransaction(isA(Transactional.class));
		expectLastCall();
		binder.invalidateTransactionalInCurrentTransaction();
		expectLastCall();

		expect(holder.get(ConnectionResource.class)).andReturn(connectionResource);
		connectionResource.commit();
		expectLastCall();

		control.replay();

		assertThat(interceptor.invoke(invocation)).isNull();
	}

	@Test(expected = Throwable.class)
	public void testInvokeOnTransactionalMethodRollsBackTransactionOnError() throws Throwable {
		expect(invocation.getMethod()).andReturn(TestObject.class.getDeclaredMethod("transactionalMethod"));
		expect(invocation.getThis()).andReturn(testObject);
		expect(invocation.proceed()).andThrow(new Throwable());

		binder.bindTransactionalToCurrentTransaction(isA(Transactional.class));
		expectLastCall();
		binder.invalidateTransactionalInCurrentTransaction();
		expectLastCall();

		expect(holder.get(ConnectionResource.class)).andReturn(connectionResource);
		connectionResource.rollback();
		expectLastCall();

		control.replay();

		assertThat(interceptor.invoke(invocation)).isNull();
	}

	private class TestObject {

		@Transactional
		private void transactionalMethod() {
		}

		@SuppressWarnings("unused")
		private void nonTransactionalMethod() {
		}

	}
}
