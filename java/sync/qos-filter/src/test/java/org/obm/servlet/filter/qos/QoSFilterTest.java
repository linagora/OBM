/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.servlet.filter.qos;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheManager;

import org.easymock.IMocksControl;
import org.eclipse.jetty.continuation.Continuation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

/**
 * Tests the {@link QoSFilter} class.
 */
@RunWith(SlowFilterRunner.class)
public class QoSFilterTest {
	private QoSFilter testee;
	private IMocksControl control;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private FilterChain chain;
	private Continuation continuation;
	private QoSRequestHandler qosRequestHandler;
	private QoSContinuationSupport suspender;
	private CacheManager cacheManager;
	
	@Before
	public void setUp() {
		control = createStrictControl();
		suspender = control.createMock(QoSContinuationSupportJettyUtils.class);
		cacheManager = null;
		request = control.createMock(HttpServletRequest.class);
		response = control.createMock(HttpServletResponse.class);
		chain = control.createMock(FilterChain.class);
		continuation = control.createMock(Continuation.class);
		qosRequestHandler = control.createMock(QoSRequestHandler.class);

		// Because we run the tests outside Jetty
		expect(continuation.isInitial()).andReturn(true).anyTimes();
		expect(request.getAttribute(eq(Continuation.ATTRIBUTE))).andReturn(continuation).anyTimes();
	}

	@Test(expected=NullPointerException.class)
	public void testNullHandlers() throws Exception {
		control.replay();

		testee = new QoSFilter(null, suspender, cacheManager);
		testee.doFilter(request, response, chain);
	}

	@Test
	public void handlerAcceptRequest() throws IOException, ServletException {
		expect(qosRequestHandler.startRequestHandling(request)).andReturn(QoSAction.ACCEPT);
		chain.doFilter(request, response);
		qosRequestHandler.finishRequestHandling(request);
		control.replay();
		testee = new QoSFilter(qosRequestHandler, suspender, cacheManager);
		testee.doFilter(request, response, chain);
		control.verify();
	}
	
	@Test
	public void handlerRefuseRequest() throws IOException, ServletException {
		expect(qosRequestHandler.startRequestHandling(request)).andReturn(QoSAction.REJECT);
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		control.replay();
		testee = new QoSFilter(qosRequestHandler, suspender, cacheManager);
		testee.doFilter(request, response, chain);
		control.verify();
	}

	@Test
	public void handlerSuspendRequest() throws IOException, ServletException {
		expect(qosRequestHandler.startRequestHandling(request)).andReturn(QoSAction.SUSPEND);
		suspender.suspend(request);
		expectLastCall().once();
		control.replay();
		testee = new QoSFilter(qosRequestHandler, suspender, cacheManager);
		testee.doFilter(request, response, chain);
		control.verify();
	}
}
