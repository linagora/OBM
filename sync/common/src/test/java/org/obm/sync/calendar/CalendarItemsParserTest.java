package org.obm.sync.calendar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.FactoryConfigurationError;

import junit.framework.Assert;

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
		Assert.assertEquals("2bf7db53-8820-4fe5-9a78-acc6d3262149", ev.getExtId());
		Assert.assertEquals("fake rdv", ev.getTitle());
		Assert.assertEquals("john@do.fr", ev.getOwner());
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
		Assert.assertEquals(ev.getDate(), ev.getRecurrence().getExceptions()[0]);
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
		Assert.assertEquals("2bf7db53-8820-4fe5-9a78-acc6d3262149", ev.getExtId());
		Assert.assertEquals("fake rdv", ev.getTitle());
		Assert.assertEquals("john@do.fr", ev.getOwner());
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
		Assert.assertEquals(ev.getDate(), ev.getRecurrence().getExceptions()[0]);
		Assert.assertNull(ev.getRecurrence().getEnd());
	}
	
	@Test
	public void testParseExternalEvent() throws SAXException, IOException, FactoryConfigurationError {
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
		
		
		Assert.assertTrue(ev.isInternalEvent());
//		Assert.assertFalse(ev.isInternalEvent());
		Assert.assertEquals(cal.getTime(), ev.getDate());
		Assert.assertEquals("2bf7db53-8820-4fe5-9a78-acc6d3262149", ev.getExtId());
		Assert.assertEquals("fake rdv", ev.getTitle());
		Assert.assertEquals("john@do.fr", ev.getOwner());
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
		Assert.assertEquals(ev.getDate(), ev.getRecurrence().getExceptions()[0]);
		Assert.assertNull(ev.getRecurrence().getEnd());
		
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
	}
	
}
