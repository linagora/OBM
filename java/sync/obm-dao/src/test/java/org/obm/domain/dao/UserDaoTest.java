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
package org.obm.domain.dao;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.sync.base.DomainName;
import org.obm.sync.base.EmailLogin;
import org.obm.sync.date.DateProvider;
import org.obm.utils.ObmHelper;

import com.google.common.base.Joiner;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;


@GuiceModule(UserDaoTest.Env.class)
@RunWith(SlowGuiceRunner.class)
public class UserDaoTest {

	public static class Env extends AbstractModule {
		private IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");
			bind(IMocksControl.class).toInstance(mocksControl);

			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Inject
	private IMocksControl mocksControl;
	
	@Inject
	private ObmHelper obmHelper;

	@Rule
	@Inject
	public H2InMemoryDatabase db;

	private UserDao userDao;

	@Before
	public void setUp() {
		userDao = createMockBuilder(UserDao.class)
					.withConstructor(ObmHelper.class)
					.withArgs(obmHelper)
					.addMockedMethod("userIdFromEmailQuery")
					.addMockedMethod("userIdFromLogin")
					.createMock(mocksControl);
	}

	@After
	public void tearDown() {
		mocksControl.verify();
	}

	@Test
	public void testUserIdFromEmailUsesEmailQueryIfEmailGiven() throws Exception {
		Integer userId = 1;
		String email = "usera@obm.com";
		EmailLogin login = new EmailLogin("usera");
		DomainName domain = new DomainName("obm.com");
		Connection connection = mocksControl.createMock(Connection.class);

		expect(userDao.userIdFromEmailQuery(connection, login, domain)).andReturn(userId);
		mocksControl.replay();

		assertThat(userDao.userIdFromEmail(connection, email, 1)).isEqualTo(userId);
	}

	@Test
	public void testUserIdFromEmailUsesLoginQueryIfLoginGiven() throws Exception {
		EmailLogin login = new EmailLogin("usera");
		Integer userId = 1, domainId = 1;
		Connection connection = mocksControl.createMock(Connection.class);

		expect(userDao.userIdFromLogin(connection, login, domainId)).andReturn(userId);
		mocksControl.replay();

		assertThat(userDao.userIdFromEmail(connection, login.get(), domainId)).isEqualTo(userId);
	}

	@Test
	public void testUserIdFromEmailUsesLoginQueryThenEmailQueryIfLoginGiven() throws Exception {
		EmailLogin login = new EmailLogin("usera");
		DomainName domain = new DomainName("-");
		Integer userId = 1, domainId = 1;
		Connection connection = mocksControl.createMock(Connection.class);
		
		expect(userDao.userIdFromLogin(connection, login, domainId)).andReturn(null); // Login fetch returns no result
		expect(userDao.userIdFromEmailQuery(connection, login, domain)).andReturn(userId);
		mocksControl.replay();

		assertThat(userDao.userIdFromEmail(connection, login.get(), 1)).isEqualTo(userId);
	}
	
	@Test
	public void testUserIdFromEmailQuery() throws Exception {
		EmailLogin login = new EmailLogin("usera");
		String domain = "test.com";
		
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 1);
		expectMatchingUserOfEmailQuery(rs, 1, login.get(), domain, null);
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, login, new DomainName(domain))).isEqualTo(1);
	}
	
	@Test
	public void testUserIdFromEmailQueryLoginDifferentCase() throws Exception {
		String loginFromDb = "userA";
		EmailLogin loginFromEvent = new EmailLogin("usera");
		String domain = "obm.com";
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 1);
		expectMatchingUserOfEmailQuery(rs, 1, loginFromDb, domain, null);
		
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, loginFromEvent, new DomainName(domain))).isEqualTo(1);
	}
	
	@Test
	public void testUserIdFromEmailQueryDomainDifferentCase() throws Exception {
		String loginFromDb = "usera";
		String domainFromDb = "tesT.com";
		EmailLogin loginFromEvent = new EmailLogin("usera");
		DomainName domainFromEvent = new DomainName("test.com");
		
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 1);
		expectMatchingUserOfEmailQuery(rs, 1, loginFromDb, domainFromDb, null);
		
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, loginFromEvent, domainFromEvent)).isEqualTo(1);
	}
	
	@Test
	public void testUserIdFromEmailQueryDomainAliasesDifferentCase() throws Exception {
		DomainName domainFromEvent = new DomainName("test.com");
		String loginFromDb = "usera";
		String domainFromDb = "longdomain.com";
		String domainFromDbAliases = Joiner.on(UserDao.DB_INNER_FIELD_SEPARATOR)
				.join("rasta.rocket", "tEst.cOm", "obm.org");
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 1);
		expectMatchingUserOfEmailQuery(rs, 1, loginFromDb, domainFromDb, domainFromDbAliases);
		
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, new EmailLogin(loginFromDb), domainFromEvent)).isEqualTo(1);
	}
	
	@Test
	public void testUserIdFromEmailQueryNoResult() throws Exception {
		EmailLogin login = new EmailLogin("usera");
		DomainName domain = new DomainName("test.com");
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 0);
		
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, login, domain)).isNull();
	}

	@Test
	public void testUserIdFromEmailQueryMultipleResults() throws Exception {
		String email = "usera";
		String domain = "test.com";
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 4);
		expectMatchingUserOfEmailQuery(rs, 1, "useraa", domain, null);
		expectMatchingUserOfEmailQuery(rs, 2, "anotherusera", domain, null);
		expectMatchingUserOfEmailQuery(rs, 3, email, "anotherdomain.com", null);
		expectMatchingUserOfEmailQuery(rs, 4, email, domain, null);
		
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, new EmailLogin(email), new DomainName(domain))).isEqualTo(4);
	}
	
	private void expectEmailQueryCalls(Connection connection, ResultSet rs, int numberOfMatches) throws Exception {
		Statement st = mocksControl.createMock(Statement.class);
		
		expect(connection.createStatement()).andReturn(st);
		expect(st.executeQuery(isA(String.class))).andReturn(rs);
		
		if (numberOfMatches > 0) {
			expect(rs.next()).andReturn(true).times(1, numberOfMatches);
		} else {
			expect(rs.next()).andReturn(false);
		}
		
		rs.close();
		expectLastCall();
		st.close();
		expectLastCall();
	}
	
	private void expectMatchingUserOfEmailQuery(ResultSet rs, Integer id, String email, String domain, String domainAlias) throws Exception {
		expect(rs.getInt(1)).andReturn(id);
		expect(rs.getString(2)).andReturn(email);
		expect(rs.getString(3)).andReturn(domain);
		expect(rs.getString(4)).andReturn(domainAlias);
	}
	
	@Test
	public void testObmUserFromResultSet() throws Exception {
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(5);
		expect(rs.getString("userobm_login")).andReturn("login");
		expect(rs.getString("userobm_ext_id")).andReturn("extid");
		expect(rs.getString(2)).andReturn("useremail" + UserDao.DB_INNER_FIELD_SEPARATOR + "useremail2");
		expect(rs.getString(5)).andReturn("yes");
		expect(rs.getString(6)).andReturn(null);
		expect(rs.wasNull()).andReturn(true);
		expect(rs.getString("userobm_firstname")).andReturn("firstname2");
		expect(rs.getString("userobm_lastname")).andReturn("lastname2");
		expect(rs.getString("userobm_commonname")).andReturn("commonname");
		expect(rs.getInt("userentity_entity_id")).andReturn(6);
		
		ObmDomain domain = ObmDomain.builder().id(1).name("obm.org").build();

		mocksControl.replay();
		ObmUser obmUser = userDao.createUserFromResultSet(domain, rs);
		mocksControl.verify();

		ObmUser expectedObmUser = ObmUser.builder()
			.uid(5)
			.entityId(6)
			.login("login")
			.domain(domain)
			.emailAndAliases(Joiner.on(ObmUser.EMAIL_FIELD_SEPARATOR).join("useremail", "useremail2"))
			.firstName("firstname2")
			.lastName("lastname2")
			.commonName("commonname")
			.extId(UserExtId.builder().extId("extid").build())
			.publicFreeBusy(true)
			.build();
		
		assertThat(obmUser).isEqualsToByComparingFields(expectedObmUser);
	}
}
