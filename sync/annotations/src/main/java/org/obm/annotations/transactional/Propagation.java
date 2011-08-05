package org.obm.annotations.transactional;

public enum Propagation {
	
	/**
	 * Support a current transaction, create a new one if none exists.
	 * This is the default setting of a transaction annotation.
	 */
	REQUIRED,

	/**
	 * Execute within a nested transaction if a current transaction exists, create a new one if none exists.
	 */
	NESTED;
}
