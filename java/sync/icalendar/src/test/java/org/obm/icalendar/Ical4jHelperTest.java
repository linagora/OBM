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
package org.obm.icalendar;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import net.fortuna.ical4j.model.parameter.CuType;
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
import net.fortuna.ical4j.util.TimeZones;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.utils.UserEmailParserUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarUserType;
import org.obm.sync.calendar.Comment;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyInterval;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.ResourceAttendee;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.calendar.UnidentifiedAttendee;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.date.DateProvider;
import org.obm.sync.exception.IllegalRecurrenceKindException;
import org.obm.sync.services.AttendeeService;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;

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
	private EventExtId.Factory eventExtIdFactory;
	private AttendeeService attendeeService;
	private Date now;

	@Before
	public void setUp() {
		now = getCalendarPrecisionOfSecond().getTime();
		dateProvider = createMock(DateProvider.class);
		eventExtIdFactory = createMock(EventExtId.Factory.class);
		attendeeService = new SimpleAttendeeService();
		ical4jHelper = new Ical4jHelper(dateProvider, eventExtIdFactory, attendeeService);
		
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
		return Ical4jUser.Factory.create().createIcal4jUser("test@test.obm.lng.org", getDefaultObmDomain());
	}

	protected ObmDomain getDefaultObmDomain() {
		return ObmDomain
				.builder()
				.uuid("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6")
				.name("test.tlse.lng")
				.build();
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
	public void testGetIsAllDay() throws ICSConversionException {

		Calendar cal = getCalendarPrecisionOfSecond();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		DtStart dtStart = new DtStart(new net.fortuna.ical4j.model.Date(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

		Cache<String, Optional<Attendee>> cache = newCache();
		DtEnd dtEnd = new DtEnd(new net.fortuna.ical4j.model.Date(cal.getTime()));

		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		AccessToken token = new AccessToken(0, null);
		token.setUserEmail("adrien@zz.com");
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, cache);
		assertTrue(event.isAllday());

		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		dtStart = new DtStart(new DateTime(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		dtEnd = new DtEnd(new DateTime(cal.getTime()));
		vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event1 = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, cache);
		assertFalse(event1.isAllday());
	}

	@Test @Slow
	public void testGetDuration() throws ICSConversionException {

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
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(172800, event.getDuration());

	}

	@Test
	public void testGetPrivacyPublic() throws ICSConversionException {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PUBLIC);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(EventPrivacy.PUBLIC, event.getPrivacy());
	}

	@Test
	public void testGetPrivacyPrivate() throws ICSConversionException {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PRIVATE);
		Event event1 = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(EventPrivacy.PRIVATE, event1.getPrivacy());
	}

	@Test
	public void testGetPrivacyConfidential() throws ICSConversionException {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.CONFIDENTIAL);
		Event event1 = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(EventPrivacy.CONFIDENTIAL, event1.getPrivacy());
	}

	@Test
	public void testGetPrivacyIsPublicWhenOther() throws ICSConversionException {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(new Clazz("other"));
		Event event1 = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(EventPrivacy.PUBLIC, event1.getPrivacy());
	}

	@Test
	public void testGetPrivacyIsPublicWhenNull() throws ICSConversionException {
		VEvent vEvent = new VEvent();
		Event event1 = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(EventPrivacy.PUBLIC, event1.getPrivacy());
	}
	
	@Test
	public void testGetOwner() throws URISyntaxException, ICSConversionException {
		Organizer orga = new Organizer();
		orga.getParameters().add(new Cn("Adrien Poupard"));
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals("Adrien Poupard", event.getOwner());
	}

	@Test
	public void testGetOwner1() throws URISyntaxException, ICSConversionException {
		Organizer orga = new Organizer();
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals("adrien@zz.com", event.getOwner());
	}

	@Test
	public void testGetAlertWithDurationAndNoRepeat() throws ICSConversionException {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		va.getProperties().add(new Duration(dur));
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertNull(event.getAlert());
	}
	
	@Test
	public void testGetAlertWithoutRepeatAndWithoutDuration() throws ICSConversionException {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertFalse(
				new Integer(-1).equals(event.getAlert())
		);
	}
	
	@Test
	public void testGetAlertWithRepeatAndNoDuration() throws ICSConversionException {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		va.getProperties().add(new Repeat());
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertNull(event.getAlert());
	}
	
	@Test
	public void testGetAlertWithRepeatAndDuration() throws ICSConversionException {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		va.getProperties().add(new Repeat());
		va.getProperties().add(new Duration(dur));
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertFalse(
				new Integer(-1).equals(event.getAlert())
		);
	}

	@Test @Slow
	public void testGetRecurence() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("getRecurence.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
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
	public void testGetAttendees() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("attendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(3, event.getAttendees().size());
	}


	@Test
	public void testOrganizerInAttendess() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("organizerInAttendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
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
	public void testOrganizerNotInAttendess() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("organizerNotInAttendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
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
	public void testIsInternal() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("eventInternal.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertTrue(event.isInternalEvent());

	}

	@Test @Slow
	public void testCreated() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(1244470973000L, event.getTimeCreate().getTime());
	}

	@Test @Slow
	public void testLastModified() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertEquals(1244470995000L, event.getTimeUpdate().getTime());

	}

	@Test @Slow
	public void testLastModifiedNull() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("eventNewComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertNull(event.getTimeUpdate());

	}

	@Test @Slow
	public void testIsExternalObm() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("eventExternalObm.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertFalse(event.isInternalEvent());

	}

	@Test @Slow
	public void testIsExternal() throws IOException, ParserException, ICSConversionException {
		InputStream icsStream = getStreamICS("eventExternal.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = ical4jHelper.convertVEventToEvent(getDefaultObmUser(), vEvent, 0, newCache());
		assertFalse(event.isInternalEvent());

	}

	private Cache<String, Optional<Attendee>> newCache() {
		return CacheBuilder.newBuilder().build();
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
	public void testAppendAllDayWithStartDateObject() {
		final Event event = new Event();
		VEvent vevent = new VEvent();
		final DtStart startDate = new DtStart();
		startDate.setDate(new net.fortuna.ical4j.model.Date());
		vevent.getProperties().add(startDate);

		ical4jHelper.appendAllDay(event, vevent);
		assertTrue(event.isAllday());
	}

	@Test
	public void testAppendAllDayWithDurationOfOneDay() {
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-15T21:39:45Z"));
		final Event event = createEventByDuration(allDayDuration);
		assertTrue(event.isAllday());
	}

	private Event createEventByDuration(Duration duration) {
		final Event event = new Event();
		VEvent vevent = new VEvent();
		vevent.getProperties().add(duration);
		ical4jHelper.appendAllDay(event, vevent);
		return event;
	}

	@Test
	public void testAppendAllDayWithDurationOfThreeDay() {
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-17T21:39:45Z"));
		final Event event = createEventByDuration(allDayDuration);
		assertTrue(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDurationOfOneWeek() {
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-21T21:39:45Z"));
		final Event event = createEventByDuration(allDayDuration);
		assertFalse(event.isAllday());
	}

	@Test
	public void testAppendAllDayWithDurationOfOneHour() {
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-14T22:39:45Z"));
		final Event event = createEventByDuration(allDayDuration);
		assertFalse(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDurationOfQuiteOneDay() {
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:40:00Z"),
				DateUtils.date("2004-12-15T21:30:00Z"));
		final Event event = createEventByDuration(allDayDuration);
		assertFalse(event.isAllday());
	}

	@Test
	public void testAppendAllDayWithDurationOfOneMinute() {
		final Duration allDayDuration = new Duration(
				DateUtils.date("2004-12-14T21:39:45Z"),
				DateUtils.date("2004-12-14T21:40:45Z"));
		final Event event = createEventByDuration(allDayDuration);
		assertFalse(event.isAllday());
	}
	
	@Test
	public void testGetPublicClazz() {
		Clazz clazz = ical4jHelper.getClazz(EventPrivacy.PUBLIC);
		assertEquals(Clazz.PUBLIC, clazz);
	}

	@Test
	public void testGetPrivateClazz() {
		Clazz clazz = ical4jHelper.getClazz(EventPrivacy.PRIVATE);
		assertEquals(Clazz.PRIVATE, clazz);
	}

	@Test
	public void testGetConfidentialClazz() {
		Clazz clazz = ical4jHelper.getClazz(EventPrivacy.CONFIDENTIAL);
		assertEquals(Clazz.CONFIDENTIAL, clazz);
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
		DtEnd dtend = ical4jHelper.getDtEnd(event.getStartDate(), event.getDuration());
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
	public void testGetRole() {
		Attendee at = UserAttendee
				.builder()
				.displayName("adrien")
				.email("adrien@zz.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.CHAIR)
				.build();

		Role role = ical4jHelper.getRole(at);
		assertEquals(role, Role.CHAIR);
	}

	@Test
	public void testGetCn() {
		Attendee at = UserAttendee
				.builder()
				.displayName("adrien")
				.build();

		Cn cn = ical4jHelper.getCn(at);
		assertEquals("adrien", cn.getValue());
	}

	@Test
	public void testGetPartStat() {
		Attendee at = UserAttendee
				.builder()
				.displayName("adrien")
				.email("adrien@zz.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.CHAIR)
				.build();

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
		ICSParsingResults parsingResults = ical4jHelper.parseICS(ics, getDefaultObmUser(), 0);
		assertEquals(parsingResults.getParsedEvents().size(), 1);
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
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
		
		ICSParsingResults parsingResults = ical4jHelper.parseICS(ics, getDefaultObmUser(), 0);
		for (final Event event: parsingResults.getParsedEvents()) {
			assertNotNull(event.getTitle());
		}
		assertEquals(221, parsingResults.getParsedEvents().size());
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
	}
	
	@Test @Slow
	public void testParsingICSFiles() throws IOException, ParserException {
		final String[] icsFiles = {"cdespino.ics", "dkaplan.ics"};
		
		for (String icsFile: icsFiles) {
			final String ics = IOUtils.toString(getStreamICS(icsFile));
			ical4jHelper.parseICS(ics, getDefaultObmUser(), 0);	
		}
		assertTrue(true);
	}

	@Test
	public void testParsingICSFilesWhichDontProvideUid() throws IOException, ParserException {
		final String ics = IOUtils.toString(getStreamICS("calendar_pst.ics"));

		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab0")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab1")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab2")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab3")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab4")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab5")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab6")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab7")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab8")).once();
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("ab9")).once();
		
		replay(eventExtIdFactory);
		ParsingResults<Event, VEvent> parsingResults = ical4jHelper.parseICSEvent(ics, getDefaultObmUser(), 0);
		verify(eventExtIdFactory);
		
		for (final Event event: parsingResults.getParsedItems()) {
			assertNotNull(event.getExtId());
		}
		
		assertEquals(10, parsingResults.getParsedItems().size());
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

	@Test
	public void testDateOfBuildIcsInvitationWithAllDayEvent() {
		final Event event = buildEvent();
		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);
		event.setAllday(true);
		AccessToken token = new AccessToken(0, "OBM");

		String invitationIcs = ical4jHelper.buildIcsInvitationRequest(ical4jUser, event, token);
		String cancelIcs = ical4jHelper.buildIcsInvitationCancel(ical4jUser, event, token);
		String replyIcs = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);

		String expectedStartDate = "DTSTART;VALUE=DATE:19700116";
		String expectedEndDate = "DTEND;VALUE=DATE:19700117";
		String notExpectedDuration = "DURATION";

		assertThat(invitationIcs).contains(expectedEndDate).contains(expectedStartDate)
			.doesNotContain(notExpectedDuration);
		assertThat(cancelIcs).contains(expectedEndDate).contains(expectedStartDate)
			.doesNotContain(notExpectedDuration);
		assertThat(replyIcs).contains(expectedEndDate).contains(expectedStartDate)
			.doesNotContain(notExpectedDuration);
	}

	@Test
	public void testDateOfBuildIcsInvitationOneHourEvent() {
		final Event event = buildEvent();
		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);
		AccessToken token = new AccessToken(0, "OBM");

		String invitationIcs = ical4jHelper.buildIcsInvitationRequest(ical4jUser, event, token);
		String cancelIcs = ical4jHelper.buildIcsInvitationCancel(ical4jUser, event, token);
		String replyIcs = ical4jHelper.buildIcsInvitationReply(event, ical4jUser, token);

		String expectedStartDate = "DTSTART:19700116T104834Z";
		String expectedDuration = "DURATION:PT1H";
		String notExpectedDEndDate = "DTEND";

		assertThat(invitationIcs).contains(expectedDuration).contains(expectedStartDate)
			.doesNotContain(notExpectedDEndDate);
		assertThat(cancelIcs).contains(expectedDuration).contains(expectedStartDate)
			.doesNotContain(notExpectedDEndDate);
		assertThat(replyIcs).contains(expectedDuration).contains(expectedStartDate)
			.doesNotContain(notExpectedDEndDate);
	}

	@Test
	public void testDateTimezoneUTC() {
		final Event event = buildEvent();
		final Ical4jUser user = buildObmUser(event.findOrganizer());
		final AccessToken token = new AccessToken(0, "OBM");
		String ics, expected;

		// A UTC alias
		event.setStartDate(DateUtils.dateInZone("2013-01-01T01:01:01", "GMT"));
		event.setTimezoneName(TimeZones.GMT_ID);
		expected = "DTSTART:20130101T010101Z";

		ics = ical4jHelper.buildIcs(user, Lists.newArrayList(event), token);
		assertThat(ics).contains(expected);
	}

	@Test
	public void testDateTimezoneNull() {
		final Event event = buildEvent();
		final Ical4jUser user = buildObmUser(event.findOrganizer());
		final AccessToken token = new AccessToken(0, "OBM");
		String ics, expected;

		// null timezoneName (uses UTC)
		event.setStartDate(DateUtils.dateInZone("2013-02-02T02:02:02", "UTC"));
		event.setTimezoneName(null);
		expected = "DTSTART:20130202T020202Z";

		ics = ical4jHelper.buildIcs(user, Lists.newArrayList(event), token);
		assertThat(ics).contains(expected);
	}

	@Test
	public void testDateTimezoneParis() {
		final Event event = buildEvent();
		final Ical4jUser user = buildObmUser(event.findOrganizer());
		final AccessToken token = new AccessToken(0, "OBM");
		String ics, expected;

		// Paris
		event.setStartDate(DateUtils.dateInZone("2013-03-03T03:03:03", "Europe/Paris"));
		event.setTimezoneName("Europe/Paris");
		expected = "DTSTART;TZID=Europe/Paris:20130303T030303";

		ics = ical4jHelper.buildIcsWithTimeZoneOnDtStart(user, Lists.newArrayList(event), token);
		assertThat(ics).contains(expected);
	}

	@Test
	public void testDateTimezoneJerusalem() {
		final Event event = buildEvent();
		final Ical4jUser user = buildObmUser(event.findOrganizer());
		final AccessToken token = new AccessToken(0, "OBM");
		String ics, expected;

		// A different Timezone
		event.setStartDate(DateUtils.dateInZone("2013-04-04T04:04:04", "Asia/Jerusalem"));
		event.setTimezoneName("Asia/Jerusalem");
		expected = "DTSTART;TZID=Asia/Jerusalem:20130404T040404";

		ics = ical4jHelper.buildIcsWithTimeZoneOnDtStart(user, Lists.newArrayList(event), token);
		assertThat(ics).contains(expected);
	}

	private Event buildEvent() {
		final Event event = new Event();
		
		event.setStartDate(new Date(1334914893));

		event.setExtId(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262eza9"));
		event.setTitle("rdv abc");
		event.setOwner("obm");
		event.setDuration(3600);
		event.setLocation("obm loca");
		
		final Attendee at1 = UserAttendee
				.builder()
				.displayName("OBM ORGANIZER")
				.email("obm@obm.org")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build();
		
		final Attendee at2 = UserAttendee
				.builder()
				.displayName("OBM USER 2")
				.email("obm2@obm.org")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.REQ)
				.asOrganizer()
				.build();
		
		final Attendee at3 = UserAttendee
				.builder()
				.displayName("OBM USER 3")
				.email("obm3@obm.org")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.REQ)
				.asOrganizer()
				.build();
		
		final Attendee at4 = UserAttendee
				.builder()
				.displayName("OBM USER 4")
				.email("obm4@obm.org")
				.participation(Participation.declined())
				.participationRole(ParticipationRole.REQ)
				.asOrganizer()
				.build();
		
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
		ObmDomain obmDomain = ObmDomain
                				.builder()
                				.uuid("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6")
                				.name(new UserEmailParserUtils().getDomain(attendeeReply.getEmail()))
                				.build();
		
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
		Attendee superLongAttendee = UserAttendee
				.builder()
				.displayName("my attendee is more than 75 characters long")
				.email("so-we-just-give-him-a-very-long-email-address@test.com")
				.build();
		
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
    public void testInvitationIcsContainsPublicClassProperty() {
        Event event = buildEvent();
        event.setPrivacy(EventPrivacy.PUBLIC);
            
        final Attendee attendeeReply = event.getAttendees().get(2);
        final Ical4jUser obmUser = buildObmUser(attendeeReply);
        AccessToken token = new AccessToken(0, "OBM");
        
        String icsRequest = ical4jHelper.buildIcsInvitationRequest(obmUser, event, token);
        String icsCancel = ical4jHelper.buildIcsInvitationCancel(obmUser, event, token);
        String icsReply = ical4jHelper.buildIcsInvitationReply(event, obmUser, token);
            
        String CLASS = "CLASS:PUBLIC";
        Assert.assertTrue(icsRequest.contains(CLASS));
        Assert.assertTrue(icsCancel.contains(CLASS));
        Assert.assertTrue(icsReply.contains(CLASS));    
    }
    
    @Test
    public void testInvitationIcsContainsPrivateClassProperty() {
        Event event = buildEvent();
        event.setPrivacy(EventPrivacy.PRIVATE);
            
        final Attendee attendeeReply = event.getAttendees().get(2);
        final Ical4jUser obmUser = buildObmUser(attendeeReply);
        AccessToken token = new AccessToken(0, "OBM");
        
        String icsRequest = ical4jHelper.buildIcsInvitationRequest(obmUser, event, token);
        String icsCancel = ical4jHelper.buildIcsInvitationCancel(obmUser, event, token);
        String icsReply = ical4jHelper.buildIcsInvitationReply(event, obmUser, token);
            
        String CLASS = "CLASS:PRIVATE";
        Assert.assertTrue(icsRequest.contains(CLASS));
        Assert.assertTrue(icsCancel.contains(CLASS));
        Assert.assertTrue(icsReply.contains(CLASS));    
    }
    
    @Test
    public void testInvitationIcsContainsConfidentialClassProperty() {
        Event event = buildEvent();
        event.setPrivacy(EventPrivacy.CONFIDENTIAL);
            
        final Attendee attendeeReply = event.getAttendees().get(2);
        final Ical4jUser obmUser = buildObmUser(attendeeReply);
        AccessToken token = new AccessToken(0, "OBM");
        
        String icsRequest = ical4jHelper.buildIcsInvitationRequest(obmUser, event, token);
        String icsCancel = ical4jHelper.buildIcsInvitationCancel(obmUser, event, token);
        String icsReply = ical4jHelper.buildIcsInvitationReply(event, obmUser, token);
            
        String CLASS = "CLASS:CONFIDENTIAL";
        Assert.assertTrue(icsRequest.contains(CLASS));
        Assert.assertTrue(icsCancel.contains(CLASS));
        Assert.assertTrue(icsReply.contains(CLASS));    
    }

	@Test
	public void testInvitationIcsHasCorrectEndingRecurrenceDate() {
		Event event = new Event();
		event.setStartDate(new Date());
		event.setExtId(new EventExtId("123"));
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		eventRecurrence.setEnd(DateUtils.date("2012-03-30T11:00:00Z"));
		Attendee attendee = UserAttendee.builder().email("foo@fr").build();

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
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);
		assertThat(parsingResults.getParsedEvents()).isNotEmpty();
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
	}

	private ICSParsingResults testIcsParsing(String icsFilename) throws IOException,
			ParserException {
		InputStream stream = getStreamICS(icsFilename);
		String ics = IOUtils.toString(stream);
		return ical4jHelper.parseICS(ics, getDefaultObmUser(), 0);
	}
	
	@Test @Slow
	public void executeJIRA2940() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2940.ics";
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);
		assertThat(parsingResults.getParsedEvents()).isNotEmpty();
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
	}
	
	@Test
	public void testDefaultParticipation() throws IOException, ParserException {
		String icsFilename = "default-part-stat.ics";
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
		Event event = Iterables.getOnlyElement(parsingResults.getParsedEvents());
		assertThat(event.getAttendees()).hasSize(2);
		Attendee userc = event.getAttendees().get(1);
		assertThat(userc.getParticipation()).isNotNull().isEqualTo(Participation.needsAction());
	}

	@Test
	public void testImportICSWithRecurrenceIdAfterParentEventDefinition() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963sorted.ics";
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);

		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
		List<Event> events = parsingResults.getParsedEvents();
		assertThat(events).hasSize(2);

		Event firstParentEvent = events.get(0);
		Event secondParentEvent = events.get(1);
		assertThat(firstParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
		assertThat(secondParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
	}

	@Test
	public void testImportICSWithRecurrenceIdBeforeParentEventDefinition() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963unsorted.ics";
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);

		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
		List<Event> events = parsingResults.getParsedEvents();
		assertThat(events).hasSize(2);

		Event firstParentEvent = events.get(0);
		Event secondParentEvent = events.get(1);
		assertThat(firstParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
		assertThat(secondParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
	}

	@Test
	public void testImportICSWithOnlyRecurrenceId() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963onlyRecurrenceId.ics";
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);

		assertThat(parsingResults.getParsedEvents()).isEmpty();
		assertEquals(4, parsingResults.getRejectedEvents().size());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
	}
	
	@Test
	public void testParseICSWithQuotesIllegalCharacter() throws IOException, ParserException {
		String icsFilename = "OBMFULL-3355.ics";
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);
		
		List<Event> events = parsingResults.getParsedEvents();
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getAttendees()).containsOnly(
				UserAttendee.builder().email("usera@obm.lng.org").build(),
				UserAttendee.builder().email("userb@obm.lng.org").build(),
				UserAttendee.builder().email("userc@obm.lng.org").build());
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
	}
	
	@Test
	public void testParseICSWithIllegalCharacter() throws IOException, ParserException {
		String icsFilename = "illegalAttendeeCN.ics";
		ICSParsingResults parsingResults = testIcsParsing(icsFilename);
		
		List<Event> events = parsingResults.getParsedEvents();
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getAttendees()).containsOnly(
				UserAttendee.builder().email("usera@obm.lng.org").build(),
				UserAttendee.builder().email("userb@obm.lng.org").build());
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
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
	public void testBuildICSWithTimeZoneOnDtStart() throws IOException {
		Event normalEvent = buildEvent();
		Event eventWithExceptions = buildRecurrentEventWithExceptions();
		Collection<Event> events = Lists.newArrayList(normalEvent, eventWithExceptions);

		Ical4jUser obmUser = buildObmUser(normalEvent.findOrganizer());
		AccessToken token = new AccessToken(0, "OBM");

		InputStream stream = getStreamICS("eventsWithExceptionsDtStartWithTimeZone.ics");
		String expectedICSWithoutTimestamp = stripTimestamps(IOUtils.toString(stream));
		stream.close();

		String ics = ical4jHelper.buildIcsWithTimeZoneOnDtStart(obmUser, events, token);
		assertThat(stripTimestamps(ics)).isEqualTo(expectedICSWithoutTimestamp);
	}
	
	@Test
	public void testParseIcsWithEmptyUid() throws Exception {
		String ics = IOUtils.toString(getStreamICS("meetingWithEmptyUid.ics"));
		
		expect(eventExtIdFactory.generate()).andReturn(new EventExtId("abc"));
		replay(eventExtIdFactory);
		ICSParsingResults parsingResults = ical4jHelper.parseICS(ics, getDefaultObmUser(), 0);
		verify(eventExtIdFactory);

		List<Event> events = parsingResults.getParsedEvents();
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getExtId().getExtId()).isEqualTo("abc");
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
	}

	@Test
	public void testCalendarUserTypeToCuTypeUNKNOWN() {
		assertThat(ical4jHelper.calendarUserTypeToCuType(CalendarUserType.UNKNOWN)).isEqualTo(CuType.UNKNOWN);
	}
	
	@Test
	public void testCalendarUserTypeToCuTypeGROUP() {
		assertThat(ical4jHelper.calendarUserTypeToCuType(CalendarUserType.GROUP)).isEqualTo(CuType.GROUP);
	}
	
	@Test
	public void testCalendarUserTypeToCuTypeROOM() {
		assertThat(ical4jHelper.calendarUserTypeToCuType(CalendarUserType.ROOM)).isEqualTo(CuType.ROOM);
	}
	
	@Test
	public void testCalendarUserTypeToCuTypeRESOURCE() {
		assertThat(ical4jHelper.calendarUserTypeToCuType(CalendarUserType.RESOURCE)).isEqualTo(CuType.RESOURCE);
	}
	
	@Test
	public void testCalendarUserTypeToCuTypeINDIVIDUAL() {
		assertThat(ical4jHelper.calendarUserTypeToCuType(CalendarUserType.INDIVIDUAL)).isEqualTo(CuType.INDIVIDUAL);
	}

	@Test
	public void testFindAttendeeUsingCuTypeNullCuType() {
		String name = "attendee";
		String email = "attendee@obm.com";
		ObmDomain domain = getDefaultObmDomain();
		Attendee attendee = UnidentifiedAttendee.builder().email(email).entityId(1).build();
		AttendeeService service = createMock(AttendeeService.class);
		Ical4jHelper helper = new Ical4jHelper(dateProvider, eventExtIdFactory, service);
		
		expect(service.findAttendee(name, email, true, domain, 1)).andReturn(attendee).once();
		replay(service);
		
		assertThat(helper.findAttendeeUsingCuType(name, email, null, domain, 1)).isEqualTo(attendee);
		
		verify(service);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFindAttendeeUsingCuTypeIllegalCuType() {		
		ical4jHelper.findAttendeeUsingCuType("", "", "InvalidCuType", null, 1);
	}
	
	@Test
	public void testFindAttendeeUsingCuTypeROOMCuType() {
		String name = "attendee";
		String email = "attendee@obm.com";
		ObmDomain domain = getDefaultObmDomain();
		ResourceAttendee attendee = ResourceAttendee.builder().email(email).entityId(1).build();
		AttendeeService service = createMock(AttendeeService.class);
		Ical4jHelper helper = new Ical4jHelper(dateProvider, eventExtIdFactory, service);
		
		expect(service.findResourceAttendee(name, email, domain, 1)).andReturn(attendee).once();
		replay(service);
		
		assertThat(helper.findAttendeeUsingCuType(name, email, "ROOM", domain, 1)).isEqualTo(attendee);
		
		verify(service);
	}
	
	@Test
	public void testFindAttendeeUsingCuTypeRESOURCECuType() {
		String name = "attendee";
		String email = "attendee@obm.com";
		ObmDomain domain = getDefaultObmDomain();
		ResourceAttendee attendee = ResourceAttendee.builder().email(email).entityId(1).build();
		AttendeeService service = createMock(AttendeeService.class);
		Ical4jHelper helper = new Ical4jHelper(dateProvider, eventExtIdFactory, service);
		
		expect(service.findResourceAttendee(name, email, domain, 1)).andReturn(attendee).once();
		replay(service);
		
		assertThat(helper.findAttendeeUsingCuType(name, email, "RESOURCE", domain, 1)).isEqualTo(attendee);
		
		verify(service);
	}
	
	@Test
	public void testFindAttendeeUsingCuTypeGROUPCuType() {
		String name = "attendee";
		String email = "attendee@obm.com";
		ObmDomain domain = getDefaultObmDomain();
		
		assertThat(ical4jHelper.findAttendeeUsingCuType(name, email, "GROUP", domain, 1)).isNull();
	}
	
	@Test
	public void testFindAttendeeUsingCuTypeINDIVIDUALCuTypeReturnsUserIfExists() {
		String name = "attendee";
		String email = "attendee@obm.com";
		ObmDomain domain = getDefaultObmDomain();
		UserAttendee attendee = UserAttendee.builder().email(email).entityId(1).build();
		AttendeeService service = createMock(AttendeeService.class);
		Ical4jHelper helper = new Ical4jHelper(dateProvider, eventExtIdFactory, service);
		
		expect(service.findUserAttendee(name, email, domain)).andReturn(attendee).once();
		replay(service);
		
		assertThat(helper.findAttendeeUsingCuType(name, email, "INDIVIDUAL", domain, 1)).isEqualTo(attendee);
		
		verify(service);
	}
	
	@Test
	public void testFindAttendeeUsingCuTypeINDIVIDUALCuTypeSearchesUserFirstThenContact() {
		String name = "attendee";
		String email = "attendee@obm.com";
		ObmDomain domain = getDefaultObmDomain();
		ContactAttendee attendee = ContactAttendee.builder().email(email).entityId(1).build();
		AttendeeService service = createMock(AttendeeService.class);
		Ical4jHelper helper = new Ical4jHelper(dateProvider, eventExtIdFactory, service);
		
		expect(service.findUserAttendee(name, email, domain)).andReturn(null).once();
		expect(service.findContactAttendee(name, email, true, domain, 1)).andReturn(attendee).once();
		replay(service);
		
		assertThat(helper.findAttendeeUsingCuType(name, email, "INDIVIDUAL", domain, 1)).isEqualTo(attendee);
		
		verify(service);
	}
	
	private Event buildAllDayEvent(Date startDate, String tzName, int duration) {
		Event event = new Event();
		
		event.setExtId(new EventExtId("ExtId"));
		event.setAllday(true);
		event.setDuration(Event.SECONDS_IN_A_DAY * duration);
		event.setStartDate(startDate); // UTC
		event.setTimezoneName(tzName);
		event.addAttendee(UserAttendee.builder().email("organizer@obm.org").asOrganizer().build());
		
		return event;
	}
	
	private void assertIcsEquals(String compareFile, String actual) throws Exception {
		String expected = IOUtils.toString(getStreamICS(compareFile));
		
		assertThat(stripTimestamps(actual)).isEqualTo(stripTimestamps(expected));
	}
	
	@Test
	public void testBuildICSWithAllDayEventInEuropeParis() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		AccessToken token = new AccessToken(0, "");
		Event event = buildAllDayEvent(DateUtils.date("2013-03-28T23:00:00Z"), "Europe/Paris", 1);
		
		assertIcsEquals("alldayOneDay.ics", ical4jHelper.buildIcs(ical4jUser, ImmutableList.of(event), token));
	}
	
	@Test
	public void testBuildICSWithAllDayThreeDaysEventInEuropeParis() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		AccessToken token = new AccessToken(0, "");
		Event event = buildAllDayEvent(DateUtils.date("2013-03-28T23:00:00Z"), "Europe/Paris", 3);
		
		assertIcsEquals("alldayThreeDays.ics", ical4jHelper.buildIcs(ical4jUser, ImmutableList.of(event), token));
	}
	
	@Test
	public void testBuildICSWithAllDayEventInAmericaGuadeloupe() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		AccessToken token = new AccessToken(0, "");
		Event event = buildAllDayEvent(DateUtils.date("2013-03-29T04:00:00Z"), "America/Guadeloupe", 1);
		
		assertIcsEquals("alldayOneDay.ics", ical4jHelper.buildIcs(ical4jUser, ImmutableList.of(event), token));
	}
	
	@Test
	public void testBuildICSWithAllDayEventInAsiaBangkok() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		AccessToken token = new AccessToken(0, "");
		Event event = buildAllDayEvent(DateUtils.date("2013-03-28T17:00:00Z"), "Asia/Bangkok", 1);
		
		assertIcsEquals("alldayOneDay.ics", ical4jHelper.buildIcs(ical4jUser, ImmutableList.of(event), token));
	}

	private EventRecurrence getRecurrence(RecurrenceKind kind, int frequence) {
		return getRecurrence(kind, frequence, null);
	}

	private EventRecurrence getRecurrence(RecurrenceKind kind, int frequence, RecurrenceDays days) {
		EventRecurrence rec = new EventRecurrence();

		rec.setKind(kind);
		rec.setFrequence(frequence);
		if (days != null) {
			rec.setDays(days);
		}

		return rec;
	}

	private void assertRecurrenceEquals(Event event, EventRecurrence recurrence) {
		assertThat(event.getRecurrence()).isEqualTo(recurrence);
	}

	@Test
	public void testParseIcsWithMonthlyByDateRecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("monthlyByDate.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.monthlybydate, 1);

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0), recurrence);
	}

	@Test
	public void testParseIcsWithMonthlyByDateFreq3RecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("monthlyByDateFreq3.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.monthlybydate, 3);

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents()
				.get(0), recurrence);
	}

	@Test
	public void testParseIcsWithDailyRecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("daily.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.daily, 1);

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0), recurrence);
	}

	@Test
	public void testParseIcsWithDailyFreq4RecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("dailyFreq4.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.daily, 4);

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0), recurrence);
	}

	@Test
	public void testParseIcsWithWeeklyRecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("weekly.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.weekly, 1, new RecurrenceDays(RecurrenceDay.Wednesday)); // 2013-04-17 is wedsneday

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0), recurrence);
	}

	@Test
	public void testParseIcsWithWeeklyAllDaysRecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("weeklyAllDays.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.weekly, 1, RecurrenceDays.ALL_DAYS);

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0), recurrence);
	}

	@Test
	public void testParseIcsWithWeeklyAllDaysFreq3RecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("weeklyAllDaysFreq3.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.weekly, 3, RecurrenceDays.ALL_DAYS);

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0), recurrence);
	}

	@Test
	public void testParseIcsWithYearlyRecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("yearly.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.yearly, 1);

		assertRecurrenceEquals(ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0), recurrence);
	}

	@Test
	public void testParseIcsWithMonthlyByDay3WRRecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("monthlyByDay3WE.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.monthlybyday, 1);
		Event event = ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0);

		assertThat(event.getStartDate()).isEqualTo(DateUtils.date("2013-04-17T12:00:00Z")); // 3WE of 2013/04
		assertRecurrenceEquals(event, recurrence);
	}

	@Test
	public void testParseIcsWithMonthlyByDayMinus1SURecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("monthlyByDay-1SU.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.monthlybyday, 1);
		Event event = ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0);

		assertThat(event.getStartDate()).isEqualTo(DateUtils.date("2013-04-28T12:00:00Z")); // -1SU of 2013/04
		assertRecurrenceEquals(event, recurrence);
	}

	@Test
	public void testParseIcsWithMonthlyByDayMinus2TURecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("monthlyByDay-2TU.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.monthlybyday, 3);
		Event event = ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0);

		assertThat(event.getStartDate()).isEqualTo(DateUtils.date("2013-04-23T12:00:00Z")); // -2TU of 2013/04
		assertRecurrenceEquals(event, recurrence);
	}

	@Test
	public void testParseIcsWithMonthlyByDay2MORecurrentEvent() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("monthlyByDay2MO.ics"));
		EventRecurrence recurrence = getRecurrence(RecurrenceKind.monthlybyday, 2);
		Event event = ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0);

		assertThat(event.getStartDate()).isEqualTo(DateUtils.date("2013-04-08T12:00:00Z")); // 2MO of 2013/04
		assertRecurrenceEquals(event, recurrence);
	}

	@Test
	public void testParceIcsHandlesEXDATEs() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("eventWithEXDATEs.ics"));
		Event event = ical4jHelper.parseICS(ics, ical4jUser, 1).getParsedEvents().get(0);
		Set<Date> expectedExceptions = ImmutableSet.of(
				DateUtils.date("2005-01-13T21:00:00Z"),
				DateUtils.date("2005-01-14T21:00:00Z"),
				DateUtils.date("2005-02-01T21:00:00Z"));

		assertThat(event.getRecurrence().getExceptions()).isEqualTo(expectedExceptions);
	}

	@Test
	public void testParseIcsUsesAttendeeCache() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("4Events.ics"));
		AttendeeService attendeeService = createMock(AttendeeService.class);
		Ical4jHelper ical4jHelper = new Ical4jHelper(dateProvider, eventExtIdFactory, attendeeService);
		UserAttendee userAttendee = UserAttendee.builder().email("user@test.tlse.lng").build();
		UserAttendee organizerAttendee = UserAttendee.builder().email("organizer@test.tlse.lng").build();

		expect(attendeeService.findAttendee(isNull(String.class), eq("user@test.tlse.lng"), anyBoolean(), isA(ObmDomain.class), isA(Integer.class))).andReturn(userAttendee).once();
		expect(attendeeService.findAttendee(isNull(String.class), eq("organizer@test.tlse.lng"), anyBoolean(), isA(ObmDomain.class), isA(Integer.class))).andReturn(organizerAttendee).once();
		replay(attendeeService);

		ICSParsingResults parsingResults = ical4jHelper.parseICS(ics, ical4jUser, 1);

		verify(attendeeService);

		assertThat(parsingResults.getParsedEvents()).hasSize(4);
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
	}

	@Test
	public void testParseIcsSetsCorrectPropertiesOnAttendees() throws Exception {
		Ical4jUser ical4jUser = getDefaultObmUser();
		String ics = IOUtils.toString(getStreamICS("4Events.ics"));
		ICSParsingResults parsingResults = ical4jHelper.parseICS(ics, ical4jUser, 1);

		List<Event> events = parsingResults.getParsedEvents();
		String organizerEmail = "organizer@test.tlse.lng", userEmail = "user@test.tlse.lng";

		Event event1 = new Event(), event2 = new Event(), event3 = new Event(), event4 = new Event();

		event1.setTitle("Event1");
		event1.setDuration(3600);
		event1.setExtId(new EventExtId("1"));
		event1.setStartDate(DateUtils.date("2013-04-18T11:00:00Z"));
		event1.setTimezoneName("Etc/GMT");
		event1.setOwner(organizerEmail);
		event1.setOwnerEmail(organizerEmail);
		event1.addAttendee(UserAttendee.builder().email(organizerEmail).participation(Participation.accepted()).asOrganizer().build());
		event1.addAttendee(UserAttendee.builder().email(userEmail).build());

		event2.setTitle("Event2");
		event2.setDuration(3600);
		event2.setExtId(new EventExtId("2"));
		event2.setStartDate(DateUtils.date("2013-04-18T12:00:00Z"));
		event2.setTimezoneName("Etc/GMT");
		event2.setOwner(organizerEmail);
		event2.setOwnerEmail(organizerEmail);
		event2.addAttendee(UserAttendee.builder().email(organizerEmail).participation(Participation.accepted()).asOrganizer().build());
		event2.addAttendee(UserAttendee.builder().email(userEmail).participation(Participation.accepted()).build());

		event3.setTitle("Event3");
		event3.setDuration(3600);
		event3.setExtId(new EventExtId("3"));
		event3.setStartDate(DateUtils.date("2013-04-18T13:00:00Z"));
		event3.setTimezoneName("Etc/GMT");
		event3.setOwner(organizerEmail);
		event3.setOwnerEmail(organizerEmail);
		event3.addAttendee(UserAttendee.builder().email(organizerEmail).participation(Participation.accepted()).asOrganizer().build());
		event3.addAttendee(UserAttendee.builder().email(userEmail).build());

		event4.setTitle("Event4");
		event4.setDuration(3600);
		event4.setExtId(new EventExtId("4"));
		event4.setStartDate(DateUtils.date("2013-04-18T14:00:00Z"));
		event4.setTimezoneName("Etc/GMT");
		event4.setOwner(userEmail);
		event4.setOwnerEmail(userEmail);
		event4.addAttendee(UserAttendee.builder().email(userEmail).participation(Participation.accepted()).asOrganizer().build());
		event4.addAttendee(UserAttendee.builder().email(organizerEmail).participation(Participation.declined()).build());

		assertThat(events).containsOnly(event1, event2, event3, event4);
		assertTrue(parsingResults.getRejectedEvents().isEmpty());
		assertTrue(parsingResults.getRejectedTodos().isEmpty());
  }
	
	@Test
	public void testParseFreeBusy() {
		String expectedFreebusy = 
				"BEGIN:VCALENDAR\r\n" +
				"PRODID:-//Aliasource Groupe LINAGORA//OBM Calendar //FR\r\n" +
				"VERSION:2.0\r\n" +
				"CALSCALE:GREGORIAN\r\n" +
				"METHOD:REPLY\r\n" +
				"BEGIN:VFREEBUSY\r\n" +
				"DTSTAMP:20130604T075838Z\r\n" +
				"ORGANIZER:mailto:userb@ext.org\r\n" +
				"DTSTART:20130328T170000Z\r\n" +
				"DTEND:20130529T040000Z\r\n" +
				"ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=usera@ext.org;ROLE=OPT-PARTICIPANT:mailto:usera@ext.org\r\n" +
				"UID:uid\r\n" +
				"FREEBUSY;FBTYPE=BUSY:20130428T170000Z/20130428T180000Z\r\n" +
				"FREEBUSY;FBTYPE=BUSY:20130502T220000Z/20130503T220000Z\r\n" +
				"END:VFREEBUSY\r\n" +
				"END:VCALENDAR\r\n";
	
		FreeBusyInterval freeBusyIntervalOfSingleEvent = new FreeBusyInterval();
		freeBusyIntervalOfSingleEvent.setAllDay(false);
		freeBusyIntervalOfSingleEvent.setStart(DateUtils.date("2013-04-28T17:00:00Z"));
		freeBusyIntervalOfSingleEvent.setDuration(3600);
		
		FreeBusyInterval freeBusyIntervalOfAllDayEvent = new FreeBusyInterval();
		freeBusyIntervalOfAllDayEvent.setAllDay(true);
		freeBusyIntervalOfAllDayEvent.setStart(DateUtils.date("2013-05-02T22:00:00Z"));
		freeBusyIntervalOfAllDayEvent.setDuration(86400);
		
		FreeBusy freebusy = new FreeBusy();
		freebusy.setAtt(UserAttendee.builder().email("usera@ext.org").build());
		freebusy.setStart(DateUtils.date("2013-03-28T17:00:00Z"));
		freebusy.setEnd(DateUtils.date("2013-05-29T04:00:00Z"));
		freebusy.setOwner("userb@ext.org");
		freebusy.setUid("uid");
		freebusy.addFreeBusyInterval(freeBusyIntervalOfSingleEvent);
		freebusy.addFreeBusyInterval(freeBusyIntervalOfAllDayEvent);
		
		String resultFreebusy = ical4jHelper.parseFreeBusy(freebusy);
		
		assertThat(stripTimestamps(resultFreebusy)).isEqualTo(stripTimestamps(expectedFreebusy));
	}
}
