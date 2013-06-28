/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.provisioning;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.DomainDao;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.UserDao;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.util.Modules;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

public abstract class CommonDomainEndPointEnvTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			install(Modules.override(new ProvisioningService()).with(new AbstractModule() {

				private IMocksControl mocksControl = createControl();

				@Override
				protected void configure() {
					bind(IMocksControl.class).toInstance(mocksControl);
					bind(UserDao.class).toInstance(mocksControl.createMock(UserDao.class));
					bind(DomainDao.class).toInstance(mocksControl.createMock(DomainDao.class));
					bind(BatchDao.class).toInstance(mocksControl.createMock(BatchDao.class));

					bind(DatabaseConnectionProvider.class).toInstance(mocksControl.createMock(DatabaseConnectionProvider.class));
				}

				@Provides
				@Singleton
				protected Server createServer() {
					Server server = new Server(0);
					Context root = new Context(server, "/", Context.SESSIONS);

					root.addFilter(GuiceFilter.class, "/*", 0);
					root.addServlet(DefaultServlet.class, "/*");

					return server;
				}

			}));
		}

	}

	protected final static ObmDomain domain = ObmDomain.builder().name("domain").id(1).uuid(ObmDomainUuid.of("a3443822-bb58-4585-af72-543a287f7c0e")).build();

	@Inject
	protected IMocksControl mocksControl;
	@Inject
	protected Server server;
	@Inject
	protected DomainDao domainDao;
	@Inject
	protected UserDao userDao;

	protected String baseUrl;
	protected int serverPort;

	@Before
	public void setUp() throws Exception {
		server.start();
		serverPort = server.getConnectors()[0].getLocalPort();
		baseUrl = "http://localhost:" + serverPort + ProvisioningService.PROVISIONING_URL_PREFIX;
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	protected void expectDomain() {
		expect(domainDao.findDomainByUuid(domain.getUuid())).andReturn(domain);
	}

	protected void expectNoDomain() {
		expect(domainDao.findDomainByUuid(domain.getUuid())).andReturn(null);
	}

	protected HttpResponse get(String path) throws Exception {
		return createGetRequest(path).execute().returnResponse();
	}

	protected HttpResponse post(String path, StringEntity content) throws ClientProtocolException, IOException {
		return createPostRequest(path, content).execute().returnResponse();
	}

	protected HttpResponse put(String path, StringEntity content) throws ClientProtocolException, IOException {
		return createPutRequest(path, content).execute().returnResponse();
	}

	protected HttpResponse delete(String path) throws ClientProtocolException, IOException {
		return createDeleteRequest(path).execute().returnResponse();
	}

	private Request createPostRequest(String path, StringEntity content) {
		return Request.Post(baseUrl + "/" + domain.getUuid().get() + path).body(content);
	}

	private Request createPutRequest(String path, StringEntity content) {
		return Request.Put(baseUrl + "/" + domain.getUuid().get() + path).body(content);
	}

	protected Request createGetRequest(String path) {
		return Request.Get(baseUrl + "/" + domain.getUuid().get() + path);
	}

	protected Request createDeleteRequest(String path) {
		return Request.Delete(baseUrl + "/" + domain.getUuid().get() + path);
	}

}
