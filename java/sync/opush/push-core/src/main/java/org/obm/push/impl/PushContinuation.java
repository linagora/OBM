/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.impl;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.handler.IContinuationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class PushContinuation implements IContinuation {

	@Singleton
	public static class Factory {
		
		private final AtomicInteger id;
		
		@Inject
		@VisibleForTesting Factory() {
			super();
			this.id = new AtomicInteger();
		}
		
		public PushContinuation createContinuation(ServletRequest req) {
			Continuation continuation = ContinuationSupport.getContinuation(req);
			Object attachedRequestId = continuation.getAttribute(KEY_ID_REQUEST);
			if (attachedRequestId == null) {
				continuation.setAttribute(KEY_ID_REQUEST, id.getAndIncrement());
			}
			return	new PushContinuation(continuation);
		}
	}
	
	private final static String KEY_BACKEND_SESSION = "key_backend_session";
	private final static String KEY_IS_ERROR = "key_is_error";
	private final static String KEY_STATUS_ERROR = "key_status_error";
	private final static String KEY_COLLECTION_CHANGE_LISTENER = "key_collection_change_listener";
	private final static String KEY_LISTENER_REGISTRATION = "key_listener_registration";
	private final static String KEY_ID_REQUEST = "key_id_request";
	private final static String KEY_LAST_CONTINUATION_HANDLER = "key_last_continuation_handler";

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Continuation c;
	
	private PushContinuation(Continuation continuation) {
		this.c = continuation;
	}

	public int getReqId() {
		return (Integer) c.getAttribute(KEY_ID_REQUEST);
	}

	@Override
	public void resume() {
		c.resume();
	}

	@Override
	public void suspend(UserDataRequest udr, long secondsTimeout) {
		logger.info("suspend for {} seconds", secondsTimeout);
		setUserDataRequest(udr);
		c.setTimeout(secondsTimeout * 1000);
		c.suspend();
	}

	@Override
	public void error(String status) {
		c.setAttribute(KEY_IS_ERROR, true);
		c.setAttribute(KEY_STATUS_ERROR, status);
	}

	@Override
	public UserDataRequest getUserDataRequest() {
		return (UserDataRequest) c.getAttribute(KEY_BACKEND_SESSION);
	}

	private void setUserDataRequest(UserDataRequest udr) {
		c.setAttribute(KEY_BACKEND_SESSION, udr);
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
	public void setCollectionChangeListener(ICollectionChangeListener l) {
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

	@Override
	public void setLastContinuationHandler(IContinuationHandler iContinuationHandler) {
		c.setAttribute(KEY_LAST_CONTINUATION_HANDLER, iContinuationHandler);
	}

	@Override
	public IContinuationHandler getLastContinuationHandler() {
		return (IContinuationHandler) c.getAttribute(KEY_LAST_CONTINUATION_HANDLER);
	}

	@Override
	public boolean needsContinuationHandling() {
		return isError() || getCollectionChangeListener() != null;
	}
}
