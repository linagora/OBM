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
package fr.aliacom.obm.common.calendar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.obm.sync.calendar.Attendee;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import fr.aliacom.obm.common.ObmSyncVersion;

public class EventMail {

	public static final String X_OBM_NOTIFICATION_EMAIL = "X-OBM-NOTIFICATION-EMAIL";
	public static final String X_OBM_NOTIFICATION_EMAIL_VALUE_IF_NO_VERSION = "0";
	
	private final Address from;
	private final List<Attendee> recipients;
	private final String subject;
	private final String bodyTxt;
	private final String bodyHtml;
	private String icsContent;
	private String icsMethod;
	private CalendarEncoding calendarEncoding;
	
	public EventMail(Address from, List<Attendee> recipients, String subject, String bodyTxt, String bodyHtml) {
		this(from, recipients, subject, bodyTxt, bodyHtml, null, null, CalendarEncoding.Auto);
	}
	
	public EventMail(Address from, List<Attendee> recipients, String subject,
			String bodyTxt, String bodyHtml, String icsContent, String icsMethod, CalendarEncoding calendarEncoding) {
				this.from = from;
				this.recipients = recipients;
				this.subject = subject;
				this.bodyTxt = bodyTxt;
				this.bodyHtml = bodyHtml;
				this.icsContent = icsContent;
				this.icsMethod = icsMethod;
				this.calendarEncoding = Objects.firstNonNull(calendarEncoding, CalendarEncoding.Auto);
	}
	
	public MimeMessage buildMimeMail(Session session) throws MessagingException, IOException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(from);
		message.setRecipients(RecipientType.TO, getRecipients());
		message.setSubject(subject);
		message.addHeader(X_OBM_NOTIFICATION_EMAIL, getObmSyncVersion());
		if(icsContent != null){
			message.setContent(buildParts());
		} else {
			message.setContent(buildAlternativePart());
		}
		return message;
	}

	private String getObmSyncVersion() {
		try {
			return ObmSyncVersion.current().toString();
		}
		catch (Exception e) {
			return X_OBM_NOTIFICATION_EMAIL_VALUE_IF_NO_VERSION;
		}
	}
	
	private Address[] getRecipients() throws UnsupportedEncodingException {
		List<Address> addresses = Lists.newArrayList();
		for (Attendee recipient: recipients) {
			addresses.add(new InternetAddress(recipient.getEmail(), recipient.getDisplayName()));
		}
		return addresses.toArray(new Address[0]);
	}
	
	private MimeMultipart buildParts() throws MessagingException, IOException {
		MimeMultipart mainPart = new MimeMultipart("mixed");
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(buildAlternativePart());
		mainPart.addBodyPart(mimeBodyPart);
		mainPart.addBodyPart(createIcsPart());
		return mainPart;
	}
	
	private BodyPart createIcsPart() throws MessagingException, IOException {
		MimeBodyPart part = new MimeBodyPart();
    	ByteArrayDataSource fds = new ByteArrayDataSource(icsContent, "application/ics");   	
        part.setDataHandler(new DataHandler(fds));
        part.addHeader("Content-Transfer-Encoding", "base64");
        part.setFileName("meeting.ics");
		return part;
	}

	private MimeMultipart buildAlternativePart() throws MessagingException {
		MimeMultipart alternativePart = new MimeMultipart("alternative");
		alternativePart.addBodyPart(createTextPart());
		alternativePart.addBodyPart(createHtmlPart());
		if(icsContent != null){
			alternativePart.addBodyPart(createCalendarPart());
		}
		return alternativePart;
	}
	
	private BodyPart createCalendarPart() throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setText(icsContent);
		part.setHeader("Content-Type", "text/calendar; charset=UTF-8; method=" + icsMethod + ";");
		
		if (!CalendarEncoding.Auto.equals(calendarEncoding)) {
			part.setHeader("Content-Transfer-Encoding", calendarEncoding.getValue());
		}
		
		return part;
	}

	private BodyPart createHtmlPart() throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setText(bodyHtml, Charsets.UTF_8.displayName(), "html");
		return part;
	}

	private MimeBodyPart createTextPart() throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setText(bodyTxt, Charsets.UTF_8.displayName(), "plain");
		return part;
	}
	
	public void setIcsContent(String icsContent) {
		this.icsContent = icsContent;
	}
	
	public void setIcsMethod(String icsMethod) {
		this.icsMethod = icsMethod;
	}
	
}
