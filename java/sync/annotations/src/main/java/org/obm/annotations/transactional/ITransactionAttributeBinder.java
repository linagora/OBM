package org.obm.annotations.transactional;

public interface ITransactionAttributeBinder {

	void bindTransactional(Transactional transactional) throws TransactionException;

	void invalidateTransactional() throws TransactionException;

	Transactional getCurrentTransactional() throws TransactionException;

}
