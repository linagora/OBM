package org.obm.annotations.transactional;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class TransactionAttributeBinderTest {
	
	
	@Test
	public void testBindTransactionalWithNotNullTransactional() throws TransactionException, SystemException{
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		Transaction transaction = EasyMock.createStrictMock(Transaction.class);
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testBindTransactionalWithNullTransactional() throws TransactionException, SystemException{
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		EasyMock.expect(transactionManager.getTransaction()).andReturn(null).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		EasyMock.verify(mocks);
	}
	
	@Test
	public void testGetCurrentTransactionalWithExistingTransaction() throws SystemException, TransactionException{
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		Transaction transaction = EasyMock.createStrictMock(Transaction.class);
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		Transactional transactionalReturned = attributeBinder.getCurrentTransactional();
		EasyMock.verify(mocks);
		
		Assert.assertEquals(transactional, transactionalReturned);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalWithoutExistingTransaction() throws SystemException, TransactionException{
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		EasyMock.expect(transactionManager.getTransaction()).andReturn(null).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.getCurrentTransactional();
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalWithUnknownTransaction() throws SystemException, TransactionException{
		Transaction transaction = EasyMock.createStrictMock(Transaction.class);
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.getCurrentTransactional();
		EasyMock.verify(mocks);
	}
	
	@Test
	public void testInvalidateTransactionalWithExistingTransaction() throws SystemException, TransactionException{
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		Transaction transaction = EasyMock.createStrictMock(Transaction.class);
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		attributeBinder.invalidateTransactional();
		EasyMock.verify(mocks);
		
	}
	
	@Test(expected=TransactionException.class)
	public void testInvalidateTransactionalWithoutExistingTransaction() throws SystemException, TransactionException{
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		
		EasyMock.expect(transactionManager.getTransaction()).andReturn(null).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.invalidateTransactional();
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalWithInvalidateTransaction() throws SystemException, TransactionException {
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		Transaction transaction = EasyMock.createStrictMock(Transaction.class);
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).times(3);
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		attributeBinder.invalidateTransactional();
		attributeBinder.getCurrentTransactional();
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testBindTransactionalThrowingSystemException() throws TransactionException, SystemException{
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		EasyMock.expect(transactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalThrowingSystemException() throws SystemException, TransactionException{
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		Transaction transaction = EasyMock.createStrictMock(Transaction.class);
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		EasyMock.expect(transactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		Transactional transactionalReturned = attributeBinder.getCurrentTransactional();
		EasyMock.verify(mocks);
		
		Assert.assertEquals(transactional, transactionalReturned);
	}
	
	@Test(expected=TransactionException.class)
	public void testInvalidateTransactionalThrowingSystemException() throws SystemException, TransactionException{
		Transactional transactional = EasyMock.createStrictMock(Transactional.class); 
		Transaction transaction = EasyMock.createStrictMock(Transaction.class);
		
		TransactionManager transactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		TransactionConfiguration transactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		EasyMock.expect(transactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		
		EasyMock.expect(transactionManager.getTransaction()).andReturn(transaction).once();
		EasyMock.expect(transactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		Object[] mocks = { transactionManager, transactionConfiguration };
		EasyMock.replay(mocks);
		
		TransactionAttributeBinder attributeBinder = new TransactionAttributeBinder(transactionManager, transactionConfiguration);
		attributeBinder.bindTransactional(transactional);
		attributeBinder.invalidateTransactional();
		EasyMock.verify(mocks);
		
	}
	
	
}
