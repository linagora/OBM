/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2013 Linagora
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

package org.obm.provisioning.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.AddressBookDao;
import org.obm.domain.dao.AddressBookDaoJdbcImpl;
import org.obm.domain.dao.ObmInfoDao;
import org.obm.domain.dao.ObmInfoDaoJdbcImpl;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserDaoJdbcImpl;
import org.obm.domain.dao.UserPatternDao;
import org.obm.domain.dao.UserPatternDaoJdbcImpl;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.Group;
import org.obm.provisioning.Group.Builder;
import org.obm.provisioning.Group.Id;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.GroupExistsException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupRecursionException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.sync.dao.EntityId;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(GroupDaoJdbcImplTest.Env.class)
public class GroupDaoJdbcImplTest {

	public static final int FIRST_AUTOMATIC_GID = 1001;

    public static class Env extends AbstractModule {

        @Override
        protected void configure() {
            bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");

            bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
            bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
            bind(ObmInfoDao.class).to(ObmInfoDaoJdbcImpl.class);
            bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
            bind(AddressBookDao.class).to(AddressBookDaoJdbcImpl.class);
            bind(UserPatternDao.class).to(UserPatternDaoJdbcImpl.class);
            bind(UserDao.class).to(UserDaoJdbcImpl.class);
        }

    }

    private ObmDomain domain1, domain2;
    private ObmUser user1, nonexistentUser;
    private Group group4, group6, group7, nonexistentGroup;

    @Before
    public void init() {
        domain1 = ToolBox.getDefaultObmDomain();
		domain2 = ObmDomain
				.builder()
				.id(2)
				.name("test2.tlse.lng")
				.uuid(ObmDomainUuid.valueOf("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf"))
				.label("test2.tlse.lng")
				.build();
        user1 = userDao.findUserById(1, domain1);

        group4 = generateGroup(4, "existing-nousers-subgroups-child1");
        group6 = generateGroup(6, "existing-users-subgroups-child1");
        group7 = generateGroup(7, "existing-users-subgroups-child2");

        nonexistentUser = generateUser(999);
        nonexistentGroup = generateGroup(123, "nonexistent");
    }

    private ObmUser generateUser(int uid) {
        String stringUid = String.valueOf(uid);
        return ObmUser.builder()
                      .uid(uid)
                      .login("user" + stringUid)
                      .commonName("")
                      .lastName("")
                      .firstName("")
                      .entityId(EntityId.valueOf(uid))
                      .extId(UserExtId.valueOf("user" + stringUid))
                      .emailAndAliases("user" + stringUid + "@test.tlse.lng")
                      .publicFreeBusy(true)
                      .domain(domain1)
                      .build();
    }

    private Group generateGroup(int uid, String prefix) {
        return Group.builder()
        			.uid(Id.valueOf(uid))
                    .extId(GroupExtId.valueOf(prefix))
                    .name(prefix + "-name")
                    .description(prefix + "-description")
                    .build();
    }

    @Inject
    private GroupDao dao;

    @Inject
    private UserDao userDao;

    @Rule
    @Inject
    public H2InMemoryDatabase db = new H2InMemoryDatabase("sql/initial.sql");

    @Test(expected = GroupNotFoundException.class)
    public void testGetNonexistantGroup() throws Exception {
        dao.get(domain1, GroupExtId.valueOf("1234"));
    }

    @Test
    public void testExistingGroup() throws Exception {
        Group group = dao.get(domain1, GroupExtId.valueOf("existing-nousers-nosubgroups"));
        testGroupBase("existing-nousers-nosubgroups", group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();
    }

    @Test(expected = GroupNotFoundException.class)
    public void testGetNonexistantRecursiveGroup() throws Exception {
        dao.getRecursive(domain1, GroupExtId.valueOf("nonexistent"), true, -1);
    }

    @Test
    public void testExistingRecursiveNoUsersNoSubgroups() throws Exception {

        Group group = dao.getRecursive(domain1, GroupExtId.valueOf("existing-nousers-nosubgroups"), true, -1);
        testGroupBase("existing-nousers-nosubgroups", group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();
    }

    @Test
    public void testExtistingRecursiveUsersNoSubgroups() throws Exception {
        String prefix = "existing-users-nosubgroups";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, true, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, false, 2);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();
    }

    @Test
    public void testExistingRecursiveNoUsersSubgroups() throws Exception {
        String prefix = "existing-nousers-subgroups";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).containsOnly(group4);

        group = dao.getRecursive(domain1, groupExtId, false, 1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).containsOnly(group4);

        group = dao.getRecursive(domain1, groupExtId, false, 2);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).containsOnly(group4);
    }

    @Test
    public void testExtistingRecursiveUsersSubgroups() throws Exception {
        String prefix = "existing-users-subgroups";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, 0);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).isEmpty();
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, true, 0);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).isEmpty();

        group = dao.getRecursive(domain1, groupExtId, true, -1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(group6, group7);

        group = dao.getRecursive(domain1, groupExtId, true, 1);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(group6);

        group = dao.getRecursive(domain1, groupExtId, true, 2);
        testGroupBase(prefix, group);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(group6, group7);

    }

    @Test
    public void testCyclicDirectChildren() throws Exception {
        /* The group structure here is:

           recursive-direct-parent
            |-recursive-direct-child1
               |-recursive-direct-parent    <-- Expansion should stop here
                  |-recursive-direct-child1
                     |-...
        */
        String prefix = "r-direct-parent";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertExpandedToDepth(group, 2);
    }

    @Test
    public void testCyclicMultipleChildren() throws Exception {
        /* The group structure here is:

            recursive-multichild-parent
             |-recursive-multichild-child1
             |  |-recursive-multichild-childcommon            <-- This must be expanded
             |     |-recursive-multichild-childcommonexpand
             |-recursive-multichild-child2
                |-recursive-multichild-childcommon            <-- This must be expanded
                   |-recursive-multichild-childcommonexpand

         */
        String prefix = "r-multichild-parent";
        GroupExtId groupExtId = GroupExtId.valueOf(prefix);

        Group group = dao.getRecursive(domain1, groupExtId, false, -1);
        testGroupBase(prefix, group);
        assertExpandedToDepth(group, 3);
    }

    @Test
    public void testCreateGroup() throws Exception {
        String prefix = "created-group";
        Group group = generateGroup(18, prefix);
        Group createdGroup = dao.create(domain1, group);
        testGroupBase(prefix, createdGroup);

        createdGroup = dao.get(domain1, group.getExtId());
        testGroupBase(prefix, createdGroup);
    }

    @Test(expected = GroupExistsException.class)
    public void testDuplicateCreate() throws Exception {
        String prefix = "created-group-duplicate";
        Group group = generateGroup(18, prefix);

        // This should work
        Group createdGroup = dao.create(domain1, group);
        testGroupBase(prefix, createdGroup);

        // This should fail
        createdGroup = dao.create(domain1, group);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testUpdateNonExistent() throws Exception {
        dao.update(domain1, nonexistentGroup);
    }

    @Test
    public void testUpdateGroup() throws Exception {
         String prefix = "modified-group";
         GroupExtId extId = GroupExtId.valueOf(prefix);

         Group modifiedGroup = Group.builder()
                                     .extId(extId)
                                     .name("modified-group-changed")
                                     .description("modified-group-description-changed")
                                     .build();

         Group modifiedReturnedGroup = dao.update(domain1, modifiedGroup);

         assertThat(modifiedGroup)
             .isLenientEqualsToByAcceptingFields(modifiedReturnedGroup,
                                                 "name", "description", "extId");

         Group retrievedGroup = dao.get(domain1, extId);
         assertThat(modifiedGroup)
             .isLenientEqualsToByAcceptingFields(retrievedGroup,
                                                 "name", "description", "extId");
    }

    @Test(expected = GroupNotFoundException.class)
    public void testDeleteNonexistent() throws Exception {
        dao.delete(domain1, nonexistentGroup.getExtId());
    }

    @Test
    public void testDelete() throws Exception {
         String prefix = "delete-group";
         GroupExtId extId = GroupExtId.valueOf(prefix);
         dao.delete(domain1, extId);

         try {
             dao.get(domain1, extId);
             throw new AssertionFailedError("Group exists after deletion");
         } catch (GroupNotFoundException e) {
             // This is expected, swallow the exception
         }
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddUserNonexistent() throws Exception {
        dao.addUser(domain1, nonexistentGroup.getExtId(), user1);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddSubgroupNonexistentParent() throws Exception {
        GroupExtId subgroupId = GroupExtId.valueOf("addusersubgroup-group-child");
        dao.addSubgroup(domain1, nonexistentGroup.getExtId(), subgroupId);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddSubgroupNonexistentChild() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("addusersubgroup-group-parent");
        dao.addSubgroup(domain1, parentId, nonexistentGroup.getExtId());
    }

    @Test(expected = GroupRecursionException.class)
    public void testAddSubgroupRecursion() throws Exception {
        Group parent = generateGroup(18, "addusersubgroup-group-parent");
        dao.addSubgroup(domain1, parent.getExtId(), parent.getExtId());
    }

    @Test
    public void testAddUserSubgroup() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("addusersubgroup-group-parent");
        Group childGroup = generateGroup(18, "addusersubgroup-group-child");
        dao.addUser(domain1, parentId, user1);
        dao.addSubgroup(domain1, parentId, childGroup.getExtId());

        Group group = dao.getRecursive(domain1, parentId, true, 1);
        assertThat(group.getUsers()).containsOnly(user1);
        assertThat(group.getSubgroups()).containsOnly(childGroup);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveUserNonexistentGroup() throws Exception {
        dao.removeUser(domain1, nonexistentGroup.getExtId(), user1);
    }

    @Test(expected = UserNotFoundException.class)
    public void testRemoveUserNonexistentUser() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("removeusersubgroup-group-parent");
        dao.removeUser(domain1, parentId, nonexistentUser);
    }

    @Test
    public void testRemoveUserSubgroups() throws Exception {
        GroupExtId parentId = GroupExtId.valueOf("removeusersubgroup-group-parent");
        GroupExtId subgroup = GroupExtId.valueOf("removeusersubgroup-group-child");
        dao.removeUser(domain1, parentId, user1);
        dao.removeSubgroup(domain1, parentId, subgroup);

        Group parent = dao.getRecursive(domain1, parentId, true, 1);
        assertThat(parent.getSubgroups()).isEmpty();
        assertThat(parent.getUsers()).isEmpty();
    }

	@Test
	public void testCreateGroupWithAllFields() throws Exception {
		Group group = Group
				.builder()
				.name("group")
				.extId(GroupExtId.valueOf("extIdGroup"))
				.privateGroup(true)
				.email("group@domain")
				.build();

		Group createdGroup = dao.create(domain1, group);

		assertThat(createdGroup.getUid().getId()).isGreaterThan(0);
		assertThat(createdGroup.getTimecreate()).isNotNull();
	}

	@Test
	public void testGetByGid() throws Exception {
		Builder groupBuilder = Group
				.builder()
				.name("group")
				.extId(GroupExtId.valueOf("extIdGroup"))
				.email("group@domain");

		Group createdGroup = dao.create(domain1, groupBuilder.build());

		assertThat(dao.getByGid(domain1, FIRST_AUTOMATIC_GID)).isEqualTo(groupBuilder
				.uid(createdGroup.getUid())
				.gid(FIRST_AUTOMATIC_GID)
				.build());
	}

	@Test
	public void testGetByGidWhenGroupDoesntExist() throws Exception {
		assertThat(dao.getByGid(domain1, 123)).isNull();
	}

	@Test
	public void testAddUserByGroupId() throws Exception {
		Builder groupBuilder = Group
				.builder()
				.name("group")
				.extId(GroupExtId.valueOf("extIdGroup"))
				.privateGroup(true)
				.email("group@domain");

		Group createdGroup = dao.create(domain1, groupBuilder.build());

		dao.addUser(domain1, createdGroup.getUid(), user1);

		Group updatedGroup = dao.getRecursive(domain1, createdGroup.getExtId(), true, -1);

		assertThat(updatedGroup).isEqualTo(groupBuilder
				.uid(createdGroup.getUid())
				.gid(FIRST_AUTOMATIC_GID)
				.user(user1)
				.build());
	}

	@Test(expected = DaoException.class)
	public void testAddUserByGroupIdWhenGroupDoesntExist() throws Exception {
		dao.addUser(domain1, Group.Id.valueOf(123), user1);
	}

	@Test(expected = DaoException.class)
	public void testAddUserByGroupIdWhenUserDoesntExist() throws Exception {
		dao.addUser(domain1, group4.getUid(), nonexistentUser);
	}

	@Test
	public void testAddUserUpdatesInternalMappings() throws Exception {
		Builder groupBuilder = Group
				.builder()
				.name("group")
				.extId(GroupExtId.valueOf("extIdGroup"))
				.privateGroup(true)
				.email("group@domain");

		Group createdGroup = dao.create(domain1, groupBuilder.build());
		Group.Id createdGroupId = createdGroup.getUid();

		dao.addUser(domain1, createdGroupId, user1);

		assertThat(getUserGroupsFromInternalMapping(user1.getUid())).isEqualTo(ImmutableSet.of(
				createdGroupId.getId()
		));
	}

	@Test
	public void testAddSubGroupUpdatesInternalMappings() throws Exception {
		Group group = Group
				.builder()
				.name("group")
				.extId(GroupExtId.valueOf("extIdGroup"))
				.email("group@domain")
				.build();
		Group subGroup = Group
				.builder()
				.name("subgroup")
				.extId(GroupExtId.valueOf("extIdSubGroup"))
				.email("subgroup@domain")
				.build();

		Group createdGroup = dao.create(domain1, group);
		Group.Id createdGroupId = createdGroup.getUid();
		Group createdSubGroup = dao.create(domain1, subGroup);
		Group.Id createdSubGroupId = createdSubGroup.getUid();

		dao.addUser(domain1, createdSubGroupId, user1);
		dao.addSubgroup(domain1, group.getExtId(), subGroup.getExtId());

		assertThat(getUserGroupsFromInternalMapping(user1.getUid())).isEqualTo(ImmutableSet.of(
				createdGroupId.getId(),
				createdSubGroupId.getId()
		));
	}

	@Test
	public void testRemoveSubGroupUpdatesInternalMappings() throws Exception {
		Group group = Group
				.builder()
				.name("group")
				.extId(GroupExtId.valueOf("extIdGroup"))
				.email("group@domain")
				.build();
		Group subGroup = Group
				.builder()
				.name("subgroup")
				.extId(GroupExtId.valueOf("extIdSubGroup"))
				.email("subgroup@domain")
				.build();

		dao.create(domain1, group);

		Group createdSubGroup = dao.create(domain1, subGroup);
		Group.Id createdSubGroupId = createdSubGroup.getUid();

		dao.addUser(domain1, createdSubGroupId, user1);
		dao.addSubgroup(domain1, group.getExtId(), subGroup.getExtId());
		dao.removeSubgroup(domain1, group.getExtId(), subGroup.getExtId());

		assertThat(getUserGroupsFromInternalMapping(user1.getUid())).isEqualTo(ImmutableSet.of(
				createdSubGroupId.getId()
		));
	}

	@Test
	public void testRemoveUserUpdatesInternalMappings() throws Exception {
		Group group1 = Group
				.builder()
				.name("group1")
				.extId(GroupExtId.valueOf("extIdGroup1"))
				.email("group1@domain")
				.build();
		Group group2 = Group
				.builder()
				.name("group2")
				.extId(GroupExtId.valueOf("extIdGroup2"))
				.email("group2@domain")
				.build();

		Group createdGroup1 = dao.create(domain1, group1);
		Group createdGroup2 = dao.create(domain1, group2);
		Group.Id createdGroup1Id = createdGroup1.getUid();
		Group.Id createdGroup2Id = createdGroup2.getUid();

		dao.addUser(domain1, createdGroup1Id, user1);
		dao.addUser(domain1, createdGroup2Id, user1);
		dao.removeUser(domain1, createdGroup1.getExtId(), user1);

		assertThat(getUserGroupsFromInternalMapping(user1.getUid())).isEqualTo(ImmutableSet.of(
				createdGroup2Id.getId()
		));
	}

	@Test
	public void testListPublicGroups() throws Exception {
		Set<Group> groups = dao.listPublicGroups(domain2);
		Set<Group> expectedGroups = ImmutableSet
				.<Group>builder()
				.add(Group
						.builder()
						.uid(Group.Id.valueOf(22))
						.name("group1")
						.description("group1-description")
						.extId(GroupExtId.valueOf("group1-id"))
						.build())
				.add(Group
						.builder()
						.uid(Group.Id.valueOf(23))
						.name("group2")
						.description("group2-description")
						.extId(GroupExtId.valueOf("group2-id"))
						.build())
				.build();

		assertThat(groups).isEqualTo(expectedGroups);
	}
	
	@Test
	public void testGetAllGroupsForUserExtId() throws Exception {
		Group group1 = dao.create(user1.getDomain(),
				Group.builder()
				.name("group1")
				.extId(GroupExtId.valueOf("extIdGroup1"))
				.privateGroup(true)
				.email("group1@domain").build());

		Group group2 = dao.create(user1.getDomain(),
				Group.builder()
				.name("group2")
				.extId(GroupExtId.valueOf("extIdGroup2"))
				.privateGroup(true)
				.email("group2@domain").build());

		ImmutableSet.Builder<Group> expectedGroupsBuilder = ImmutableSet.builder();

		expectedGroupsBuilder
				.add(group1)
				.add(group2);

		dao.addUser(domain1, group1.getUid(), user1);
		dao.addUser(domain1, group2.getUid(), user1);

		Set<Group> groups = dao.getAllGroupsForUserExtId(user1.getDomain(), user1.getExtId());

		assertThat(groups).isEqualTo(expectedGroupsBuilder.build());
	}

	private Set<Integer> getUserGroupsFromInternalMapping(int userId) throws Exception {
		ImmutableSet.Builder<Integer> groupIds = ImmutableSet.builder();
		ResultSet rs = db.execute("SELECT of_usergroup_group_id FROM of_usergroup WHERE of_usergroup_user_id = ?", userId);

		while (rs.next()) {
			groupIds.add(rs.getInt(1));
		}

		return groupIds.build();
	}
    /**
     * Helper function to make sure the base properties of the groups are correct
     *
     * @param prefix        The group prefix, which is the extId and the prefix for the base fields
     * @param group         The group to check.
     */
    private void testGroupBase(String prefix, Group group) {
        assertThat(group.getName()).isEqualTo(prefix + "-name");
        assertThat(group.getDescription()).isEqualTo(prefix + "-description");
        assertThat(group.getExtId().getId()).isEqualTo(prefix);
    }

    /**
     * Make sure the given group is expanded to a certain depth. If the depth
     * is greater than 1, each subgroup must have the same depth.
     *
     * @param group     The group to check
     * @param depth     0  -> Group should have no subgroups
     *                  1  -> Just the group itself should have subgroups
     *                  >1 -> Subgroups will recursively be checked for the depth
     */
    private void assertExpandedToDepth(Group group, int depth) {
        Set<Group> subgroups = group.getSubgroups();
        if (depth > 0) {
            assertThat(subgroups.size()).isGreaterThan(0);
            for (Group sub : group.getSubgroups()) {
                assertExpandedToDepth(sub, depth - 1);
            }
        } else {
            assertThat(subgroups.size()).isEqualTo(0);
        }
    }
}
