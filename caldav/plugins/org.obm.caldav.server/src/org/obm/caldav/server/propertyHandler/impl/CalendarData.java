package org.obm.caldav.server.propertyHandler.impl;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.CalendarMultiGetPropertyHandler;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

public class CalendarData extends DavPropertyHandler implements CalendarMultiGetPropertyHandler {

	@Override
	public void appendPropertyValue(Element prop, Token t, DavRequest req) {
		
		
		CDATASection cdata = prop.getOwnerDocument().createCDATASection(
				"" + "BEGIN:VCALENDAR\r\n"
				+ "PRODID:-//Google Inc//Google Calendar 70.9054//EN\r\n"
						+ "VERSION:2.0\r\n" + "CALSCALE:GREGORIAN\r\n"
						+ "METHOD:PUBLISH\r\n"
						+ "X-WR-CALNAME:Thomas Cataldo\r\n"
						+ "X-WR-TIMEZONE:Europe/Paris\r\n"
						+ "BEGIN:VTIMEZONE\r\n" + "TZID:Europe/Paris\r\n"
						+ "X-LIC-LOCATION:Europe/Paris\r\n"
						+ "BEGIN:DAYLIGHT\r\n" + "TZOFFSETFROM:+0100\r\n"
						+ "TZOFFSETTO:+0200\r\n" + "TZNAME:CEST\r\n"
						+ "DTSTART:19700329T020000\r\n"
						+ "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n"
						+ "END:DAYLIGHT\r\n" + "BEGIN:STANDARD\r\n"
						+ "TZOFFSETFROM:+0200\r\n" + "TZOFFSETTO:+0100\r\n"
						+ "TZNAME:CET\r\n" + "DTSTART:19701025T030000\r\n"
						+ "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n"
						+ "END:STANDARD\r\n" + "END:VTIMEZONE\r\n"
						
						+ "BEGIN:VEVENT\r\n"
						+ "DTSTART:20090210T153000Z\r\n"
						+ "DTEND:20090210T163000Z\r\n"
						+ "DTSTAMP:20090410T142730Z\r\n"
						+ "UID:938A83E1D15246E3AB19A4F8DD3C168800000000000000000000000000000000\r\n"
						+ "CLASS:PRIVATE\r\n"
						+ "CREATED:20090210T105007Z\r\n"
						+ "DESCRIPTION:\r\n"
						+ "LAST-MODIFIED:20090407T090111Z\r\n"
						+ "LOCATION:\r\n"
						+ "SEQUENCE:1\r\n"
						+ "STATUS:CONFIRMED\r\n"
						+ "SUMMARY:Créé sûr iphone\r\n"
						+ "TRANSP:OPAQUE\r\n"
						+ "CATEGORIES:http://schemas.google.com/g/2005#event\r\n"
						+ "X-MOZ-LASTACK:20090407T085950Z\r\n"
						+ "X-LIC-ERROR;X-LIC-ERRORTYPE=VALUE-PARSE-ERROR:No value for DESCRIPTION prop\r\n"
						+ " erty. Removing entire property:\r\n"
						+ "X-MOZ-GENERATION:2\r\n"
						+ "BEGIN:VALARM\r\n"
						+ "ACTION:DISPLAY\r\n"
						+ "DESCRIPTION:This is an event reminder\r\n"
						+ "TRIGGER:-P0DT0H10M0S\r\n"
						+ "END:VALARM\r\n"
						+ "END:VEVENT\r\n"
													
						+ "END:VCALENDAR\r\n");
		prop.appendChild(cdata);
	}
	
	

	@Override
	public void appendCalendarMultiGetPropertyValue(Element prop, IProxy proxy,
			String eventId, String eventIcs) {
		Element val = DOMUtils.createElement(prop,"calendar-data");
		CDATASection cdata = prop.getOwnerDocument().createCDATASection(eventIcs);
		val.appendChild(cdata);
	}

}
