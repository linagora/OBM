package org.obm.provisioning.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.exceptions.PermissionsNotFoundException;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class PermissionDaoHardcodedImplTest {
	
	ObmDomain localDomain = ObmDomain.builder().name("dummydomain.obm.org")
			.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
			.global(false)
			.build();
	
	ObmDomain globalDomain = ObmDomain.builder().name("global.virt")
			.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec555"))
			.global(true)
			.build();
	
	ProfileName dummyProfile = ProfileName.builder().name("dummyprofile").build();
	
	ProfileName adminProfile = ProfileName.builder().name("admin").build();
	ProfileName userProfile = ProfileName.builder().name("user").build();
	ProfileName editorProfile = ProfileName.builder().name("editor").build();
	ProfileName delegateAdminProfile = ProfileName.builder().name("admin_delegue").build();
			
	Collection<String> globalAdminPerms = ImmutableList.of("*:*:*");
	Collection<String> globalDelegateAdminPerms = ImmutableList.of("*:batches:read,create,delete", "*:users:*", "*:groups:*", "*:profiles:*");
	Collection<String> globalUserPerms = ImmutableList.of("*:users:read", "*:groups:read", "*:profiles:*");
	Collection<String> globalEditorPerms = ImmutableList.of("*:users:read", "*:groups:read", "*:profiles:*");
	
	Collection<String> localAdminPerms = ImmutableList.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:*:*");
	Collection<String> localDelegateAdminPerms = ImmutableList.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:batches:read,create,delete",
																   "ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:users:*",
																   "ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:groups:*",
																   "ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:profiles:*");
	Collection<String> localUserPerms = ImmutableList.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:users:read",
														  "ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:groups:read",
														  "ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:profiles:*");
	Collection<String> localEditorPerms = ImmutableList.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:users:read",
															"ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:groups:read",
															"ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6:profiles:*");


	@Inject PermissionDao dao;
	
	@Before
	public void setUp() {
		dao = new PermissionDaoHardcodedImpl();
	}
	
	@Test(expected = PermissionsNotFoundException.class)
	public void testGetPermissionsForNonExistentProfile() throws Exception {
		dao.getPermissionsForProfile(dummyProfile, localDomain);
		assertThat(true).isFalse();
	}
	
	@Test
	public void testGetPermissionsForGlobalUser() throws Exception {
		assertThat(dao.getPermissionsForProfile(adminProfile, globalDomain)).isEqualTo(globalAdminPerms);
		assertThat(dao.getPermissionsForProfile(delegateAdminProfile, globalDomain)).isEqualTo(globalDelegateAdminPerms);
		assertThat(dao.getPermissionsForProfile(userProfile, globalDomain)).isEqualTo(globalUserPerms);
		assertThat(dao.getPermissionsForProfile(editorProfile, globalDomain)).isEqualTo(globalEditorPerms);
	}
	
	@Test
	public void testGetRolesForLocalUser() throws Exception {
		assertThat(dao.getPermissionsForProfile(adminProfile, localDomain)).isEqualTo(localAdminPerms);
		assertThat(dao.getPermissionsForProfile(delegateAdminProfile, localDomain)).isEqualTo(localDelegateAdminPerms);
		assertThat(dao.getPermissionsForProfile(userProfile, localDomain)).isEqualTo(localUserPerms);
		assertThat(dao.getPermissionsForProfile(editorProfile, localDomain)).isEqualTo(localEditorPerms);
	}
}
