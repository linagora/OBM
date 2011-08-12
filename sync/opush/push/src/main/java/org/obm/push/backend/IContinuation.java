package org.obm.push.backend;

import org.obm.push.bean.BackendSession;
import org.obm.push.handler.IContinuationHandler;


public interface IContinuation {

	Boolean isInitial();
	Boolean isResumed();
	
	void suspend(long msTimeout);

	void resume();
	
	void error(String status);

	Boolean isError();

	String getErrorStatus();

	BackendSession getBackendSession();
	void setBackendSession(BackendSession bs);
	
	IListenerRegistration getListenerRegistration();
	void setListenerRegistration(IListenerRegistration reg);

	CollectionChangeListener getCollectionChangeListener();
	void setCollectionChangeListener(CollectionChangeListener l);
	
	int getReqId();
	
	void setLastContinuationHandler(IContinuationHandler iContinuationHandler);
	
	IContinuationHandler getLastContinuationHandler();
}
