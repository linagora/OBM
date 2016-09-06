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

import static fr.aliacom.obm.ToolBox.mockAccessToken;
import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.after;
import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import net.fortuna.ical4j.data.ParserException;

import org.apache.commons.io.IOUtils;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.CalendarDao;
import org.obm.domain.dao.CommitedOperationDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.provisioning.dao.exceptions.FindException;
import org.obm.service.calendar.CalendarService;
import org.obm.service.domain.DomainService;
import org.obm.service.user.UserService;
import org.obm.sync.NotAllowedException;
import org.obm.sync.PermissionException;
import org.obm.sync.Right;
import org.obm.sync.addition.CommitedElement;
import org.obm.sync.addition.CommitedOperation;
import org.obm.sync.addition.Kind;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Comment;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.Participation.State;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.ResourceAttendee;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.calendar.UnidentifiedAttendee;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.dao.EntityId;
import org.obm.sync.date.DateProvider;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.services.AttendeeService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ServicesToolBox;
import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import fr.aliacom.obm.utils.CalendarRights;
import fr.aliacom.obm.utils.HelperService;

@GuiceModule(CalendarBindingImplTest.Env.class)
@RunWith(GuiceRunner.class)
public class CalendarBindingImplTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(CalendarDao.class);
			bindWithMock(CommitedOperationDao.class);
			bindWithMock(EventNotificationService.class);
			bindWithMock(MessageQueueService.class);
			bindWithMock(DomainService.class);
			bindWithMock(UserService.class);
			bindWithMock(HelperService.class);
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bindWithMock(AnonymizerService.class);
			bindWithMock(ObmSyncConfigurationService.class);
			bind(AttendeeService.class).toInstance(new SimpleAttendeeService());
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Inject
	private IMocksControl mocksControl;
	@Inject
	private CalendarBindingImpl binding;
	@Inject
	private HelperService helperService;
	@Inject
	private AttendeeService attendeeService;
	@Inject
	private UserService userService;
	@Inject
	private CalendarDao calendarDao;
	@Inject
	private CalendarService calendarService;
	@Inject
	private MessageQueueService messageQueueService;
	@Inject
	private EventChangeHandler eventChangeHandler;
	@Inject
	private CommitedOperationDao commitedOperationDao;
	@Inject
	private EventNotificationService eventNotifier;
	@Inject
	private ObmSyncConfigurationService configurationService;
	@Inject
	private AnonymizerService anonymizerService;

	private AccessToken token;

	@Before
	public void setUp() {
		token = ToolBox.mockAccessToken(mocksControl);
	}

	private HelperService mockRightsHelper(String calendar, AccessToken accessToken) {
		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		expect(rightsHelper.canReadCalendar(eq(accessToken), eq(calendar))).andReturn(true).anyTimes();
		return rightsHelper;
	}

	private HelperService mockNoRightsHelper(String calendar, AccessToken accessToken) {
		HelperService noRightsHelper = createMock(HelperService.class);
		expect(noRightsHelper.canWriteOnCalendar(eq(accessToken), eq(calendar))).andReturn(false).anyTimes();
		expect(noRightsHelper.canReadCalendar(eq(accessToken), eq(calendar))).andReturn(false).anyTimes();
		return noRightsHelper;
	}

	private String stripEmail(String email) {
		String strippedEmail = email.substring(0, email.indexOf('@'));
		return strippedEmail;
	}

	@Test
	public void testGetCalendarMetadata() throws ServerFault, FindException {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		CalendarInfo beriaInfo = new CalendarInfo();
		beriaInfo.setUid(defaultUser.getLogin());
		beriaInfo.setFirstname("Lavrenti");
		beriaInfo.setLastname("Beria");
		beriaInfo.setMail(defaultUser.getEmailAtDomain());
		beriaInfo.setRead(true);
		beriaInfo.setWrite(true);

		CalendarInfo hooverInfo = new CalendarInfo();
		hooverInfo.setUid("hoover");
		hooverInfo.setFirstname("John");
		hooverInfo.setLastname("Hoover");
		hooverInfo.setMail("hoover@usa");
		hooverInfo.setRead(true);
		hooverInfo.setWrite(false);

		CalendarInfo mccarthyInfo = new CalendarInfo();
		mccarthyInfo.setUid("mccarthy");
		mccarthyInfo.setFirstname("Joseph");
		mccarthyInfo.setLastname("McCarthy");
		mccarthyInfo.setMail("mccarthy@usa");
		mccarthyInfo.setRead(true);
		mccarthyInfo.setWrite(false);

		String[] calendarEmails = {
				beriaInfo.getMail(),
				hooverInfo.getMail(),
				mccarthyInfo.getMail()
		};

		String[] calendarEmailsWithStrippedEmail = {
				beriaInfo.getMail(),
				hooverInfo.getMail(),
				stripEmail(hooverInfo.getMail()),
				mccarthyInfo.getMail(),
				stripEmail(mccarthyInfo.getMail()),
		};

		Collection<CalendarInfo> expectedCalendarInfos = ImmutableSet.of(
				beriaInfo,
				hooverInfo,
				mccarthyInfo
		);

		CalendarInfo[] calendarInfosFromDao = {
				beriaInfo,
				hooverInfo,
				mccarthyInfo,
		};

		AccessToken accessToken = mockAccessToken();
		HelperService rightsHelper = createMock(HelperService.class);

		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		expectLastCall().andReturn(defaultUser).once();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(defaultUser), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
		// Wrap the returned list into array list because we need a mutable list
		expectLastCall().andReturn( new ArrayList<CalendarInfo>(Arrays.asList(calendarInfosFromDao)) ).once();

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, null, null);

		Object[] mocks = {accessToken, userService, calendarDao, rightsHelper};

		replay(mocks);
		Collection<CalendarInfo> result = calendarBinding.getCalendarMetadata(accessToken, calendarEmails);
		assertThat(expectedCalendarInfos).containsExactlyElementsOf(result);
	}

	@Test
	public void testGetCalendarMetadataExceptCurrentUser() throws ServerFault, FindException {
		CalendarInfo hooverInfo = new CalendarInfo();
		hooverInfo.setUid("hoover");
		hooverInfo.setFirstname("John");
		hooverInfo.setLastname("Hoover");
		hooverInfo.setMail("hoover@usa");
		hooverInfo.setRead(true);
		hooverInfo.setWrite(false);

		CalendarInfo mccarthyInfo = new CalendarInfo();
		mccarthyInfo.setUid("mccarthy");
		mccarthyInfo.setFirstname("Joseph");
		mccarthyInfo.setLastname("McCarthy");
		mccarthyInfo.setMail("mccarthy@usa");
		mccarthyInfo.setRead(true);
		mccarthyInfo.setWrite(false);

		String[] calendarEmails = {
				hooverInfo.getMail(),
				mccarthyInfo.getMail()
		};

		String[] calendarEmailsWithStrippedEmail = {
				hooverInfo.getMail(),
				stripEmail(hooverInfo.getMail()),
				mccarthyInfo.getMail(),
				stripEmail(mccarthyInfo.getMail())
		};

		Collection<CalendarInfo> expectedCalendarInfos = ImmutableSet.of(
				hooverInfo,
				mccarthyInfo
		);

		CalendarInfo[] calendarInfosFromDao = {
				hooverInfo,
				mccarthyInfo,
		};

		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		AccessToken accessToken = mockAccessToken();
		HelperService rightsHelper = createMock(HelperService.class);

		UserService userService = createMock(UserService.class);
		userService.getUserFromAccessToken(eq(accessToken));
		expectLastCall().andReturn(defaultUser).once();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		calendarDao.getCalendarMetadata(eq(defaultUser), eq(Arrays.asList(calendarEmailsWithStrippedEmail)));
		// Wrap the returned list into array list because we need a mutable list
		expectLastCall().andReturn( new ArrayList<CalendarInfo>(Arrays.asList(calendarInfosFromDao)) ).once();

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, null, null);

		Object[] mocks = {accessToken, userService, calendarDao, rightsHelper};

		replay(mocks);
		Collection<CalendarInfo> result = calendarBinding.getCalendarMetadata(accessToken, calendarEmails);
		assertThat(expectedCalendarInfos).containsExactlyElementsOf(result);
	}

	@Test
	public void testGetResourceMetadata() throws FindException, ServerFault {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		ResourceInfo resource1 = buildResourceInfo1();
		ResourceInfo resource2 = buildResourceInfo2();
		Collection<ResourceInfo> resourceInfo = Arrays.asList(new ResourceInfo[] { resource1,
				resource2 });

		AccessToken accessToken = createMock(AccessToken.class);
		expect(accessToken.getConversationUid()).andReturn(1).anyTimes();

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultUser);

		String[] resources = {"res-1@domain.com", "res-2@domain.com"};

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.getResourceMetadata(defaultUser, Arrays.asList(resources))).andReturn(resourceInfo);

		Object[] mocks = { accessToken, calendarDao, userService };
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService,
				calendarDao, calendarService, null, commitedOperationDao, null, null, null, attendeeService, null, null);
		assertThat(ImmutableList.of(resource1, resource2))
			.containsExactlyElementsOf(calendarBinding.getResourceMetadata(accessToken, resources));

		verify(mocks);
	}

	@Test
	public void testGetResourceMetadataWithNoResource() throws ServerFault {
		AccessToken accessToken = createMock(AccessToken.class);

		Object[] mocks = { accessToken };
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null,
				null, null, null, null, null, null, null, attendeeService, null, null);
		assertThat(calendarBinding.getResourceMetadata(accessToken, new String[0])).isEmpty();


		verify(mocks);
	}

	@Test(expected=ServerFault.class)
	public void testCalendarOwnerNotAnAttendee() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		String calendar = defaultUser.getEmailAtDomain();
		EventExtId eventExtId = new EventExtId("extid");

		AccessToken accessToken = mockAccessToken(mocksControl);
		HelperService rightsHelper = mocksControl.createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(accessToken, calendar)).andReturn(true);

		Event event = mocksControl.createMock(Event.class);
		expect(event.getExtId()).andReturn(eventExtId).atLeastOnce();
		expect(event.getObmId()).andReturn(null).atLeastOnce();
		expect(event.getEventsExceptions()).andReturn(ImmutableSet.<Event>of()).anyTimes();

		event.findAttendeeFromEmail(defaultUser.getEmailAtDomain());
		expectLastCall().andReturn(null).atLeastOnce();

		userService.getUserFromCalendar(eq(calendar), eq(defaultUser.getDomain().getName()));
		expectLastCall().andReturn(defaultUser).atLeastOnce();

		calendarDao.findEventByExtId(eq(accessToken), eq(defaultUser), eq(eventExtId));
		expectLastCall().andReturn(null).once();

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, null, null);
		mocksControl.replay();
		try {
			calendarBinding.createEvent(accessToken, calendar, event, true, null);
		} catch (ServerFault e) {
			mocksControl.verify();
			assertThat(e.getMessage()).contains("Cannot find owner attendee");
			throw e;
		}
	}

	@Test
	public void testPurge() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = "cal1";
		EventExtId oldEventNoOtherAttendeesExtId = new EventExtId("oldEventNoOtherAttendeesExtId");
		EventExtId oldEventWithOtherAttendeesExtId = new EventExtId("oldEventWithOtherAttendeesExtId");
		EventObmId oldEventNoOtherAttendeesUid = new EventObmId("1");
		EventObmId oldEventWithOtherAttendeesUid = new EventObmId("2");

		String otherUserEmail = "user2@domain1";
		Attendee userAttendee = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		Attendee otherAttendee = ToolBox.getFakeAttendee(otherUserEmail);
		userAttendee.setParticipation(Participation.needsAction());

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());

		final Calendar oldEventDate = Calendar.getInstance();
		oldEventDate.add(Calendar.MONTH, -8);

		Event oldEventNoOtherAttendees = new Event();
		oldEventNoOtherAttendees.setExtId(oldEventNoOtherAttendeesExtId);
		oldEventNoOtherAttendees.setUid(oldEventNoOtherAttendeesUid);
		oldEventNoOtherAttendees.setAttendees(ImmutableList.of(userAttendee));
		oldEventNoOtherAttendees.setOwner(defaultUser.getEmailAtDomain());
		oldEventNoOtherAttendees.setType(EventType.VEVENT);
		oldEventNoOtherAttendees.setInternalEvent(true);

		Event oldEventWithOtherAttendees = new Event();
		oldEventWithOtherAttendees.setExtId(oldEventWithOtherAttendeesExtId);
		oldEventWithOtherAttendees.setUid(oldEventWithOtherAttendeesUid);
		oldEventWithOtherAttendees.setAttendees(ImmutableList.of(userAttendee, otherAttendee));
		oldEventWithOtherAttendees.setOwner(defaultUser.getEmailAtDomain());
		oldEventWithOtherAttendees.setType(EventType.VEVENT);

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		eventChangeHandler.delete(oldEventNoOtherAttendees, false, accessToken);
		eventChangeHandler.updateParticipation(oldEventWithOtherAttendees, defaultUser,
				Participation.declined(), false, accessToken);

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		expect(userService.getUserFromLogin(defaultUser.getEmailAtDomain(), defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.listEventsByIntervalDate(eq(accessToken), eq(defaultUser), isA(Date.class),
				isA(Date.class), (EventType)isNull())).
			andReturn(ImmutableList.of(oldEventNoOtherAttendees, oldEventWithOtherAttendees)).once();
		expect(calendarDao.findEventById(accessToken, oldEventNoOtherAttendeesUid)).
			andReturn(oldEventNoOtherAttendees).atLeastOnce();
		expect(calendarDao.removeEventById(accessToken, oldEventNoOtherAttendeesUid,
				oldEventNoOtherAttendees.getType(), oldEventNoOtherAttendees.getSequence()+1)).
				andReturn(oldEventNoOtherAttendees);
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, oldEventWithOtherAttendeesExtId)).
			andReturn(oldEventWithOtherAttendees).atLeastOnce();
		ParticipationService participationService = createMock(ParticipationService.class);
		expect(participationService.changeOnEvent(
				accessToken, oldEventWithOtherAttendeesExtId, Participation.declined(), 0, defaultUser, oldEventWithOtherAttendees)).
				andReturn(true);

		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);

		Object[] mocks = { accessToken, userService, rightsHelper, calendarDao, eventChangeHandler,
				participationService };
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, participationService, null, attendeeService, null, null);

		calendarBinding.purge(accessToken, calendar);

		verify(mocks);
	}

	@Test
	public void testPurgeWhenAnAttendeeHasNoEmail() throws Exception {
		Event event = new Event();
		String calendar = "user@test.tlse.lng";
		ObmUser user = ToolBox.getDefaultObmUser();
		EventExtId extId = new EventExtId("ExtId");

		event.setExtId(extId);
		event.addAttendee(ContactAttendee.builder().displayName("Contact 1").participation(Participation.needsAction()).build());
		event.addAttendee(UserAttendee.builder().displayName("Owner").email(user.getEmailAtDomain()).participation(Participation.accepted()).build());

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, user.getDomain().getName())).andReturn(user).times(3);
		expect(calendarDao.listEventsByIntervalDate(eq(token), eq(user), isA(Date.class), isA(Date.class), isNull(EventType.class))).andReturn(ImmutableList.of(event));
		expect(calendarDao.findEventByExtId(token, user, extId)).andReturn(event).times(2);
		expect(calendarDao.changeParticipation(token, user, extId, Participation.declined())).andReturn(true);
		messageQueueService.writeIcsInvitationReply(token, event, user);
		expectLastCall();
		mocksControl.replay();

		binding.purge(token, calendar);

		mocksControl.verify();
	}

	@Test
	public void testCommentResettedOnChangeParticipationState() throws SQLException {
		Participation participation = Participation.builder()
				.state(State.ACCEPTED)
				.comment("a comment")
				.build();
		ObmUser calendarOwner = ToolBox.getDefaultObmUser();
		AccessToken accessToken = new AccessToken(0, "origin");

		Event currentEvent = new Event();
		currentEvent.setSequence(0);
		currentEvent.addAttendee(
				UserAttendee.builder().email(calendarOwner.getEmail()).participation(Participation.needsAction()).build());

		CalendarDao calendarDao = createMock(CalendarDao.class);
		EventExtId extId = new EventExtId("0000");
		expect(calendarDao.changeParticipation(accessToken, calendarOwner,
				extId, participation)).andReturn(true);

		replay(calendarDao);

		CalendarBindingImpl calendarBindingImpl = new CalendarBindingImpl(null, null, null, calendarDao, calendarService, null, commitedOperationDao, null, null, null, attendeeService, null, null);

		boolean changed =
				calendarBindingImpl.applyParticipationChange(accessToken, extId, participation, 0, calendarOwner, currentEvent);
		verify(calendarDao);

		assertThat(changed).isTrue();
		assertThat(participation.getComment()).isEqualTo(Comment.EMPTY);
	}

	@Test
	public void testCommentResettedOnChangeParticipationStateWithRecurrenceId() throws SQLException, ParseException {
		Participation participation = Participation.builder()
				.state(State.ACCEPTED)
				.comment("a comment")
				.build();
		ObmUser calendarOwner = ToolBox.getDefaultObmUser();
		AccessToken accessToken = new AccessToken(0, "origin");

		Event currentEvent = new Event();
		currentEvent.setSequence(0);
		currentEvent.addAttendee(
			UserAttendee.builder().asAttendee().email(calendarOwner.getEmail()).participation(Participation.needsAction()).build());
		
		CalendarDao calendarDao = createMock(CalendarDao.class);
		EventExtId extId = new EventExtId("0000");
		RecurrenceId recurrenceId = new RecurrenceId("recId");
		expect(calendarDao.changeParticipation(accessToken, calendarOwner,
				extId, recurrenceId , participation)).andReturn(true);

		replay(calendarDao);

		CalendarBindingImpl calendarBindingImpl = new CalendarBindingImpl(null, null, null, calendarDao, calendarService, null, commitedOperationDao, null, null, null, attendeeService, null, null);

		boolean changed =
				calendarBindingImpl.applyParticipationChange(accessToken, extId, recurrenceId, participation, 0, calendarOwner, currentEvent);
		verify(calendarDao);

		assertThat(changed).isTrue();
		assertThat(participation.getComment()).isEqualTo(Comment.EMPTY);
	}
	
	@Test
	public void testModifyNullEvent() throws Exception {

		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		replay(accessToken);

		Event modifiedEvent = calendarBinding.modifyEvent(accessToken, calendar, null, false, false);

		assertThat(modifiedEvent).isNull();
	}

	@Test
	public void testModifyNotExistingEvent() throws Exception {

		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";
		Event event = new Event();

		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		final UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser)
						.once();
		CalendarDao calendarDao = createMock(CalendarDao.class);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, null, null, null, attendeeService, null, null);
		expect(calendarBinding.loadCurrentEvent(accessToken, defaultObmUser, event)).andReturn(null).once();

		replay(accessToken, userService, calendarDao);

		Event modifiedEvent = calendarBinding.modifyEvent(accessToken, calendar, event, false, false);
		assertThat(modifiedEvent).isNull();
	}

	@Test(expected=NotAllowedException.class)
	public void testToModifyEventWithoutWriteRightOnCalendar() throws Exception {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = defaultObmUser.getEmailAtDomain();
		Event event = new Event();
		event.setOwner("user");

		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		final UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser)
						.once();

		CalendarDao calendarDao = createMock(CalendarDao.class);

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canWriteOnCalendar(accessToken, calendar))
				.andReturn(false).once();

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, null, null);
		expect(calendarBinding.loadCurrentEvent(accessToken, defaultObmUser, event)).andReturn(event).once();

		replay(accessToken, userService, calendarDao, rightsHelper);

		calendarBinding.modifyEvent(accessToken, calendar, event, false, false);
	}

	@Test
	public void testAttendeeHasRightToWriteOnCalendar() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = defaultUser.getEmailAtDomain();
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		attendee.setParticipation(Participation.needsAction());

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setTitle("firstTitle");
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.setSequence(0);

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setLocation("aLocation");
		event.setSequence(1);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(attendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helper.listRightsOnCalendars(accessToken, ImmutableSet.of(attendee.getEmail()))).andReturn(calendarToRights).atLeastOnce();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).atLeastOnce();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event, updateAttendee, 1)).andReturn(event).atLeastOnce();
		eventChangeHandler.update(beforeEvent, event, notification, accessToken);

		replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, null, attendeeService, null, null);
		Event newEvent = calendarBinding.modifyEvent(accessToken, calendar, event, updateAttendee, notification);

		verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(Participation.accepted(), newEvent.getAttendees().get(0).getParticipation());
		Assert.assertEquals(1, newEvent.getAttendees().size());
		Assert.assertEquals(true, newEvent.getAttendees().iterator().next().isCanWriteOnCalendar());

		Assert.assertEquals(1, event.getAttendees().size());
		Assert.assertEquals(true, event.getAttendees().iterator().next().isCanWriteOnCalendar());
	}

	@Test(expected=NotAllowedException.class)
	public void testAttendeeHasNoRightToWriteOnCalendar() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = "cal1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		attendee.setParticipation(Participation.accepted());

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.setSequence(0);

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setLocation("aLocation");
		event.setSequence(1);

		AccessToken accessToken = ToolBox.mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, defaultUser.getEmailAtDomain())).andReturn(false).anyTimes();

		replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, null, attendeeService, null, null);

		calendarBinding.modifyEvent(accessToken, calendar, event, updateAttendee, notification);

		verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
	}

	@Test
	public void testAttendeeOfExceptionHasRightToWriteOnCalendar() throws Exception {

		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = "cal1";
		String exceptionAttendeeEmail = "exception_attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		attendee.setParticipation(Participation.needsAction());

		Attendee exceptionAttendee = ToolBox.getFakeAttendee(exceptionAttendeeEmail);
		exceptionAttendee.setParticipation(Participation.accepted());

		Date recurrenceId = new Date();

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setTitle("firstTitle");
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.setSequence(0);

		EventRecurrence beforeRecurrence = new EventRecurrence();
		beforeRecurrence.setKind(RecurrenceKind.daily);
		beforeEvent.setRecurrence(beforeRecurrence);

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setTitle("firstTitle");
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setSequence(1);
		Event exception = new Event();
		exception.setType(EventType.VEVENT);
		exception.setTitle("firstTitle");
		exception.setInternalEvent(true);
		exception.setExtId(extId);
		exception.addAttendee(attendee);
		exception.addAttendee(exceptionAttendee);
		exception.setSequence(1);
		exception.setStartDate(recurrenceId);
		exception.setRecurrenceId(recurrenceId);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setEventExceptions(Sets.newHashSet(exception));
		event.setRecurrence(recurrence);

		Event dummyException = new Event();
		dummyException.setType(EventType.VEVENT);
		dummyException.setTitle("firstTitle");
		dummyException.setInternalEvent(true);
		dummyException.setExtId(extId);
		dummyException.addAttendee(attendee);
		dummyException.setSequence(0);
		dummyException.setStartDate(recurrenceId);
		dummyException.setRecurrenceId(recurrenceId);
		dummyException.setRecurrence(new EventRecurrence());
		dummyException.getRecurrence().setKind(RecurrenceKind.none);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, defaultUser.getEmailAtDomain())).andReturn(true).atLeastOnce();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(attendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helper.listRightsOnCalendars(accessToken, ImmutableSet.of(attendee.getEmail())))
				.andReturn(calendarToRights).times(2);
		CalendarRights calendarToRights2 = CalendarRights.builder()
				.addRights(attendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.addRights(exceptionAttendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helper.listRightsOnCalendars(accessToken,
						ImmutableSet.of(attendee.getEmail(), exceptionAttendee.getEmail())))
				.andReturn(calendarToRights2).times(2);
		expect(helper.eventBelongsToCalendar(beforeEvent, defaultUser.getEmailAtDomain())).andReturn(true).once();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event,updateAttendee, 1))
				.andReturn(event).atLeastOnce();
		eventChangeHandler.update(beforeEvent, event, notification, accessToken);

		replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, null, attendeeService, null, null);
		Event newEvent = calendarBinding.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(Participation.needsAction(),
				Iterables.getOnlyElement(newEvent.getAttendees()).getParticipation());
		Event afterException = Iterables.getOnlyElement(newEvent.getRecurrence()
				.getEventExceptions());
		Attendee afterExceptionAttendee = afterException
				.findAttendeeFromEmail(exceptionAttendeeEmail);
		Assert.assertEquals(Participation.accepted(), afterExceptionAttendee.getParticipation());
		Assert.assertEquals(true, afterExceptionAttendee.isCanWriteOnCalendar());
	}

	@Test(expected=NotAllowedException.class)
	public void testAttendeeOfExceptionHasNoRightToWriteOnCalendar() throws Exception {

		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = "cal1";
		String exceptionAttendeeEmail = "exception_attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		attendee.setParticipation(Participation.needsAction());

		Attendee exceptionAttendee = ToolBox.getFakeAttendee(exceptionAttendeeEmail);
		exceptionAttendee.setParticipation(Participation.needsAction());

		Date recurrenceId = new Date();

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setTitle("firstTitle");
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.setSequence(0);

		EventRecurrence beforeRecurrence = new EventRecurrence();
		beforeRecurrence.setKind(RecurrenceKind.daily);
		beforeEvent.setRecurrence(beforeRecurrence);

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setTitle("firstTitle");
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.addAttendee(attendee);
		event.setSequence(1);
		Event exception = new Event();
		exception.setType(EventType.VEVENT);
		exception.setTitle("firstTitle");
		exception.setInternalEvent(true);
		exception.setExtId(extId);
		exception.addAttendee(attendee);
		exception.addAttendee(exceptionAttendee);
		exception.setSequence(1);
		exception.setStartDate(recurrenceId);
		exception.setRecurrenceId(recurrenceId);
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setEventExceptions(Sets.newHashSet(exception));
		event.setRecurrence(recurrence);

		Event dummyException = new Event();
		dummyException.setType(EventType.VEVENT);
		dummyException.setTitle("firstTitle");
		dummyException.setInternalEvent(true);
		dummyException.setExtId(extId);
		dummyException.addAttendee(attendee);
		dummyException.setSequence(0);
		dummyException.setStartDate(recurrenceId);
		dummyException.setRecurrenceId(recurrenceId);
		dummyException.setRecurrence(new EventRecurrence());
		dummyException.getRecurrence().setKind(RecurrenceKind.none);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).once();
		expect(helper.canWriteOnCalendar(accessToken, attendee.getEmail())).andReturn(false)
				.atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, exceptionAttendee.getEmail()))
				.andReturn(false).atLeastOnce();

		replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, null, attendeeService, null, null);

		calendarBinding.modifyEvent(accessToken, calendar, event, updateAttendee, notification);

		verify(accessToken, helper, calendarDao, userService, eventChangeHandler);
	}

	@Test
	public void testCreateAnEventExceptionAndUpdateItsStatusButNotTheParent() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = defaultUser.getEmailAtDomain();
		String attendeeEmail = "attendee@domain1";
		EventExtId extId = new EventExtId("extId");
		boolean updateAttendee = true;
		boolean notification = false;
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.lookup("daily"));

		Attendee attendee = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		attendee.setParticipation(Participation.accepted());
		Attendee attendee2 = ToolBox.getFakeAttendee(attendeeEmail);
		attendee2.setParticipation(Participation.accepted());

		Event beforeEvent = new Event();
		beforeEvent.setType(EventType.VEVENT);
		beforeEvent.setInternalEvent(true);
		beforeEvent.setExtId(extId);
		beforeEvent.addAttendee(attendee);
		beforeEvent.addAttendee(attendee2);
		beforeEvent.setRecurrence(recurrence);
		beforeEvent.setSequence(0);

		Event eventException = beforeEvent.clone();
		EventRecurrence recurrence2 = new EventRecurrence();
		recurrence2.setKind(RecurrenceKind.lookup("none"));
		eventException.setRecurrence(recurrence2);
		eventException.setRecurrenceId(new Date());
		beforeEvent.getRecurrence().addEventException(eventException);

		Event event = beforeEvent.clone();
		event.setSequence(1);
		Iterables.getOnlyElement(event.getRecurrence().getEventExceptions()).setLocation("aLocation");

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = createMock(HelperService.class);
		CalendarDao calendarDao = createMock(CalendarDao.class);
		UserService userService = createMock(UserService.class);
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser)
				.atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(
				beforeEvent).atLeastOnce();
		expect(helper.canWriteOnCalendar(accessToken, calendar)).andReturn(true).once();
		expect(helper.eventBelongsToCalendar(beforeEvent, calendar)).andReturn(true).once();

		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(attendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.addRights(attendee2.getEmail(), EnumSet.of(Right.ACCESS))
				.build();
		expect(helper.listRightsOnCalendars(accessToken, ImmutableSet.of(attendee.getEmail(), attendee2.getEmail()))).andReturn(calendarToRights).atLeastOnce();

		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, event, updateAttendee, 1)).andReturn(event).atLeastOnce();

		eventChangeHandler.update(beforeEvent, event, notification, accessToken);
		expectLastCall().atLeastOnce();

		replay(accessToken, helper, calendarDao, userService, eventChangeHandler);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, null, attendeeService, null, null);
		Event newEvent = calendarBinding.modifyEvent(accessToken, calendar, event, updateAttendee,
				notification);

		verify(accessToken, helper, calendarDao, userService, eventChangeHandler);

		Assert.assertEquals(Participation.accepted(), newEvent.getAttendees().get(0)
				.getParticipation());
		Assert.assertEquals(Participation.needsAction(), Iterables.getOnlyElement(newEvent.getRecurrence().getEventExceptions()).getAttendees().get(1)
				.getParticipation());
	}

	public void testDontSendEmailsAndDontUpdateStatusForUnimportantChanges() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = "cal1";
		String userEmail = "user@domain1";
		String guestAttendee1Email = "guestAttendee1@domain1";
		String guestAttendee2Email = "guestAttendee2@domain1";
		EventExtId eventExtId = new EventExtId("extid");
		EventObmId eventUid = new EventObmId("0");
		int sequence = 2;

		Attendee userAttendee = UserAttendee.builder().email(userEmail).participation(Participation.accepted()).build();
		Attendee guestAttendee1 = ContactAttendee.builder().email(guestAttendee1Email).participation(Participation.accepted()).build();
		Attendee guestAttendee2 = ContactAttendee.builder().email(guestAttendee2Email).participation(Participation.needsAction()).build();

		boolean updateAttendees = true;
		boolean notification = true;

		List<Attendee> oldAttendees = Arrays.asList(userAttendee, guestAttendee1);
		List<Attendee> newAttendees = Arrays.asList(userAttendee, guestAttendee1, guestAttendee2);

		Event oldEvent = new Event();
		oldEvent.setExtId(eventExtId);
		oldEvent.setUid(eventUid);
		oldEvent.setInternalEvent(true);
		oldEvent.setAttendees(oldAttendees);
		oldEvent.setSequence(sequence);

		Event newEvent = new Event();
		newEvent.setExtId(eventExtId);
		newEvent.setAttendees(newAttendees);
		newEvent.setSequence(sequence);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();

		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, eventExtId)).andReturn(oldEvent).once();
		expect(calendarDao.modifyEventForcingSequence(accessToken, calendar, newEvent, updateAttendees, sequence)).andReturn(newEvent).once();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		Object[] mocks = {accessToken, calendarDao, userService, rightsHelper, eventChangeHandler};
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, null, null);

		calendarBinding.modifyEvent(accessToken, calendar, newEvent, updateAttendees, notification);

		verify(mocks);

		Assert.assertEquals(Participation.accepted(), userAttendee.getParticipation());
		Assert.assertEquals(Participation.accepted(), guestAttendee1.getParticipation());
		Assert.assertEquals(Participation.needsAction(), guestAttendee2.getParticipation());
	}

	@Test
	public void testListResources() throws FindException, ServerFault {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		ResourceInfo resource1 = buildResourceInfo1();
		ResourceInfo resource2 = buildResourceInfo2();
		Collection<ResourceInfo> resourceInfo = Arrays.asList(new ResourceInfo[] { resource1,
				resource2 });

		AccessToken accessToken = createMock(AccessToken.class);
		expect(accessToken.getConversationUid()).andReturn(1).anyTimes();

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultUser);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.listResources(defaultUser, null, 0, null)).andReturn(resourceInfo);

		Object[] mocks = { accessToken, calendarDao, userService };
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, null, null, null, attendeeService, null, null);
		assertThat(ImmutableList.of(resource1, resource2))
			.containsExactlyElementsOf(calendarBinding.listResources(accessToken, null, 0, null));

		verify(mocks);
	}

	private Ical4jHelper mockIcal4jHelper(Integer ownerId, Ical4jUser ical4jUser, String icsData, Event eventWithOwnerAttendee) throws IOException, ParserException{
		Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
		expect(ical4jHelper.parseICS(icsData, ical4jUser, ownerId)).andReturn(ImmutableList.of(eventWithOwnerAttendee)).once();
		return ical4jHelper;
	}

	@Test
	public void testParseICSEventNotInternal() throws Exception {
		EventExtId extId = new EventExtId("extId1");

		Event result = testParseICS(extId, null);
		assertThat(result.getObmId()).isNull();
		assertThat(result.getExtId()).isSameAs(extId);
	}

	@Test
	public void testParseICSIncludeEventObmId() throws Exception {
		Event eventFromDao = new Event();
		EventObmId eventObmId = new EventObmId(12);
		eventFromDao.setUid(eventObmId);

		EventExtId extId = new EventExtId("extId");

		Event result = testParseICS(extId, eventFromDao);
		assertThat(result.getObmId()).isSameAs(eventObmId);
		assertThat(result.getExtId()).isSameAs(extId);
	}

	private Event testParseICS(EventExtId extId, Event eventFromDao) throws Exception {
		Ical4jUser ical4jUser = ServicesToolBox.getIcal4jUser();
		String calendar = ical4jUser.getEmail();
		String ics = "icsData";

		ObmUser obmUser = ObmUser.builder()
			.uid(1)
			.entityId(EntityId.valueOf(2))
			.login(UserLogin.valueOf("user"))
			.domain(ical4jUser.getObmDomain())
			.emails(UserEmails.builder()
				.addAddress(calendar)
				.domain(ical4jUser.getObmDomain())
				.build())
			.build();

		Event eventFromIcs = new Event();
		eventFromIcs.setExtId(extId);

		AccessToken accessToken = mockAccessToken(calendar, ical4jUser.getObmDomain());
		HelperService helper = mockRightsHelper(calendar, accessToken);

		expect(userService.getUserFromCalendar(calendar, ical4jUser.getObmDomain().getName())).andReturn(obmUser).once();
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(obmUser).once();

		Ical4jHelper ical4jHelper = mockIcal4jHelper(1, ical4jUser, ics, eventFromIcs);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(accessToken, obmUser, extId)).andReturn(eventFromDao).once();

		Object[] mocks = new Object[] {calendarDao, ical4jHelper, accessToken, helper};

		replay(mocks);
		mocksControl.replay();

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, ical4jHelper, attendeeService, null, null);
		List<Event> events = calendarBinding.parseICS(accessToken, ics);

		verify(mocks);
		mocksControl.verify();

		assertThat(events).hasSize(1);
		Event result = events.get(0);
		return result;
	}

	@Test
	public void testCreateExternalEventCalendarOwnerWithDeclinedPartState() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		String calendar = "cal1";
		EventExtId extId = new EventExtId("extId");
		boolean notification = false;

		Attendee calOwner = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		calOwner.setParticipation(Participation.declined());

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(false);
		event.setExtId(extId);
		event.setSequence(0);
		event.addAttendee(calOwner);


		Event eventCreated = new Event();
		eventCreated.setType(EventType.VEVENT);
		eventCreated.setInternalEvent(false);
		eventCreated.setExtId(extId);
		eventCreated.setSequence(0);
		eventCreated.addAttendee(calOwner);
		EventObmId obmId = new EventObmId(1);
		eventCreated.setUid(obmId);

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = mockRightsHelper(calendar, accessToken);
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(defaultUser.getEmail(), EnumSet.of(Right.ACCESS))
				.build();
		expect(helper.listRightsOnCalendars(accessToken, ImmutableSet.of(calOwner.getEmail()))).andReturn(calendarToRights).atLeastOnce();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(commitedOperationDao.findAsEvent(accessToken, null)).andReturn(null);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(null).once();
		expect(calendarDao.createEvent(accessToken, calendar, event)).andReturn(eventCreated).once();
		expect(calendarDao.removeEvent(accessToken, eventCreated, eventCreated.getType(), eventCreated.getSequence())).andReturn(eventCreated).once();
		eventChangeHandler.updateParticipation(eventCreated, defaultUser, calOwner.getParticipation(), notification, accessToken);
		expectLastCall().once();

		replay(accessToken, helper, eventChangeHandler);
		mocksControl.replay();

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, null, attendeeService, null, null);
		calendarBinding.createEvent(accessToken, calendar, event, notification, null);

		verify(accessToken, helper, eventChangeHandler);
		mocksControl.verify();

	}

	@Test
	public void testRecurrenceIdAtTheProperFormatInGetSyncResponse() throws Exception {
		String calendar = "cal1";
		String userName = "user";
		Date lastSync = new Date(1327690144000L);
		EventChanges daoChanges = getFakeEventChanges(RecurrenceKind.none);
		EventChanges sortedChanges = mockGetSyncWithSortedChanges(calendar, userName, lastSync, daoChanges);
		
		Set<ParticipationChanges> participationUpdated = sortedChanges.getParticipationUpdated();
		final ParticipationChanges firstParticipationChanges = participationUpdated.iterator().next();
		Assert.assertEquals(firstParticipationChanges.getRecurrenceId().serializeToString(), "20120127T160000Z");
	}

	@Test
	public void testGetSyncWithRecurrentEventAlwaysInUpdatedTag() throws Exception {
		String calendar = "cal1";
		String userName = "user";
		Date lastSync = new Date(1327680144000L);
		EventChanges daoChanges = getFakeAllRecurrentEventChanges();

		EventChanges sortedChanges = mockGetSyncWithSortedChanges(calendar, userName, lastSync, daoChanges);

		Set<Event> updatedEvents = sortedChanges.getUpdated();

		assertThat(updatedEvents).containsOnly(
				getFakeEvent(1, RecurrenceKind.daily),
				getFakeEvent(2, RecurrenceKind.monthlybydate),
				getFakeEvent(3, RecurrenceKind.monthlybyday),
				getFakeEvent(4, RecurrenceKind.weekly),
				getFakeEvent(5, RecurrenceKind.yearly));
	}

	private EventChanges mockGetSyncWithSortedChanges(String calendar, String userName,
			Date lastSync, EventChanges daoChanges) throws Exception {

		ObmUser defaultUser = ToolBox.getDefaultObmUser();

		AccessToken accessToken = mockAccessToken(userName, defaultUser.getDomain());

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();

		HelperService rightsHelper = mockRightsHelper(calendar, accessToken);

		CalendarDao calendarDao = createMock(CalendarDao.class);
		AnonymizerService anonymizerService = createMock(AnonymizerService.class);
		expect(calendarDao.getSync(accessToken, defaultUser, lastSync, null, null, false)).andReturn(daoChanges).once();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(accessToken))).andReturn(daoChanges).once();
		ObmSyncConfigurationService configurationService = createMock(ObmSyncConfigurationService.class);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		
		Object[] mocks = {calendarDao, accessToken, userService, rightsHelper, anonymizerService, configurationService};
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, anonymizerService, configurationService);

		EventChanges sortedChanges = calendarBinding.getSyncWithSortedChanges(accessToken, calendar, lastSync, null);
		verify(mocks);
		return sortedChanges;
	}

	private EventChanges getFakeEventChanges(RecurrenceKind recurrenceKind) {
		Event updatedEvent = getFakeEvent(1, recurrenceKind);
		return EventChanges.builder()
					.updates(Lists.newArrayList(updatedEvent))
					.lastSync(new Date())
					.build();
	}

	private EventChanges getFakeAllRecurrentEventChanges() {
		List<Event> changedRecurrentEvents = Lists.newArrayList(getFakeEvent(1, RecurrenceKind.daily),
				getFakeEvent(2, RecurrenceKind.monthlybydate),
				getFakeEvent(3, RecurrenceKind.monthlybyday),
				getFakeEvent(4, RecurrenceKind.weekly),
				getFakeEvent(5, RecurrenceKind.yearly));

		return EventChanges.builder()
					.lastSync(new Date())
					.updates(changedRecurrentEvents)
					.build();
	}

	private Event getFakeEvent(int eventId, RecurrenceKind recurrenceKind) {
		Event updatedEvent = new Event();
		updatedEvent.setUid(new EventObmId(eventId));
		updatedEvent.setExtId(new EventExtId(String.valueOf(eventId)));
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(recurrenceKind);
		updatedEvent.setRecurrence(eventRecurrence);
		updatedEvent.setTimeCreate(new Date(1327680143000L));
		updatedEvent.setTimeUpdate(new Date(1327680144000L));
		updatedEvent.setRecurrenceId(new Date(1327680000000L)); // Fri, 27 Jan 2012 16:00:00 GMT <=> 20120127T160000Z

		Attendee attendee = ToolBox.getFakeAttendee("user2@domain1");
		attendee.setParticipation(Participation.accepted());

		updatedEvent.addAttendee(attendee);
		return updatedEvent;
	}

	@Test(expected=ServerFault.class)
	public void testCreateNullEvent() throws Exception {

		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		try {
			calendarBinding.createEvent(accessToken, calendar, null, false, null);
		} catch (ServerFault e) {
			assertThat(e.getMessage()).isEqualTo("event creation without any data");
			throw e;
		}
	}

	@Test(expected=ServerFault.class)
	public void testCreateEventWithObmId() throws Exception {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		Event event = new Event();
		event.setUid(new EventObmId(42));
		try {
			calendarBinding.createEvent(accessToken, calendar, event, false, null);
		} catch (ServerFault e) {
			assertThat(e.getMessage()).isEqualTo("event creation with an event coming from OBM");
			throw e;
		}
	}

	@Test(expected=EventAlreadyExistException.class)
	public void testCreateDuplicateEvent() throws Exception {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		String calendar = "cal1";
		Event event = new Event();
		event.setExtId(new EventExtId("123"));
		event.setStartDate(new Date());

		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultObmUser, event.getExtId())).andReturn(event).once();

		replay(accessToken);
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, null, null, null, attendeeService, null, null);

		try {
			mocksControl.replay();
			calendarBinding.createEvent(accessToken, calendar, event, false, null);
		} finally {
			verify(accessToken);
			mocksControl.verify();
		}
	}

	@Test(expected=NotAllowedException.class)
	public void testCreateUnauthorizedEventOnCalendar() throws Exception {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		String calendar = "cal1";
		Event event = new Event();
		event.setExtId(new EventExtId("123"));
		event.setStartDate(new Date());

		expect(userService.getUserFromCalendar(calendar, defaultObmUser.getDomain().getName())).andReturn(defaultObmUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultObmUser, event.getExtId())).andReturn(null).once();

		HelperService helperService = mockNoRightsHelper(calendar, accessToken);

		replay(accessToken, helperService);
		mocksControl.replay();
		
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService, calendarDao, calendarService, null, commitedOperationDao, helperService, null, null, attendeeService, null, null);

		try {
			calendarBinding.createEvent(accessToken, calendar, event, false, null);
		} catch (Exception e) {
			mocksControl.verify();
			assertThat(e.getMessage()).contains("User user has no WRITE rights on calendar cal1");
			throw e;
		}
	}

	@Test
	public void testCreateInternalEvent() throws EventNotFoundException, ServerFault, FindException, SQLException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		String calendar = "cal1";
		Event internalEvent = new Event();
		internalEvent.setUid(new EventObmId(1));

		Event expectedEvent = new Event();
		expectedEvent.setUid(new EventObmId(1));

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.createEvent(accessToken, calendar, internalEvent)).andReturn(internalEvent).once();
		expect(calendarDao.findEventById(accessToken, internalEvent.getObmId())).andReturn(internalEvent).once();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		eventChangeHandler.create(internalEvent, false, accessToken);

		replay(calendarDao, eventChangeHandler);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null, null, calendarDao, calendarService, null, commitedOperationDao, null, null, null, attendeeService, null, null);
		Event createdEvent = calendarBinding.createInternalEvent(accessToken, calendar, internalEvent, false);

		verify(calendarDao, eventChangeHandler);

		assertThat(createdEvent).isEqualTo(expectedEvent);
	}

	@Test(expected = PermissionException.class)
	public void testAssignDelegationRightsNoRightsOnAttendees() throws PermissionException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(),
				defaultObmUser.getDomain());
		Event before = new Event();
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();

		before.setAttendees(attendees);

		Event after = before.clone();
		after.setLocation("a location");

		HelperService noRightsHelper = createMock(HelperService.class);
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights("beria", EnumSet.noneOf(Right.class))
				.addRights("hoover", EnumSet.noneOf(Right.class))
				.addRights("mccarthy", EnumSet.noneOf(Right.class))
				.build();
		expect(
				noRightsHelper.listRightsOnCalendars(accessToken,
						ImmutableSet.of("beria", "hoover", "mccarthy")))
				.andReturn(calendarToRights).atLeastOnce();

		replay(accessToken, noRightsHelper);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null,
				null, noRightsHelper, null, null, attendeeService, null, null);
		try {
			calendarBinding.assignDelegationRightsOnAttendees(accessToken, after);
		} catch (PermissionException ex) {
			verify(accessToken, noRightsHelper);
			throw ex;
		}
	}

	@Test
	public void testChangePartipationStateNoRightsOnAttendees() throws PermissionException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		Event before = new Event();
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();

		before.setAttendees(attendees);

		Event after = before.clone();
		after.setLocation("a location");

		HelperService noRightsHelper = createMock(HelperService.class);
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights("beria", EnumSet.of(Right.ACCESS))
				.addRights("hoover", EnumSet.of(Right.ACCESS))
				.addRights("mccarthy", EnumSet.of(Right.ACCESS))
				.build();
		expect(noRightsHelper.listRightsOnCalendars(accessToken, ImmutableSet.of("beria", "hoover", "mccarthy"))).andReturn(calendarToRights).atLeastOnce();

		replay(accessToken, noRightsHelper);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, noRightsHelper, null, null, attendeeService, null, null);
		calendarBinding.assignDelegationRightsOnAttendees(accessToken, after);
		calendarBinding.applyParticipationModifications(before, after);

		List<Attendee> attendeesToTest = after.getAttendees();
		assertThat(attendeesToTest).hasSize(3);

		Participation needsaction = Participation.builder()
												.state(State.NEEDSACTION)
												.comment("")
												.build();
		for(Attendee attendee: attendeesToTest) {
			assertThat(attendee.getParticipation()).isEqualTo(needsaction);
			assertThat(attendee.isCanWriteOnCalendar()).isEqualTo(false);
		}
		verify(accessToken, noRightsHelper);
	}

	@Test
	public void testChangeParticipationHasRightOnOneAttendee() throws PermissionException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
		Event before = new Event();
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();

		before.setAttendees(attendees);

		Event after = before.clone();
		after.setLocation("a location");

		Attendee beriaAttendee = before.getAttendees().get(0);
		Attendee hooverAttendee = before.getAttendees().get(1);
		Attendee mccarthyAttendee = before.getAttendees().get(2);

		HelperService rightsHelper = createMock(HelperService.class);
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(beriaAttendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.addRights(hooverAttendee.getEmail(), EnumSet.of(Right.ACCESS))
				.addRights(mccarthyAttendee.getEmail(), EnumSet.of(Right.ACCESS))
				.build();
		Set<String> emails = ImmutableSet.of(beriaAttendee.getEmail(),
				hooverAttendee.getEmail(),
				mccarthyAttendee.getEmail());
		expect(rightsHelper.listRightsOnCalendars(accessToken, emails)).andReturn(calendarToRights).atLeastOnce();
		replay(accessToken, rightsHelper);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, rightsHelper, null, null, attendeeService, null, null);
		calendarBinding.assignDelegationRightsOnAttendees(accessToken, after);
		calendarBinding.applyParticipationModifications(before, after);

		List<Attendee> attendeesToTest = after.getAttendees();
		assertThat(attendeesToTest).hasSize(3);
		Attendee beria = attendeesToTest.get(0);
		Attendee hoover = attendeesToTest.get(1);
		Attendee mccarthy = attendeesToTest.get(2);

		assertThat(beria.isCanWriteOnCalendar()).isEqualTo(true);
		Assert.assertEquals(beria.getParticipation(), Participation.accepted());
		assertThat(hoover.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(hoover.getParticipation(), Participation.needsAction());
		assertThat(mccarthy.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(mccarthy.getParticipation(), Participation.needsAction());
	}

	@Test
	public void testChangeParticipationHasOnlyWriteRight() throws PermissionException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(),
				defaultObmUser.getDomain());
		Event before = new Event();
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();

		before.setAttendees(attendees);

		Event after = before.clone();
		after.setLocation("a location");

		Attendee beriaAttendee = before.getAttendees().get(0);
		Attendee hooverAttendee = before.getAttendees().get(1);
		Attendee mccarthyAttendee = before.getAttendees().get(2);

		HelperService rightsHelper = createMock(HelperService.class);
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(beriaAttendee.getEmail(), EnumSet.of(Right.WRITE))
				.build();
		Set<String> emails = ImmutableSet.of(beriaAttendee.getEmail(), hooverAttendee.getEmail(), mccarthyAttendee.getEmail());
		expect(rightsHelper.listRightsOnCalendars(accessToken, emails)).andReturn(calendarToRights)
				.atLeastOnce();

		replay(accessToken, rightsHelper);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null,
				null, rightsHelper, null, null, attendeeService, null, configurationService);
		calendarBinding.assignDelegationRightsOnAttendees(accessToken, after);
		calendarBinding.applyParticipationModifications(before, after);

		List<Attendee> attendeesToTest = after.getAttendees();
		assertThat(attendeesToTest).hasSize(3);
		Attendee beria = attendeesToTest.get(0);

		assertThat(beria.isCanWriteOnCalendar()).isEqualTo(true);
		Assert.assertEquals(beria.getParticipation(), Participation.accepted());
	}
	
	@Test
	public void testChangeParticipationRecurrentEventWithException() throws PermissionException {
		ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
		AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

		Event beforeRecurrentEvent = new Event();
		beforeRecurrentEvent.setRecurrence(new EventRecurrence());
		List<Attendee> attendees = ToolBox.getFakeListOfAttendees();
		beforeRecurrentEvent.setAttendees(attendees);

		Event occurrence = beforeRecurrentEvent.getOccurrence(new Date());
		beforeRecurrentEvent.addEventException(occurrence);

		Event afterRecurrentEvent = beforeRecurrentEvent.clone();
		afterRecurrentEvent.setLocation("a location");

		Attendee beriaAttendee = beforeRecurrentEvent.getAttendees().get(0);
		Attendee hooverAttendee = beforeRecurrentEvent.getAttendees().get(1);
		Attendee mccarthyAttendee = beforeRecurrentEvent.getAttendees().get(2);

		HelperService rightsHelper = createMock(HelperService.class);
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(beriaAttendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.addRights(hooverAttendee.getEmail(), EnumSet.of(Right.ACCESS))
				.addRights(mccarthyAttendee.getEmail(), EnumSet.of(Right.ACCESS))
				.build();
		Set<String> emails = ImmutableSet.of(beriaAttendee.getEmail(), hooverAttendee.getEmail(), mccarthyAttendee.getEmail());
		expect(rightsHelper.listRightsOnCalendars(accessToken, emails)).andReturn(calendarToRights).times(2);

		replay(accessToken, rightsHelper);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, rightsHelper, null, null, attendeeService, null, null);
		calendarBinding.assignDelegationRightsOnAttendees(accessToken, afterRecurrentEvent);
		calendarBinding.applyParticipationModifications(beforeRecurrentEvent, afterRecurrentEvent);

		EventRecurrence eventRecurrence = afterRecurrentEvent.getRecurrence();
		Event exception = Iterables.getOnlyElement(eventRecurrence.getEventExceptions());

		List<Attendee> attendeesToTest = exception.getAttendees();
		assertThat(attendeesToTest).hasSize(3);
		Attendee beria = attendeesToTest.get(0);
		Attendee hoover = attendeesToTest.get(1);
		Attendee mccarthy = attendeesToTest.get(2);

		assertThat(beria.isCanWriteOnCalendar()).isEqualTo(true);
		Assert.assertEquals(beria.getParticipation(), Participation.accepted());
		assertThat(hoover.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(hoover.getParticipation(), Participation.needsAction());
		assertThat(mccarthy.isCanWriteOnCalendar()).isEqualTo(false);
		Assert.assertEquals(mccarthy.getParticipation(), Participation.needsAction());
	}

	@Test
	public void testNegativeExceptionChange() throws Exception {
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();

		Attendee userAttendee = ToolBox.getFakeAttendee(user.getEmailAtDomain());
		userAttendee.setParticipation(Participation.accepted());
		Attendee angletonAttendee = ToolBox.getFakeAttendee("james.jesus.angleton");
		angletonAttendee.setParticipation(Participation.accepted());
		Attendee dullesAttendee = ToolBox.getFakeAttendee("allen.dulles");
		dullesAttendee.setParticipation(Participation.accepted());

		int previousSequence = 0;
		Date eventDate = after();
		AccessToken token = ToolBox.mockAccessToken();
		Event previousEvent = ToolBox.getFakeDailyRecurrentEvent(eventDate, previousSequence,
				userAttendee, angletonAttendee, dullesAttendee);
		previousEvent.setInternalEvent(true);

		int currentSequence = previousSequence + 1;
		Event currentEvent = ToolBox.getFakeDailyRecurrentEvent(eventDate, currentSequence,
				userAttendee, angletonAttendee, dullesAttendee);
		Date exceptionDate = new DateTime(eventDate).plusMonths(1).toDate();
		currentEvent.addException(exceptionDate);
		currentEvent.setInternalEvent(true);

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(user.getEmailAtDomain(), "test.tlse.lng"))
				.andReturn(user).atLeastOnce();

		HelperService rightsHelper = createMock(HelperService.class);
		Set<String> emails = ImmutableSet.of(userAttendee.getEmail(), angletonAttendee.getEmail(), dullesAttendee.getEmail());
		CalendarRights emailToRights = CalendarRights.builder()
				.addRights(userAttendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.addRights(angletonAttendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.addRights(dullesAttendee.getEmail(), EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(rightsHelper.canWriteOnCalendar(token, calendar)).andReturn(true).once();
		expect(rightsHelper.eventBelongsToCalendar(previousEvent, calendar)).andReturn(true).once();
		expect(rightsHelper.listRightsOnCalendars(token, emails)).andReturn(emailToRights).times(2);

		boolean notification = true;
		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);
		eventChangeHandler.update(previousEvent, currentEvent, notification, token);

		boolean updateAttendees = true;

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.findEventByExtId(token, user, currentEvent.getExtId())).andReturn(
				previousEvent).once();
		expect(
				calendarDao.modifyEventForcingSequence(token, user.getEmailAtDomain(), currentEvent,
						updateAttendees, currentEvent.getSequence())).andReturn(currentEvent);

		replay(token, eventChangeHandler, userService, rightsHelper, calendarDao);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null,
				userService, calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, null, null);
		calendarBinding.modifyEvent(token, calendar, currentEvent, updateAttendees,
				notification);
	}

	@Test
	public void testRemoveEventByExtIdWhenOwnerInInternal() throws Exception {
		String calendar = "user@test";
		ObmUser userOwner = ToolBox.getDefaultObmUser();
		ObmUser userAttendee = ToolBox.getDefaultObmUserWithEmails("other@test");
		Attendee organizer = ContactAttendee.builder()
				.asOrganizer()
				.email(userOwner.getEmail()).participation(Participation.accepted()).build();
		Attendee attendee = UserAttendee.builder()
				.asAttendee()
				.email(userAttendee.getEmail()).participation(Participation.accepted()).build();

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(true);
		event.addAttendees(ImmutableSet.of(organizer, attendee));
		event.setOwner(organizer.getEmail());
		event.setUid(new EventObmId(546));
		event.setExtId(new EventExtId("132"));

		expect(helperService.canWriteOnCalendar(token, userOwner.getEmail())).andReturn(true);
		expect(userService.getUserFromCalendar(userOwner.getEmail(),"test.tlse.lng")).andReturn(userOwner).anyTimes();
		expect(userService.getUserFromLogin(event.getOwner(),"test.tlse.lng")).andReturn(userOwner);

		expect(calendarDao.findEventByExtId(token, userOwner, event.getExtId())).andReturn(event);
		expect(calendarDao.removeEventByExtId(token, userOwner, event.getExtId(), 1)).andReturn(event);

		messageQueueService.writeIcsInvitationCancel(token, event);
		expectLastCall();
		eventNotifier.notifyDeletedEvent(event, token);
		expectLastCall();

		mocksControl.replay();
		binding.removeEventByExtId(token, calendar, event.getExtId(), 0, true);
		mocksControl.verify();
	}

	@Test
	public void testRemoveEventByExtIdWhenAttendeeInInternal() throws Exception {
		String calendar = "user@test";
		ObmUser userAttendee = ToolBox.getDefaultObmUser();
		ObmUser userOwner = ToolBox.getDefaultObmUserWithEmails("other@test");
		Attendee organizer = ContactAttendee.builder()
				.asOrganizer()
				.email(userOwner.getEmail()).participation(Participation.accepted()).build();
		Attendee attendee = UserAttendee.builder()
				.asAttendee()
				.email(userAttendee.getEmail()).participation(Participation.accepted()).build();

		Event originalEvent = new Event();
		originalEvent.setType(EventType.VEVENT);
		originalEvent.setInternalEvent(true);
		originalEvent.addAttendees(ImmutableSet.of(organizer, attendee));
		originalEvent.setOwner(organizer.getEmail());
		originalEvent.setUid(new EventObmId(546));
		originalEvent.setExtId(new EventExtId("132"));
		Event changedParticipationEvent = originalEvent.clone();
		changedParticipationEvent
			.findAttendeeFromEmail(userAttendee.getEmail())
			.setParticipation(Participation.declined());

		expect(helperService.canWriteOnCalendar(token, userAttendee.getEmail())).andReturn(true);
		expect(userService.getUserFromCalendar(userAttendee.getEmail(),"test.tlse.lng")).andReturn(userAttendee).anyTimes();
		expect(userService.getUserFromLogin(originalEvent.getOwner(),"test.tlse.lng")).andReturn(userOwner);

		expect(calendarDao.findEventByExtId(token, userAttendee, originalEvent.getExtId()))
			.andReturn(originalEvent).times(2);
		expect(calendarDao.changeParticipation(token, userAttendee, originalEvent.getExtId(), Participation.declined()))
			.andReturn(true);
		expect(calendarDao.findEventByExtId(token, userAttendee, originalEvent.getExtId()))
			.andReturn(changedParticipationEvent).times(2);

		messageQueueService.writeIcsInvitationReply(token, changedParticipationEvent, userAttendee);
		expectLastCall();
		eventNotifier.notifyUpdatedParticipationAttendees(changedParticipationEvent, userAttendee,Participation.declined(), token);
		expectLastCall();

		mocksControl.replay();
		binding.removeEventByExtId(token, calendar, originalEvent.getExtId(), 0, true);
		mocksControl.verify();
	}

	@Test
	public void testRemoveEventByExtIdWhenAttendeeInExternal() throws Exception {
		String calendar = "user@test";
		ObmUser user = ToolBox.getDefaultObmUser();

		Attendee organizer = ContactAttendee.builder()
				.asOrganizer()
				.email("contact@domain").participation(Participation.accepted()).build();
		Attendee attendee = UserAttendee.builder()
				.asAttendee()
				.email(user.getEmailAtDomain()).participation(Participation.accepted()).build();

		Event originalEvent = new Event();
		originalEvent.setType(EventType.VEVENT);
		originalEvent.setInternalEvent(false);
		originalEvent.addAttendees(ImmutableSet.of(organizer, attendee));
		originalEvent.setOwner(organizer.getEmail());
		originalEvent.setUid(new EventObmId(546));
		originalEvent.setExtId(new EventExtId("132"));
		Event changedParticipationEvent = originalEvent.clone();
		changedParticipationEvent
			.findAttendeeFromEmail(attendee.getEmail())
			.setParticipation(Participation.declined());

		expect(helperService.canWriteOnCalendar(token, user.getEmailAtDomain())).andReturn(true);
		expect(userService.getUserFromCalendar(user.getEmailAtDomain(),"test.tlse.lng")).andReturn(user).anyTimes();
		expect(userService.getUserFromLogin(originalEvent.getOwner(),"test.tlse.lng")).andReturn(user);

		expect(calendarDao.findEventByExtId(token, user, originalEvent.getExtId()))
			.andReturn(originalEvent).times(2);
		expect(calendarDao.changeParticipation(token, user, originalEvent.getExtId(), Participation.declined()))
			.andReturn(true);
		expect(calendarDao.findEventByExtId(token, user, originalEvent.getExtId()))
			.andReturn(changedParticipationEvent).times(2);

		messageQueueService.writeIcsInvitationReply(token, changedParticipationEvent, user);
		expectLastCall();
		eventNotifier.notifyUpdatedParticipationAttendees(changedParticipationEvent, user,Participation.declined(), token);
		expectLastCall();

		mocksControl.replay();
		binding.removeEventByExtId(token, calendar, originalEvent.getExtId(), 0, true);
		mocksControl.verify();
	}


	@Test
	public void testRemoveEventByIdWhenOwnerInInternal() throws Exception {
		String calendar = "user@test";
		ObmUser userOwner = ToolBox.getDefaultObmUser();
		ObmUser userAttendee = ToolBox.getDefaultObmUserWithEmails("other@test");
		Attendee organizer = ContactAttendee.builder()
				.asOrganizer()
				.email(userOwner.getEmail()).participation(Participation.accepted()).build();
		Attendee attendee = UserAttendee.builder()
				.asAttendee()
				.email(userAttendee.getEmail()).participation(Participation.accepted()).build();

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(true);
		event.addAttendees(ImmutableSet.of(organizer, attendee));
		event.setOwner(organizer.getEmail());
		event.setUid(new EventObmId(546));
		event.setExtId(new EventExtId("132"));

		expect(helperService.canWriteOnCalendar(token, userOwner.getEmail())).andReturn(true);
		expect(userService.getUserFromCalendar(userOwner.getEmail(),"test.tlse.lng")).andReturn(userOwner).anyTimes();
		expect(userService.getUserFromLogin(event.getOwner(),"test.tlse.lng")).andReturn(userOwner);

		expect(calendarDao.findEventById(token, event.getObmId())).andReturn(event);
		expect(calendarDao.removeEventById(token, event.getObmId(), event.getType(), 1)).andReturn(event);

		messageQueueService.writeIcsInvitationCancel(token, event);
		expectLastCall();
		eventNotifier.notifyDeletedEvent(event, token);
		expectLastCall();

		mocksControl.replay();
		binding.removeEventById(token, calendar, event.getObmId(), 0, true);
		mocksControl.verify();
	}

	@Test
	public void testRemoveEventByIdWhenAttendeeInInternal() throws Exception {
		String calendar = "user@test";
		ObmUser userAttendee = ToolBox.getDefaultObmUser();
		ObmUser userOwner = ToolBox.getDefaultObmUserWithEmails("other@test");
		Attendee organizer = ContactAttendee.builder()
				.asOrganizer()
				.email(userOwner.getEmail()).participation(Participation.accepted()).build();
		Attendee attendee = UserAttendee.builder()
				.asAttendee()
				.email(userAttendee.getEmail()).participation(Participation.accepted()).build();

		Event originalEvent = new Event();
		originalEvent.setType(EventType.VEVENT);
		originalEvent.setInternalEvent(true);
		originalEvent.addAttendees(ImmutableSet.of(organizer, attendee));
		originalEvent.setOwner(organizer.getEmail());
		originalEvent.setUid(new EventObmId(546));
		originalEvent.setExtId(new EventExtId("132"));
		Event changedParticipationEvent = originalEvent.clone();
		changedParticipationEvent
			.findAttendeeFromEmail(userAttendee.getEmail())
			.setParticipation(Participation.declined());

		expect(helperService.canWriteOnCalendar(token, userAttendee.getEmail())).andReturn(true);
		expect(userService.getUserFromCalendar(userAttendee.getEmail(),"test.tlse.lng")).andReturn(userAttendee).anyTimes();
		expect(userService.getUserFromLogin(originalEvent.getOwner(),"test.tlse.lng")).andReturn(userOwner);

		expect(calendarDao.findEventById(token, originalEvent.getObmId())).andReturn(originalEvent);
		expect(calendarDao.findEventByExtId(token, userAttendee, originalEvent.getExtId())).andReturn(originalEvent);
		expect(calendarDao.changeParticipation(token, userAttendee, originalEvent.getExtId(), Participation.declined())).andReturn(true);
		expect(calendarDao.findEventByExtId(token, userAttendee, originalEvent.getExtId())).andReturn(changedParticipationEvent);

		messageQueueService.writeIcsInvitationReply(token, changedParticipationEvent, userAttendee);
		expectLastCall();
		eventNotifier.notifyUpdatedParticipationAttendees(changedParticipationEvent, userAttendee,Participation.declined(), token);
		expectLastCall();

		mocksControl.replay();
		binding.removeEventById(token, calendar, originalEvent.getObmId(), 0, true);
		mocksControl.verify();
	}

	@Test
	public void testRemoveEventByIdWhenAttendeeInExternal() throws Exception {
		String calendar = "user@test";
		ObmUser user = ToolBox.getDefaultObmUser();
		Attendee organizer = ContactAttendee.builder()
				.asOrganizer()
				.email("contact@domain").participation(Participation.accepted()).build();
		Attendee attendee = UserAttendee.builder()
				.asAttendee()
				.email(user.getEmail()).participation(Participation.accepted()).build();

		Event originalEvent = new Event();
		originalEvent.setType(EventType.VEVENT);
		originalEvent.setInternalEvent(false);
		originalEvent.addAttendees(ImmutableSet.of(organizer, attendee));
		originalEvent.setOwner(organizer.getEmail());
		originalEvent.setUid(new EventObmId(546));
		originalEvent.setExtId(new EventExtId("132"));
		Event changedParticipationEvent = originalEvent.clone();
		changedParticipationEvent
			.findAttendeeFromEmail(attendee.getEmail())
			.setParticipation(Participation.declined());

		expect(helperService.canWriteOnCalendar(token, user.getEmail())).andReturn(true);
		expect(userService.getUserFromCalendar(user.getEmail(),"test.tlse.lng")).andReturn(user).anyTimes();
		expect(userService.getUserFromLogin(originalEvent.getOwner(),"test.tlse.lng")).andReturn(user);

		expect(calendarDao.findEventById(token, originalEvent.getObmId())).andReturn(originalEvent);
		expect(calendarDao.findEventByExtId(token, user, originalEvent.getExtId())).andReturn(originalEvent);
		expect(calendarDao.changeParticipation(token, user, originalEvent.getExtId(), Participation.declined())).andReturn(true);
		expect(calendarDao.findEventByExtId(token, user, originalEvent.getExtId())).andReturn(changedParticipationEvent);

		messageQueueService.writeIcsInvitationReply(token, changedParticipationEvent, user);
		expectLastCall();
		eventNotifier.notifyUpdatedParticipationAttendees(changedParticipationEvent, user,Participation.declined(), token);
		expectLastCall();

		mocksControl.replay();
		binding.removeEventById(token, calendar, originalEvent.getObmId(), 0, true);
		mocksControl.verify();
	}

	@Test
	public void testReadOnlyCalendarWithPrivateEventsIsAnonymized() throws Exception {
		String calendar = "bill.colby@cia.gov";
		ObmUser user = ToolBox.getDefaultObmUser();
		AccessToken token = ToolBox.mockAccessToken();

		Date timeCreate = DateUtils.date("1974-09-04T14:00:00");
		Date lastSync = DateUtils.date("1973-09-04T14:00:00");
		Date syncDateFromDao = new DateTime(lastSync).plusSeconds(5).toDate();

		DeletedEvent deletedEvent1 = DeletedEvent.builder().eventObmId(1).eventExtId("deleted event 1").build();
		DeletedEvent deletedEvent2 = DeletedEvent.builder().eventObmId(2).eventExtId("deleted event 2").build();

		Event publicUpdatedEvent1 = new Event();
		publicUpdatedEvent1.setTitle("publicUpdatedEvent1");
		publicUpdatedEvent1.setTimeCreate(timeCreate);
		Event publicUpdatedEvent2 = new Event();
		publicUpdatedEvent2.setTitle("publicUpdatedEvent2");
		publicUpdatedEvent2.setTimeCreate(timeCreate);
		Event privateUpdatedEvent1 = new Event();
		privateUpdatedEvent1.setTitle("privateUpdatedEvent1");
		privateUpdatedEvent1.setPrivacy(EventPrivacy.PRIVATE);
		privateUpdatedEvent1.setTimeCreate(timeCreate);

		EventChanges eventChangesFromDao =
				EventChanges.builder()
					.lastSync(syncDateFromDao)
					.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
					.participationChanges(new ArrayList<ParticipationChanges>())
					.updates(Lists.newArrayList(publicUpdatedEvent1, publicUpdatedEvent2, privateUpdatedEvent1))
					.build();

		Event privateUpdatedAndAnonymizedEvent1 = new Event();
		privateUpdatedAndAnonymizedEvent1.setTitle(null);
		privateUpdatedAndAnonymizedEvent1.setPrivacy(EventPrivacy.PRIVATE);
		privateUpdatedAndAnonymizedEvent1.setAnonymized(true);
		privateUpdatedAndAnonymizedEvent1.setTimeCreate(timeCreate);

		EventChanges anonymizedEventChanges =
				EventChanges.builder()
					.lastSync(syncDateFromDao)
					.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
					.participationChanges(new ArrayList<ParticipationChanges>())
					.updates(Lists.newArrayList(
							publicUpdatedEvent1, publicUpdatedEvent2, privateUpdatedAndAnonymizedEvent1))
					.build();

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, "test.tlse.lng")).andReturn(user)
				.atLeastOnce();

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canReadCalendar(token, calendar)).andReturn(true).once();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.getSync(token, user, lastSync, null, null, false)).andReturn(
				eventChangesFromDao);
		AnonymizerService anonymizerService = createMock(AnonymizerService.class);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(anonymizedEventChanges).once();

		ObmSyncConfigurationService configurationService = createMock(ObmSyncConfigurationService.class);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);

		Object[] mocks = { token, userService, calendarDao, rightsHelper, anonymizerService, configurationService };
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService,
				calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, anonymizerService, configurationService);

		EventChanges actualChanges = calendarBinding.getSyncWithSortedChanges(token, calendar,
				lastSync, null);
		verify(mocks);
		assertThat(actualChanges).isEqualTo(anonymizedEventChanges);
	}

	@Test
	public void testReadWriteCalendarWithPrivateEventsIsNotAnonymized() throws Exception {
		String calendar = "bill.colby@cia.gov";
		ObmUser user = ToolBox.getDefaultObmUser();
		AccessToken token = ToolBox.mockAccessToken();

		Date timeCreate = DateUtils.date("1974-09-04T14:00:00");
		Date lastSync = DateUtils.date("1973-09-04T14:00:00");
		Date syncDateFromDao = new DateTime(lastSync).plusSeconds(5).toDate();

		DeletedEvent deletedEvent1 = DeletedEvent.builder().eventObmId(1).eventExtId("deleted event 1").build();
		DeletedEvent deletedEvent2 = DeletedEvent.builder().eventObmId(2).eventExtId("deleted event 2").build();

		Event publicUpdatedEvent1 = new Event();
		publicUpdatedEvent1.setTitle("publicUpdatedEvent1");
		publicUpdatedEvent1.setTimeCreate(timeCreate);
		Event publicUpdatedEvent2 = new Event();
		publicUpdatedEvent2.setTitle("publicUpdatedEvent2");
		publicUpdatedEvent2.setTimeCreate(timeCreate);
		Event privateUpdatedEvent1 = new Event();
		privateUpdatedEvent1.setTitle("privateUpdatedEvent1");
		privateUpdatedEvent1.setPrivacy(EventPrivacy.PRIVATE);
		privateUpdatedEvent1.setTimeCreate(timeCreate);
		privateUpdatedEvent1.addAttendee( UserAttendee
				.builder()
				.entityId(user.getEntityId())
				.email(user.getEmail())
				.build());

		EventChanges eventChangesFromDao =
				EventChanges.builder()
					.lastSync(syncDateFromDao)
					.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
					.participationChanges(new ArrayList<ParticipationChanges>())
					.updates(Lists.newArrayList(publicUpdatedEvent1, publicUpdatedEvent2, privateUpdatedEvent1))
					.build();

		UserService userService = createMock(UserService.class);
		expect(userService.getUserFromCalendar(calendar, "test.tlse.lng")).andReturn(user)
				.atLeastOnce();

		HelperService rightsHelper = createMock(HelperService.class);
		expect(rightsHelper.canReadCalendar(token, calendar)).andReturn(true).once();

		CalendarDao calendarDao = createMock(CalendarDao.class);
		expect(calendarDao.getSync(token, user, lastSync, null, null, false)).andReturn(
				eventChangesFromDao);
		AnonymizerService anonymizerService = createMock(AnonymizerService.class);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(eventChangesFromDao).once();

		ObmSyncConfigurationService configurationService = createMock(ObmSyncConfigurationService.class);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);

		Object[] mocks = { token, userService, calendarDao, rightsHelper, anonymizerService, configurationService};
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, userService,
				calendarDao, calendarService, null, commitedOperationDao, rightsHelper, null, null, attendeeService, anonymizerService, configurationService);

		EventChanges actualChanges = calendarBinding.getSyncWithSortedChanges(token, calendar,
				lastSync, null);
		verify(mocks);
		assertThat(actualChanges).isEqualTo(eventChangesFromDao);
	}

	@Test
	public void testGetSyncMoveNotAllowedConfidentialEvents() throws FindException, ServerFault, NotAllowedException {
		String calendar = "bill.colby@cia.gov";
		ObmUser user = ToolBox.getDefaultObmUser();

		Date timeCreate = DateUtils.date("1974-09-04T14:00:00");
		Date lastSync = DateUtils.date("1973-09-04T14:00:00");
		Date syncDateFromDao = new DateTime(lastSync).plusSeconds(5).toDate();

		DeletedEvent deletedEvent1 = DeletedEvent.builder().eventObmId(1).eventExtId("deleted event 1").build();

		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");
		publicEvent.setTimeCreate(timeCreate);

		Event confidentialEventWithAttendee = new Event();
		confidentialEventWithAttendee.setUid(new EventObmId(3));
		confidentialEventWithAttendee.setPrivacy(EventPrivacy.CONFIDENTIAL);
		confidentialEventWithAttendee.setExtId(new EventExtId("confidential_event"));
		confidentialEventWithAttendee.addAttendee(ContactAttendee.builder().email("user@test.tlse.lng").build());
		confidentialEventWithAttendee.setTimeCreate(timeCreate);

		Event simpleConfidentialEvent = new Event();
		simpleConfidentialEvent.setUid(new EventObmId(4));
		simpleConfidentialEvent.setPrivacy(EventPrivacy.CONFIDENTIAL);
		simpleConfidentialEvent.setExtId(new EventExtId("confidential_event2"));
		simpleConfidentialEvent.setTimeCreate(timeCreate);

		EventChanges eventChangesFromDao = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.deletes(ImmutableSet.of(deletedEvent1))
				.participationChanges(new ArrayList<ParticipationChanges>())
				.updates(Lists.newArrayList(
						publicEvent, confidentialEventWithAttendee, simpleConfidentialEvent))
				.build();

		DeletedEvent confidentialEventToDeletedEvent =
				DeletedEvent.builder().eventObmId(4).eventExtId("confidential_event2").build();

		EventChanges expectedEventChanges = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.deletes(ImmutableSet.of(deletedEvent1, confidentialEventToDeletedEvent))
				.participationChanges(new ArrayList<ParticipationChanges>())
				.updates(Lists.newArrayList(publicEvent, confidentialEventWithAttendee))
				.build();

		expect(userService.getUserFromCalendar(calendar, "test.tlse.lng")).andReturn(user).atLeastOnce();

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true).once();

		expect(calendarDao.getSync(token, user, lastSync, null, null, false)).andReturn(eventChangesFromDao);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(expectedEventChanges).once();

		mocksControl.replay();

		EventChanges actualEventChanges = binding.getSyncWithSortedChanges(token, calendar, lastSync, null);

		mocksControl.verify();

		assertThat(actualEventChanges).isEqualTo(expectedEventChanges);
	}

	@Test
	public void testGetFirstSync() throws FindException, ServerFault, NotAllowedException {
		String calendar = "bill.colby@cia.gov";
		ObmUser user = ToolBox.getDefaultObmUser();

		Date timeCreate = DateUtils.date("1974-09-04T14:00:00");
		Date lastSync = DateUtils.date("1973-09-04T14:00:00");
		Date syncDateFromDao = new DateTime(lastSync).plusSeconds(5).toDate();

		DeletedEvent deletedEvent1 = DeletedEvent.builder().eventObmId(1).eventExtId("deleted event 1").build();

		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");
		publicEvent.setTimeCreate(timeCreate);

		Event confidentialEventWithAttendee = new Event();
		confidentialEventWithAttendee.setUid(new EventObmId(3));
		confidentialEventWithAttendee.setPrivacy(EventPrivacy.CONFIDENTIAL);
		confidentialEventWithAttendee.setExtId(new EventExtId("confidential_event"));
		confidentialEventWithAttendee.addAttendee(ContactAttendee.builder().email("user@test.tlse.lng").build());
		confidentialEventWithAttendee.setTimeCreate(timeCreate);

		Event simpleConfidentialEvent = new Event();
		simpleConfidentialEvent.setUid(new EventObmId(4));
		simpleConfidentialEvent.setPrivacy(EventPrivacy.CONFIDENTIAL);
		simpleConfidentialEvent.setExtId(new EventExtId("confidential_event2"));
		simpleConfidentialEvent.setTimeCreate(timeCreate);

		EventChanges eventChangesFromDao = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.deletes(ImmutableSet.of(deletedEvent1))
				.participationChanges(new ArrayList<ParticipationChanges>())
				.updates(Lists.newArrayList(
						publicEvent, confidentialEventWithAttendee, simpleConfidentialEvent))
				.build();

		EventChanges expectedEventChanges = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.participationChanges(new ArrayList<ParticipationChanges>())
				.updates(Lists.newArrayList(publicEvent, confidentialEventWithAttendee))
				.build();

		expect(userService.getUserFromCalendar(calendar, "test.tlse.lng")).andReturn(user).atLeastOnce();

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true).once();

		expect(calendarDao.getSync(token, user, lastSync, null, null, false)).andReturn(eventChangesFromDao);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(expectedEventChanges).once();

		mocksControl.replay();

		EventChanges actualEventChanges = binding.getFirstSync(token, calendar, lastSync);

		mocksControl.verify();

		assertThat(actualEventChanges).isEqualTo(expectedEventChanges);
	}

	@Test
	public void testGetFirstSyncEventDate() throws FindException, ServerFault, NotAllowedException {
		String calendar = "bill.colby@cia.gov";
		ObmUser user = ToolBox.getDefaultObmUser();

		Date timeCreate = DateUtils.date("1974-09-04T14:00:00");
		Date lastSync = DateUtils.date("1973-09-04T14:00:00");
		Date syncDateFromDao = new DateTime(lastSync).plusSeconds(5).toDate();

		DeletedEvent deletedEvent1 = DeletedEvent.builder().eventObmId(1).eventExtId("deleted event 1").build();

		Event publicEvent = new Event();
		publicEvent.setExtId(new EventExtId("public_event"));
		publicEvent.setTitle("public event");
		publicEvent.setTimeCreate(timeCreate);

		Event confidentialEventWithAttendee = new Event();
		confidentialEventWithAttendee.setUid(new EventObmId(3));
		confidentialEventWithAttendee.setPrivacy(EventPrivacy.CONFIDENTIAL);
		confidentialEventWithAttendee.setExtId(new EventExtId("confidential_event"));
		confidentialEventWithAttendee.addAttendee(ContactAttendee.builder().email("user@test.tlse.lng").build());
		confidentialEventWithAttendee.setTimeCreate(timeCreate);

		Event simpleConfidentialEvent = new Event();
		simpleConfidentialEvent.setUid(new EventObmId(4));
		simpleConfidentialEvent.setPrivacy(EventPrivacy.CONFIDENTIAL);
		simpleConfidentialEvent.setExtId(new EventExtId("confidential_event2"));
		simpleConfidentialEvent.setTimeCreate(timeCreate);

		EventChanges eventChangesFromDao = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.deletes(ImmutableSet.of(deletedEvent1))
				.participationChanges(new ArrayList<ParticipationChanges>())
				.updates(Lists.newArrayList(
						publicEvent, confidentialEventWithAttendee, simpleConfidentialEvent))
				.build();

		EventChanges expectedEventChanges = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.participationChanges(new ArrayList<ParticipationChanges>())
				.updates(Lists.newArrayList(publicEvent, confidentialEventWithAttendee))
				.build();

		expect(userService.getUserFromCalendar(calendar, "test.tlse.lng")).andReturn(user).atLeastOnce();

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true).once();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(expectedEventChanges).once();
		expect(calendarDao.getSync(token, user, lastSync, null, null, true)).andReturn(eventChangesFromDao);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);

		mocksControl.replay();

		EventChanges actualEventChanges = binding.getFirstSyncEventDate(token, calendar, lastSync);

		mocksControl.verify();

		assertThat(actualEventChanges).isEqualTo(expectedEventChanges);
	}

	@Test
	public void testGetSyncDoesNotMoveConfidentialEvents() throws FindException, ServerFault, NotAllowedException {
		String calendar = "user@test.tlse.lng";
		ObmUser user =
				ObmUser.builder().uid(1)
					.entityId(EntityId.valueOf(2))
					.login(UserLogin.valueOf("user"))
					.domain(ToolBox.getDefaultObmDomain())
					.emails(UserEmails.builder()
						.addAddress("user@test.tlse.lng")
						.domain(ToolBox.getDefaultObmDomain())
						.build())
					.identity(UserIdentity.builder().firstName("Obm").lastName("User").build())
					.build();

		Date timeCreate = new DateTime(1974, Calendar.SEPTEMBER, 4, 14, 0).toDate();
		Date lastSync = new DateTime(1973, Calendar.SEPTEMBER, 4, 14, 0).toDate();
		Date syncDateFromDao = new DateTime(lastSync).plusSeconds(5).toDate();

		Event simpleConfidentialEvent = new Event();
		simpleConfidentialEvent.setUid(new EventObmId(4));
		simpleConfidentialEvent.setPrivacy(EventPrivacy.CONFIDENTIAL);
		simpleConfidentialEvent.setExtId(new EventExtId("confidential_event2"));
		simpleConfidentialEvent.setTimeCreate(timeCreate);

		EventChanges eventChangesFromDao = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.updates(Lists.newArrayList(simpleConfidentialEvent))
				.build();

		EventChanges expectedEventChanges = EventChanges.builder()
				.lastSync(syncDateFromDao)
				.updates(Lists.newArrayList(simpleConfidentialEvent))
				.build();

		expect(userService.getUserFromCalendar(calendar, "test.tlse.lng")).andReturn(user).atLeastOnce();

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true).once();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(expectedEventChanges).once();
		expect(calendarDao.getSync(token, user, lastSync, null, null, false)).andReturn(eventChangesFromDao);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);

		mocksControl.replay();

		EventChanges actualEventChanges = binding.getSyncWithSortedChanges(token, calendar, lastSync, null);

		mocksControl.verify();

		assertThat(actualEventChanges).isEqualTo(expectedEventChanges);
	}

	@Test(expected=NotAllowedException.class)
	public void testAssertEventCanBeModifiedWhenCannotWriteOnCalendar() throws Exception {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();
		Event eventToModify = new Event();

		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(false);
		replay(helperService);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, helperService, null, null, attendeeService, null, null);

		calendarBinding.assertEventCanBeModified(token, user, eventToModify);
	}

	@Test(expected=NotAllowedException.class)
	public void testAssertEventCanBeModifiedWhenEventDoesNotBelongToCalendar() throws Exception {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();
		Event eventToModify = new Event();

		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(helperService.eventBelongsToCalendar(eventToModify, calendar)).andReturn(false);
		replay(helperService, token);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, helperService, null, null, attendeeService, null, null);

		calendarBinding.assertEventCanBeModified(token, user, eventToModify);
	}

	@Test
	public void testAssertEventCanBeModifiedWhenRequirementsAreOK() throws Exception {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();
		Event eventToModify = new Event();

		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(helperService.eventBelongsToCalendar(eventToModify, calendar)).andReturn(true);
		replay(helperService);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, helperService, null, null, attendeeService, null, null);

		calendarBinding.assertEventCanBeModified(token, user, eventToModify);
	}

	@Test
	public void testAssertEventCanBeModifiedWhenEventBelongsToEditorInAnotherCalendar() throws Exception {
		AccessToken token = ToolBox.mockAccessToken();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();
		Event eventToModify = new Event();

		HelperService helperService = createMock(HelperService.class);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(helperService.eventBelongsToCalendar(eventToModify, calendar)).andReturn(false);
		replay(helperService, token);

		eventToModify.setOwnerEmail(token.getUserEmail());

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, helperService, null, null, attendeeService, null, null);

		calendarBinding.assertEventCanBeModified(token, user, eventToModify);
	}

	@Test
	public void testInheritsParticipationForSpecificAttendee() {
		Attendee expectedAttendee = UserAttendee.builder()
			.email("attendee@test.lng")
			.participation(Participation.needsAction())
			.build();
		Event event = createEvent(Arrays.asList(expectedAttendee));

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		Attendee attendee = UserAttendee.builder()
			.email("attendee@test.lng")
			.participation(Participation.accepted())
			.build();
		calendarBinding.inheritsParticipationForSpecificAttendee(event, attendee);
		assertThat(attendee).isEqualTo(expectedAttendee);
	}

	@Test
	public void testInheritsParticipationFromExistingEventEmptyAttendees() {
		Event before = new Event();
		Event after = new Event();

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(after.getAttendees()).isEmpty();
	}

	@Test
	public void testInheritsParticipationFromExistingEventOneHandEmptyAttendees() {
		Event before = new Event();

		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.accepted());
		Event after = createEvent(expectedAttendees);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(after.getAttendees()).isEqualTo(expectedAttendees);
	}

	@Test
	public void testInheritsParticipationFromExistingEventThOtherHandEmptyAttendees() {
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.accepted());
		Event before = createEvent(expectedAttendees);

		Event after = new Event();

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(after.getAttendees()).isEmpty();
	}

	@Test
	public void testInheritsParticipationFromExistingEvent() {
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.needsAction());
		Event before = createEvent(expectedAttendees);

		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(after.getAttendees()).isEqualTo(expectedAttendees);
	}

	@Test
	public void testInheritsParticipationOnEmptyExceptions() {
		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.needsAction());
		Event before = createEvent(expectedAttendees);

		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationOnExceptions(before, after);
		assertThat(after.getEventsExceptions()).isEmpty();
	}

	@Test
	public void testInheritsParticipationOnExceptions() {
		DateTime eventDate = DateTime.now();
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.declined());
		Event before = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		Event afterException = createEventException(createOrganiserAndContactAttendees(Participation.declined()), eventDate.plusDays(1).toDate());
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		before.addEventException(createEventException(expectedAttendeesException, eventDate.plusDays(1).toDate()));
		before.setExtId(new EventExtId("Event"));
		after.addEventException(afterException);
		after.setExtId(new EventExtId("Event"));

		calendarBinding.inheritsParticipationOnExceptions(before, after);

		assertAttendeesHaveSameParticipation(afterException.getAttendees(), expectedAttendeesException);
	}

	@Test
	public void testInheritsParticipationOnExceptionsMultipleExceptions() {
		DateTime eventDate = DateTime.now();
		List<Attendee> expectedAttendeesException_1 = createOrganiserAndContactAttendees(Participation.declined());
		List<Attendee> expectedAttendeesException_2 = createOrganiserAndContactAttendees(Participation.needsAction());
		List<Attendee> expectedAttendeesException_3 = createOrganiserAndContactAttendees(Participation.tentative());
		Event before = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		Event afterException_1 = createEventException(createOrganiserAndContactAttendees(Participation.declined()), eventDate.plusDays(1).toDate());
		Event afterException_2 = createEventException(createOrganiserAndContactAttendees(Participation.needsAction()), eventDate.plusDays(2).toDate());
		Event afterException_3 = createEventException(createOrganiserAndContactAttendees(Participation.tentative()), eventDate.plusDays(3).toDate());
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		before.addEventException(createEventException(expectedAttendeesException_1, eventDate.plusDays(1).toDate()));
		before.addEventException(createEventException(expectedAttendeesException_2, eventDate.plusDays(2).toDate()));
		before.addEventException(createEventException(expectedAttendeesException_3, eventDate.plusDays(3).toDate()));
		before.setExtId(new EventExtId("Event"));

		after.addEventException(afterException_1);
		after.addEventException(afterException_2);
		after.addEventException(afterException_3);
		after.setExtId(new EventExtId("Event"));

		calendarBinding.inheritsParticipationOnExceptions(before, after);

		assertAttendeesHaveSameParticipation(afterException_1.getAttendees(), expectedAttendeesException_1);
		assertAttendeesHaveSameParticipation(afterException_2.getAttendees(), expectedAttendeesException_2);
		assertAttendeesHaveSameParticipation(afterException_3.getAttendees(), expectedAttendeesException_3);
	}

	private void assertAttendeesHaveSameParticipation(List<Attendee> newAttendees, List<Attendee> expectedAttendees) {
		assertThat(expectedAttendees).hasSameSizeAs(newAttendees);

		for (Attendee attendee : newAttendees) {
			int indexOfAttendee = expectedAttendees.indexOf(attendee);

			assertThat(indexOfAttendee).isGreaterThan(-1);
			assertThat(attendee.getParticipation()).isEqualTo(expectedAttendees.get(indexOfAttendee).getParticipation());
		}
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEvent() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.declined());
		Event beforeException = createEvent(expectedAttendeesException);

		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.needsAction());
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);

		Event afterException = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		after.addEventException(afterException);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendeesException);
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventEmptyExceptions() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.declined());
		Event beforeException = createEvent(expectedAttendeesException);

		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.needsAction());
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);
		String expectedExtId = "123";
		EventExtId expectedEventExtId = new EventExtId(expectedExtId);
		beforeException.setExtId(expectedEventExtId);

		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(after.getEventsExceptions()).isEmpty();
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventComparator() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.declined());
		Event beforeException = createEvent(expectedAttendeesException);

		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.needsAction());
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);
		String expectedExtId = "123";
		EventExtId expectedEventExtId = new EventExtId(expectedExtId);
		beforeException.setExtId(expectedEventExtId);

		Event afterException = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		afterException.setExtId(new EventExtId(expectedExtId));
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		after.addEventException(afterException);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendeesException);
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventMultipleExceptions() {
		List<Attendee> expectedAttendeesException = createOrganiserAndContactAttendees(Participation.declined());
		Event beforeException = createEvent(expectedAttendeesException);

		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.needsAction());
		Event before = createEvent(expectedAttendees);
		before.addEventException(beforeException);
		String expectedExtId = "123";
		EventExtId expectedEventExtId = new EventExtId(expectedExtId);
		beforeException.setExtId(expectedEventExtId);

		Event firstException = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		firstException.setExtId(new EventExtId("012"));
		Event afterException = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		afterException.setExtId(new EventExtId(expectedExtId));
		Event thirdException = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		thirdException.setExtId(new EventExtId("234"));
		Event after = createEvent(createOrganiserAndContactAttendees(Participation.accepted()));
		after.addEventException(thirdException);
		after.addEventException(firstException);
		after.addEventException(afterException);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendeesException);
	}

	@Test
	public void testRecursiveInheritsParticipationFromExistingEventNotFoundExceptions() {
		Event beforeException = createEvent(createOrganiserAndContactAttendees(Participation.declined()));

		Event before = createEvent(createOrganiserAndContactAttendees(Participation.needsAction()));
		before.addEventException(beforeException);
		beforeException.setExtId(new EventExtId("123"));

		List<Attendee> expectedAttendees = createOrganiserAndContactAttendees(Participation.accepted());
		Event firstException = createEvent(expectedAttendees);
		firstException.setExtId(new EventExtId("012"));
		Event afterException = createEvent(expectedAttendees);
		afterException.setExtId(new EventExtId("123"));
		Event thirdException = createEvent(expectedAttendees);
		thirdException.setExtId(new EventExtId("234"));
		Event after = createEvent(expectedAttendees);
		after.addEventException(thirdException);
		after.addEventException(firstException);
		after.addEventException(afterException);

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.inheritsParticipationFromExistingEvent(before, after);
		assertThat(afterException.getAttendees()).isEqualTo(expectedAttendees);
	}

	@Test
	public void testBuildTreeMapEmptyList() {
		ImmutableSet<Event> events = ImmutableSet.<Event> of();

		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		SortedMap<Event, Event> treeMap = calendarBinding.buildSortedMap(events);
		assertThat(treeMap).isEmpty();
	}

	@Test
	public void testBuildTreeMapNullList() {
		CalendarBindingImpl calendarBinding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		SortedMap<Event, Event> treeMap = calendarBinding.buildSortedMap(null);
		assertThat(treeMap).isEmpty();
	}

	@Test
	public void testBuildTreeMap() {
		DateTime eventDate = DateTime.now();
		List<Attendee> attendees = createOrganiserAndContactAttendees(Participation.accepted());
		Event firstException = createEventException(attendees, eventDate.plusDays(1).toDate());
		Event secondException = createEventException(attendees, eventDate.plusDays(2).toDate());
		Event thirdException = createEventException(attendees, eventDate.plusDays(3).toDate());

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);
		SortedMap<Event, Event> treeMap = calendarBinding.buildSortedMap(ImmutableSet.of(firstException, secondException, thirdException));

		assertThat(treeMap.keySet()).containsExactly(firstException, secondException, thirdException);
	}

    @Test
	public void testGetResourceEvents() throws ServerFault, FindException {
		String resourceEmail = "resource@domain";
		Event mockEvent1 = createMock(Event.class);
		Event mockEvent2 = createMock(Event.class);
		Collection<Event> expectedEvents = Lists.newArrayList(mockEvent1, mockEvent2);

		Date date = new Date();
		Date threeMonthsBefore = new org.joda.time.DateTime(date).minus(Months.THREE).toDate();
		Date sixMonthsAfter = new org.joda.time.DateTime(date).plus(Months.SIX).toDate();
		SyncRange syncRange = new SyncRange(sixMonthsAfter, threeMonthsBefore);
		CalendarDao mockDao = createMock(CalendarDao.class);
		ResourceInfo mockResource = createMock(ResourceInfo.class);
		expect(mockDao.getResource(resourceEmail)).andReturn(mockResource);
		expect(mockDao.getResourceEvents(mockResource, syncRange)).andReturn(expectedEvents);

		Object[] mocks = { mockEvent1, mockEvent2, mockDao, mockResource };
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, mockDao,
				null, null, null, null, null, null, attendeeService, null, null);
		Collection<Event> events = calendarBinding.getResourceEvents(resourceEmail, date, syncRange);
		assertThat(events).isEqualTo(expectedEvents);

		verify(mocks);
    }

	@Test(expected=ResourceNotFoundException.class)
	public void testGetResourceEventsWithMissingResource() throws ServerFault, FindException {
		String resourceEmail = "resource@domain";

		CalendarDao mockDao = createMock(CalendarDao.class);
		expect(mockDao.getResource(resourceEmail)).andReturn(null);

		Object[] mocks = { mockDao };
		replay(mocks);

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, mockDao,
				null, null, null, null, null, null, attendeeService, null, null);
		try {
			calendarBinding.getResourceEvents(resourceEmail, new Date(), new SyncRange(null, null));
		}
		finally {
			verify(mocks);
		}
	}

	@Test
	public void testGetResourceEventsWithNullRange() throws ServerFault, FindException {
		String resourceEmail = "resource@domain";
		Event mockEvent1 = createMock(Event.class);
		Event mockEvent2 = createMock(Event.class);
		Collection<Event> expectedEvents = Lists.newArrayList(mockEvent1, mockEvent2);

		Date date = new Date();
		SyncRange defaultRange = new SyncRange(
				new org.joda.time.DateTime(date).plus(Months.SIX).toDate(),
				new org.joda.time.DateTime(date).minus(Months.THREE).toDate());
		CalendarDao mockDao = createMock(CalendarDao.class);
		ResourceInfo mockResource = createMock(ResourceInfo.class);
		expect(mockDao.getResource(resourceEmail)).andReturn(mockResource);
		expect(mockDao.getResourceEvents(mockResource, defaultRange)).andReturn(expectedEvents);

		Object[] mocks = { mockEvent1, mockEvent2, mockDao, mockResource };

		replay(mocks);
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, mockDao,
				null, null, null, null, null, null, attendeeService, null, null);
		Collection<Event> events = calendarBinding.getResourceEvents(resourceEmail, date, null);
		verify(mocks);

		assertThat(events).isEqualTo(expectedEvents);
	}

	@Test
	public void testApplyParticipationStateModificationsWithoutDelegations() {
		Attendee organizer = UserAttendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.accepted()).build();
		Attendee att1 = UserAttendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.needsAction()).build();
		Event before = createEvent(Arrays.asList(organizer)), event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.applyParticipationModifications(before, event);

		assertThat(att1.getParticipation()).isEqualTo(Participation.needsAction());
	}

	@Test
	public void testApplyParticipationStateModificationsWithDelegations() {
		Attendee organizer = UserAttendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.accepted()).build();
		Attendee att1 = UserAttendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.needsAction()).build();
		Event before = createEvent(Arrays.asList(organizer)), event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		att1.setCanWriteOnCalendar(true);
		calendarBinding.applyParticipationModifications(before, event);

		assertThat(att1.getParticipation()).isEqualTo(Participation.accepted());
	}

	@Test
	public void testApplyParticipationStateModificationsWithDelegationsWithExceptions() {
		Attendee organizer = UserAttendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.accepted()).build();
		Attendee organizerForExc = UserAttendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.accepted()).build();
		Attendee att1 = UserAttendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.needsAction()).build();
		Attendee att1ForExc = UserAttendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.needsAction()).build();
		Event exception = createEvent(Arrays.asList(organizerForExc, att1ForExc));
		Event before = createEvent(Arrays.asList(organizer)), event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		att1.setCanWriteOnCalendar(true);
		att1ForExc.setCanWriteOnCalendar(true);
		event.addEventException(exception);
		calendarBinding.applyParticipationModifications(before, event);

		assertThat(att1.getParticipation()).isEqualTo(Participation.accepted());
		assertThat(att1ForExc.getParticipation()).isEqualTo(Participation.accepted());
	}

	@Test
	public void testInitDefaultParticipationState() {
		Attendee organizer = UserAttendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.accepted()).build();
		Attendee att1 = UserAttendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.accepted()).build();
		Event event = createEvent(Arrays.asList(organizer, att1));
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		calendarBinding.initDefaultParticipation(event);

		assertThat(organizer.getParticipation()).isEqualTo(Participation.needsAction());
		assertThat(att1.getParticipation()).isEqualTo(Participation.needsAction());
	}

	@Test
	public void testInitDefaultParticipationStateWithExceptions() {
		Attendee organizer = UserAttendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.accepted()).build();
		Attendee organizerForExc = UserAttendee.builder().asOrganizer().email("organizer@eve.nt").participation(Participation.accepted()).build();
		Attendee att1 = UserAttendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.accepted()).build();
		Attendee att1ForExc = UserAttendee.builder().asAttendee().email("att1@eve.nt").participation(Participation.accepted()).build();
		Event event = createEvent(Arrays.asList(organizer, att1));
		Event exception = createEvent(Arrays.asList(organizerForExc, att1ForExc));
		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);

		event.addEventException(exception);
		calendarBinding.initDefaultParticipation(event);

		assertThat(organizer.getParticipation()).isEqualTo(Participation.needsAction());
		assertThat(att1.getParticipation()).isEqualTo(Participation.needsAction());
		assertThat(organizerForExc.getParticipation()).isEqualTo(Participation.needsAction());
		assertThat(att1ForExc.getParticipation()).isEqualTo(Participation.needsAction());
	}

	@Test(expected=NotAllowedException.class)
	public void testRemoveEventByIdNoWriteRights() throws Exception {
		EventObmId eventId = new EventObmId(1);

		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.removeEventById(token, "calendar", eventId, 0, false);
	}

	@Test(expected=NotAllowedException.class)
	public void testRemoveEventByExtIdNoWriteRights() throws Exception {
		EventExtId eventExtId = new EventExtId("abc");

		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.removeEventByExtId(token, "calendar", eventExtId, 0, false);
	}

	@Test(expected=NotAllowedException.class)
	public void testChangeParticipationStateNoWriteRights() throws Exception {
		EventExtId eventExtId = new EventExtId("abc");

		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.changeParticipationState(token, "calendar", eventExtId, Participation.accepted(), 0, false);
	}

	@Test(expected=NotAllowedException.class)
	public void testChangeParticipationStateRecurrentNoWriteRights() throws Exception {
		EventExtId eventExtId = new EventExtId("abc");
		RecurrenceId recurrenceId = new RecurrenceId("RecurrenceId");

		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.changeParticipationState(token, "calendar", eventExtId, recurrenceId, Participation.accepted(), 0, false);
	}

	@Test(expected=NotAllowedException.class)
	public void testGetSyncNoReadRights() throws Exception {
		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.getSync(token, "calendar", null);
	}

	@Test(expected=NotAllowedException.class)
	public void testGetSyncInRangeNoReadRights() throws Exception {
		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.getSyncInRange(token, "calendar", null, new SyncRange(null, DateUtils.date("2012-01-01T00:00:00")));
	}

	@Test(expected=NotAllowedException.class)
	public void testGetSyncWithSortedChangedNoReadRights() throws Exception {
		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.getSyncWithSortedChanges(token, "calendar", null, null);
	}

	@Test(expected=NotAllowedException.class)
	public void testGetSyncEventDateNoReadRights() throws Exception {
		expectNoRightsForCalendar("calendar");
		mocksControl.replay();

		binding.getSyncEventDate(token, "calendar", null);
	}

	@Test
	public void testCreateInternalEventConvertsAttendees() throws Exception {
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();
		String attendeeEmail = "test@obm.org";
		String resourceEmail = "resource@obm.org";
		String clientId = "123";

		UserAttendee userAttendee = UserAttendee.builder().email(calendar).build();
		ContactAttendee contactAttendee = ContactAttendee.builder().email(attendeeEmail).build();
		ResourceAttendee resourceAttendee = ResourceAttendee.builder().email(resourceEmail).build();
		AttendeeService attendeeService = mocksControl.createMock(AttendeeService.class);

		List<Attendee> attendees = ImmutableList.of(ToolBox.getFakeAttendee(calendar), ToolBox.getFakeAttendee(attendeeEmail), ToolBox.getFakeAttendee(resourceEmail));
		Event event = createEvent(attendees);
		Event exception = createEventException(attendees, DateUtils.date("2012-01-01T00:00:00"));
		Event exception2 = createEventException(ImmutableList.of(ToolBox.getFakeAttendee(calendar)), DateUtils.date("2012-02-01T00:00:00"));

		event.setEntityId(EntityId.valueOf(6));
		event.setInternalEvent(true);
		exception.setInternalEvent(true);
		exception2.setInternalEvent(true);
		event.addEventException(exception);
		event.addEventException(exception2);
		event.getRecurrence().setKind(RecurrenceKind.daily);

		mockCommitedOperationNewEvent(event, clientId);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true).atLeastOnce();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(calendar, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token, ImmutableSet.of(calendar))).andReturn(
				calendarToRights).atLeastOnce();

		expect(userService.getUserFromCalendar(calendar, user.getDomain().getName())).andReturn(user).anyTimes();
		// times(3) = 1 for the event, 1 for each exception
		expect(attendeeService.findUserAttendee(null, calendar, user.getDomain())).andReturn(userAttendee).times(3);
		// times(2) = 1 for the event, 1 for the first exception
		expect(attendeeService.findAttendee(null, attendeeEmail, true, user.getDomain(), user.getUid())).andReturn(contactAttendee).times(2);
		expect(attendeeService.findAttendee(null, resourceEmail, true, user.getDomain(), user.getUid())).andReturn(resourceAttendee).times(2);
		expect(calendarDao.createEvent(token, calendar, event)).andReturn(event);
		expect(calendarDao.findEventById(token, null)).andReturn(event);
		messageQueueService.writeIcsInvitationRequest(token, event);
		expectLastCall();


		mocksControl.replay();

		CalendarBindingImpl binding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helperService, null, null, attendeeService, null, null);
		binding.createEvent(token, calendar, event, false, clientId);

		mocksControl.verify();
	}

	@Test
	public void testCreateInternalWhenOwnerUseAliasInEvent() throws Exception {
		String userEventAlias = "alias2@obm.org";
		ObmUser user = ToolBox.getDefaultObmUserWithEmails("main@obm.org", "alias1@obm.org", userEventAlias, "alias3@obm.org");
		String userEmail = user.getEmailAtDomain();
		String attendeeEmail = "test@obm.org";
		String clientId = "123";

		UserAttendee userAttendee = UserAttendee.builder().email(userEmail).build();
		ContactAttendee contactAttendee = ContactAttendee.builder().email(attendeeEmail).build();
		AttendeeService attendeeService = mocksControl.createMock(AttendeeService.class);

		List<Attendee> incommingAttendees = ImmutableList.of(ToolBox.getFakeAttendee(userEventAlias), ToolBox.getFakeAttendee(attendeeEmail));
		List<Attendee> toStoreAttendees = ImmutableList.of(ToolBox.getFakeAttendee(userEmail), ToolBox.getFakeAttendee(attendeeEmail));
		Event incommingEvent = createEvent(incommingAttendees);
		incommingEvent.setInternalEvent(true);
		incommingEvent.setEntityId(EntityId.valueOf(7));
		Event toStoreEvent = createEvent(toStoreAttendees);
		toStoreEvent.setInternalEvent(true);
		toStoreEvent.setEntityId(EntityId.valueOf(7));

		mockCommitedOperationNewEvent(incommingEvent, clientId);
		expect(helperService.canWriteOnCalendar(token, userEmail)).andReturn(true).atLeastOnce();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(userEmail, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token,
						ImmutableSet.of(userEmail))).andReturn(calendarToRights)
				.atLeastOnce();
		expect(helperService.canWriteOnCalendar(token, attendeeEmail)).andReturn(false).anyTimes();
		expect(userService.getUserFromCalendar(userEmail, user.getDomain().getName())).andReturn(user).anyTimes();
		expect(attendeeService.findUserAttendee(null, userEventAlias, user.getDomain())).andReturn(userAttendee);
		expect(attendeeService.findAttendee(null, attendeeEmail, true, user.getDomain(), user.getUid()))
			.andReturn(contactAttendee);
		expect(attendeeService.findResourceAttendee(null, attendeeEmail, user.getDomain(), user.getUid()))
			.andReturn(null).anyTimes();

		expect(calendarDao.createEvent(token, userEmail, toStoreEvent)).andReturn(toStoreEvent);
		expect(calendarDao.findEventById(token, null)).andReturn(toStoreEvent);
		messageQueueService.writeIcsInvitationRequest(token, toStoreEvent);
		expectLastCall();

		mocksControl.replay();

		CalendarBindingImpl binding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helperService, null, null, attendeeService, null, null);
		binding.createEvent(token, userEmail, incommingEvent, false, clientId);

		mocksControl.verify();
	}

	@Test
	public void testCreateExternalEvent() throws Exception {
		ObmUser user = ToolBox.getDefaultObmUser();
		String userEmail = user.getEmailAtDomain();
		String attendeeEmail = "2" + user.getEmailAtDomain();
		String clientId = "123";

		UserAttendee userAttendee = UserAttendee.builder().email(userEmail).build();
		ContactAttendee contactAttendee = ContactAttendee.builder().email(attendeeEmail).build();
		AttendeeService attendeeService = mocksControl.createMock(AttendeeService.class);

		List<Attendee> nonTypedAttendees = ImmutableList.of(ToolBox.getFakeAttendee(userEmail), ToolBox.getFakeAttendee(attendeeEmail));
		Event event = createEvent(nonTypedAttendees);
		event.setInternalEvent(false);
		event.setEntityId(EntityId.valueOf(5));

		mockCommitedOperationNewEvent(event, clientId);
		expect(helperService.canWriteOnCalendar(token, userEmail)).andReturn(true).atLeastOnce();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(userEmail, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token, ImmutableSet.of(userEmail))).andReturn(calendarToRights).atLeastOnce();
		expect(helperService.canWriteOnCalendar(token, attendeeEmail)).andReturn(false).anyTimes();
		expect(userService.getUserFromCalendar(userEmail, user.getDomain().getName())).andReturn(user).anyTimes();
		expect(attendeeService.findUserAttendee(null, userEmail, user.getDomain())).andReturn(userAttendee);
		expect(attendeeService.findContactAttendee(null, attendeeEmail, true, user.getDomain(), user.getUid()))
			.andReturn(contactAttendee);
		expect(calendarDao.createEvent(token, userEmail, event)).andReturn(event);
		messageQueueService.writeIcsInvitationReply(token, event, user);
		expectLastCall();

		mocksControl.replay();

		CalendarBindingImpl binding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helperService, null, null, attendeeService, null, null);
		binding.createEvent(token, userEmail, event, false, clientId);

		mocksControl.verify();
	}

	@Test
	public void testCreateExternalEventAlreadyCommited() throws Exception {
		ObmUser user = ToolBox.getDefaultObmUser();
		String userEmail = user.getEmail();
		String attendeeEmail = "2" + user.getEmail();
		String clientId = "123";

		UserAttendee userAttendee = UserAttendee.builder().email(userEmail).build();
		ContactAttendee contactAttendee = ContactAttendee.builder().email(attendeeEmail).build();
		AttendeeService attendeeService = mocksControl.createMock(AttendeeService.class);

		List<Attendee> nonTypedAttendees = ImmutableList.of(ToolBox.getFakeAttendee(userEmail), ToolBox.getFakeAttendee(attendeeEmail));
		Event event = createEvent(nonTypedAttendees);
		event.setInternalEvent(false);
		event.setEntityId(EntityId.valueOf(5));

		mockCommitedOperationExistingEvent(event, clientId);
		expect(helperService.canWriteOnCalendar(token, userEmail)).andReturn(true).anyTimes();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(userEmail, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token, ImmutableSet.of(userEmail))).andReturn(calendarToRights).atLeastOnce();
		expect(userService.getUserFromCalendar(userEmail, user.getDomain().getName())).andReturn(user).anyTimes();
		expect(attendeeService.findUserAttendee(null, userEmail, user.getDomain())).andReturn(userAttendee);
		expect(attendeeService.findContactAttendee(null, attendeeEmail, true, user.getDomain(), user.getUid()))
			.andReturn(contactAttendee);

		mocksControl.replay();

		CalendarBindingImpl binding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helperService, null, null, attendeeService, null, null);
		binding.createEvent(token, userEmail, event, false, clientId);

		mocksControl.verify();
	}

	@Test
	public void testCreateExternalEventWithExceptions() throws Exception {
		ObmUser user = ToolBox.getDefaultObmUser();
		String userEmail = user.getEmailAtDomain();
		String attendeeEmail = "2" + user.getEmailAtDomain();
		String clientId = "123";

		UserAttendee userAttendee = UserAttendee.builder().email(userEmail).build();
		ContactAttendee contactAttendee = ContactAttendee.builder().email(attendeeEmail).build();
		AttendeeService attendeeService = mocksControl.createMock(AttendeeService.class);

		List<Attendee> nonTypedAttendees = ImmutableList.of(ToolBox.getFakeAttendee(userEmail), ToolBox.getFakeAttendee(attendeeEmail));
		Event event = createEvent(nonTypedAttendees);
		Event exception = createEventException(nonTypedAttendees, DateUtils.date("2012-01-01T00:00:00"));
		Event exception2 = createEventException(ImmutableList.of(ToolBox.getFakeAttendee(userEmail)), DateUtils.date("2012-02-01T00:00:00"));

		event.setEntityId(EntityId.valueOf(4));
		event.setInternalEvent(false);
		exception.setInternalEvent(false);
		exception2.setInternalEvent(false);
		event.addEventException(exception);
		event.addEventException(exception2);
		event.getRecurrence().setKind(RecurrenceKind.daily);

		mockCommitedOperationNewEvent(event, clientId);
		expect(helperService.canWriteOnCalendar(token, userEmail)).andReturn(true).anyTimes();
		CalendarRights calendarToRights2 = CalendarRights.builder()
				.addRights(userEmail, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token, ImmutableSet.of(userEmail))).andReturn(calendarToRights2).atLeastOnce();
		expect(userService.getUserFromCalendar(userEmail, user.getDomain().getName())).andReturn(user).anyTimes();
		// times(3) = 1 for the event, 1 for each exception
		expect(attendeeService.findUserAttendee(null, userEmail, user.getDomain())).andReturn(userAttendee).times(3);
		expect(attendeeService.findContactAttendee(null, attendeeEmail, true, user.getDomain(), user.getUid())).andReturn(contactAttendee).times(2);
		expect(calendarDao.createEvent(token, userEmail, event)).andReturn(event);
		messageQueueService.writeIcsInvitationReply(token, event, user);
		expectLastCall();

		mocksControl.replay();

		CalendarBindingImpl binding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helperService, null, null, attendeeService, null, null);
		binding.createEvent(token, userEmail, event, false, clientId);

		mocksControl.verify();
	}

	@Test
	public void testCreateInternalEventAlreadyCommited() throws Exception {
		ObmUser defaultUser = ToolBox.getDefaultObmUser();
		String calendar = "cal1";
		EventExtId extId = new EventExtId("extId");
		boolean notification = false;
		String clientId = "123";

		Attendee calOwner = ToolBox.getFakeAttendee(defaultUser.getEmailAtDomain());
		calOwner.setParticipation(Participation.declined());

		Event event = new Event();
		event.setType(EventType.VEVENT);
		event.setInternalEvent(true);
		event.setExtId(extId);
		event.setSequence(0);
		event.addAttendee(calOwner);
		event.setEntityId(EntityId.valueOf(4));

		AccessToken accessToken = mockAccessToken(calendar, defaultUser.getDomain());
		HelperService helper = mockRightsHelper(calendar, accessToken);

		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(defaultUser.getEmail(), EnumSet.of(Right.ACCESS))
				.build();
		expect(helper.listRightsOnCalendars(accessToken, ImmutableSet.of(defaultUser.getEmail())))
				.andReturn(calendarToRights).atLeastOnce();

		EventChangeHandler eventChangeHandler = createMock(EventChangeHandler.class);

		expect(userService.getUserFromCalendar(calendar, defaultUser.getDomain().getName())).andReturn(defaultUser).atLeastOnce();
		expect(calendarDao.findEventByExtId(accessToken, defaultUser, event.getExtId())).andReturn(null).once();

		CommitedOperation<Event> commitedOperation = new CommitedOperation<Event>(event, null);
		CommitedOperationDao commitedOperationDao = createMock(CommitedOperationDao.class);
		expect(commitedOperationDao.findAsEvent(accessToken, clientId)).andReturn(commitedOperation).once();
		commitedOperationDao.store(accessToken, CommitedElement.builder()
				.entityId(event.getEntityId())
				.clientId(clientId)
				.kind(Kind.VEVENT)
				.build());
		expectLastCall().once();

		replay(accessToken, helper, eventChangeHandler, commitedOperationDao);
		mocksControl.replay();

		CalendarBindingImpl calendarBinding = new CalendarBindingImpl(eventChangeHandler, null, userService, calendarDao, calendarService, null, commitedOperationDao, helper, null, null, attendeeService, null, null);
		EventObmId eventObmId = calendarBinding.createEvent(accessToken, calendar, event, notification, clientId);

		verify(accessToken, helper, calendarDao, userService, eventChangeHandler, commitedOperationDao);
		mocksControl.verify();
		assertThat(eventObmId).isEqualTo(event.getObmId());
	}

	@Test(expected=NotAllowedException.class)
	public void testImportICSWhenNoWriteRightOnCalendar() throws Exception {
		String calendar = "user";
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/4Events.ics"));

		expect(helperService.canWriteOnCalendar(eq(token), eq(calendar))).andReturn(false);
		
		try {
			mocksControl.replay();
			binding.importICalendar(token, calendar, ics, null);
		} finally {
			mocksControl.verify();
		}
	}

	@Test
	public void testImportICSWhenWriteRightOnCalendar() throws Exception {
		ObmUser obmUser = ToolBox.getDefaultObmUser();
		String domainName = "test.tlse.lng";
		String calendar = "user";
		String ics = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("ics/4Events.ics"));

		expect(helperService.canWriteOnCalendar(eq(token), eq(calendar))).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, domainName)).andReturn(obmUser);
		expect(userService.getUserFromAccessToken(token)).andReturn(obmUser);
		expect(userService.getUserFromAttendee(isA(Attendee.class), eq(domainName))).andReturn(obmUser).anyTimes();
		expect(calendarDao.findEventByExtId(eq(token), eq(obmUser), isA(EventExtId.class))).andReturn(null).anyTimes();
		expect(calendarDao.createEvent(eq(token), eq(calendar), isA(Event.class))).andReturn(null).anyTimes();
		mocksControl.replay();

		binding.importICalendar(token, calendar, ics, null);

		mocksControl.verify();
	}

	@Test
	public void testSortUpdatedEvents() {

		DeletedEvent deletedEvent = DeletedEvent.builder().eventObmId(0).eventExtId("deleted_event").build();
		Set<DeletedEvent> deletedEvents = Sets.newHashSet(deletedEvent);

		Event updatedSimpleEvent = getFakeEvent(1, RecurrenceKind.none);
		updatedSimpleEvent.setTimeCreate(DateUtils.date("2012-01-01T14:00:01Z"));
		updatedSimpleEvent.setTimeUpdate(null);
		updatedSimpleEvent.setRecurrenceId(null);

		Event updatedRecurrenceEvent = getFakeEvent(2, RecurrenceKind.daily);
		updatedRecurrenceEvent.setTimeCreate(DateUtils.date("2012-01-01T13:59:58Z"));
		updatedRecurrenceEvent.setTimeUpdate(DateUtils.date("2012-01-01T13:59:59Z"));

		Event shouldMoveSimpleEvent = getFakeEvent(3, RecurrenceKind.none);
		shouldMoveSimpleEvent.setTimeCreate(DateUtils.date("2012-01-01T13:59:58Z"));
		shouldMoveSimpleEvent.setTimeUpdate(DateUtils.date("2012-01-01T13:59:59Z"));
		shouldMoveSimpleEvent.setRecurrenceId(null);

		Event shouldMoveEventException = getFakeEvent(4, RecurrenceKind.none);
		shouldMoveEventException.setRecurrenceId(DateUtils.date("2012-01-01T13:00:00Z"));
		shouldMoveEventException.setTimeCreate(DateUtils.date("2012-01-01T13:59:57Z"));
		shouldMoveEventException.setTimeUpdate(null);

		Date lastSync = DateUtils.date("2012-01-01T14:00:00Z");

		EventChanges eventChangesToSort =
				EventChanges.builder()
					.lastSync(lastSync)
					.deletes(deletedEvents)
					.updates(Lists.newArrayList(
							updatedSimpleEvent, updatedRecurrenceEvent,
							shouldMoveEventException, shouldMoveSimpleEvent))
					.build();

		EventChanges sortedEventChanges = binding.sortUpdatedEvents(eventChangesToSort, lastSync);

		final Attendee attendee = ToolBox.getFakeAttendee("user2@domain1");
		attendee.setParticipation(Participation.accepted());

		ParticipationChanges expectedParticipationChange1 =
				ParticipationChanges.builder()
					.eventExtId("3")
					.eventObmId(3)
					.attendees(Lists.newArrayList(attendee))
					.build();

		ParticipationChanges expectedParticipationChange2 =
				ParticipationChanges.builder()
				.eventExtId("4")
				.eventObmId(4)
				.attendees(Lists.newArrayList(attendee))
				.recurrenceId("20120101T130000Z")
				.build();

		EventChanges expectedEventChanges =
			EventChanges.builder()
				.lastSync(lastSync)
				.deletes(deletedEvents)
				.participationChanges(
						Lists.newArrayList(expectedParticipationChange1, expectedParticipationChange2))
				.updates(Lists.newArrayList(updatedSimpleEvent, updatedRecurrenceEvent))
				.build();

		assertThat(sortedEventChanges).isEqualTo(expectedEventChanges);
	}

	@Test
	public void testSortShouldMoveAllUpdatedEvents() {

		DeletedEvent deletedEvent = DeletedEvent.builder().eventObmId(0).eventExtId("deleted_event").build();
		Set<DeletedEvent> deletedEvents = Sets.newHashSet(deletedEvent);

		Event shouldMoveSimpleEvent = getFakeEvent(3, RecurrenceKind.none);
		shouldMoveSimpleEvent.setTimeCreate(DateUtils.date("2012-01-01T13:59:58Z"));
		shouldMoveSimpleEvent.setTimeUpdate(DateUtils.date("2012-01-01T13:59:59Z"));
		shouldMoveSimpleEvent.setRecurrenceId(null);

		Event shouldMoveEventException = getFakeEvent(4, RecurrenceKind.none);
		shouldMoveEventException.setRecurrenceId(DateUtils.date("2012-01-01T13:00:00Z"));
		shouldMoveEventException.setTimeCreate(DateUtils.date("2012-01-01T13:59:57Z"));
		shouldMoveEventException.setTimeUpdate(null);

		Date lastSync = DateUtils.date("2012-01-01T14:00:00Z");

		EventChanges eventChangesToSort =
				EventChanges.builder()
					.lastSync(lastSync)
					.deletes(deletedEvents)
					.updates(Lists.newArrayList(shouldMoveEventException, shouldMoveSimpleEvent))
					.build();

		EventChanges sortedEventChanges = binding.sortUpdatedEvents(eventChangesToSort, lastSync);

		final Attendee attendee = ToolBox.getFakeAttendee("user2@domain1");
		attendee.setParticipation(Participation.accepted());

		ParticipationChanges expectedParticipationChange1 =
				ParticipationChanges.builder()
					.eventExtId("3")
					.eventObmId(3)
					.attendees(Lists.newArrayList(attendee))
					.build();

		ParticipationChanges expectedParticipationChange2 =
				ParticipationChanges.builder()
				.eventExtId("4")
				.eventObmId(4)
				.attendees(Lists.newArrayList(attendee))
				.recurrenceId("20120101T130000Z")
				.build();

		EventChanges expectedEventChanges =
			EventChanges.builder()
				.lastSync(lastSync)
				.deletes(deletedEvents)
				.participationChanges(Lists.newArrayList(expectedParticipationChange1, expectedParticipationChange2))
				.build();

		assertThat(sortedEventChanges).isEqualTo(expectedEventChanges);
	}

	@Test
	public void testSortUpdatedEventsFirstSyncDoNothing() {
		DeletedEvent deletedEvent = DeletedEvent.builder().eventObmId(0).eventExtId("deleted_event").build();
		Set<DeletedEvent> deletedEvents = Sets.newHashSet(deletedEvent);

		Event updatedSimpleEvent = getFakeEvent(1, RecurrenceKind.none);
		updatedSimpleEvent.setTimeCreate(DateUtils.date("2012-01-01T14:00:01"));
		updatedSimpleEvent.setTimeUpdate(null);

		Event updatedRecurrenceEvent = getFakeEvent(2, RecurrenceKind.daily);
		updatedRecurrenceEvent.setTimeCreate(DateUtils.date("2012-01-01T13:59:59"));
		updatedRecurrenceEvent.setTimeUpdate(DateUtils.date("2012-01-01T14:00:01"));

		Date lastSync = DateUtils.date("2012-01-01T14:00:00");

		EventChanges eventChangesToSort =
				EventChanges.builder()
					.lastSync(lastSync)
					.deletes(deletedEvents)
					.updates(Lists.newArrayList(updatedSimpleEvent, updatedRecurrenceEvent))
					.build();

		EventChanges sortedEventChanges = binding.sortUpdatedEvents(eventChangesToSort, lastSync);

		EventChanges expectedEventChanges =
			EventChanges.builder()
				.lastSync(lastSync)
				.deletes(deletedEvents)
				.updates(Lists.newArrayList(updatedSimpleEvent, updatedRecurrenceEvent))
				.build();

		assertThat(sortedEventChanges).isEqualTo(expectedEventChanges);
	}

	@Test
	public void testGetSyncWithSortedChangesInheritsAlertFromEventOwnerIfNotSet() throws Exception {
		String calendar = "user";
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();
		Event event = new Event();
		EventChanges eventChanges = EventChanges.builder().updates(Sets.newHashSet(event)).lastSync(new Date()).build();

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, domain.getName())).andReturn(calendarUser);
		expect(calendarDao.getSync(token, calendarUser, null, null, null, false)).andReturn(eventChanges);
		expect(calendarDao.getEventAlertForUser(null, 2)).andReturn(30);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(eventChanges).once();
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		mocksControl.replay();

		EventChanges changesFromService = binding.getSyncWithSortedChanges(token, calendar, null, null);

		assertThat(Iterables.getFirst(changesFromService.getUpdated(), null).getAlert()).isEqualTo(30);
		mocksControl.verify();
	}

	@Test
	public void testGetSyncWithSortedChangesDoesntInheritAlertFromEventOwnerIfNotInDelegation() throws Exception {
		String calendar = "user";
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();
		Event event = new Event();
		EventChanges eventChanges = EventChanges.builder().updates(Sets.newHashSet(event)).lastSync(new Date()).build();

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, domain.getName())).andReturn(calendarUser);
		expect(calendarDao.getEventAlertForUser(null, 2)).andReturn(null);
		expect(calendarDao.getSync(token, calendarUser, null, null, null, false)).andReturn(eventChanges);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(eventChanges).once();

		mocksControl.replay();

		EventChanges changesFromService = binding.getSyncWithSortedChanges(token, calendar, null, null);
		mocksControl.verify();
		assertThat(Iterables.getFirst(changesFromService.getUpdated(), null).getAlert()).isNull();
		
	}

	@Test
	public void testGetSyncWithSortedChangesDoesntInheritAlertFromEventOwnerIfSet() throws Exception {
		String calendar = "user";
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();
		Event event = new Event();

		event.setAlert(10);

		EventChanges eventChanges = EventChanges.builder().updates(Sets.newHashSet(event)).lastSync(new Date()).build();

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, domain.getName())).andReturn(calendarUser);
		expect(calendarDao.getSync(token, calendarUser, null, null, null, false)).andReturn(eventChanges);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq(calendar), eq(token))).andReturn(eventChanges).once();

		mocksControl.replay();

		EventChanges changesFromService = binding.getSyncWithSortedChanges(token, calendar, null, null);

		assertThat(Iterables.getFirst(changesFromService.getUpdated(), null).getAlert()).isEqualTo(10);
		mocksControl.verify();
	}

	@Test
	public void testGetEventFromIdInheritsAlertFromEventOwnerIfNotSet() throws Exception {
		String calendar = "user";
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();
		Event event = new Event();
		EventObmId eventId = new EventObmId(1);

		event.setOwner(calendar);

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(calendarDao.findEventById(token, eventId)).andReturn(event);
		expect(userService.getUserFromLogin(calendar, domain.getName())).andReturn(calendarUser);
		expect(calendarDao.getEventAlertForUser(null, 2)).andReturn(30);
		mocksControl.replay();

		Event eventFromService = binding.getEventFromId(token, calendar, eventId);

		assertThat(eventFromService.getAlert()).isEqualTo(30);
		mocksControl.verify();
	}

	@Test
	public void testGetEventFromIdDoesntInheritAlertFromEventOwnerIfNotInDelegation() throws Exception {
		String calendar = "user";
		Event event = new Event();
		EventObmId eventId = new EventObmId(1);

		event.setOwner(calendar);

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(false);
		expect(calendarDao.findEventById(token, eventId)).andReturn(event);
		mocksControl.replay();

		Event eventFromService = binding.getEventFromId(token, calendar, eventId);

		assertThat(eventFromService.getAlert()).isNull();
		mocksControl.verify();
	}

	@Test
	public void testGetEventFromIdDoesntInheritAlertFromEventOwnerIfSet() throws Exception {
		String calendar = "user";
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();
		Event event = new Event();
		EventObmId eventId = new EventObmId(1);

		event.setAlert(10);
		event.setOwner(calendar);

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(calendarDao.findEventById(token, eventId)).andReturn(event);
		expect(userService.getUserFromLogin(calendar, domain.getName())).andReturn(calendarUser);
		mocksControl.replay();

		Event eventFromService = binding.getEventFromId(token, calendar, eventId);

		assertThat(eventFromService.getAlert()).isEqualTo(10);
		mocksControl.verify();
	}

	@Test
	public void testGetEventFromExtIdInheritsAlertFromEventOwnerIfNotSet() throws Exception {
		String calendar = "user";
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();
		Event event = new Event();
		EventExtId eventId = new EventExtId("1");

		event.setOwner(calendar);

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, domain.getName())).andReturn(calendarUser);
		expect(calendarDao.findEventByExtId(token, calendarUser, eventId)).andReturn(event);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		expect(calendarDao.getEventAlertForUser(null, 2)).andReturn(30);
		mocksControl.replay();

		Event eventFromService = binding.getEventFromExtId(token, calendar, eventId);

		assertThat(eventFromService.getAlert()).isEqualTo(30);
		mocksControl.verify();
	}

	@Test
	public void testGetEventFromExtIdDoesntInheritAlertFromEventOwnerIfNotInDelegation() throws Exception {
		String calendar = "user";
		Event event = new Event();
		EventExtId eventId = new EventExtId("1");
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();

		event.setOwner(calendar);

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, domain.getName())).andReturn(calendarUser);
		expect(calendarDao.findEventByExtId(token, calendarUser, eventId)).andReturn(event);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(false);
		mocksControl.replay();

		Event eventFromService = binding.getEventFromExtId(token, calendar, eventId);

		assertThat(eventFromService.getAlert()).isNull();
		mocksControl.verify();
	}

	@Test
	public void testGetEventFromExtIdDoesntInheritAlertFromEventOwnerIfSet() throws Exception {
		String calendar = "user";
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		ObmUser calendarUser = ObmUser.builder().login(UserLogin.valueOf(calendar)).uid(2).domain(domain).build();
		Event event = new Event();
		EventExtId eventId = new EventExtId("1");

		event.setAlert(10);
		event.setOwner(calendar);

		expect(helperService.canReadCalendar(token, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, domain.getName())).andReturn(calendarUser);
		expect(calendarDao.findEventByExtId(token, calendarUser, eventId)).andReturn(event);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true);
		mocksControl.replay();

		Event eventFromService = binding.getEventFromExtId(token, calendar, eventId);

		assertThat(eventFromService.getAlert()).isEqualTo(10);
		mocksControl.verify();
	}

	private void expectNoRightsForCalendar(String calendar) {
		expect(helperService.canWriteOnCalendar(eq(token), eq(calendar))).andReturn(false).anyTimes();
		expect(helperService.canReadCalendar(eq(token), eq(calendar))).andReturn(false).anyTimes();
	}

	private Event createEvent(List<Attendee> expectedAttendees) {
		Event event = new Event();

		event.addAttendees(expectedAttendees);

		return event;
	}

	private Event createEventException(List<Attendee> expectedAttendees, Date recurrenceId) {
		Event exception = createEvent(expectedAttendees);

		exception.setRecurrenceId(recurrenceId);

		return exception;
	}

	private List<Attendee> createOrganiserAndContactAttendees(Participation contactState) {
		return Arrays.asList(
				UserAttendee.builder()
				.asOrganizer()
				.participation(Participation.accepted())
				.email("organiser@test.lng").build(),
				ContactAttendee.builder()
				.participation(contactState)
				.email("attendee@test.lng").build());
	}

	private ResourceInfo buildResourceInfo1() {
		return ResourceInfo.builder().id(1).name("resource1").mail("res-1@domain.com").read(true).write(true).domainName("domain").build();
	}

	private ResourceInfo buildResourceInfo2() {
		return ResourceInfo.builder().id(2).name("resource2").mail("res-2@domain.com").read(true).write(false).domainName("domain").build();
	}

	@Test
	public void testStoreEventCreatesEventIfNotPresent() throws Exception {
		Event event = new Event();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();

		event.setExtId(new EventExtId("ExtId"));
		event.setInternalEvent(true);
		event.addAttendee(UserAttendee.builder().email(calendar).asOrganizer().build());

		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true).anyTimes();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(calendar, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token, ImmutableSet.of(calendar))).andReturn(calendarToRights).atLeastOnce();
		expect(userService.getUserFromLogin(calendar, user.getDomain().getName())).andReturn(user);
		expect(calendarDao.findEventByExtId(token, user, event.getExtId())).andReturn(null);
		expect(userService.getUserFromCalendar(calendar, user.getDomain().getName())).andReturn(user);
		expect(commitedOperationDao.findAsEvent(token, null)).andReturn(null);
		expect(calendarDao.createEvent(token, calendar, event)).andReturn(event);
		messageQueueService.writeIcsInvitationRequest(token, event);
		expectLastCall();
		expect(calendarDao.findEventById(token, null)).andReturn(event);
		mocksControl.replay();

		binding.storeEvent(token, calendar, event, false, null);

		mocksControl.verify();
	}

	@Test(expected = ServerFault.class)
	public void testStoreEventWithNullEvent() throws Exception {
		mocksControl.replay();

		try {
			binding.storeEvent(token, "calendar", null, false, null);
		} finally {
			mocksControl.verify();
		}
	}

	@Test(expected = NotAllowedException.class)
	public void testStoreEventWhenNotAllowedToCreateEvent() throws Exception {
		Event event = new Event();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();

		event.setExtId(new EventExtId("ExtId"));
		event.setInternalEvent(true);
		event.addAttendee(UserAttendee.builder().email(calendar).asOrganizer().build());

		expect(userService.getUserFromLogin(calendar, user.getDomain().getName())).andReturn(user);
		expect(calendarDao.findEventByExtId(token, user, event.getExtId())).andReturn(null);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(false).anyTimes();
		mocksControl.replay();

		try {
			binding.storeEvent(token, calendar, event, false, null);
		} finally {
			mocksControl.verify();
		}
	}

	@Test(expected = NotAllowedException.class)
	public void testStoreEventWhenNotAllowedToModifyEvent() throws Exception {
		Event event = new Event();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();

		event.setExtId(new EventExtId("ExtId"));
		event.setInternalEvent(true);
		event.addAttendee(UserAttendee.builder().email(calendar).asOrganizer().build());

		expect(userService.getUserFromLogin(calendar, user.getDomain().getName())).andReturn(user);
		expect(calendarDao.findEventByExtId(token, user, event.getExtId())).andReturn(event).anyTimes();
		expect(userService.getUserFromCalendar(calendar, user.getDomain().getName())).andReturn(user);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(false).anyTimes();
		mocksControl.replay();

		try {
			binding.storeEvent(token, calendar, event, false, null);
		} finally {
			mocksControl.verify();
		}
	}

	@Test(expected = NotAllowedException.class)
	public void testStoreEventWhenModifiedEventDoesntBelongToUserOrCalendar() throws Exception {
		Event event = new Event();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();

		event.setOwnerEmail("another@test");
		event.setExtId(new EventExtId("ExtId"));
		event.setInternalEvent(true);
		event.addAttendee(UserAttendee.builder().email(calendar).asOrganizer().build());

		expect(userService.getUserFromLogin(calendar, user.getDomain().getName())).andReturn(user);
		expect(calendarDao.findEventByExtId(token, user, event.getExtId())).andReturn(event).anyTimes();
		expect(userService.getUserFromCalendar(calendar, user.getDomain().getName())).andReturn(user);
		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true).anyTimes();
		expect(helperService.eventBelongsToCalendar(event, calendar)).andReturn(false);
		mocksControl.replay();

		try {
			binding.storeEvent(token, calendar, event, false, null);
		} finally {
			mocksControl.verify();
		}
	}

	@Test
	public void testStoreEventModifiesEventIfPresent() throws Exception {
		Event event = new Event();
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmailAtDomain();

		event.setExtId(new EventExtId("ExtId"));
		event.setInternalEvent(true);
		event.addAttendee(UserAttendee.builder().email(calendar).asOrganizer().build());

		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true).once();
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(calendar, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token, ImmutableSet.of(calendar))).andReturn(calendarToRights).atLeastOnce();
		expect(userService.getUserFromLogin(calendar, user.getDomain().getName())).andReturn(user);
		expect(calendarDao.findEventByExtId(token, user, event.getExtId())).andReturn(event).anyTimes();
		expect(helperService.eventBelongsToCalendar(event, calendar)).andReturn(true);
		expect(userService.getUserFromCalendar(calendar, user.getDomain().getName())).andReturn(user);
		expect(calendarDao.modifyEventForcingSequence(token, calendar, event, true, 0)).andReturn(event);
		mocksControl.replay();

		binding.storeEvent(token, calendar, event, false, null);

		mocksControl.verify();
	}

	@Test
	public void testClientIdCreateEventDeleteItThenCreateAgain() throws Exception {
		ObmUser user = ToolBox.getDefaultObmUser();
		String calendar = user.getEmail();
		boolean notify = false;
		String clientId = "123";

		Event event1 = new Event();
		event1.setOwner(calendar);
		event1.setInternalEvent(true);
		event1.addAttendee(UserAttendee.builder().email(calendar).asOrganizer().build());
		event1.setExtId(new EventExtId("ExtId"));
		event1.setEntityId(EntityId.valueOf(8));
		Event event2 = event1.clone();
		event2.setEntityId(EntityId.valueOf(9));
		event2.setUid(new EventObmId(6));
		Event event3 = event1.clone();
		event3.setEntityId(EntityId.valueOf(10));
		event3.setUid(new EventObmId(7));

		expect(helperService.canWriteOnCalendar(token, calendar)).andReturn(true).times(3);
		CalendarRights calendarToRights = CalendarRights.builder()
				.addRights(calendar, EnumSet.of(Right.ACCESS, Right.WRITE))
				.build();
		expect(helperService.listRightsOnCalendars(token, ImmutableSet.of(calendar))).andReturn(calendarToRights).atLeastOnce();
		expect(userService.getUserFromCalendar(calendar, user.getDomain().getName())).andReturn(user).anyTimes();
		expect(userService.getUserFromLogin(calendar, user.getDomain().getName())).andReturn(user);

		// FIRST CREATION
		expect(calendarDao.findEventByExtId(token, user, event1.getExtId())).andReturn(null);
		expect(calendarDao.createEvent(token, calendar, event1)).andReturn(event2);
		expect(calendarDao.findEventById(token, event2.getObmId())).andReturn(event2);
		messageQueueService.writeIcsInvitationRequest(token, event2);
		expectLastCall();
		mockCommitedOperationNewEvent(event2, clientId);

		// DELETION
		expect(calendarDao.findEventById(token, event2.getObmId())).andReturn(event2);
		expect(calendarDao.removeEventById(token, event2.getObmId(), EventType.VEVENT, 1)).andReturn(event2);
		messageQueueService.writeIcsInvitationCancel(token, event2);
		expectLastCall();

		// SECOND CREATION
		expect(calendarDao.findEventByExtId(token, user, event1.getExtId())).andReturn(null);
		expect(calendarDao.createEvent(token, calendar, event1)).andReturn(event3);
		expect(calendarDao.findEventById(token, event3.getObmId())).andReturn(event3);
		messageQueueService.writeIcsInvitationRequest(token, event3);
		expectLastCall();
		mockCommitedOperationNewEvent(event3, clientId);

		mocksControl.replay();

		binding.createEvent(token, calendar, event1, notify, clientId);
		binding.removeEventById(token, calendar, event2.getObmId(), 0, notify);
		binding.createEvent(token, calendar, event1, notify, clientId);

		mocksControl.verify();
	}

	@Test
	public void testCommitOperationNullClientId() throws Exception {
		Event event = new Event();
		String clientId = null;

		mocksControl.replay();
		binding.commitOperation(token, event, clientId);
		mocksControl.verify();
	}

	@Test
	public void testCommitOperationEmptyClientId() throws Exception {
		Event event = new Event();
		String clientId = "";

		mocksControl.replay();
		binding.commitOperation(token, event, clientId);
		mocksControl.verify();
	}

	@Test
	public void testCommitOperation() throws Exception {
		Event event = new Event();
		event.setEntityId(EntityId.valueOf(9));
		String clientId = "0123456789012345678901234567890123456789";

		commitedOperationDao.store(token, CommitedElement.builder()
				.entityId(event.getEntityId())
				.clientId(clientId)
				.kind(Kind.VEVENT)
				.build());
		expectLastCall();

		mocksControl.replay();
		binding.commitOperation(token, event, clientId);
		mocksControl.verify();
	}

	@Test
	public void convertUnidentifiedAttendee() throws ServerFault {
		Event event = new Event();
		event.setInternalEvent(true);
		ObmUser owner = ToolBox.getDefaultObmUser();
		Attendee ownerAttendee = UnidentifiedAttendee.builder().displayName("user").email("user@test").build();
		Attendee userAttendee = UnidentifiedAttendee.builder().displayName("user").email("user@obm.org").build();
		Attendee contactAttendee = UnidentifiedAttendee.builder().displayName("contact").email("contact@obm.org").build();
		Attendee resourceAttendee = UnidentifiedAttendee.builder().displayName("resource").email("resource@obm.org").build();
		event.addAttendees(ImmutableSet.of(ownerAttendee, userAttendee, contactAttendee, resourceAttendee));

		AttendeeService attendeeService = mocksControl.createMock(AttendeeService.class);

		expect(attendeeService.findUserAttendee("user", "user@test", owner.getDomain()))
			.andReturn(UserAttendee.builder().displayName("name").email("user@test").build());

		expect(attendeeService.findAttendee("user", "user@obm.org", true, owner.getDomain(), owner.getUid()))
			.andReturn(UserAttendee.builder().displayName("name").email("user@obm.org").build());

		expect(attendeeService.findAttendee("contact", "contact@obm.org", true, owner.getDomain(), owner.getUid()))
			.andReturn(ContactAttendee.builder().displayName("contact").email("contact@obm.org").build());

		expect(attendeeService.findAttendee("resource", "resource@obm.org", true, owner.getDomain(), owner.getUid()))
			.andReturn(ResourceAttendee.builder().displayName("resource").email("resource@obm.org").build());

		mocksControl.replay();
		CalendarBindingImpl binding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);
		binding.convertAttendeesOnEvent(event, owner);
		mocksControl.verify();

		assertThat(event.getAttendees()).containsOnly(
				UserAttendee.builder().displayName("name").email("user@test").build(),
				UserAttendee.builder().displayName("name").email("user@obm.org").build(),
				ContactAttendee.builder().displayName("contact").email("contact@obm.org").build(),
				ResourceAttendee.builder().displayName("resource").email("resource@obm.org").build());
	}

	@Test
	public void convertUnidentifiedAttendeeToContactForExternalEvent() throws ServerFault {
		Event event = new Event();
		event.setInternalEvent(false);
		ObmUser owner = ToolBox.getDefaultObmUser();
		Attendee ownerAttendee = UnidentifiedAttendee.builder().displayName("user").email("user@test").build();
		Attendee userAttendee = UnidentifiedAttendee.builder().displayName("user").email("user@obm.org").build();
		Attendee contactAttendee = UnidentifiedAttendee.builder().displayName("contact").email("contact@obm.org").build();
		Attendee resourceAttendee = UnidentifiedAttendee.builder().displayName("resource").email("resource@obm.org").build();
		event.addAttendees(ImmutableSet.of(ownerAttendee, userAttendee, contactAttendee, resourceAttendee));

		AttendeeService attendeeService = mocksControl.createMock(AttendeeService.class);

		expect(attendeeService.findUserAttendee("user", "user@test", owner.getDomain()))
			.andReturn(UserAttendee.builder().displayName("name").email("user@test").build());

		expect(attendeeService.findContactAttendee("user", "user@obm.org", true, owner.getDomain(), owner.getUid()))
			.andReturn(ContactAttendee.builder().displayName("user").email("user@obm.org").build());

		expect(attendeeService.findContactAttendee("contact", "contact@obm.org", true, owner.getDomain(), owner.getUid()))
			.andReturn(ContactAttendee.builder().displayName("contact").email("contact@obm.org").build());

		expect(attendeeService.findContactAttendee("resource", "resource@obm.org", true, owner.getDomain(), owner.getUid()))
			.andReturn(ContactAttendee.builder().displayName("resource").email("resource@obm.org").build());

		mocksControl.replay();
		CalendarBindingImpl binding =
				new CalendarBindingImpl(null, null, null, null, null, null, null, null, null, null, attendeeService, null, null);
		binding.convertAttendeesOnEvent(event, owner);
		mocksControl.verify();

		assertThat(event.getAttendees()).containsOnly(
				UserAttendee.builder().displayName("name").email("user@test").build(),
				ContactAttendee.builder().displayName("name").email("user@obm.org").build(),
				ContactAttendee.builder().displayName("contact").email("contact@obm.org").build(),
				ContactAttendee.builder().displayName("resource").email("resource@obm.org").build());
	}

	@Test(expected=ServerFault.class)
	public void convertUnidentifiedAttendeeThrowServerFaultIfNoFoundOwner() throws ServerFault {
		Event event = new Event();
		ObmUser owner = ToolBox.getDefaultObmUser();
		Attendee userAttendee = UnidentifiedAttendee.builder().displayName("user").email("user@obm.org").build();
		Attendee contactAttendee = UnidentifiedAttendee.builder().displayName("contact").email("contact@obm.org").build();
		Attendee resourceAttendee = UnidentifiedAttendee.builder().displayName("resource").email("resource@obm.org").build();
		event.addAttendees(ImmutableSet.of(userAttendee, contactAttendee, resourceAttendee));

		binding.convertAttendeesOnEvent(event, owner);
	}
	
	private void mockCommitedOperationNewEvent(Event event, String clientId) throws Exception {
		expect(commitedOperationDao.findAsEvent(token, clientId)).andReturn(null).once();
		commitedOperationDao.store(token, CommitedElement.builder()
				.entityId(event.getEntityId())
				.clientId(clientId)
				.kind(Kind.VEVENT)
				.build());
		expectLastCall().once();
	}

	private void mockCommitedOperationExistingEvent(Event event, String clientId) throws Exception {
		CommitedOperation<Event> commitedOperation = new CommitedOperation<Event>(event, null);
		expect(commitedOperationDao.findAsEvent(token, clientId)).andReturn(commitedOperation).once();
		commitedOperationDao.store(token, CommitedElement.builder()
				.entityId(event.getEntityId())
				.clientId(clientId)
				.kind(Kind.VEVENT)
				.build());
		expectLastCall().once();
	}
	
	private void mockSimpleGetSync(
					ObmUser defaultUser, Date lastSync, EventChanges daoChanges, boolean onEventDate)
					throws FindException {
			int getTransactionToleranceTimeoutInSeconds = 60;
			expect(helperService.canReadCalendar(token, "calendar")).andReturn(true).once();
			expect(userService.getUserFromCalendar("calendar", "test.tlse.lng")).andReturn(defaultUser).once();
			expect(calendarDao.getSync(token, defaultUser, lastSync, null, null, onEventDate)).andReturn(daoChanges).once();
			expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(getTransactionToleranceTimeoutInSeconds);
	}
	
	@Test
	public void testReturnOfGetSyncChangeLastSyncIfUserHasReadOnly()
			throws ServerFault, NotAllowedException, FindException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		EventChanges daoChanges = EventChanges.builder().lastSync(lastSync).build();

		mockSimpleGetSync(ToolBox.getDefaultObmUser(), lastSync, daoChanges, false);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(daoChanges).once();
		mocksControl.replay();
		EventChanges eventChanges = binding.getSync(token, "calendar",lastSync, null, false);
		mocksControl.verify();

		assertThat(eventChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:16:00"));
	}

	@Test
	public void testReturnOfGetSyncChangeLastSync() throws ServerFault,
			NotAllowedException, FindException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		EventChanges daoChanges = EventChanges.builder().lastSync(lastSync).build();

		mockSimpleGetSync(ToolBox.getDefaultObmUser(), lastSync, daoChanges, false);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(daoChanges).once();
		mocksControl.replay();
		EventChanges eventChanges = binding.getSync(token, "calendar", lastSync, null, false);
		mocksControl.verify();

		assertThat(eventChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:16:00"));
	}

	@Test
	public void testLastSyncOfGetSync() throws ServerFault,
			NotAllowedException, FindException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		EventChanges daoChanges = EventChanges.builder().lastSync(lastSync).build();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(daoChanges).once();
		mockSimpleGetSync(ToolBox.getDefaultObmUser(), lastSync, daoChanges, false);

		mocksControl.replay();
		EventChanges eventChanges = binding.getSync(token, "calendar", lastSync);
		mocksControl.verify();

		assertThat(eventChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:16:00"));
	}

	@Test
	public void testLastSyncOfGetSyncEventDate() throws ServerFault,
			NotAllowedException, FindException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		EventChanges daoChanges = EventChanges.builder().lastSync(lastSync).build();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(daoChanges).once();
		mockSimpleGetSync(ToolBox.getDefaultObmUser(), lastSync, daoChanges, true);

		mocksControl.replay();
		EventChanges eventChanges = binding.getSyncEventDate(token, "calendar", lastSync);
		mocksControl.verify();

		assertThat(eventChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:16:00"));
	}

	@Test
	public void testLastSyncOfGetSyncInRange() throws FindException,
			ServerFault, NotAllowedException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		EventChanges daoChanges = EventChanges.builder().lastSync(lastSync).build();
		
		mockSimpleGetSync(ToolBox.getDefaultObmUser(), lastSync, daoChanges, false);
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(daoChanges).once();
		mocksControl.replay();
		EventChanges eventChanges = binding.getSyncInRange(token, "calendar", lastSync, null);
		mocksControl.verify();

		assertThat(eventChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:16:00"));
	}

	@Test
	public void testLastSyncOfGetSyncWithSortedChanges() throws ServerFault,
			NotAllowedException, FindException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		EventChanges daoChanges = EventChanges.builder().lastSync(lastSync).build();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(daoChanges).once();

		mockSimpleGetSync(ToolBox.getDefaultObmUser(), lastSync, daoChanges, false);

		mocksControl.replay();
		EventChanges eventChanges = binding.getSyncWithSortedChanges(token, "calendar", lastSync, null);
		mocksControl.verify();

		assertThat(eventChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:16:00"));
	}

	@Test
	public void testAnonymizeServicePrivateEvent() throws ServerFault, NotAllowedException, FindException {
		Date lastSync = DateUtils.date("2012-04-14T14:00:00");
		ObmUser viewer = ToolBox.getDefaultObmUser();
		Event privateEvent = ToolBox.getFakeEvent(1);

		privateEvent.setPrivacy(EventPrivacy.PRIVATE);
		EventChanges daoChanges = EventChanges
				.builder()
				.lastSync(lastSync)
				.updates(Sets.newHashSet(privateEvent))
				.build();
		EventChanges syncResult = EventChanges
				.builder()
				.lastSync(lastSync)
				.updates(Sets.newHashSet(privateEvent.anonymizePrivateItems(viewer)))
				.build();
		
		expect(userService.getUserFromCalendar("calendar", "test.tlse.lng")).andReturn(viewer).atLeastOnce();
		expect(helperService.canReadCalendar(token, "calendar")).andReturn(true).once();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(syncResult).once();
		expect(calendarDao.getSync(token, viewer, lastSync, null, null, false)).andReturn(daoChanges);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		mocksControl.replay();
		EventChanges eventChanges = binding.getSync(token, "calendar", lastSync);
		mocksControl.verify();
		assertThat(eventChanges.getUpdated().iterator().next().isAnonymized()).isEqualTo(true);
		assertThat(eventChanges).isEqualTo(syncResult);

	}

	@Test
	public void testAnonymizeServicePublicEvent() throws ServerFault, NotAllowedException, FindException {
		Date lastSync = DateUtils.date("2012-04-14T14:00:00");
		ObmUser viewer = ToolBox.getDefaultObmUser();
		Event publicEvent = ToolBox.getFakeEvent(1);

		EventChanges daoChanges = EventChanges
				.builder()
				.lastSync(lastSync)
				.updates(Sets.newHashSet(publicEvent))
				.build();
		EventChanges syncResult = EventChanges
				.builder()
				.lastSync(lastSync)
				.updates(Sets.newHashSet(publicEvent.anonymizePrivateItems(viewer)))
				.build();
		expect(userService.getUserFromCalendar("calendar", "test.tlse.lng")).andReturn(viewer).atLeastOnce();
		expect(helperService.canReadCalendar(token, "calendar")).andReturn(true).once();
		expect(anonymizerService.anonymize(isA(EventChanges.class), eq("calendar"), eq(token))).andReturn(syncResult).once();
		expect(calendarDao.getSync(token, viewer, lastSync, null, null, false)).andReturn(daoChanges);
		expect(configurationService.getTransactionToleranceTimeoutInSeconds()).andReturn(0);
		mocksControl.replay();
		EventChanges eventChanges = binding.getSync(token, "calendar", lastSync);
		mocksControl.verify();
		assertThat(eventChanges.getUpdated().iterator().next().isAnonymized()).isEqualTo(false);
		assertThat(eventChanges).isEqualTo(syncResult);

	}

}
