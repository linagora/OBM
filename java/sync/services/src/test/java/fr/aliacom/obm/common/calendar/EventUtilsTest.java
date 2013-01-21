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
package fr.aliacom.obm.common.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.UserAttendee;

@RunWith(SlowFilterRunner.class)
public class EventUtilsTest {

	private Event getSimpleEvent() {
		Event ev = new Event();
		ev.setInternalEvent(true);
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		ev.setStartDate(cal.getTime());
		ev.setExtId(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149"));
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
		Attendee at = UserAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build();
		
		la.add(at);
		ev.setAttendees(la);

		Assert.assertTrue(EventUtils.isInternalEvent(ev));
	}

	@Test
	public void testAttendeeAsContact() {
		Event ev = getSimpleEvent();

		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = ContactAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build();
		
		la.add(at);
		ev.setAttendees(la);

		Assert.assertFalse(EventUtils.isInternalEvent(ev));
	}

	@Test
	public void testWithoutOrganizerAndOneObmUser() {
		Event ev = getSimpleEvent();

		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = UserAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asAttendee()
				.build();
		
		la.add(at);

		at = ContactAttendee
				.builder()
				.displayName("obm TheUser")
				.email("notin@mydb.com")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asAttendee()
				.build();
		
		la.add(at);
		ev.setAttendees(la);
		Assert.assertFalse(EventUtils.isInternalEvent(ev));
	}
	
	@Test
	public void testWithoutOrganizerAndTwoObmUser() {
		Event ev = getSimpleEvent();

		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = UserAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build();
		
		la.add(at);

		at = UserAttendee
				.builder()
				.displayName("obm TheUser")
				.email("notin@mydb.com")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asAttendee()
				.build();

		la.add(at);
		ev.setAttendees(la);
		
		Assert.assertTrue(EventUtils.isInternalEvent(ev));
	}

}
