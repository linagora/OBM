/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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


package org.obm.imap.archive.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.domain.dao.DomainDao;
import org.obm.domain.dao.UserDao;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserLogin;

public class AuthorizationServiceImplTest {

	private IMocksControl control;
	
	private DomainDao domainDao;
	private UserDao userDao;
	private AuthorizationServiceImpl testee;
	
	@Before
	public void setup() {
		control = createControl();
		
		domainDao = control.createMock(DomainDao.class);
		userDao = control.createMock(UserDao.class);
		
		testee = new AuthorizationServiceImpl(userDao, domainDao);
	}
	
	@Test
	public void getRolesShouldReturnAdminWhenAdmin() {
		String domainName = "mydomain.org";
		ObmDomain domain = ObmDomain.builder().name(domainName).build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(domain);
		
		String login = "admin";
		expect(userDao.findUserByLogin(login, domain))
			.andReturn(ObmUser.builder()
					.extId(UserExtId.valueOf("2ba43b7a-9c29-477a-8249-9c728ecb72a1"))
					.login(UserLogin.valueOf(login))
					.domain(domain)
					.admin(true)
					.build());
		
		control.replay();
		List<Authorization> roles = testee.getRoles(login, domainName);
		control.verify();
		
		assertThat(roles).containsOnly(Authorization.ADMIN, Authorization.USER, Authorization.NONE);
	}
	
	@Test
	public void getRolesShouldReturnUserWhenCommonUser() {
		String domainName = "mydomain.org";
		ObmDomain domain = ObmDomain.builder().name(domainName).build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(domain);
		
		String login = "admin";
		expect(userDao.findUserByLogin(login, domain))
			.andReturn(ObmUser.builder()
					.extId(UserExtId.valueOf("2ba43b7a-9c29-477a-8249-9c728ecb72a1"))
					.login(UserLogin.valueOf(login))
					.domain(domain)
					.build());
		
		control.replay();
		List<Authorization> roles = testee.getRoles(login, domainName);
		control.verify();
		
		assertThat(roles).containsOnly(Authorization.USER, Authorization.NONE);
	}
	
	@Test
	public void getRolesShouldReturnNoneWhenUnknownUser() {
		String domainName = "mydomain.org";
		ObmDomain domain = ObmDomain.builder().name(domainName).build();
		expect(domainDao.findDomainByName(domainName))
			.andReturn(domain);
		
		String login = "admin";
		expect(userDao.findUserByLogin(login, domain))
			.andReturn(null);
		
		control.replay();
		List<Authorization> roles = testee.getRoles(login, domainName);
		control.verify();
		
		assertThat(roles).containsOnly(Authorization.NONE);
	}
}
