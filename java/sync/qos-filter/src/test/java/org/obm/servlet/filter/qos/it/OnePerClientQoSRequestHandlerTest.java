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
package org.obm.servlet.filter.qos.it;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.StatusLine;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.servlet.filter.qos.handlers.BusinessKeyProvider;
import org.obm.servlet.filter.qos.util.AsyncServletRequestUtils;
import org.obm.servlet.filter.qos.util.BlockingServletUtils;
import org.obm.servlet.filter.qos.util.KeyByRequestProvider;
import org.obm.servlet.filter.qos.util.server.BlockingServlet;
import org.obm.servlet.filter.qos.util.server.EmbeddedServer;
import org.obm.servlet.filter.qos.util.server.QoSFilterTestModule;

import com.google.inject.Inject;

@GuiceModule(OnePerClientQoSRequestHandlerTest.Configuration.class)
@RunWith(GuiceRunner.class)
public class OnePerClientQoSRequestHandlerTest {
	
	public static class Configuration extends NPerClientQosConfiguration {
		@Override
		protected int getN() {
			return 1;
		}
	}
	
	@Inject IMocksControl control;
	@Inject BusinessKeyProvider<String> businessKeyProvider;
	@Inject EmbeddedServer server;
	@Inject BlockingServlet blockingServlet;

	private AsyncServletRequestUtils async;
	private ExecutorService threadpool;

	private BlockingServletUtils blockingServletUtils;

	@Before
	public void setup() throws Exception {
		threadpool = Executors.newFixedThreadPool(4);
		server.start();
		async = new AsyncServletRequestUtils(threadpool, server.getPort(), QoSFilterTestModule.BLOCKING_SERVLET_NAME);
		blockingServletUtils = new BlockingServletUtils(blockingServlet);
		System.out.println("test started");
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
		threadpool.shutdown();
	}
	
	@Test
	public void oneAcceptedOneReject() throws InterruptedException, ExecutionException, TimeoutException {
		expect(businessKeyProvider.provideKey(anyObject(HttpServletRequest.class))).andReturn("sameKey").anyTimes();
		control.replay();

		Future<StatusLine> request1 = async.asyncHttpGet();
		blockingServletUtils.waitingServletRequestHandling();
		Future<StatusLine> request2 = async.asyncHttpGet();
		StatusLine response2 = async.retrieveRequestStatus(request2);
		blockingServletUtils.unlockServerRequestHandling("unlock request1");
		StatusLine response1 = async.retrieveRequestStatus(request1);
		
		assertThat(response1).isNotNull();
		assertThat(response2).isNotNull();
		assertThat(response1.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		assertThat(response2.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_UNAUTHORIZED);
		control.verify();
	}

	@Test
	public void oneAcceptedTenReject() throws InterruptedException, ExecutionException, TimeoutException {
		expect(businessKeyProvider.provideKey(anyObject(HttpServletRequest.class))).andReturn("sameKey").anyTimes();
		control.replay();

		Future<StatusLine> request1 = async.asyncHttpGet();
		blockingServletUtils.waitingServletRequestHandling();
		List<Future<StatusLine>> requests = async.asyncHttpGets(10);
		List<StatusLine> responses = async.retrieveRequestsStatus(requests);
		blockingServletUtils.unlockServerRequestHandling("unlock request1");
		StatusLine response1 = async.retrieveRequestStatus(request1);
		
		assertThat(response1).isNotNull();
		assertThat(responses).isNotNull();
		assertThat(response1.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		assertThat(async.codes(responses)).containsOnly(org.apache.http.HttpStatus.SC_UNAUTHORIZED);
		control.verify();
	}
	
	@Test
	public void acceptedTwoSerialRequests() throws InterruptedException, ExecutionException, TimeoutException {
		expect(businessKeyProvider.provideKey(anyObject(HttpServletRequest.class))).andReturn("sameKey").anyTimes();
		control.replay();

		Future<StatusLine> request1 = async.asyncHttpGet();
		blockingServletUtils.waitingServletRequestHandling();
		blockingServletUtils.unlockServerRequestHandling("unlock request1");
		Future<StatusLine> request2 = async.asyncHttpGet();
		blockingServletUtils.unlockServerRequestHandling("unlock request2");
		
		StatusLine response1 = async.retrieveRequestStatus(request1);
		StatusLine response2 = async.retrieveRequestStatus(request2);
		
		assertThat(response1).isNotNull();
		assertThat(response2).isNotNull();
		assertThat(response1.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		assertThat(response2.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		control.verify();
	}
	
	@Test
	public void acceptedTwoRequestsWithDifferentKeys() throws InterruptedException, ExecutionException, TimeoutException {
		expect(businessKeyProvider.provideKey(anyObject(HttpServletRequest.class))).andDelegateTo(new KeyByRequestProvider()).anyTimes(); 
		control.replay();

		Future<StatusLine> request1 = async.asyncHttpGet();
		blockingServletUtils.waitingServletRequestHandling();
		Future<StatusLine> request2 = async.asyncHttpGet();
		blockingServletUtils.waitingServletRequestHandling();
		blockingServletUtils.unlockServerRequestHandling("unlock request1");
		blockingServletUtils.unlockServerRequestHandling("unlock request2");
		
		StatusLine response1 = async.retrieveRequestStatus(request1);
		StatusLine response2 = async.retrieveRequestStatus(request2);
		
		assertThat(response1).isNotNull();
		assertThat(response2).isNotNull();
		assertThat(response1.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		assertThat(response2.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		control.verify();
	}

}