package org.obm.annotations.transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Assert;
import org.junit.Test;

import com.atomikos.icatch.jta.UserTransactionManager;

public class TransactionManagerTest {

	@Test
	public void testCommitTransaction() throws Exception {
		TransactionManager tm = new UserTransactionManager();
		tm.begin();
		assertNotNull(tm.getTransaction());
		tm.commit();
		assertNull(tm.getTransaction());
	}
	
	@Test
	public void testRollbackTransaction() throws Exception {
		TransactionManager tm = new UserTransactionManager();
		tm.begin();
		assertNotNull(tm.getTransaction());
		tm.rollback();
		assertNull(tm.getTransaction());
	}
	
	@Test
	public void testEmptyTransaction() throws Exception {
		TransactionManager tm = new UserTransactionManager();
		assertNull(tm.getTransaction());
	}
	
	@Test
	public void testNestedTransaction() throws Exception {
		TransactionManager tm = new UserTransactionManager();
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
	
	@Test
	public void testDefaultTimeout() throws Exception {
		TransactionManager tm = new UserTransactionManager();
		tm.begin();
		assertTrue(tm.getStatus() == Status.STATUS_ACTIVE);
		Thread.sleep(11000);
		assertTrue(tm.getStatus() == Status.STATUS_MARKED_ROLLBACK);
		tm.rollback();
	}
	
	@Test
	public void testSet2sTimeout() throws Exception {
		TransactionManager tm = new UserTransactionManager();
		tm.setTransactionTimeout(2);
		tm.begin();
		assertTrue(tm.getStatus() == Status.STATUS_ACTIVE);
		Thread.sleep(3000);
		assertTrue(tm.getStatus() == Status.STATUS_MARKED_ROLLBACK);
	}
	
}
