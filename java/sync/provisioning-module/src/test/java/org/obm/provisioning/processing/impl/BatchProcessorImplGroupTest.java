package org.obm.provisioning.processing.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.domain.dao.PGroupDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.Group;
import org.obm.provisioning.Group.Id;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupRecursionException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(BatchProcessorImplGroupTest.Env.class)
public class BatchProcessorImplGroupTest extends BatchProcessorImplTestEnv {

	@Inject
	private PGroupDao pGroupDao;

	private void expectPGroupDaoDelete(Group group) throws DaoException {
		pGroupDao.delete(group);
		expectLastCall();
	}

	private void expectPGroupDaoInsert(Group group) throws DaoException {
		pGroupDao.insert(group);
		expectLastCall();
	}

	private void expectLdapCreateGroup(Group group) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.createGroup(group, domain);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapDeleteGroup(Group group) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.deleteGroup(domain, group);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapModifyGroup(Group group, Group oldGroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.modifyGroup(domain, group, oldGroup);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapAddGroupToGroup(Group group, Group subgroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.addSubgroupToGroup(domain, group, subgroup);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapRemoveGroupFromGroup(Group group, Group subgroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.removeSubgroupFromGroup(domain, group, subgroup);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	@Test
	public void testProcessDeleteGroup() throws DaoException,
			BatchNotFoundException, GroupNotFoundException,
			DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(
						Request.builder().resourcePath("/groups/extIdGroup1")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.verb(HttpVerb.DELETE).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder().name("group1").extId(extId)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		groupDao.delete(domain, extId);
		expectLastCall();
		expectLdapDeleteGroup(groupFromDao);
		expectPGroupDaoDelete(groupFromDao);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyGroup() throws DaoException,
			BatchNotFoundException, GroupNotFoundException,
			DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(
						Request.builder()
								.resourcePath("/groups/extIdGroup1")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdGroup1\","
										+ "\"name\": \"group1\","
										+ "\"description\": \"newDescription\""
										+ "}").build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder().name("group1").extId(extId)
				.build();
		final Group newGroup = Group.builder().name("group1").extId(extId)
				.description("newDescription").build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.update(domain, newGroup)).andReturn(newGroup);
		expectLdapModifyGroup(newGroup, groupFromDao);
		expectPGroupDaoDelete(newGroup);
		expectPGroupDaoInsert(newGroup);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessCreateGroup() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(
						Request.builder()
								.resourcePath("/groups/")
								.verb(HttpVerb.POST)
								.body("{" + "\"id\": \"newGroupExtId\","
										+ "\"name\": \"newGroup\","
										+ "\"description\": \"description\""
										+ "}").build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("newGroupExtId");
		final Group groupFromDao = Group.builder().name("newGroup").gid(1)
				.uid(Id.valueOf(1)).description("description").extId(extId)
				.build();
		final Group newGroup = Group.builder().name("newGroup")
				.description("description").extId(extId).build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.create(domain, newGroup)).andReturn(groupFromDao);
		expectLdapCreateGroup(groupFromDao);
		expectPGroupDaoInsert(groupFromDao);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessRenameGroup() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(
						Request.builder()
								.resourcePath("/groups/extIdGroup1")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.verb(HttpVerb.PUT)
								.body("{" + "\"id\": \"extIdGroup1\","
										+ "\"name\": \"newName\","
										+ "\"description\": \"description\""
										+ "}").build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		Group groupFromDao = Group.builder().name("group1").extId(extId)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder
										.status(BatchStatus.ERROR)
										.error("org.obm.provisioning.exception.ProcessingException: Cannot rename a group.")
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessPatchGroup() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(
						Request.builder()
								.resourcePath("/groups/extIdGroup1")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.verb(HttpVerb.PATCH)
								.body("{" + "\"name\": \"newName\","
										+ "\"description\": \"newDescription\""
										+ "}").build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder().name("group1").gid(1)
				.uid(Id.valueOf(1)).description("description").extId(extId)
				.build();
		final Group newGroup = Group.builder().name("newName").gid(1)
				.uid(Id.valueOf(1)).description("newDescription").extId(extId)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.update(domain, newGroup)).andReturn(newGroup);
		expectLdapModifyGroup(newGroup, groupFromDao);
		expectPGroupDaoDelete(newGroup);
		expectPGroupDaoInsert(newGroup);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessAddUserToGroup() throws DaoException,
			BatchNotFoundException, GroupNotFoundException,
			UserNotFoundException, SQLException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER_MEMBERSHIP)
				.request(
						Request.builder()
								.resourcePath(
										"/groups/extIdGroup1/users/extIdUser1")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.PUT).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder().name("group1").extId(extId)
				.build();
		final ObmUser userFromDao = ObmUser.builder()
				.extId(UserExtId.valueOf("extIdUser1")).domain(domain)
				.login("log").build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domain))
				.andReturn(userFromDao);
		groupDao.addUser(domain, extId, userFromDao);
		expectLastCall();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expectLdapAddUserToAllParentsOfGroup(groupFromDao, userFromDao);
		expectPGroupDaoDelete(groupFromDao);
		expectPGroupDaoInsert(groupFromDao);

		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	private void expectLdapAddUserToAllParentsOfGroup(Group group,
			ObmUser userToAdd) throws DaoException, GroupNotFoundException {
		ImmutableSet<Group.Id> groupIds = ImmutableSet.of(Group.Id.valueOf(1),
				Group.Id.valueOf(2));

		expect(groupDao.listParents(domain, group.getExtId())).andReturn(
				groupIds);
		LdapManager ldapManager = expectLdapBuild();

		Group group1 = Group.builder().uid(Group.Id.valueOf(1)).build();

		Group group2 = Group.builder().uid(Group.Id.valueOf(2)).build();

		expect(groupDao.get(Group.Id.valueOf(1))).andReturn(group1);
		expect(groupDao.get(Group.Id.valueOf(2))).andReturn(group2);

		ldapManager.addUserToGroup(domain, group1, userToAdd);
		expectLastCall();
		ldapManager.addUserToGroup(domain, group2, userToAdd);
		expectLastCall();

		ldapManager.shutdown();
		expectLastCall();
	}

	@Test
	public void testProcessDeleteUserFromGroup() throws DaoException,
			BatchNotFoundException, GroupNotFoundException,
			UserNotFoundException, SQLException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER_MEMBERSHIP)
				.request(
						Request.builder()
								.resourcePath(
										"/groups/extIdGroup1/users/extIdUser1")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.param(Request.USERS_ID_KEY, "extIdUser1")
								.verb(HttpVerb.DELETE).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder().name("group1").extId(extId)
				.build();
		final ObmUser userFromDao = ObmUser.builder()
				.extId(UserExtId.valueOf("extIdUser1")).domain(domain)
				.login("log").build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domain))
				.andReturn(userFromDao);
		groupDao.removeUser(domain, extId, userFromDao);
		expectLastCall();
		expectLdapDeleteUserFromAllParentGroup(groupFromDao, userFromDao);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	private void expectLdapDeleteUserFromAllParentGroup(Group group,
			ObmUser userToDelete) throws GroupNotFoundException, DaoException {
		ImmutableSet<Group.Id> groupIds = ImmutableSet.of(Group.Id.valueOf(1),
				Group.Id.valueOf(2));

		expect(groupDao.listParents(domain, group.getExtId())).andReturn(
				groupIds);
		LdapManager ldapManager = expectLdapBuild();

		Group group1 = Group.builder().uid(Group.Id.valueOf(1)).build();

		Group group2 = Group.builder().uid(Group.Id.valueOf(2)).build();

		expect(groupDao.get(Group.Id.valueOf(1))).andReturn(group1);
		expect(groupDao.get(Group.Id.valueOf(2))).andReturn(group2);

		ldapManager.removeUserFromGroup(domain, group1, userToDelete);
		expectLastCall();
		ldapManager.removeUserFromGroup(domain, group2, userToDelete);
		expectLastCall();

		ldapManager.shutdown();
		expectLastCall();
	}

	@Test
	public void testProcessAddGroupToGroup() throws DaoException,
			BatchNotFoundException, GroupNotFoundException,
			GroupRecursionException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP_MEMBERSHIP)
				.request(
						Request.builder()
								.resourcePath(
										"/groups/extIdGroup1/subgroups/extIdGroup2")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.param(Request.SUBGROUPS_ID_KEY, "extIdGroup2")
								.verb(HttpVerb.PUT).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder().name("group1").extId(extId)
				.build();
		final GroupExtId subgroupExtId = GroupExtId.valueOf("extIdGroup2");
		final Group subgroupFromDao = Group.builder().name("group2")
				.extId(subgroupExtId).build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		groupDao.addSubgroup(domain, extId, subgroupExtId);
		expectLastCall();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.get(domain, subgroupExtId)).andReturn(subgroupFromDao);
		expectLdapAddGroupToGroup(groupFromDao, subgroupFromDao);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessRemoveGroupFromGroup() throws DaoException,
			BatchNotFoundException, GroupNotFoundException,
			DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP_MEMBERSHIP)
				.request(
						Request.builder()
								.resourcePath(
										"/groups/extIdGroup1/subgroups/extIdGroup2")
								.param(Request.GROUPS_ID_KEY, "extIdGroup1")
								.param(Request.SUBGROUPS_ID_KEY, "extIdGroup2")
								.verb(HttpVerb.DELETE).build());
		Batch.Builder batchBuilder = Batch.builder().id(batchId(1))
				.domain(domain).status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder().name("group1").extId(extId)
				.build();
		final GroupExtId subgroupExtId = GroupExtId.valueOf("extIdGroup2");
		final Group subgroupFromDao = Group.builder().name("group2")
				.extId(subgroupExtId).build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		groupDao.removeSubgroup(domain, extId, subgroupExtId);
		expectLastCall();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.get(domain, subgroupExtId)).andReturn(subgroupFromDao);
		expectLdapRemoveGroupFromGroup(groupFromDao, subgroupFromDao);
		expect(
				batchDao.update(batchBuilder
						.operation(
								opBuilder.status(BatchStatus.SUCCESS)
										.timecommit(date).build())
						.status(BatchStatus.SUCCESS).timecommit(date).build()))
				.andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

}
