/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.technicallog;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.aopalliance.intercept.MethodInvocation;
import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.push.technicallog.bean.KindToBeLogged;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.TechnicalLogging;
import org.obm.push.technicallog.logger.ITechnicalLoggingBinder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.matcher.Matchers;


public class TechnicalLoggingTest {
	
	public abstract static class AbstractTestClass {
		@TechnicalLogging
		public abstract void transaction();
		@TechnicalLogging
		public abstract void throwingMethod();
		@TechnicalLogging
		public abstract void request();
		@TechnicalLogging
		public abstract void resource();
	}
	
	public static class TestClass extends AbstractTestClass {
		@Override
		@TechnicalLogging(kindToBeLogged=KindToBeLogged.TRANSACTION, onStartOfMethod=true, onEndOfMethod=true)
		public void transaction() {
		}

		@Override
		@TechnicalLogging(kindToBeLogged=KindToBeLogged.TRANSACTION, onStartOfMethod=true, onEndOfMethod=true)
		public void throwingMethod() {
			throw new RuntimeException();
		}

		@Override
		@TechnicalLogging(kindToBeLogged=KindToBeLogged.REQUEST, onStartOfMethod=true)
		public void request() {
		}

		@Override
		@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onEndOfMethod=true, resourceType=ResourceType.JDBC_CONNECTION)
		public void resource() {
		}
	}
	
	private TestClass createTestClass(final Provider<ITechnicalLoggingBinder> provider) {
		Injector injector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(ITechnicalLoggingBinder.class).toProvider(provider);
				
				TechnicalLoggingInterceptor technicalLoggingInterceptor = new TechnicalLoggingInterceptor();
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(TechnicalLogging.class), 
						technicalLoggingInterceptor);
				requestInjection(technicalLoggingInterceptor);
			}
		});
		
		return injector.getInstance(TestClass.class);
	}
	
	private <T> Provider<T> getProvider(final T obj) {
		return new Provider<T>() {
			@Override
			public T get() {
				return obj;
			}
		};
	}
	
	@Test
	public void testTransaction() {
		DateTime nullDateTime = null;
		ITechnicalLoggingBinder technicalLoggingBinder = createStrictMock(ITechnicalLoggingBinder.class);
		technicalLoggingBinder.logTransaction(anyObject(DateTime.class), eq(nullDateTime));
		expectLastCall().once();
		technicalLoggingBinder.logTransaction(eq(nullDateTime), anyObject(DateTime.class));
		expectLastCall().once();
		
		replay(technicalLoggingBinder);
		
		TestClass testClass = createTestClass(getProvider(technicalLoggingBinder));
		testClass.transaction();
		
		verify(technicalLoggingBinder);
	}
	
	@Test(expected=RuntimeException.class)
	public void testThrowingMethod() {
		DateTime nullDateTime = null;
		ITechnicalLoggingBinder technicalLoggingBinder = createStrictMock(ITechnicalLoggingBinder.class);
		technicalLoggingBinder.logTransaction(anyObject(DateTime.class), eq(nullDateTime));
		expectLastCall().once();
		technicalLoggingBinder.logTransaction(eq(nullDateTime), anyObject(DateTime.class));
		expectLastCall().once();
		
		replay(technicalLoggingBinder);
		
		TestClass testClass = createTestClass(getProvider(technicalLoggingBinder));
		testClass.throwingMethod();
		
		verify(technicalLoggingBinder);
	}
	
	@Test
	public void testRequest() {
		DateTime nullDateTime = null;
		ITechnicalLoggingBinder technicalLoggingBinder = createStrictMock(ITechnicalLoggingBinder.class);
		technicalLoggingBinder.logRequest(anyObject(MethodInvocation.class), anyObject(DateTime.class), eq(nullDateTime));
		expectLastCall().once();
		
		replay(technicalLoggingBinder);
		
		TestClass testClass = createTestClass(getProvider(technicalLoggingBinder));
		testClass.request();
		
		verify(technicalLoggingBinder);
	}
	
	@Test
	public void testResource() {
		DateTime nullDateTime = null;
		ITechnicalLoggingBinder technicalLoggingBinder = createStrictMock(ITechnicalLoggingBinder.class);
		technicalLoggingBinder.logResource(eq(ResourceType.JDBC_CONNECTION), eq(nullDateTime), anyObject(DateTime.class));
		expectLastCall().once();
		
		replay(technicalLoggingBinder);
		
		TestClass testClass = createTestClass(getProvider(technicalLoggingBinder));
		testClass.resource();
		
		verify(technicalLoggingBinder);
	}
}
