/*
 * Created on Oct 29, 2003
 *
 */
package fr.aliasource.obm.aliapool.tm;

import javax.transaction.xa.Xid;

/**
 * @author tom
 *
 */
class TxId implements Xid {

	private byte[] branchQual;
	private byte[] globalTransId;

	TxId(int xid, byte[] branchQual, byte[] globalTransId) {
		this.branchQual = branchQual;
		this.globalTransId = globalTransId;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.Xid#getFormatId()
	 */
	public int getFormatId() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.Xid#getBranchQualifier()
	 */
	public byte[] getBranchQualifier() {
		return branchQual;
	}

	/* (non-Javadoc)
	 * @see javax.transaction.xa.Xid#getGlobalTransactionId()
	 */
	public byte[] getGlobalTransactionId() {
		return globalTransId;
	}

}
