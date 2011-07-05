/*
 * Created on Oct 29, 2003
 *
 */
package fr.aliasource.obm.aliapool.tm;

import java.util.ArrayList;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tom
 *
 */
public class Tx implements Transaction {

	private int status;
	private ArrayList<XAResource> xaResources;

	private ArrayList<Synchronization> syncs;
	private boolean rollbackOnly;
	private Log logger;

	private Xid xid;

	public Tx() throws InterruptedException {
		xid = TxIdFactory.getInstance().createNew();
		syncs = new ArrayList<Synchronization>(1);
		xaResources = new ArrayList<XAResource>(5);
		status = Status.STATUS_NO_TRANSACTION;
		logger = LogFactory.getLog(getClass());
		rollbackOnly = false;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.Transaction#commit()
	 */
	public void commit()
		throws
			RollbackException,
			HeuristicMixedException,
			HeuristicRollbackException,
			SecurityException,
			SystemException {
		try {
			if (rollbackOnly) {
				rollback();
			} else {
				if (status != Status.STATUS_ACTIVE) {
					throw new SystemException("Committing a non active tx");
				}
				// precommit
				for (int i = 0; i < syncs.size(); i++) {
					syncs.get(i).beforeCompletion();
				}

				status = Status.STATUS_COMMITTING;
				for (int i = 0; i < xaResources.size(); i++) {
					XAResource xares = xaResources.get(i);
					xares.commit(xid, true);
				}
				xaResources.clear();

				status = Status.STATUS_COMMITTED;
				if (logger.isDebugEnabled()) {
					logger.debug("Commited.");
				}

				// postcommit
				for (int i = 0; i < syncs.size(); i++) {
					syncs.get(i).afterCompletion(
						Status.STATUS_COMMITTED);
				}

				syncs.clear();
			}
		} catch (XAException e) {
			throw new SystemException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.transaction.Transaction#delistResource(javax.transaction.xa.XAResource, int)
	 */
	public boolean delistResource(XAResource xares, int xastatus)
		throws IllegalStateException, SystemException {
		if (xares != null && !xaResources.contains(xares)) {
			throw new IllegalStateException(
				"Ressource not enlisted previously (xares=" + xares + ")");
		}
		try {
			xares.end(xid, xastatus);
		} catch (XAException e) {
			throw new SystemException(e.getMessage());
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.Transaction#enlistResource(javax.transaction.xa.XAResource)
	 */
	public boolean enlistResource(XAResource xares)
		throws RollbackException, IllegalStateException, SystemException {
		rollbackOnly = false;
		if (xares == null) {
			throw new SystemException("Trying to enlist a null XAResource");
		}
		try {
			xares.start(xid, 0);
			xaResources.add(xares);
		} catch (XAException e) {
			throw new SystemException(e.getMessage());
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.Transaction#getStatus()
	 */
	public int getStatus() throws SystemException {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.Transaction#registerSynchronization(javax.transaction.Synchronization)
	 */
	public void registerSynchronization(Synchronization arg0)
		throws RollbackException, IllegalStateException, SystemException {
		syncs.add(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.transaction.Transaction#rollback()
	 */
	public void rollback() throws IllegalStateException, SystemException {

		try {
			status = Status.STATUS_ROLLING_BACK;

			for (int i = 0; i < xaResources.size(); i++) {
				XAResource xares = (XAResource) xaResources.get(i);
				xares.rollback(xid);
			}

			status = Status.STATUS_ROLLEDBACK;
		} catch (XAException e) {
			throw new SystemException(e.getMessage());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Rolled back");
		}

		// post-rollback
		for (int i = 0; i < syncs.size(); i++) {
			((Synchronization) syncs.get(i)).afterCompletion(
				Status.STATUS_ROLLEDBACK);
		}

		syncs.clear();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.Transaction#setRollbackOnly()
	 */
	public void setRollbackOnly()
		throws IllegalStateException, SystemException {
		if (logger.isDebugEnabled()) {
			logger.debug("setRollbackOnly");
		}
		status = Status.STATUS_MARKED_ROLLBACK;
		rollbackOnly = true;
	}

}
