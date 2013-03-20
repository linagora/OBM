/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.common.user;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;

@RunWith(SlowFilterRunner.class)
public class UserServiceImplTest {

	@Test(expected=IllegalArgumentException.class)
	public void testGetUserFromInvalidCalendar() throws FindException {
		String domainName = "aDomain";

		DomainService domainService = EasyMock.createMock(DomainService.class);
		UserDao userDao = EasyMock.createMock(UserDao.class);

		EasyMock.replay(domainService, userDao);

		new UserServiceImpl(domainService, userDao).getUserFromCalendar("@@", domainName);
	}

	@Test(expected=FindException.class)
	public void testGetUserFromCalendarWithNoDomainFound() throws FindException {
		String domainName = "aDomain";
		String calendar = "aCalendar";

		DomainService domainService = EasyMock.createMock(DomainService.class);
		expect(domainService.findDomainByName(domainName)).andReturn(null).once();

		UserDao userDao = EasyMock.createMock(UserDao.class);

		EasyMock.replay(domainService, userDao);

		new UserServiceImpl(domainService, userDao).getUserFromCalendar(calendar, domainName);
	}

	@Test(expected=FindException.class)
	public void testGetNullUserFromCalendar() throws FindException {
		String domainName = "aDomain";
		String calendar = "aCalendar";
		ObmDomain obmDomain = ObmDomain.builder().build();

		DomainService domainService = EasyMock.createMock(DomainService.class);
		expect(domainService.findDomainByName(domainName)).andReturn(obmDomain).once();

		UserDao userDao = EasyMock.createMock(UserDao.class);
		expect(userDao.findUser("acalendar", obmDomain)).andReturn(null).once();

		EasyMock.replay(domainService, userDao);

		new UserServiceImpl(domainService, userDao).getUserFromCalendar(calendar, domainName);
	}

	@Test
	public void testGetUserFromCalendar() throws FindException {
		String userEmail = "User@domain";
		String domainName = "domain";
		ObmDomain obmDomain = ObmDomain.builder().name(domainName).build();
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(obmDomain);
		obmUser.setEmail(userEmail);

		DomainService domainService = EasyMock.createMock(DomainService.class);
		expect(domainService.findDomainByName(domainName)).andReturn(obmDomain).once();

		UserDao userDao = EasyMock.createMock(UserDao.class);
		expect(userDao.findUser("user@domain", obmDomain)).andReturn(obmUser).once();
		
		EasyMock.replay(domainService, userDao);

		UserServiceImpl userServiceImpl = new UserServiceImpl(domainService, userDao);
		userServiceImpl.getUserFromCalendar(userEmail, domainName);
	}
}
