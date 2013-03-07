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
package fr.aliacom.obm.utils;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.UserService;

@RunWith(SlowFilterRunner.class)
public class HelperServiceImplTest {

	private AccessToken accessToken;

	@Before
	public void setUp() {
		accessToken = new AccessToken(1, "o-push");
	}

	@Test
	public void testIsNotSameDomainWhenDifferent() {
		accessToken.setDomain(domainWithName("otherdomain.org"));

		String userEmail = "test@domain.org";
		UserService userService = createStrictMock(UserService.class);
		expect(userService.getDomainNameFromEmail(userEmail)).andReturn("domain.org").once();
		replay(userService);

		boolean isNotSameDomain = new HelperServiceImpl(null, null, userService)
				.isNotSameDomain(accessToken, userEmail);

		verify(userService);
		
		assertThat(isNotSameDomain).isTrue();
	}

	@Test
	public void testIsNotSameDomainWhenSame() {
		accessToken.setDomain(domainWithName("domain.org"));

		String userEmail = "test@domain.org";
		UserService userService = createStrictMock(UserService.class);
		expect(userService.getDomainNameFromEmail(userEmail)).andReturn("domain.org").once();
		replay(userService);

		boolean isNotSameDomain = new HelperServiceImpl(null, null, userService)
				.isNotSameDomain(accessToken, userEmail);

		verify(userService);
		
		assertThat(isNotSameDomain).isFalse();
	}

	@Test
	public void testIsSameDomainWhenNoDomain() {
		accessToken.setDomain(domainWithName("otherdomain.org"));

		String userLogin = "test";
		UserService userService = createStrictMock(UserService.class);
		expect(userService.getDomainNameFromEmail(userLogin)).andReturn(null).once();
		replay(userService);

		boolean isNotSameDomain = new HelperServiceImpl(null, null, userService)
				.isNotSameDomain(accessToken, userLogin);

		verify(userService);
		
		assertThat(isNotSameDomain).isFalse();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenNoCalendar() {
		new HelperServiceImpl(null, null, null).eventBelongsToCalendar(new Event(), null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenEmptyCalendar() {
		new HelperServiceImpl(null, null, null).eventBelongsToCalendar(new Event(), "");
	}
	
	@Test(expected=NullPointerException.class)
	public void testEventBelongsToCalendarWhenNoEvent() {
		new HelperServiceImpl(null, null, null).eventBelongsToCalendar(null, "user@domain.org");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenNoOwnerEmail() {
		Event event = new Event();
		event.setOwnerEmail(null);
		new HelperServiceImpl(null, null, null).eventBelongsToCalendar(event, "user@domain.org");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenEmptyOwnerEmail() {
		Event event = new Event();
		event.setOwnerEmail("");
		new HelperServiceImpl(null, null, null).eventBelongsToCalendar(event, "user@domain.org");
	}

	@Test
	public void testEventBelongsToCalendarWhenOwnerEmailIsDifferentFromCalendar() {
		Event event = new Event();
		event.setOwnerEmail("aguy@domain.org");
		
		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(null, null, null);
		boolean eventBelongsToCalendar = helperServiceImpl.eventBelongsToCalendar(event, "user@domain.org");

		Assertions.assertThat(eventBelongsToCalendar).isFalse();
	}

	@Test
	public void testEventBelongsToCalendarWhenOwnerEmailIsEqualsOfCalendar() {
		Event event = new Event();
		event.setOwnerEmail("user@domain.org");
		
		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(null, null, null);
		boolean eventBelongsToCalendar = helperServiceImpl.eventBelongsToCalendar(event, "user@domain.org");

		Assertions.assertThat(eventBelongsToCalendar).isTrue();
	}

	private ObmDomain domainWithName(String domainName) {
		return ObmDomain
				.builder()
				.name(domainName)
				.build();
	}
}
