/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package fr.aliacom.obm.ldap;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;

import fr.aliacom.obm.common.user.UserPassword;

public class LDAPAuthServiceTest {

	private LDAPAuthService service;
	private LDAPUtilsFactory ldapUtilsFactory;

	private final IMocksControl mocksControl = createControl();
	private final LDAPDirectory directory = new LDAPDirectory("uri", "u=%u,d=%d", null, null, "dc=local", null, null);
	private final Credentials credentials = Credentials
			.builder()
			.login("login")
			.password(UserPassword.valueOf("password"))
			.hashedPassword(false)
			.domain("domain")
			.build();
	private final Credentials credentialsWithHashedPassword = Credentials
			.builder()
			.login("login")
			.password(UserPassword.valueOf("letsSayThisIsAHash"))
			.hashedPassword(true)
			.domain("domain")
			.build();

	@Before
	public void setUp() {
		ldapUtilsFactory = mocksControl.createMock(LDAPUtilsFactory.class);
		service = new LDAPAuthService(directory, ldapUtilsFactory);
	}

	@After
	public void tearDown() {
		mocksControl.verify();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDoAuthWithHashedPassword() throws Exception {
		mocksControl.replay();

		service.doAuth(credentialsWithHashedPassword);
	}

	@Test(expected = RuntimeException.class)
	public void testDoAuthWhenSearchFails() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);

		expect(utils.getConnection()).andThrow(new NamingException());
		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test(expected = AuthFault.class)
	public void testDoAuthWhenSearchReturnsNoResult() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		DirContext context = mocksControl.createMock(DirContext.class);

		expect(utils.getConnection()).andReturn(context);
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(null);

		context.close();
		expectLastCall();

		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test(expected = RuntimeException.class)
	public void testDoAuthWhenBindFails() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		LDAPUtils bindUtils = expectLDAPUtils("uid=login,dc=local", UserPassword.valueOf("password"));
		DirContext context = mocksControl.createMock(DirContext.class);
		SearchResult result = new SearchResult("uid=login", null, null);

		expect(utils.getConnection()).andReturn(context);
		expect(bindUtils.getConnection()).andThrow(new NamingException());
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(result);

		context.close();
		expectLastCall();

		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test(expected = AuthFault.class)
	public void testDoAuthWhenBindCannotAuthenticate() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		LDAPUtils bindUtils = expectLDAPUtils("uid=login,dc=local", UserPassword.valueOf("password"));
		DirContext context = mocksControl.createMock(DirContext.class);
		SearchResult result = new SearchResult("uid=login", null, null);

		expect(utils.getConnection()).andReturn(context);
		expect(bindUtils.getConnection()).andThrow(new AuthenticationException());
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(result);

		context.close();
		expectLastCall();

		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test
	public void testDoAuth() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		LDAPUtils bindUtils = expectLDAPUtils("uid=login,dc=local", UserPassword.valueOf("password"));
		DirContext context = mocksControl.createMock(DirContext.class);
		SearchResult result = new SearchResult("uid=login", null, null);

		expect(utils.getConnection()).andReturn(context);
		expect(bindUtils.getConnection()).andReturn(context);
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(result);

		context.close();
		expectLastCall().times(2);

		mocksControl.replay();

		service.doAuth(credentials);
	}

	private LDAPUtils expectLDAPUtils(String dn, UserPassword password) {
		LDAPUtils utils = mocksControl.createMock(LDAPUtils.class);

		expect(ldapUtilsFactory.create("uri", dn, password, "dc=local")).andReturn(utils);

		return utils;
	}

}
