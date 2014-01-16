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
package org.obm.sync.client.calendar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortuna.ical4j.data.ParserException;

import org.obm.sync.ObmSyncTestCase;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.CalendarItemsWriter;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyInterval;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

public class CalendarClientTests extends ObmSyncTestCase {

	protected CalendarClient cal;
	protected AccessToken token;

	protected Event getTestEvent() {
		Event ev = new Event();

		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		ev.setDate(cal.getTime());

		ev.setExtId(UUID.randomUUID().toString());

		ev.setTitle("fake rdv " + System.currentTimeMillis());
		ev.setOwner(p("calendar"));
		ev.setDuration(3600);
		ev.setLocation("tlse");

		List<Attendee> la = new LinkedList<Attendee>();

		Attendee at = new Attendee();
		at.setDisplayName(p("login"));
		at.setEmail(p("login"));
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		la.add(at);

		at = new Attendee();
		at.setDisplayName("noIn TheDatabase");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		la.add(at);

		ev.setAttendees(la);
		ev.setAlert(60);

		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.daily);
		er.setFrequence(1);
		er.addException(ev.getDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);

		ev.setRecurrence(er);
		return ev;
	}

	public void testDoSyncPerf() {
		testCreateEvent();

		try {
			EventChanges changes = cal.getSync(token, p("calendar"), null);
			assertNotNull(changes);
			assertNotNull(changes.getLastSync());
			assertNotNull(changes.getRemoved());
			assertNotNull(changes.getUpdated());

			assertTrue(changes.getUpdated().length > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception on getSync");
		}

		System.out.println("Start getSync() speed test...");
		int count = 1000;
		try {
			long time = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				EventChanges changes = cal.getSync(token, p("calendar"), null);
				assertNotNull(changes);
			}
			time = System.currentTimeMillis() - time;
			System.out.println(count + " getSync() calls took " + time
					+ "ms. Performing at " + (count * 1000) / time + "/sec");
		} catch (Exception e) {
			e.printStackTrace();
			fail("speed test failed");
		}

	}

	public void testDoSync() {
		try {
			EventChanges changes = cal.getSync(token, p("calendar"), null);
			assertNotNull(changes);
			assertNotNull(changes.getLastSync());
			assertNotNull(changes.getRemoved());
			assertNotNull(changes.getUpdated());

			assertTrue(changes.getUpdated().length >= 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception on getSync");
		}
	}

	public void testCreateEventSameAttendee() {
		try {
			Event e = getTestEvent();

			Attendee at = new Attendee();
			at.setDisplayName(p("login"));
			at.setEmail(p("login"));
			at.setState(ParticipationState.ACCEPTED);
			at.setRequired(ParticipationRole.OPT);
			e.getAttendees().add(at);

			at = new Attendee();
			at.setDisplayName(p("login"));
			at.setEmail(p("login"));
			at.setState(ParticipationState.ACCEPTED);
			at.setRequired(ParticipationRole.OPT);
			e.getAttendees().add(at);

			String ret = cal.createEvent(token, p("calendar"), e);

			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);
		} catch (Exception e) {
			e.printStackTrace();

			fail("error on createEvent");
		}
	}

	public void testDoSyncEventDate() {
		try {
			// simple event must be sync
			Event event1 = getTestEvent();
			event1.setRecurrence(null);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
			event1.setDate(calendar.getTime());
			String ret1 = cal.createEvent(token, p("calendar"), event1);
			assertNotNull(ret1);

			// recur event must be sync
			Event event2 = getTestEvent();
			EventRecurrence er = new EventRecurrence();
			er.setKind(RecurrenceKind.daily);
			er.setFrequence(1);
			er.setEnd(null);
			event2.setRecurrence(er);
			event2.setDate(new Date(0));
			String ret2 = cal.createEvent(token, p("calendar"), event2);
			assertNotNull(ret2);

			// simple event in past. must not be sync
			Event event3 = getTestEvent();
			event3.setRecurrence(null);
			event3.setDate(new Date(0));
			String ret3 = cal.createEvent(token, p("calendar"), event3);
			assertNotNull(ret3);

			EventChanges changesSync = cal.getSync(token, p("calendar"),
					new Date());
			assertNotNull(changesSync);
			assertNotNull(changesSync.getLastSync());
			assertNotNull(changesSync.getRemoved());
			assertNotNull(changesSync.getUpdated());
			boolean find1 = false;
			boolean find2 = false;
			boolean find3 = false;
			for (Event e : changesSync.getUpdated()) {
				if (ret1.equals(e.getUid())) {
					find1 = true;
				} else if (ret2.equals(e.getUid())) {
					find2 = true;
				} else if (ret3.equals(e.getUid())) {
					find3 = true;
				}
			}
			assertFalse(find1);
			assertFalse(find2);
			assertFalse(find3);

			EventChanges changesSyncEventDate = cal.getSyncEventDate(token,
					p("calendar"), new Date());
			assertNotNull(changesSyncEventDate);
			assertNotNull(changesSyncEventDate.getLastSync());
			assertNotNull(changesSyncEventDate.getRemoved());
			assertNotNull(changesSyncEventDate.getUpdated());
			assertTrue(changesSyncEventDate.getUpdated().length > 0);
			find1 = false;
			find2 = false;
			find3 = false;
			for (Event e : changesSyncEventDate.getUpdated()) {
				if (ret1.equals(e.getUid())) {
					find1 = true;
				} else if (ret2.equals(e.getUid())) {
					find2 = true;
				} else if (ret3.equals(e.getUid())) {
					find3 = true;
				}
			}
			assertTrue(find1);
			assertTrue(find2);
			assertFalse(find3);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception on getSync");
		}
	}

	public void testDoSyncEventException() {
		try {

			CalendarItemsWriter ciw = new CalendarItemsWriter();
			// Event 1
			Event event1 = getTestEvent();

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);

			Calendar calExce = Calendar.getInstance();
			calExce.setTime(event1.getDate());
			calExce.set(Calendar.MONTH, calExce.get(Calendar.MONTH) + 2);
			event2.setRecurrenceId(calExce.getTime());

			event1.getRecurrence().addEventException(event2);

			String ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			EventChanges changes = cal.getSync(token, p("calendar"), null);
			assertNotNull(changes);
			assertNotNull(changes.getUpdated());
			assertTrue(changes.getUpdated().length > 0);
			boolean find = false;
			for (Event e : changes.getUpdated()) {
				if (e.getUid().equals(ev1)) {
					find = true;
				}
			}
			assertTrue(find);

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testDoSyncDeleteEventException() {
		try {
			// Date d = new Date();

			CalendarItemsWriter ciw = new CalendarItemsWriter();
			// Event 1
			Event event1 = getTestEvent();
			event1.getRecurrence().getListExceptions().clear();

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			Calendar calExce = Calendar.getInstance();
			calExce.setTime(event1.getDate());
			calExce.set(Calendar.MONTH, calExce.get(Calendar.MONTH) + 2);
			event2.setRecurrenceId(calExce.getTime());
			event1.getRecurrence().addEventException(event2);

			String ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			event1.setUid(ev1);
			event1.getRecurrence().setEventExceptions(new ArrayList<Event>());
			event1.getRecurrence().addException(calExce.getTime());
			event1 = cal.modifyEvent(token, p("calendar"), event1, true);

			assertEquals(event1.getRecurrence().getEventExceptions().size(), 0);

			boolean find = false;
			for (Date dd : event1.getRecurrence().getExceptions()) {
				if (dd.equals(calExce.getTime())) {
					find = true;
				}
			}
			assertTrue(find);

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testListCalendars() {
		try {
			CalendarInfo[] lc = cal.listCalendars(token);
			assertNotNull(lc);
			assertTrue(lc.length > 0);
			for (CalendarInfo ci : lc) {
				System.out.println("ci: " + ci.getMail() + " uid: "
						+ ci.getUid() + " read: " + ci.isRead() + " write: "
						+ ci.isWrite());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("cannot list calendars");
		}
	}

	public void testCreateEvent() {
		try {
			String ret = cal.createEvent(token, p("calendar"), getTestEvent());
			CalendarItemsWriter ciw = new CalendarItemsWriter();
			System.out.println(ciw.getEventString(getTestEvent()));

			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testFindEvent() {
		try {
			String ret = cal.createEvent(token, p("calendar"), getTestEvent());
			CalendarItemsWriter ciw = new CalendarItemsWriter();
			System.out.println(ciw.getEventString(getTestEvent()));

			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			Event found = cal.getEventFromId(token, p("calendar"), ret);
			assertNotNull(found);
			assertTrue(found.getAttendees().size() >= 1);
			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(found.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}

			System.out.println("found event by id with title "
					+ found.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testFindEventException() {
		try {

			CalendarItemsWriter ciw = new CalendarItemsWriter();
			// Event 1
			Event event1 = getTestEvent();

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			Calendar calExce = Calendar.getInstance();
			calExce.setTime(event1.getDate());
			calExce.set(Calendar.MONTH, calExce.get(Calendar.MONTH) + 2);
			event2.setRecurrenceId(calExce.getTime());

			event1.getRecurrence().addEventException(event2);

			String ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			Event found = cal.getEventFromId(token, p("calendar"), ev1);
			assertNotNull(found);
			assertNotNull(found.getRecurrence());
			assertEquals(1, found.getRecurrence().getEventExceptions().size());

			System.out.println("found event by id with title "
					+ found.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testFindEventByExtId() {
		try {
			Event ev = getTestEvent();
			String ret = cal.createEvent(token, p("calendar"), ev);
			CalendarItemsWriter ciw = new CalendarItemsWriter();
			System.out.println(ciw.getEventString(ev));

			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			Event found = cal.getEventFromExtId(token, p("calendar"),
					ev.getExtId());
			assertNotNull(found);
			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(found.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}
			assertTrue(found.getAttendees().size() >= 1);
			System.out.println("found event by extId with title "
					+ found.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testFindTodoByExtId() {
		try {
			Event ev = getTestEvent();
			for (Attendee att : ev.getAttendees()) {
				att.setPercent(20);
			}

			String ret = cal.createEvent(token, p("calendar"), ev);
			CalendarItemsWriter ciw = new CalendarItemsWriter();
			System.out.println(ciw.getEventString(ev));

			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			Event found = cal.getEventFromExtId(token, p("calendar"),
					ev.getExtId());
			assertNotNull(found);
			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(found.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}
			for (Attendee att : found.getAttendees()) {
				assertEquals(20, att.getPercent());
			}

			assertTrue(found.getAttendees().size() >= 1);
			System.out.println("found event by extId with title "
					+ found.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testModifyEvent() {
		try {
			Event ev = getTestEvent();
			String ret = cal.createEvent(token, p("calendar"), ev);
			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			ev.setUid(ret);
			ev.setTitle(ev.getTitle() + " modified");
			for (Attendee at : ev.getAttendees()) {
				at.setState(ParticipationState.DELEGATED);
			}

			ev.addAttendee(getTestAttendee());
			String title = ev.getTitle();

			ev = cal.modifyEvent(token, p("calendar"), ev, true);
			System.out.println("modified event with new title '"
					+ ev.getTitle() + "'");
			assertEquals(title, ev.getTitle());
			assertTrue(ev.getAttendees().size() > 0);
			for (Attendee at : ev.getAttendees()) {
				assertEquals(at.getState(), ParticipationState.DELEGATED);
				System.out.println("new at " + at.getEmail() + " state: "
						+ at.getState());
			}

			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(ev.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on modifyEvent");
		}
	}

	public void testModifyEventLoop() {
		try {
			Event ev = getTestEvent();
			String ret = cal.createEvent(token, p("calendar"), ev);
			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			ev.setUid(ret);
			ev.setTitle(ev.getTitle() + " modified");
			for (Attendee at : ev.getAttendees()) {
				at.setState(ParticipationState.DELEGATED);
			}

			ev.addAttendee(getTestAttendee());
			String title = ev.getTitle();

			int CNT = 10000;

			for (int i = 0; i < CNT; i++) {
				ev = cal.modifyEvent(token, p("calendar"), ev, true);
				System.out.println("modified event with new title '"
						+ ev.getTitle() + "'");
				assertEquals(title, ev.getTitle());
				assertTrue(ev.getAttendees().size() > 0);
				Pattern p = Pattern
						.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
				Matcher m = p.matcher(ev.getOwnerEmail());
				if (!m.find()) {
					fail("EmailOwner isn't valid");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on modifyEvent");
		}
	}

	private Attendee getTestAttendee() {
		Attendee at = new Attendee();
		at.setDisplayName("The Added Attendee");
		at.setEmail("attendee@zz.com");
		at.setState(ParticipationState.DELEGATED);
		at.setRequired(ParticipationRole.OPT);
		return at;
	}

	public void testRemoveEvent() {
		try {
			String ret = cal.createEvent(token, p("calendar"), getTestEvent());
			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			System.out.println("Trying rm with id: " + ret);
			Event removed = cal.removeEvent(token, p("calendar"), ret);
			assertNotNull(removed);

			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(removed.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}

			Event ev = cal.getEventFromId(token, p("calendar"), ret);
			assertNull(ev);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testRemoveEventByExtId() {
		try {
			Event ev = getTestEvent();
			String ret = cal.createEvent(token, p("calendar"), ev);
			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			System.out.println("Trying rm with extId: " + ev.getExtId());
			Event removed = cal.removeEventByExtId(token, p("calendar"),
					ev.getExtId());
			assertNotNull(removed);

			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(removed.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}

			Event evn = cal.getEventFromExtId(token, p("calendar"),
					ev.getExtId());
			assertNull(evn);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testRemoveEventWithException() {
		try {
			CalendarItemsWriter ciw = new CalendarItemsWriter();

			// Event 1
			Event event1 = getTestEvent();
			String ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			event2.setRecurrenceId(event1.getDate());
			String ev2 = cal.createEvent(token, p("calendar"), event2);
			System.out.println(ciw.getEventString(event2));
			assertNotNull(ev2);

			System.out.println("Trying rm with id: " + ev1);
			Event removed = cal.removeEvent(token, p("calendar"), ev1);
			assertNotNull(removed);

			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(removed.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}

			Event ev = cal.getEventFromId(token, p("calendar"), ev1);
			assertNull(ev);

			ev = cal.getEventFromId(token, p("calendar"), ev2);

			// FIXME disabled in 2.2 for mysql bug
			// assertNull(ev);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testRemoveOnNonEvent() {
		Event removed = null;
		try {
			removed = cal.removeEvent(token, p("calendar"), "12345678");
			fail("client should send en error");
		} catch (Exception e) {
			assertNull(removed);
		}
	}

	public void testModifyOnNonEvent() {
		Event removed = getTestEvent();
		removed.setUid("12345678");
		try {
			removed = cal.modifyEvent(token, p("calendar"), removed, true);
			fail("client should send en error");
		} catch (Exception e) {
		}
	}

	public void testGetUserEmail() {
		try {
			String ret = cal.getUserEmail(token);
			assertNotNull(ret);
			System.out.println("Found email: " + ret);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getUserEmail");
		}
	}

	public void testGetRefusedKeys() {
		try {
			KeyList ret = cal.getRefusedKeys(token, p("calendar"), null);
			assertNotNull(ret);
			assertNotNull(ret.getKeys());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getRefusedKeys");
		}
	}

	public void testGetEventTwinKeys() {
		try {
			Event ev = getTestEvent();
			KeyList ret = null;

			ev.setTitle("twin");
			System.out.println("twin date: " + ev.getDate());
			String uid = cal.createEvent(token, p("calendar"), ev);
			ret = cal.getEventTwinKeys(token, p("calendar"), ev);
			assertNotNull(ret);
			System.out.println("got keylist with " + ret.getKeys().size()
					+ " for " + ev.getTitle() + " at " + ev.getDate());
			assertTrue(ret.getKeys().size() > 0);

			cal.removeEvent(token, p("calendar"), uid);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getEventTwinKeys");
		}
	}

	public void testGetEventFromId() {
		try {
			Event origEv = getTestEvent();
			String ret = cal.createEvent(token, p("calendar"), origEv);
			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			Event ev = cal.getEventFromId(token, p("calendar"), ret);
			assertNotNull(ev);
			System.out.println("getEventFromId found event '" + ev.getTitle()
					+ "'");
			assertEquals(origEv.getDate(), ev.getDate());

			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			Matcher m = p.matcher(ev.getOwnerEmail());
			if (!m.find()) {
				fail("EmailOwner isn't valid");
			}

			System.out.println("Orig ev date: " + origEv.getDate()
					+ " found date: " + ev.getDate());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getEventFromId");
		}
	}

	public void testListCategories() {
		try {
			List<Category> cats = cal.listCategories(token);
			assertNotNull(cats);
			System.out.println("categories size: " + cats.size());
			int count = 500;
			long time = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				cal.listCategories(token);
			}
			time = System.currentTimeMillis() - time;
			System.out.println(count + " listCategories() calls took " + time
					+ "ms. Performing at " + (count * 1000) / time + "/sec");

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		}
	}

	public void testFreeBusy() throws IOException {
		String url = p("obm.sync.url").replace("/services",
				"/freebusy/" + p("calendar"));
		try {
			InputStream in = new URL(url).openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (MalformedURLException e) {
		}
	}

	public void testGetAllEvents() {
		try {
			CalendarItemsWriter ciw = new CalendarItemsWriter();

			// Event 1
			Event event1 = getTestEvent();
			event1.setRecurrence(null);
			String ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			String ev2 = cal.createEvent(token, p("calendar"), event2);
			System.out.println(ciw.getEventString(event2));
			assertNotNull(ev2);

			List<Event> found = cal.getAllEvents(token, p("calendar"),
					EventType.VEVENT);
			assertNotNull(found);
			assertTrue(found.size() > 1);

			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			for (Event event : found) {
				Matcher m = p.matcher(event.getOwnerEmail());
				if (!m.find()) {
					fail("EmailOwner isn't valid");
				}
			}
		} catch (Exception e) {
			fail("error on getAllEvents");
		}

	}

	public void testGetListEventsFromIntervalDate() {
		String ev1 = "";
		String ev2 = "";
		String ev3 = "";
		String ev4 = "";
		String ev5 = "";
		String ev6 = "";
		try {
			CalendarItemsWriter ciw = new CalendarItemsWriter();

			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 1990);

			// Event 1
			Event event1 = getTestEvent();
			event1.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 10);
			event1.setDate(calendar.getTime());
			ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);
			System.out.println(ev1);

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 60);
			event2.setDate(calendar.getTime());
			ev2 = cal.createEvent(token, p("calendar"), event2);
			System.out.println(ciw.getEventString(event2));
			assertNotNull(ev2);
			System.out.println(ev2);

			// Event 3
			Event event3 = getTestEvent();
			event3.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 30);
			event3.setDate(calendar.getTime());
			ev3 = cal.createEvent(token, p("calendar"), event3);
			System.out.println(ciw.getEventString(event3));
			assertNotNull(ev3);
			System.out.println(ev3);

			// Event 4 which should not be in the result
			Event event4 = getTestEvent();
			event4.setRecurrence(null);
			calendar.set(Calendar.YEAR, 1991);
			event4.setDate(calendar.getTime());
			ev4 = cal.createEvent(token, p("calendar"), event4);
			System.out.println(ciw.getEventString(event4));
			assertNotNull(ev4);
			System.out.println(ev4);

			// Event 5 rec
			Event event5 = getTestEvent();
			calendar.set(Calendar.YEAR, 1989);
			event5.setDate(calendar.getTime());
			EventRecurrence er = new EventRecurrence();
			er.setKind(RecurrenceKind.daily);
			er.setFrequence(1);
			calendar.set(Calendar.YEAR, 1991);
			er.setEnd(calendar.getTime());
			event5.setRecurrence(er);
			ev5 = cal.createEvent(token, p("calendar"), event5);
			System.out.println(ciw.getEventString(event5));
			assertNotNull(ev5);
			System.out.println(ev5);

			// Event 6 rec
			Event event6 = getTestEvent();
			calendar.set(Calendar.MONTH, 1);
			calendar.set(Calendar.YEAR, 1989);
			event6.setDate(calendar.getTime());
			EventRecurrence er6 = new EventRecurrence();
			er6.setKind(RecurrenceKind.daily);
			er6.setFrequence(10);
			calendar.set(Calendar.YEAR, 1991);
			er6.setEnd(calendar.getTime());
			event6.setRecurrence(er6);
			ev6 = cal.createEvent(token, p("calendar"), event6);
			System.out.println(ciw.getEventString(event6));
			assertNotNull(ev6);
			System.out.println(ev6);

			calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, 1990);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			Date start = calendar.getTime();

			calendar.set(Calendar.DAY_OF_MONTH,
					calendar.get(Calendar.DAY_OF_MONTH) + 1);
			Date end = calendar.getTime();

			List<Event> found = cal.getListEventsFromIntervalDate(token,
					p("calendar"), start, end);

			Pattern p = Pattern
					.compile("[a-z0-9._-]+@[a-z0-9.-]{2,}[.][a-z]{2,4}");
			for (Event event : found) {
				System.out.println(event.getDatabaseId());
				Matcher m = p.matcher(event.getOwnerEmail());
				if (!m.find()) {
					fail("EmailOwner isn't valid");
				}
			}

			assertNotNull("result list must not be null", found);
			assertTrue("result list must contain only 4 events",
					found.size() >= 4);

			for (Event foundE : found) {
				assertFalse("event4 must not be in the list",
						ev4.equals(foundE.getUid()));
				assertFalse("event6 must not be in the list",
						ev6.equals(foundE.getUid()));
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		} finally {
			try {
				cal.removeEvent(token, p("calendar"), ev1);
				cal.removeEvent(token, p("calendar"), ev2);
				cal.removeEvent(token, p("calendar"), ev3);
				cal.removeEvent(token, p("calendar"), ev4);
				cal.removeEvent(token, p("calendar"), ev5);
				cal.removeEvent(token, p("calendar"), ev6);
			} catch (Exception e) {
			}
		}

	}

	public void testGetEventTimeUpdateNotRefusedFromIntervalDate() {
		String ev1 = "";
		String ev2 = "";
		String ev3 = "";
		String ev4 = "";
		String ev5 = "";
		String ev6 = "";
		try {
			CalendarItemsWriter ciw = new CalendarItemsWriter();

			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 1990);

			// Event 1
			Event event1 = getTestEvent();
			event1.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 10);
			event1.setDate(calendar.getTime());
			ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 60);
			event2.setDate(calendar.getTime());
			ev2 = cal.createEvent(token, p("calendar"), event2);
			System.out.println(ciw.getEventString(event2));
			assertNotNull(ev2);

			// Event 3
			Event event3 = getTestEvent();
			event3.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 30);
			event3.setDate(calendar.getTime());
			ev3 = cal.createEvent(token, p("calendar"), event3);
			System.out.println(ciw.getEventString(event3));
			assertNotNull(ev3);

			// Event 4 which should not be in the result
			Event event4 = getTestEvent();
			event4.setRecurrence(null);
			calendar.set(Calendar.YEAR, 1991);
			event4.setDate(calendar.getTime());
			ev4 = cal.createEvent(token, p("calendar"), event4);
			System.out.println(ciw.getEventString(event4));
			assertNotNull(ev4);

			// Event 5 rec
			Event event5 = getTestEvent();
			for (Attendee att : event5.getAttendees()) {
				att.setState(ParticipationState.DECLINED);
			}
			calendar.set(Calendar.YEAR, 1989);
			event5.setDate(calendar.getTime());
			EventRecurrence er = new EventRecurrence();
			er.setKind(RecurrenceKind.daily);
			er.setFrequence(10);
			calendar.set(Calendar.YEAR, 1991);
			er.setEnd(calendar.getTime());
			event5.setRecurrence(er);
			ev5 = cal.createEvent(token, p("calendar"), event5);
			System.out.println(ciw.getEventString(event5));
			assertNotNull(ev5);

			// Event 6 rec
			Event event6 = getTestEvent();
			calendar.set(Calendar.MONTH, 1);
			calendar.set(Calendar.YEAR, 1989);
			event6.setDate(calendar.getTime());
			EventRecurrence er6 = new EventRecurrence();
			er6.setKind(RecurrenceKind.daily);
			er6.setFrequence(10);
			calendar.set(Calendar.YEAR, 1991);
			er6.setEnd(calendar.getTime());
			event6.setRecurrence(er6);
			ev6 = cal.createEvent(token, p("calendar"), event6);
			System.out.println(ciw.getEventString(event6));
			assertNotNull(ev6);

			calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 1990);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date start = calendar.getTime();

			calendar.set(Calendar.DAY_OF_MONTH,
					calendar.get(Calendar.DAY_OF_MONTH) + 1);
			Date end = calendar.getTime();

			List<EventTimeUpdate> found = cal
					.getEventTimeUpdateNotRefusedFromIntervalDate(token,
							p("calendar"), start, end);

			assertNotNull("result list must not be null", found);
			assertTrue("result list must contain only 3 events",
					found.size() >= 3);

			for (EventTimeUpdate foundE : found) {
				assertFalse("event4 must not be in the list",
						ev4.equals(foundE.getUid()));
				assertFalse("event5 must not be in the list",
						ev5.equals(foundE.getUid()));
				assertFalse("event6 must not be in the list",
						ev6.equals(foundE.getUid()));
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on createEvent");
		} finally {
			try {
				cal.removeEvent(token, p("calendar"), ev1);
				cal.removeEvent(token, p("calendar"), ev2);
				cal.removeEvent(token, p("calendar"), ev3);
				cal.removeEvent(token, p("calendar"), ev4);
				cal.removeEvent(token, p("calendar"), ev5);
				cal.removeEvent(token, p("calendar"), ev6);
			} catch (Exception e) {
			}
		}
	}

	public void testParseEvents() {
		try {
			CalendarItemsWriter ciw = new CalendarItemsWriter();
			// Event 1
			Event event1 = getTestEvent();
			event1.setRecurrence(null);
			String ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			String ev2 = cal.createEvent(token, p("calendar"), event2);
			System.out.println(ciw.getEventString(event2));
			assertNotNull(ev2);

			List<Event> le = new LinkedList<Event>();
			le.add(event1);
			le.add(event2);

			String ll = cal.parseEvents(token, le);

			assertNotNull(ll);
			assertFalse("".endsWith(ll));

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testParseEvent() {
		try {
			// Event 1
			Event event1 = getTestEvent();

			String ll = cal.parseEvent(token, event1);
			assertNotNull(ll);
			assertFalse("".endsWith(ll));

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testParseICS() throws IOException, ParserException {
		InputStream in = ObmSyncTestCase.class.getClassLoader()
				.getResourceAsStream("icsFile/eventComplet.ics");
		if (in == null) {
			fail("Cannot load event1.ics");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				fail(e.getMessage());
			}
		}

		String ics = sb.toString();
		List<Event> events;
		try {
			events = cal.parseICS(token, ics);
			assertEquals(1, events.size());
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	public void testParseFreeBusyICS() {
		InputStream in = ObmSyncTestCase.class.getClassLoader()
				.getResourceAsStream("icsFile/freeBusyComplet.ics");
		if (in == null) {
			fail("Cannot load freeBusyComplet.ics");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				fail(e.getMessage());
			}
		}

		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String ics = sb.toString();
		try {
			FreeBusyRequest fb = cal.parseICSFreeBusy(token, ics);
			assertNotNull(fb);
			assertEquals("adrien@test.tlse.lng", fb.getOwner());
			assertEquals(df.parse("20091011T220000Z"), fb.getStart());
			assertEquals(df.parse("20091027T230000Z"), fb.getEnd());
			assertNotNull(fb);
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	public void testGetEventParticipationStateWithAlertFromIntervalDate() {
		String ev1 = "";
		String ev2 = "";
		String ev3 = "";
		String ev4 = "";
		String ev5 = "";
		String ev6 = "";
		try {
			CalendarItemsWriter ciw = new CalendarItemsWriter();

			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 1990);

			// Event 1
			Event event1 = getTestEvent();
			event1.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 10);
			event1.setDate(calendar.getTime());
			ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 60);
			event2.setDate(calendar.getTime());
			ev2 = cal.createEvent(token, p("calendar"), event2);
			System.out.println(ciw.getEventString(event2));
			assertNotNull(ev2);

			// Event 3
			Event event3 = getTestEvent();
			event3.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 30);
			event3.setDate(calendar.getTime());
			ev3 = cal.createEvent(token, p("calendar"), event3);
			System.out.println(ciw.getEventString(event3));
			assertNotNull(ev3);

			// Event 4 which should not be in the result
			Event event4 = getTestEvent();
			event4.setRecurrence(null);
			calendar.set(Calendar.YEAR, 1991);
			event4.setDate(calendar.getTime());
			ev4 = cal.createEvent(token, p("calendar"), event4);
			System.out.println(ciw.getEventString(event4));
			assertNotNull(ev4);

			// Event 5 rec
			Event event5 = getTestEvent();
			calendar.set(Calendar.YEAR, 1989);
			event5.setDate(calendar.getTime());
			EventRecurrence er = new EventRecurrence();
			er.setKind(RecurrenceKind.daily);
			// er.setFrequence(1);
			calendar.set(Calendar.YEAR, 1991);
			er.setEnd(calendar.getTime());
			event5.setRecurrence(er);
			ev5 = cal.createEvent(token, p("calendar"), event5);
			System.out.println(ciw.getEventString(event5));
			assertNotNull(ev5);

			// Event 6 rec
			Event event6 = getTestEvent();
			calendar.set(Calendar.MONTH, 1);
			calendar.set(Calendar.YEAR, 1989);
			event6.setDate(calendar.getTime());
			EventRecurrence er6 = new EventRecurrence();
			er6.setKind(RecurrenceKind.daily);
			er6.setFrequence(10);
			calendar.set(Calendar.YEAR, 1991);
			er6.setEnd(calendar.getTime());
			event6.setRecurrence(er6);
			ev6 = cal.createEvent(token, p("calendar"), event6);
			System.out.println(ciw.getEventString(event6));
			assertNotNull(ev6);

			calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 1990);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date start = calendar.getTime();

			calendar.set(Calendar.DAY_OF_MONTH,
					calendar.get(Calendar.DAY_OF_MONTH) + 1);
			Date end = calendar.getTime();

			List<EventParticipationState> found = cal
					.getEventParticipationStateWithAlertFromIntervalDate(token,
							p("calendar"), start, end);

			assertNotNull("result list must not be null", found);
			assertTrue("result list must contain only 4 events",
					found.size() >= 3);

			for (EventParticipationState foundE : found) {
				assertNotSame(0, foundE.getAlert());
				System.out.println(foundE.getDate());
				assertNotNull(foundE.getDate());
				assertFalse("event4 must not be in the list",
						ev4.equals(foundE.getUid()));
				assertFalse("event6 must not be in the list",
						ev6.equals(foundE.getUid()));
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on testGetEventParticipationStateFromIntervalDate");
		} finally {
			try {
				cal.removeEvent(token, p("calendar"), ev1);
				cal.removeEvent(token, p("calendar"), ev2);
				cal.removeEvent(token, p("calendar"), ev3);
				cal.removeEvent(token, p("calendar"), ev4);
				cal.removeEvent(token, p("calendar"), ev5);
				cal.removeEvent(token, p("calendar"), ev6);
			} catch (Exception e) {
			}
		}

	}

	public void testGetLastUpdate() {
		try {
			String ret = cal.createEvent(token, p("calendar"), getTestEvent());
			CalendarItemsWriter ciw = new CalendarItemsWriter();
			System.out.println(ciw.getEventString(getTestEvent()));

			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);

			Event found = cal.getEventFromId(token, p("calendar"), ret);
			assertNotNull(found);

			Date lastUp = cal.getLastUpdate(token, p("calendar"));

			assertEquals(found.getTimeCreate(), lastUp);

		} catch (Exception e) {
			e.printStackTrace();
			fail("error");
		}
	}

	public void testIsWritableCalendar() {
		try {
			boolean writable = cal.isWritableCalendar(token, p("calendar"));
			assertTrue(writable);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error");
		}
	}

	public void testGetFreeBusy() {
		String ev1 = "";
		String ev2 = "";
		String ev3 = "";
		String ev4 = "";
		String ev5 = "";
		String ev6 = "";
		try {
			CalendarItemsWriter ciw = new CalendarItemsWriter();

			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 1990);

			// Event 1
			Event event1 = getTestEvent();
			event1.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 10);
			event1.setDate(calendar.getTime());
			ev1 = cal.createEvent(token, p("calendar"), event1);
			System.out.println(ciw.getEventString(event1));
			assertNotNull(ev1);

			// Event 2
			Event event2 = getTestEvent();
			event2.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 60);
			event2.setDate(calendar.getTime());
			ev2 = cal.createEvent(token, p("calendar"), event2);
			System.out.println(ciw.getEventString(event2));
			assertNotNull(ev2);

			// Event 3
			Event event3 = getTestEvent();
			event3.setRecurrence(null);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 30);
			event3.setDate(calendar.getTime());
			ev3 = cal.createEvent(token, p("calendar"), event3);
			System.out.println(ciw.getEventString(event3));
			assertNotNull(ev3);

			// Event 4 which should not be in the result
			Event event4 = getTestEvent();
			event4.setRecurrence(null);
			calendar.set(Calendar.YEAR, 1991);
			event4.setDate(calendar.getTime());
			ev4 = cal.createEvent(token, p("calendar"), event4);
			System.out.println(ciw.getEventString(event4));
			assertNotNull(ev4);

			// Event 5 rec
			Event event5 = getTestEvent();
			calendar.set(Calendar.YEAR, 1989);
			event5.setDate(calendar.getTime());
			EventRecurrence er = new EventRecurrence();
			er.setKind(RecurrenceKind.daily);
			// er.setFrequence(1);
			calendar.set(Calendar.YEAR, 1991);
			er.setEnd(calendar.getTime());
			event5.setRecurrence(null);
			ev5 = cal.createEvent(token, p("calendar"), event5);
			System.out.println(ciw.getEventString(event5));
			assertNotNull(ev5);

			// Event 6 rec
			Event event6 = getTestEvent();
			calendar.set(Calendar.MONTH, 1);
			calendar.set(Calendar.YEAR, 1989);
			event6.setDate(calendar.getTime());
			EventRecurrence er6 = new EventRecurrence();
			er6.setKind(RecurrenceKind.daily);
			er6.setFrequence(10);
			calendar.set(Calendar.YEAR, 1991);
			er6.setEnd(calendar.getTime());
			event6.setRecurrence(null);
			ev6 = cal.createEvent(token, p("calendar"), event6);
			System.out.println(ciw.getEventString(event6));
			assertNotNull(ev6);

			calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 1990);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date start = calendar.getTime();

			calendar.set(Calendar.DAY_OF_MONTH,
					calendar.get(Calendar.DAY_OF_MONTH) + 1);
			Date end = calendar.getTime();

			FreeBusyRequest fbr = new FreeBusyRequest();
			fbr.setStart(start);
			fbr.setEnd(end);
			fbr.setOwner(p("calendar"));

			List<Attendee> atts = new LinkedList<Attendee>();
			Attendee at = new Attendee();
			at.setDisplayName(p("login"));
			at.setEmail(p("login"));
			at.setState(ParticipationState.NEEDSACTION);
			at.setRequired(ParticipationRole.CHAIR);
			atts.add(at);

			fbr.setAttendees(atts);

			List<FreeBusy> freeBusys = cal.getFreeBusy(token, fbr);
			assertNotNull("result freeBusy must not be null", freeBusys);
			for (FreeBusy fb : freeBusys) {
				assertEquals(start, fb.getStart());
				assertEquals(end, fb.getEnd());
				assertEquals(p("calendar"), fb.getOwner());

				assertEquals(
						"result list must contain only 3 freeBusyInterval", 3,
						fb.getFreeBusyIntervals().size());
			}
			// for (EventParticipationState foundE : found) {
			// assertNotSame(0, foundE.getAlert());
			// System.out.println(foundE.getDate());
			// assertNotNull(foundE.getDate());
			// assertFalse("event4 must not be in the list", ev4.equals(foundE
			// .getUid()));
			// assertFalse("event6 must not be in the list", ev6.equals(foundE
			// .getUid()));
			// }

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on testGetEventParticipationStateFromIntervalDate");
		} finally {
			try {
				cal.removeEvent(token, p("calendar"), ev1);
				cal.removeEvent(token, p("calendar"), ev2);
				cal.removeEvent(token, p("calendar"), ev3);
				cal.removeEvent(token, p("calendar"), ev4);
				cal.removeEvent(token, p("calendar"), ev5);
				cal.removeEvent(token, p("calendar"), ev6);
			} catch (Exception e) {
			}
		}
	}

	public void testGetFreeBusyRecurEvent() {
		String ev1 = "";
		try {
			Calendar calendar = new GregorianCalendar();
			// Event recur
			Event event1 = getTestEvent();

			calendar.set(2007, 10, 1);
			event1.setDate(calendar.getTime());
			event1.setDate(calendar.getTime());
			EventRecurrence er = new EventRecurrence();
			er.setKind(RecurrenceKind.daily);
			er.setFrequence(1);

			calendar.set(2007, 10, 18);
			er.addException(calendar.getTime());

			Event eventException = getTestEvent();
			eventException.setRecurrence(null);
			calendar.set(2007, 10, 19);
			eventException.setDate(calendar.getTime());
			eventException.setRecurrenceId(calendar.getTime());
			er.addEventException(eventException);

			calendar.set(2007, 10, 30);
			er.setEnd(calendar.getTime());
			event1.setRecurrence(er);
			ev1 = cal.createEvent(token, p("calendar"), event1);
			assertNotNull(ev1);

			calendar.set(2007, 10, 17);
			Date start = calendar.getTime();

			calendar.set(2007, 10, 20, 23, 59);
			Date end = calendar.getTime();

			FreeBusyRequest fbr = new FreeBusyRequest();
			fbr.setStart(start);
			fbr.setEnd(end);
			fbr.setOwner(p("calendar"));

			List<Attendee> atts = new LinkedList<Attendee>();
			Attendee at = new Attendee();
			at.setDisplayName(p("login"));
			at.setEmail(p("login"));
			at.setState(ParticipationState.NEEDSACTION);
			at.setRequired(ParticipationRole.CHAIR);
			atts.add(at);

			fbr.setAttendees(atts);

			List<FreeBusy> freeBusys = cal.getFreeBusy(token, fbr);
			assertNotNull("result freeBusy must not be null", freeBusys);
			for (FreeBusy fb : freeBusys) {
				assertEquals(start, fb.getStart());
				assertEquals(end, fb.getEnd());
				assertEquals(p("calendar"), fb.getOwner());
				boolean exist17 = false;
				boolean exist18 = false;
				boolean exist19 = false;
				boolean exist20 = false;
				for (FreeBusyInterval inter : fb.getFreeBusyIntervals()) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(inter.getStart());
					if (cal.get(Calendar.MONTH) == 10
							&& cal.get(Calendar.YEAR) == 2007)
						if (cal.get(Calendar.DAY_OF_MONTH) == 17) {
							exist17 = true;
						} else if (cal.get(Calendar.DAY_OF_MONTH) == 18) {
							exist18 = true;
						} else if (cal.get(Calendar.DAY_OF_MONTH) == 19) {
							exist19 = true;
						} else if (cal.get(Calendar.DAY_OF_MONTH) == 20) {
							exist20 = true;
						}
				}
				assertTrue(exist17);
				assertFalse(exist18);
				assertTrue(exist19);
				assertTrue(exist20);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on testGetEventParticipationStateFromIntervalDate");
		} finally {
			try {
				cal.removeEvent(token, p("calendar"), ev1);
			} catch (Exception e) {
			}
		}
	}

	public void testParseFreeBusyToICS() throws IOException, ParserException {
		FreeBusy fb = new FreeBusy();
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.get(Calendar.DAY_OF_MONTH) - 1);
		fb.setStart(calendar.getTime());

		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.get(Calendar.DAY_OF_MONTH) + 2);
		fb.setEnd(calendar.getTime());

		Attendee at = new Attendee();
		at.setDisplayName(p("login"));
		at.setEmail(p("login"));
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);

		calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		FreeBusyInterval fbl1 = new FreeBusyInterval();
		fbl1.setStart(calendar.getTime());
		fbl1.setDuration(1);
		fb.addFreeBusyInterval(fbl1);

		calendar.set(Calendar.HOUR_OF_DAY, 14);
		FreeBusyInterval fbl2 = new FreeBusyInterval();
		fbl2.setStart(calendar.getTime());
		fbl2.setDuration(2);
		fb.addFreeBusyInterval(fbl2);

		fb.setAtt(at);

		try {
			String ics = cal.parseFreeBusyToICS(token, fb);
			System.out.println(ics);
			assertNotNull(ics);
			assertNotSame("", ics);
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}
	
	public void testSyncTask() {
		try {
			Event e = getTestEvent();
			e.setType(EventType.VTODO);
			String ret = cal.createEvent(token, p("calendar"), e);
			assertNotNull(ret);
			System.out.println("Created event with id: " + ret);
			
			EventChanges changes = cal.getSync(token, p("calendar"), null);
			assertNotNull(changes);
			assertNotNull(changes.getLastSync());
			assertNotNull(changes.getRemoved());
			assertNotNull(changes.getUpdated());
			assertTrue(changes.getUpdated().length == 0);

			System.out.println("Trying rm with id: " + ret);
			Event removed = cal.removeEvent(token, p("calendar"), ret);
			assertNotNull(removed);
			
			changes = cal.getSync(token, p("calendar"), changes.getLastSync());
			assertTrue(changes.getRemoved().length == 0);

			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception on getSync");
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cal = new CalendarLocator().locate(p("obm.sync.url"));
		assertNotNull(cal);
		token = cal.login(p("login"), p("password"), "junit");
		assertNotNull(token);
	}

	@Override
	protected void tearDown() throws Exception {
		cal.logout(token);
		super.tearDown();
	}

	@Override
	protected Contact getTestContact() {
		return null;
	}

}
