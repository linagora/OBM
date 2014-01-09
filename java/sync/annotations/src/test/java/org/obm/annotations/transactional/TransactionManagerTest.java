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
package org.obm.annotations.transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.obm.transaction.TransactionManagerRule;

import bitronix.tm.BitronixTransactionManager;


public class TransactionManagerTest {

	@Rule public TransactionManagerRule transactionManagerRule = new TransactionManagerRule();
	
	private BitronixTransactionManager tm;

	@Before
	public void setUp() {
		tm = transactionManagerRule.getTransactionManager();
	}
	
	@After
	public void cleanUp() throws IllegalStateException, SecurityException, SystemException {
		if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
			tm.rollback();
		}
		tm.shutdown();
	}
	
	@Test
	public void testCommitTransaction() throws Exception {
		tm.begin();
		assertNotNull(tm.getTransaction());
		tm.commit();
		assertNull(tm.getTransaction());
	}
	
	@Test
	public void testRollbackTransaction() throws Exception {
		tm.begin();
		assertNotNull(tm.getTransaction());
		tm.rollback();
		assertNull(tm.getTransaction());
	}
	
	@Test
	public void testEmptyTransaction() throws Exception {
		assertNull(tm.getTransaction());
	}
	
	@Ignore("bitronix doesn't support nested transactions")
	@Test
	public void testNestedTransaction() throws Exception {
		tm.begin();
		final Transaction t1 = tm.getTransaction();
		tm.begin();
		final Transaction t2 = tm.getTransaction();
		tm.commit();
		final Transaction t3 = tm.getTransaction();
		tm.commit();		
		
		assertNotNull(t1);
		assertNotNull(t2);
		assertNotNull(t3);
		Assert.assertNotSame(t1, t2);
		Assert.assertSame(t1, t3);
		assertNull(tm.getTransaction());
	}
	
	@Ignore("too long, should be integration testing")
	@Test
	public void testDefaultTimeout() throws Exception {
		tm.begin();
		assertTrue(tm.getStatus() == Status.STATUS_ACTIVE);
		Thread.sleep(61000);
		assertTrue(tm.getStatus() == Status.STATUS_MARKED_ROLLBACK);
		tm.rollback();
	}

	@Test
	public void testSet2sTimeout() throws Exception {
		tm.setTransactionTimeout(2);
		tm.begin();
		assertTrue(tm.getStatus() == Status.STATUS_ACTIVE);
		Thread.sleep(3000);
		assertTrue(tm.getStatus() == Status.STATUS_MARKED_ROLLBACK);
	}
	
}
