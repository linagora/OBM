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

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.obm.configuration.TransactionConfiguration;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
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
