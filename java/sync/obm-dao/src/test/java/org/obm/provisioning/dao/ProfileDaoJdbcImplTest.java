package org.obm.provisioning.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.beans.ProfileEntry;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.sync.Right;

import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.profile.CheckBoxState;
import fr.aliacom.obm.common.profile.Module;
import fr.aliacom.obm.common.profile.ModuleCheckBoxStates;
import fr.aliacom.obm.common.profile.Profile;
import fr.aliacom.obm.common.profile.Profile.AccessRestriction;
import fr.aliacom.obm.common.profile.Profile.AdminRealm;
import fr.aliacom.obm.common.profile.Profile.Builder;
import fr.aliacom.obm.common.user.ObmUser;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(ProfileDaoJdbcImplTest.Env.class)
public class ProfileDaoJdbcImplTest implements H2TestClass {

	public static class Env extends DaoTestModule {

		@Override
		protected void configureImpl() {
			bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		}

	}
	
	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}

	@Inject
	private ProfileDao dao;
	
	private final ObmDomainUuid uuid1 = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
	private final ObmDomainUuid uuid2 = ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf");

	@Test
	public void testGetProfileNamesOnNonExistentDomains() throws Exception {
		ObmDomainUuid uuid = ObmDomainUuid.of("892ba641-4ae0-4b40-aa5e-c3fd3acb78bf");
		Set<ProfileEntry> domainProfiles = dao.getProfileEntries(uuid);

		assertThat(domainProfiles).containsOnly(ProfileEntry.builder().domainUuid(uuid).id(4l).build());
	}

	@Test
	public void testGetProfileNamesOnExistingDomains() throws Exception {
		Set<ProfileEntry> firstDomainProfiles = dao.getProfileEntries(uuid1);
		Set<ProfileEntry> secondDomainProfiles = dao.getProfileEntries(uuid2);

		assertThat(firstDomainProfiles).containsOnly(
				ProfileEntry.builder().domainUuid(uuid1).id(1l).build(),
				ProfileEntry.builder().domainUuid(uuid1).id(2l).build(),
				ProfileEntry.builder().domainUuid(uuid1).id(4l).build());
		assertThat(secondDomainProfiles).containsOnly(
				ProfileEntry.builder().domainUuid(uuid2).id(3l).build(),
				ProfileEntry.builder().domainUuid(uuid2	).id(4l).build());
	}

	@Test
	public void testGetProfileNameNameByID() throws Exception {
		ProfileName profileName1 = dao.getProfileName(uuid1, ProfileId.builder().id(1).build());
		ProfileName profileName2 = dao.getProfileName(uuid1, ProfileId.builder().id(2).build());
		ProfileName profileName3 = dao.getProfileName(uuid2, ProfileId.builder().id(3).build());
		
		assertThat(profileName1).isEqualTo(ProfileName.builder().name("admin").build());
		assertThat(profileName2).isEqualTo(ProfileName.builder().name("user").build());
		assertThat(profileName3).isEqualTo(ProfileName.builder().name("editor").build());
	}

	@Test(expected = ProfileNotFoundException.class)
	public void testGetProfileNameNameOnNonExistentID() throws Exception {
		dao.getProfileName(uuid1, ProfileId.builder().id(64).build());
	}

	@Test(expected = ProfileNotFoundException.class)
	public void testGetProfileNameNameOnNonExistentDomain() throws Exception {
		dao.getProfileName(ObmDomainUuid.of("99999999-9999-9999-9999-e50cfbfec5b6"), ProfileId.builder().id(1).build());
	}
	
	@Test
	public void testGetProfileNameForUser() throws Exception {
		ProfileName userProfile = dao.getUserProfileName("user2", uuid1);
		ProfileName adminProfile = dao.getUserProfileName("user2", uuid2);
		
		assertThat(userProfile).isEqualTo(ProfileName.builder().name("user").build());
		assertThat(adminProfile).isEqualTo(ProfileName.builder().name("admin").build());
	}
	
	@Test(expected = UserNotFoundException.class)
	public void testGetProfileNameForNonExistentUser() throws Exception {
		dao.getUserProfileName("non-existent-user", uuid1);
	}

	@Test
	public void testGetWhenProfileDoesntExist() throws Exception {
		assertThat(dao.get(ProfileId.valueOf("666"), ToolBox.getDefaultObmDomain())).isNull();
	}

	@Test
	public void testGet() throws Exception {
		ProfileId id = ProfileId.valueOf("1");
		final Builder profileBuilder = profileBuilderToTest();
		
		Profile profile = profileBuilder
				.id(id)
				.name(ProfileName.valueOf("admin"))
				.build();

		assertThat(dao.get(id, ToolBox.getDefaultObmDomain())).isEqualTo(profile);
	}

	private Builder profileBuilderToTest() {
		final Builder profileBuilderToTest = Profile
				.builder()
				.domain(ToolBox.getDefaultObmDomain())
				.level(0)
				.managePeers(true)
				.accessRestriction(AccessRestriction.ALLOW_ALL)
				.accessExceptions("")
				.adminRealms(AdminRealm.DOMAIN, AdminRealm.DELEGATION)
				.defaultMailQuota(0)
				.maxMailQuota(0)
				.defaultCheckBoxState(Module.CALENDAR, ModuleCheckBoxStates
						.builder()
						.module(Module.CALENDAR)
						.checkBoxState(Right.ACCESS, CheckBoxState.CHECKED)
						.checkBoxState(Right.READ, CheckBoxState.DISABLED_CHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.DISABLED_UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.MAILBOX, ModuleCheckBoxStates
						.builder()
						.module(Module.MAILBOX)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.MAILSHARE, ModuleCheckBoxStates
						.builder()
						.module(Module.MAILSHARE)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.RESOURCE, ModuleCheckBoxStates
						.builder()
						.module(Module.RESOURCE)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.CONTACTS, ModuleCheckBoxStates
						.builder()
						.module(Module.CONTACTS)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build());
		return profileBuilderToTest;
	}

	@Test(expected = UserNotFoundException.class)
	public void testGetUserProfileWhenUserNotFound() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.uid(666)
				.login("lucifer")
				.domain(ToolBox.getDefaultObmDomain())
				.build();

		dao.getUserProfile(user);
	}
	
	@Test
	public void testGetWithInvalidQuota() throws DaoException {
		ProfileId id = ProfileId.valueOf("3");
		final Builder profileBuilder = profileBuilderToTest();
		
		Profile profile = profileBuilder
				.id(id)
				.name(ProfileName.valueOf("editor"))
				.build();

		assertThat(dao.get(id, ToolBox.getDefaultObmDomain())).isEqualTo(profile);
	}
	
	@Test
	public void testGetWithInvalidDefaultRight() throws DaoException {
		ProfileId id = ProfileId.valueOf("4");
		Profile profile = Profile
				.builder()
				.id(id)
				.name(ProfileName.valueOf("superadmin"))
				.domain(ToolBox.getDefaultObmDomain())
				.level(9)
				.managePeers(false)
				.accessRestriction(AccessRestriction.ALLOW_ALL)
				.accessExceptions("")
				.adminRealms(AdminRealm.DOMAIN, AdminRealm.USER)
				.defaultMailQuota(0)
				.maxMailQuota(0)
				.defaultCheckBoxState(Module.CALENDAR, ModuleCheckBoxStates
						.builder()
						.module(Module.CALENDAR)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.MAILBOX, ModuleCheckBoxStates
						.builder()
						.module(Module.MAILBOX)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.MAILSHARE, ModuleCheckBoxStates
						.builder()
						.module(Module.MAILSHARE)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.RESOURCE, ModuleCheckBoxStates
						.builder()
						.module(Module.RESOURCE)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.CONTACTS, ModuleCheckBoxStates
						.builder()
						.module(Module.CONTACTS)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.build();

		assertThat(dao.get(id, ToolBox.getDefaultObmDomain())).isEqualTo(profile);
	}

	@Test
	public void testGetUserProfile() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login("user1")
				.domain(ToolBox.getDefaultObmDomain())
				.build();
		Profile profile = Profile
				.builder()
				.id(ProfileId.valueOf("2"))
				.name(ProfileName.valueOf("user"))
				.domain(ToolBox.getDefaultObmDomain())
				.level(9)
				.managePeers(false)
				.accessRestriction(AccessRestriction.ALLOW_ALL)
				.accessExceptions("")
				.adminRealms(AdminRealm.DOMAIN, AdminRealm.USER)
				.defaultMailQuota(0)
				.maxMailQuota(0)
				.defaultCheckBoxState(Module.CALENDAR, ModuleCheckBoxStates
						.builder()
						.module(Module.CALENDAR)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.MAILBOX, ModuleCheckBoxStates
						.builder()
						.module(Module.MAILBOX)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.MAILSHARE, ModuleCheckBoxStates
						.builder()
						.module(Module.MAILSHARE)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.RESOURCE, ModuleCheckBoxStates
						.builder()
						.module(Module.RESOURCE)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.defaultCheckBoxState(Module.CONTACTS, ModuleCheckBoxStates
						.builder()
						.module(Module.CONTACTS)
						.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.READ, CheckBoxState.UNCHECKED)
						.checkBoxState(Right.WRITE, CheckBoxState.UNCHECKED)
						.build())
				.build();

		assertThat(dao.getUserProfile(user)).isEqualTo(profile);
	}

}