/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.domain.dao.DomainDao;
import org.obm.domain.dao.UserDao;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;
import org.obm.sync.server.auth.AuthentificationServiceFactory;
import org.obm.sync.server.auth.IAuthentificationService;
import org.obm.sync.server.auth.impl.DatabaseAuthentificationService;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;


public class LoginBindingImplTest {

	private IMocksControl control;
	private IAuthentificationService authentificationService;
	private AuthentificationServiceFactory authentificationServiceFactory;
	private DatabaseAuthentificationService databaseAuthentificationService;
	private ObmSyncConfigurationService obmSyncConfigurationService;
	private DomainDao domainDao;
	private UserDao userDao;
	private LoginBindingImpl loginBindingImpl;

	@Before
	public void setup() {
		control = createControl();
		authentificationService = control.createMock(IAuthentificationService.class);
		authentificationServiceFactory = control.createMock(AuthentificationServiceFactory.class);
		expect(authentificationServiceFactory.get()).andReturn(authentificationService).anyTimes();
		
		obmSyncConfigurationService = control.createMock(ObmSyncConfigurationService.class);
		databaseAuthentificationService = control.createMock(DatabaseAuthentificationService.class);
		domainDao = control.createMock(DomainDao.class);
		userDao = control.createMock(UserDao.class);
		
		loginBindingImpl = new LoginBindingImpl(null, authentificationServiceFactory, databaseAuthentificationService, obmSyncConfigurationService, domainDao, userDao);
	}
	
	@Test
	public void testAuthenticateGlobalAdmin() throws AuthFault {
		String domain = "global.virt";
		expect(obmSyncConfigurationService.getGlobalDomain())
			.andReturn(domain);
		
		String login = "admin0";
		String password = "admin";
		expect(databaseAuthentificationService.doAuth(
			Credentials.builder()
				.login(login)
				.domain(domain)
				.hashedPassword(false)
				.password(password)
				.build()))
			.andReturn(true);
		
		control.replay();
		boolean authenticated = loginBindingImpl.authenticateGlobalAdmin(login, password, "origin", false);
		control.verify();
		               
		assertThat(authenticated).isTrue();
	}

	@Test
	public void testAuthenticateAdminOnGlobalDomain() throws AuthFault {
		String domainName = "global.virt";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.id(1)
				.alias(domainName)
				.uuid(ObmDomainUuid.of("83baccbb-a36d-4602-8516-3e1581a2c4d4"))
				.global(true)
				.build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(obmDomain);
		
		String login = "admin0";
		expect(userDao.findUserByLogin(login, obmDomain))
			.andReturn(ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf(login))
				.domain(obmDomain)
				.admin(true)
				.build());
		
		String password = "admin";
		expect(databaseAuthentificationService.doAuth(
			Credentials.builder()
				.login(login)
				.domain(domainName)
				.hashedPassword(false)
				.password(password)
				.build()))
			.andReturn(true);
		
		control.replay();
		boolean authenticated = loginBindingImpl.authenticateAdmin(login, "admin", "origin", domainName, false);
		control.verify();
		
		assertThat(authenticated).isTrue();
	}
	
	@Test
	public void testAuthenticateAdminOnGlobalDomainNoAdminRights() throws AuthFault {
		String domainName = "global.virt";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.id(1)
				.alias(domainName)
				.uuid(ObmDomainUuid.of("83baccbb-a36d-4602-8516-3e1581a2c4d4"))
				.global(true)
				.build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(obmDomain);
		
		String login = "admin0";
		expect(userDao.findUserByLogin(login, obmDomain))
			.andReturn(ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf(login))
				.domain(obmDomain)
				.admin(false)
				.build());
		
		control.replay();
		boolean authenticated = loginBindingImpl.authenticateAdmin(login, "admin", "origin", domainName, false);
		control.verify();
		
		assertThat(authenticated).isFalse();
	}
	
	@Test
	public void testAuthenticateAdminOnOtherDomain() throws AuthFault {
		String domainName = "domain";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.id(1)
				.alias(domainName)
				.uuid(ObmDomainUuid.of("83baccbb-a36d-4602-8516-3e1581a2c4d4"))
				.build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(obmDomain);
		
		String login = "admin0";
		expect(userDao.findUserByLogin(login, obmDomain))
			.andReturn(ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf(login))
				.domain(obmDomain)
				.admin(true)
				.build());
		
		String password = "admin";
		expect(authentificationService.doAuth(
			Credentials.builder()
				.login(login)
				.domain(domainName)
				.hashedPassword(false)
				.password(password)
				.build()))
			.andReturn(true);
		
		control.replay();
		boolean authenticated = loginBindingImpl.authenticateAdmin(login, password, "origin", domainName, false);
		control.verify();
		
		assertThat(authenticated).isTrue();
	}
	
	@Test
	public void testAuthenticateAdminOnOtherDomainNoAdminRights() throws AuthFault {
		String domainName = "domain";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.id(1)
				.alias(domainName)
				.uuid(ObmDomainUuid.of("83baccbb-a36d-4602-8516-3e1581a2c4d4"))
				.build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(obmDomain);
		
		String login = "admin0";
		expect(userDao.findUserByLogin(login, obmDomain))
			.andReturn(ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf(login))
				.domain(obmDomain)
				.admin(false)
				.build());
		
		control.replay();
		boolean authenticated = loginBindingImpl.authenticateAdmin(login, "admin", "origin", domainName, false);
		control.verify();
		
		assertThat(authenticated).isFalse();
	}
}
