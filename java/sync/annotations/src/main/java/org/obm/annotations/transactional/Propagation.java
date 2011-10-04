package org.obm.annotations.transactional;

public enum Propagation {
	
	/**
	 * Method needs a transaction to run. A transaction is created 
	 * if none is associated with current context and use current 
	 * transaction if it already exists.
	 * This is the default setting of a transaction annotation.
	 */
	REQUIRED,

	/**
	 * Method needs to be run in its own transaction.
	 * A new transaction is always created.
	 */
	REQUIRES_NEW;
}
