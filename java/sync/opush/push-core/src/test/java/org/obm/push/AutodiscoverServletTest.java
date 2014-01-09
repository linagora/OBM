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
package org.obm.push;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.same;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.internal.MocksControl;
import org.easymock.internal.MocksControl.MockType;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.handler.AutodiscoverHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.resource.ResourcesService;

import com.google.common.collect.Sets;


public class AutodiscoverServletTest {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private AutodiscoverHandler autodiscoverHandler;
	private Responder responder;
	private ResourcesService resourcesService;
	private UserDataRequest userDataRequest;
	private Credentials credentials;
	private MocksControl mocksControl;
	
	@Before
	public void setUp() {
		mocksControl = new MocksControl(MockType.DEFAULT);
		
		credentials = mocksControl.createMock(Credentials.class);
		
		request = mocksControl.createMock(HttpServletRequest.class);
		expect(request.getMethod()).andReturn("POST");
		expect(request.getAttribute(RequestProperties.CREDENTIALS)).andReturn(credentials);
		
		response = mocksControl.createMock(HttpServletResponse.class);
		autodiscoverHandler = mocksControl.createMock(AutodiscoverHandler.class);
		resourcesService = mocksControl.createMock(ResourcesService.class);
	}
	
	@Test
	public void testClosingUserDataRequestResources() throws ServletException, IOException {
		userDataRequest = mocksControl.createMock(UserDataRequest.class);
		resourcesService.closeResources(userDataRequest);
		
		AutodiscoverServlet autodiscoverServlet = createAutodiscoverServlet(autodiscoverHandler);
		mocksControl.replay();
		
		autodiscoverServlet.service(request, response);
		
		mocksControl.verify();
	}
	
	private LoggerService bindLoggerService() {
		LoggerService loggerService = mocksControl.createMock(LoggerService.class);
		loggerService.defineCommand("autodiscover");
		return loggerService;
	}
	
	private ResponderImpl.Factory bindResponderFactory(Responder responder) {
		ResponderImpl.Factory responderFactory = mocksControl.createMock(ResponderImpl.Factory.class);
		expect(responderFactory.createResponder(request, response)).andReturn(responder).anyTimes();
		return responderFactory;
	}

	private void bindProcess(Responder responder) throws IOException {
		autodiscoverHandler.process(isNull(IContinuation.class), 
									anyObject(UserDataRequest.class), 
									anyObject(ActiveSyncRequest.class), 
									eq(responder));
	}
	
	private AutodiscoverServlet createAutodiscoverServlet(AutodiscoverHandler autodiscoverHandler) 
			throws IOException {
		
		LoggerService loggerService = bindLoggerService();
		
		responder = mocksControl.createMock(Responder.class);
		ResponderImpl.Factory responderFactory = bindResponderFactory(responder);
		bindProcess(responder);

		UserDataRequest.Factory userDataRequestFactory = mocksControl.createMock(UserDataRequest.Factory.class);
		expect(userDataRequestFactory.createUserDataRequest(same(credentials), eq("autodiscover"), isNull(Device.class)))
			.andReturn(userDataRequest).anyTimes();
		
		return new AutodiscoverServlet(
					autodiscoverHandler, 
					responderFactory, 
					userDataRequestFactory,
					loggerService,
					Sets.newHashSet(resourcesService));
	}
}
