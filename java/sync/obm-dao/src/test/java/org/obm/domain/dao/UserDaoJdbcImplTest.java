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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.GroupDaoJdbcImpl;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.ProfileDaoJdbcImpl;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.sync.dao.EntityId;
import org.obm.sync.host.ObmHost;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.ObmUser.Builder;
import fr.aliacom.obm.common.user.UserAddress;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserPassword;
import fr.aliacom.obm.common.user.UserPhones;
import fr.aliacom.obm.common.user.UserWork;

@RunWith(GuiceRunner.class)
@GuiceModule(UserDaoJdbcImplTest.Env.class)
public class UserDaoJdbcImplTest implements H2TestClass {

	private final UserLogin validLogin = UserLogin.valueOf("login");
	private final UserIdentity validIdentity = UserIdentity.builder().lastName("lastname").build();
	private final UserIdentity johnIdentity = UserIdentity.builder()
			.kind("Mr")
			.lastName("Doe")
			.firstName("John")
			.commonName("J. Doe")
			.build();
	private final UserAddress johnAddress = UserAddress.builder()
			.addressPart("1 OBM Street")
			.addressPart("2 OBM Street")
			.addressPart("3 OBM Street")
			.town("OBMCity")
			.countryCode("OB")
			.zipCode("OBMZip")
			.expressPostal("OBMExpressPostal")
			.build();
	
	private final UserPhones validPhones = UserPhones.builder()
			.addPhone("+OBM 123456")
			.addPhone("+OBM 789")
			.mobile("+OBMMobile 123")
			.addFax("+OBMFax 123456")
			.addFax("+OBMFax 789")
			.build();

	private final UserWork validJob = UserWork.builder()
			.company("Linagora")
			.service("OBMDev")
			.direction("LGS")
			.title("Software Dev")
			.build();

	private final ObmDomain domain = ObmDomain
			.builder()
			.id(1)
			.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
			.name("domain")
			.build();
	
	private final ObmHost mailHost = ObmHost
			.builder()
			.id(1)
			.name("mail")
			.fqdn("mail.tlse.lng")
			.ip("1.2.3.4")
			.domainId(domain.getId())
			.build();
	
	private final UserEmails johnEmails = UserEmails.builder()
			.addAddress("jdoe")
			.addAddress("john.doe")
			.server(mailHost)
			.quota(500)
			.domain(domain)
			.build();
	
	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}
	
	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			install(new DaoTestModule());
			bind(DomainDao.class);
			bind(ObmInfoDao.class).to(ObmInfoDaoJdbcImpl.class);
			bind(AddressBookDao.class).to(AddressBookDaoJdbcImpl.class);
			bind(UserPatternDao.class).to(UserPatternDaoJdbcImpl.class);
			bind(UserDao.class).to(UserDaoJdbcImpl.class);
			bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
			bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		}

	}

	@Inject
	private UserDaoJdbcImpl dao;

	private final ObmDomain domain2 = ObmDomain
			.builder()
			.id(2)
			.uuid(ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf"))
			.name("test2.tlse.lng")
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
				.login(validLogin)
				.identity(validIdentity)
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
				.login(validLogin)
				.identity(validIdentity)
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
				.login(validLogin)
				.identity(validIdentity)
				.domain(domain)
				.extId(extId)
				.createdBy(creator);

		dao.create(userBuilder.build());

		ObmUser foundUser = dao.getByExtIdWithGroups(extId, domain);

		assertThat(foundUser.getGroups()).isNotNull();
	}

	@Test
	public void testGetByExtIdShouldReadDelegationTarget() throws Exception {
		UserExtId extId = UserExtId.valueOf("testExtId");
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(extId)
				.login(UserLogin.valueOf("haveadelegationtarget"))
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.delegationTarget("delegationTarget");

		dao.create(userBuilder.build());

		assertThat(dao.getByExtId(extId, domain).getDelegationTarget()).isEqualTo("delegationTarget");
	}

	@Test
	public void testFindUserByIdFetchesOneCreatorLevelOnly() throws Exception {
		ObmUser creator = dao.findUserById(1, domain);
		ObmUser user = ObmUser
				.builder()
				.login(validLogin)
				.identity(validIdentity)
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
	public void testFindUserByIdShouldReadDelegationTarget() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.login(validLogin)
				.identity(validIdentity)
				.domain(domain)
				.extId(UserExtId.valueOf("testExtId"))
				.delegationTarget("delegationTarget")
				.build();

		ObmUser createdUser = dao.create(user);
		ObmUser foundUser = dao.findUserById(createdUser.getUid(), domain);

		assertThat(foundUser.getDelegationTarget()).isEqualTo("delegationTarget");
	}

	@Test
	public void testFindUserByIdFetchesOneUpdatorLevelOnly() throws Exception {
		ObmUser creator = dao.findUserById(1, domain);
		Builder userBuilder = ObmUser
				.builder()
				.login(validLogin)
				.identity(validIdentity)
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
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(validIdentity)
				.domain(domain)
				.publicFreeBusy(true);

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
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.address(johnAddress)
				.phones(validPhones)
				.work(validJob)
				.emails(johnEmails)
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
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.address(johnAddress)
				.phones(validPhones)
				.work(validJob)
				.emails(johnEmails)
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
				.publicFreeBusy(true)
				.build()).isEqualTo(createdUser);
	}

	@Test
	public void testCreateInsertsAddressBooks() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(validIdentity)
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
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.address(johnAddress)
				.phones(validPhones)
				.work(validJob)
				.emails(johnEmails)
				.uidNumber(123)
				.gidNumber(456)
				.domain(domain)
				.publicFreeBusy(true);

		ObmUser createdUser = dao.create(userBuilder.build());

		assertThat(dao.findUserById(createdUser.getUid(), domain)).isEqualTo(userBuilder
				.uid(createdUser.getUid())
				.entityId(createdUser.getEntityId())
				.build());
	}

	@Test
	public void testCreateShouldWriteArchivedFlag() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("123456"))
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.archived(true);

		ObmUser createdUser = dao.create(userBuilder.build());

		assertThat(createdUser.isArchived()).isTrue();
	}
	
	@Test
	public void testFindByLoginShouldReadExpirationDate() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("123456"))
				.login(UserLogin.valueOf("iwillexpiresoon"))
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.expirationDate(DateUtils.date("2015-12-31"));

		dao.create(userBuilder.build());

		assertThat(dao.findUserByLogin("iwillexpiresoon", domain).getExpirationDate()).isEqualTo(DateUtils.date("2015-12-31"));
	}
	
	@Test
	public void testCreateShouldWriteExpirationDate() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("123456"))
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.expirationDate(DateUtils.date("2015-12-31"));

		ObmUser createdUser = dao.create(userBuilder.build());
		
		assertThat(createdUser.getExpirationDate()).isEqualTo(DateUtils.date("2015-12-31"));
	}

	@Test
	public void testUpdateShouldWriteExpirationDate() throws Exception {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.expirationDate(DateUtils.date("2015-12-31"))
				.build();

		assertThat(dao.update(user)).isEqualTo(user);
	}

	@Test
	public void testFindByLoginShouldReadDelegation() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("123456"))
				.login(UserLogin.valueOf("haveadelegation"))
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.delegation("delegation");

		dao.create(userBuilder.build());

		assertThat(dao.findUserByLogin("haveadelegation", domain).getDelegation()).isEqualTo("delegation");
	}

	@Test
	public void testFindByLoginShouldReadDelegationTarget() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("123456"))
				.login(UserLogin.valueOf("haveadelegationtarget"))
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.delegationTarget("delegationTarget");

		dao.create(userBuilder.build());

		assertThat(dao.findUserByLogin("haveadelegationtarget", domain).getDelegationTarget()).isEqualTo("delegationTarget");
	}

	@Test
	public void testCreateShouldWriteDelegation() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("123456"))
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.delegation("delegation");

		ObmUser createdUser = dao.create(userBuilder.build());
		
		assertThat(createdUser.getDelegation()).isEqualTo("delegation");
	}

	@Test
	public void testCreateShouldWriteDelegationTarget() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.extId(UserExtId.valueOf("123456"))
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
				.profileName(ProfileName.valueOf("user"))
				.identity(johnIdentity)
				.domain(domain)
				.delegationTarget("delegationTarget")
				.build();

		assertThat(dao.create(user).getDelegationTarget()).isEqualTo(user.getDelegationTarget());
	}

	@Test
	public void testUpdateShouldWriteDelegation() throws Exception {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.delegation("delegation")
				.build();

		assertThat(dao.update(user)).isEqualTo(user);
	}

	@Test
	public void testUpdateShouldWriteDelegationTarget() throws Exception {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.delegationTarget("delegationTarget")
				.build();

		assertThat(dao.update(user)).isEqualTo(user);
	}

	@Test(expected = UserNotFoundException.class)
	public void testUpdateWhenUserDoestExist() throws SQLException, UserNotFoundException {
		ObmUser user = ObmUser
				.builder()
				.uid(666)
				.login(UserLogin.valueOf("lucifer"))
				.domain(domain)
				.build();

		dao.update(user);
	}

	@Test
	public void testUpdate() throws SQLException, UserNotFoundException {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.identity(johnIdentity)
				.hidden(true)
				.build();

		assertThat(dao.update(user)).isEqualTo(user);
	}

	@Test
	public void testUpdateClearingEmailingForUser() throws SQLException, UserNotFoundException {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.profileName(ProfileName.valueOf("admin"))
				.admin(true)
				.build();

		assertThat(dao.update(user)).isEqualTo(user);
	}

	@Test
	public void testUpdateShouldWriteArchiveFlag() throws SQLException, UserNotFoundException {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.identity(johnIdentity)
				.archived(true)
				.build();

		dao.update(user);

		assertThat(dao.findUserById(1, domain)).isEqualTo(user);
	}

	@Test
	public void testGetAfterUpdate() throws SQLException, UserNotFoundException {
		ObmUser user = sampleUserBuilder(1, 3, "1")
				.identity(johnIdentity)
				.publicFreeBusy(true)
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
						.login(UserLogin.valueOf("lucifer"))
						.domain(domain)
						.build();
		dao.delete(user);
	}

	@Test
	public void testCreateInsertsEmtpyStringsForNamePartsIfNull() throws Exception {
		ObmUser.Builder userBuilder = ObmUser
				.builder()
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login(validLogin)
				.password(UserPassword.valueOf("secure"))
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
				.identity(UserIdentity.empty())
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
				.login(UserLogin.valueOf("jdoe"))
				.identity(johnIdentity)
				.domain(domain)
				.build();

		ObmUser createdUser = dao.create(user);

		Set<String> patterns = Sets.newHashSet();
		Set<String> expectedPatterns = ImmutableSet.of("jdoe", "john", "doe");
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
				.login(validLogin)
				.identity(johnIdentity)
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
				.login(validLogin)
				.identity(johnIdentity)
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
				.login(validLogin)
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
				.login(validLogin)
				.emails(UserEmails.builder()
					.addAddress("jdoe")
					.server(mailHost)
					.domain(domain)
					.build())
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.update(userBuilder
				.uid(createdUser.getUid())
				.emails(UserEmails.builder().domain(domain).build())
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
				.login(validLogin)
				.emails(UserEmails.builder()
					.addAddress("jdoe")
					.server(mailHost)
					.domain(domain)
					.build())
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
				.login(validLogin)
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.update(userBuilder
				.uid(createdUser.getUid())
				.emails(UserEmails.builder()
					.addAddress("jdoe")
					.server(mailHost)
					.domain(domain)
					.build())
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
				.publicFreeBusy(true)
				.build();

		assertThat(dao.getByExtIdWithGroups(UserExtId.valueOf("7"), domain2)).isEqualTo(user);
	}

	@Test
	public void testListAlsoListsArchivedUsers() throws Exception {
		List<ObmUser> users = ImmutableList.of(
				sampleUserBuilder(1, 7, "5")
					.uid(5)
					.emails(UserEmails.builder()
						.addAddress("user1")
						.server(mailHost)
						.domain(domain2)
						.build())
					.domain(domain2)
					.build(),
				sampleUserBuilder(2, 8, "6")
					.uid(6)
					.profileName(ProfileName.valueOf("admin"))
					.admin(true)
					.emails(UserEmails.builder()
						.addAddress("user2")
						.server(mailHost)
						.domain(domain2)
						.build())
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
				.login(validLogin)
				.domain(domain);

		ObmUser createdUser = dao.create(userBuilder.build());

		dao.archive(createdUser);

		ResultSet rs = db.execute("SELECT userobm_archive FROM UserObm WHERE userobm_id = ?", createdUser.getUid());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getBoolean(1)).isTrue();
	}

	@Test
	public void testGetAllEmailsFrom() throws SQLException {
		ImmutableSet<String> allEmails = dao.getAllEmailsFrom(domain, UserExtId.valueOf("1"));
		
		assertThat(allEmails).containsOnly("group1", "group2", "mailshare1", "user2", "user3");
	}
	

	@Test
	public void testQuota0ToNullableIsNull() {
		assertThat(dao.quotaToNullable(0)).isNull();
	}

	@Test
	public void testQuota500ToNullableIsNotNull() {
		assertThat(dao.quotaToNullable(500)).isEqualTo(500);
	}
	
	@Test
	public void testNullToIntQuotaIs0() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("login"))
				.domain(domain)
				.emails(UserEmails.builder()
					.quota(null)
					.domain(domain)
					.build())
				.build();
		assertThat(dao.getQuotaAsInt0(user)).isEqualTo(0);
	}

	@Test
	public void test500ToIntQuotaIs500() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("login"))
				.domain(domain)
				.emails(UserEmails.builder()
					.quota(500)
					.domain(domain)
					.build())
				.build();
		assertThat(dao.getQuotaAsInt0(user)).isEqualTo(500);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetAllEmailsFromWithNullToIgnoreExtId() throws SQLException {
		dao.getAllEmailsFrom(domain, null);
	}

	@Test(expected = DomainNotFoundException.class)
	public void testGetUniqueObmDomainFailsWhenUserIsInSeveralDomains() throws Exception {
		dao.getUniqueObmDomain("user2");
	}

	@Test(expected = DomainNotFoundException.class)
	public void testGetUniqueObmDomainFailsWhenUserDoesntExist() throws Exception {
		dao.getUniqueObmDomain("iamnotaregistereduser!");
	}

	@Test
	public void testGetUniqueObmDomain() throws Exception {
		assertThat(dao.getUniqueObmDomain("user8")).isEqualTo("test2.tlse.lng");
	}

	private ObmUser.Builder sampleUserBuilder(int id, int entityId, String extId) {
		return ObmUser
				.builder()
				.login(UserLogin.valueOf("user" + id))
				.uid(id)
				.entityId(EntityId.valueOf(entityId))
				.identity(UserIdentity.builder()
					.lastName("Lastname")
					.firstName("Firstname")
					.build())
				.domain(domain)
				.profileName(ProfileName.valueOf("user"))
				.password(UserPassword.valueOf("user" + id))
				.uidNumber(1000)
				.gidNumber(512)
				.address(UserAddress.builder().countryCode("0").build())
				.extId(UserExtId.valueOf(extId))
				.publicFreeBusy(true);
	}
	private ObmUser sampleUser(int id, int entityId, String extId) {
		return sampleUserBuilder(id, entityId, extId)
				.emails(UserEmails.builder()
						.addAddress("user" + id)
						.server(mailHost)
						.domain(domain)
						.build())
				.build();
	}

	private ObmUser sampleUserWithoutMail(int id, int entityId, String extId) {
		return sampleUserBuilder(id, entityId, extId)
				.build();
	}

}
