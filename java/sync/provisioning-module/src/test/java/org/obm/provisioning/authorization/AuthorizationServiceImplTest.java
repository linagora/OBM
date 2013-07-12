package org.obm.provisioning.authorization;


import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collection;

import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.PermissionDao;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.PermissionsNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(AuthorizationServiceImplTest.Env.class)
public class AuthorizationServiceImplTest {
	
	public static class Env extends AbstractModule {

		IMocksControl mocksControl = createControl();
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			bind(PermissionDao.class).toInstance(mocksControl.createMock(PermissionDao.class));
			bind(ProfileDao.class).toInstance(mocksControl.createMock(ProfileDao.class));
			bind(AuthorizationService.class).to(AuthorizationServiceImpl.class);
		}
	}
	@Inject
	IMocksControl mocksControl;
	@Inject
	ProfileDao profileDao;
	@Inject
	PermissionDao permissionsDao;
	@Inject
	AuthorizationService authService;
	
	ObmDomainUuid dummyUuid = ObmDomainUuid.of("dummyUuid");
	ObmDomain dummyDomain = ObmDomain.builder().name("dummydomain.obm.org")
			.uuid(dummyUuid).build();
	ProfileName adminProfile = ProfileName.builder().name("admin").build();
	
	Collection<String> adminRoles = ImmutableList.of("batch_all", "user_all", "group_all", "profile_all");

	@Test(expected = AuthorizationException.class)
	public void testGetPermissionsOnProfileDaoException() throws Exception {
		
		DaoException daoEx = new DaoException();
		
		expect(profileDao.getProfileForUser("user1", dummyUuid)).andThrow(daoEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain);
		}
		catch (AuthorizationException e) {
			assertThat(e.getCause()).isEqualTo(daoEx);
			assertThat(e.isTechnicalError()).isTrue();
			mocksControl.verify();
			throw(e);
		}
	}
	
	@Test(expected = AuthorizationException.class)
	public void testGetPermissionsOnRoleDaoException() throws Exception {
		
		DaoException daoEx = new DaoException();
		
		expect(profileDao.getProfileForUser("user1", dummyUuid)).andReturn(adminProfile).once();
		expect(permissionsDao.getPermissionsForProfile(adminProfile, dummyDomain)).andThrow(daoEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain);
		}
		catch (AuthorizationException e) {
			assertThat(e.getCause()).isEqualTo(daoEx);
			assertThat(e.isTechnicalError()).isTrue();
			mocksControl.verify();
			throw(e);
		}
	}
	
	@Test(expected = AuthorizationException.class)
	public void testGetPermissionsOnUserNotFoundException() throws Exception {
		
		UserNotFoundException userEx = new UserNotFoundException("user1", dummyUuid);
		
		expect(profileDao.getProfileForUser("user1", dummyUuid)).andThrow(userEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain);
		}
		catch (AuthorizationException e) {
			assertThat(e.getCause()).isEqualTo(userEx);
			assertThat(e.isTechnicalError()).isFalse();
			mocksControl.verify();
			throw(e);
		}
	}
	
	@Test(expected = AuthorizationException.class)
	public void testGetPermissionsOnRoleNotFoundException() throws Exception {
		
		PermissionsNotFoundException permEx = new PermissionsNotFoundException(adminProfile, dummyDomain);
		
		expect(profileDao.getProfileForUser("user1", dummyUuid)).andReturn(adminProfile).once();
		expect(permissionsDao.getPermissionsForProfile(adminProfile, dummyDomain)).andThrow(permEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain);
		}
		catch (AuthorizationException e) {
			assertThat(e.getCause()).isEqualTo(permEx);
			assertThat(e.isTechnicalError()).isFalse();
			mocksControl.verify();
			throw(e);
		}
	}
	
	@Test
	public void testGetPermissions() throws Exception	{
		expect(profileDao.getProfileForUser("user1", dummyUuid)).andReturn(adminProfile);
		expect(permissionsDao.getPermissionsForProfile(adminProfile, dummyDomain)).andReturn(adminRoles);
		mocksControl.replay();
		Collection<String> roles = authService.getPermissions("user1", dummyDomain);
		assertThat(roles).isEqualTo(adminRoles);
		mocksControl.verify();
	}
	
}
