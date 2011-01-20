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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.fortuna.ical4j.data.CalendarBuilder;
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
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
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

	protected Event getTestEvent() {
		Event ev = new Event();

		Calendar cal = new GregorianCalendar();
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
		String ics = Ical4jHelper.parseEvents(l);
		assertNotNull(ics);
		assertNotSame("", ics);
	}

	@Test
	public void testGetRecur() {
		Calendar cal = new GregorianCalendar();
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

		Recur recur = Ical4jHelper.getRecur(event1.getRecurrence(), event1
				.getDate());
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

		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		DtStart dtStart = new DtStart(new DateTime(cal.getTime()));
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

		DtEnd dtEnd = new DtEnd(new DateTime(cal.getTime()));

		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		AccessToken token = new AccessToken(0, 0, null);
		token.setEmail("adrien@zz.com");
		Event event = Ical4jHelper.getEvent(vEvent);
		assertTrue(event.isAllday());

		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		dtEnd = new DtEnd(new DateTime(cal.getTime()));
		vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event1 = Ical4jHelper.getEvent(vEvent);
		assertFalse(event1.isAllday());
	}

	@Test
	public void testGetDuration() {

		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		DtStart dtStart = new DtStart(new DateTime(cal.getTime()));

		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
		DtEnd dtEnd = new DtEnd(new DateTime(cal.getTime()));

		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(dtStart);
		vEvent.getProperties().add(dtEnd);
		Event event = Ical4jHelper.getEvent(vEvent);
		assertEquals(172800, event.getDuration());

	}

	@Test
	public void testGetPrivacy() {
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PUBLIC);
		Event event = Ical4jHelper.getEvent(vEvent);
		assertEquals(0, event.getPrivacy());

		vEvent = new VEvent();
		vEvent.getProperties().add(Clazz.PRIVATE);
		Event event1 = Ical4jHelper.getEvent(vEvent);
		assertEquals(1, event1.getPrivacy());
	}

	@Test
	public void testGetOwner() {
		Organizer orga = new Organizer();
		try {
			orga.getParameters().add(new Cn("Adrien Poupard"));
			orga.setValue("mailto:" + "adrien@zz.com");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = Ical4jHelper.getEvent(vEvent);
		assertEquals("Adrien Poupard", event.getOwner());
	}

	@Test
	public void testGetOwner1() {
		Organizer orga = new Organizer();
		try {
			orga.setValue("mailto:" + "adrien@zz.com");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		VEvent vEvent = new VEvent();
		vEvent.getProperties().add(orga);
		Event event = Ical4jHelper.getEvent(vEvent);
		assertEquals("adrien@zz.com", event.getOwner());
	}

	@Test
	public void testGetAlert() {
		Dur dur = new Dur(0, 0, -30, 0);
		VAlarm va = new VAlarm(dur);
		Trigger ti = va.getTrigger();
		ti.getParameters().add(new Value("DURATION"));

		VEvent vEvent = new VEvent();
		vEvent.getAlarms().add(va);
		Event event = Ical4jHelper.getEvent(vEvent);
		assertEquals(new Integer(30 * 60), event.getAlert());
	}

	@Test
	public void testGetRecurence() {
		InputStream icsStream = getStreamICS("getRecurence.ics");
		CalendarBuilder builder = new CalendarBuilder();
		net.fortuna.ical4j.model.Calendar calendar = null;
		try {
			calendar = builder.build(icsStream);
			ComponentList vEvents = Ical4jHelper.getComponents(calendar, Component.VEVENT);
			VEvent vEvent = (VEvent) vEvents.get(0);
			Event event = Ical4jHelper.getEvent(vEvent);
			EventRecurrence er = event.getRecurrence();
			assertNotNull(er);
			assertEquals(1, er.getFrequence());
			assertEquals("0111110", er.getDays());
			assertEquals(RecurrenceKind.weekly, er.getKind());
			assertEquals(1, er.getExceptions().length);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetAttendees() {
		InputStream icsStream = getStreamICS("attendee.ics");
		CalendarBuilder builder = new CalendarBuilder();
		try {
			net.fortuna.ical4j.model.Calendar calendar = builder
					.build(icsStream);
			ComponentList vEvents = Ical4jHelper.getComponents(calendar,
					Component.VEVENT);
			VEvent vEvent = (VEvent) vEvents.get(0);
			Event event = Ical4jHelper.getEvent(vEvent);
			assertEquals(3, event.getAttendees().size());

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	public void testGetExDate() {
		Event event = new Event();
		Calendar cal = new GregorianCalendar();
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
		Event event = new Event();
		event.setAlert(30);
		VAlarm valarm = Ical4jHelper.getVAlarm(event.getAlert());
		assertEquals(30, valarm.getTrigger().getDuration().getSeconds());
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
		Organizer orga = Ical4jHelper.getOrganizer(event.getOwner(), event
				.getOwnerEmail());
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
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MILLISECOND, 0);
		event.setDate(cal.getTime());
		event.setDuration(3600);
		DtEnd dtend = Ical4jHelper.getDtEnd(event.getDate(), event
				.getDuration(), false);
		assertEquals(cal.getTime().getTime() + 3600000, dtend.getDate()
				.getTime());
	}

	@Test
	public void testGetDtStart() {
		Event event = new Event();
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MILLISECOND, 0);
		event.setDate(cal.getTime());
		DtStart dtstart = Ical4jHelper.getDtStart(event.getDate(), event
				.isAllday());
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
		Calendar cal = new GregorianCalendar();
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
	public void testParserAttendee() throws IOException {
		String ics = IOUtils.toString(getStreamICS("bugGn.ics"));
		try {
			AccessToken token = new AccessToken(0, 0, null);
			List<Event> event = Ical4jHelper.parseICSEvent(ics, token);
			assertEquals(event.size(), 1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	public InputStream getStreamICS(String filename) {
		InputStream in = ClassLoader.getSystemClassLoader()
				.getResourceAsStream("icsFile/" + filename);
		if (in == null) {
			fail("Cannot load " + filename);
		}
		return in;
	}

}
