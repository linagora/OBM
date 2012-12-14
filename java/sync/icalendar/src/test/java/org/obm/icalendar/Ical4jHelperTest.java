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
package org.obm.icalendar;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.utils.UserEmailParserUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Comment;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.date.DateProvider;
import org.obm.sync.exception.IllegalRecurrenceKindException;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

@RunWith(SlowFilterRunner.class)
public class Ical4jHelperTest {

	private static class StringLengthLessThan extends TypeSafeMatcher<String> {

		private final int length;

		public StringLengthLessThan(int length) {
			this.length = length;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("check that given string's length is less than " + length);
		}

		@Override
		public boolean matchesSafely(String item) {
			return (item.length() < length);
		}
	}
	
	private static class StringContainsIcsProperty extends TypeSafeMatcher<String> {

		private final String propertyName;
		private final String propertyValue;
		

		public StringContainsIcsProperty(String propertyName, String propertyValue) {
			this.propertyName = propertyName;
			this.propertyValue = propertyValue;
			
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("check that given string contains the ics property named" + propertyName + " with the value "+propertyValue);
		}

		@Override
		public boolean matchesSafely(String item) {
			String field = propertyName+":"+propertyValue+"\r\n";
			return item.contains(field);
		}
	}
	
	private Ical4jHelper ical4jHelper;
	private DateProvider dateProvider;
	private Date now;

	@Before
	public void setUp() {
		now = getCalendarPrecisionOfSecond().getTime();
		dateProvider = createMock(DateProvider.class);
		ical4jHelper = new Ical4jHelper(dateProvider);
		
		expect(dateProvider.getDate()).andReturn(now).anyTimes();
		replay(dateProvider);
	}
	
	@BeforeClass
	public static void setUpOnce() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	}

	private Calendar getCalendarPrecisionOfSecond() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(DateUtils.date("2004-12-14T21:39:45Z"));
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	
	protected Ical4jUser getDefaultObmUser() {
		ObmUser obmUser = new ObmUser();
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName("test.tlse.lng");
		obmDomain.setUuid("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		obmUser.setDomain(obmDomain);
		return Ical4jUser.Factory.create().createIcal4jUser("test@test.obm.lng.org", obmDomain);
	}

	protected Event getTestEvent() {
		final Event ev = buildEvent();

		final Calendar cal = getCalendarPrecisionOfSecond();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
	
		ev.setAlert(60);

		final EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.monthlybydate);
		er.setFrequence(1);
		er.addException(ev.getStartDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);

		ev.setRecurrence(er);
		return ev;
	}

	@Test
	public void testParseEvents() throws IOException {
		Event event1 = getTestEvent();
		Event event2 = getTestEvent();
		List<Event> l = new LinkedList<Event>();
		l.add(event1);
		l.add(event2);

		InputStream stream = getStreamICS("serializedOBMEvent.ics");
		String expectedICS = IOUtils.toString(stream);
		stream.close();

		AccessToken token = new AccessToken(0, "OBM");
		String ics = ical4jHelper.parseEvents(getDefaultObmUser(), l, token);

		String icsWithoutTimestamps = stripTimestamps(ics);
		String expectedICSWithoutTimestamps = stripTimestamps(expectedICS);

		assertThat(icsWithoutTimestamps).isNotNull().isNotEmpty()
				.isEqualTo(expectedICSWithoutTimestamps);
	}
	
	private String stripTimestamps(String ics) {
		String minusDtStamp = ics.replaceAll("DTSTAMP:\\d{8}T\\d{6}Z", "");
		String minusLastAck = minusDtStamp.replaceAll("X-MO-LASTACK:\\d{8}T\\d{6}Z", "");
		return minusLastAck;
	}
	
	@Test(expected = IllegalRecurrenceKindException.class)
	public void testGetRecurWithIllegalRecurrenceKind() {
		EventRecurrence eventRecurrence = createMock(EventRecurrence.class);

		expect(eventRecurrence.isRecurrent()).andReturn(true).anyTimes();
		expect(eventRecurrence.getKind()).andReturn(null).anyTimes();

		replay(eventRecurrence);
		ical4jHelper.getRecur(eventRecurrence, new Date());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetRecur() {
		Calendar cal = getCalendarPrecisionOfSecond();
		cal.setTime(new Date());
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		Event event = new Event();

		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.weekly);
		eventRecurrence.setFrequence(1);
		eventRecurrence.setDays(new RecurrenceDays(RecurrenceDay.Monday,
				RecurrenceDay.Tuesday, RecurrenceDay.Wednesday,
				RecurrenceDay.Thursday, RecurrenceDay.Friday));
		eventRecurrence.setEnd(null);
		event.setRecurrence(eventRecurrence);

		Recur recur = ical4jHelper.getRecur(event.getRecurrence(), event.getStartDate());

		assertThat(recur.getDayList()).containsOnly(WeekDay.MO,
				WeekDay.TU, WeekDay.WE, WeekDay.TH, WeekDay.FR);
		assertThat(recur.getInterval()).isEqualTo(eventRecurrence.getFrequence());
		assertThat(recur.getFrequency()).isEqualTo(Recur.WEEKLY);
		assertThat(recur.getUntil()).isNull();
	}

	@Test
	public void testGetRecurOnNotRecurrentEvent() {
		EventRecurrence eventRecurrence = new EventRecurrence();
		Recur recur = ical4jHelper.getRecur(eventRecurrence, new Date());
		assertThat(recur).isNull();
	}

	@Test
	public void testGetDailyRecur() {
		Recur recur = getFakeRecurByRecurrenceKind(RecurrenceKind.daily, new Date());
		assertThat(recur.getFrequency()).isEqualTo(Recur.DAILY);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetMonthlyByDayRecur() {
		Recur recur = getFakeRecurByRecurrenceKind(RecurrenceKind.monthlybyday, new Date());
		assertThat(recur.getFrequency()).isEqualTo(Recur.MONTHLY);
		assertThat(recur.getDayList()).contains(WeekDay.getMonthlyOffset(new GregorianCalendar()));
	}

	@Test
	public void testGetMonthlyByDateRecur() {
		Recur recur = getFakeRecurByRecurrenceKind(RecurrenceKind.monthlybydate, new Date());
		assertThat(recur.getFrequency()).isEqualTo(Recur.MONTHLY);
	}

	@Test
	public void testGetYearlyRecur() {
		Recur recur = getFakeRecurByRecurrenceKind(RecurrenceKind.yearly, new Date());
		assertThat(recur.getFrequency()).isEqualTo(Recur.YEARLY);
	}

	@Test
	public void testGetYearlyByDayRecur() {
		Recur recur = getFakeRecurByRecurrenceKind(RecurrenceKind.yearlybyday, new Date());
		assertThat(recur.getFrequency()).isEqualTo(Recur.YEARLY);
	}

	private Recur getFakeRecurByRecurrenceKind(RecurrenceKind recurrenceKind, Date date) {
		EventRecurrence eventRecurrence = new EventRecurrence(recurrenceKind);
		Recur recur = ical4jHelper.getRecur(eventRecurrence, date);
		return recur;
	}

	@Test
	public void testGetListDay() {
		EventRecurrence er = new EventRecurrence();
		er.setDays(new RecurrenceDays(RecurrenceDay.Sunday, RecurrenceDay.Tuesday, RecurrenceDay.Thursday,
				RecurrenceDay.Saturday));
		Set<WeekDay> swd = ical4jHelper.getListDay(er);
		assertTrue(swd.contains(WeekDay.SU));
		assertTrue(swd.contains(WeekDay.TU));
		assertTrue(swd.contains(WeekDay.TH));
		assertTrue(swd.contains(WeekDay.SA));
		ical4jHelper.getListDay(er);
	}

	@Ignore
	@Test
	public void testGetIsAllDay() {

		Calendar cal = getCalendarPrecisionOfSecond();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		DtStart dtStart = new DtStart(new net.fortuna.ical4j.model.Date(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

		DtEnd dtEnd = new DtEnd(new net.fortuna.ical4j.model.Date(cal.getTime()));

		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		AccessToken token = new AccessToken(0, null);
		token.setUserEmail("adrien@zz.com");
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertTrue(event.isAllday());

		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		dtStart = new DtStart(new DateTime(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		dtEnd = new DtEnd(new DateTime(cal.getTime()));
		vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event1 = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(event1.isAllday());
	}

	@Test @Slow
	public void testGetDuration() {

		Calendar cal = getCalendarPrecisionOfSecond();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		DtStart dtStart = new DtStart(new DateTime(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		DtEnd dtEnd = new DtEnd(new DateTime(cal.getTime()));

		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(172800, event.getDuration());

	}

	@Test
	public void testGetPrivacyPublic() {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PUBLIC);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(EventPrivacy.PUBLIC, event.getPrivacy());
	}

	@Test
	public void testGetPrivacyPrivate() {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PRIVATE);
		Event event1 = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(EventPrivacy.PRIVATE, event1.getPrivacy());
	}

	
	@Test
	public void testGetOwner() throws URISyntaxException {
		Organizer orga = new Organizer();
		orga.getParameters().add(new Cn("Adrien Poupard"));
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals("Adrien Poupard", event.getOwner());
	}

	@Test
	public void testGetOwner1() throws URISyntaxException {
		Organizer orga = new Organizer();
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals("adrien@zz.com", event.getOwner());
	}

	@Test
	public void testGetAlertWithDurationAndNoRepeat() {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		va.getProperties().add(new Duration(dur));
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertNull(event.getAlert());
	}
	
	@Test
	public void testGetAlertWithoutRepeatAndWithoutDuration() {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(
				new Integer(-1).equals(event.getAlert())
		);
	}
	
	@Test
	public void testGetAlertWithRepeatAndNoDuration() {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		va.getProperties().add(new Repeat());
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertNull(event.getAlert());
	}
	
	@Test
	public void testGetAlertWithRepeatAndDuration() {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		va.getProperties().add(new Repeat());
		va.getProperties().add(new Duration(dur));
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(
				new Integer(-1).equals(event.getAlert())
		);
	}

	@Test @Slow
	public void testGetRecurence() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("getRecurence.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		EventRecurrence er = event.getRecurrence();
		assertNotNull(er);
		assertEquals(1, er.getFrequence());
		assertEquals(EnumSet.of(RecurrenceDay.Monday, RecurrenceDay.Tuesday,
				RecurrenceDay.Wednesday, RecurrenceDay.Thursday, RecurrenceDay.Friday),
				er.getDays());
		assertEquals(RecurrenceKind.weekly, er.getKind());
		assertThat(er.getExceptions()).hasSize(1);
	}

	
	@Test @Slow
	public void testGetAttendees() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("attendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(3, event.getAttendees().size());
	}


	@Test
	@SuppressWarnings("null")
	public void testOrganizerInAttendess() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("organizerInAttendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		Attendee organizer = null;
		for(Attendee att : event.getAttendees()){
			if(att.isOrganizer()){
				organizer = att;
			}
		}
		assertNotNull(organizer);
		assertEquals("adrien@zz.com", organizer.getEmail());
		assertTrue(organizer.isOrganizer());
	}
	
	@Test
	@SuppressWarnings("null")
	public void testOrganizerNotInAttendess() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("organizerNotInAttendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		Attendee organizer = null;
		for(Attendee att : event.getAttendees()){
			if(att.isOrganizer()){
				organizer = att;
			}
		}
		assertNotNull(organizer);
		assertEquals("adrien@zz.com", organizer.getEmail());
		assertTrue(organizer.isOrganizer());
	}
	
	@Test @Slow
	public void testIsInternal() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventInternal.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertTrue(event.isInternalEvent());

	}

	@Test @Slow
	public void testCreated() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(1244470973000L, event.getTimeCreate().getTime());
	}

	@Test @Slow
	public void testLastModified() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(1244470995000L, event.getTimeUpdate().getTime());

	}

	@Test @Slow
	public void testLastModifiedNull() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventNewComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertNull(event.getTimeUpdate());

	}

	@Test @Slow
	public void testIsExternalObm() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventExternalObm.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(event.isInternalEvent());

	}

	@Test @Slow
	public void testIsExternal() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventExternal.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(event.isInternalEvent());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetExDate() {
		Calendar cal = getCalendarPrecisionOfSecond();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		Event event = new Event();
		event.setStartDate(cal.getTime());
		EventRecurrence er = new EventRecurrence();
		er.setDays(new RecurrenceDays());
		er.setKind(RecurrenceKind.daily);

		Date[] except = new Date[2];
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		except[0] = cal.getTime();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		except[1] = cal.getTime();
		er.setExceptions(Arrays.asList(except));
		Date exceptionOne = org.obm.push.utils.DateUtils.getOneDayLater(cal.getTime());
		Date exceptionTwo = org.obm.push.utils.DateUtils.getOneDayLater(exceptionOne);
		er.setExceptions(Lists.newArrayList(exceptionOne, exceptionTwo));
		event.setRecurrence(er);

		DateTime expectedExceptionOne = new DateTime(exceptionOne);
		DateTime expectedExceptionTwo = new DateTime(exceptionTwo);
		
		ExDate ret = ical4jHelper.getExDate(event);
		assertEquals(2, ret.getDates().size());
		assertThat(ret.getDates()).containsOnly(expectedExceptionOne, expectedExceptionTwo);
		assertThat(ret.getDates().isUtc());
	}

	@Test
	public void testGetExDateReturnNullWhenNoException() {
		Calendar cal = getCalendarPrecisionOfSecond();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		EventRecurrence er = new EventRecurrence();
		er.setExceptions(Collections.<Date>emptyList());
		Event event = new Event();
		event.setStartDate(cal.getTime());
		event.setRecurrence(er);

		ExDate exDate = ical4jHelper.getExDate(event);
		assertNull(exDate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetExDateWhenDeletedAndRegularExceptionsExists() {
		Calendar cal = getCalendarPrecisionOfSecond();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		Date deletedExceptionOne = org.obm.push.utils.DateUtils.getOneDayLater(cal.getTime());
		Date deletedExceptionTwo = org.obm.push.utils.DateUtils.getOneDayLater(deletedExceptionOne);

		Event regularException = new Event();
		Date regularExceptionDate = org.obm.push.utils.DateUtils.getOneDayLater(deletedExceptionTwo);
		regularException.setRecurrenceId(regularExceptionDate);
		regularException.setStartDate(DateUtils.date("2004-12-14T21:39:45Z"));

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setExceptions(Lists.newArrayList(deletedExceptionOne, deletedExceptionTwo));
		recurrence.setEventExceptions(Sets.newHashSet(regularException));
		
		Event event = new Event();
		event.setRecurrence(recurrence);

		ExDate ret = ical4jHelper.getExDate(event);

		DateTime expectedExceptionOne = new DateTime(deletedExceptionOne);
		DateTime expectedExceptionTwo = new DateTime(deletedExceptionTwo);
		assertThat(ret.getDates()).hasSize(2);
		assertThat(ret.getDates()).containsOnly(
				expectedExceptionOne, expectedExceptionTwo);
		assertThat(ret.getDates().isUtc());
	}

	@Test
	public void testGetVAlarm() {
		final Event event = new Event();
		event.setAlert(30);
		final VAlarm valarm = ical4jHelper.getVAlarm(event.getAlert());
		assertEquals(30, valarm.getTrigger().getDuration().getSeconds());
	}

	@Test
	public void testAppendAllDayWithDateTimeObject() throws ParseException {
		final Event event = new Event();
		final DtStart startDate = new DtStart();
		final DateProperty endDate = new DtEnd();
		
		startDate.setValue("19980118T230000");
		endDate.setValue("19980118T230000");
		
		ical4jHelper.appendAllDay(event, startDate, endDate);
		assertFalse(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDateObject() {
		final Event event = new Event();
		final DtStart startDate = new DtStart();
		final DateProperty endDate = new DtEnd();
		
		startDate.setDate(new net.fortuna.ical4j.model.Date());
		endDate.setDate(new net.fortuna.ical4j.model.Date());
		
		ical4jHelper.appendAllDay(event, startDate, endDate);
		assertTrue(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDurationOfOneDay() {
		final Event event = new Event();
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-15T21:39:45Z"));
		
		ical4jHelper.appendAllDay(event, allDayDuration);
		assertTrue(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDurationOfThreeDay() {
		final Event event = new Event();
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-17T21:39:45Z"));
		
		ical4jHelper.appendAllDay(event, allDayDuration);
		assertTrue(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDurationOfOneWeek() {
		final Event event = new Event();
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-21T21:39:45Z"));
		
		ical4jHelper.appendAllDay(event, allDayDuration);
		assertFalse(event.isAllday());
	}

	@Test
	public void testAppendAllDayWithDurationOfOneHour() {
		final Event event = new Event();
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-14T22:39:45Z"));
		
		ical4jHelper.appendAllDay(event, allDayDuration);
		assertFalse(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDurationOfQuiteOneDay() {
		final Event event = new Event();
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:40:00Z"),
				DateUtils.date("2004-12-15T21:30:00Z"));
		
		ical4jHelper.appendAllDay(event, allDayDuration);
		assertFalse(event.isAllday());
	}

	@Test
	public void testAppendAllDayWithDurationOfOneMinute() {
		final Event event = new Event();
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-14T21:40:45Z"));
		
		ical4jHelper.appendAllDay(event, allDayDuration);
		assertFalse(event.isAllday());
	}
	
	@Test
	public void testGetPublicClazz() {
		Event event = new Event();
		event.setPrivacy(EventPrivacy.PUBLIC);
		Clazz clazz = ical4jHelper.getClazz(event.getPrivacy());
		assertEquals("PUBLIC", clazz.getValue());
	}

	@Test
	public void testGetPrivateClazz() {
		Event event = new Event();
		event.setPrivacy(EventPrivacy.PRIVATE);
		Clazz clazz = ical4jHelper.getClazz(event.getPrivacy());
		assertEquals("PRIVATE", clazz.getValue());
	}

	
	@Test
	public void testGetOrganizer() {
		Event event = new Event();
		event.setOwner("Adrien Poupard");
		event.setOwnerEmail("adrien@zz.com");
		Organizer orga = ical4jHelper.getOrganizer(event.getOwner(),
				event.getOwnerEmail());
		assertEquals("mailto:adrien@zz.com", orga.getValue());
		assertEquals("Adrien Poupard", orga.getParameter(Parameter.CN)
				.getValue());

	}

	@Test
	public void testGetTransp() {
		Event event = new Event();
		event.setOpacity(EventOpacity.OPAQUE);
		Transp t = ical4jHelper.getTransp(event.getOpacity());
		assertEquals(Transp.OPAQUE, t);

		event.setOpacity(EventOpacity.TRANSPARENT);
		t = ical4jHelper.getTransp(event.getOpacity());
		assertEquals(Transp.TRANSPARENT, t);

	}

	@Test
	public void testGetDtEnd() {
		Event event = new Event();
		Calendar cal = getCalendarPrecisionOfSecond();
		event.setStartDate(cal.getTime());
		event.setDuration(3600);
		DtEnd dtend = ical4jHelper.getDtEnd(event.getStartDate(),
				event.getDuration(), false);
		assertEquals(cal.getTime().getTime() + 3600000, dtend.getDate()
				.getTime());
	}

	@Test
	public void testGetDtStart() {
		Event event = new Event();
		Calendar cal = getCalendarPrecisionOfSecond();
		event.setStartDate(cal.getTime());
		
		DtStart dtstart = ical4jHelper.getDtStart(event.getStartDate());
		
		assertEquals(cal.getTime().getTime(), dtstart.getDate().getTime());
	}

	@Test
	public void testGetDtStartAllDay() {
		Calendar cal = getCalendarPrecisionOfSecond();
		Event event = new Event();
		event.setAllday(true);
		event.setStartDate(cal.getTime());

		DtStart dtstart = ical4jHelper.getDtStart(event.getStartDate());

		assertEquals(dtstart.getDate().getTime(), event.getStartDate().getTime());
	}

	@Test
	public void testGetDurationAllDay() {
		Event event = new Event();
		Calendar cal = getCalendarPrecisionOfSecond();
		event.setStartDate(cal.getTime());
		event.setAllday(true);
		
		Duration duration = ical4jHelper.getDuration(event.getStartDate(), event.getEndDate());
		
		assertEquals(duration.getDuration().getWeeks(), 0);
		assertEquals(duration.getDuration().getDays(), 1);
		assertEquals(duration.getDuration().getHours(), 0);
		assertEquals(duration.getDuration().getMinutes(), 0);
		assertEquals(duration.getDuration().getSeconds(), 0);
	}

	@Test
	public void testGetRole() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");
		at.setEmail("adrien@zz.com");
		at.setParticipation(Participation.accepted());
		at.setParticipationRole(ParticipationRole.CHAIR);

		Role role = ical4jHelper.getRole(at);
		assertEquals(role, Role.CHAIR);
	}

	@Test
	public void testGetCn() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");

		Cn cn = ical4jHelper.getCn(at);
		assertEquals("adrien", cn.getValue());
	}

	@Test
	public void testGetPartStat() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");
		at.setEmail("adrien@zz.com");
		at.setParticipation(Participation.accepted());
		at.setParticipationRole(ParticipationRole.CHAIR);

		PartStat ps = ical4jHelper.getPartStat(at);
		assertEquals(ps, PartStat.ACCEPTED);
	}

	@Test
	public void testGetRRule() {
		Calendar cal = getCalendarPrecisionOfSecond();
		cal.setTime(new Date());
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		Event event = new Event();

		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.weekly);
		er.setFrequence(1);
		er.addException(cal.getTime());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());

		er.setDays(new RecurrenceDays(RecurrenceDay.Monday, RecurrenceDay.Tuesday, RecurrenceDay.Wednesday,
				RecurrenceDay.Thursday, RecurrenceDay.Friday));
		er.setEnd(null);
		event.setRecurrence(er);

		RRule rule = ical4jHelper.getRRule(event);
		Recur recur = rule.getRecur();
		assertTrue(recur.getDayList().contains(WeekDay.MO));
		assertTrue(recur.getDayList().contains(WeekDay.TU));
		assertTrue(recur.getDayList().contains(WeekDay.WE));
		assertTrue(recur.getDayList().contains(WeekDay.TH));
		assertTrue(recur.getDayList().contains(WeekDay.FR));

		assertNull(er.getEnd());

		assertEquals(er.getFrequence(), 1);
		er.setKind(RecurrenceKind.weekly);

		assertThat(er.getExceptions()).hasSize(2);
	}

	@Test @Slow
	public void testParserAttendee() throws IOException, ParserException {
		String ics = IOUtils.toString(getStreamICS("bugGn.ics"));
		List<Event> event = ical4jHelper.parseICS(ics, getDefaultObmUser());
		assertEquals(event.size(), 1);
	}

	private InputStream getStreamICS(String filename) {
		InputStream in = ClassLoader.getSystemClassLoader()
				.getResourceAsStream("icsFile/" + filename);
		if (in == null) {
			fail("Cannot load " + filename);
		}
		return in;
	}
	
	@Test @Slow
	public void testParsingICSFileOf200kio() throws IOException, ParserException {
		final String ics = IOUtils.toString(getStreamICS("bellemin-calendrierobm.ics"));
		
		final List<Event> events = ical4jHelper.parseICS(ics, getDefaultObmUser());
		for (final Event event: events) {
			assertNotNull(event.getTitle());
		}
		assertEquals(221, events.size());
	}
	
	@Test @Slow
	public void testParsingICSFiles() throws IOException, ParserException {
		final String[] icsFiles = {"cdespino.ics", "dkaplan.ics"};
		
		for (String icsFile: icsFiles) {
			final String ics = IOUtils.toString(getStreamICS(icsFile));
			ical4jHelper.parseICS(ics, getDefaultObmUser());	
		}
		assertTrue(true);
	}

	@Test
	public void testParsingICSFilesWhichDontProvideUid() throws IOException, ParserException {
		final String ics = IOUtils.toString(getStreamICS("calendar_pst.ics"));
		
		final List<Event> events = ical4jHelper.parseICSEvent(ics, getDefaultObmUser());
		for (final Event event: events) {
			assertNotNull(event.getExtId());
		}
		
		assertEquals(10, events.size());
	}
	
	@Test
	public void testBuildIcsInvitationReply() {
		final Event event = buildEvent();
		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);
		AccessToken token = new AccessToken(0, "OBM");
		
		final String ics = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);
		
		String icsAttendee = "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;RSVP=TRUE;" +
		"CN=OBM USER 3;ROLE=\r\n REQ-PARTICIPANT:mailto:obm3@obm.org";
		
		Assert.assertThat(ics, StringContains.containsString(icsAttendee));
		Assert.assertEquals(1, countStringOccurrences(ics, "ATTENDEE;"));	
	}
	
	private Event buildEvent() {
		final Event event = new Event();
		
		event.setStartDate(new Date(1334914893));

		event.setExtId(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262eza9"));
		event.setTitle("rdv abc");
		event.setOwner("obm");
		event.setDuration(3600);
		event.setLocation("obm loca");
		
		final Attendee at1 = new Attendee();
		at1.setDisplayName("OBM ORGANIZER");
		at1.setEmail("obm@obm.org");
		at1.setParticipation(Participation.accepted());
		at1.setParticipationRole(ParticipationRole.CHAIR);
		at1.setOrganizer(true);
		
		final Attendee at2 = new Attendee();
		at2.setDisplayName("OBM USER 2");
		at2.setEmail("obm2@obm.org");
		at2.setParticipation(Participation.accepted());
		at2.setParticipationRole(ParticipationRole.REQ);
		
		final Attendee at3 = new Attendee();
		at3.setDisplayName("OBM USER 3");
		at3.setEmail("obm3@obm.org");
		at3.setParticipation(Participation.accepted());
		at3.setParticipationRole(ParticipationRole.REQ);
		
		final Attendee at4 = new Attendee();
		at4.setDisplayName("OBM USER 4");
		at4.setEmail("obm4@obm.org");
		at4.setParticipation(Participation.declined());
		at4.setParticipationRole(ParticipationRole.REQ);
		
		event.addAttendee(at1);
		event.addAttendee(at2);
		event.addAttendee(at3);
		event.addAttendee(at4);
		return event;
	}

	private Event buildRecurrentEventWithExceptions() {
		Event event = buildEvent();
		event.setExtId(new EventExtId("99fbaea9-f1dd-4cd5-9d1b-553a8a083224"));
		event.setTitle("recurrent event with exceptions");
		event.getRecurrence().setKind(RecurrenceKind.daily);
		Event eventEx = event.clone();
		Date recurrenceId = new org.joda.time.DateTime(eventEx.getStartDate().getTime())
				.plusDays(2).toDate();
		eventEx.setRecurrenceId(recurrenceId);
		Date exDate = new org.joda.time.DateTime(eventEx.getStartDate().getTime()).plusDays(4)
				.toDate();

		event.getRecurrence().addEventException(eventEx);
		event.getRecurrence().addException(exDate);
		return event;
	}
	
	private Ical4jUser buildObmUser(final Attendee attendeeReply) {
		final ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName(new UserEmailParserUtils().getDomain(attendeeReply.getEmail()));
		obmDomain.setUuid("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		return Ical4jUser.Factory.create().createIcal4jUser(attendeeReply.getEmail(), obmDomain);
	}
	
	private int countStringOccurrences(String str, String occ) {
		int i = str.indexOf(occ);
		int countIndexOf = 0;
		while (i != -1) {
			countIndexOf += 1;
			i = str.indexOf(occ, i + occ.length());
		}
		return countIndexOf;
	}

	@Test
	public void testInvitationRequestWithLongAttendee() {
		Event event = buildEvent();
		Attendee superLongAttendee = new Attendee();
		superLongAttendee.setDisplayName("my attendee is more than 75 characters long");
		superLongAttendee.setEmail("so-we-just-give-him-a-very-long-email-address@test.com");
		event.addAttendee(superLongAttendee);
		AccessToken token = new AccessToken(0, "OBM");

		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);

		String icsRequest = ical4jHelper.buildIcsInvitationRequest(ical4jUser, event, token);
		String icsCancel = ical4jHelper.buildIcsInvitationCancel(ical4jUser, event, token);
		String icsReply = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);
		
		checkStringLengthLessThan(icsRequest, 75);
		checkStringLengthLessThan(icsCancel, 75);
		checkStringLengthLessThan(icsReply, 75);
	}
	
	@Test
	public void testXObmDomainInInvitation() {
		Event event = buildEvent();

		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);
		AccessToken token = new AccessToken(0, "OBM");

		String icsRequest = ical4jHelper.buildIcsInvitationRequest(ical4jUser, event, token);
		String icsCancel = ical4jHelper.buildIcsInvitationCancel(ical4jUser, event, token);
		String icsReply = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);
		
		checkContainIcsProperty(icsRequest, "X-OBM-DOMAIN", ical4jUser.getObmDomain().getName());
		checkContainIcsProperty(icsCancel, "X-OBM-DOMAIN", ical4jUser.getObmDomain().getName());
		checkContainIcsProperty(icsReply, "X-OBM-DOMAIN", ical4jUser.getObmDomain().getName());
	}
	
	private void checkContainIcsProperty(String ics, String propertyName, String propertyValue) {
		Assert.assertThat(ics, new StringContainsIcsProperty(propertyName, propertyValue));
	}

	@Test
	public void testXObmDomainUUIDInInvitation() {
		Event event = buildEvent();

		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);
		AccessToken token = new AccessToken(0, "OBM");

		String icsRequest = ical4jHelper.buildIcsInvitationRequest(ical4jUser, event, token);
		String icsCancel = ical4jHelper.buildIcsInvitationCancel(ical4jUser, event, token);
		String icsReply = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);
		
		checkContainIcsProperty(icsRequest, "X-OBM-DOMAIN-UUID", ical4jUser.getObmDomain().getUuid());
		checkContainIcsProperty(icsCancel, "X-OBM-DOMAIN-UUID", ical4jUser.getObmDomain().getUuid());
		checkContainIcsProperty(icsReply, "X-OBM-DOMAIN-UUID", ical4jUser.getObmDomain().getUuid());
	}
	
	@Test
	public void testCommentPropertyInReply() {
		Event event = buildEvent();

		final Attendee attendeeReply = event.getAttendees().get(2);
		Participation status = attendeeReply.getParticipation();
		status.setComment(new Comment("I declined your invitation."));
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);
		AccessToken token = new AccessToken(0, "OBM");

		String icsReply = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);

		checkContainIcsProperty(icsReply, "COMMENT", status.getComment().serializeToString());
	}

	@Test
	public void testNullCommentNotAppendedToICS() {
		Event event = buildEvent();

		final Attendee attendeeReply = event.getAttendees().get(2);
		Participation status = attendeeReply.getParticipation();
		status.setComment(null);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);
		AccessToken token = new AccessToken(0, "OBM");

		String icsReply = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);

		assertThat(icsReply).doesNotContain("COMMENT");
	}

	private void checkStringLengthLessThan(String ics, int length) {
		Iterable<String> lines = Splitter.on("\r\n").split(ics);
		for (String line: lines) {
			Assert.assertThat(line, new StringLengthLessThan(length));
		}
	}
    @Test
    public void testInvitationIcsContainsEventOriginProperty() {
        Event event = buildEvent();
            
        final Attendee attendeeReply = event.getAttendees().get(2);
        final Ical4jUser obmUser = buildObmUser(attendeeReply);
        AccessToken token = new AccessToken(0, "OBM");
        
        String icsRequest = ical4jHelper.buildIcsInvitationRequest(obmUser, event, token);
        String icsCancel = ical4jHelper.buildIcsInvitationCancel(obmUser, event, token);
        String icsReply = ical4jHelper.buildIcsInvitationReply(event, obmUser, token);
            
        String XOBMORIGIN = "X-OBM-ORIGIN:OBM";
        Assert.assertTrue(icsRequest.contains(XOBMORIGIN));
        Assert.assertTrue(icsCancel.contains(XOBMORIGIN));
        Assert.assertTrue(icsReply.contains(XOBMORIGIN));    
    }

	@Test
	public void testInvitationIcsHasCorrectEndingRecurrenceDate() {
		Event event = new Event();
		event.setStartDate(new Date());
		event.setExtId(new EventExtId("123"));
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		eventRecurrence.setEnd(DateUtils.date("2012-03-30T11:00:00Z"));
		Attendee attendee = new Attendee();
		attendee.setEmail("foo@fr");

		event.addAttendee(attendee);
		event.setRecurrence(eventRecurrence);

		AccessToken token = new AccessToken(0, "OBM");
		Ical4jUser obmUser = buildObmUser(attendee);

		String icsRequest = ical4jHelper.buildIcsInvitationRequest(obmUser, event, token);

		String UNTIL = "UNTIL=20120330T110000Z";
		assertThat(icsRequest).contains(UNTIL);
	}

	@Test @Slow
	public void executeParsingTestLotusNotesICS() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2891.ics";
		List<Event> events = testIcsParsing(icsFilename);
		assertThat(events).isNotNull().isNotEmpty();
	}

	private List<Event> testIcsParsing(String icsFilename) throws IOException,
			ParserException {
		InputStream stream = getStreamICS(icsFilename);
		String ics = IOUtils.toString(stream);
		return ical4jHelper.parseICS(ics, getDefaultObmUser());
	}
	
	@Test @Slow
	public void executeJIRA2940() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2940.ics";
		List<Event> events = testIcsParsing(icsFilename);
		assertThat(events).isNotNull();
	}
	
	@Test
	public void testDefaultParticipation() throws IOException, ParserException {
		String icsFilename = "default-part-stat.ics";
		List<Event> events = testIcsParsing(icsFilename);
		Event event = Iterables.getOnlyElement(events);
		assertThat(event.getAttendees()).hasSize(2);
		Attendee userc = event.getAttendees().get(1);
		assertThat(userc.getParticipation()).isNotNull().isEqualTo(Participation.needsAction());
	}

	@Test
	public void testImportICSWithRecurrenceIdAfterParentEventDefinition() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963sorted.ics";
		List<Event> events = testIcsParsing(icsFilename);

		assertThat(events).hasSize(2);

		Event firstParentEvent = events.get(0);
		Event secondParentEvent = events.get(1);
		assertThat(firstParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
		assertThat(secondParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
	}

	@Test
	public void testImportICSWithRecurrenceIdBeforeParentEventDefinition() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963unsorted.ics";
		List<Event> events = testIcsParsing(icsFilename);

		assertThat(events).hasSize(2);

		Event firstParentEvent = events.get(0);
		Event secondParentEvent = events.get(1);
		assertThat(firstParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
		assertThat(secondParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
	}

	@Test
	public void testImportICSWithOnlyRecurrenceId() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963onlyRecurrenceId.ics";
		List<Event> events = testIcsParsing(icsFilename);

		assertThat(events).isEmpty();
	}
	
	@Test
	public void testParseICSWithQuotesIllegalCharacter() throws IOException, ParserException {
		String icsFilename = "OBMFULL-3355.ics";
		List<Event> events = testIcsParsing(icsFilename);
		
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getAttendees()).containsOnly(
				Attendee.builder().email("usera@obm.lng.org").build(),
				Attendee.builder().email("userb@obm.lng.org").build(),
				Attendee.builder().email("userc@obm.lng.org").build());
	}
	
	@Test
	public void testParseICSWithIllegalCharacter() throws IOException, ParserException {
		String icsFilename = "illegalAttendeeCN.ics";
		List<Event> events = testIcsParsing(icsFilename);
		
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getAttendees()).containsOnly(
				Attendee.builder().email("usera@obm.lng.org").build(),
				Attendee.builder().email("userb@obm.lng.org").build());
	}

	@Test
	public void testDtstampWithNullTimeUpdate() {
		Event event = buildEvent();
		Ical4jUser obmUser = buildObmUser(event.findOrganizer());
		AccessToken token = new AccessToken(0, "OBM");

		event.setTimeCreate(DateUtils.date("2012-01-01T09:12:13Z"));
		event.setTimeUpdate(null);

		String icsRequest = ical4jHelper.buildIcsInvitationRequest(obmUser, event, token);
		String icsCancel = ical4jHelper.buildIcsInvitationCancel(obmUser, event, token);

		String DTSTAMP = "DTSTAMP:20120101T091213Z";

		assertThat(icsRequest).contains(DTSTAMP);
		assertThat(icsCancel).contains(DTSTAMP);
	}

	@Test
	public void testDtstampWithNotNullTimeUpdate() {
		Event event = buildEvent();
		Ical4jUser obmUser = buildObmUser(event.findOrganizer());
		AccessToken token = new AccessToken(0, "OBM");

		event.setTimeCreate(DateUtils.date("2012-01-01T09:12:13Z"));
		event.setTimeUpdate(DateUtils.date("2012-01-02T10:14:15Z"));

		String icsRequest = ical4jHelper.buildIcsInvitationRequest(obmUser, event, token);
		String icsCancel = ical4jHelper.buildIcsInvitationCancel(obmUser, event, token);

		String DTSTAMP = "DTSTAMP:20120102T101415Z";
		assertThat(icsRequest).contains(DTSTAMP);
		assertThat(icsCancel).contains(DTSTAMP);
	}
	
	@Test
	public void testDtstampIsTransactionTimeInReply() throws Exception {
		Event event = buildEvent();
		Ical4jUser obmUser = buildObmUser(event.findOrganizer());
		AccessToken token = new AccessToken(0, "OBM");
		
		String ics = ical4jHelper.buildIcsInvitationReply(event, obmUser, token);
		VEvent vEvent = (VEvent) new CalendarBuilder().build(new StringReader(ics)).getComponents().get(0);
		
		assertThat(vEvent.getDateStamp().getDate()).isEqualTo(now);
	}

	@Test
	public void testBuildICS() throws IOException {
		Event normalEvent = buildEvent();
		Event eventWithExceptions = buildRecurrentEventWithExceptions();
		Collection<Event> events = Lists.newArrayList(normalEvent, eventWithExceptions);

		Ical4jUser obmUser = buildObmUser(normalEvent.findOrganizer());
		AccessToken token = new AccessToken(0, "OBM");

		InputStream stream = getStreamICS("eventsWithExceptions.ics");
		String expectedICSWithoutTimestamp = stripTimestamps(IOUtils.toString(stream));
		stream.close();

		String ics = ical4jHelper.buildIcs(obmUser, events, token);
		assertThat(stripTimestamps(ics)).isEqualTo(expectedICSWithoutTimestamp);
	}
	
	@Test
	public void testParseIcsWithEmptyUid() throws Exception {
		String ics = IOUtils.toString(getStreamICS("meetingWithEmptyUid.ics"));
		List<Event> events = ical4jHelper.parseICS(ics, getDefaultObmUser());

		assertThat(events).hasSize(1);
		assertThat(events.get(0).getExtId().getExtId()).isNotNull();
	}
}
