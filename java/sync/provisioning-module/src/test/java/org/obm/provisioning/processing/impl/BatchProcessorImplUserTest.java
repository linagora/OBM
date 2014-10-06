package org.obm.provisioning.processing.impl;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.cyrus.imap.admin.CyrusImapService;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.domain.dao.EntityRightDao;
import org.obm.domain.dao.PUserDao;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserSystemDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.Group;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.utils.DateUtils;
import org.obm.satellite.client.Configuration;
import org.obm.satellite.client.Connection;
import org.obm.satellite.client.SatelliteService;
import org.obm.sync.dao.EntityId;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;
import org.obm.utils.ObmHelper;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.profile.Module;
import fr.aliacom.obm.common.profile.ModuleCheckBoxStates;
import fr.aliacom.obm.common.profile.Profile;
import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserPassword;

@RunWith(GuiceRunner.class)
@GuiceModule(BatchProcessorImplUserTest.Env.class)
public class BatchProcessorImplUserTest extends BatchProcessorImplTestEnv {

	@Inject
	private EntityRightDao entityRightDao;
	@Inject
	private UserSystemDao userSystemDao;
	@Inject
	private PUserDao pUserDao;
	@Inject
	private ObmHelper obmHelper;
	@Inject
	private CyrusImapService cyrusService;
	@Inject
	private SatelliteService satelliteService;

	private final Date date = DateUtils.date("2013-08-01T12:00:00");

	private final Group usersGroup = Group.builder().uid(Group.Id.valueOf(1))
			.gid(UserDao.DEFAULT_GID).name("Users").build();

	private final ObmSystemUser obmSatelliteUser = ObmSystemUser.builder()
			.id(1).login("obmsatelliterequest").password(UserPassword.valueOf("secret")).build();

	private final ObmSystemUser obmCyrusUser = ObmSystemUser.builder().id(2)
			.login("cyrus").password(UserPassword.valueOf("secret")).build();

	private final Profile profile = Profile
			.builder()
			.id(ProfileId.valueOf("1"))
			.name(ProfileName.valueOf("user"))
			.level(0)
			.domain(domain)
			.defaultCheckBoxState(
					Module.CALENDAR,
					ModuleCheckBoxStates.builder().module(Module.CALENDAR)
							.build())
			.defaultCheckBoxState(
					Module.MAILBOX,
					ModuleCheckBoxStates.builder().module(Module.MAILBOX)
							.build()).build();

	private final ObmDomain domainWithImapAndLdap = ObmDomain
			.builder()
			.name("domain")
			.id(1)
			.uuid(ObmDomainUuid.of("a3443822-bb58-4585-af72-543a287f7c0e"))
			.host(ServiceProperty.IMAP,
					ObmHost.builder().name("Cyrus").localhost().build())
			.host(ServiceProperty.IMAP,
					ObmHost.builder().name("NewCyrus").localhost().build())
			.host(ServiceProperty.LDAP,
					ObmHost.builder().name("OpenLDAP").localhost().build())
			.alias("domain.com")
			.build();

	private final ObmDomain domainWithImapWithoutLdap = ObmDomain
			.builder()
			.name("domain")
			.id(1)
			.uuid(ObmDomainUuid.of("a3443822-bb58-4585-af72-543a287f7c0e"))
			.host(ServiceProperty.IMAP,
					ObmHost.builder().name("host").ip("127.0.0.1").localhost().build())
			.alias("domain.com")
			.build();

	private final UserLogin user1Login = UserLogin.valueOf("user1");
	private final UserIdentity user1Name = UserIdentity.builder().lastName("user1").build();
	
	@Test
	public void testProcessCreateUserWithInvalidJSONData() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request.builder()
								.resourcePath("/users/").verb(HttpVerb.POST)
								.body("{" + "\"invalid\": \"json\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot parse ObmUser object from request body {\"invalid\": \"json\"}.")
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessCreateUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		final ObmUser user = ObmUser.builder().login(user1Login).identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1")).domain(domain)
				.build();
		final ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").localhost().build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getAllEmailsFrom(domain, user.getExtId())).andReturn(ImmutableSet.<String>of());
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(
				usersGroup);
		groupDao.addUser(domain, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expectSetDefaultRights(userFromDao);
		expectLdapCreateUser(userFromDao, usersGroup);
		expectCyrusCreateMailbox(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoInsert(userFromDao);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessCreateArchivedUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"archived\": true,"
										+ "\"mails\":[\"john@domain\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		final ObmUser user = ObmUser.builder().login(user1Login).identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1")).domain(domain)
				.archived(true)
				.build();
		final ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").localhost().build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.archived(true)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getAllEmailsFrom(domain, user.getExtId())).andReturn(ImmutableSet.<String>of());
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(
				usersGroup);
		groupDao.addUser(domain, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expectSetDefaultRights(userFromDao);
		expectCyrusCreateMailbox(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoInsert(userFromDao);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessCreateUserWithNoLdap() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapWithoutLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		final ObmUser user = ObmUser.builder().login(user1Login).identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").ip("127.0.0.1").build())
					.domain(domainWithImapWithoutLdap)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1")).domain(domainWithImapWithoutLdap)
				.build();
		final ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").localhost().build())
					.domain(domainWithImapWithoutLdap)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapWithoutLdap)
				.build();

		expectDomain(domainWithImapWithoutLdap);
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getAllEmailsFrom(domainWithImapWithoutLdap, user.getExtId())).andReturn(ImmutableSet.<String>of());
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domainWithImapWithoutLdap, UserDao.DEFAULT_GID)).andReturn(
				usersGroup);
		groupDao.addUser(domainWithImapWithoutLdap, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expect(configurationService.isLdapModuleEnabled()).andReturn(false);
		expectSetDefaultRights(userFromDao);
		expectCyrusCreateMailbox(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoInsert(userFromDao);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessCreateUserFailsWithExistingEmails() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getAllEmailsFrom(domain, UserExtId.valueOf("extIdUser1"))).andReturn(ImmutableSet.of("john@domain"));

		expect(batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot create/modify user because similar emails have been found : [john@domain]")
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	private void expectSetDefaultRights(ObmUser user) throws Exception {
		expect(profileDao.getUserProfile(user)).andReturn(profile);
		expect(obmHelper.fetchEntityId("Calendar", 1)).andReturn(
				EntityId.valueOf(2));
		expect(obmHelper.fetchEntityId("Mailbox", 1)).andReturn(
				EntityId.valueOf(3));
		entityRightDao.grantRights(eq(EntityId.valueOf(2)),
				isNull(EntityId.class), isA(Set.class));
		expectLastCall();
		entityRightDao.grantRights(eq(EntityId.valueOf(3)),
				isNull(EntityId.class), isA(Set.class));
		expectLastCall();
	}

	private void expectPUserDaoDelete(ObmUser user) throws DaoException {
		pUserDao.delete(user);
		expectLastCall();
	}

	private void expectPUserDaoArchive(ObmUser user) throws DaoException {
		pUserDao.archive(user);
		expectLastCall();
	}

	private void expectPUserDaoInsert(ObmUser user) throws DaoException {
		pUserDao.insert(user);
		expectLastCall();
	}

	@Test
	public void testProcessCreateUserWhenDefaultGroupDoesntExist()
			throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser user = ObmUser.builder().login(user1Login).identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1")).domain(domain)
				.build();
		ObmUser userFromDao = ObmUser
				.builder()
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").localhost().build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getAllEmailsFrom(domain, user.getExtId())).andReturn(ImmutableSet.<String>of());
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(null);
		expectLastCall();

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.error("org.obm.provisioning.exception.ProcessingException: Default group with GID 1000 not found for domain domain.")
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	private void expectCyrusCreateMailbox(final ObmUser user) throws Exception {
		CyrusManager cyrusManager = expectCyrusBuild();
		cyrusManager.create(user);
		expectLastCall().once();
		cyrusManager.setAcl(user, "anyone",
				Acl.builder().user("user1").rights("p").build());
		expectLastCall().once();
		cyrusManager.applyQuota(user);
		expectLastCall();
		expectCyrusShutDown(cyrusManager);
	}

	@Test
	public void testProcessCreateUserAndUpdateSatellite() throws Exception {

		ObmDomain domainWithSmtpIn = ObmDomain
				.builder()
				.from(domainWithImapAndLdap)
				.host(ServiceProperty.SMTP_IN,
						ObmHost.builder().name("Postfix").localhost()
								.build()).build();

		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithSmtpIn).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Connection satelliteConnection = mocksControl
				.createMock(Connection.class);

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());

		ObmUser user = ObmUser.builder().login(user1Login).identity(user1Name)
				.password(UserPassword.valueOf("secret")).profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithSmtpIn).build();
		ObmUser userFromDao = ObmUser.builder().uid(1).login(user1Login)
				.password(UserPassword.valueOf("secret")).profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithSmtpIn).build();
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domainWithSmtpIn, UserDao.DEFAULT_GID))
				.andReturn(usersGroup);
		groupDao.addUser(domainWithSmtpIn, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expectSetDefaultRights(userFromDao);

		expectLdapCreateUser(userFromDao, usersGroup);
		expect(userSystemDao.getByLogin("obmsatelliterequest")).andReturn(
				obmSatelliteUser);
		expect(
				satelliteService.create(isA(Configuration.class),
						eq(domainWithSmtpIn))).andReturn(satelliteConnection);
		satelliteConnection.updateMTA();
		expectLastCall();
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoInsert(userFromDao);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	private void createBatchWithOneUserAndCommit() {
		given().auth().basic("username@domain", "password").post("/batches/");
		given().auth().basic("username@domain", "password")
				.post("/batches/1/users");
		given().auth().basic("username@domain", "password").put("/batches/1");
	}

	private void createBatchWithOneUserUpdateAndCommit() {
		given().auth().basic("username@domain", "password").post("/batches/");
		given().auth().basic("username@domain", "password")
				.put("/batches/1/users/1");
		given().auth().basic("username@domain", "password").put("/batches/1");
	}

	private void createBatchWithOneUserPatchAndCommit() {
		given().auth().basic("username@domain", "password").post("/batches/");
		given().auth().basic("username@domain", "password")
				.patch("/batches/1/users/1");
		given().auth().basic("username@domain", "password").put("/batches/1");
	}

	private void expectBatchCreationAndRetrieval(Batch batch) throws Exception {
		expectSuccessfulAuthenticationAndFullAuthorization();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expectSuccessfulAuthenticationAndFullAuthorization();

		expect(batchDao.create(isA(Batch.class))).andReturn(batch);
		batchDao.addOperation(eq(batch), isA(Operation.class));
		expectLastCall();
		expect(batchDao.get(batchId(1), domain)).andReturn(batch);
		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(batchDao.get(batchId(1), domain)).andReturn(batch);
	}

	private void expectLdapCreateUser(ObmUser userToAdd, Group defaultGroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.createUser(userToAdd);
		expectLastCall();
		ldapManager.addUserToDefaultGroup(userToAdd.getDomain(), defaultGroup,
				userToAdd);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapModifyUser(ObmUser user, ObmUser oldUser) {
		LdapManager ldapManager = expectLdapBuild();

		ldapManager.modifyUser(user, oldUser);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapdeleteUser(ObmUser userToRemove, Group defaultGroup) throws SQLException {
		LdapManager ldapManager = expectLdapBuild();

		expect(groupDao.getAllGroupsForUserExtId(domainWithImapAndLdap, UserExtId.valueOf("extIdUser1"))).andReturn(Collections.EMPTY_SET);

		ldapManager.removeUserFromDefaultGroup(userToRemove.getDomain(), defaultGroup, userToRemove);
		expectLastCall();
		ldapManager.deleteUser(userToRemove);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	@Test
	public void testCreateUserWithNoEmail() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						Request.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser.builder().login(user1Login).identity(user1Name)
				.password(UserPassword.valueOf("secret")).profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap).build();
		final ObmUser userFromDao = ObmUser.builder().uid(1).login(user1Login)
				.password(UserPassword.valueOf("secret")).profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1")).domain(domain).build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(
				usersGroup);
		groupDao.addUser(domain, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expectSetDefaultRights(userFromDao);
		expectLdapCreateUser(userFromDao, usersGroup);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoInsert(userFromDao);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testCreateUserWithNoEmailButAMailHost() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						Request.builder()
								.resourcePath("/users/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"mail_server\":\"Cyrus\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot parse ObmUser object from request body {\"id\": \"extIdUser1\",\"mail_server\":\"Cyrus\",\"login\": \"user1\",\"lastname\": \"user1\",\"profile\": \"user\",\"password\": \"secret\"}.")
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessDeleteUserWithFalseExpunge() throws SQLException,
			DaoException, BatchNotFoundException, UserNotFoundException,
			DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						Request.builder().resourcePath("/users/extIdUser1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.DELETE).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser.builder().login(user1Login)
				.password(UserPassword.valueOf("secret")).profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1")).domain(domain).build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		final UserExtId extId = UserExtId.valueOf("extIdUser1");
		expect(userDao.getByExtId(extId, domain)).andReturn(user);
		userDao.archive(user);
		expectLastCall();
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(
				usersGroup);
		expectLdapdeleteUser(user, usersGroup);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoArchive(user);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessDeleteUserWithTrueExpunge() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						Request.builder().resourcePath("/users/extIdUser1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.param(Request.EXPUNGE_KEY, "true")
								.verb(HttpVerb.DELETE).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser
				.builder()
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.emails(UserEmails.builder()
					.addAddress("user1@domain")
					.server(ObmHost.builder().name("host").localhost().build())
					.domain(domain)
					.build())
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		final UserExtId extId = UserExtId.valueOf("extIdUser1");
		expect(userDao.getByExtId(extId, domain)).andReturn(user);
		userDao.delete(user);
		expectLastCall();
		expectDeleteUserMailbox(user);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(
				usersGroup);
		expectLdapdeleteUser(user, usersGroup);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoDelete(user);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessDeleteUserWithoutMailWithTrueExpunge()
			throws SQLException, DaoException, BatchNotFoundException,
			UserNotFoundException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						Request.builder().resourcePath("/users/extIdUser1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.param(Request.EXPUNGE_KEY, "true")
								.verb(HttpVerb.DELETE).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser
				.builder()
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.emails(UserEmails.builder()
					.server(ObmHost.builder().name("host").localhost().build())
					.domain(domain)
					.build())
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		final UserExtId extId = UserExtId.valueOf("extIdUser1");
		expect(userDao.getByExtId(extId, domain)).andReturn(user);
		userDao.delete(user);
		expectLastCall();
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(
				usersGroup);
		expectLdapdeleteUser(user, usersGroup);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);
		expectPUserDaoDelete(user);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessDeleteArchivedUserWithoutLdapEntry() throws Exception,
			DaoException, BatchNotFoundException, UserNotFoundException,
			IMAPException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						Request.builder().resourcePath("/users/extIdUser1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.param(Request.EXPUNGE_KEY, "true")
								.verb(HttpVerb.DELETE).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		final ObmUser user = ObmUser
				.builder()
				.login(UserLogin.valueOf("user1"))
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.archived(true)
				.emails(UserEmails.builder()
					.addAddress("user1@domain")
					.server(ObmHost.builder().name("host").localhost().build())
					.domain(domain)
					.build())
				.build();

		UserExtId extId = UserExtId.valueOf("extIdUser1");
		Date batchCommitDate = DateUtils.date("2013-08-01T12:00:00");

		expect(userDao.getByExtId(extId, domain)).andReturn(user);
		userDao.delete(user);
		expectLastCall();
		expectDeleteUserMailbox(user);

		expect(dateProvider.getDate()).andReturn(batchCommitDate).anyTimes();

		Batch batch =  batchBuilder
			.operation(
					opBuilder.status(BatchStatus.SUCCESS)
							.timecommit(batchCommitDate).build())
			.status(BatchStatus.SUCCESS).timecommit(batchCommitDate).build();

		expect(batchDao.update(batch)).andReturn(batch);
		expectPUserDaoDelete(user);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.domain(domainWithImapAndLdap)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();
		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.domain(domainWithImapAndLdap)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(
				userDao.getByExtId(UserExtId.valueOf("extIdUser1"),
						domainWithImapAndLdap)).andReturn(userFromDao);
		expect(userDao.update(user)).andReturn(userFromDao);
		expectLdapModifyUser(userFromDao, userFromDao);
		CyrusManager cyrusManager = expectCyrusBuild();
		expectApplyQuota(cyrusManager, userFromDao);
		expectCyrusShutDown(cyrusManager);
		expectPUserDaoDelete(userFromDao);
		expectPUserDaoInsert(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUserFailsWithExistingEmails() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\", \"alias1\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.domain(domainWithImapAndLdap)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"),
						domainWithImapAndLdap)).andReturn(userFromDao);
		expect(userDao.getAllEmailsFrom(domain, UserExtId.valueOf("extIdUser1"))).andReturn(ImmutableSet.of("john@domain", "alias1@domain"));

		expect(batchDao.update(batchBuilder
				.operation(
						opBuilder
								.status(BatchStatus.ERROR)
								.error("org.obm.provisioning.exception.ProcessingException: Cannot create/modify user because similar emails have been found : [john@domain]")
								.timecommit(date).build())
				.status(BatchStatus.SUCCESS).timecommit(date).build()))
		.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUserCannotChangeLogin() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1new\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\"],"
										+ "\"mail_server\":\"Cyrus\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(
				userDao.getByExtId(UserExtId.valueOf("extIdUser1"),
						domainWithImapAndLdap)).andReturn(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.timecommit(date)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot change user login")
										.build()).status(BatchStatus.SUCCESS)
						.timecommit(date).build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUserCannotChangeMailHost() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\"],"
										+ "\"mail_server\":\"NewCyrus\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.domain(domain)
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(
				userDao.getByExtId(UserExtId.valueOf("extIdUser1"),
						domainWithImapAndLdap)).andReturn(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.timecommit(date)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot change user mail host")
										.build()).status(BatchStatus.SUCCESS)
						.timecommit(date).build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testModifyWhenArchivingNonArchivedUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"archived\": true" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.archived(true)
				.build();
		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domainWithImapAndLdap)).andReturn(userFromDao);
		expect(userDao.update(user)).andReturn(user);
		expect(groupDao.getByGid(domainWithImapAndLdap, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		expectLdapdeleteUser(user, usersGroup);
		expectPUserDaoDelete(user);
		expectPUserDaoInsert(user);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testModifyWhenArchivingArchivedUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"archived\": true" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.archived(true)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domainWithImapAndLdap)).andReturn(user);
		expect(userDao.update(user)).andReturn(user);
		expectLdapModifyUser(user, user);
		expectPUserDaoDelete(user);
		expectPUserDaoInsert(user);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testModifyWhenUnarchivingArchivedUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"archived\": false" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();
		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.identity(user1Name)
				.password(UserPassword.valueOf("secret"))
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.archived(true)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domainWithImapAndLdap)).andReturn(userFromDao);
		expect(userDao.update(user)).andReturn(user);
		expect(groupDao.getByGid(domainWithImapAndLdap, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		expect(groupDao.getAllGroupsForUserExtId(domainWithImapAndLdap, UserExtId.valueOf("extIdUser1"))).andReturn(Collections.EMPTY_SET);
		expectLdapCreateUser(user, usersGroup);
		expectPUserDaoDelete(user);
		expectPUserDaoInsert(user);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessPatchUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request.builder()
								.resourcePath("/users/extIdUser1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PATCH)
								.body("{" + "\"lastname\": \"user1\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser.Builder builder = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain);
				
		ObmUser user = builder.identity(user1Name).build();

		ObmUser userFromDao = builder.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domain))
				.andReturn(userFromDao);
		expect(userDao.update(user)).andReturn(userFromDao);
		expectLdapModifyUser(userFromDao, userFromDao);
		CyrusManager cyrusManager = expectCyrusBuild();
		expectApplyQuota(cyrusManager, userFromDao);
		expectCyrusShutDown(cyrusManager);
		expectPUserDaoDelete(userFromDao);
		expectPUserDaoInsert(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserPatchAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessPatchUserFailsWithExistingMail() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request.builder()
								.resourcePath("/users/extIdUser1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PATCH)
								.body("{" + "\"id\": \"extIdUser1\","
										+ "\"login\": \"user1\","
										+ "\"lastname\": \"user1\","
										+ "\"profile\": \"user\","
										+ "\"password\": \"secret\","
										+ "\"mails\":[\"john@domain\", \"alias1\"]" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser.Builder builder = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("host").build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain);

		ObmUser userFromDao = builder.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domain))
				.andReturn(userFromDao);
		expect(userDao.getAllEmailsFrom(domain, UserExtId.valueOf("extIdUser1"))).andReturn(ImmutableSet.of("john@domain", "alias1@domain"));

		expect(batchDao.update(batchBuilder
				.operation(
						opBuilder
								.status(BatchStatus.ERROR)
								.error("org.obm.provisioning.exception.ProcessingException: Cannot create/modify user because similar emails have been found : [john@domain]")
								.timecommit(date).build())
				.status(BatchStatus.SUCCESS).timecommit(date).build()))
		.andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserPatchAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessPatchUserCannotChangeLogin() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PATCH)
								.body("{" + "\"login\": \"user1new\"" + "}")
								.build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.domain(domainWithImapAndLdap)
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.build())
				.profileName(ProfileName.valueOf("user"))
				.identity(user1Name)
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(
				userDao.getByExtId(UserExtId.valueOf("extIdUser1"),
						domainWithImapAndLdap)).andReturn(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.timecommit(date)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot change user login")
										.build()).status(BatchStatus.SUCCESS)
						.timecommit(date).build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessPatchUserCannotChangeMailHost() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(
						org.obm.provisioning.beans.Request
								.builder()
								.resourcePath("/users/1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PATCH)
								.body("{" + "\"mail_server\":\"NewCyrus\""
										+ "}").build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domainWithImapAndLdap).status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login(user1Login)
				.password(UserPassword.valueOf("secret"))
				.emails(UserEmails.builder()
					.addAddress("john@domain")
					.server(ObmHost.builder().name("Cyrus").localhost().build())
					.domain(domain)
					.build())
				.profileName(ProfileName.valueOf("user"))
				.identity(user1Name)
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(
				userDao.getByExtId(UserExtId.valueOf("extIdUser1"),
						domainWithImapAndLdap)).andReturn(userFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.timecommit(date)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot change user mail host")
										.build()).status(BatchStatus.SUCCESS)
						.timecommit(date).build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	private void expectDeleteUserMailbox(final ObmUser user) throws Exception {
		CyrusManager cyrusManager = expectCyrusBuild();
		cyrusManager.setAcl(user, "cyrus",
				Acl.builder().user("user1").rights("lc").build());
		expectLastCall().once();
		cyrusManager.delete(user);
		expectLastCall().once();
		expectCyrusShutDown(cyrusManager);
	}

	private void expectApplyQuota(CyrusManager cyrusManager, ObmUser user) throws Exception {
		cyrusManager.applyQuota(user);
		expectLastCall();
	}

	private void expectCyrusShutDown(CyrusManager cyrusManager) throws ImapTimeoutException {
		cyrusManager.close();
		expectLastCall().once();
	}

	private CyrusManager expectCyrusBuild() throws DaoException, IMAPException {
		expect(userSystemDao.getByLogin("cyrus")).andReturn(obmCyrusUser);
		CyrusManager cyrusManager = mocksControl.createMock(CyrusManager.class);
		expect(cyrusService.buildManager("127.0.0.1", "cyrus", UserPassword.valueOf("secret")))
				.andReturn(cyrusManager);
		return cyrusManager;
	}

}
