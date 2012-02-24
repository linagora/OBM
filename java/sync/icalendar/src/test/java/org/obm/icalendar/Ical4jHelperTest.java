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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import net.fortuna.ical4j.model.Property;
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
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.io.IOUtils;
import org.fest.assertions.Assertions;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.obm.push.utils.UserEmailParserUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

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
	
	@BeforeClass
	public static void setUpOnce() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	}

	private Ical4jHelper getIcal4jHelper(){
		return new Ical4jHelper();
	}

	private Calendar getCalendar() {
		return new GregorianCalendar();
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

		final Calendar cal = getCalendar();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	
		ev.setAlert(60);

		final EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.monthlybydate);
		er.setFrequence(1);
		er.addException(ev.getDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);

		ev.setRecurrence(er);
		return ev;
	}

	@Test
	public void testParseEvents() {
		Event event1 = getTestEvent();
		Event event2 = getTestEvent();
		List<Event> l = new LinkedList<Event>();
		l.add(event1);
		l.add(event2);
		String ics = getIcal4jHelper().parseEvents(getDefaultObmUser(), l);
		assertNotNull(ics);
		assertNotSame("", ics);
	}

	@Test
	public void testGetRecur() {
		Calendar cal = getCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Event event1 = new Event();

		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.weekly);
		er.setFrequence(1);
		er.addException(cal.getTime());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());

		er.setDays("0111110");
		er.setEnd(null);
		event1.setRecurrence(er);

		Recur recur = getIcal4jHelper().getRecur(event1.getRecurrence(),
				event1.getDate());
		assertTrue(recur.getDayList().contains(WeekDay.MO));
		assertTrue(recur.getDayList().contains(WeekDay.TU));
		assertTrue(recur.getDayList().contains(WeekDay.WE));
		assertTrue(recur.getDayList().contains(WeekDay.TH));
		assertTrue(recur.getDayList().contains(WeekDay.FR));

		assertNull(er.getEnd());

		assertEquals(er.getFrequence(), 1);
		er.setKind(RecurrenceKind.weekly);

		Assertions.assertThat(er.getExceptions()).hasSize(2);
	}

	@Test
	public void testGetListDay() {
		EventRecurrence er = new EventRecurrence();
		er.setDays("1010101");
		Set<WeekDay> swd = getIcal4jHelper().getListDay(er);
		assertTrue(swd.contains(WeekDay.SU));
		assertTrue(swd.contains(WeekDay.TU));
		assertTrue(swd.contains(WeekDay.TH));
		assertTrue(swd.contains(WeekDay.SA));
		getIcal4jHelper().getListDay(er);
	}

	@Ignore
	@Test
	public void testGetIsAllDay() {

		Calendar cal = getCalendar();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		DtStart dtStart = new DtStart(new net.fortuna.ical4j.model.Date(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

		DtEnd dtEnd = new DtEnd(new net.fortuna.ical4j.model.Date(cal.getTime()));

		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		AccessToken token = new AccessToken(0, null);
		token.setUserEmail("adrien@zz.com");
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertTrue(event.isAllday());

		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		dtStart = new DtStart(new DateTime(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		dtEnd = new DtEnd(new DateTime(cal.getTime()));
		vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event1 = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(event1.isAllday());
	}

	@Test
	public void testGetDuration() {

		Calendar cal = getCalendar();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		DtStart dtStart = new DtStart(new DateTime(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		DtEnd dtEnd = new DtEnd(new DateTime(cal.getTime()));

		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(172800, event.getDuration());

	}

	@Test
	public void testGetPrivacy() {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PUBLIC);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(0, event.getPrivacy());

		vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PRIVATE);
		Event event1 = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(1, event1.getPrivacy());
	}

	@Test
	public void testGetOwner() throws URISyntaxException {
		Organizer orga = new Organizer();
		orga.getParameters().add(new Cn("Adrien Poupard"));
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals("Adrien Poupard", event.getOwner());
	}

	@Test
	public void testGetOwner1() throws URISyntaxException {
		Organizer orga = new Organizer();
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
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
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(new Integer(-1), event.getAlert());
	}
	
	@Test
	public void testGetAlertWithoutRepeatAndWithoutDuration() {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
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
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(new Integer(-1), event.getAlert());
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
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(
				new Integer(-1).equals(event.getAlert())
		);
	}

	@Test
	public void testGetRecurence() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("getRecurence.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		EventRecurrence er = event.getRecurrence();
		assertNotNull(er);
		assertEquals(1, er.getFrequence());
		assertEquals("0111110", er.getDays());
		assertEquals(RecurrenceKind.weekly, er.getKind());
		Assertions.assertThat(er.getExceptions()).hasSize(1);
	}

	
	@Test
	public void testGetAttendees() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("attendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(3, event.getAttendees().size());
	}


	@Test
	@SuppressWarnings("null")
	public void testOrganizerInAttendess() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("organizerInAttendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
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
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
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
	public void testIsInternal() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventInternal.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertTrue(event.isInternalEvent());

	}

	@Test
	public void testCreated() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(1244470973000L, event.getTimeCreate().getTime());
	}

	@Test
	public void testLastModified() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertEquals(1244470995000L, event.getTimeUpdate().getTime());

	}

	@Test
	public void testLastModifiedNull() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventNewComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertNull(event.getTimeUpdate());

	}

	@Test
	public void testIsExternalObm() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventExternalObm.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(event.isInternalEvent());

	}

	@Test
	public void testIsExternal() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventExternal.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = getIcal4jHelper().getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = getIcal4jHelper().convertVEventToEvent(getDefaultObmUser(), vEvent);
		assertFalse(event.isInternalEvent());

	}

	@Test
	public void testGetExDate() {
		Event event = new Event();
		Calendar cal = getCalendar();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		event.setDate(cal.getTime());

		EventRecurrence er = new EventRecurrence();
		er.setDays("1111111");
		er.setKind(RecurrenceKind.daily);

		Date[] except = new Date[2];
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		except[0] = cal.getTime();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		except[1] = cal.getTime();
		er.setExceptions(Arrays.asList(except));
		event.setRecurrence(er);

		Set<Property> ret = getIcal4jHelper().getExDate(event);
		assertEquals(2, ret.size());

	}

	@Test
	public void testGetVAlarm() {
		final Event event = new Event();
		event.setAlert(30);
		final VAlarm valarm = getIcal4jHelper().getVAlarm(event.getAlert());
		assertEquals(30, valarm.getTrigger().getDuration().getSeconds());
	}

	@Test
	public void testAppendAllDayWithDateTimeObject() throws ParseException {
		final Event event = new Event();
		final DtStart startDate = new DtStart();
		final DateProperty endDate = new DtEnd();
		
		startDate.setValue("19980118T230000");
		endDate.setValue("19980118T230000");
		
		getIcal4jHelper().appendAllDay(event, startDate, endDate);
		assertFalse(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDateObject() {
		final Event event = new Event();
		final DtStart startDate = new DtStart();
		final DateProperty endDate = new DtEnd();
		
		startDate.setDate(new net.fortuna.ical4j.model.Date());
		endDate.setDate(new net.fortuna.ical4j.model.Date());
		
		getIcal4jHelper().appendAllDay(event, startDate, endDate);
		assertTrue(event.isAllday());
	}	
	
	@Test
	public void testGetClazz() {
		Event event = new Event();
		event.setPrivacy(0);
		Clazz clazz = getIcal4jHelper().getClazz(event.getPrivacy());
		assertEquals("PUBLIC", clazz.getValue());

		event.setPrivacy(1);
		clazz = getIcal4jHelper().getClazz(event.getPrivacy());
		assertEquals("PRIVATE", clazz.getValue());
	}

	@Test
	public void testGetOrganizer() {
		Event event = new Event();
		event.setOwner("Adrien Poupard");
		event.setOwnerEmail("adrien@zz.com");
		Organizer orga = getIcal4jHelper().getOrganizer(event.getOwner(),
				event.getOwnerEmail());
		assertEquals("mailto:adrien@zz.com", orga.getValue());
		assertEquals("Adrien Poupard", orga.getParameter(Parameter.CN)
				.getValue());

	}

	@Test
	public void testGetTransp() {
		Event event = new Event();
		event.setOpacity(EventOpacity.OPAQUE);
		Transp t = getIcal4jHelper().getTransp(event.getOpacity());
		assertEquals(Transp.OPAQUE, t);

		event.setOpacity(EventOpacity.TRANSPARENT);
		t = getIcal4jHelper().getTransp(event.getOpacity());
		assertEquals(Transp.TRANSPARENT, t);

	}

	@Test
	public void testGetDtEnd() {
		Event event = new Event();
		Calendar cal = getCalendar();
		cal.set(Calendar.MILLISECOND, 0);
		event.setDate(cal.getTime());
		event.setDuration(3600);
		DtEnd dtend = getIcal4jHelper().getDtEnd(event.getDate(),
				event.getDuration(), false);
		assertEquals(cal.getTime().getTime() + 3600000, dtend.getDate()
				.getTime());
	}

	@Test
	public void testGetDtStart() {
		Event event = new Event();
		Calendar cal = getCalendar();
		cal.set(Calendar.MILLISECOND, 0);
		event.setDate(cal.getTime());
		DtStart dtstart = getIcal4jHelper().getDtStart(event.getDate(),
				event.isAllday());
		assertEquals(cal.getTime().getTime(), dtstart.getDate().getTime());
	}

	@Test
	public void testGetRole() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");
		at.setEmail("adrien@zz.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.CHAIR);

		Role role = getIcal4jHelper().getRole(at);
		assertEquals(role, Role.CHAIR);
	}

	@Test
	public void testGetCn() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");

		Cn cn = getIcal4jHelper().getCn(at);
		assertEquals("adrien", cn.getValue());
	}

	@Test
	public void testGetPartStat() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");
		at.setEmail("adrien@zz.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.CHAIR);

		PartStat ps = getIcal4jHelper().getPartStat(at);
		assertEquals(ps, PartStat.ACCEPTED);
	}

	@Test
	public void testGetRRule() {
		Calendar cal = getCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Event event = new Event();

		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.weekly);
		er.setFrequence(1);
		er.addException(cal.getTime());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());

		er.setDays("0111110");
		er.setEnd(null);
		event.setRecurrence(er);

		RRule rule = getIcal4jHelper().getRRule(event);
		Recur recur = rule.getRecur();
		assertTrue(recur.getDayList().contains(WeekDay.MO));
		assertTrue(recur.getDayList().contains(WeekDay.TU));
		assertTrue(recur.getDayList().contains(WeekDay.WE));
		assertTrue(recur.getDayList().contains(WeekDay.TH));
		assertTrue(recur.getDayList().contains(WeekDay.FR));

		assertNull(er.getEnd());

		assertEquals(er.getFrequence(), 1);
		er.setKind(RecurrenceKind.weekly);

		Assertions.assertThat(er.getExceptions()).hasSize(2);
	}

	@Test
	public void testParserAttendee() throws IOException, ParserException {
		String ics = IOUtils.toString(getStreamICS("bugGn.ics"));
		List<Event> event = getIcal4jHelper().parseICSEvent(ics, getDefaultObmUser());
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
	
	@Test
	public void testParsingICSFileOf200kio() throws IOException, ParserException {
		final String ics = IOUtils.toString(getStreamICS("bellemin-calendrierobm.ics"));
		
		final List<Event> events = getIcal4jHelper().parseICSEvent(ics, getDefaultObmUser());
		for (final Event event: events) {
			assertNotNull(event.getTitle());
		}
		assertEquals(221, events.size());
	}
	
	@Test
	public void testParsingICSFiles() throws IOException, ParserException {
		final String[] icsFiles = {"cdespino.ics", "dkaplan.ics"};
		
		for (String icsFile: icsFiles) {
			final String ics = IOUtils.toString(getStreamICS(icsFile));
			getIcal4jHelper().parseICSEvent(ics, getDefaultObmUser());	
		}
		assertTrue(true);
	}

	@Test
	public void testBuildIcsInvitationReply() {
		final Event event = buildEvent();
		final Attendee attendeeReply = event.getAttendees().get(2);

		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);

		Ical4jHelper ical4jHelper = new Ical4jHelper();
		final String ics = ical4jHelper.buildIcsInvitationReply(event, ical4jUser);
		
		String icsAttendee = "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;RSVP=TRUE;" +
		"CN=OBM USER 3;ROLE=\r\n REQ-PARTICIPANT:mailto:obm3@obm.org";
		
		Assert.assertThat(ics, StringContains.containsString(icsAttendee));
		Assert.assertEquals(1, countStringOccurrences(ics, "ATTENDEE;"));	
	}
	
	private Event buildEvent() {
		final Event event = new Event();
		event.setDate(new Date());

		event.setExtId(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262eza9"));
		event.setTitle("rdv " + System.currentTimeMillis());
		event.setOwner("obm");
		event.setDuration(3600);
		event.setLocation("obm loca");
		
		final Attendee at1 = new Attendee();
		at1.setDisplayName("OBM ORGANIZER");
		at1.setEmail("obm@obm.org");
		at1.setState(ParticipationState.ACCEPTED);
		at1.setRequired(ParticipationRole.CHAIR);
		at1.setOrganizer(true);
		
		final Attendee at2 = new Attendee();
		at2.setDisplayName("OBM USER 2");
		at2.setEmail("obm2@obm.org");
		at2.setState(ParticipationState.ACCEPTED);
		at2.setRequired(ParticipationRole.REQ);
		
		final Attendee at3 = new Attendee();
		at3.setDisplayName("OBM USER 3");
		at3.setEmail("obm3@obm.org");
		at3.setState(ParticipationState.ACCEPTED);
		at3.setRequired(ParticipationRole.REQ);
		
		final Attendee at4 = new Attendee();
		at4.setDisplayName("OBM USER 4");
		at4.setEmail("obm4@obm.org");
		at4.setState(ParticipationState.DECLINED);
		at4.setRequired(ParticipationRole.REQ);
		
		event.addAttendee(at1);
		event.addAttendee(at2);
		event.addAttendee(at3);
		event.addAttendee(at4);
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

		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);

		String icsRequest = new Ical4jHelper().buildIcsInvitationRequest(ical4jUser, event);
		String icsCancel = new Ical4jHelper().buildIcsInvitationCancel(ical4jUser, event);
		String icsReply = new Ical4jHelper().buildIcsInvitationReply(event, ical4jUser);
		
		checkStringLengthLessThan(icsRequest, 75);
		checkStringLengthLessThan(icsCancel, 75);
		checkStringLengthLessThan(icsReply, 75);
	}
	
	@Test
	public void testXObmDomainInInvitation() {
		Event event = buildEvent();

		final Attendee attendeeReply = event.getAttendees().get(2);
		final Ical4jUser ical4jUser = buildObmUser(attendeeReply);

		String icsRequest = new Ical4jHelper().buildIcsInvitationRequest(ical4jUser, event);
		String icsCancel = new Ical4jHelper().buildIcsInvitationCancel(ical4jUser, event);
		String icsReply = new Ical4jHelper().buildIcsInvitationReply(event, ical4jUser);
		
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

		String icsRequest = new Ical4jHelper().buildIcsInvitationRequest(ical4jUser, event);
		String icsCancel = new Ical4jHelper().buildIcsInvitationCancel(ical4jUser, event);
		String icsReply = new Ical4jHelper().buildIcsInvitationReply(event, ical4jUser);
		
		checkContainIcsProperty(icsRequest, "X-OBM-DOMAIN-UUID", ical4jUser.getObmDomain().getUuid());
		checkContainIcsProperty(icsCancel, "X-OBM-DOMAIN-UUID", ical4jUser.getObmDomain().getUuid());
		checkContainIcsProperty(icsReply, "X-OBM-DOMAIN-UUID", ical4jUser.getObmDomain().getUuid());
	}
	
	private void checkStringLengthLessThan(String ics, int length) {
		Iterable<String> lines = Splitter.on("\r\n").split(ics);
		for (String line: lines) {
			Assert.assertThat(line, new StringLengthLessThan(length));
		}
	}
	
	@Test
	public void executeParsingTestLotusNotesICS() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2891.ics";
		List<Event> events = testIcsParsing(icsFilename);
		Assertions.assertThat(events).isNotNull().isNotEmpty();
	}

	private List<Event> testIcsParsing(String icsFilename) throws IOException,
			ParserException {
		InputStream stream = getStreamICS(icsFilename);
		String ics = IOUtils.toString(stream);
		return getIcal4jHelper().parseICSEvent(ics, getDefaultObmUser());
	}
	
	@Test
	public void executeJIRA2940() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2940.ics";
		List<Event> events = testIcsParsing(icsFilename);
		Assertions.assertThat(events).isNotNull();
	}
	
	@Test
	public void testDefaultParticipation() throws IOException, ParserException {
		String icsFilename = "default-part-stat.ics";
		List<Event> events = testIcsParsing(icsFilename);
		Event event = Iterables.getOnlyElement(events);
		Assertions.assertThat(event.getAttendees()).hasSize(2);
		Attendee userc = event.getAttendees().get(1);
		Assertions.assertThat(userc.getState()).isNotNull().isEqualTo(ParticipationState.NEEDSACTION);
	}

	@Test
	public void testImportICSWithRecurrenceIdAfterParentEventDefinition() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963sorted.ics";
		List<Event> events = testIcsParsing(icsFilename);

		Assertions.assertThat(events).hasSize(2);

		Event firstParentEvent = events.get(0);
		Event secondParentEvent = events.get(1);
		Assertions.assertThat(firstParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
		Assertions.assertThat(secondParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
	}

	@Test
	public void testImportICSWithRecurrenceIdBeforeParentEventDefinition() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963unsorted.ics";
		List<Event> events = testIcsParsing(icsFilename);

		Assertions.assertThat(events).hasSize(2);

		Event firstParentEvent = events.get(0);
		Event secondParentEvent = events.get(1);
		Assertions.assertThat(firstParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
		Assertions.assertThat(secondParentEvent.getRecurrence().getEventExceptions()).hasSize(2);
	}

	@Test
	public void testImportICSWithOnlyRecurrenceId() throws IOException, ParserException {
		String icsFilename = "OBMFULL-2963onlyRecurrenceId.ics";
		List<Event> events = testIcsParsing(icsFilename);

		Assertions.assertThat(events).isEmpty();
	}
}
