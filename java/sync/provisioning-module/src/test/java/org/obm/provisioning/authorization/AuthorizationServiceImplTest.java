/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.Collection;

import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.domain.dao.DomainDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
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

@RunWith(GuiceRunner.class)
@GuiceModule(AuthorizationServiceImplTest.Env.class)
public class AuthorizationServiceImplTest {
	
	public static class Env extends AbstractModule {

		IMocksControl mocksControl = createControl();
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			bind(PermissionDao.class).toInstance(mocksControl.createMock(PermissionDao.class));
			bind(ProfileDao.class).toInstance(mocksControl.createMock(ProfileDao.class));
			bind(DomainDao.class).toInstance(mocksControl.createMock(DomainDao.class));
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
	DomainDao domainDao;
	@Inject
	AuthorizationService authService;
	
	ObmDomainUuid dummyUuid = ObmDomainUuid.of("fc72d37d-b82d-45be-b05d-f186e88e7a26");
	ObmDomain dummyDomain = ObmDomain.builder().name("dummydomain.obm.org")
			.uuid(dummyUuid).build();
	ProfileName adminProfile = ProfileName.builder().name("admin").build();
	
	Collection<String> adminRoles = ImmutableList.of("batch_all", "user_all", "group_all", "profile_all");

	@Test(expected = AuthorizationException.class)
	public void testGetPermissionsOnProfileDaoException() throws Exception {
		
		DaoException daoEx = new DaoException();
		
		expect(domainDao.findDomainByName(dummyDomain.getName())).andReturn(dummyDomain).atLeastOnce();
		expect(profileDao.getUserProfileName("user1", dummyUuid)).andThrow(daoEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain.getName());
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
		
		expect(domainDao.findDomainByName(dummyDomain.getName())).andReturn(dummyDomain).atLeastOnce();
		expect(profileDao.getUserProfileName("user1", dummyUuid)).andReturn(adminProfile).once();
		expect(permissionsDao.getPermissionsForProfile(adminProfile, dummyDomain)).andThrow(daoEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain.getName());
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
		
		expect(domainDao.findDomainByName(dummyDomain.getName())).andReturn(dummyDomain).atLeastOnce();
		expect(profileDao.getUserProfileName("user1", dummyUuid)).andThrow(userEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain.getName());
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
		
		expect(domainDao.findDomainByName(dummyDomain.getName())).andReturn(dummyDomain).atLeastOnce();
		expect(profileDao.getUserProfileName("user1", dummyUuid)).andReturn(adminProfile).once();
		expect(permissionsDao.getPermissionsForProfile(adminProfile, dummyDomain)).andThrow(permEx).once();
		mocksControl.replay();
		
		try {
			authService.getPermissions("user1", dummyDomain.getName());
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
		expect(domainDao.findDomainByName(dummyDomain.getName())).andReturn(dummyDomain).atLeastOnce();
		expect(profileDao.getUserProfileName("user1", dummyUuid)).andReturn(adminProfile);
		expect(permissionsDao.getPermissionsForProfile(adminProfile, dummyDomain)).andReturn(adminRoles);
		mocksControl.replay();
		Collection<String> roles = authService.getPermissions("user1", dummyDomain.getName());
		assertThat(roles).isEqualTo(adminRoles);
		mocksControl.verify();
	}
	
}
