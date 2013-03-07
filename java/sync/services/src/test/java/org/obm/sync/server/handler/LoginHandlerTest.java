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
package org.obm.sync.server.handler;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.sync.ObmSmtpConf;
import org.obm.sync.server.Request;
import org.obm.sync.server.XmlResponder;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.HelperService;

@RunWith(SlowFilterRunner.class)
public class LoginHandlerTest {

	private ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
	private ServletOutputStream servletOutputStream = new ServletOutputStream() {
		@Override
		public void write(int b) throws IOException {
			resultStream.write(b);
		}
	};

	@Rule
	public JUnitGuiceRule rule = new JUnitGuiceRule(Env.class);

	@Inject
	private LoginHandler handler;
	@Inject
	private IMocksControl control;
	@Inject
	private ResultSet rs;

	@Before
	public void setUp() {
		resultStream.reset();
	}

	@After
	public void tearDown() {
		control.verify();
		control.reset();
	}
	
	@Test
	public void testDoLoginNoParameters() throws Exception {
		ImmutableMap<String, String> parameters = ImmutableMap.<String, String> of();
		HttpServletRequest httpServletRequest = createHttpServletRequestMock(parameters);

		assertLoginResult(httpServletRequest, "loginRefusedWithNullOrigin.xml");
	}

	@Test
	public void testDoLoginWithOnlyLogin() throws Exception {
		ImmutableMap<String, String> parameters = ImmutableMap.<String, String> of("login", "usera");
		HttpServletRequest httpServletRequest = createHttpServletRequestMock(parameters);

		assertLoginResult(httpServletRequest, "loginRefusedWithNullOrigin.xml");
	}

	@Test
	public void testDoLoginWithOnlyOrigin() throws Exception {
		ImmutableMap<String, String> parameters = ImmutableMap.<String, String> of("origin", "origin");
		HttpServletRequest httpServletRequest = createHttpServletRequestMock(parameters);

		assertLoginResult(httpServletRequest, "loginRefusedWithNullLogin.xml");
	}
	
	@Test
	public void testDoLoginUnknownDomainForLogin() throws Exception {
		ImmutableMap<String, String> parameters = ImmutableMap.<String, String> of("origin", "origin", "login", "usera");
		HttpServletRequest httpServletRequest = createHttpServletRequestMock(parameters);

		expect(rs.next()).andReturn(false).anyTimes(); // So that domain selection returns nothing
		
		assertLoginResult(httpServletRequest, "loginFailedForUserUsera.xml");
	}
	
	@Test
	public void testDoLoginInvalidPassword() throws Exception {
		ImmutableMap<String, String> parameters = ImmutableMap.<String, String> of("origin", "origin", "login", "usera", "password", "invalid");
		HttpServletRequest httpServletRequest = createHttpServletRequestMock(parameters);

		expectPaswordVerification("valid");
		
		assertLoginResult(httpServletRequest, "loginFailedForUserUsera.xml");
	}

	private void expectPaswordVerification(String password) throws SQLException {
		expect(rs.next()).andReturn(true).once(); // So that domain selection returns something...
		expect(rs.next()).andReturn(false).once(); // ...but exactly one record
		expect(rs.getString(eq(1))).andReturn("domain").once(); // Domain name
		expect(rs.next()).andReturn(true).once(); // So that the password selection returns something
		expect(rs.getString(eq(1))).andReturn("PLAIN").once(); // Password type
		expect(rs.getString(eq(2))).andReturn(password).once();// Password
	}

	private void assertLoginResult(HttpServletRequest httpServletRequest, String controlFile) throws Exception {
		XmlResponder responder = createResponder();

		control.replay();
		handler.handle(new Request(httpServletRequest), responder);

		XMLAssert.assertXMLEqual(IOUtils.toString(getClass().getResourceAsStream(controlFile)), resultStream.toString());
	}

	private XmlResponder createResponder() throws IOException {
		HttpServletResponse response = control.createMock(HttpServletResponse.class);

		expect(response.getOutputStream()).andReturn(servletOutputStream).anyTimes();
		response.setContentType(isA(String.class));
		expectLastCall().anyTimes();

		return new XmlResponder(response);
	}

	private HttpServletRequest createHttpServletRequestMock(final Map<String, String> parameters) {
		HttpSession session = control.createMock(HttpSession.class);
		HttpServletRequest request = control.createMock(HttpServletRequest.class);

		expect(session.getId()).andReturn("sessionId").anyTimes();
		session.invalidate();
		expectLastCall().anyTimes();

		expect(request.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
		expect(request.getHeader(isA(String.class))).andReturn(null).anyTimes();
		expect(request.getPathInfo()).andReturn("/login/doLogin").anyTimes();
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getSession(anyBoolean())).andReturn(session).anyTimes();
		expect(request.getParameterMap()).andReturn(parameters).anyTimes();
		expect(request.getParameter(isA(String.class))).andAnswer(new IAnswer<String>() {
			@Override
			public String answer() throws Throwable {
				return parameters.get(getCurrentArguments()[0]);
			}
		}).anyTimes();

		return request;
	}

	private static class Env extends AbstractModule {
		private IMocksControl control = createControl();
		
		private <T> T bindMock(Class<T> cls) {
			T mock = control.createMock(cls);
			
			bind(cls).toInstance(mock);
			
			return mock;
		}
		
		@Override
		protected void configure() {
			bind(String.class).annotatedWith(Names.named("application-name")).toInstance("obm-sync");
			Connection connection = control.createMock(Connection.class);
			PreparedStatement ps = control.createMock(PreparedStatement.class);
			ResultSet rs = control.createMock(ResultSet.class);
			
			DatabaseConnectionProvider dbcp = bindMock(DatabaseConnectionProvider.class);
			DomainService domainService = bindMock(DomainService.class);
			
			bind(IMocksControl.class).toInstance(control);
			bind(ResultSet.class).toInstance(rs);
			
			bindMock(HelperService.class);
			bindMock(SettingsService.class);
			bindMock(UserService.class);
			bindMock(ConfigurationService.class);
			bindMock(ObmSmtpConf.class);
			bindMock(ITemplateLoader.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
			
			expect(domainService.findDomainByName(isA(String.class))).andAnswer(new IAnswer<ObmDomain>() {
				@Override
				public ObmDomain answer() throws Throwable {
					return ObmDomain
            				.builder()
            				.id(1)
            				.name(String.valueOf(getCurrentArguments()[0]))
        					.uuid("1234567890")
            				.build();
				}
			}).anyTimes();
			
			try {
				expect(dbcp.getConnection()).andReturn(connection).anyTimes();
				expect(connection.prepareStatement(isA(String.class))).andReturn(ps).anyTimes();
				expect(ps.executeQuery()).andReturn(rs).anyTimes();
				
				ps.setString(anyInt(), isA(String.class));
				expectLastCall().anyTimes();
				ps.setInt(anyInt(), anyInt());
				expectLastCall().anyTimes();
				ps.setBoolean(anyInt(), anyBoolean());
				expectLastCall().anyTimes();
				ps.close();
				expectLastCall().anyTimes();
				
				connection.close();
				expectLastCall().anyTimes();
				
				rs.close();
				expectLastCall().anyTimes();
			}
			catch (Exception e) {
			}
		}
	}
}
