package org.obm.push.backend;

import org.obm.push.bean.BackendSession;
import org.obm.push.handler.IContinuationHandler;


public interface IContinuation {

	Boolean isInitial();
	Boolean isResumed();
	
	void suspend(BackendSession bs, long secondsTimeout);

	void resume();
	
	void error(String status);

	Boolean isError();

	String getErrorStatus();

	BackendSession getBackendSession();
	
	IListenerRegistration getListenerRegistration();
	void setListenerRegistration(IListenerRegistration reg);

	CollectionChangeListener getCollectionChangeListener();
	void setCollectionChangeListener(CollectionChangeListener l);
	
	int getReqId();
	
	void setLastContinuationHandler(IContinuationHandler iContinuationHandler);
	
	IContinuationHandler getLastContinuationHandler();
}
