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

import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.GroupDaoJdbcImpl;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.sync.dao.EntityId;
import org.obm.sync.host.ObmHost;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.ObmUser.Builder;
import fr.aliacom.obm.common.user.UserExtId;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(UserDaoJdbcImplTest.Env.class)
public class UserDaoJdbcImplTest implements H2TestClass {

	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}
	
	public static class Env extends DaoTestModule {

		@Override
		protected void configureImpl() {
			bind(DomainDao.class);
			bind(ObmInfoDao.class).to(ObmInfoDaoJdbcImpl.class);
			bind(AddressBookDao.class).to(AddressBookDaoJdbcImpl.class);
			bind(UserPatternDao.class).to(UserPatternDaoJdbcImpl.class);
			bind(UserDao.class).to(UserDaoJdbcImpl.class);
			bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
		}

	}

	@Inject
	private UserDaoJdbcImpl dao;

	private final ObmDomain domain = ObmDomain
			.builder()
			.id(1)
			.uuid(ObmDomainUuid.of("3af47236-3638-458e-9c3e-5eebbaa8f9ae"))
			.name("domain")
			.build();
	private final ObmDomain domain2 = ObmDomain
			.builder()
			.id(2)
			.uuid(ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf"))
			.name("test2.tlse.lng")
			.build();
	private final ObmHost mailHost = ObmHost
			.builder()
			.id(1)
			.name("mail")
			.fqdn("mail.tlse.lng")
			.ip("1.2.3.4")
			.domainId(domain.getId())
			.build();

	@Test
	public void testList() throws Exception {
		List<ObmUser> users = ImmutableList.of(
				sampleUser(1, 3, "1"),
				sampleUser(2, 4, "2"),
				sampleUser(3, 5, "3"),
				sampleUserWithoutMail(4, 6, "4"));

		assertThat(dao.list(domain)).isEqualTo(users);
	}

	@Test
	public void testFindUserById() {
		ObmUser user = sampleUser(1, 3, "1");

		assertThat(dao.findUserById(1, domain)).isEqualTo(user);
	}

	@Test
	public void testGetByExtIdFetchesOneCreatorLevelOnly() throws Exception {
		ObmUser creator = dao.findUserById(1, domain);
		UserExtId extId = UserExtId.valueOf("testExtId");
		ObmUser user = ObmUser
				.builder()
				.login("login")
				.lastName("lastname")
				.domain(domain)
				.extId(extId)
				.createdBy(creator)
				.build();

		dao.create(user);

		ObmUser foundUser = dao.getByExtId(extId, domain);

		assertThat(foundUser.getCreatedBy().getUpdatedBy()).isNull();
		assertThat(foundUser.getCreatedBy().getCreatedBy()).isNull();
	}

	@Test
	public void testGetByExtIdFetchesOneUpdatorLevelOnly() throws Exception {
		ObmUser creator = dao.findUserById(1, domain);
		UserExtId extId = UserExtId.valueOf("testExtId");
		Builder userBuilder = ObmUser
				.builder()
				.login("login")
				.lastName("lastname")
				.domain(domain)
				.extId(extId)
				.createdBy(creator);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.update(userBuilder
				.uid(createdUser.getUid())
				.updatedBy(creator)
				.build());

		ObmUser foundUser = dao.getByExtId(extId, domain);

		assertThat(foundUser.getUpdatedBy().getUpdatedBy()).isNull();
		assertThat(foundUser.getUpdatedBy().getCreatedBy()).isNull();
	}

	@Test
	public void testGetByExtIdWithGroups() throws Exception {
		ObmUser creator = dao.findUserById(1, domain);
		UserExtId extId = UserExtId.valueOf("testExtId");
		Builder userBuilder = ObmUser
				.builder()
				.login("login")
				.lastName("lastname")
				.domain(domain)
				.extId(extId)
				.createdBy(creator);

		dao.create(userBuilder.build());

		ObmUser foundUser = dao.getByExtIdWithGroups(extId, domain);

		assertThat(foundUser.getGroups()).isNotNull();
	}

	@Test
	public void testFindUserByIdFetchesOneCreatorLevelOnly() throws Exception {
		ObmUser creator = dao.findUserById(1, domain);
		ObmUser user = ObmUser
				.builder()
				.login("login")
				.lastName("lastname")
				.domain(domain)
				.extId(UserExtId.valueOf("testExtId"))
				.createdBy(creator)
				.build();

		ObmUser createdUser = dao.create(user);
		ObmUser foundUser = dao.findUserById(createdUser.getUid(), domain);

		assertThat(foundUser.getCreatedBy().getUpdatedBy()).isNull();
		assertThat(foundUser.getCreatedBy().getCreatedBy()).isNull();
	}

	@Test
	public void testFindUserByIdFetchesOneUpdatorLevelOnly() throws Exception {
		ObmUser creator = dao.findUserById(1, domain);
		Builder userBuilder = ObmUser
				.builder()
				.login("login")
				.lastName("lastname")
				.domain(domain)
				.extId(UserExtId.valueOf("testExtId"))
				.createdBy(creator);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.update(userBuilder
				.uid(createdUser.getUid())
				.updatedBy(creator)
				.build());

		ObmUser foundUser = dao.findUserById(createdUser.getUid(), domain);

		assertThat(foundUser.getUpdatedBy().getUpdatedBy()).isNull();
		assertThat(foundUser.getUpdatedBy().getCreatedBy()).isNull();
	}

	@Test
	public void testFindUserByIdWhenUserHasNoMail() {
		ObmUser user = sampleUserWithoutMail(4, 6, "4");

		assertThat(dao.findUserById(4, domain)).isEqualTo(user);
	}

	@Test
	public void testCreateSimpleUserWithNoMail() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.password("secure")
				.profileName(ProfileName.valueOf("user"))
				.lastName("Doe")
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		assertThat(createdUser.getUid()).isGreaterThan(0);
		assertThat(createdUser.getEntityId().getId()).isGreaterThan(0);
		assertThat(userBuilder
				.uid(createdUser.getUid())
				.entityId(createdUser.getEntityId())
				.uidNumber(UserDao.FIRST_UID)
				.gidNumber(UserDao.DEFAULT_GID)
				.build()).isEqualTo(createdUser);
	}

	@Test
	public void testCreateMultipleUsersIncrementsUid() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.login("jdoe")
				.password("secure")
				.profileName(ProfileName.valueOf("user"))
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.address1("1 OBM Street")
				.address2("2 OBM Street")
				.address3("3 OBM Street")
				.town("OBMCity")
				.countryCode("OB")
				.zipCode("OBMZip")
				.expresspostal("OBMExpressPostal")
				.phone("+OBM 123456")
				.phone2("+OBM 789")
				.mobile("+OBMMobile 123")
				.fax("+OBMFax 123456")
				.fax2("+OBMFax 789")
				.company("Linagora")
				.service("OBMDev")
				.direction("LGS")
				.title("Software Dev")
				.emailAndAliases("jdoe\r\njohn.doe")
				.kind("Mr")
				.mailHost(mailHost)
				.mailQuota(500)
				.domain(domain);

		ObmUser createdUser1 = dao.create(userBuilder.extId(UserExtId.valueOf("JohnDoeExtI1")).build());
		ObmUser createdUser2 = dao.create(userBuilder.extId(UserExtId.valueOf("JohnDoeExtI2")).build());
		ObmUser createdUser3 = dao.create(userBuilder.extId(UserExtId.valueOf("JohnDoeExtI3")).build());

		assertThat(createdUser1.getUidNumber()).isEqualTo(UserDao.FIRST_UID);
		assertThat(createdUser2.getUidNumber()).isEqualTo(UserDao.FIRST_UID + 1);
		assertThat(createdUser3.getUidNumber()).isEqualTo(UserDao.FIRST_UID + 2);
	}

	@Test
	public void testCreate() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.password("secure")
				.profileName(ProfileName.valueOf("user"))
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.address1("1 OBM Street")
				.address2("2 OBM Street")
				.address3("3 OBM Street")
				.town("OBMCity")
				.countryCode("OB")
				.zipCode("OBMZip")
				.expresspostal("OBMExpressPostal")
				.phone("+OBM 123456")
				.phone2("+OBM 789")
				.mobile("+OBMMobile 123")
				.fax("+OBMFax 123456")
				.fax2("+OBMFax 789")
				.company("Linagora")
				.service("OBMDev")
				.direction("LGS")
				.title("Software Dev")
				.emailAndAliases("jdoe\r\njohn.doe")
				.kind("Mr")
				.mailHost(mailHost)
				.mailQuota(500)
				.hidden(true)
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		assertThat(createdUser.getUid()).isGreaterThan(0);
		assertThat(createdUser.getEntityId().getId()).isGreaterThan(0);
		assertThat(userBuilder
				.uid(createdUser.getUid())
				.entityId(createdUser.getEntityId())
				.uidNumber(UserDao.FIRST_UID)
				.gidNumber(UserDao.DEFAULT_GID)
				.build()).isEqualTo(createdUser);
	}

	@Test
	public void testCreateInsertsAddressBooks() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.password("secure")
				.profileName(ProfileName.valueOf("user"))
				.lastName("Doe")
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		ResultSet rs = db.execute("SELECT is_default FROM AddressBook WHERE owner = ? AND name = 'contacts'", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getBoolean("is_default")).isTrue();
		rs.close();

		rs = db.execute("SELECT is_default FROM AddressBook WHERE owner = ? AND name = 'collected_contacts'", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getBoolean("is_default")).isTrue();
		rs.close();
	}

	@Test
	public void testGetAfterCreate() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.password("secure")
				.profileName(ProfileName.valueOf("user"))
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.address1("1 OBM Street")
				.address2("2 OBM Street")
				.address3("3 OBM Street")
				.town("OBMCity")
				.countryCode("OB")
				.zipCode("OBMZip")
				.expresspostal("OBMExpressPostal")
				.phone("+OBM 123456")
				.phone2("+OBM 789")
				.mobile("+OBMMobile 123")
				.fax("+OBMFax 123456")
				.fax2("+OBMFax 789")
				.company("Linagora")
				.service("OBMDev")
				.direction("LGS")
				.title("Software Dev")
				.emailAndAliases("jdoe\r\njohn.doe")
				.kind("Mr")
				.mailHost(mailHost)
				.mailQuota(500)
				.uidNumber(123)
				.gidNumber(456)
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		assertThat(dao.findUserById(createdUser.getUid(), domain)).isEqualTo(userBuilder
				.uid(createdUser.getUid())
				.entityId(createdUser.getEntityId())
				.build());
	}

	@Test(expected = UserNotFoundException.class)
	public void testUpdateWhenUserDoestExist() throws SQLException, UserNotFoundException {
		ObmUser user = ObmUser
				.builder()
				.uid(666)
				.login("lucifer")
				.domain(domain)
				.build();

		dao.update(user);
	}

	@Test
	public void testUpdate() throws SQLException, UserNotFoundException {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.firstName("John")
				.lastName("Doe")
				.hidden(true)
				.build();

		assertThat(dao.update(user)).isEqualTo(user);
	}

	@Test
	public void testUpdateClearingEmailingForUser() throws SQLException, UserNotFoundException {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.emailAndAliases("")
				.mailHost(null)
				.profileName(ProfileName.valueOf("admin"))
				.build();

		assertThat(dao.update(user)).isEqualTo(user);
	}

	@Test
	public void testGetAfterUpdate() throws SQLException, UserNotFoundException {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.firstName("John")
				.lastName("Doe")
				.build();

		dao.update(user);

		assertThat(dao.findUserById(1, domain)).isEqualTo(user);
	}

	@Test
	public void testGetAfterDelete() throws Exception {
		dao.delete(sampleUser(3, 3, "3"));

		assertThat(dao.findUserById(3, domain)).isNull();
	}

	@Test
	public void testListAfterDelete() throws Exception {
		dao.delete(sampleUser(1, 3, "1"));

		List<ObmUser> users = ImmutableList.of(
				sampleUser(2, 4, "2"),
				sampleUser(3, 5, "3"),
				sampleUserWithoutMail(4, 6, "4"));

		assertThat(dao.list(domain)).isEqualTo(users);
	}

	@Test(expected = UserNotFoundException.class)
	public void testDeleteWhenUserDoesntExist() throws Exception {
		ObmUser user = ObmUser.builder()
						.extId(UserExtId.valueOf("666"))
						.login("lucifer")
						.domain(domain)
						.build();
		dao.delete(user);
	}

	@Test
	public void testCreateInsertsEmtpyStringsForNamePartsIfNull() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.password("secure")
				.profileName(ProfileName.valueOf("user"))
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());
		ResultSet rs = db.execute("SELECT userobm_lastname, userobm_firstname, userobm_commonname FROM UserObm WHERE userobm_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getString("userobm_lastname")).isEmpty();
		assertThat(rs.getString("userobm_firstname")).isEmpty();
		assertThat(rs.getString("userobm_commonname")).isEmpty();
	}

	@Test
	public void testUpdateInsertsEmtpyStringsForNamePartsIfNull() throws Exception {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.firstName(null)
				.lastName(null)
				.commonName(null)
				.build();

		ObmUser updatedUser = dao.update(user);
		ResultSet rs = db.execute("SELECT userobm_lastname, userobm_firstname, userobm_commonname FROM UserObm WHERE userobm_id = ?", updatedUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getString("userobm_lastname")).isEmpty();
		assertThat(rs.getString("userobm_firstname")).isEmpty();
		assertThat(rs.getString("userobm_commonname")).isEmpty();
	}

	@Test
	public void testCreateUpdatesUserIndex() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.extId(UserExtId.valueOf("extIdJDoe"))
				.login("jdoe")
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.domain(domain)
				.build();

		ObmUser createdUser = dao.create(user);

		Set<String> patterns = Sets.newHashSet();
		Set<String> expectedPatterns = ImmutableSet.of("jdoe", "John", "Doe");
		ResultSet rs = db.execute("SELECT pattern FROM _userpattern WHERE id = ?", createdUser.getUid());

		while (rs.next()) {
			patterns.add(rs.getString(1));
		}

		assertThat(patterns).isEqualTo(expectedPatterns);
	}

	@Test
	public void testCreateInsertsCalendarEntity() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.extId(UserExtId.valueOf("extIdJDoe"))
				.login("jdoe")
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.domain(domain)
				.build();

		ObmUser createdUser = dao.create(user);
		ResultSet rs = db.execute("SELECT COUNT(*) FROM CalendarEntity WHERE calendarentity_calendar_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test
	public void testCreateInsertsMailboxEntity() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.extId(UserExtId.valueOf("extIdJDoe"))
				.login("jdoe")
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.domain(domain)
				.build();

		ObmUser createdUser = dao.create(user);
		ResultSet rs = db.execute("SELECT COUNT(*) FROM MailboxEntity WHERE mailboxentity_mailbox_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test
	public void testCreateUserWithNoMailWrites0AsMailPerms() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());
		ResultSet rs = db.execute("SELECT userobm_mail_perms FROM UserObm WHERE userobm_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getInt(1)).isEqualTo(0);
	}

	@Test
	public void testUpdateUserWithNoMailWrites0AsMailPerms() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.email("jdoe")
				.mailHost(mailHost)
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.update(userBuilder
				.uid(createdUser.getUid())
				.mailHost(null)
				.email(null)
				.build());

		ResultSet rs = db.execute("SELECT userobm_mail_perms FROM UserObm WHERE userobm_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getInt(1)).isEqualTo(0);
	}

	@Test
	public void testCreateUserWithMailWrites1AsMailPerms() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.email("jdoe")
				.mailHost(mailHost)
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());
		ResultSet rs = db.execute("SELECT userobm_mail_perms FROM UserObm WHERE userobm_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test
	public void testUpdateUserWithMailWrites1AsMailPerms() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.update(userBuilder
				.uid(createdUser.getUid())
				.mailHost(mailHost)
				.email("jdoe")
				.build());

		ResultSet rs = db.execute("SELECT userobm_mail_perms FROM UserObm WHERE userobm_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test
	public void testGetArchivedUser() {
		ObmUser user = sampleUserBuilder(7, 32, "7")
				.archived(true)
				.domain(domain2)
				.build();

		assertThat(dao.findUserById(7, domain2)).isEqualTo(user);
	}

	@Test
	public void testGetArchivedUserByExtId() throws Exception {
		ObmUser user = sampleUserBuilder(7, 32, "7")
				.archived(true)
				.domain(domain2)
				.build();

		assertThat(dao.getByExtId(UserExtId.valueOf("7"), domain2)).isEqualTo(user);
	}

	@Test
	public void testGetArchivedUserByExtIdWithGroups() throws Exception {
		ObmUser user = sampleUserBuilder(7, 32, "7")
				.archived(true)
				.domain(domain2)
				.build();

		assertThat(dao.getByExtIdWithGroups(UserExtId.valueOf("7"), domain2)).isEqualTo(user);
	}

	@Test
	public void testListAlsoListsArchivedUsers() throws Exception {
		List<ObmUser> users = ImmutableList.of(
				sampleUserBuilder(1, 7, "5")
					.uid(5)
					.mailHost(mailHost)
					.emailAndAliases("user1")
					.domain(domain2)
					.build(),
				sampleUserBuilder(2, 8, "6")
					.uid(6)
					.profileName(ProfileName.valueOf("admin"))
					.mailHost(mailHost)
					.emailAndAliases("user2")
					.domain(domain2)
					.build(),
				sampleUserBuilder(7, 32, "7")
					.archived(true)
					.domain(domain2)
					.build());

		assertThat(dao.list(domain2)).isEqualTo(users);
	}

	public void testArchiveUser() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login("jdoe")
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.archive(createdUser);

		ResultSet rs = db.execute("SELECT userobm_archive FROM UserObm WHERE userobm_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getBoolean(1)).isTrue();
	}


	private ObmUser.Builder sampleUserBuilder(int id, int entityId, String extId) {
		return ObmUser
				.builder()
				.login("user" + id)
				.uid(id)
				.entityId(EntityId.valueOf(entityId))
				.lastName("Lastname")
				.firstName("Firstname")
				.domain(domain)
				.profileName(ProfileName.valueOf("user"))
				.password("user" + id)
				.uidNumber(1000)
				.gidNumber(512)
				.countryCode("0")
				.extId(UserExtId.valueOf(extId));
	}
	private ObmUser sampleUser(int id, int entityId, String extId) {
		return sampleUserBuilder(id, entityId, extId)
				.mailHost(mailHost)
				.emailAndAliases("user" + id)
				.build();
	}

	private ObmUser sampleUserWithoutMail(int id, int entityId, String extId) {
		return sampleUserBuilder(id, entityId, extId)
				.emailAndAliases("")
				.build();
	}
}
