package org.obm.provisioning.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.beans.ProfileEntry;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(ProfileDaoJdbcImplTest.Env.class)
public class ProfileDaoJdbcImplTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");

			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		}
	}

	@Inject
	private ProfileDao dao;

	@Rule
	@Inject
	public H2InMemoryDatabase db;
	
	ObmDomainUuid uuid1 = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
	ObmDomainUuid uuid2 = ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf");

	@Test
	public void testGetProfilesOnNonExistentDomains() throws Exception {
		Set<ProfileEntry> domainProfiles = dao.getProfiles(ObmDomainUuid.of("892ba641-4ae0-4b40-aa5e-c3fd3acb78bf"));

		assertThat(domainProfiles).isEmpty();
	}
	
	@Test
	public void testGetProfilesOnExistingDomains() throws Exception {
		Set<ProfileEntry> firstDomainProfiles = dao.getProfiles(uuid1);
		Set<ProfileEntry> secondDomainProfiles = dao.getProfiles(uuid2);

		assertThat(firstDomainProfiles).containsOnly(
				ProfileEntry.builder().domainUuid(uuid1).id(1l).build(),
				ProfileEntry.builder().domainUuid(uuid1).id(2l).build());
		assertThat(secondDomainProfiles).containsOnly(
				ProfileEntry.builder().domainUuid(uuid2).id(3l).build());
	}
	
	@Test
	public void testGetProfileNameByID() throws Exception {
		ProfileName profileName1 = dao.getProfile(uuid1, ProfileId.builder().id(1).build());
		ProfileName profileName2 = dao.getProfile(uuid1, ProfileId.builder().id(2).build());
		ProfileName profileName3 = dao.getProfile(uuid2, ProfileId.builder().id(3).build());
		
		assertThat(profileName1).isEqualTo(ProfileName.builder().name("admin").build());
		assertThat(profileName2).isEqualTo(ProfileName.builder().name("user").build());
		assertThat(profileName3).isEqualTo(ProfileName.builder().name("editor").build());
	}
	
	@Test(expected = ProfileNotFoundException.class)
	public void testGetProfileNameOnNonExistentID() throws Exception {
		dao.getProfile(uuid1, ProfileId.builder().id(64).build());
	}
	
	@Test(expected = ProfileNotFoundException.class)
	public void testGetProfileNameOnNonExistentDomain() throws Exception {
		dao.getProfile(ObmDomainUuid.of("99999999-9999-9999-9999-e50cfbfec5b6"), ProfileId.builder().id(1).build());
	}
	
	@Test
	public void testGetProfileForUser() throws Exception {
		ProfileName userProfile = dao.getProfileForUser("user2", uuid1);
		ProfileName adminProfile = dao.getProfileForUser("user2", uuid2);
		
		assertThat(userProfile).isEqualTo(ProfileName.builder().name("user").build());
		assertThat(adminProfile).isEqualTo(ProfileName.builder().name("admin").build());
	}
	
	@Test(expected = UserNotFoundException.class)
	public void testGetProfileForNonExistentUser() throws Exception {
		dao.getProfileForUser("non-existent-user", uuid1);
	}
}