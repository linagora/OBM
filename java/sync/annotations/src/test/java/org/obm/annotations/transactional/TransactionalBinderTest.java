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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.easymock.IMocksControl;
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
	private IMocksControl control;
	private TransactionProvider transactionProvider;
	
	private TransactionalBinder attributeBinder;
	
	@Before
	public void init(){
		control = createControl();
		this.mockTransactional = control.createMock(Transactional.class); 
		this.mockTransaction = control.createMock(Transaction.class);
		this.mockTransactionManager = control.createMock(TransactionManager.class);
		this.transactionProvider = control.createMock(TransactionProvider.class);
		this.mockTransactionConfiguration = control.createMock(TransactionConfiguration.class);

		expect(transactionProvider.get()).andReturn(mockTransactionManager).anyTimes();
		
		this.attributeBinder = new TransactionalBinder(transactionProvider);
	}
	
	@Test
	public void testBindTransactionalWithNotNullTransactional() throws TransactionException, SystemException{
		
		expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		
		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		control.verify();
	}
	
	@Test(expected=TransactionException.class)
	public void testBindTransactionalWithNullTransactional() throws TransactionException, SystemException{
		
		expect(mockTransactionManager.getTransaction()).andReturn(null).once();
		
		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		control.verify();
	}
	
	@Test
	public void testGetCurrentTransactionalWithExistingTransaction() throws SystemException, TransactionException{
		
		expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).times(2);

		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		Transactional transactionalReturned = attributeBinder.getTransactionalInCurrentTransaction();
		control.verify();
		
		Assert.assertEquals(mockTransactional, transactionalReturned);
	}

	@Test
	public void testGetCurrentTransactionalWithoutExistingTransaction() throws Exception {
		expect(mockTransactionManager.getTransaction()).andReturn(null).once();
		
		control.replay();
		
		Transactional transactional = attributeBinder.getTransactionalInCurrentTransaction();
		control.verify();

		Assert.assertNull(transactional);
	}

	@Test
	public void testGetCurrentTransactionalWithUnknownTransaction() throws Exception {
		expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		
		control.replay();
		
		Transactional transactional = attributeBinder.getTransactionalInCurrentTransaction();
		control.verify();

		Assert.assertNull(transactional);
	}
	
	@Test
	public void testInvalidateTransactionalWithExistingTransaction() throws SystemException, TransactionException{
		
		expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).times(2);
		
		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		control.verify();
		
	}
	
	@Test(expected=TransactionException.class)
	public void testInvalidateTransactionalWithoutExistingTransaction() throws SystemException, TransactionException{
		
		expect(mockTransactionManager.getTransaction()).andReturn(null).once();
		
		control.replay();
		
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		control.verify();
	}

	@Test
	public void testGetCurrentTransactionalWithInvalidateTransaction() throws Exception {
		expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).times(3);
		
		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		Transactional transactional = attributeBinder.getTransactionalInCurrentTransaction();
		control.verify();

		Assert.assertNull(transactional);
	}
	
	@Test(expected=TransactionException.class)
	public void testBindTransactionalThrowingSystemException() throws TransactionException, SystemException{
		
		expect(mockTransactionConfiguration.getTimeOutInSecond()).andReturn(60).once();
		expect(mockTransactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		control.verify();
	}
	
	@Test(expected=TransactionException.class)
	public void testGetCurrentTransactionalThrowingSystemException() throws SystemException, TransactionException{
		
		expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		expect(mockTransactionManager.getTransaction()).andThrow(new SystemException()).once();

		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		Transactional mockTransactionalReturned = attributeBinder.getTransactionalInCurrentTransaction();
		control.verify();
		
		Assert.assertEquals(mockTransactional, mockTransactionalReturned);
	}
	
	@Test(expected=TransactionException.class)
	public void testInvalidateTransactionalThrowingSystemException() throws SystemException, TransactionException{
		
		expect(mockTransactionManager.getTransaction()).andReturn(mockTransaction).once();
		expect(mockTransactionManager.getTransaction()).andThrow(new SystemException()).once();
		
		control.replay();
		
		attributeBinder.bindTransactionalToCurrentTransaction(mockTransactional);
		attributeBinder.invalidateTransactionalInCurrentTransaction();
		control.verify();
	}
	
}
