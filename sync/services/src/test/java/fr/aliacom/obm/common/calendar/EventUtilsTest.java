package fr.aliacom.obm.common.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;

public class EventUtilsTest {

	private Event getSimpleEvent() {
		Event ev = new Event();
		ev.setInternalEvent(true);
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		ev.setDate(cal.getTime());
		ev.setExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149");
		ev.setTitle("fake rdv");
		ev.setOwner("john@do.fr");
		ev.setDuration(3600);
		ev.setLocation("tlse");
		return ev;
	}

	@Test
	public void testNullEvent() {
		Assert.assertFalse(EventUtils.isInternalEvent(null));
	}

	@Test
	public void testAttendeeAsObmUser() {
		Event ev = getSimpleEvent();

		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		at.setObmUser(true);
		la.add(at);
		ev.setAttendees(la);

		Assert.assertTrue(EventUtils.isInternalEvent(ev));
	}

	@Test
	public void testAttendeeAsContact() {
		Event ev = getSimpleEvent();

		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		at.setObmUser(false);
		la.add(at);
		ev.setAttendees(la);

		Assert.assertFalse(EventUtils.isInternalEvent(ev));
	}

	@Test
	public void testWithoutOrganizerAndOneObmUser() {
		Event ev = getSimpleEvent();

		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(false);
		at.setObmUser(true);
		la.add(at);

		at = new Attendee();
		at.setDisplayName("obm TheUser");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		at.setOrganizer(false);
		at.setObmUser(false);
		la.add(at);
		ev.setAttendees(la);
		Assert.assertFalse(EventUtils.isInternalEvent(ev));
	}
	
	@Test
	public void testWithoutOrganizerAndTwoObmUser() {
		Event ev = getSimpleEvent();

		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(false);
		at.setObmUser(true);
		la.add(at);

		at = new Attendee();
		at.setDisplayName("obm TheUser");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		at.setOrganizer(false);
		at.setObmUser(true);
		la.add(at);
		ev.setAttendees(la);
		Assert.assertTrue(EventUtils.isInternalEvent(ev));
	}

}
