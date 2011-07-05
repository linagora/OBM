package org.obm.push.impl;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;

public class PushContinuation implements IContinuation {

	private static int id = 0;

	private final static String KEY_BACKEND_SESSION = "key_backend_session";
	private final static String KEY_IS_ERROR = "key_is_error";
	private final static String KEY_STATUS_ERROR = "key_stauts_error";
	private final static String KEY_COLLECTION_CHANGE_LISTENER = "key_collection_change_listener";
	private final static String KEY_LISTENER_REGISTRATION = "key_listener_registration";
	private final static String KEY_ID_REQUEST = "key_id_request";
	
	private Continuation c;

	public PushContinuation(HttpServletRequest req) {
		this.c = ContinuationSupport.getContinuation(req);
		this.c.setAttribute(KEY_ID_REQUEST, id++);
	}

	public int getReqId() {
		return (Integer) c.getAttribute(KEY_ID_REQUEST);
	}

	@Override
	public void resume() {
		c.resume();
	}

	@Override
	public void suspend(long msTimeout) {
		c.setTimeout(msTimeout);
		c.suspend();
	}

	@Override
	public void error(String status) {
		c.setAttribute(KEY_IS_ERROR, true);
		c.setAttribute(KEY_STATUS_ERROR, status);
	}

	@Override
	public BackendSession getBackendSession() {
		return (BackendSession) c.getAttribute(KEY_BACKEND_SESSION);
	}

	@Override
	public void setBackendSession(BackendSession bs) {
		c.setAttribute(KEY_BACKEND_SESSION, bs);
	}

	@Override
	public Boolean isError() {
		Object err = c.getAttribute(KEY_IS_ERROR);
		return err != null ? (Boolean) err : false;
	}

	@Override
	public String getErrorStatus() {
		return (String) c.getAttribute(KEY_STATUS_ERROR);
	}

	@Override
	public IListenerRegistration getListenerRegistration() {
		return (IListenerRegistration) c.getAttribute(KEY_LISTENER_REGISTRATION);
	}

	@Override
	public void setListenerRegistration(IListenerRegistration reg) {
		c.setAttribute(KEY_LISTENER_REGISTRATION, reg);
	}

	@Override
	public CollectionChangeListener getCollectionChangeListener() {
		return (CollectionChangeListener) c.getAttribute(KEY_COLLECTION_CHANGE_LISTENER);
	}

	@Override
	public void setCollectionChangeListener(CollectionChangeListener l) {
		c.setAttribute(KEY_COLLECTION_CHANGE_LISTENER, l);
	}

	@Override
	public Boolean isInitial() {
		return c.isInitial();
	}

	@Override
	public Boolean isResumed() {
		return c.isResumed();
	}

}
