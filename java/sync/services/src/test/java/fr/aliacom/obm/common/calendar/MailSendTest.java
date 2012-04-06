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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.internal.matchers.StringContains;
import org.obm.sync.calendar.Attendee;

import com.ctc.wstx.io.CharsetNames;
import com.google.common.collect.ImmutableList;

import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class MailSendTest {

	@Test
	public void testBasicEventEmail() throws MessagingException, IOException {
		InternetAddress from = new InternetAddress("sender@test");
		Attendee attendee1 = new Attendee();
		attendee1.setEmail("attendee1@test");
		ImmutableList<Attendee> attendees = ImmutableList.of(attendee1);
		String subject = "subject";
		String bodyTxt = "text";
		String bodyHtml = "html";
		String icsContent = "ics";
		EventMail eventMail = new EventMail(from, attendees, subject, bodyTxt, bodyHtml, icsContent, "REQUEST");
		MimeMessage mail = eventMail.buildMimeMail(Session.getDefaultInstance(new Properties()));
		ByteArrayOutputStream mailByteStream = new ByteArrayOutputStream();
		mail.writeTo(mailByteStream);
		String content = new String(mailByteStream.toByteArray(), CharsetNames.CS_UTF8);
		Assert.assertThat(content, new StringContains("Subject: subject"));
		Assert.assertThat(content, new StringContains("To: " + attendee1.getEmail()));
	}
	
}
