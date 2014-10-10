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
package fr.aliacom.obm.utils;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Set;

import org.easymock.IMocksControl;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.sync.Right;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.date.DateProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserServiceImpl;

@RunWith(SlowFilterRunner.class)
public class HelperServiceImplTest {

	private static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(DomainService.class);
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bindWithMock(HelperDao.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
			bind(UserService.class).to(UserServiceImpl.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(Env.class);
	
	@Inject
	private UserService userService;

	@Inject
	private HelperDao helperDao;

	@Inject
	private IMocksControl mocksControl;

	private HelperServiceImpl helperService;
	private AccessToken accessToken;

	@Before
	public void setUp() {
		accessToken = new AccessToken(1, "o-push");
		helperService = new HelperServiceImpl(null, userService);
	}

	@Test
	public void testIsNotSameDomainWhenDifferent() {
		accessToken.setDomain(domainWithName("otherdomain.org"));

		assertThat(helperService.isSameDomain(accessToken, "test@domain.org")).isFalse();
	}

	@Test
	public void testIsSameDomainWhenSame() {
		accessToken.setDomain(domainWithName("domain.org"));

		assertThat(helperService.isSameDomain(accessToken, "test@domain.org")).isTrue();
	}
	
	@Test
	public void testIsSameDomainWhenSameButCaseDiffers() {
		accessToken.setDomain(domainWithName("DOmaiN.org"));

		assertThat(helperService.isSameDomain(accessToken, "test@domain.org")).isTrue();
	}

	@Test
	public void testIsSameDomainWhenNoDomain() {
		accessToken.setDomain(domainWithName("domain.org"));

		assertThat(helperService.isSameDomain(accessToken, "test")).isTrue();
	}
	
	@Test
	public void testIsSameDomainWhenDomainHasOneAlias(){
		accessToken.setDomain(domainWithAliases("domain.org", ImmutableSet.of("alias.org")));

		assertThat(helperService.isSameDomain(accessToken, "test@alias.org")).isTrue();
	}
	
	@Test
	public void testIsSameDomainWhenDomainHasMultipleAliases(){
		accessToken.setDomain(domainWithAliases("domain.org", ImmutableSet.of("alias.org", "alias2.org")));
		
		assertThat(helperService.isSameDomain(accessToken, "test@alias2.org")).isTrue();
	}
	
	@Test
	public void testIsSameDomainWhenDomainHasOneAliasButCaseDiffers(){
		accessToken.setDomain(domainWithAliases("domain.org", ImmutableSet.of("AliAs.org")));

		assertThat(helperService.isSameDomain(accessToken, "test@alias.org")).isTrue();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenNoCalendar() {
		new HelperServiceImpl(null, null).eventBelongsToCalendar(new Event(), null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenEmptyCalendar() {
		new HelperServiceImpl(null, null).eventBelongsToCalendar(new Event(), "");
	}
	
	@Test(expected=NullPointerException.class)
	public void testEventBelongsToCalendarWhenNoEvent() {
		new HelperServiceImpl(null, null).eventBelongsToCalendar(null, "user@domain.org");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenNoOwnerEmail() {
		Event event = new Event();
		event.setOwnerEmail(null);
		new HelperServiceImpl(null, null).eventBelongsToCalendar(event, "user@domain.org");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEventBelongsToCalendarWhenEmptyOwnerEmail() {
		Event event = new Event();
		event.setOwnerEmail("");
		new HelperServiceImpl(null, null).eventBelongsToCalendar(event, "user@domain.org");
	}

	@Test
	public void testEventBelongsToCalendarWhenOwnerEmailIsDifferentFromCalendar() {
		Event event = new Event();
		event.setOwnerEmail("aguy@domain.org");
		
		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(null, null);
		boolean eventBelongsToCalendar = helperServiceImpl.eventBelongsToCalendar(event, "user@domain.org");

		Assertions.assertThat(eventBelongsToCalendar).isFalse();
	}

	@Test
	public void testEventBelongsToCalendarWhenOwnerEmailIsEqualsOfCalendar() {
		Event event = new Event();
		event.setOwnerEmail("user@domain.org");
		
		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(null, null);
		boolean eventBelongsToCalendar = helperServiceImpl.eventBelongsToCalendar(event, "user@domain.org");

		Assertions.assertThat(eventBelongsToCalendar).isTrue();
	}

	@Test
	public void testListRightsOnCalendarWithUserLogin() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of())).andReturn(
				ImmutableMap.<String, EnumSet<Right>> of()).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getDomainNameFromEmail("foo@bar")).andReturn("bar").once();
		expect(userService.getLoginFromEmail("foo@bar")).andReturn("foo").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		CalendarRights expectedMailToRights = CalendarRights.builder()
				.addRights("foo@bar", EnumSet.of(Right.ACCESS, Right.READ, Right.WRITE))
				.build();
		CalendarRights mailToRights = helperServiceImpl.listRightsOnCalendars(
				accessToken, ImmutableSet.of("foo@bar"));

		Assertions.assertThat(mailToRights).isEqualTo(expectedMailToRights);
		mocksControl.verify();
	}

	@Test
	public void testListRightsOnCalendarWithAdminLogin() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("admin");
		accessToken.setDomain(domainWithName("bar"));
		accessToken.setRootAccount(true);
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of())).andReturn(
				ImmutableMap.<String, EnumSet<Right>> of()).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getDomainNameFromEmail("foo@bar")).andReturn("bar").once();
		expect(userService.getLoginFromEmail("foo@bar")).andReturn("foo").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);

		CalendarRights expectedMailToRights = CalendarRights.builder().
				addRights("foo@bar", EnumSet.of(Right.ACCESS, Right.READ, Right.WRITE))
				.build();
		CalendarRights mailToRights = helperServiceImpl.listRightsOnCalendars(
				accessToken, ImmutableSet.of("foo@bar"));

		Assertions.assertThat(mailToRights).isEqualTo(expectedMailToRights);
		mocksControl.verify();
	}

	@Test
	public void testListRightsOnCalendarWithOtherDomainCalendar() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of())).andReturn(
				ImmutableMap.<String, EnumSet<Right>> of()).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getDomainNameFromEmail("foo@pub")).andReturn("pub").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		CalendarRights expectedMailToRights = CalendarRights.builder()
				.addRights("foo@pub", EnumSet.of(Right.ACCESS))
				.build();
		CalendarRights mailToRights = helperServiceImpl.listRightsOnCalendars(
				accessToken, ImmutableSet.of("foo@pub"));

		Assertions.assertThat(mailToRights).isEqualTo(expectedMailToRights);
		mocksControl.verify();
	}

	@Test
	public void testListRightsOnCalendarWithOwnDomainCalendar() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("beer")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of(
								"beer", EnumSet.of(Right.ACCESS, Right.READ))).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getDomainNameFromEmail("beer@bar")).andReturn("bar").once();
		expect(userService.getLoginFromEmail("beer@bar")).andReturn("beer").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		CalendarRights expectedMailToRights = CalendarRights.builder()
				.addRights("beer@bar", EnumSet.of(Right.ACCESS, Right.READ))
				.build();
		CalendarRights mailToRights = helperServiceImpl.listRightsOnCalendars(
				accessToken, ImmutableSet.of("beer@bar"));

		Assertions.assertThat(mailToRights).isEqualTo(expectedMailToRights);
		mocksControl.verify();
	}

	@Test
	public void testListRightsOnCalendarWithOwnDomainCalendarButNoRights() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("beer")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of()).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getDomainNameFromEmail("beer@bar")).andReturn("bar").once();
		expect(userService.getLoginFromEmail("beer@bar")).andReturn("beer").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		CalendarRights expectedMailToRights = CalendarRights.builder()
				.addRights("beer@bar", EnumSet.noneOf(Right.class))
				.build();
		CalendarRights mailToRights = helperServiceImpl.listRightsOnCalendars(
				accessToken, ImmutableSet.of("beer@bar"));

		Assertions.assertThat(mailToRights).isEqualTo(expectedMailToRights);
		mocksControl.verify();
	}

	@Test
	public void testListRightsOnCalendarWithMixedCalendars() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("beer")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of(
								"beer", EnumSet.of(Right.ACCESS, Right.READ))).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getDomainNameFromEmail("foo@bar")).andReturn("bar").once();
		expect(userService.getLoginFromEmail("foo@bar")).andReturn("foo").once();
		expect(userService.getDomainNameFromEmail("foo@pub")).andReturn("pub").once();
		expect(userService.getDomainNameFromEmail("beer@bar")).andReturn("bar").once();
		expect(userService.getLoginFromEmail("beer@bar")).andReturn("beer").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		CalendarRights expectedMailToRights = CalendarRights.builder()
				.addRights("foo@bar", EnumSet.of(Right.ACCESS, Right.READ, Right.WRITE))
				.addRights("foo@pub", EnumSet.of(Right.ACCESS))
				.addRights("beer@bar", EnumSet.of(Right.ACCESS, Right.READ))
				.build();
		CalendarRights mailToRights = helperServiceImpl.listRightsOnCalendars(
				accessToken, ImmutableSet.of("foo@bar", "foo@pub", "beer@bar"));

		Assertions.assertThat(mailToRights).isEqualTo(expectedMailToRights);
		mocksControl.verify();
	}

	private ObmDomain domainWithName(String domainName) {
		return ObmDomain
				.builder()
				.name(domainName)
				.build();
	}
	
	private ObmDomain domainWithAliases(String domainName, Set<String> aliases) {
		return ObmDomain
				.builder()
				.name(domainName)
				.aliases(aliases)
				.build();
	}
}
