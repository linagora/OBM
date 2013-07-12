package org.obm.provisioning.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.exceptions.PermissionsNotFoundException;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(PermissionDaoHardcodedImplTest.Env.class)
public class PermissionDaoHardcodedImplTest {
	
	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bind(PermissionDao.class).to(PermissionDaoHardcodedImpl.class);
		}
	}

	@Inject
	private PermissionDao dao;
	
	ObmDomain dummyDomain = ObmDomain.builder().name("dummydomain.obm.org")
												.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6")).build();
	
	ProfileName dummyProfile = ProfileName.builder().name("dummyprofile").build();
	
	ProfileName adminProfile = ProfileName.builder().name("admin").build();
	ProfileName userProfile = ProfileName.builder().name("user").build();
	ProfileName editorProfile = ProfileName.builder().name("editor").build();
	ProfileName delegateAdminProfile = ProfileName.builder().name("admin_delegue").build();
			
	Collection<String> adminRoles = ImmutableList.of("*:*");
	Collection<String> delegateAdminRoles = ImmutableList.of("batches:read,create,delete", "users:*", "groups:*", "profiles:*");
	Collection<String> userRoles = ImmutableList.of("users:read", "groups:read", "profiles:*");
	Collection<String> editorRoles = ImmutableList.of("users:read", "groups:read", "profiles:*");
	
	@Test(expected = PermissionsNotFoundException.class)
	public void testGetRolesForNonExistentProfile() throws Exception {
		dao.getPermissionsForProfile(dummyProfile, dummyDomain);
	}
	
	@Test
	public void testGetRolesForUser() throws Exception {
		assertThat(dao.getPermissionsForProfile(adminProfile, dummyDomain)).isEqualTo(adminRoles);
		assertThat(dao.getPermissionsForProfile(delegateAdminProfile, dummyDomain)).isEqualTo(delegateAdminRoles);
		assertThat(dao.getPermissionsForProfile(userProfile, dummyDomain)).isEqualTo(userRoles);
		assertThat(dao.getPermissionsForProfile(editorProfile, dummyDomain)).isEqualTo(editorRoles);
	}
}
