package org.obm.annotations.transactional;

public class TransactionException extends Exception{

	public TransactionException(Exception e) {
		super(e);
	}

	public TransactionException(String message) {
		super(message);
	}

}
