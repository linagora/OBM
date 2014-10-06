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
package org.obm.satellite.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.obm.satellite.client.Configuration.SatelliteProtocol;
import org.obm.satellite.client.exceptions.SatteliteClientException;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.UserPassword;


public class ConnectionImplTest {

	private TestServlet servlet;
	private Server server;
	private int serverPort;
	private ConnectionImpl testee;
	private IMocksControl control;
	private Configuration configuration;
	private CloseableHttpClient httpClient;
	private final ObmDomain domain = ObmDomain
			.builder()
			.name("domain")
			.host(ServiceProperty.IMAP, ObmHost.builder().ip("localhost").name("localIMAP").build())
			.host(ServiceProperty.SMTP_IN, ObmHost.builder().ip("localhost").name("localMTA").build())
			.build();
	private final ObmDomain domainWithNoHosts = ObmDomain
			.builder()
			.name("domain")
			.build();
	private final ObmDomain domainWithThreeSmtpInHosts = ObmDomain
			.builder()
			.name("domain")
			.host(ServiceProperty.SMTP_IN, ObmHost.builder().ip("localhost").name("localMTA").build())
			.host(ServiceProperty.SMTP_IN, ObmHost.builder().ip("localhost").name("localMTA_2").build())
			.host(ServiceProperty.SMTP_IN, ObmHost.builder().ip("localhost").name("localMTA_3").build())
			.build();

	@Before
	public void setUp() throws Exception {
		control = createControl();
		configuration = control.createMock(Configuration.class);

		servlet = new TestServlet();
		server = createServer();
		server.start();
		serverPort = server.getConnectors()[0].getLocalPort();
		httpClient = HttpClientBuilder.create().build();
		testee = new ConnectionImpl(httpClient, configuration, domain);
	}

	@After
	public void tearDown() throws Exception {
		control.verify();
		server.stop();
		httpClient.close();
	}

	@Test(expected = SatteliteClientException.class)
	public void testUpdateMTAShouldFailWhenNoSmtpInHostsAreLinked() {
		testee = new ConnectionImpl(httpClient, configuration, domainWithNoHosts);
		control.replay();

		testee.updateMTA();
	}

	@Test(expected = SatteliteClientException.class)
	public void testUpdateIMAPServerShouldFailWhenNoIMAPHostsAreLinked() {
		testee = new ConnectionImpl(httpClient, configuration, domainWithNoHosts);

		expect(configuration.isIMAPServerManaged()).andReturn(true);
		control.replay();

		testee.updateIMAPServer();
	}

	@Test
	public void testUpdateMTA() {
		expect(configuration.getSatelliteProtocol()).andReturn(SatelliteProtocol.HTTP);
		expect(configuration.getSatellitePort()).andReturn(serverPort);
		expect(configuration.getUsername()).andReturn("user");
		expect(configuration.getPassword()).andReturn(UserPassword.valueOf("pass"));
		control.replay();

		testee.updateMTA();

		assertThat(servlet.requests).isEqualTo(ImmutableMap.of("/postfixsmtpinmaps/host/localMTA", "Basic dXNlcjpwYXNz"));
	}

	@Test
	public void testUpdateMTAShouldUpdateAllSmtpInHosts() {
		testee = new ConnectionImpl(httpClient, configuration, domainWithThreeSmtpInHosts);

		expect(configuration.getSatelliteProtocol()).andReturn(SatelliteProtocol.HTTP).times(3);
		expect(configuration.getSatellitePort()).andReturn(serverPort).times(3);
		expect(configuration.getUsername()).andReturn("user").times(3);
		expect(configuration.getPassword()).andReturn(UserPassword.valueOf("pass")).times(3);
		control.replay();

		testee.updateMTA();

		assertThat(servlet.requests).isEqualTo(ImmutableMap.of(
				"/postfixsmtpinmaps/host/localMTA", "Basic dXNlcjpwYXNz",
				"/postfixsmtpinmaps/host/localMTA_2", "Basic dXNlcjpwYXNz",
				"/postfixsmtpinmaps/host/localMTA_3", "Basic dXNlcjpwYXNz"));
	}

	@Test(expected = SatteliteClientException.class)
	public void testUpdateMTAOnError() {
		expect(configuration.getSatelliteProtocol()).andReturn(SatelliteProtocol.HTTP);
		expect(configuration.getSatellitePort()).andReturn(serverPort);
		expect(configuration.getUsername()).andReturn("user");
		expect(configuration.getPassword()).andReturn(UserPassword.valueOf("pass"));
		control.replay();

		servlet.statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;

		testee.updateMTA();
	}

	@Test
	public void testUpdateIMAPServer() {
		expect(configuration.getSatelliteProtocol()).andReturn(SatelliteProtocol.HTTP);
		expect(configuration.getSatellitePort()).andReturn(serverPort);
		expect(configuration.getUsername()).andReturn("user");
		expect(configuration.getPassword()).andReturn(UserPassword.valueOf("pass"));
		expect(configuration.isIMAPServerManaged()).andReturn(true);
		control.replay();

		testee.updateIMAPServer();

		assertThat(servlet.requests).isEqualTo(ImmutableMap.of("/cyruspartition/host/add/localIMAP", "Basic dXNlcjpwYXNz"));
	}

	@Test
	public void testUpdateIMAPServerWhenDisabled() {
		expect(configuration.isIMAPServerManaged()).andReturn(false);
		control.replay();

		testee.updateIMAPServer();

		assertThat(servlet.requests).isEmpty();
	}

	@Test(expected = SatteliteClientException.class)
	public void testUpdateIMAPServerOnError() {
		expect(configuration.getSatelliteProtocol()).andReturn(SatelliteProtocol.HTTP);
		expect(configuration.getSatellitePort()).andReturn(serverPort);
		expect(configuration.getUsername()).andReturn("user");
		expect(configuration.getPassword()).andReturn(UserPassword.valueOf("pass"));
		expect(configuration.isIMAPServerManaged()).andReturn(true);
		control.replay();

		servlet.statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;

		testee.updateIMAPServer();
	}

	private Server createServer() {
		Server server = new Server(0);
		Context root = new Context(server, "/", Context.SESSIONS);

		root.addServlet(new ServletHolder(servlet), "/*");

		return server;
	}

	private class TestServlet extends HttpServlet {

		Map<String, String> requests = Maps.newConcurrentMap();
		int statusCode = HttpStatus.SC_OK;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			requests.put(req.getPathInfo(), req.getHeader("Authorization"));

			resp.setStatus(statusCode);
		}

	}

}
