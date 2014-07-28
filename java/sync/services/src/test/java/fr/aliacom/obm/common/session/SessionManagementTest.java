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
package fr.aliacom.obm.common.session;


import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.Login;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.server.auth.AuthentificationServiceFactory;
import org.obm.sync.server.auth.IAuthentificationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.DomainNotFoundException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import fr.aliacom.obm.services.constant.SpecialAccounts;


public class SessionManagementTest {

	private SessionManagement sessionManagement;
	private SpecialAccounts specialAccounts;
	private ObmSyncConfigurationService configurationService;
	private UserDao userDao;
	private DomainService domainService;
	private AuthentificationServiceFactory authentificationServiceFactory;
	private IMocksControl control;
	private IAuthentificationService authenticationService;
	private ObmDomain obmDomain;
	private ObmUser obmUser;
	
	private final static String DOMAIN = "test.tlse.lng";
	private final static String LOGIN = "user";
	private final static String LOGINATDOMAIN = "user@test.tlse.lng";
	private final static String TRUSTEDIP = "1.2.3.4";

	@Before
	public void setup() {
		control = createControl();
		authentificationServiceFactory = control.createMock(AuthentificationServiceFactory.class);
		domainService = control.createMock(DomainService.class);
		userDao = control.createMock(UserDao.class);
		configurationService = control.createMock(ObmSyncConfigurationService.class);
		specialAccounts = control.createMock(SpecialAccounts.class);
		authenticationService = control.createMock(IAuthentificationService.class);
		sessionManagement = createMockBuilder(SessionManagement.class)
				.withConstructor(authentificationServiceFactory, domainService, userDao, configurationService, specialAccounts)
				.addMockedMethod("getObmSyncVersion")
				.createMock(control);
		obmDomain = ToolBox.getDefaultObmDomain();
		obmUser = ToolBox.getDefaultObmUser();
	}
	
	@Test
	public void prepareLoginLemonComesFirst() throws DomainNotFoundException {
		control.replay();
		
		Login actualLogin = sessionManagement.prepareLogin("specifiedLogin@domain", "lemonLogin", "lemonDomain");
		
		control.verify();
		assertThat(actualLogin).isEqualTo(Login.builder().login("lemonLogin").domain("lemonDomain").build());
	}
	
	@Test
	public void prepareLoginLemonComesFirstIgnoringNullLogin() throws DomainNotFoundException {
		control.replay();
		
		Login actualLogin = sessionManagement.prepareLogin(null, "lemonLogin", "lemonDomain");
		
		control.verify();
		assertThat(actualLogin).isEqualTo(Login.builder().login("lemonLogin").domain("lemonDomain").build());
	}
	
	@Test
	public void prepareLoginFallbackToSpecifiedLoginWhenLemonDomainIsNull() throws DomainNotFoundException {
		control.replay();
		
		Login actualLogin = sessionManagement.prepareLogin("specifiedLogin@domain", "lemonLogin", null);
		
		control.verify();
		assertThat(actualLogin).isEqualTo(Login.builder().login("specifiedLogin").domain("domain").build());
	}
	
	@Test
	public void prepareLoginFallbackToSpecifiedLoginWhenLemonLoginIsNull() throws DomainNotFoundException {
		control.replay();
		
		Login actualLogin = sessionManagement.prepareLogin("specifiedLogin@domain", null, "lemonDomain");
		
		control.verify();
		assertThat(actualLogin).isEqualTo(Login.builder().login("specifiedLogin").domain("domain").build());
	}
	
	@Test
	public void prepareLoginSpecifiedLoginWhenLemonNotProvided() throws DomainNotFoundException {
		control.replay();
		
		Login actualLogin = sessionManagement.prepareLogin("specifiedLogin@domain", null, null);
		
		control.verify();
		assertThat(actualLogin).isEqualTo(Login.builder().login("specifiedLogin").domain("domain").build());
	}
	
	@Test(expected=IllegalStateException.class)
	public void prepareLoginNoLoginAtAll() throws DomainNotFoundException {
		control.replay();
		
		try {
			sessionManagement.prepareLogin(null, null, null);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void prepareLoginLoginWithoutDomain() throws DomainNotFoundException {
		expect(userDao.getUniqueObmDomain("login")).andReturn(null);
		control.replay();
		
		Login actualLogin = sessionManagement.prepareLogin("login", null, null);
		control.verify();
		assertThat(actualLogin).isEqualTo(Login.builder().login("login").build());
	}
	
	@Test
	public void prepareLoginLoginWithoutProvidedDomainButFoundWithService() throws DomainNotFoundException {
		expect(userDao.getUniqueObmDomain("login")).andReturn("domain");
		control.replay();
		
		Login actualLogin = sessionManagement.prepareLogin("login", null, null);
		control.verify();
		assertThat(actualLogin).isEqualTo(Login.builder().login("login").domain("domain").build());
	}

	@Test
	public void testLoginWithoutPasswordFromTrustedLemonIP() throws DomainNotFoundException {
		expect(authentificationServiceFactory.get()).andReturn(authenticationService);
		expect(authenticationService.getType()).andReturn("TestAuthService");
		expect(domainService.findDomainByName(DOMAIN)).andReturn(obmDomain);
		expect(configurationService.getLemonLdapIps()).andReturn(ImmutableSet.of(TRUSTEDIP));
		expect(userDao.findUserByLogin(LOGIN, obmDomain)).andReturn(obmUser);
		expect(sessionManagement.getObmSyncVersion()).andReturn(new MavenVersion("2", "5", "0"));
		expect(userDao.loadUserProperties(isA(AccessToken.class))).andReturn(ImmutableMap.<String, String>of());
		control.replay();

		AccessToken token = sessionManagement.login(LOGIN, null, "test", TRUSTEDIP, TRUSTEDIP, LOGIN, DOMAIN, false);

		assertThat(token).isNotNull();

		control.verify();
	}
	
	@Test
	public void testLoginIsLoweredBeforeAuthServiceUsage() throws DomainNotFoundException {
		expect(authentificationServiceFactory.get()).andReturn(authenticationService);
		expect(authenticationService.getType()).andReturn("TestAuthService");
		expect(userDao.getUniqueObmDomain(LOGIN)).andReturn(DOMAIN);
		expect(domainService.findDomainByName(DOMAIN)).andReturn(obmDomain);
		expect(specialAccounts.isRootAccount(LOGINATDOMAIN, null)).andReturn(false);
		expect(specialAccounts.isAnyUserAccount(null)).andReturn(true);
		expect(userDao.findUserByLogin(LOGIN, obmDomain)).andReturn(obmUser);
		expect(sessionManagement.getObmSyncVersion()).andReturn(new MavenVersion("2", "5", "0"));
		expect(userDao.loadUserProperties(isA(AccessToken.class))).andReturn(ImmutableMap.<String, String>of());
		
		control.replay();

		AccessToken token = sessionManagement.login("UseR", null, "test", null, null, null, DOMAIN, false);
		assertThat(token).isNotNull();

		control.verify();
	}
}
