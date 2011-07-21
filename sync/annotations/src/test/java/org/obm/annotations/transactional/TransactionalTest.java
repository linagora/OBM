package org.obm.annotations.transactional;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.matcher.Matchers;


public class TransactionalTest {

	public static class TestClass {
		@Transactional
		public void successfullMethod() {
			//success !!
		}
		
		@Transactional
		public void throwingRuntimeExceptionMethod() {
			throw new RuntimeException();
		}
		
		@Transactional(noRollbackOn=RuntimeException.class)
		public void throwingRuntimeExceptionButNoRollbackMethod() {
			throw new RuntimeException();
		}
		
		@Transactional
		public void nestedThrowingRuntimeExceptionMethod() {
			throwingRuntimeExceptionMethod();
		}
		
		@Transactional
		public void nestedRuntimeExceptionButNoRollbackMethod() {
			throwingRuntimeExceptionButNoRollbackMethod();
		}
		
		@Transactional
		public void nestedMethod() {
			successfullMethod();
		}
	}
	
	private TestClass createTestClass(final Provider<UserTransaction> provider) {
		Injector injector = Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				bind(UserTransaction.class).toProvider(provider);
				TransactionalInterceptor transactionalInterceptor = new TransactionalInterceptor();
				bindInterceptor(Matchers.any(), 
						Matchers.annotatedWith(Transactional.class), 
						transactionalInterceptor);
				requestInjection(transactionalInterceptor);
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
	public void testOneTransaction() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		UserTransaction transaction = EasyMock.createStrictMock(UserTransaction.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.successfullMethod();
		EasyMock.verify(transaction);
	}

	@Test
	public void testNested() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		UserTransaction transaction = EasyMock.createStrictMock(UserTransaction.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION)).once();
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).once();
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		testClass.nestedMethod();
		EasyMock.verify(transaction);
	}
	
	@Test
	public void testRollback() throws NotSupportedException, SystemException, SecurityException, IllegalStateException {
		UserTransaction transaction = EasyMock.createStrictMock(UserTransaction.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.throwingRuntimeExceptionMethod();	
		} catch (RuntimeException e) {
			return;
		} finally {
			EasyMock.verify(transaction);
		}
		Assert.fail("RuntimeExceptionExpected");
	}

	@Test
	public void testNestedRollback() throws NotSupportedException, SystemException, SecurityException, IllegalStateException {
		UserTransaction transaction = EasyMock.createStrictMock(UserTransaction.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION)).once();
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(2);
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION)).once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.nestedThrowingRuntimeExceptionMethod();
		} catch (RuntimeException e) {
			return;
		} finally {
			EasyMock.verify(transaction);
		}
		Assert.fail("RuntimeExceptionExpected");
	}
	
	@Test(expected=RuntimeException.class)
	public void testRollbackException() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		UserTransaction transaction = EasyMock.createStrictMock(UserTransaction.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE));
		transaction.commit();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.throwingRuntimeExceptionButNoRollbackMethod();	
		} finally {
			EasyMock.verify(transaction);
		}
	}
	
	@Test(expected=RuntimeException.class)
	public void testNestedRollbackException() throws NotSupportedException, SystemException, SecurityException, IllegalStateException {
		UserTransaction transaction = EasyMock.createStrictMock(UserTransaction.class);
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_NO_TRANSACTION));
		transaction.begin();
		EasyMock.expectLastCall().once();
		EasyMock.expect(transaction.getStatus()).andReturn(Integer.valueOf(Status.STATUS_ACTIVE)).times(3);
		transaction.rollback();
		EasyMock.expectLastCall().once();
		EasyMock.replay(transaction);
		
		TestClass testClass = createTestClass(getProvider(transaction));
		try {
			testClass.nestedRuntimeExceptionButNoRollbackMethod();	
		} finally {
			EasyMock.verify(transaction);
		}
	}
}
