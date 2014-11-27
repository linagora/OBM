/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.obm.servlet.filter.qos.QoSAction;
import org.obm.servlet.filter.qos.QoSContinuationSupport;
import org.obm.servlet.filter.qos.QoSFilter;
import org.obm.servlet.filter.qos.QoSRequestHandler;
import org.obm.servlet.filter.qos.handlers.ConcurrentRequestInfoStore.RequestInfoReference;
import org.obm.servlet.filter.qos.handlers.ConcurrentRequestInfoStore.StoreFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class RejectCeilRequestHandler<K extends Serializable> implements QoSRequestHandler {

	private final Logger logger = LoggerFactory.getLogger(QoSFilter.class);
	
	public final static String REJECTING_CEIL_PER_CLIENT_PARAM = "rejectingCeilPerClient";

	private final BusinessKeyProvider<K> businessKeyProvider;
	private final ConcurrentRequestInfoStore<K> concurrentRequestInfoStore;
	private final NPerClientQoSRequestSuspendHandler<K> suspendHandler;
	private final int rejectingCeilPerClient;
	
	@Inject
	@VisibleForTesting RejectCeilRequestHandler(
			BusinessKeyProvider<K> businessKeyProvider,
			ConcurrentRequestInfoStore<K> concurrentRequestInfoStore,
			QoSContinuationSupport continuationSupport,
			@Named(NPerClientQoSRequestHandler.MAX_REQUESTS_PER_CLIENT_PARAM) int maxSimultaneousRequestsPerClient,
			@Named(REJECTING_CEIL_PER_CLIENT_PARAM) int rejectingCeilPerClient) {

		Preconditions.checkArgument(rejectingCeilPerClient > maxSimultaneousRequestsPerClient, String.format( 
				"rejectingCeilPerClient[%d] must be greater than maxSimultaneousRequestsPerClient[%d]", 
				rejectingCeilPerClient, maxSimultaneousRequestsPerClient));
		
		logger.info("max request per client before suspend new ones: {}", maxSimultaneousRequestsPerClient);
		logger.info("max request per client before reject new ones: {}", rejectingCeilPerClient);
		
		this.businessKeyProvider = businessKeyProvider;
		this.concurrentRequestInfoStore = concurrentRequestInfoStore;
		this.rejectingCeilPerClient = rejectingCeilPerClient;
		this.suspendHandler = new NPerClientQoSRequestSuspendHandler<K>(
				businessKeyProvider, concurrentRequestInfoStore, continuationSupport, maxSimultaneousRequestsPerClient);
	}

	@Override
	public final QoSAction startRequestHandling(final HttpServletRequest request) throws ServletException {
		final K key = businessKeyProvider.provideKey(request);
		return concurrentRequestInfoStore.executeInTransaction(key, new StoreFunction<K, QoSAction>() {
			@Override
			public QoSAction execute(RequestInfoReference<K> ref) {
				return startRequestImpl(request, key, ref);
			}

			@Override
			public void cleanup(RequestInfoReference<K> store) {
				// nothing to cleanup
			}
		});
	}

	@VisibleForTesting QoSAction startRequestImpl(final HttpServletRequest request, final K key, RequestInfoReference<K> ref) {
		RequestInfo<K> requestInfo = ref.get();
		if (requestInfo.getPendingRequestCount() >= rejectingCeilPerClient) {
			logger.warn("a request is rejected for the key:{}", key);
			return QoSAction.REJECT;
		} else {
			return suspendHandler.startRequestHandling(request);
		}
	}
	
	@Override
	public final void finishRequestHandling(final HttpServletRequest request) {
		suspendHandler.finishRequestHandling(request);
	}
}
