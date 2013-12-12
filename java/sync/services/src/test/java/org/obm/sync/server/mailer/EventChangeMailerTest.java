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
package org.obm.sync.server.mailer;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;

import org.apache.commons.io.IOUtils;
import org.easymock.Capture;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.icalendar.Ical4jHelper;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Comment;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.date.DateProvider;
import org.obm.sync.server.template.ITemplateLoader;
import org.obm.sync.server.template.TemplateLoaderFreeMarkerImpl;
import org.obm.sync.services.AttendeeService;
import org.slf4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fr.aliacom.obm.ServicesToolBox;
import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(SlowFilterRunner.class)
public abstract class EventChangeMailerTest {

	protected static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");

	private EventChangeMailer eventChangeMailer;
	protected abstract EventChangeMailer newEventChangeMailer();
	protected abstract Locale getLocale();
	protected abstract ArrayList<String> getInvitationPlainMessage();
	protected abstract ArrayList<String> getInvitationHtmlMessage();
	protected abstract ArrayList<String> getUpdatePlainMessage();
	protected abstract ArrayList<String> getUpdateHtmlMessage();
	protected abstract ArrayList<String> getCancelPlainMessage();
	protected abstract ArrayList<String> getCancelHtmlMessage();
	protected abstract ArrayList<String> getRecurrentInvitationPlainMessage();
	protected abstract ArrayList<String> getRecurrentInvitationHtmlMessage();
	protected abstract ArrayList<String> getRecurrentUpdatePlainMessage();
	protected abstract ArrayList<String> getRecurrentUpdateHtmlMessage();
	protected abstract ArrayList<String> getNonRecurrentToRecurrentUpdatePlainMessage();
	protected abstract ArrayList<String> getNonRecurrentToRecurrentUpdateHtmlMessage();
	protected abstract ArrayList<String> getRecurrentToNonRecurrentUpdatePlainMessage();
	protected abstract ArrayList<String> getRecurrentToNonRecurrentUpdateHtmlMessage();
	protected abstract ArrayList<String> getRecurrentCancelPlainMessage();
	protected abstract ArrayList<String> getRecurrentCancelHtmlMessage();
	protected abstract ArrayList<String> getChangeParticipationPlainMessage();
	protected abstract ArrayList<String> getChangeParticipationHtmlMessage();
	protected abstract String getNewEventSubject();
	protected abstract String getNewRecurrentEventSubject();
	protected abstract String getCancelEventSubject();
	protected abstract String getCancelRecurrentEventSubject();
	protected abstract String getUpdateEventSubject();
	protected abstract String getUpdateRecurrentEventSubject();
	protected abstract String getChangeParticipationSubject();
	protected abstract String getNotice();

	protected MailService mailService;
	protected DateProvider dateProvider;
	private AccessToken accessToken;
	private ObmUser obmUser;
	private Ical4jHelper ical4jHelper;
	private AttendeeService attendeeService;
	
	protected Logger logger;

	private Event event;
	private Event recurrentEvent;
	private ArrayList<String> icsToCheck;

	private final static String RECIPIENTS =
			"Ronan LANORE <rlanore@linagora.com>, " +
			"Guillaume ALAUX <galaux@linagora.com>, " +
			"Matthieu BAECHLER <mbaechler@linagora.com>, " +
			"Blandine DESCAMPS <blandine.descamps@linagora.com>";

	protected EventChangeMailer getLocaleEventChangeMailer(Locale locale) {
		ITemplateLoader templateLoader = new ITemplateLoader() {
			@Override
			public Template getTemplate(String templateName, Locale locale, TimeZone timezone)
					throws IOException {
				Configuration cfg = new Configuration();
				cfg.setClassForTemplateLoading(getClass(), TemplateLoaderFreeMarkerImpl.getTemplatePathPrefix(locale));
				Template template = cfg.getTemplate(templateName, locale);
				template.setTimeZone(TIMEZONE);
				return cfg.getTemplate(templateName, locale);
			}
		};
		
		ObmSyncConfigurationService constantService = createMock(ObmSyncConfigurationService.class);
		
		expect(dateProvider.getDate()).andReturn(new Date()).anyTimes();

		expect(constantService.getObmUIBaseUrl()).andReturn("baseUrl").once();
		expect(constantService.getResourceBundle(locale)).andReturn(ResourceBundle.getBundle("Messages", locale)).atLeastOnce();
		expect(constantService.getEmailCalendarEncoding()).andReturn(null).atLeastOnce();
		replay(constantService, dateProvider);

		return new EventChangeMailer(mailService, constantService, templateLoader, logger);
	}
	
	private ArrayList<String> getRawMessageWithSubject(String subject) {
		return Lists.newArrayList(
				"From: Obm User <user@test>",
				"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
				"Subject: " + subject
		);
	}
	
	private ArrayList<String> getCommonICSFields() {
		return Lists.newArrayList(
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"BEGIN:VEVENT",
				"SUMMARY:Sprint planning OBM",
				"ORGANIZER;CN=Raphael ROUGERON:mailto:rrougeron@linagora.com",
				"UID:f1514f44bf39311568d64072c1fec10f47fe",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"SEQUENCE:2",
				"DURATION:PT45M"
		);
	}
	
	private ArrayList<String> getNoAllowedTimeFormat() {
		return Lists.newArrayList(
				"11:00:00",
				"11:45:00",
				"12:00:00",
				"13:00:00",
				"1:00:00"
		);
	}

	@Before
	public void setup() {
		dateProvider = createMock(DateProvider.class);
		mailService = createMock(MailService.class);
		logger = createNiceMock(Logger.class);
		attendeeService = new SimpleAttendeeService();
		ical4jHelper = new Ical4jHelper(dateProvider, null, attendeeService);
		accessToken = new AccessToken(1, "unitTest");
		obmUser = ServicesToolBox.getDefaultObmUser();

		eventChangeMailer = newEventChangeMailer();
		event = buildTestEvent();
		recurrentEvent = buildTestRecurrentEvent();
		icsToCheck = getCommonICSFields();
	}

	private static Attendee createAttendee(String name, String email) {
		return UserAttendee.builder().email(email).displayName(name).build();
	}

	private List<InternetAddress> createAddressList(String addresses) throws AddressException {
		return ImmutableList.copyOf(InternetAddress.parse(addresses));
	}
	
	private static Event buildTestEvent() {
		Event event = new Event();
		event.setSequence(2);
		event.setTimeCreate(date("2009-06-08T16:22:53"));
		event.setTimeUpdate(date("2009-06-08T16:23:15"));
		event.addAttendee(createAttendee("Ronan LANORE", "rlanore@linagora.com"));
		event.addAttendee(createAttendee("Guillaume ALAUX", "galaux@linagora.com"));
		event.addAttendee(createAttendee("Matthieu BAECHLER", "mbaechler@linagora.com"));
		event.addAttendee(createAttendee("Blandine DESCAMPS", "blandine.descamps@linagora.com"));
		event.setTitle("Sprint planning OBM");
		event.setOwner("raphael");
		event.setOwnerDisplayName("Raphael ROUGERON");
		event.setOwnerEmail("rrougeron@linagora.com");
		event.setCreatorDisplayName("Emmanuel SURLEAU");
		event.setCreatorEmail("esurleau@linagora.com");
		event.setStartDate(date("2010-11-08T11:00:00"));
		event.setExtId(new EventExtId("f1514f44bf39311568d64072c1fec10f47fe"));
		event.setDuration(2700);
		event.setLocation("A random location");
		event.setUid(new EventObmId(1354));
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.none);
		event.setRecurrence(recurrence);
		return event;
	}
	
	private static Event buildTestRecurrentEvent() {
		Event event = buildTestEvent();
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.weekly);
		recurrence.setFrequence(2);
		recurrence.setDays(new RecurrenceDays(
				RecurrenceDay.Monday,
				RecurrenceDay.Wednesday,
				RecurrenceDay.Thursday));
		recurrence.setEnd(date("2012-11-23T12:00:00"));
		event.setRecurrence(recurrence);
		return event;
	}
	
	private Capture<MimeMessage> expectMailServiceSendMessageWithRecipients(String expectedRecipients)
			throws AddressException, MessagingException {
		Capture<MimeMessage> capturedMessage = new Capture<MimeMessage>();

		List<InternetAddress> addressList = createAddressList(expectedRecipients);
		mailService.sendMessage(
				EventNotificationServiceTestTools.compareCollections(addressList), 
				capture(capturedMessage),
				anyObject(AccessToken.class));
		expectLastCall().atLeastOnce();
		
		replay(mailService);
		return capturedMessage;
	}
	
	private void checkStringContains(String text, List<String> expected) {
		for (String s: expected) {
			assertThat(text).contains(s);
		}
	}
	
	private void checkStringDoesNotContains(String text, List<String> expected) {
		for (String s: expected) {
			assertThat(text).doesNotContain(s);
		}
	}
	
	private void checkHtmlMessage(InvitationParts parts, List<String> expected) throws IOException, MessagingException {
		BodyPart htmlText = parts.htmlText;
		assertThat(htmlText.getContent()).isInstanceOf(String.class);
		String text = Jsoup.parse((String)htmlText.getContent()).text();
		checkStringContains(text, expected);
		checkStringDoesNotContains(text, getNoAllowedTimeFormat());
	}

	private void checkPlainMessage(InvitationParts parts, List<String> expected) throws IOException, MessagingException {
		BodyPart plainText = parts.plainText;
		assertThat(plainText.getContent()).isInstanceOf(String.class);
		String text = (String) plainText.getContent();
		checkStringContains(text, expected);
		checkStringDoesNotContains(text, getNoAllowedTimeFormat());
	}
	
	private void checkNotice(InvitationParts parts) throws IOException, MessagingException {
		String htmlText = Jsoup.parse((String)parts.htmlText.getContent()).text();
		String plainText = (String) parts.plainText.getContent();
		checkStringContains(htmlText, Lists.newArrayList(getNotice()));
		checkStringContains(plainText, Lists.newArrayList(getNotice()));
	}

	private void checkRawMessage(InvitationParts parts, List<String> expected) {
		String rawMessage = parts.rawMessage;
		checkStringContains(rawMessage, expected);
	}
	
	private void checkIcs(InvitationParts parts, List<String> expected) throws IOException, MessagingException {
		checkTextCalendar(parts.textCalendar, expected);
		checkApplicationIcs(parts.applicationIcs, expected);
	}
	
	private void checkTextCalendar(BodyPart textCalendar, List<String> expected) throws IOException, MessagingException {
		assertThat(textCalendar.getContent()).isInstanceOf(String.class);
		String text = (String) textCalendar.getContent();
		checkStringContains(text, expected);
	}

	private void checkApplicationIcs(BodyPart applicationIcs, List<String> expected) throws IOException, MessagingException {
		assertThat(applicationIcs.getContent()).isInstanceOf(SharedByteArrayInputStream.class);
		SharedByteArrayInputStream stream = (SharedByteArrayInputStream) applicationIcs.getContent();
		String decodedString = IOUtils.toString(stream, Charsets.US_ASCII.displayName());
		checkStringContains(decodedString, expected);
	}

	private static String getRawMessage(MimeMessage actualMessage)
			throws IOException, MessagingException,	UnsupportedEncodingException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		actualMessage.writeTo(output);
		String rawMessage = new String(output.toByteArray(), Charsets.UTF_8.displayName());
		return rawMessage;
	}
	
	public static class InvitationParts {
		public String rawMessage;
		public BodyPart plainText;
		public BodyPart htmlText;
		public BodyPart textCalendar;
		public BodyPart applicationIcs;
	}
	
	protected InvitationParts checkNotificationStructure(MimeMessage mimeMessage)
			throws UnsupportedEncodingException, IOException, MessagingException {
		InvitationParts parts = new InvitationParts();
		parts.rawMessage = getRawMessage(mimeMessage);
		assertThat(mimeMessage.getContentType()).startsWith("multipart/alternative");
		assertThat(mimeMessage.getContent()).isInstanceOf(Multipart.class);
		Multipart alternative = (Multipart) mimeMessage.getContent();
		assertThat(alternative.getCount()).isEqualTo(2);
		parts.plainText = alternative.getBodyPart(0);
		assertThat(parts.plainText.getContentType()).startsWith("text/plain; charset=UTF-8");
		parts.htmlText = alternative.getBodyPart(1);
		assertThat(parts.htmlText.getContentType()).startsWith("text/html; charset=UTF-8");
		return parts;
	}

	private InvitationParts checkInvitationStructure(MimeMessage mimeMessage)
			throws UnsupportedEncodingException, IOException, MessagingException {
		InvitationParts parts = new InvitationParts();
		parts.rawMessage = getRawMessage(mimeMessage);
		assertThat(mimeMessage.getContentType()).startsWith("multipart/mixed");
		assertThat(mimeMessage.getContent()).isInstanceOf(Multipart.class);
		Multipart mixed = (Multipart) mimeMessage.getContent();
		assertThat(mixed.getCount()).isEqualTo(2);
		BodyPart firstPart = mixed.getBodyPart(0);
		assertThat(firstPart.getContentType()).startsWith("multipart/alternative");
		assertThat(firstPart.getContent()).isInstanceOf(Multipart.class);
		Multipart alternative = (Multipart) firstPart.getContent();
		assertThat(alternative.getCount()).isEqualTo(3);
		parts.plainText = alternative.getBodyPart(0);
		assertThat(parts.plainText.getContentType()).startsWith("text/plain; charset=UTF-8");
		parts.htmlText = alternative.getBodyPart(1);
		assertThat(parts.htmlText.getContentType()).startsWith("text/html; charset=UTF-8");
		parts.textCalendar = alternative.getBodyPart(2);
		parts.applicationIcs = mixed.getBodyPart(1);
		assertThat(parts.applicationIcs.getContentType()).isEqualTo("application/ics; name=meeting.ics");
		return parts;
	}

	/*
	 * Test against :
	 * EventNoticeHtml_en.tpl, EventNoticePlain_en.tpl
	 * EventNoticeHtml_fr.tpl, EventNoticePlain_fr.tpl
	 */
	@Test
	public void testAcceptedCreation() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		eventChangeMailer.notifyAcceptedNewUsers(
				obmUser, event.getAttendees(),
				event, getLocale(),
				TIMEZONE, accessToken);

		verify(mailService);

		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkNotificationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getNewEventSubject()));
		checkPlainMessage(parts, getInvitationPlainMessage());
		checkHtmlMessage(parts, getInvitationHtmlMessage());
		checkNotice(parts);
	}

	/*
	 * Test against :
	 * RecurrentEventNoticeHtml_en.tpl, RecurrentEventNoticePlain_en.tpl
	 * RecurrentEventNoticeHtml_fr.tpl, RecurrentEventNoticePlain_fr.tpl
	 */
	@Test
	public void testAcceptedCreationRecurrentEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		eventChangeMailer.notifyAcceptedNewUsers(
				obmUser, recurrentEvent.getAttendees(),
				recurrentEvent, getLocale(),
				TIMEZONE, accessToken);

		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkNotificationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getNewRecurrentEventSubject()));
		checkPlainMessage(parts, getRecurrentInvitationPlainMessage());
		checkHtmlMessage(parts, getRecurrentInvitationHtmlMessage());
		checkNotice(parts);
	}
	
	/*
	 * Test against :
	 * ParticipationChangeHtml_en.tpl, ParticipationChangePlain_en.tpl
	 * ParticipationChangeHtml_fr.tpl, ParticipationChangePlain_fr.tpl
	 */
	@Test
	public void testAcceptedParticipationChangeEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients("Raphael ROUGERON <rrougeron@linagora.com>");

		List<Attendee> attendees = event.getAttendees();
		Attendee updatedAttendee = attendees.get(2);
		updatedAttendee.setParticipation(Participation.accepted());
		Participation updatedAttendeeStatus = updatedAttendee.getParticipation();
		updatedAttendeeStatus.setComment(new Comment("This is a random comment"));
		event.addAttendee(createAttendee("Raphael ROUGERON", "rrougeron@linagora.com"));
		Attendee organizer = attendees.get(4);
		organizer.setOrganizer(true);
		
		String ics = ical4jHelper.buildIcsInvitationReply(event,
				ServicesToolBox.getIcal4jUserFrom("mbaechler@linagora.com"), accessToken);

		eventChangeMailer.notifyUpdateParticipation(
				event, event.findOrganizer(),
				ServicesToolBox.getSpecificObmUserFrom("mbaechler@linagora.com", "Matthieu", "BAECHLER"),
				updatedAttendeeStatus, getLocale(),
				TIMEZONE, ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		ArrayList<String> rawMessage = getRawMessageWithSubject(getChangeParticipationSubject());
		rawMessage.remove("From: Obm User <user@test>");
		rawMessage.add("From: Matthieu BAECHLER <mbaechler@linagora.com>");
		checkRawMessage(parts, rawMessage);
		checkPlainMessage(parts, getChangeParticipationPlainMessage());
		checkHtmlMessage(parts, getChangeParticipationHtmlMessage());
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REPLY;");
		icsToCheck.add("METHOD:REPLY");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		icsToCheck.add("ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;RSVP=TRUE;CN=Matthieu BAECHLE\r\n R;ROLE=OPT-P" +
				"ARTICIPANT:mailto:mbaechler@linagora.com");
		icsToCheck.add("COMMENT:This is a random comment");
		checkIcs(parts, icsToCheck);
	}
	
	/*
	 * Test against :
	 * EventCancelHtml_en.tpl, EventCancelPlain_en.tpl
	 * EventCancelHtml_fr.tpl, EventCancelPlain_fr.tpl
	 */
	@Test
	public void testCancelation() throws AddressException, MessagingException, UnsupportedEncodingException, IOException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		String ics = ical4jHelper.buildIcsInvitationCancel(ServicesToolBox.getIcal4jUser(), event, accessToken);
		eventChangeMailer.notifyRemovedUsers(
				ServicesToolBox.getDefaultObmUser(), event.getAttendees(),
				event, getLocale(),
				TIMEZONE, ics,
				accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getCancelEventSubject()));
		checkPlainMessage(parts, getCancelPlainMessage());
		checkHtmlMessage(parts, getCancelHtmlMessage());
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=CANCEL;");
		icsToCheck.add("METHOD:CANCEL");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		checkIcs(parts, icsToCheck);
	}
	
	/*
	 * Test against :
	 * RecurrentEventCancelHtml_en.tpl, RecurrentEventCancelPlain_en.tpl
	 * RecurrentEventCancelHtml_fr.tpl, RecurrentEventCancelPlain_fr.tpl
	 */
	@Test
	public void testCancelationRecurrentEvent() throws AddressException, MessagingException, UnsupportedEncodingException, IOException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		String ics = ical4jHelper.buildIcsInvitationCancel(ServicesToolBox.getIcal4jUser(), recurrentEvent, accessToken);
		eventChangeMailer.notifyRemovedUsers(
				ServicesToolBox.getDefaultObmUser(), recurrentEvent.getAttendees(),
				recurrentEvent, getLocale(),
				TIMEZONE, ics,
				accessToken);

		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getCancelRecurrentEventSubject()));
		checkPlainMessage(parts, getRecurrentCancelPlainMessage());
		checkHtmlMessage(parts, getRecurrentCancelHtmlMessage());
		icsToCheck.add("METHOD:CANCEL");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		icsToCheck.add("RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE");
		checkIcs(parts, icsToCheck);
	}

	/*
	 * Test against :
	 * EventInvitationHtml_en.tpl, EventInvitationPlain_en.tpl
	 * EventInvitationHtml_fr.tpl, EventInvitationPlain_fr.tpl
	 */
	@Test
	public void testNeedActionCreation() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		String ics  = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), event, accessToken);
		eventChangeMailer.notifyNeedActionNewUsers(
				ServicesToolBox.getDefaultObmUser(),event.getAttendees(),
				event, getLocale(),
				TIMEZONE, ics,
				accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REQUEST;");
		checkRawMessage(parts, getRawMessageWithSubject(getNewEventSubject()));
		checkPlainMessage(parts, getInvitationPlainMessage());
		checkHtmlMessage(parts, getInvitationHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		checkIcs(parts, icsToCheck);
	}
	
	/*
	 * Test against :
	 * RecurrentEventInvitationHtml_en.tpl, RecurrentEventInvitationPlain_en.tpl
	 * RecurrentEventInvitationHtml_fr.tpl, RecurrentEventInvitationPlain_fr.tpl
	 */
	@Test
	public void testNeedActionCreationRecurrentEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		String ics  = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), recurrentEvent, accessToken);
		eventChangeMailer.notifyNeedActionNewUsers(
				ServicesToolBox.getDefaultObmUser(), recurrentEvent.getAttendees(),
				recurrentEvent, getLocale(),
				TIMEZONE, ics,
				accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getNewRecurrentEventSubject()));
		checkPlainMessage(parts, getRecurrentInvitationPlainMessage());
		checkHtmlMessage(parts, getRecurrentInvitationHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		icsToCheck.add("RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE");
		checkIcs(parts, icsToCheck);
	}
	
	/*
	 * Test against :
	 * EventUpdateInvitationHtml_en.tpl, EventUpdateInvitationPlain_en.tpl
	 * EventUpdateInvitationHtml_fr.tpl, EventUpdateInvitationPlain_fr.tpl
	 */
	@Test
	public void testNeedActionUpdate() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event before = buildTestEvent();
		Event after = before.clone();
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att : before.getAttendees()) {
			att.setParticipation(Participation.needsAction());
		}
		after.setSequence(4);
		
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(
				ServicesToolBox.getDefaultObmUser(), before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REQUEST;");
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateEventSubject()));
		checkPlainMessage(parts, getUpdatePlainMessage());
		checkHtmlMessage(parts, getUpdateHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T120000");
		icsToCheck.remove("SEQUENCE:2");
		icsToCheck.add("SEQUENCE:4");
		icsToCheck.remove("DURATION:PT45M");
		icsToCheck.add("DURATION:PT1H");
		checkIcs(parts, icsToCheck);
	}
	
	/*
	 * Test against :
	 * RecurrentEventUpdateInvitationHtml_en.tpl, RecurrentEventUpdateInvitationPlain_en.tpl
	 * RecurrentEventUpdateInvitationHtml_fr.tpl, RecurrentEventUpdateInvitationPlain_fr.tpl
	 */
	@Test
	public void testNeedActionUpdateRecurrentEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event before = buildTestRecurrentEvent();
		Event after = before.clone();
		before.getRecurrence().setEnd(null);
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att : before.getAttendees()) {
			att.setParticipation(Participation.needsAction());
		}
		after.setSequence(4);
		
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(
				ServicesToolBox.getDefaultObmUser(), before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REQUEST;");
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateRecurrentEventSubject()));
		checkPlainMessage(parts, getRecurrentUpdatePlainMessage());
		checkHtmlMessage(parts, getRecurrentUpdateHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T120000");
		icsToCheck.remove("SEQUENCE:2");
		icsToCheck.add("SEQUENCE:4");
		icsToCheck.add("RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE");
		icsToCheck.remove("DURATION:PT45M");
		icsToCheck.add("DURATION:PT1H");
		checkIcs(parts, icsToCheck);
	}
	
	/*
	 * Test against :
	 * EventUpdateNoticeHtml_en.tpl, EventUpdateNoticePlain_en.tpl
	 * EventUpdateNoticeHtml_fr.tpl, EventUpdateNoticePlain_fr.tpl
	 */
	@Test
	public void testNotifyAcceptedUpdateUsers() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event before = buildTestEvent();
		Event after = before.clone();
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att: before.getAttendees()) {
			att.setParticipation(Participation.accepted());
		}
		
		eventChangeMailer.notifyAcceptedUpdateUsers(
				ServicesToolBox.getDefaultObmUser(), before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				"", accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateEventSubject()));
		checkPlainMessage(parts, getUpdatePlainMessage());
		checkHtmlMessage(parts, getUpdateHtmlMessage());
		checkNotice(parts);
	}

	/*
	 * Test against :
	 * RecurrentEventUpdateNoticeHtml_en.tpl, RecurrentEventUpdateNoticePlain_en.tpl
	 * RecurrentEventUpdateNoticeHtml_fr.tpl, RecurrentEventUpdateNoticePlain_fr.tpl
	 */
	@Test
	public void testNotifyAcceptedUpdateUsersWithRecurrentEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event before = buildTestRecurrentEvent();
		Event after = before.clone();
		before.getRecurrence().setEnd(null);
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att: before.getAttendees()) {
			att.setParticipation(Participation.accepted());
		}
		
		eventChangeMailer.notifyAcceptedUpdateUsers(
				ServicesToolBox.getDefaultObmUser(), before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				"", accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateRecurrentEventSubject()));
		checkPlainMessage(parts, getRecurrentUpdatePlainMessage());
		checkHtmlMessage(parts, getRecurrentUpdateHtmlMessage());
		checkNotice(parts);
	}
	
	/*
	 * Test against :
	 * EventUpdateNoticeHtml_en.tpl, EventUpdateNoticePlain_en.tpl
	 * EventUpdateNoticeHtml_fr.tpl, EventUpdateNoticePlain_fr.tpl
	 */
	@Test
	public void testNotifyAcceptedUpdateUsersCanWriteOnCalendar() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event before = buildTestEvent();
		Event after = before.clone();
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att: before.getAttendees()) {
			att.setParticipation(Participation.accepted());
		}
		
		eventChangeMailer.notifyAcceptedUpdateUsersCanWriteOnCalendar(
				obmUser, before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkNotificationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateEventSubject()));
		checkPlainMessage(parts, getUpdatePlainMessage());
		checkHtmlMessage(parts, getUpdateHtmlMessage());
		checkNotice(parts);
	}
	
	@Test
	public void testBuildUpdateParticipationDatamodel() {
		Event event = new Event();
		event.setStartDate(new Date());
		ObmUser obmUser = ToolBox.getDefaultObmUser();
		Participation status = Participation.accepted();
		status.setComment(new Comment(null));

		ObmSyncConfigurationService constantService = createMock(ObmSyncConfigurationService.class);
		expect(constantService.getObmUIBaseUrl()).andReturn("baseUrl").once();
		expect(constantService.getResourceBundle(getLocale())).andReturn(ResourceBundle.getBundle("Messages", getLocale())).atLeastOnce();
		expect(constantService.getEmailCalendarEncoding()).andReturn(null).atLeastOnce();

		replay(constantService);

		EventChangeMailer eventChangeMailer = new EventChangeMailer(null, constantService, null, logger);

		eventChangeMailer.buildUpdateParticipationDatamodel(event, obmUser, status, getLocale());
	}
	
	@Test
	public void testNonRecurrentToRecurrentNotification() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event after = buildTestRecurrentEvent();
		after.setSequence(4);
		Event before = after.clone();
		before.setRecurrence(new EventRecurrence());
		
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyAcceptedUpdateUsers(
				obmUser, before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateRecurrentEventSubject()));
		checkPlainMessage(parts, getNonRecurrentToRecurrentUpdatePlainMessage());
		checkHtmlMessage(parts, getNonRecurrentToRecurrentUpdateHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		icsToCheck.remove("SEQUENCE:2");
		icsToCheck.add("SEQUENCE:4");
		icsToCheck.add("RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE");
		checkIcs(parts, icsToCheck);
		checkNotice(parts);
	}
	
	@Test
	public void testNonRecurrentToRecurrentNotifyNeedActionUpdateUsers() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event after = buildTestRecurrentEvent();
		Event before = after.clone();
		before.setRecurrence(new EventRecurrence());
		
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(
				obmUser, before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateRecurrentEventSubject()));
		checkPlainMessage(parts, getNonRecurrentToRecurrentUpdatePlainMessage());
		checkHtmlMessage(parts, getNonRecurrentToRecurrentUpdateHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		icsToCheck.add("RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE");
		checkIcs(parts, icsToCheck);
	}
	
	@Test
	public void testRecurrentToNonRecurrentNotification() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event before = buildTestRecurrentEvent();
		Event after = before.clone();
		after.setRecurrence(new EventRecurrence());
		
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyAcceptedUpdateUsers(
				obmUser, before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateEventSubject()));
		checkPlainMessage(parts, getRecurrentToNonRecurrentUpdatePlainMessage());
		checkHtmlMessage(parts, getRecurrentToNonRecurrentUpdateHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		checkIcs(parts, icsToCheck);
		checkNotice(parts);
	}

	@Test
	public void testRecurrentToNonRecurrentNotifyNeedActionUpdateUsers() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(RECIPIENTS);

		Event before = buildTestRecurrentEvent();
		Event after = before.clone();
		after.setRecurrence(new EventRecurrence());
		
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(
				obmUser, before.getAttendees(),
				before, after,
				getLocale(), TIMEZONE,
				ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, getRawMessageWithSubject(getUpdateEventSubject()));
		checkPlainMessage(parts, getRecurrentToNonRecurrentUpdatePlainMessage());
		checkHtmlMessage(parts, getRecurrentToNonRecurrentUpdateHtmlMessage());
		icsToCheck.add("METHOD:REQUEST");
		icsToCheck.add("DTSTART;TZID=Europe/Paris:20101108T110000");
		checkIcs(parts, icsToCheck);
	}

}
