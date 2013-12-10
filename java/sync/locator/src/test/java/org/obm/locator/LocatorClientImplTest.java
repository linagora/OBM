/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.locator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.LocatorConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(LocatorClientImplTest.Module.class)
public class LocatorClientImplTest {

	public static class Module extends AbstractModule {

		final IMocksControl control = createControl();
		
		@Override
		protected void configure() {
			install(new BlockingContainerModule());
			bind(IMocksControl.class).toInstance(control);
			
			LocatorConfiguration configurationService = control.createMock(LocatorConfiguration.class);
			bind(LocatorConfiguration.class).toInstance(configurationService);
		}
		
	}
	
	LocatorClientImpl client;
	@Inject IMocksControl control;
	@Inject EmbeddedServer server;
	@Inject BlockingServlet blockingServlet;
	@Inject LocatorConfiguration configurationService;

	@Before
	public void setup() throws Exception {
		server.start();
		expect(configurationService.getLocatorUrl()).andReturn(
				"http://127.0.0.1:" + server.getPort() + "/" + BlockingContainerModule.BLOCKING_SERVLET_NAME);
		expect(configurationService.getLocatorClientTimeoutInSeconds()).andReturn(2).times(2);
		control.replay();
		
		client = new LocatorClientImpl(configurationService, LoggerFactory.getLogger(LocatorClientImplTest.class));
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
		control.verify();
	}
	
	@Test(expected=SocketTimeoutException.class)
	public void testBlockUntilTimeout() throws Throwable { 
		Throwable ex = new Exception();
		String serviceLocation = null;
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		try {
			serviceLocation = client.getServiceLocation("service/prop", "login@domain");
		} catch (LocatorClientException e) {
			ex = e.getCause();
			assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isGreaterThan(2000).isLessThan(3000);
			assertThat(serviceLocation).isNull();
		}
		throw ex;
	}
	
	@Test
	public void testUnLocked() throws Throwable { 
		String expectedResponse = "expected response";
		blockingServlet.unlockNextRequestWithResponse(expectedResponse);
		Stopwatch stopwatch = Stopwatch.createStarted();
		String serviceLocation = client.getServiceLocation("service/prop", "login@domain");
		assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isLessThan(2000);
		assertThat(serviceLocation).isEqualTo(expectedResponse);
	}
	
	@Test
	public void testReturnsOnlyFirstLine() throws Throwable { 
		String expectedResponse = "expected response";
		blockingServlet.unlockNextRequestWithResponse(expectedResponse + "\nnot expected");
		String serviceLocation = client.getServiceLocation("service/prop", "login@domain");
		assertThat(serviceLocation).isEqualTo(expectedResponse);
	}

	@Test(expected=LocatorClientException.class)
	public void testHttpStatusServerError() throws Throwable { 
		blockingServlet.unlockNextRequestWithResponse("ko", HttpStatus.SC_INTERNAL_SERVER_ERROR);
		client.getServiceLocation("service/prop", "login@domain");
	}
	
	@Test(expected=LocatorClientException.class)
	public void testHttpStatusNotFound() throws Throwable { 
		blockingServlet.unlockNextRequestWithResponse("ko", HttpStatus.SC_NOT_FOUND);
		client.getServiceLocation("service/prop", "login@domain");
	}
}
