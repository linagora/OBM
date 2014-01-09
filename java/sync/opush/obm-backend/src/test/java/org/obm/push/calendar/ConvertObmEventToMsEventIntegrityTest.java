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
package org.obm.push.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;
import static org.obm.push.calendar.ObmEventToMSEventConverter.ACTIVESYNC_DEFAULT_FREQUENCY;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;

import com.google.common.collect.Iterables;


public abstract class ConvertObmEventToMsEventIntegrityTest {


	private ObmEventToMSEventConverter converter;
	protected abstract ObmEventToMSEventConverter newObmEventToMSEventConverter();
	
	@Before
	public void setUp() {
		converter = newObmEventToMSEventConverter();
	}

	private Event basicEvent() {
		Event event = new Event();
		event.setAlert(120);
		event.setAllday(false);
		event.setCategory("category");
		event.setCreatorDisplayName("creatorDisplayName");
		event.setCreatorEmail("creator@email");
		event.setDescription("description");
		event.setDuration(3600);
		event.setExtId(new EventExtId("36cb5540-aa8c-4c89-9dfc-3dff56c68df2"));
		event.setInternalEvent(false);
		event.setLocation("Lyon, France");
		event.setOpacity(EventOpacity.OPAQUE);
		event.setOwner("owner@id");
		event.setOwnerDisplayName("owner display name");
		event.setOwnerEmail("owner@email");
		event.setPriority(1);
		event.setPrivacy(EventPrivacy.PRIVATE);
		event.setSequence(1);
		event.setStartDate(date("2004-12-13T21:39:45Z"));
		event.setTimeCreate(date("2004-12-11T10:10:10Z"));
		event.setTimeUpdate(date("2004-12-11T11:15:10Z"));
		event.setTimezoneName("Europe/Paris");
		event.setTitle("title");
		event.setType(EventType.VEVENT);
		event.setUid(new EventObmId(121));
		event.addAttendees(Arrays.asList(
				UserAttendee.builder().email("jaures@sfio.fr")
					.displayName("Jean Jaures")
					.participation(Participation.accepted())
					.participationRole(ParticipationRole.REQ)
					.asOrganizer().build(),
				UserAttendee.builder().email("blum@sfio.fr")
					.displayName("Léon Blum")
					.participation(Participation.needsAction())
					.participationRole(ParticipationRole.OPT)
					.asAttendee().build()));
		return event;
	}
	
	private User jauresUser() {
		return User.Factory.create().createUser("jaures@domain", "jaures@sfio.fr", "Jean Jaures");
	}

	@Ignore("OBMFULL-3787")
	@Test
	public void testSimpleObmEvent() throws ConversionException {
		Event event = basicEvent();
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		
		assertThat(msEvent).isNotNull();
		assertThat(msEvent.getAllDayEvent()).isFalse();
		assertThat(msEvent.getAttendees()).containsOnly(
				MSAttendee.builder().withEmail("jaures@sfio.fr")
					.withName("Jean Jaures")
					.withStatus(AttendeeStatus.ACCEPT)
					.withType(AttendeeType.REQUIRED).build(),
				MSAttendee.builder().withEmail("blum@sfio.fr")
					.withName("Léon Blum")
					.withStatus(AttendeeStatus.NOT_RESPONDED)
					.withType(AttendeeType.OPTIONAL).build());
		assertThat(msEvent.getBusyStatus()).isEqualTo(CalendarBusyStatus.BUSY);
		assertThat(msEvent.getCategories()).containsOnly("category");
		assertThat(msEvent.getDescription()).isEqualTo("description");
		assertThat(msEvent.getDtStamp()).isEqualTo(date("2004-12-11T11:15:10Z"));
		assertThat(msEvent.getEndTime()).isEqualTo(date("2004-12-13T22:39:45Z"));
		assertThat(msEvent.getExceptions()).isEmpty();
		assertThat(msEvent.getLocation()).isEqualTo("Lyon, France");
		assertThat(msEvent.getMeetingStatus()).isEqualTo(CalendarMeetingStatus.IS_A_MEETING);
		assertThat(msEvent.getObmSequence()).isEqualTo(1);
		assertThat(msEvent.getOrganizerEmail()).isEqualTo("jaures@sfio.fr");
		assertThat(msEvent.getOrganizerName()).isEqualTo("Jean Jaures");
		assertThat(msEvent.getRecurrence()).isNull();
		assertThat(msEvent.getReminder()).isEqualTo(2);
		assertThat(msEvent.getSensitivity()).isEqualTo(CalendarSensitivity.PRIVATE);
		assertThat(msEvent.getStartTime()).isEqualTo(date("2004-12-13T21:39:45Z"));
		assertThat(msEvent.getSubject()).isEqualTo("title");
		assertThat(msEvent.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Europe/Paris"));
		assertThat(msEvent.getType()).isEqualTo(PIMDataType.CALENDAR);
		assertThat(msEvent.getUid()).isEqualTo(new MSEventUid("mseventuid"));
	}
	
	@Test
	public void testNullAlertEvent() throws ConversionException {
		Event event = basicEvent();
		event.setAlert(null);
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		assertThat(msEvent.getReminder()).isNull();
	}
	
	@Test
	public void testAllDayEvent() throws ConversionException {
		Event event = basicEvent();
		event.setAllday(true);
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		assertThat(msEvent.getAllDayEvent()).isEqualTo(true);
		assertThat(msEvent.getEndTime()).isEqualTo(date("2004-12-14T00:00:00"));
	}
	
	@Test
	public void testDailyRecurrence() throws ConversionException {
		Event event = basicEvent();
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		event.setRecurrence(eventRecurrence);
		User jaures = jauresUser();
		
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		MSRecurrence msRecurrence = msEvent.getRecurrence();
		
		assertThat(msRecurrence.getDayOfMonth()).isNull();
		assertThat(msRecurrence.getDayOfWeek()).isNull();
		assertThat(msRecurrence.getDeadOccur()).isNull();
		assertThat(msRecurrence.getInterval()).isEqualTo(ACTIVESYNC_DEFAULT_FREQUENCY);
		assertThat(msRecurrence.getMonthOfYear()).isNull();
		assertThat(msRecurrence.getOccurrences()).isNull();
		assertThat(msRecurrence.getRegenerate()).isNull();
		assertThat(msRecurrence.getStart()).isNull();
		assertThat(msRecurrence.getType()).isEqualTo(RecurrenceType.DAILY);
		assertThat(msRecurrence.getUntil()).isNull();
		assertThat(msRecurrence.getWeekOfMonth()).isNull();
	}
	
	@Ignore
	@Test
	public void testDailyRecurrenceDeletionException() throws ConversionException {
		Event event = basicEvent();
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		eventRecurrence.addException(date("2004-12-14T21:39:45Z"));
		event.setRecurrence(eventRecurrence);
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		
		List<MSEventException> exceptions = msEvent.getExceptions();
		assertThat(exceptions).hasSize(1);
		MSEventException deletedException = Iterables.getOnlyElement(exceptions);
		assertThat(deletedException.isDeleted()).isTrue();
		assertThat(deletedException.getExceptionStartTime()).isEqualTo(date("2004-12-14T21:39:45Z"));
		assertThat(deletedException.getAllDayEvent()).isNull();
		assertThat(deletedException.getBusyStatus()).isNull();
		assertThat(deletedException.getCategories()).isNull();
		assertThat(deletedException.getDescription()).isNull();
		assertThat(deletedException.getDtStamp()).isNull();
		assertThat(deletedException.getEndTime()).isNull();
		assertThat(deletedException.getLocation()).isNull();
		assertThat(deletedException.getMeetingStatus()).isNull();
		assertThat(deletedException.getReminder()).isNull();
		assertThat(deletedException.getSensitivity()).isNull();
		assertThat(deletedException.getSubject()).isNull();
		assertThat(deletedException.getStartTime()).isNull();
	}
	
	@Test
	public void testNoneRecurrenceWithDeletionException() throws ConversionException {
		Event event = basicEvent();
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.none);
		eventRecurrence.addException(date("2004-12-14T21:39:45Z"));
		event.setRecurrence(eventRecurrence);
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		
		List<MSEventException> exceptions = msEvent.getExceptions();
		assertThat(exceptions).isEmpty();
	}
	
	@Test
	public void testDailyMondyWithTuesdayException() throws ConversionException {
		Event event = basicEvent();
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		eventRecurrence.setDays(new RecurrenceDays(RecurrenceDay.Monday));
		Event secondOccurence = new Event();
		secondOccurence.setDuration(event.getDuration());
		secondOccurence.setRecurrenceId(date("2004-12-20T21:39:45Z"));
		secondOccurence.setStartDate(date("2004-12-21T21:39:45Z"));
		eventRecurrence.addEventException(secondOccurence);
		event.setRecurrence(eventRecurrence);
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		List<MSEventException> exceptions = msEvent.getExceptions();
		assertThat(exceptions).hasSize(1);
		MSEventException exception = Iterables.getOnlyElement(exceptions);
		assertThat(exception.getAllDayEvent()).isFalse();
		assertThat(exception.getBusyStatus()).isEqualTo(CalendarBusyStatus.BUSY);
		assertThat(exception.getCategories()).isNullOrEmpty();
		assertThat(exception.getDescription()).isNull();
		assertThat(exception.getDtStamp()).isNull();
		assertThat(exception.getEndTime()).isEqualTo(date("2004-12-21T22:39:45Z"));
	}
	
	@Test
	public void testExceptionWithNoDuration() throws ConversionException {
		Event event = basicEvent();
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		eventRecurrence.setDays(new RecurrenceDays(RecurrenceDay.Monday));
		Event secondOccurence = new Event();
		secondOccurence.setRecurrenceId(date("2004-12-20T21:39:45Z"));
		secondOccurence.setStartDate(date("2004-12-21T21:39:45Z"));
		eventRecurrence.addEventException(secondOccurence);
		event.setRecurrence(eventRecurrence);
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		List<MSEventException> exceptions = msEvent.getExceptions();
		assertThat(exceptions).hasSize(1);
		MSEventException exception = Iterables.getOnlyElement(exceptions);
		assertThat(exception.getAllDayEvent()).isFalse();
		assertThat(exception.getBusyStatus()).isEqualTo(CalendarBusyStatus.BUSY);
		assertThat(exception.getCategories()).isNullOrEmpty();
		assertThat(exception.getDescription()).isNull();
		assertThat(exception.getDtStamp()).isNull();
		assertThat(exception.getEndTime()).isEqualTo(date("2004-12-21T21:39:45Z"));
	}
	
	@Test
	public void testKeepGeneralParticipation() throws ConversionException {
		Event event = basicEvent();
		
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		eventRecurrence.setDays(new RecurrenceDays(RecurrenceDay.Monday));
		
		Event secondOccurence = new Event();
		secondOccurence.addAttendee(UserAttendee.builder().email("jaures@sfio.fr")
				.displayName("Jean Jaures")
				.participation(Participation.declined())
				.participationRole(ParticipationRole.REQ).build());
		secondOccurence.setDuration(event.getDuration());
		secondOccurence.setRecurrenceId(date("2004-12-20T21:39:45Z"));
		secondOccurence.setStartDate(date("2004-12-21T21:39:45Z"));
		
		eventRecurrence.addEventException(secondOccurence);
		event.setRecurrence(eventRecurrence);
		
		User jaures = jauresUser();
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jaures);
		List<MSEventException> exceptions = msEvent.getExceptions();
		assertThat(exceptions).hasSize(1);
		MSEventException exception = Iterables.getOnlyElement(exceptions);
		assertThat(exception).isNotNull();
		assertThat(msEvent.getAttendees()).contains(MSAttendee.builder().withEmail("jaures@sfio.fr")
				.withName("Jean Jaures")
				.withStatus(AttendeeStatus.ACCEPT)
				.withType(AttendeeType.REQUIRED).build());
	}
	
	@Test
	public void testTimeZoneConversion() throws ConversionException {
		Event event = basicEvent();
		
		TimeZone timeZone = TimeZone.getTimeZone("Pacific/Auckland");
		event.setTimezoneName(timeZone.getID());
		
		MSEvent msEvent = converter.convert(event, new MSEventUid("mseventuid"), jauresUser());
		assertThat(msEvent.getTimeZone()).isEqualTo(timeZone);
	}
}
