/*
 * Created on Mar 19, 2004
 *
 */
package fr.aliasource.obm.aliapool.tm;

import java.util.concurrent.Semaphore;

import javax.transaction.xa.Xid;

/**
 * We do not support distributed transaction so a const
 * branch qualifier seems ok
 * 
 * @author tom
 *
 */
public class TxIdFactory {

	private static final String BRANCH_QUALIFIER = "txBranch";

	private static TxIdFactory factory;

	public static TxIdFactory getInstance() {
		return factory;
	}

	static {
		factory = new TxIdFactory();
	}

	private int idGen;

	private byte[] branchQualifier;

	private Semaphore idGenLock;

	private TxIdFactory() {
		idGen = 0;
		branchQualifier = BRANCH_QUALIFIER.getBytes();
		idGenLock = new Semaphore(1);
	}

	public Xid createNew() throws InterruptedException {
		Xid ret = null;
		idGenLock.acquire();
		ret =
			new TxId(
				++idGen,
				branchQualifier,
				(idGen + "-" + branchQualifier).getBytes());
		idGenLock.release();
		return ret;
	}

}
