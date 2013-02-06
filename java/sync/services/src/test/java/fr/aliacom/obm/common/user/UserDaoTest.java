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
package fr.aliacom.obm.common.user;

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
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.sync.date.DateProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.utils.ObmHelper;

public class UserDaoTest {

	private static class Env extends AbstractModule {
		private IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(Env.class);

	@Inject
	private IMocksControl mocksControl;
	@Inject
	private ObmHelper obmHelper;

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
		Connection connection = mocksControl.createMock(Connection.class);

		expect(userDao.userIdFromEmailQuery(connection, "usera", "obm.com")).andReturn(userId);

		mocksControl.replay();

		assertThat(userDao.userIdFromEmail(connection, email, 1)).isEqualTo(userId);
	}

	@Test
	public void testUserIdFromEmailUsesLoginQueryIfLoginGiven() throws Exception {
		String email = "usera";
		Integer userId = 1, domainId = 1;
		Connection connection = mocksControl.createMock(Connection.class);

		expect(userDao.userIdFromLogin(connection, "usera", domainId)).andReturn(userId);

		mocksControl.replay();

		assertThat(userDao.userIdFromEmail(connection, email, domainId)).isEqualTo(userId);
	}

	@Test
	public void testUserIdFromEmailUsesLoginQueryThenEmailQueryIfLoginGiven() throws Exception {
		String email = "usera";
		Integer userId = 1, domainId = 1;
		Connection connection = mocksControl.createMock(Connection.class);
		
		expect(userDao.userIdFromLogin(connection, "usera", domainId)).andReturn(null); // Login fetch returns no result
		expect(userDao.userIdFromEmailQuery(connection, "usera", "-")).andReturn(userId);

		mocksControl.replay();

		assertThat(userDao.userIdFromEmail(connection, email, 1)).isEqualTo(userId);
	}
	
	@Test
	public void testUserIdFromEmailQuery() throws Exception {
		String email = "usera";
		String domain = "test.com";
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 1);
		expectMatchingUserOfEmailQuery(rs, 1, email, domain, null);
		
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, email, domain)).isEqualTo(1);
	}
	
	@Test
	public void testUserIdFromEmailQueryNoResult() throws Exception {
		String email = "usera";
		String domain = "test.com";
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDao dao = new UserDao(obmHelper);
		
		expectEmailQueryCalls(connection, rs, 0);
		
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, email, domain)).isNull();
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
		
		assertThat(dao.userIdFromEmailQuery(connection, email, domain)).isEqualTo(4);
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

}
