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

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.TransformerException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serializes calendar related items to XML
 * 
 * @author tom
 * 
 */
public class CalendarItemsWriterTest extends AbstractItemsWriter {

private CalendarItemsWriter writer;
	
	@Before
	public void initCalendarWriter(){
		writer = new CalendarItemsWriter();
	}
	
	@Test
	public void testGetEventString() throws TransformerException {
		Event ev = new Event();
		ev.setInternalEvent(true);
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		ev.setDate(cal.getTime());
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
		er.addException(ev.getDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);
		ev.setRecurrence(er);
		ev.setSequence(3);

		
		String eventS = writer.getEventString(ev);
		
		String xmlExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<event allDay=\"false\" isInternal=\"true\" sequence=\"3\" type=\"VEVENT\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">" +
		"<timeupdate>1292580000000</timeupdate>" +
		"<timecreate>1289988000000</timecreate>"+
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
		"<attendee displayName=\"John Do\" email=\"john@do.fr\" isOrganizer=\"true\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\"/>" +
		"<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" isOrganizer=\"false\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>" +
		"</attendees><recurrence days=\"\" freq=\"1\" kind=\"daily\">" +
		"<exceptions>" +
		"<exception>1295258400000</exception>" +
		"<exception>1292580000000</exception>" +
		"</exceptions><eventExceptions/>" +
		"</recurrence>" +
		"</event>";
		Assert.assertEquals(xmlExpected, eventS);
	}
	
	@Test
	public void appendEvent() throws TransformerException {
		Event ev = new Event();
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(1295258400000L);
		ev.setDate(cal.getTime());
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
		er.addException(ev.getDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);
		ev.setRecurrence(er);
		ev.setSequence(6);
		
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/event.xsd", "event");
		Element root = doc.getDocumentElement();
		writer.appendEvent(root, ev);
		
		String xmlExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<event allDay=\"false\" isInternal=\"false\" sequence=\"6\" type=\"VEVENT\" xmlns=\"http://www.obm.org/xsd/sync/event.xsd\">" +
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
		"<attendee displayName=\"John Do\" email=\"john@do.fr\" isOrganizer=\"false\" percent=\"0\" required=\"CHAIR\" state=\"NEEDS-ACTION\"/>" +
		"<attendee displayName=\"noIn TheDatabase\" email=\"notin@mydb.com\" isOrganizer=\"false\" percent=\"0\" required=\"OPT\" state=\"ACCEPTED\"/>" +
		"</attendees><recurrence days=\"\" freq=\"1\" kind=\"daily\">" +
		"<exceptions>" +
		"<exception>1295258400000</exception>" +
		"<exception>1297936800000</exception>" +
		"</exceptions><eventExceptions/>" +
		"</recurrence>" +
		"</event>";
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtils.serialize(doc, out);
		
		Assert.assertEquals(xmlExpected, out.toString());
		
	}

}
