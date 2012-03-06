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
package org.obm.sync.calendar;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

/**
 * Serializes calendar related items to XML
 */
public class CalendarItemsWriterTest extends AbstractItemsWriter {

private CalendarItemsWriter writer;
	
	@Before
	public void initCalendarWriter(){
		writer = new CalendarItemsWriter();
	}
	
	@Test
	public void testGetEventString() throws TransformerException, SAXException, IOException {
		Event ev = getFakeEvent();

		String resultXML = writer.getEventString(ev);

		String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<event allDay=\"false\" isInternal=\"true\" sequence=\"3\" type=\"VEVENT\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">"
				+ "<timeupdate>1292580000000</timeupdate>"
				+ "<timecreate>1289988000000</timecreate>"
				+ "<extId>2bf7db53-8820-4fe5-9a78-acc6d3262149</extId>"
				+ "<opacity>OPAQUE</opacity>"
				+ "<title>fake rdv</title>"
				+ "<owner>john@do.fr</owner>"
				+ "<tz>Europe/Paris</tz>"
				+ "<date>1295258400000</date>"
				+ "<duration>3600</duration>"
				+ "<location>tlse</location>"
				+ "<alert>60</alert>"
				+ "<priority>0</priority>"
				+ "<privacy>0</privacy>"
				+ "<attendees>"
				+ "<attendee displayName=\"John Do\" email=\"john@do.fr\" isOrganizer=\"true\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\"/>"
				+ "<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" isOrganizer=\"false\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>"
				+ "</attendees>"
				+ "<recurrence days=\"\" freq=\"1\" kind=\"daily\">"
				+ "<exceptions>"
				+ "<exception>1295258400000</exception>"
				+ "<exception>1292580000000</exception>"
				+ "</exceptions><eventExceptions/>"
				+ "</recurrence>"
				+ "</event>";

		XMLAssert.assertXMLEqual(expectedXML, resultXML);
	}

	@Test
	public void testGetXMLDocumentFromWithNoChange() throws SAXException, IOException, TransformerException {
		EventChanges eventChanges = new EventChanges();
		eventChanges.setLastSync(new Date(1330957589525L));

		String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<calendar-changes lastSync=\"1330957589525\" xmlns=\"http://www.obm.org/xsd/sync/calendar-changes.xsd\">"
				+ "<removed/>"
				+ "<updated/>"
				+ "<participationChanges/>"
				+ "</calendar-changes>";

		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromWithRemovedElements() throws SAXException, IOException, TransformerException {
		EventChanges eventChanges = new EventChanges();
		eventChanges.setLastSync(new Date(1330957589525L));

		DeletedEvent deletedEvent1 = new DeletedEvent(new EventObmId(1), new EventExtId("123"));
		DeletedEvent deletedEvent2 = new DeletedEvent(new EventObmId(2), new EventExtId("456"));
		eventChanges.setDeletions(Lists.newArrayList(deletedEvent1, deletedEvent2));

		String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<calendar-changes lastSync=\"1330957589525\" xmlns=\"http://www.obm.org/xsd/sync/calendar-changes.xsd\">"
				+ "<removed>" 
				+ "<event extId=\"123\" id=\"1\"/>"
				+ "<event extId=\"456\" id=\"2\"/>" 
				+ "</removed>"
				+ "<updated/>"
				+ "<participationChanges/>"
				+ "</calendar-changes>";

		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}
	
	@Test
	public void testGetXMLDocumentFromWithUpdatedElements() throws SAXException, IOException, TransformerException {
		EventChanges eventChanges = new EventChanges();
		eventChanges.setLastSync(new Date(1330957589525L));

		Event updatedEvent = getFakeEvent();
		Event eventException = updatedEvent.getOccurrence(updatedEvent.getStartDate());
		updatedEvent.addEventException(eventException);

		Event[] updated = {updatedEvent};
		eventChanges.setUpdated(updated);

		String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<calendar-changes lastSync=\"1330957589525\" xmlns=\"http://www.obm.org/xsd/sync/calendar-changes.xsd\">"
				+ "<removed/>"
				+ "<updated>"
				+ "<event allDay=\"false\" isInternal=\"true\" sequence=\"3\" type=\"VEVENT\">"
				+ "<timeupdate>1292580000000</timeupdate>"
				+ "<timecreate>1289988000000</timecreate>"
				+ "<extId>2bf7db53-8820-4fe5-9a78-acc6d3262149</extId>"
				+ "<opacity>OPAQUE</opacity>"
				+ "<title>fake rdv</title>"
				+ "<owner>john@do.fr</owner>"
				+ "<tz>Europe/Paris</tz>"
				+ "<date>1295258400000</date>"
				+ "<duration>3600</duration>"
				+ "<location>tlse</location>"
				+ "<alert>60</alert>"
				+ "<priority>0</priority>"
				+ "<privacy>0</privacy>"
				+ "<attendees>"
				+ "<attendee displayName=\"John Do\" email=\"john@do.fr\" isOrganizer=\"true\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\"/>"
				+ "<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" isOrganizer=\"false\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>"
				+ "</attendees>"
				+ "<recurrence days=\"\" freq=\"1\" kind=\"daily\">"
				+ "<exceptions>"
				+ "<exception>1295258400000</exception>"
				+ "<exception>1292580000000</exception>"
				+ "</exceptions>"
				+ "<eventExceptions>"
				+ "<eventException allDay=\"false\" isInternal=\"true\" sequence=\"3\" type=\"VEVENT\">"
				+ "<timeupdate>1292580000000</timeupdate>"
				+ "<timecreate>1289988000000</timecreate>"
				+ "<recurrenceId>1295258400000</recurrenceId>"
				+ "<extId>2bf7db53-8820-4fe5-9a78-acc6d3262149</extId>"
				+ "<opacity>OPAQUE</opacity>"
				+ "<title>fake rdv</title>"
				+ "<owner>john@do.fr</owner>"
				+ "<tz>Europe/Paris</tz>"
				+ "<date>1295258400000</date>"
				+ "<duration>3600</duration>"
				+ "<location>tlse</location>"
				+ "<alert>60</alert>"
				+ "<priority>0</priority>"
				+ "<privacy>0</privacy>"
				+ "<attendees>"
				+ "<attendee displayName=\"John Do\" email=\"john@do.fr\" isOrganizer=\"true\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\"/>"
				+ "<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" isOrganizer=\"false\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>"
				+ "</attendees>"
				+ "<recurrence kind=\"none\"/>"
				+ "</eventException>"
				+ "</eventExceptions>"
				+ "</recurrence>"
				+ "</event>"
				+ "</updated>"
				+ "<participationChanges/>"
				+ "</calendar-changes>";

		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		String serialize = DOMUtils.serialize(resultDocument);
		XMLAssert.assertXMLEqual(expectedXML, serialize);
	}

	@Test
	public void testGetXMLDocumentFromWithParticipationChangesElements() throws SAXException, IOException, TransformerException {
		EventChanges eventChanges = new EventChanges();
		eventChanges.setLastSync(new Date(1330957589525L));

		ParticipationChanges[] participationUpdated = getFakeListOfParticipationChanges();
		eventChanges.setParticipationUpdated(participationUpdated);

		String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<calendar-changes lastSync=\"1330957589525\" xmlns=\"http://www.obm.org/xsd/sync/calendar-changes.xsd\">"
				+ "<removed/>"
				+ "<updated/>"
				+ "<participationChanges>"
				+ "<participation extId=\"123\" id=\"1\">"
				+ "<attendees>"
				+ "<attendee email=\"john@doe\" state=\"ACCEPTED\"/>"
				+ "<attendee email=\"jane@doe\" state=\"NEEDS-ACTION\"/>"
				+ "</attendees>"
				+ "</participation>"
				+ "<participation extId=\"456\" id=\"2\" recurrenceId=\"789\">"
				+ "<attendees>"
				+ "<attendee email=\"john@doe\" state=\"ACCEPTED\"/>"
				+ "<attendee email=\"jane@doe\" state=\"NEEDS-ACTION\"/>"
				+ "</attendees>"
				+ "</participation>"
				+ "</participationChanges>"
				+ "</calendar-changes>";

		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	private Event getFakeEvent() {
		Event ev = new Event();
		ev.setInternalEvent(true);
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		ev.setStartDate(cal.getTime());
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		ev.setTimeUpdate(cal.getTime());
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		ev.setTimeCreate(cal.getTime());
		ev.setExtId(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149"));
		ev.setTitle("fake rdv");
		ev.setOwner("john@do.fr");
		ev.setDuration(3600);
		ev.setLocation("tlse");
		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setParticipationRole(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		la.add(at);
		at = new Attendee();
		at.setDisplayName("noIn TheDatabase");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setParticipationRole(ParticipationRole.OPT);
		la.add(at);
		ev.setAttendees(la);
		ev.setAlert(60);
		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.daily);
		er.setFrequence(1);
		er.addException(ev.getStartDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);
		ev.setRecurrence(er);
		ev.setSequence(3);
		return ev;
	}

	private ParticipationChanges[] getFakeListOfParticipationChanges() {
		ParticipationChanges participationChanges1 = new ParticipationChanges();
		participationChanges1.setEventExtId(new EventExtId("123"));
		participationChanges1.setEventId(new EventObmId(1));
		List<Attendee> attendees = getFakeListOfAttendee();
		participationChanges1.setAttendees(attendees);

		ParticipationChanges participationChanges2 = new ParticipationChanges();
		participationChanges2.setEventExtId(new EventExtId("456"));
		participationChanges2.setEventId(new EventObmId(2));
		participationChanges2.setRecurrenceId(new RecurrenceId("789"));
		participationChanges2.setAttendees(attendees);

		ParticipationChanges[] participationUpdated = {participationChanges1, participationChanges2};
		return participationUpdated;
	}

	private List<Attendee> getFakeListOfAttendee() {
		Attendee john = new Attendee();
		john.setEmail("john@doe");
		john.setState(ParticipationState.ACCEPTED);
		Attendee jane = new Attendee();
		jane.setEmail("jane@doe");
		jane.setState(ParticipationState.NEEDSACTION);

		List<Attendee> attendees = Lists.newArrayList(john, jane);
		return attendees;
	}
}
