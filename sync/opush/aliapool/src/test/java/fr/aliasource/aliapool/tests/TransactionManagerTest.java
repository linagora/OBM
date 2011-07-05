package fr.aliasource.aliapool.tests;

import static org.junit.Assert.*;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;

import org.junit.Test;

import fr.aliasource.obm.aliapool.tm.TransactionManager;


public class TransactionManagerTest {


	private TransactionManager getTm() {
		return TransactionManager.getInstance();
	}

	@Test
	public void testNested() throws Exception {
		getTm().begin();
		boolean reached = false;
		try {
			getTm().begin();
			fail("A nested transaction should be reported here");
		} catch (NotSupportedException nse) {
			reached = true;
		}
		assertTrue(reached);
		getTm().rollback();
	}

	@Test
	public void testEmptyTx() throws Exception {
		assertNull(getTm().getTransaction());
		assertTrue(getTm().getStatus() == Status.STATUS_NO_TRANSACTION);

		getTm().begin();
		assertNotNull(getTm().getTransaction());
		assertTrue(getTm().getStatus() == Status.STATUS_ACTIVE);
		getTm().rollback();

		assertNull(getTm().getTransaction());

		getTm().begin();
		assertNotNull(getTm().getTransaction());
		assertTrue(getTm().getStatus() == Status.STATUS_ACTIVE);
		getTm().commit();

		assertNull(getTm().getTransaction());
	}

}
