/*
 * Created on Oct 29, 2003
 *
 */
package fr.aliasource.obm.aliapool.tm;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tom
 *
 */
public class TransactionManager
	implements javax.transaction.TransactionManager, UserTransaction {

	private static TransactionManager singleton;

	public static TransactionManager getInstance() {
		return singleton;
	}

	static {
		singleton = new TransactionManager();
	}

	private ThreadLocal<Tx> txContext;
	private ThreadLocal<StackTraceElement[]> allocationStack;
	private Log logger;

	private TransactionManager() {
		logger = LogFactory.getLog(getClass());
		txContext = new ThreadLocal<Tx>();
		allocationStack = new ThreadLocal<StackTraceElement[]>();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#begin()
	 */
	public void begin() throws NotSupportedException, SystemException {
		if (txContext.get() != null) {
			StackTraceElement[] frames =
				(StackTraceElement[]) allocationStack.get();
			if (frames != null) {
				logger.warn("The previous tx began here :");
				for (int i = 0; i < frames.length; i++) {
					System.err.println(frames[i].toString());
				}
				logger.warn("End of previous tx start stack");
			}

			throw new NotSupportedException(
				"Nested transaction not " + "supported by alia-pool");
		} else {
			//if (logger.isDebugEnabled()) {
			allocationStack.set(new Throwable().getStackTrace());
			//}
		}

		Tx tx;
		try {
			tx = new Tx();
		} catch (InterruptedException e) {
			SystemException toThrow = new SystemException(e.getMessage());
			toThrow.initCause(e);
			throw toThrow;
		}

		txContext.set(tx);
		tx.setStatus(Status.STATUS_ACTIVE);
		if (logger.isDebugEnabled()) {
			logger.debug("aliapool TX BEGIN");
		}
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#commit()
	 */
	public void commit()
		throws
			RollbackException,
			HeuristicMixedException,
			HeuristicRollbackException,
			SecurityException,
			IllegalStateException,
			SystemException {

		((Transaction) txContext.get()).commit();
		txContext.set(null);
		allocationStack.set(null);
		if (logger.isDebugEnabled()) {
			logger.debug("aliapool TX COMMIT");
		}
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#getStatus()
	 */
	public int getStatus() throws SystemException {
		if (getTransaction() == null) {
			return Status.STATUS_NO_TRANSACTION;
		} else {
			return getTransaction().getStatus();
		}
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#getTransaction()
	 */
	public Transaction getTransaction() throws SystemException {
		return (Transaction) txContext.get();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
	 */
	public void resume(Transaction arg0)
		throws InvalidTransactionException, IllegalStateException, SystemException {
		throw new SystemException("Unsupported/Unimplemented operation");
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#rollback()
	 */
	public void rollback()
		throws IllegalStateException, SecurityException, SystemException {
		Transaction tx = (Transaction) txContext.get();
		tx.rollback();
		txContext.set(null);
		allocationStack.set(null);
		if (logger.isDebugEnabled()) {
			logger.debug("aliapool TX ROLLBACK");
		}
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#setRollbackOnly()
	 */
	public void setRollbackOnly()
		throws IllegalStateException, SystemException {
		getTransaction().setRollbackOnly();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
	 */
	public void setTransactionTimeout(int arg0) throws SystemException {
		throw new SystemException("Unsupported/Unimplemented operation");
	}

	/* (non-Javadoc)
	 * @see javax.transaction.TransactionManager#suspend()
	 */
	public Transaction suspend() throws SystemException {
		throw new SystemException("Unsupported/Unimplemented operation");
	}

}
