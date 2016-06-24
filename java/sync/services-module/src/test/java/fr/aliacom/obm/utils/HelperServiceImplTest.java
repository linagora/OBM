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

import java.sql.SQLException;
import java.util.EnumSet;

import org.assertj.core.api.Assertions;
import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.AddressBookDao;
import org.obm.domain.dao.ObmInfoDao;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserDaoJdbcImpl;
import org.obm.domain.dao.UserPatternDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.GroupDaoJdbcImpl;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.ProfileDaoJdbcImpl;
import org.obm.service.domain.DomainService;
import org.obm.service.user.UserService;
import org.obm.service.user.UserServiceImpl;
import org.obm.sync.Right;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.date.DateProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

@GuiceModule(HelperServiceImplTest.Env.class)
@RunWith(GuiceRunner.class)
public class HelperServiceImplTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(DomainService.class);
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bindWithMock(ObmInfoDao.class);
			bindWithMock(AddressBookDao.class);
			bindWithMock(UserPatternDao.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
			bind(UserService.class).to(UserServiceImpl.class);
			bind(UserDao.class).to(UserDaoJdbcImpl.class);
			bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
			bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Inject
	private HelperDao helperDao;

	@Inject
	private IMocksControl mocksControl;

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
		HelperDao helperDao = mocksControl.createMock(HelperDao.class);

		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of())).andReturn(
				ImmutableMap.<String, EnumSet<Right>> of()).once();
		UserService userService = mocksControl.createMock(UserService.class);
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
	public void testListRightsOnCalendarWithAdminLogin() {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("admin");
		accessToken.setDomain(domainWithName("bar"));
		accessToken.setRootAccount(true);
		UserService userService = mocksControl.createMock(UserService.class);
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
	public void testListRightsOnCalendarWithOwnDomainCalendar() throws SQLException {
		HelperDao helperDao = mocksControl.createMock(HelperDao.class);
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("beer")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of(
								"beer", EnumSet.of(Right.ACCESS, Right.READ))).once();
		UserService userService = mocksControl.createMock(UserService.class);
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
		HelperDao helperDao = mocksControl.createMock(HelperDao.class);

		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("beer")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of()).once();
		UserService userService = mocksControl.createMock(UserService.class);
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
	public void canWriteOnCalendarShouldReturnFalseWhenAccessAndReadRights() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		HelperDao helperDao = mocksControl.createMock(HelperDao.class);
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("other")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of(
								"other", EnumSet.of(Right.ACCESS, Right.READ))).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getLoginFromEmail("other@bar")).andReturn("other").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		boolean canWriteOnCalendar = helperServiceImpl.canWriteOnCalendar(accessToken, "other@bar");

		Assertions.assertThat(canWriteOnCalendar).isFalse();
		mocksControl.verify();
	}

	@Test
	public void canWriteOnCalendarShouldReturnFalseWhenAccessOnlyRight() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		HelperDao helperDao = mocksControl.createMock(HelperDao.class);
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("other")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of(
								"other", EnumSet.of(Right.ACCESS))).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getLoginFromEmail("other@bar")).andReturn("other").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		boolean canWriteOnCalendar = helperServiceImpl.canWriteOnCalendar(accessToken, "other@bar");

		Assertions.assertThat(canWriteOnCalendar).isFalse();
		mocksControl.verify();
	}

	@Test
	public void canWriteOnCalendarShouldReturnTrueWhenWriteOnlyRight() throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		HelperDao helperDao = mocksControl.createMock(HelperDao.class);
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("other")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of(
								"other", EnumSet.of(Right.WRITE))).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getLoginFromEmail("other@bar")).andReturn("other").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		boolean canWriteOnCalendar = helperServiceImpl.canWriteOnCalendar(accessToken, "other@bar");

		Assertions.assertThat(canWriteOnCalendar).isTrue();
		mocksControl.verify();
	}

	@Test
	public void canWriteOnCalendarShouldReturnTrueWhenAccessReadAndWriteRights()
			throws SQLException {
		AccessToken accessToken = new AccessToken(1, "outer space");
		accessToken.setUserLogin("foo");
		accessToken.setDomain(domainWithName("bar"));
		HelperDao helperDao = mocksControl.createMock(HelperDao.class);
		expect(helperDao.listRightsOnCalendars(accessToken, ImmutableSet.<String> of("other")))
				.andReturn(
						ImmutableMap.<String, EnumSet<Right>> of(
								"other", EnumSet.of(Right.ACCESS, Right.READ, Right.WRITE))).once();
		UserService userService = mocksControl.createMock(UserService.class);
		expect(userService.getLoginFromEmail("other@bar")).andReturn("other").once();
		mocksControl.replay();

		HelperServiceImpl helperServiceImpl = new HelperServiceImpl(helperDao, userService);
		boolean canWriteOnCalendar = helperServiceImpl.canWriteOnCalendar(accessToken, "other@bar");

		Assertions.assertThat(canWriteOnCalendar).isTrue();
		mocksControl.verify();
	}

	private ObmDomain domainWithName(String domainName) {
		return ObmDomain
				.builder()
				.name(domainName)
				.build();
	}

}
