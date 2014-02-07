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
package org.obm.sync.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.calendar.Participation.State;
import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

/**
 * Serializes calendar related items to XML
 */

public class CalendarItemsWriterTest extends AbstractItemsWriter {

private CalendarItemsWriter writer;

	private Date lastSync;
	
	@Before
	public void initCalendarWriter(){
		writer = new CalendarItemsWriter();
		XMLUnit.setIgnoreWhitespace(true);
		lastSync = DateUtils.date("2012-03-05T14:26:29");
	}

	@Test
	public void testGetXMLDocumentFromEventChangesWithNoChange() throws SAXException, IOException, TransformerException {
		EventChanges eventChanges = EventChanges.builder()
										.lastSync(lastSync)
										.build();

		String expectedXML = loadXmlFile("OBMFULL-3301_WithNoChange.xml");
		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromEventChangesWithRemovedElements() throws SAXException, IOException, TransformerException {
		DeletedEvent deletedEvent1 = DeletedEvent.builder()
										.eventObmId(1)
										.eventExtId("123")
										.build();
		DeletedEvent deletedEvent2 = DeletedEvent.builder()
										.eventObmId(2)
										.eventExtId("456")
										.build();
		
		EventChanges eventChanges = EventChanges.builder()
										.lastSync(lastSync)
										.deletes(ImmutableSet.of(deletedEvent1, deletedEvent2))
										.build();

		String expectedXML = loadXmlFile("OBMFULL-3301_WithRemovedElements.xml");
		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromEventChangesWithUpdatedElements() throws SAXException, IOException, TransformerException {
		Event updatedEvent = getFakeEvent(Participation.needsAction());
		Event eventException = updatedEvent.getOccurrence(updatedEvent.getStartDate());
		updatedEvent.addEventException(eventException);

		List<Event> updated = Lists.newArrayList(updatedEvent);
		
		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.updates(updated)
				.build();

		String expectedXML = loadXmlFile("OBMFULL-3301_WithUpdatedElements.xml");
		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		String serialize = DOMUtils.serialize(resultDocument);
		XMLAssert.assertXMLEqual(expectedXML, serialize);
	}

	@Test
	public void testGetXMLDocumentFromEventChangesWithParticipationChangesElements() throws SAXException, IOException, TransformerException {
		List<ParticipationChanges> participationUpdated = getFakeListOfParticipationChanges();
		
		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.participationChanges(participationUpdated)
				.build();

		String expectedXML = loadXmlFile("OBMFULL-3301_WithParticipationChangesElements.xml");
		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromEventChangesWithParticipationChangesElementsWithNullParticipation() throws SAXException, IOException, TransformerException {
		List<ParticipationChanges> participationUpdated = getFakeListOfParticipationChangesWithNullParticipation();
		
		EventChanges eventChanges = EventChanges.builder()
				.lastSync(lastSync)
				.participationChanges(participationUpdated)
				.build();

		String expectedXML = loadXmlFile("OBMFULL-3301_WithParticipationChangesElementsWithNullParticipation.xml");
		Document resultDocument = writer.getXMLDocumentFrom(eventChanges);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromEvent() throws SAXException, IOException, TransformerException {
		Event event = getFakeEvent(Participation.needsAction());

		String expectedXML = loadXmlFile("OBMFULL-3301_SimpleEvent.xml");
		Document resultDocument = writer.getXMLDocumentFrom(event);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromAnonymizedEvent() throws SAXException, IOException, TransformerException {
		Event event = new Event();
		
		event.setExtId(new EventExtId("Ext"));
		event.setTitle("Anonymized");
		event.addAttendee(UserAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build());
		event.setAnonymized(true);

		String expectedXML = loadXmlFile("AnonymizedSimpleEvent.xml");
		Document resultDocument = writer.getXMLDocumentFrom(event);

		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromNotAnonymizedEvent() throws SAXException, IOException, TransformerException {
		Event event = new Event();

		event.setExtId(new EventExtId("Ext"));
		event.setTitle("Not Anonymized");
		event.addAttendee(UserAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build());
		event.setAnonymized(false);

		String expectedXML = loadXmlFile("NotAnonymizedSimpleEvent.xml");
		Document resultDocument = writer.getXMLDocumentFrom(event);

		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentWithNoAnonymizedAttributeEvent() throws SAXException, IOException, TransformerException {
		Event event = new Event();

		event.setExtId(new EventExtId("Ext"));
		event.setTitle("Not Anonymized");
		event.addAttendee(UserAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build());

		String expectedXML = loadXmlFile("NotAnonymizedSimpleEvent.xml");
		Document resultDocument = writer.getXMLDocumentFrom(event);

		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromListOfEvent() throws SAXException, IOException, TransformerException {
		Event event = getFakeEvent(Participation.needsAction());
		Event eventClone = event.clone();
		List<Event> events = Lists.newArrayList(event, eventClone);

		String expectedXML = loadXmlFile("OBMFULL-3301_ListOfEvent.xml");
		Document resultDocument = writer.getXMLDocumentFrom(events);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetResourceInfo() throws IOException, SAXException, TransformerException {
		ResourceInfo resourceInfo = ResourceInfo.builder().id(42).name("myresource")
				.mail("res-42@somedomain.com").description("mydescription").read(true).write(false)
				.domainName("domain").build();
		String expectedXML = loadXmlFile("ResourceInfo.xml");
		Document resultDocument = writer.getXMLDocumentFrom(resourceInfo);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetResourceInfoWithoutDescription() throws IOException, SAXException, TransformerException {
		ResourceInfo resourceInfo = ResourceInfo.builder().id(42).name("myresource")
				.mail("res-42@somedomain.com").read(true).write(false)
				.domainName("domain").build();

		String expectedXML = loadXmlFile("ResourceInfoWithoutDescription.xml");
		Document resultDocument = writer.getXMLDocumentFrom(resourceInfo);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	private Event getFakeEvent(Participation participation) {
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
		Attendee at = UserAttendee
				.builder()
				.displayName("John Do")
				.email("john@do.fr")
				.participation(participation)
				.participationRole(ParticipationRole.CHAIR)
				.asOrganizer()
				.build();
		
		la.add(at);
		
		at = ContactAttendee
				.builder()
				.displayName("noIn TheDatabase")
				.email("notin@mydb.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.OPT)
				.build();

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

	private List<ParticipationChanges> getFakeListOfParticipationChanges() {
		ParticipationChanges participationChanges1 = ParticipationChanges.builder()
			.eventExtId("123")
			.eventObmId(1)
			.attendees(getFakeListOfAttendee())
			.build();
		
		ParticipationChanges participationChanges2 = ParticipationChanges.builder()
			.eventExtId("456")
			.eventObmId(2)
			.recurrenceId("789")
			.attendees(getFakeListOfAttendee())
			.build();

		List<ParticipationChanges> participationUpdated = Lists.newArrayList(participationChanges1, participationChanges2);
		return participationUpdated;
	}

	private List<ParticipationChanges> getFakeListOfParticipationChangesWithNullParticipation() {
		ParticipationChanges participationChanges1 = ParticipationChanges.builder()
			.eventExtId("123")
			.eventObmId(1)
			.attendees(getFakeListOfAttendeeWithNullParticipation())
			.build();
		
		ParticipationChanges participationChanges2 = ParticipationChanges.builder()
			.eventExtId("456")
			.eventObmId(2)
			.recurrenceId("789")
			.attendees(getFakeListOfAttendeeWithNullParticipation())
			.build();

		List<ParticipationChanges> participationUpdated = Lists.newArrayList(participationChanges1, participationChanges2);
		return participationUpdated;
	}


	private List<Attendee> getFakeListOfAttendee() {
		Attendee john = UserAttendee.builder().email("john@doe").participation(Participation.accepted()).build();
		Attendee jane = UserAttendee
				.builder().email("jane@doe")
				.participation(Participation.builder().state(State.NEEDSACTION).comment("this is a new comment").build())
				.build();

		List<Attendee> attendees = Lists.newArrayList(john, jane);
		
		return attendees;
	}

	private List<Attendee> getFakeListOfAttendeeWithNullParticipation() {
		Attendee john = UserAttendee.builder().email("john@doe").build();
		Attendee jane = UserAttendee
				.builder().email("jane@doe")
				.participation(Participation.builder().state(State.NEEDSACTION).comment("this is a new comment").build())
				.build();

		List<Attendee> attendees = Lists.newArrayList(john, jane);
		
		return attendees;
	}

	private String loadXmlFile(String filename) throws IOException {
		InputStream inputStream = ClassLoader.getSystemClassLoader()
				.getResourceAsStream(filename);

		String fileContent = CharStreams.toString(new InputStreamReader(inputStream));
		fileContent = fileContent.replaceAll("\n|\t", "");
		return fileContent;
	}
}
