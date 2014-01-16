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
package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.easymock.IMocksControl;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jRecurrenceHelper;
import org.obm.locator.store.LocatorService;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.utils.DateUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.date.DateProvider;
import org.obm.sync.services.AttendeeService;
import org.obm.sync.solr.SolrManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;


public class CalendarDaoJdbcImplTest {

	private static final ObmUser PATTERN_MATCHING_USER = ObmUser
			.builder()
			.uid(1)
			.login("login")
			.lastName("lastname")
			.firstName("firstname")
			.domain(ToolBox.getDefaultObmDomain())
			.build();

	private static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			
			bindWithMock(UserDao.class);
			bindWithMock(LocatorService.class);
			bindWithMock(CalendarDao.class);
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bindWithMock(SolrManager.class);
			bind(AttendeeService.class).to(SimpleAttendeeService.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
			bind(Ical4jRecurrenceHelper.class).to(Ical4jHelper.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(Env.class);
	
	@Inject
	private IMocksControl mocksControl;
	@Inject
	private UserDao userDao;
	@Inject
	private CalendarDaoJdbcImpl calendarDaoJdbcImpl;
	@Inject
	private DatabaseConnectionProvider dbcp;
	
	@Test
	public void touchParentOfDeclinedRecurrentEventsMustNotIncludeDuplicatesWhenExDatesDiffer() {
		Date date = date("2004-12-14T22:00:00");
		
		Event firstRecurrentEvent = new Event();
		firstRecurrentEvent.setTitle("firstRecurrentEvent");
		firstRecurrentEvent.setStartDate(date("2012-12-17T15:19:00"));
		firstRecurrentEvent.addException(date("2012-12-18T15:19:00"));
		firstRecurrentEvent.addException(date("2012-12-19T15:19:00"));

		Event secondRecurrentEvent = new Event();
		secondRecurrentEvent.setTitle("firstRecurrentEvent");
		secondRecurrentEvent.setStartDate(date("2012-12-17T15:19:00"));
		secondRecurrentEvent.addException(date("2012-12-19T15:19:00"));
		secondRecurrentEvent.addException(date("2012-12-18T15:19:00"));

		ArrayList<Event> result = Lists.newArrayList();
		Set<Event> parentOfDeclinedRecurrentEvent = ImmutableSet.of(firstRecurrentEvent, secondRecurrentEvent);
		
		calendarDaoJdbcImpl.touchParentOfDeclinedRecurrentEvents(parentOfDeclinedRecurrentEvent, result, date);
		
		assertThat(result).containsOnly(firstRecurrentEvent);
	}

	@Test
	public void touchParentOfDeclinedRecurrentEventsMustNotIncludeDuplicatesWhenMovedExceptionsDiffer() {
		Date date = date("2004-12-14T22:00:00");
		
		Event firstEventException = new Event();
		firstEventException.setStartDate(date("2012-12-18T15:19:00"));
		firstEventException.setDuration(3600);

		Event secondEventException = new Event();
		secondEventException.setStartDate(date("2012-12-19T15:19:00"));
		secondEventException.setDuration(3600);
		
		Event firstRecurrentEvent = new Event();
		firstRecurrentEvent.setTitle("firstRecurrentEvent");
		firstRecurrentEvent.setStartDate(date("2012-12-17T15:19:00"));
		firstRecurrentEvent.addEventException(firstEventException);
		firstRecurrentEvent.addEventException(secondEventException);

		Event secondRecurrentEvent = new Event();
		secondRecurrentEvent.setTitle("firstRecurrentEvent");
		secondRecurrentEvent.setStartDate(date("2012-12-17T15:19:00"));
		secondRecurrentEvent.addEventException(secondEventException);
		secondRecurrentEvent.addEventException(firstEventException);

		HashSet<Event> parentOfDeclinedRecurrentEvent = Sets.<Event>newHashSet();
		parentOfDeclinedRecurrentEvent.add(firstRecurrentEvent);
		parentOfDeclinedRecurrentEvent.add(secondRecurrentEvent);
		
		ArrayList<Event> result = Lists.<Event>newArrayList();
		calendarDaoJdbcImpl.touchParentOfDeclinedRecurrentEvents(parentOfDeclinedRecurrentEvent, result, date);
		
		assertThat(result).containsOnly(firstRecurrentEvent);
	}

	@Test
	public void touchParentOfDeclinedRecurrentEventsMustCheckExistenceWithUid() {
		Date date = date("2004-12-14T22:00:00");
		
		Event firstRecurrentEvent = new Event();
		firstRecurrentEvent.setUid(new EventObmId(1));
		firstRecurrentEvent.setTitle("firstRecurrentEvent");

		Event secondRecurrentEvent = new Event();
		secondRecurrentEvent.setUid(new EventObmId(1));
		secondRecurrentEvent.setTitle("newTitle");
		
		Event firstRecurrentEvent2 = new Event();
		firstRecurrentEvent2.setUid(new EventObmId(2));
		firstRecurrentEvent2.setTitle("firstRecurrentEvent");

		Event secondRecurrentEvent2 = new Event();
		secondRecurrentEvent2.setUid(new EventObmId(2));
		secondRecurrentEvent2.setTitle("newTitle");

		ArrayList<Event> result = Lists.newArrayList();
		result.add(secondRecurrentEvent);
		result.add(secondRecurrentEvent2);
		Set<Event> parentOfDeclinedRecurrentEvent = ImmutableSet.of(firstRecurrentEvent, firstRecurrentEvent2);
		
		calendarDaoJdbcImpl.touchParentOfDeclinedRecurrentEvents(parentOfDeclinedRecurrentEvent, result, date);
		
		assertThat(result).containsOnly(secondRecurrentEvent, secondRecurrentEvent2);
	}

	@Test
	public void testInsertAttendeesWithSameUserTwice() throws Exception {
		Integer userEntity = 1;
		Event event = new Event();
		ObmUser user = ToolBox.getDefaultObmUser();
		Connection connection = mocksControl.createMock(Connection.class);
		PreparedStatement ps = mocksControl.createMock(PreparedStatement.class);
		AccessToken token = ToolBox.mockAccessToken(user.getLogin(), user.getDomain(), mocksControl);
		List<Attendee> attendees = ImmutableList.of(getAttendee("user1@test.com", userEntity), getAttendee("user1alias@test.com", userEntity));
		
		event.setUid(new EventObmId(1));
		
		ps.addBatch();
		expectLastCall().times(1); // Because the two attendees have the same entityId
		
		expectgetJDBCObjects();
		expectPreparedStatementDatabaseObjectsCalls(connection, ps);
		
		mocksControl.replay();
		
		calendarDaoJdbcImpl.insertAttendees(token, event, connection, attendees);
		
		mocksControl.verify();
	}
	
	private Attendee getAttendee(String email, Integer entityId) {
		return UserAttendee.builder().email(email).entityId(entityId).build();
	}

	private void expectgetJDBCObjects() throws Exception {
		expect(dbcp.getJdbcObject(isA(String.class), isA(String.class))).andReturn("").anyTimes();
	}

	private void expectPreparedStatementDatabaseObjectsCalls(Connection connection, PreparedStatement ps) throws Exception {
		expect(connection.prepareStatement(isA(String.class))).andReturn(ps).anyTimes();
		
		ps.setInt(anyInt(), anyInt());
		expectLastCall().anyTimes();
		
		ps.setString(anyInt(), anyObject(String.class));
		expectLastCall().anyTimes();
		
		ps.setObject(anyInt(), anyObject());
		expectLastCall().anyTimes();
		
		ps.setBoolean(anyInt(), anyBoolean());
		expectLastCall().anyTimes();

		ps.setNull(anyInt(), anyInt());
		expectLastCall().anyTimes();
		
		ps.close();
		expectLastCall().anyTimes();
		
		expect(ps.executeBatch())
			.andReturn(new int[0]).anyTimes();

		expect(ps.executeUpdate())
			.andReturn(1).anyTimes();
	
		expect(ps.getConnection())
			.andReturn(connection).anyTimes();
		
		connection.close();
		expectLastCall().anyTimes();
		
		expect(connection.createStatement())
			.andReturn(ps).anyTimes();
	}

	@Test
	public void testCreateEventSetsEntityId() throws Exception {
		Event event = new Event();
		event.setExtId(new EventExtId("1"));
		ObmUser user = ToolBox.getDefaultObmUser();
		Connection connection = mocksControl.createMock(Connection.class);
		PreparedStatement ps = mocksControl.createMock(PreparedStatement.class);
		AccessToken token = ToolBox.mockAccessToken(user.getLogin(), user.getDomain(), mocksControl);
		
		expect(dbcp.getConnection())
			.andReturn(connection).once();
		
		String email = "user@test.org";
		expect(userDao.userIdFromEmail(connection, email, user.getDomain().getId()))
			.andReturn(1).once();
		
		int obmId = 1;
		event.setUid(new EventObmId(obmId));
		
		ps.addBatch();
		expectLastCall().anyTimes();
		
		expectgetJDBCObjects();
		expectPreparedStatementDatabaseObjectsCalls(connection, ps);
		
		int expectedEntityId = 531;
		expectLastInsertId(ps, obmId);
		expectLinkEntity(ps, expectedEntityId);
		
		mocksControl.replay();
		
		Event createdEvent = calendarDaoJdbcImpl.createEvent(token, email, event, true);
		
		mocksControl.verify();
		assertThat(createdEvent.getEntityId()).isEqualTo(expectedEntityId);
	}

	@Test
	public void testEventFromCursor() throws Exception {
		TimeZone beforeDefaultTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(DateTimeZone.UTC.toTimeZone());
		
		ResultSet resultSet = mocksControl.createMock(ResultSet.class);
		expect(resultSet.getInt("event_id")).andReturn(12);
		expect(resultSet.getInt("evententity_entity_id")).andReturn(8);
		expect(resultSet.getTimestamp("event_timecreate")).andReturn(Timestamp.valueOf("2012-05-04 11:50:01"));
		expect(resultSet.getTimestamp("event_timeupdate")).andReturn(Timestamp.valueOf("2012-05-04 11:55:12"));
		expect(resultSet.getString("event_timezone")).andReturn("America/San_Francisco");
		expect(resultSet.getString("event_type")).andReturn("VEVENT");
		expect(resultSet.getString("event_ext_id")).andReturn("abcd");
		expect(resultSet.getString("event_opacity")).andReturn("TRANSPARENT");
		expect(resultSet.getString("eventcategory1_label")).andReturn("category");
		expect(resultSet.getString("event_title")).andReturn("title");
		expect(resultSet.getString("event_location")).andReturn("location");
		expect(resultSet.getTimestamp("event_date")).andReturn(Timestamp.valueOf("2012-06-12 12:02:03"));
		expect(resultSet.getInt("event_duration")).andReturn(100);
		expect(resultSet.getInt("event_priority")).andReturn(1);
		expect(resultSet.getInt("event_privacy")).andReturn(1);
		expect(resultSet.getBoolean("event_allday")).andReturn(false);
		expect(resultSet.getString("event_description")).andReturn("desc");
		expect(resultSet.getInt("event_sequence")).andReturn(5);
		expect(resultSet.getString("owner")).andReturn("user@domain.org");
		expect(resultSet.getString("domain_name")).andReturn("domain.org");
		expect(resultSet.getString("userobm_email")).andReturn("useremail");
		expect(resultSet.getString("ownerFirstName")).andReturn("first");
		expect(resultSet.getString("ownerLastName")).andReturn("last");
		expect(resultSet.getString("ownerCommonName")).andReturn("first last");
		expect(resultSet.getString("creatorEmail")).andReturn("creator@domain.org");
		expect(resultSet.getString("creatorFirstName")).andReturn("c first");
		expect(resultSet.getString("creatorLastName")).andReturn("c last");
		expect(resultSet.getString("creatorCommonName")).andReturn("c common");
		
		expect(resultSet.getString("event_repeatkind")).andReturn("daily");
		expect(resultSet.getString("event_repeatdays")).andReturn("0101010");
		expect(resultSet.getInt("event_repeatfrequence")).andReturn(2);
		expect(resultSet.getTimestamp("event_endrepeat")).andReturn(Timestamp.valueOf("2013-06-12 12:02:03"));
		expect(resultSet.getTimestamp("recurrence_id")).andReturn(Timestamp.valueOf("2013-06-13 12:02:03"));
		
		Event expectedEvent = new Event();
		expectedEvent.setUid(new EventObmId(12));
		expectedEvent.setEntityId(8);
		expectedEvent.setTimeCreate(date("2012-05-04T11:50:01+00"));
		expectedEvent.setTimeUpdate(date("2012-05-04T11:55:12+00"));
		expectedEvent.setTimezoneName("America/San_Francisco");
		expectedEvent.setType(EventType.VEVENT);
		expectedEvent.setExtId(new EventExtId("abcd"));
		expectedEvent.setOpacity(EventOpacity.TRANSPARENT);
		expectedEvent.setCategory("category");
		expectedEvent.setTitle("title");
		expectedEvent.setLocation("location");
		expectedEvent.setStartDate(date("2012-06-12T12:02:03+00"));
		expectedEvent.setDuration(100);
		expectedEvent.setPriority(1);
		expectedEvent.setPrivacy(EventPrivacy.PRIVATE);
		expectedEvent.setAllday(false);
		expectedEvent.setDescription("desc");
		expectedEvent.setSequence(5);
		expectedEvent.setOwner("user@domain.org");
		expectedEvent.setOwnerEmail("useremail@domain.org");
		expectedEvent.setOwnerDisplayName("first last");
		expectedEvent.setCreatorEmail("creator@domain.org");
		expectedEvent.setCreatorDisplayName("c common");
		expectedEvent.setRecurrenceId(date("2013-06-13T12:02:03+00"));

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setDays(new RecurrenceDays(RecurrenceDay.Monday, RecurrenceDay.Wednesday, RecurrenceDay.Friday));
		recurrence.setFrequence(2);
		recurrence.setEnd(date("2013-06-12T12:02:03+00"));			
		expectedEvent.setRecurrence(recurrence);
		
		mocksControl.replay();
		Event resultEvent = calendarDaoJdbcImpl.eventFromCursor(DateUtils.getCurrentGMTCalendar(), resultSet);
		mocksControl.verify();
		
		assertThat(resultEvent).isEqualTo(expectedEvent);
		TimeZone.setDefault(beforeDefaultTimeZone);
	}

	private void expectLastInsertId(PreparedStatement ps, int obmId) throws SQLException {
		ResultSet resultSet = mocksControl.createMock(ResultSet.class);
		expect(ps.executeQuery(anyObject(String.class)))
			.andReturn(resultSet).once();
		expect(resultSet.next())
			.andReturn(true).once();
		expect(resultSet.getInt(1))
			.andReturn(obmId).once();
		resultSet.close();
		expectLastCall();
	}

	private void expectLinkEntity(PreparedStatement ps, Integer entityId) throws SQLException {
		ResultSet resultSet = mocksControl.createMock(ResultSet.class);
		expect(ps.executeQuery(anyObject(String.class)))
			.andReturn(resultSet).once();
		expect(resultSet.next())
			.andReturn(true).once();
		expect(resultSet.getInt(1))
			.andReturn(entityId).once();
		resultSet.close();
		expectLastCall();
	}

	@Test
	public void testUserMatchesPatternWithNullUser() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(null, "pattern")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWithNullPattern() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, null)).isTrue();
	}

	@Test
	public void testUserMatchesPatternWithEmptyPattern() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenNothingMatches() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "user")).isFalse();
	}

	@Test
	public void testUserMatchesPatternWhenMatchingLogin() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "log")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenExactlyMatchingLogin() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "login")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenMatchingLoginICase() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "LOg")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenMatchingLastname() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "lastna")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenExactlyMatchingLastname() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "lastname")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenMatchingLastnameICase() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "lAStnAM")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenMatchingFirstname() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "first")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenExactlyMatchingFirstname() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "firstname")).isTrue();
	}

	@Test
	public void testUserMatchesPatternWhenMatchingFirstnameICase() {
		assertThat(calendarDaoJdbcImpl.userMatchesPattern(PATTERN_MATCHING_USER, "FIRst")).isTrue();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildCalendarsQueryWithNegativeOffset() {
		calendarDaoJdbcImpl.buildCalendarsQuery("", 1, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildCalendarsQueryWithNullOffset() {
		calendarDaoJdbcImpl.buildCalendarsQuery("", 1, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildCalendarsQueryWithNegativeLimit() {
		calendarDaoJdbcImpl.buildCalendarsQuery("", -1, 0);
	}

	@Test
	public void testBuildCalendarsQueryIgnoresNullLimit() {
		assertThat(calendarDaoJdbcImpl.buildCalendarsQuery("", null, 0)).doesNotContain("LIMIT");
	}

	@Test
	public void testBuildCalendarsQueryConsidersLimitAndOffset() {
		assertThat(calendarDaoJdbcImpl.buildCalendarsQuery("", 10, 5)).contains("LIMIT 10 OFFSET 5");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildPublicAndUserAndGroupResourcesQueryNegativeLimit() {
		calendarDaoJdbcImpl.buildPublicAndUserAndGroupResourcesQuery("", "", "", -1, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildPublicAndUserAndGroupResourcesQueryWithNullOffset() {
		calendarDaoJdbcImpl.buildPublicAndUserAndGroupResourcesQuery("", "", "", 1, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildPublicAndUserAndGroupResourcesQueryWithNegativeOffset() {
		calendarDaoJdbcImpl.buildPublicAndUserAndGroupResourcesQuery("", "", "", 1, -1);
	}

	@Test
	public void testBuildPublicAndUserAndGroupResourcesQueryIgnoresNullLimit() {
		assertThat(calendarDaoJdbcImpl.buildPublicAndUserAndGroupResourcesQuery("", "", "", null, null)).doesNotContain("LIMIT");
	}

	@Test
	public void testBuildPublicAndUserAndGroupResourcesQueryConsidersLimitAndOffset() {
		assertThat(calendarDaoJdbcImpl.buildPublicAndUserAndGroupResourcesQuery("", "", "", 10, 5)).contains("LIMIT 10 OFFSET 5");
	}

}
