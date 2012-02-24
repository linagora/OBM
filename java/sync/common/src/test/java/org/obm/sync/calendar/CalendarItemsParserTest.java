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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.FactoryConfigurationError;

import junit.framework.Assert;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class CalendarItemsParserTest {
	
	private CalendarItemsParser parser;
	
	@Before
	public void initCalendarParser(){
		parser = new CalendarItemsParser();
	}

	@Test
	public void testParseInternalEventTrue() throws SAXException, IOException, FactoryConfigurationError {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<event allDay=\"false\" id=\"\" type=\"VEVENT\" isInternal=\"true\" sequence=\"0\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">" +
		"<extId>2bf7db53-8820-4fe5-9a78-acc6d3262149</extId>" +
		"<opacity>OPAQUE</opacity>" +
		"<title>fake rdv</title>" +
		"<owner>john@do.fr</owner>" +
		"<tz>Europe/Paris</tz>" +
		"<date>1295258400000</date>" +
		"<duration>3600</duration>" +
		"<location>tlse</location>" +
		"<alert>60</alert>" +
		"<priority>0</priority>" +
		"<privacy>0</privacy>" +
		"<attendees>" +
		"<attendee displayName=\"John Do\" email=\"john@do.fr\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\" isOrganizer=\"true\"/>" +
		"<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\" isOrganizer=\"false\"/>" +
		"<attendee displayName=\"noIn TheDatabase2\" email=\"notin2@mydb.com\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>" +
		"</attendees><recurrence days=\"\" freq=\"1\" kind=\"daily\">" +
		"<exceptions>" +
		"<exception>1295258400000</exception>" +
		"</exceptions><eventExceptions/>" +
		"</recurrence>" +
		"</event>";
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Event ev = parser.parseEvent(doc.getDocumentElement());

		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		
		Assert.assertTrue(ev.isInternalEvent());
		Assert.assertEquals(cal.getTime(), ev.getDate());
		Assert.assertEquals(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149"), ev.getExtId());
		Assert.assertEquals("fake rdv", ev.getTitle());
		Assert.assertEquals("john@do.fr", ev.getOwner());
		Assert.assertEquals("john@do.fr", ev.getOwnerDisplayName());
		Assert.assertEquals(3600, ev.getDuration());
		Assert.assertEquals("tlse", ev.getLocation());
		Assert.assertEquals(new Integer(60), ev.getAlert());
		Assert.assertEquals(1295262000000L,ev.getEndDate().getTime());
		
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		at = new Attendee();
		at.setDisplayName("noIn TheDatabase");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		at = new Attendee();
		at.setDisplayName("noIn TheDatabase2");
		at.setEmail("notin2@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		Assert.assertNotNull(ev.getRecurrence());
		Assert.assertEquals(RecurrenceKind.daily, ev.getRecurrence().getKind());
		Assert.assertEquals(1, ev.getRecurrence().getFrequence());
		Assertions.assertThat(ev.getRecurrence().getExceptions()).containsOnly(ev.getDate());
		Assert.assertNull(ev.getRecurrence().getEnd());
	}
	
	@Test
	public void testParseInternalEventFalse() throws SAXException, IOException, FactoryConfigurationError {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<event allDay=\"false\" id=\"\" type=\"VEVENT\" isInternal=\"false\" sequence=\"0\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">" +
		"<extId>2bf7db53-8820-4fe5-9a78-acc6d3262149</extId>" +
		"<opacity>OPAQUE</opacity>" +
		"<title>fake rdv</title>" +
		"<owner>john@do.fr</owner>" +
		"<tz>Europe/Paris</tz>" +
		"<date>1295258400000</date>" +
		"<duration>3600</duration>" +
		"<location>tlse</location>" +
		"<alert>60</alert>" +
		"<priority>0</priority>" +
		"<privacy>0</privacy>" +
		"<attendees>" +
		"<attendee displayName=\"John Do\" email=\"john@do.fr\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\" isOrganizer=\"true\"/>" +
		"<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\" isOrganizer=\"false\"/>" +
		"<attendee displayName=\"noIn TheDatabase2\" email=\"notin2@mydb.com\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>" +
		"</attendees><recurrence days=\"\" freq=\"1\" kind=\"daily\">" +
		"<exceptions>" +
		"<exception>1295258400000</exception>" +
		"</exceptions><eventExceptions/>" +
		"</recurrence>" +
		"</event>";
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Event ev = parser.parseEvent(doc.getDocumentElement());

		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		
		Assert.assertFalse(ev.isInternalEvent());
		Assert.assertEquals(cal.getTime(), ev.getDate());
		Assert.assertEquals(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149"), ev.getExtId());
		Assert.assertEquals("fake rdv", ev.getTitle());
		Assert.assertEquals("john@do.fr", ev.getOwner());
		Assert.assertEquals("john@do.fr", ev.getOwnerDisplayName());
		Assert.assertEquals(3600, ev.getDuration());
		Assert.assertEquals("tlse", ev.getLocation());
		Assert.assertEquals(new Integer(60), ev.getAlert());
		Assert.assertEquals(1295262000000L,ev.getEndDate().getTime());
		
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		at = new Attendee();
		at.setDisplayName("noIn TheDatabase");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		at = new Attendee();
		at.setDisplayName("noIn TheDatabase2");
		at.setEmail("notin2@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		Assert.assertNotNull(ev.getRecurrence());
		Assert.assertEquals(RecurrenceKind.daily, ev.getRecurrence().getKind());
		Assert.assertEquals(1, ev.getRecurrence().getFrequence());
		Assertions.assertThat(ev.getRecurrence().getExceptions()).containsOnly(ev.getDate());
		Assert.assertNull(ev.getRecurrence().getEnd());
	}
	
	@Test
	public void testParseExternalEvent() throws SAXException, IOException, FactoryConfigurationError {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<event allDay=\"false\" id=\"\" type=\"VEVENT\" isInternal=\"false\" sequence=\"0\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">" +
		"<timeupdate>1292580000000</timeupdate>" +
		"<timecreate>1289988000000</timecreate>" +
		"<extId>2bf7db53-8820-4fe5-9a78-acc6d3262149</extId>" +
		"<opacity>OPAQUE</opacity>" +
		"<title>fake rdv</title>" +
		"<owner>john@do.fr</owner>" +
		"<tz>Europe/Paris</tz>" +
		"<date>1295258400000</date>" +
		"<duration>3600</duration>" +
		"<location>tlse</location>" +
		"<alert>60</alert>" +
		"<priority>0</priority>" +
		"<privacy>0</privacy>" +
		"<attendees>" +
		"<attendee displayName=\"John Do\" email=\"john@do.fr\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\" isOrganizer=\"true\"/>" +
		"<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>" +
		"</attendees><recurrence days=\"\" freq=\"1\" kind=\"daily\">" +
		"<exceptions>" +
		"<exception>1295258400000</exception>" +
		"</exceptions><eventExceptions/>" +
		"</recurrence>" +
		"</event>";
		
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Event ev = parser.parseEvent(doc.getDocumentElement());

		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		
		
		Assert.assertFalse(ev.isInternalEvent());
		Assert.assertEquals(cal.getTime(), ev.getDate());
		Assert.assertEquals(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149"), ev.getExtId());
		Assert.assertEquals("fake rdv", ev.getTitle());
		Assert.assertEquals("john@do.fr", ev.getOwner());
		Assert.assertEquals("john@do.fr", ev.getOwnerDisplayName());
		Assert.assertEquals(3600, ev.getDuration());
		Assert.assertEquals("tlse", ev.getLocation());
		Assert.assertEquals(new Integer(60), ev.getAlert());
		Assert.assertEquals(1295262000000L,ev.getEndDate().getTime());
		
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		at = new Attendee();
		at.setDisplayName("noIn TheDatabase");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		Assert.assertTrue(ev.getAttendees().contains(at));
		
		Assert.assertNotNull(ev.getRecurrence());
		Assert.assertEquals(RecurrenceKind.daily, ev.getRecurrence().getKind());
		Assert.assertEquals(1, ev.getRecurrence().getFrequence());
		Assertions.assertThat(ev.getRecurrence().getExceptions()).containsOnly(ev.getDate());
		Assert.assertNull(ev.getRecurrence().getEnd());
		
		Assert.assertEquals(1289988000000L,ev.getTimeCreate().getTime());
		Assert.assertEquals(1292580000000L,ev.getTimeUpdate().getTime());
	}

	@Test
	public void testParseExternalEventWithEmptyExtId() throws SAXException, IOException, FactoryConfigurationError {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<event allDay=\"false\" id=\"\" type=\"VEVENT\" isInternal=\"false\" sequence=\"0\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">" +
		"<timeupdate>1292580000000</timeupdate>" +
		"<timecreate>1289988000000</timecreate>" +
		"<extId></extId>" +
		"<opacity>OPAQUE</opacity>" +
		"<title>fake rdv</title>" +
		"<owner>john@do.fr</owner>" +
		"<tz>Europe/Paris</tz>" +
		"<date>1295258400000</date>" +
		"<duration>3600</duration>" +
		"<location>tlse</location>" +
		"<alert>60</alert>" +
		"<priority>0</priority>" +
		"<privacy>0</privacy>" +
		"<attendees/>" +
		"</event>";
		
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Event ev = parser.parseEvent(doc.getDocumentElement());

		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		
		
		Assert.assertFalse(ev.isInternalEvent());
		Assert.assertEquals(cal.getTime(), ev.getDate());
		Assert.assertNull(ev.getExtId().getExtId());
		Assert.assertEquals("fake rdv", ev.getTitle());
		Assert.assertEquals("john@do.fr", ev.getOwner());
		Assert.assertEquals("john@do.fr", ev.getOwnerDisplayName());
		Assert.assertEquals(3600, ev.getDuration());
		Assert.assertEquals("tlse", ev.getLocation());
		Assert.assertEquals(new Integer(60), ev.getAlert());
		Assert.assertEquals(1295262000000L,ev.getEndDate().getTime());
		Assert.assertEquals(1289988000000L,ev.getTimeCreate().getTime());
		Assert.assertEquals(1292580000000L,ev.getTimeUpdate().getTime());
	}
	
	@Test
	public void testParseNullRecurrence() throws SAXException, IOException, FactoryConfigurationError {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<event allDay=\"false\" id=\"\" type=\"VEVENT\" sequence=\"0\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">" +
		"<timeupdate>1292580000000</timeupdate>" +
		"<timecreate>1289988000000</timecreate>" +
		"<extId>2bf7db53-8820-4fe5-9a78-acc6d3262149</extId>" +
		"<opacity>OPAQUE</opacity>" +
		"<title>fake rdv</title>" +
		"<owner>john@do.fr</owner>" +
		"<tz>Europe/Paris</tz>" +
		"<date>1295258400000</date>" +
		"<duration>3600</duration>" +
		"<location>tlse</location>" +
		"<alert>60</alert>" +
		"<priority>0</priority>" +
		"<privacy>0</privacy>" +
		"<attendees>" +
		"<attendee displayName=\"John Do\" email=\"john@do.fr\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\" isOrganizer=\"true\"/>" +
		"</attendees><recurrence days=\"\" freq=\"1\">" +
		"<exceptions>" +
		"<exception>1295258400000</exception>" +
		"</exceptions><eventExceptions/>" +
		"</recurrence>" +
		"</event>";
		
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Event ev = parser.parseEvent(doc.getDocumentElement());
		
		Assert.assertEquals(RecurrenceKind.none, ev.getRecurrence().getKind());
		Assert.assertEquals(null, ev.getRecurrence().getDays());
	}

	@Test
	public void testEventException() throws SAXException, IOException {
		String xml ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<event id=\"316\" isInternal=\"true\" sequence=\"1\" allDay=\"false\" type=\"VEVENT\">" +
			"  <extId>a7db3cd5-adf3-42f4-95f3-d0a7a9c01aa3</extId>" +
			"  <owner>test2</owner>" +
			"  <priority>2</priority>" +
			"  <privacy>0</privacy>" +
			"  <date>1313994600000</date>" +
			"  <duration>3600</duration>" +
			"  <title>New Event from TBird</title>" +
			"  <alert>-1</alert>" +
			"  <opacity>OPAQUE</opacity>" +
			"  <ownerEmail>test2@par.lng</ownerEmail>" +
			"  <attendees>" +
			"    <attendee email=\"test2@par.lng\" displayName=\"test2 test2\" state=\"NEEDS-ACTION\" required=\"REQ\" isOrganizer=\"true\"/>" +
			"  </attendees>" +
			"  <recurrence kind=\"daily\" freq=\"1\" days=\"\">" +
			"    <eventExceptions>" +
			"      <eventException allDay=\"false\" type=\"VEVENT\">" +
			"        <recurrenceId>1314081000000</recurrenceId>" +
			"        <owner>test2</owner>" +
			"        <priority>2</priority>" +
			"        <privacy>0</privacy>" +
			"        <date>1314077400000</date>" +
			"        <duration>3600</duration>" +
			"        <title>New Event from TBird</title>" +
			"        <alert>-1</alert>" +
			"        <opacity>OPAQUE</opacity>" +
			"        <ownerEmail>test2@par.lng</ownerEmail>" +
			"        <attendees>" +
			"          <attendee email=\"test@par.lng\" displayName=\"test test\" state=\"NEEDS-ACTION\" required=\"OPT\"/>" +
			"          <attendee email=\"test2@par.lng\" displayName=\"test2 test2\" state=\"NEEDS-ACTION\" required=\"REQ\" isOrganizer=\"true\"/>" +
			"        </attendees>" +
			"        <recurrence kind=\"none\" freq=\"1\" days=\"\" end=\"1314177107089\"/>" +
			"      </eventException>" +
			"    </eventExceptions>" +
			"  </recurrence>" +
			"</event>";
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Event ev = parser.parseEvent(doc.getDocumentElement());

		Assert.assertEquals(1, ev.getRecurrence().getEventExceptions().size());

		Event evEx = ev.getRecurrence().getEventExceptions().get(0);
		Assert.assertEquals(null, evEx.getObmId());
		Assert.assertEquals(new EventExtId("a7db3cd5-adf3-42f4-95f3-d0a7a9c01aa3"), evEx.getExtId());
		Assert.assertEquals(false, evEx.isAllday());
		Assert.assertEquals(EventType.VEVENT, evEx.getType());
		Assert.assertEquals(1314081000000L, evEx.getRecurrenceId().getTime());
		Assert.assertEquals("test2", evEx.getOwner());
		Assert.assertEquals("test2", evEx.getOwnerDisplayName());
		Assert.assertEquals(new Integer(2), evEx.getPriority());
		Assert.assertEquals(0, evEx.getPrivacy());
		Assert.assertEquals(1314077400000L, evEx.getDate().getTime());
		Assert.assertEquals(3600, evEx.getDuration());
		Assert.assertEquals(new Integer(-1), evEx.getAlert());
		Assert.assertEquals(EventOpacity.OPAQUE, evEx.getOpacity());
		Assert.assertEquals("test2@par.lng", evEx.getOwnerEmail());
		Assert.assertEquals(1, evEx.getSequence());
		Assert.assertEquals(2, evEx.getAttendees().size());
		Assert.assertEquals("0000000", ev.getRecurrence().getDays());
	}
}
