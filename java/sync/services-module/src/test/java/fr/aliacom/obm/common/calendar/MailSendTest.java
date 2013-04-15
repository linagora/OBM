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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.UserAttendee;

import com.ctc.wstx.io.CharsetNames;
import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
public class MailSendTest {

	private static final String ICS_METHOD = "REQUEST";
	private static final String ICS = "ics";
	private static final String BODY_HTML = "html";
	private static final String BODY_TEXT = "text";
	private static final String SUBJECT = "subject";

	@Test
	public void testBasicEventEmail() throws MessagingException, IOException {
		Attendee attendee1 = newAttendee("attendee1");
		EventMail eventMail = newEventMail(ImmutableList.of(attendee1));		
		String content = writeEventMail(eventMail);
		
		assertThat(content).contains("Subject: " + SUBJECT).contains("To: " + attendee1.getEmail());
	}
	
	@Test
	public void testCustomObmHeaderIsPresent() throws Exception {
		EventMail eventMail = newEventMail(ImmutableList.of(newAttendee("attendee1")));
		String content = writeEventMail(eventMail);
		
		assertThat(content).contains(EventMail.X_OBM_NOTIFICATION_EMAIL);
	}
	
	@Test
	public void testCalendarEncodingBase64() throws Exception {
		assertTextCalendarContentTransferEncodingIsCorrect(CalendarEncoding.Base64);
	}
	
	@Test
	public void testCalendarEncodingQuotedPrintable() throws Exception {
		assertTextCalendarContentTransferEncodingIsCorrect(CalendarEncoding.QuotedPrintable);
	}
	
	@Test
	public void testCalendarEncodingSevenBit() throws Exception {
		assertTextCalendarContentTransferEncodingIsCorrect(CalendarEncoding.SevenBit);
	}
	
	private void assertTextCalendarContentTransferEncodingIsCorrect(CalendarEncoding encoding) throws Exception {
		String icsContent = IOUtils.toString(getClass().getResourceAsStream("meetingWithOneAttendee.ics"));
		EventMail eventMail = new EventMail(new InternetAddress("sender@test"), ImmutableList.of(newAttendee("attendee1")), SUBJECT, BODY_TEXT, BODY_HTML, icsContent, ICS_METHOD, encoding);
		String content = writeEventMail(eventMail);
		LineIterator lineIterator = new LineIterator(new StringReader(content));
		boolean textCalendarFound = false;
		
		while (lineIterator.hasNext()) {
			if (lineIterator.next().contains("Content-Type: text/calendar")) {
				textCalendarFound = true;
				break;
			}
		}
		
		assertThat(textCalendarFound).isTrue();
		assertThat(lineIterator.next()).contains("Content-Transfer-Encoding: " + encoding.getValue());
	}
	
	private String writeEventMail(EventMail eventMail) throws IOException, MessagingException {
		MimeMessage mail = eventMail.buildMimeMail(Session.getDefaultInstance(new Properties()));
		ByteArrayOutputStream mailByteStream = new ByteArrayOutputStream();
		
		mail.writeTo(mailByteStream);
		
		return new String(mailByteStream.toByteArray(), CharsetNames.CS_UTF8);
	}
	
	private EventMail newEventMail(List<Attendee> attendees) throws AddressException {
		return newEventMail(attendees, null);
	}
	
	private EventMail newEventMail(List<Attendee> attendees, CalendarEncoding calendarEncoding) throws AddressException {
		return new EventMail(new InternetAddress("sender@test"), attendees, SUBJECT, BODY_TEXT, BODY_HTML, ICS, ICS_METHOD, calendarEncoding);
	}
	
	private Attendee newAttendee(String name) {
		return UserAttendee.builder().email(name + "@test").build();
	}
}
