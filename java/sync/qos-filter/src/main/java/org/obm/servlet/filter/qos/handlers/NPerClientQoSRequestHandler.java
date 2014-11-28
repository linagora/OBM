/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.servlet.filter.qos.handlers;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.obm.servlet.filter.qos.QoSAction;
import org.obm.servlet.filter.qos.QoSRequestHandler;
import org.obm.servlet.filter.qos.handlers.TransactionalKeyRequestsInfoStore.RequestInfoReference;
import org.obm.servlet.filter.qos.handlers.TransactionalKeyRequestsInfoStore.StoreFunction;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class NPerClientQoSRequestHandler<K extends Serializable> implements QoSRequestHandler {

	@VisibleForTesting final class StartRequestFunction implements StoreFunction<K, QoSAction> {
		private final HttpServletRequest request;

		private StartRequestFunction(HttpServletRequest request) {
			this.request = request;
		}

		@Override
		public QoSAction execute(RequestInfoReference<K> ref) {
			return startRequestImpl(ref, request);
		}
		
		@Override
		public void cleanup(RequestInfoReference<K> store) {
			//nothing goes here
		}
	}
	
	@VisibleForTesting final class RequestDoneFunction implements StoreFunction<K, Void> {

		private RequestDoneFunction() {
			super();
		}

		@Override
		public Void execute(RequestInfoReference<K> store) {
			requestDoneImpl(store);
			return null;
		}

		@Override
		public void cleanup(RequestInfoReference<K> store) {
			cleanupImpl(store);
		}
	}

	public static final String MAX_REQUESTS_PER_CLIENT_PARAM = "maxSimultaneousRequestsPerClient";
	public static final String QOS_ACTION = "QoSAction";

	protected final TransactionalKeyRequestsInfoStore<K> store;
	protected final BusinessKeyProvider<K> businessKeyProvider;

	private final int maxSimultaneousRequestsPerClient;
	private final QoSAction qosAction;

	@Inject
	@VisibleForTesting NPerClientQoSRequestHandler(BusinessKeyProvider<K> businessKeyProvider,
			TransactionalKeyRequestsInfoStore<K> concurrentRequestInfoStore,
			@Named(MAX_REQUESTS_PER_CLIENT_PARAM) int maxSimultaneousRequestsPerClient) {
		this(businessKeyProvider, concurrentRequestInfoStore, maxSimultaneousRequestsPerClient, QoSAction.REJECT);
	}
	
	protected NPerClientQoSRequestHandler(
			BusinessKeyProvider<K> businessKeyProvider,
			TransactionalKeyRequestsInfoStore<K> concurrentRequestInfoStore,
			int maxSimultaneousRequestsPerClient, QoSAction qosAction) {
		this.businessKeyProvider = businessKeyProvider;
		this.store = concurrentRequestInfoStore;
		this.maxSimultaneousRequestsPerClient = maxSimultaneousRequestsPerClient;
		this.qosAction = qosAction;
	}

	@Override
	public final QoSAction startRequestHandling(final HttpServletRequest request) {
		final K key = businessKeyProvider.provideKey(request);
		return store.executeInTransaction(key, new StartRequestFunction(request));
	}

	@Override
	public final void finishRequestHandling(final HttpServletRequest request) {
		final K key = businessKeyProvider.provideKey(request);
		store.executeInTransaction(key, new RequestDoneFunction());
	}
	
	protected QoSAction startRequestImpl(RequestInfoReference<K> ref, @SuppressWarnings("unused") HttpServletRequest request) {
		KeyRequestsInfo<K> requestInfoHolder = ref.get();
		if (requestInfoHolder.getNumberOfRunningRequests() >= maxSimultaneousRequestsPerClient) {
			return qosAction;
		} else {
			ref.put(requestInfoHolder.oneMoreRequest());
			return QoSAction.ACCEPT;
		}
	}
	
	protected void requestDoneImpl(RequestInfoReference<K> ref) {
		KeyRequestsInfo<K> info = ref.get();
		KeyRequestsInfo<K> newInfo = info.removeOneRequest();
		ref.put(newInfo);
	}
	
	protected void cleanupImpl(RequestInfoReference<K> ref) {
		KeyRequestsInfo<K> info = ref.get();
		if (info.getPendingRequestCount() == 0) {
			ref.clear();
		}
	}
	
}
