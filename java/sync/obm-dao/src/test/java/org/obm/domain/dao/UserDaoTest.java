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
package org.obm.domain.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.sync.base.DomainName;
import org.obm.sync.base.EmailLogin;
import org.obm.sync.dao.EntityId;
import org.obm.sync.date.DateProvider;
import org.obm.sync.host.ObmHost;
import org.obm.utils.ObmHelper;

import com.google.common.base.Joiner;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserAddress;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserNomad;
import fr.aliacom.obm.common.user.UserPassword;


@GuiceModule(UserDaoTest.Env.class)
@RunWith(GuiceRunner.class)
public class UserDaoTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
			bindWithMock(ObmInfoDao.class);
			bindWithMock(AddressBookDao.class);
			bindWithMock(UserPatternDao.class);
			bindWithMock(GroupDao.class);
			bindWithMock(ProfileDao.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Inject
	private IMocksControl mocksControl;
	
	@Inject
	private ObmHelper obmHelper;
	@Inject
	private ObmInfoDao obmInfoDao;
	@Inject
	private AddressBookDao addressBookDao;
	@Inject
	private UserPatternDao userPatternDao;
	@Inject
	private GroupDao groupDao;
	@Inject
	private ProfileDao profileDao;

	private UserDaoJdbcImpl userDao;

	@Before
	public void setUp() {
		userDao = createMockBuilder(UserDaoJdbcImpl.class)
					.withConstructor(ObmHelper.class, ObmInfoDao.class, AddressBookDao.class, UserPatternDao.class, GroupDao.class, ProfileDao.class)
					.withArgs(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao)
					.addMockedMethod("userIdFromEmailQuery")
					.addMockedMethod("userIdFromLogin")
					.addMockedMethod("findUserById", Integer.TYPE, ObmDomain.class, Boolean.TYPE)
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
		UserDaoJdbcImpl dao = new UserDaoJdbcImpl(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao);
		
		expectEmailQueryCalls(connection, rs, 1);
		expectMatchingUserOfEmailQuery(rs, 1, login.get(), domain, null);
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, login, new DomainName(domain))).isEqualTo(1);
	}

	@Test
	public void testUserIdFromEmailQueryWithEmptyAlias() throws Exception {
		EmailLogin login = new EmailLogin("usera");
		String domain = "test.com";
		String alias = "alias.com";
		
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDaoJdbcImpl dao = new UserDaoJdbcImpl(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao);
		
		expectEmailQueryCalls(connection, rs, 1);
		expectMatchingUserOfEmailQuery(rs, 1, login.get(), domain, Joiner.on("\r\n").join(new String[]{"", alias}).toString());
		mocksControl.replay();
		
		assertThat(dao.userIdFromEmailQuery(connection, login, new DomainName(alias))).isEqualTo(1);
	}

	
	@Test
	public void testUserIdFromEmailQueryLoginDifferentCase() throws Exception {
		String loginFromDb = "userA";
		EmailLogin loginFromEvent = new EmailLogin("usera");
		String domain = "obm.com";
		Connection connection = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		UserDaoJdbcImpl dao = new UserDaoJdbcImpl(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao);
		
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
		UserDaoJdbcImpl dao = new UserDaoJdbcImpl(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao);
		
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
		UserDaoJdbcImpl dao = new UserDaoJdbcImpl(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao);
		
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
		UserDaoJdbcImpl dao = new UserDaoJdbcImpl(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao);
		
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
		UserDaoJdbcImpl dao = new UserDaoJdbcImpl(obmHelper, obmInfoDao, addressBookDao, userPatternDao, groupDao, profileDao);
		
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
		String profileName = "admin";
		ObmDomain domain = ObmDomain.builder().id(1).name("obm.org").build();
		expect(userDao.findUserById(0, domain, false)).andReturn(null).atLeastOnce();

		expect(profileDao.isAdminProfile(profileName)).andReturn(true);
		
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		expect(rs.getInt("userobm_id")).andReturn(5);
		expect(rs.getString("userobm_login")).andReturn("login");
		expect(rs.getString("userobm_ext_id")).andReturn("extid");
		expect(rs.getString("userobm_perms")).andReturn(profileName);
		expect(rs.getString("userobm_email")).andReturn("useremail\r\nuseremail2");
		expect(rs.getString("defpref_userobmpref_value")).andReturn("yes");
		expect(rs.getString("userpref_userobmpref_value")).andReturn(null);
		expect(rs.wasNull()).andReturn(true);
		expect(rs.getString("userobm_firstname")).andReturn("firstname2");
		expect(rs.getString("userobm_lastname")).andReturn("lastname2");
		expect(rs.getString("userobm_commonname")).andReturn("commonname");
		expect(rs.getInt("userentity_entity_id")).andReturn(6);
		expect(rs.getString("userobm_password")).andReturn("password");
		expect(rs.getString("userobm_perms")).andReturn("user");
		expect(rs.getString("userobm_kind")).andReturn(null);
		expect(rs.getString("userobm_title")).andReturn(null);
		expect(rs.getString("userobm_description")).andReturn(null);
		expect(rs.getString("userobm_company")).andReturn(null);
		expect(rs.getString("userobm_service")).andReturn(null);
		expect(rs.getString("userobm_direction")).andReturn(null);
		expect(rs.getString("userobm_address1")).andReturn(null);
		expect(rs.getString("userobm_address2")).andReturn(null);
		expect(rs.getString("userobm_address3")).andReturn(null);
		expect(rs.getString("userobm_phone")).andReturn(null);
		expect(rs.getString("userobm_phone2")).andReturn(null);
		expect(rs.getString("userobm_mobile")).andReturn(null);
		expect(rs.getString("userobm_fax")).andReturn(null);
		expect(rs.getString("userobm_fax2")).andReturn(null);
		expect(rs.getString("userobm_town")).andReturn(null);
		expect(rs.getString("userobm_zipcode")).andReturn(null);
		expect(rs.getString("userobm_expresspostal")).andReturn(null);
		expect(rs.getString("userobm_country_iso3166")).andReturn("0");
		expect(rs.getInt("userobm_mail_quota")).andReturn(100);
		expect(rs.getInt("userobm_hidden")).andReturn(1);
		expect(rs.getInt("userobm_uid")).andReturn(1001);
		expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("userobm_gid")).andReturn(1000);
		expect(rs.wasNull()).andReturn(false);
		expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("userobm_mail_server_id")).andReturn(1);
		expect(rs.getBoolean("userobm_archive")).andReturn(false);
		expect(rs.getString("host_name")).andReturn("host");
		expect(rs.getString("host_ip")).andReturn("ip");
		expect(rs.getString("host_fqdn")).andReturn("fqdn");
		expect(rs.getInt("host_domain_id")).andReturn(1);
		expect(rs.getTimestamp("userobm_timecreate")).andReturn(null);
		expect(rs.getTimestamp("userobm_timeupdate")).andReturn(null);
		expect(rs.getInt("userobm_userupdate")).andReturn(0);
		expect(rs.getInt("userobm_usercreate")).andReturn(0);
		expect(rs.getTimestamp("userobm_account_dateexp")).andReturn(null);
		expect(rs.getString("userobm_delegation")).andReturn(null);
		expect(rs.getString("userobm_delegation_target")).andReturn(null);
		expect(rs.getBoolean("userobm_samba_perms")).andReturn(true);
		expect(rs.getString("userobm_samba_home_drive")).andReturn("ab");
		expect(rs.getString("userobm_samba_home")).andReturn("myfolder");
		expect(rs.getString("userobm_samba_logon_script")).andReturn("script");
		expect(rs.getInt("userobm_nomade_enable")).andReturn(0);
		expect(rs.getString("userobm_email_nomade")).andReturn("nomad_email");
		expect(rs.getInt("userobm_nomade_perms")).andReturn(0);
		expect(rs.getInt("userobm_nomade_local_copy")).andReturn(0);

		mocksControl.replay();
		ObmUser obmUser = userDao.createUserFromResultSetAndFetchCreators(domain, rs);
		mocksControl.verify();

		ObmUser expectedObmUser = ObmUser.builder()
			.uid(5)
			.entityId(EntityId.valueOf(6))
			.login(UserLogin.valueOf("login"))
			.admin(true)
			.domain(domain)
			.identity(UserIdentity.builder()
				.firstName("firstname2")
				.lastName("lastname2")
				.commonName("commonname")
				.build())
			.extId(UserExtId.builder().extId("extid").build())
			.publicFreeBusy(true)
			.password(UserPassword.valueOf("password"))
			.emails(UserEmails.builder()
				.quota(100)
				.server(ObmHost
						.builder()
						.id(1)
						.name("host")
						.ip("ip")
						.fqdn("fqdn")
						.domainId(domain.getId())
						.build())
				.addAddress("useremail")
				.addAddress( "useremail2")
				.domain(domain)
				.build())
			.profileName(ProfileName.builder().name("user").build())
			.address(UserAddress.builder().countryCode("0").build())
			.hidden(true)
			.nomad(UserNomad.builder().email("nomad_email").build())
			.uidNumber(1001)
			.gidNumber(1000)
			.sambaAllowed(true)
			.sambaHomeDrive("ab")
			.sambaHomeFolder("myfolder")
			.sambaLogonScript("script")
			.build();
		
		assertThat(obmUser).isEqualToComparingFieldByField(expectedObmUser);
	}
}
