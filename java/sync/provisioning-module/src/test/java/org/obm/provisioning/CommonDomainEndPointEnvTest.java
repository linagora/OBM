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
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.codehaus.jackson.map.InjectableValues;
import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.obm.DateUtils;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.DomainDao;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserSystemDao;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.satellite.client.SatelliteService;
import org.obm.sync.date.DateProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.util.Modules;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public abstract class CommonDomainEndPointEnvTest {

	public static class Env extends AbstractModule {

		private Server server;
		private Context context;
		
		@Override
		protected void configure() {
			
			server = createServer();
			bind(Server.class).toInstance(server);
			
			context = createContext(server);
			
			install(Modules.override(new ProvisioningService(context.getServletContext())).with(new AbstractModule() {

				private IMocksControl mocksControl = createControl();

				@Override
				protected void configure() {
					bind(IMocksControl.class).toInstance(mocksControl);
					bind(UserDao.class).toInstance(mocksControl.createMock(UserDao.class));
					bind(DomainDao.class).toInstance(mocksControl.createMock(DomainDao.class));
					bind(BatchDao.class).toInstance(mocksControl.createMock(BatchDao.class));
					bind(UserSystemDao.class).toInstance(mocksControl.createMock(UserSystemDao.class));
					bind(ResourceForTest.class);
					bind(SatelliteService.class).toInstance(mocksControl.createMock(SatelliteService.class));
					bind(BatchProcessor.class).toInstance(mocksControl.createMock(BatchProcessor.class));
					bind(DomainBasedSubResourceForTest.class);

					bind(DateProvider.class).toInstance(mocksControl.createMock(DateProvider.class));
					bind(DatabaseConnectionProvider.class).toInstance(mocksControl.createMock(DatabaseConnectionProvider.class));
					bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
				}
			}));
		}
		
		private Context createContext(Server server) {
			Context root = new Context(server, "/", Context.SESSIONS);
			
			root.addFilter(ShiroFilter.class, "/*", 0);
			root.addFilter(GuiceFilter.class, "/*", 0);
			root.addServlet(DefaultServlet.class, "/*");
			
			return root;
		}
		
		private Server createServer() {
			Server server = new Server(0);
			return server;
		}
	}

	protected static final ObmDomain domain = ObmDomain
			.builder()
			.name("domain")
			.id(1)
			.uuid(ObmDomainUuid.of("a3443822-bb58-4585-af72-543a287f7c0e"))
			.build();
	
	protected static final Batch batch = Batch
			.builder()
			.id(batchId(1))
			.domain(domain)
			.status(BatchStatus.ERROR)
			.operation(Operation
					.builder()
					.id(operationId(1))
					.status(BatchStatus.SUCCESS)
					.entityType(BatchEntityType.USER)
					.request(org.obm.provisioning.beans.Request
							.builder()
							.url("/users")
							.verb(HttpVerb.POST)
							.body("{\"id\":123456}")
							.build())
					.build())
			.operation(Operation
					.builder()
					.id(operationId(2))
					.status(BatchStatus.ERROR)
					.entityType(BatchEntityType.USER)
					.error("Invalid User")
					.request(org.obm.provisioning.beans.Request
							.builder()
							.url("/users/1")
							.verb(HttpVerb.PATCH)
							.body("{}")
							.build())
					.build())
			.build();

	@Inject
	protected IMocksControl mocksControl;
	@Inject
	protected Server server;
	@Inject
	protected DomainDao domainDao;
	@Inject
	protected UserDao userDao;
	@Inject
	protected BatchDao batchDao;
	@Inject
	protected ObjectMapper objectMapper;
	@Inject
	protected Realm realm;

	protected String baseUrl;
	protected int serverPort;

	@Before
	public void setUp() throws Exception {
		server.start();
		serverPort = server.getConnectors()[0].getLocalPort();
		baseUrl = "http://localhost:" + serverPort + ProvisioningService.PROVISIONING_URL_PREFIX;

		objectMapper.setInjectableValues(new InjectableValues.Std().addValue(ObmDomain.class, domain));
		SecurityUtils.setSecurityManager(new DefaultWebSecurityManager(realm));
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	protected void expectDomain() {
		expect(domainDao.findDomainByUuid(domain.getUuid())).andReturn(domain);
	}

	protected void expectBatch() throws DaoException {
		expect(batchDao.get(batch.getId())).andReturn(batch);
	}

	protected void expectNoDomain() {
		expect(domainDao.findDomainByUuid(domain.getUuid())).andReturn(null);
	}

	protected void expectNoBatch() throws DaoException {
		expect(batchDao.get(batch.getId())).andReturn(null);
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
	
	protected HttpResponse patch(String path, StringEntity content) throws ClientProtocolException, IOException {
		return new DefaultHttpClient().execute(createPatchRequest(path, content));
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
	
	private HttpRequestBase createPatchRequest(String path, StringEntity content) {
		final HttpPatch patch = new HttpPatch(baseUrl + "/" + domain.getUuid().get() + path);
		patch.setEntity(content);
		return patch;
	}

	protected Request createGetRequest(String path) {
		return Request.Get(baseUrl + "/" + domain.getUuid().get() + path);
	}

	protected Request createDeleteRequest(String path) {
		return Request.Delete(baseUrl + "/" + domain.getUuid().get() + path);
	}

	public static Batch.Id batchId(Integer id) {
		return Batch.Id.builder().id(id).build();
	}

	public static Operation.Id operationId(Integer id) {
		return Operation.Id.builder().id(id).build();
	}

	protected Operation operation(BatchEntityType entityType, String path, String entity, HttpVerb verb, Map<String, String> params) {
		return Operation
				.builder()
				.entityType(entityType)
				.status(BatchStatus.IDLE)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.url(domain.getUuid().get() + path)
						.body(entity)
						.verb(verb)
						.params(params)
						.build())
				.build();
	}

	protected StringEntity obmUserToJson() throws UnsupportedEncodingException {
		final StringEntity userToJson = new StringEntity(obmUserToJsonString());
		userToJson.setContentType(MediaType.APPLICATION_JSON);
		return  userToJson;
	}

	protected String obmUserToJsonString() {
		return 	
			"{" +
				"\"id\":\"extId\"," +
				"\"login\":\"user1\"," +
				"\"lastname\":\"Doe\"," +
				"\"profile\":\"Not implemented yet\"," +
				"\"firstname\":\"Jesus\"," +
				"\"commonname\":\"John Doe\"," +
				"\"password\":\"Not implemented yet\"," +
				"\"kind\":\"Not implemented yet\"," +
				"\"title\":\"title\"," +
				"\"description\":\"description\"," +
				"\"company\":\"Not implemented yet\"," +
				"\"service\":\"service\"," +
				"\"direction\":\"Not implemented yet\"," +
				"\"addresses\":[\"address1\",\"address2\"]," +
				"\"town\":\"town\"," +
				"\"zipcode\":\"zipcode\"," +
				"\"business_zipcode\":\"Not implemented yet\"," +
				"\"country\":\"Not implemented yet\"," +
				"\"phones\":[\"Not implemented yet\"]," +
				"\"mobile\":\"mobile\"," +
				"\"faxes\":[\"Not implemented yet\"]," +
				"\"mail_quota\":\"Not implemented yet\"," +
				"\"mail_server\":\"Not implemented yet\"," +
				"\"mails\":[\"john@domain\"]," +
				"\"timecreate\":\"2013-06-11T12:00:00.000+0000\"," +
				"\"timeupdate\":\"2013-06-11T13:00:00.000+0000\"," +
				"\"groups\":[\"Not implemented yet\"]" +
			"}";
	}
	
	protected ObmUser fakeUser() {
		return ObmUser.builder()
				.domain(domain)
				.extId(userExtId("extId"))
				.login("user1")
				.lastName("Doe")
				//.profile("Utilisateurs")	// Not implemented yet in ObmUser
				.firstName("Jesus")
				.commonName("John Doe")
				//.kind("")					// Not implemented yet in ObmUser
				.title("title")
				.description("description")
				//.company("")				// Not implemented yet in ObmUser
				.service("service")
				//.direction()				// Not implemented yet in ObmUser
				.address1("address1")
				.address2("address2")
				.town("town")
				.zipCode("zipcode")
				//.business_zipcode()		// Not implemented yet in ObmUser
				//.country()				// Not implemented yet in ObmUser
				//.phones()					// Not implemented yet in ObmUser
				.mobile("mobile")
				//.faxes()					// Not implemented yet in ObmUser
				//.mail_quota()				// Not implemented yet in ObmUser
				//.mail_server()			// Not implemented yet in ObmUser
				.emailAndAliases("john@domain")
				.timeCreate(DateUtils.date("2013-06-11T14:00:00"))
				.timeUpdate(DateUtils.date("2013-06-11T15:00:00"))
				//.groups()					// Not implemented yet in ObmUser
				.build();
				
				
	}

	protected UserExtId userExtId(String extId) {
		return UserExtId.builder().extId(extId).build();
	}
}
