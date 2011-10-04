package org.obm.annotations.transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import bitronix.tm.TransactionManagerServices;

public class TransactionManagerTest {

	private TransactionManager tm;

	@Before
	public void setUp() {
		tm = TransactionManagerServices.getTransactionManager();
	}
	
	@After
	public void cleanUp() throws IllegalStateException, SecurityException, SystemException {
		if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
			tm.rollback();
		}
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
