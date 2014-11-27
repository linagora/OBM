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
package org.obm.servlet.filter.qos.handlers;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.obm.servlet.filter.qos.QoSAction;
import org.obm.servlet.filter.qos.QoSContinuationSupport;
import org.obm.servlet.filter.qos.QoSContinuationSupport.QoSContinuation;
import org.obm.servlet.filter.qos.handlers.ConcurrentRequestInfoStore.RequestInfoReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class NPerClientQoSRequestSuspendHandler<K extends Serializable> extends NPerClientQoSRequestHandler<K> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final QoSContinuationSupport continuationSupport;

	@Inject
	@VisibleForTesting NPerClientQoSRequestSuspendHandler(
			BusinessKeyProvider<K> businessKeyProvider,
			ConcurrentRequestInfoStore<K> concurrentRequestInfoStore,
			QoSContinuationSupport continuationSupport,
			@Named(MAX_REQUESTS_PER_CLIENT_PARAM) int maxSimultaneousRequestsPerClient) {
		super(businessKeyProvider, concurrentRequestInfoStore, maxSimultaneousRequestsPerClient, QoSAction.SUSPEND);
		this.continuationSupport = continuationSupport;
	}

	@Override
	protected QoSAction startRequestImpl(RequestInfoReference<K> ref, HttpServletRequest request) {
		QoSAction action = super.startRequestImpl(ref, request);
		RequestInfo<K> info = ref.get();
		if (action == QoSAction.SUSPEND) {
			logger.debug("will suspend request {}", request.getQueryString());
			ref.put(info.appendContinuation(continuationSupport.getContinuationFor(request)));
		}
		return action;
	}

	@Override
	protected void requestDoneImpl(RequestInfoReference<K> ref) {
		super.requestDoneImpl(ref);
		RequestInfo<K> info = ref.get();
		QoSContinuation continuation = info.nextContinuation();
		if (continuation != null) {
			logger.debug("resume continuation after request");
			ref.put(info.popContinuation());
			continuation.getContinuation().resume();
		} else {
			logger.debug("no continuation to resume");
		}
	}

	@Override
	protected void cleanupImpl(RequestInfoReference<K> ref) {
		RequestInfo<K> info = ref.get();
		if (info.getContinuationIds().isEmpty()) {
			super.cleanupImpl(ref);
		}
	}
	
}
