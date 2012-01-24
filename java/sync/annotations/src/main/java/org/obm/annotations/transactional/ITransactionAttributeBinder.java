package org.obm.annotations.transactional;

public interface ITransactionAttributeBinder {

	void bindTransactionalToCurrentTransaction(Transactional transactional) throws TransactionException;

	void invalidateTransactionalInCurrentTransaction() throws TransactionException;

	Transactional getTransactionalInCurrentTransaction() throws TransactionException;

}
