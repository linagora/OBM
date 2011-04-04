package fr.aliacom.obm.utils;

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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

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
import org.junit.Ignore;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

public class Ical4jHelperTest {

	private Calendar getCalendar() {
		return new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	}
	
	protected AccessToken getMockAccessToken() {
		AccessToken at = new AccessToken(1, 1, "unitTest");
		at.setDomain("test.tlse.lng");
		return at;
	}

	protected Event getTestEvent() {
		Event ev = new Event();

		Calendar cal = getCalendar();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ev.setDate(cal.getTime());

		ev.setExtId(UUID.randomUUID().toString());

		ev.setTitle("fake rdv " + System.currentTimeMillis());
		ev.setOwner("adrien");
		ev.setDuration(3600);
		ev.setLocation("tlse");

		List<Attendee> la = new LinkedList<Attendee>();

		Attendee at = new Attendee();
		at.setDisplayName("adrien@zz.com");
		at.setEmail("adrien@zz.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		la.add(at);

		at = new Attendee();
		at.setDisplayName("noIn TheDatabase");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.OPT);
		la.add(at);

		at = new Attendee();
		at.setDisplayName("pouic");
		at.setEmail("pouic@zz.com");
		at.setState(ParticipationState.INPROGRESS);
		at.setRequired(ParticipationRole.OPT);
		la.add(at);

		ev.setAttendees(la);
		ev.setAlert(60);

		EventRecurrence er = new EventRecurrence();
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
		String ics = Ical4jHelper.parseEvents(getMockAccessToken(), l);
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

		Recur recur = Ical4jHelper.getRecur(event1.getRecurrence(),
				event1.getDate());
		assertTrue(recur.getDayList().contains(WeekDay.MO));
		assertTrue(recur.getDayList().contains(WeekDay.TU));
		assertTrue(recur.getDayList().contains(WeekDay.WE));
		assertTrue(recur.getDayList().contains(WeekDay.TH));
		assertTrue(recur.getDayList().contains(WeekDay.FR));

		assertNull(er.getEnd());

		assertEquals(er.getFrequence(), 1);
		er.setKind(RecurrenceKind.weekly);

		Date[] ldt = er.getExceptions();
		assertEquals(ldt.length, 2);
	}

	@Test
	public void testGetListDay() {
		EventRecurrence er = new EventRecurrence();
		er.setDays("1010101");
		Set<WeekDay> swd = Ical4jHelper.getListDay(er);
		assertTrue(swd.contains(WeekDay.SU));
		assertTrue(swd.contains(WeekDay.TU));
		assertTrue(swd.contains(WeekDay.TH));
		assertTrue(swd.contains(WeekDay.SA));
		Ical4jHelper.getListDay(er);
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
		AccessToken token = new AccessToken(0, 0, null);
		token.setEmail("adrien@zz.com");
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertTrue(event.isAllday());

		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		dtStart = new DtStart(new DateTime(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		dtEnd = new DtEnd(new DateTime(cal.getTime()));
		vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event1 = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertEquals(172800, event.getDuration());

	}

	@Test
	public void testGetPrivacy() {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PUBLIC);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertEquals(0, event.getPrivacy());

		vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PRIVATE);
		Event event1 = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertEquals(1, event1.getPrivacy());
	}

	@Test
	public void testGetOwner() throws URISyntaxException {
		Organizer orga = new Organizer();
		orga.getParameters().add(new Cn("Adrien Poupard"));
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertEquals("Adrien Poupard", event.getOwner());
	}

	@Test
	public void testGetOwner1() throws URISyntaxException {
		Organizer orga = new Organizer();
		orga.setValue("mailto:" + "adrien@zz.com");
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertFalse(
				new Integer(-1).equals(event.getAlert())
		);
	}

	@Test
	public void testGetRecurence() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("getRecurence.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		EventRecurrence er = event.getRecurrence();
		assertNotNull(er);
		assertEquals(1, er.getFrequence());
		assertEquals("0111110", er.getDays());
		assertEquals(RecurrenceKind.weekly, er.getKind());
		assertEquals(1, er.getExceptions().length);
	}

	
	@Test
	public void testGetAttendees() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("attendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertEquals(3, event.getAttendees().size());
	}


	@Test
	@SuppressWarnings("null")
	public void testOrganizerInAttendess() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("organizerInAttendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertTrue(event.isInternalEvent());

	}

	@Test
	public void testCreated() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertEquals(1244470973000L, event.getTimeCreate().getTime());
	}

	@Test
	public void testLastModified() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertEquals(1244470995000L, event.getTimeUpdate().getTime());

	}

	@Test
	public void testLastModifiedNull() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventNewComplet.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertNull(event.getTimeUpdate());

	}

	@Test
	public void testIsExternalObm() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventExternalObm.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
		assertFalse(event.isInternalEvent());

	}

	@Test
	public void testIsExternal() throws IOException, ParserException {
		InputStream icsStream = getStreamICS("eventExternal.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = builder.build(icsStream);
		ComponentList vEvents = Ical4jHelper.getComponents(calendar,
				Component.VEVENT);
		VEvent vEvent = (VEvent) vEvents.get(0);
		Event event = Ical4jHelper.getEvent(getMockAccessToken(), vEvent);
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

		Date[] except = new Date[2];
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		except[0] = cal.getTime();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		except[1] = cal.getTime();
		er.setExceptions(except);
		event.setRecurrence(er);

		Set<Property> ret = Ical4jHelper.getExDate(event);
		assertEquals(2, ret.size());

	}

	@Test
	public void testGetVAlarm() {
		final Event event = new Event();
		event.setAlert(30);
		final VAlarm valarm = Ical4jHelper.getVAlarm(event.getAlert());
		assertEquals(30, valarm.getTrigger().getDuration().getSeconds());
	}

	@Test
	public void testAppendAllDayWithDateTimeObject() throws ParseException {
		final Event event = new Event();
		final DtStart startDate = new DtStart();
		final DateProperty endDate = new DtEnd();
		
		startDate.setValue("19980118T230000");
		endDate.setValue("19980118T230000");
		
		Ical4jHelper.appendAllDay(event, startDate, endDate);
		assertFalse(event.isAllday());
	}
	
	@Test
	public void testAppendAllDayWithDateObject() {
		final Event event = new Event();
		final DtStart startDate = new DtStart();
		final DateProperty endDate = new DtEnd();
		
		startDate.setDate(new net.fortuna.ical4j.model.Date());
		endDate.setDate(new net.fortuna.ical4j.model.Date());
		
		Ical4jHelper.appendAllDay(event, startDate, endDate);
		assertTrue(event.isAllday());
	}	
	
	@Test
	public void testGetClazz() {
		Event event = new Event();
		event.setPrivacy(0);
		Clazz clazz = Ical4jHelper.getClazz(event.getPrivacy());
		assertEquals("PUBLIC", clazz.getValue());

		event.setPrivacy(1);
		clazz = Ical4jHelper.getClazz(event.getPrivacy());
		assertEquals("PRIVATE", clazz.getValue());
	}

	@Test
	public void testGetOrganizer() {
		Event event = new Event();
		event.setOwner("Adrien Poupard");
		event.setOwnerEmail("adrien@zz.com");
		Organizer orga = Ical4jHelper.getOrganizer(event.getOwner(),
				event.getOwnerEmail());
		assertEquals("mailto:adrien@zz.com", orga.getValue());
		assertEquals("Adrien Poupard", orga.getParameter(Parameter.CN)
				.getValue());

	}

	@Test
	public void testGetTransp() {
		Event event = new Event();
		event.setOpacity(EventOpacity.OPAQUE);
		Transp t = Ical4jHelper.getTransp(event.getOpacity());
		assertEquals(Transp.OPAQUE, t);

		event.setOpacity(EventOpacity.TRANSPARENT);
		t = Ical4jHelper.getTransp(event.getOpacity());
		assertEquals(Transp.TRANSPARENT, t);

	}

	@Test
	public void testGetDtEnd() {
		Event event = new Event();
		Calendar cal = getCalendar();
		cal.set(Calendar.MILLISECOND, 0);
		event.setDate(cal.getTime());
		event.setDuration(3600);
		DtEnd dtend = Ical4jHelper.getDtEnd(event.getDate(),
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
		DtStart dtstart = Ical4jHelper.getDtStart(event.getDate(),
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

		Role role = Ical4jHelper.getRole(at);
		assertEquals(role, Role.CHAIR);
	}

	@Test
	public void testGetCn() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");

		Cn cn = Ical4jHelper.getCn(at);
		assertEquals("adrien", cn.getValue());
	}

	@Test
	public void testGetPartStat() {
		Attendee at = new Attendee();
		at.setDisplayName("adrien");
		at.setEmail("adrien@zz.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.CHAIR);

		PartStat ps = Ical4jHelper.getPartStat(at);
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

		RRule rule = Ical4jHelper.getRRule(event);
		Recur recur = rule.getRecur();
		assertTrue(recur.getDayList().contains(WeekDay.MO));
		assertTrue(recur.getDayList().contains(WeekDay.TU));
		assertTrue(recur.getDayList().contains(WeekDay.WE));
		assertTrue(recur.getDayList().contains(WeekDay.TH));
		assertTrue(recur.getDayList().contains(WeekDay.FR));

		assertNull(er.getEnd());

		assertEquals(er.getFrequence(), 1);
		er.setKind(RecurrenceKind.weekly);

		Date[] ldt = er.getExceptions();
		assertEquals(ldt.length, 2);

	}

	@Test
	public void testParserAttendee() throws IOException, ParserException {
		String ics = IOUtils.toString(getStreamICS("bugGn.ics"));
		AccessToken token = new AccessToken(0, 0, null);
		List<Event> event = Ical4jHelper.parseICSEvent(ics, token);
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
		final AccessToken token = new AccessToken(0, 0, null);
		
		final List<Event> events = Ical4jHelper.parseICSEvent(ics, token);
		for (final Event event: events) {
			assertNotNull(event.getTitle());
		}
		assertEquals(221, events.size());
	}
	
	@Test
	public void testParsingICSFiles() throws IOException, ParserException {
		final String[] icsFiles = {"cdespino.ics", "dkaplan.ics"};
		
		final AccessToken token = new AccessToken(0, 0, null);
		for (String icsFile: icsFiles) {
			final String ics = IOUtils.toString(getStreamICS(icsFile));
			Ical4jHelper.parseICSEvent(ics, token);	
		}
		assertTrue(true);
	}

}
