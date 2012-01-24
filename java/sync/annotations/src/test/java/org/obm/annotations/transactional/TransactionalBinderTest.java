package org.obm.annotations.transactional;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TransactionalBinderTest {
	
	private Transactional mockTransactional;
	private Transaction mockTransaction;
	private TransactionManager mockTransactionManager;
	private TransactionConfiguration mockTransactionConfiguration;
	
	private TransactionalBinder attributeBinder;
	
	@Before
	public void init(){
		this.mockTransactional = EasyMock.createStrictMock(Transactional.class); 
		this.mockTransaction = EasyMock.createStrictMock(Transaction.class);
		this.mockTransactionManager = EasyMock.createStrictMock(TransactionManager.class); 
		this.mockTransactionConfiguration = EasyMock.createStrictMock(TransactionConfiguration.class);
		
		this.attributeBinder = new TransactionalBinder(mockTransactionManager);
	}
	
	@Test
	public void testBindTransactionalWithNotNullTransactional() throws TransactionException, SystemException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testBindTransactionalWithNullTransactional() throws TransactionException, SystemException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(null).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		EasyMock.verify(mocks);
	}
	
	@Test
	public void testGetCurrentTransactionalWithExistingTransaction() throws SystemException, TransactionException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).times(2);

		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		Transactional transactionalReturned = attributeBinder.getTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
		
		Assert.assertEquals(mockTransactional, transactionalReturned);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalWithoutExistingTransaction() throws SystemException, TransactionException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(null).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.getTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalWithUnknownTransaction() throws SystemException, TransactionException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.getTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
	}
	
	@Test
	public void testInvalidateTransactionalWithExistingTransaction() throws SystemException, TransactionException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).times(2);
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
		
	}
	
	@Test(expected=TransactionException.class)
	public void testInvalidateTransactionalWithoutExistingTransaction() throws SystemException, TransactionException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(null).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalWithInvalidateTransaction() throws SystemException, TransactionException {
		
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).times(3);
		
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		attributeBinder.getTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testBindTransactionalThrowingSystemException() throws TransactionException, SystemException{
		
		EasyMock.expect(mockTransactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		EasyMock.expect(mockTransactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		EasyMock.verify(mocks);
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalThrowingSystemException() throws SystemException, TransactionException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		EasyMock.expect(mockTransactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		Transactional mockTransactionalReturned = attributeBinder.getTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
		
		Assert.assertEquals(mockTransactional, mockTransactionalReturned);
	}
	
	@Test(expected=TransactionException.class)
	public void testInvalidateTransactionalThrowingSystemException() throws SystemException, TransactionException{
		
		EasyMock.expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		EasyMock.expect(mockTransactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		Object[] mocks = getMocksObjects();
		EasyMock.replay(mocks);
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		EasyMock.verify(mocks);
	}
	
	private Object[] getMocksObjects() {
		Object[] mocks = {mockTransactional, mockTransaction, mockTransactionManager, mockTransactionConfiguration };
		return mocks;
	}
	
}
