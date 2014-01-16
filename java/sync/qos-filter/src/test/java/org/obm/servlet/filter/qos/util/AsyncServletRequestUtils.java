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
package org.obm.servlet.filter.qos.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Request;

import com.google.common.collect.Lists;

public class AsyncServletRequestUtils {

	private final Async async;
	private final String serviceUri;
	
	public AsyncServletRequestUtils(ExecutorService executorService, int port, String servletName) {
		this.async = Async.newInstance().use(executorService);
		this.serviceUri = "http://localhost:" + port + "/" + servletName;

	}
	
	public List<Integer> codes(List<StatusLine> statusList) {
		List<Integer> codes = Lists.newArrayList();
		for (StatusLine statusLine: statusList) {
			codes.add(statusLine.getStatusCode());
		}
		return codes;
	}
	
	public List<StatusLine> retrieveRequestsStatus(List<Future<StatusLine>> requests) throws InterruptedException, ExecutionException, TimeoutException {
		List<StatusLine> statusList = Lists.newArrayList();
		for (Future<StatusLine> future: requests) {
			statusList.add(retrieveRequestStatus(future));
		}
		return statusList;
	}

	public StatusLine retrieveRequestStatus(Future<StatusLine> request)
			throws InterruptedException, ExecutionException, TimeoutException {
		return request.get(15, TimeUnit.SECONDS);
	}


	public List<Future<StatusLine>> asyncHttpGets(int number) {
		List<Future<StatusLine>> list = Lists.newArrayList();
		for (int i = 0; i < number; ++i) {
			list.add(async.execute(httpGet(), new HttpStatusHandler()));
		}
		return list;
	}
	
	public Future<StatusLine> asyncHttpGet() {
		System.out.println("CLIENT: sending request");
		return async.execute(httpGet(), new HttpStatusHandler());
	}

	public Request httpGet() {
		return Request.Get(serviceUri);
	}
	
}
