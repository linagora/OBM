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
import org.easymock.EasyMock;
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
import org.obm.sync.calendar.EventExtId.Factory;
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
import org.obm.sync.services.AttendeeService;
import org.slf4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.ServicesToolBox;
import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(SlowFilterRunner.class)
public class EventChangeMailerTest {

	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	
	private MailService mailService;
	private EventChangeMailer eventChangeMailer;
	private AccessToken accessToken;
	private ObmUser obmUser;
	private Ical4jHelper ical4jHelper;
	private DateProvider dateProvider;
	private AttendeeService attendeeService;
	private Date now;
	private Logger logger;
	
	@Before
	public void setup() {
		now = new Date();
		dateProvider = createMock(DateProvider.class);
		attendeeService = new SimpleAttendeeService();
		Factory eventExtIdFactory = null;
		ical4jHelper = new Ical4jHelper(dateProvider, eventExtIdFactory, attendeeService);
		
		accessToken = new AccessToken(1, "unitTest");
		obmUser = ServicesToolBox.getDefaultObmUser();
		ITemplateLoader templateLoader = new ITemplateLoader() {
			@Override
			public Template getTemplate(String templateName, Locale locale, TimeZone timezone)
					throws IOException {
				Configuration cfg = new Configuration();
				cfg.setClassForTemplateLoading(getClass(), "template");
				Template template = cfg.getTemplate(templateName, locale);
				template.setTimeZone(TIMEZONE);
				return cfg.getTemplate(templateName, locale);
			}
		};
		
		ObmSyncConfigurationService constantService = createMock(ObmSyncConfigurationService.class);
		
		expect(dateProvider.getDate()).andReturn(now).anyTimes();		
		
		expect(constantService.getObmUIBaseUrl()).andReturn("baseUrl").once();
		expect(constantService.getResourceBundle(Locale.FRENCH)).andReturn(ResourceBundle.getBundle("Messages", Locale.FRENCH)).atLeastOnce();
		expect(constantService.getEmailCalendarEncoding()).andReturn(null).atLeastOnce();
		replay(constantService, dateProvider);
		
		mailService = createMock(MailService.class);
		logger = createNiceMock(Logger.class);
		eventChangeMailer = new EventChangeMailer(mailService, constantService, templateLoader, logger);
	}
	
	private static Attendee createAttendee(String name, String email) {
		Attendee attendee = new Attendee();
		attendee.setEmail(email);
		attendee.setDisplayName(name);
		return attendee;
	}

	private List<InternetAddress> createAddressList(String addresses) throws AddressException {
		return ImmutableList.copyOf(InternetAddress.parse(addresses));
	}
	
	private static Event buildTestEvent() {
		Event event = new Event();
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
		event.setUid(new EventObmId(1354));
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.lookup("none"));
		event.setRecurrence(recurrence);
		return event;
	}
	
	private static Event buildTestRecurrentEvent() {
		Event event = new Event();
		event.setTimeCreate(date("2009-06-08T16:22:53"));
		event.setTimeUpdate(date("2009-06-08T16:23:15"));
		event.addAttendee(createAttendee("Jean Dupont", "jdupont@obm.linagora.com"));
		event.addAttendee(createAttendee("Pierre Dupond", "pdupond@obm.linagora.com"));
		event.setTitle("A random recurrent event");
		event.setOwner("jack");
		event.setOwnerDisplayName("Jack de Linagora");
		event.setOwnerEmail("jdlinagora@obm.linagora.com");
		event.setStartDate(date("2012-01-23T12:00:00"));
		event.setExtId(new EventExtId("1234567890"));
		event.setDuration(3600);
		event.setLocation("A random location");
		event.setUid(new EventObmId(1234));
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.lookup("weekly"));
		recurrence.setFrequence(2);
		recurrence.setDays(new RecurrenceDays(RecurrenceDay.Monday, RecurrenceDay.Wednesday, RecurrenceDay.Thursday));
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
	
	private void checkStringContains(String text, String... expected) {
		for (String s: expected) {
			assertThat(text).contains(s);
		}
	}
	
	private void checkHtmlMessage(InvitationParts parts, String... expected) throws IOException, MessagingException {
		BodyPart htmlText = parts.htmlText;
		assertThat(htmlText.getContent()).isInstanceOf(String.class);
		String text = Jsoup.parse((String)htmlText.getContent()).text();
		checkStringContains(text, expected);
	}

	private void checkPlainMessage(InvitationParts parts, String... expected) throws IOException, MessagingException {
		BodyPart plainText = parts.plainText;
		assertThat(plainText.getContent()).isInstanceOf(String.class);
		String text = (String) plainText.getContent();
		checkStringContains(text, expected);
	}
	
	private void checkRawMessage(InvitationParts parts, String... expected) {
		String rawMessage = parts.rawMessage;
		checkStringContains(rawMessage, expected);
	}
	
	private void checkIcs(InvitationParts parts, String... expected) throws IOException, MessagingException {
		checkTextCalendar(parts.textCalendar, expected);
		checkApplicationIcs(parts.applicationIcs, expected);
	}
	
	private void checkTextCalendar(BodyPart textCalendar, String... expected) throws IOException, MessagingException {
		assertThat(textCalendar.getContent()).isInstanceOf(String.class);
		String text = (String) textCalendar.getContent();
		checkStringContains(text, expected);
	}

	private void checkApplicationIcs(BodyPart applicationIcs, String... expected) throws IOException, MessagingException {
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
	
	protected InvitationParts checkNotificationStructure(MimeMessage mimeMessage) throws UnsupportedEncodingException, IOException, MessagingException {
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

	private InvitationParts checkInvitationStructure(MimeMessage mimeMessage) throws UnsupportedEncodingException, IOException, MessagingException {
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

	@Test
	public void testAcceptedCreation() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Ronan LANORE <rlanore@linagora.com>, Guillaume ALAUX " +
				"<galaux@linagora.com>, Matthieu BAECHLER <mbaechler@linagora.com>, Blandine " +
				"DESCAMPS <blandine.descamps@linagora.com>");

		Event event = buildTestEvent();
		
		eventChangeMailer.notifyAcceptedNewUsers(obmUser, event.getAttendees(), event, Locale.FRENCH, TIMEZONE, accessToken);

		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkNotificationStructure(mimeMessage);
		checkRawMessage(parts, 
				"From: Obm User <user@test>",
				"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
				"Subject: =?UTF-8?Q?Nouvel_=C3=A9v=C3=A9nement_de_Raphael_R?=\r\n =?UTF-8?Q?OUGERON_:_Sprint_planning_OBM");
		checkPlainMessage(parts, 
				"NOUVEAU RENDEZ-VOUS",
				"du              : 8 nov. 2010 11:00", 
				"au              : 8 nov. 2010 11:45",
				"sujet           : Sprint planning OBM", 
				"lieu            : ",
				"organisateur    : Raphael ROUGERON",
				"créé par        : Emmanuel SURLEAU",
				"::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ce nouveau rendez-vous.");
		checkHtmlMessage(parts, 
				"Invitation à un événement",
				"Du 8 nov. 2010 11:00", 
				"Au 8 nov. 2010 11:45", 
				"Sujet Sprint planning OBM", 
				"Lieu", 
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU",
			    "Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ce nouveau rendez-vous.");
	}
	
	@Test
	public void testAcceptedCreationRecurrentEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond " +
				"<pdupond@obm.linagora.com>");
		Event event = buildTestRecurrentEvent();
		eventChangeMailer.notifyAcceptedNewUsers(obmUser, event.getAttendees(), event, Locale.FRENCH, TIMEZONE, accessToken);

		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkNotificationStructure(mimeMessage);
		checkRawMessage(parts, 
				"From: Obm User <user@test>",
				"To: Jean Dupont <jdupont@obm.linagora.com>",
				"Subject: =?UTF-8?Q?Nouvel_=C3=A9v=C3=A9nement_r=C3=A9current_de_Jack_d?=\r\n =?UTF-8?Q?e_Linagora_:_A_random_recurrent_event");
		checkPlainMessage(parts, 
				"NOUVEAU RENDEZ-VOUS RÉCURRENT",
				"du            : 23 janv. 2012", 
				"au            : 23 nov. 2012",
				"heure         : 12:00:00 - 13:00:00",
				"recurrence    : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet         : A random recurrent event", 
				"lieu          : A random location",
				"organisateur  : Jack de Linagora",		
				"::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ce nouveau rendez-vous.");
		checkHtmlMessage(parts, 
				"Invitation à un événement récurrent",
				"Du 23 janv. 2012", 
				"Au 23 nov. 2012", 
				"Sujet A random recurrent event", 
				"Lieu A random location", 
				"Organisateur Jack de Linagora",
				"Heure 12:00:00 - 13:00:00",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
			    "Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ce nouveau rendez-vous.");
	}
	
	@Test
	public void testAcceptedParticipationChangeEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Raphael ROUGERON <rrougeron@linagora.com>"
			);
		
		Event event = buildTestEvent();
		event.setSequence(4);
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
				event,
				event.findOrganizer(),
				ServicesToolBox.getSpecificObmUserFrom("mbaechler@linagora.com", "Matthieu", "BAECHLER"),
				updatedAttendeeStatus, Locale.FRENCH, TIMEZONE, ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, 
				"From: Matthieu BAECHLER <mbaechler@linagora.com>",
				"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
				"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_de_participation_?=\r\n =?UTF-8?Q?dans_OBM_:_Sprint_planning_OBM");
		checkPlainMessage(parts, 
				"PARTICIPATION : MISE A JOUR",
				"Matthieu BAECHLER a accepté",
				"l'événement Sprint planning OBM prévu le 8 nov. 2010",
				"This is a random comment");
		checkHtmlMessage(parts, 
				"Participation : mise à jour ",
				"Matthieu BAECHLER a accepté",
				"l'événement Sprint planning OBM prévu le 8 nov. 2010",
				"Commentaire This is a random comment");
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REPLY;");
		checkIcs(parts, "BEGIN:VCALENDAR",
					"CALSCALE:GREGORIAN",
					"VERSION:2.0",
					"METHOD:REPLY",
					"BEGIN:VEVENT",
					"DTSTART:20101108T100000Z",
					"SUMMARY:Sprint planning OBM",
					"ORGANIZER;CN=Raphael ROUGERON:mailto:rrougeron@linagora.com",
					"UID:f1514f44bf39311568d64072c1fec10f47fe",
					"X-OBM-DOMAIN:test.tlse.lng",
					"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
					"CREATED:20090608T142253Z",
					"SEQUENCE:4",
					"ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;RSVP=TRUE;CN=Matthieu BAECHLE\r\n R;ROLE=OPT-P" +
					"ARTICIPANT:mailto:mbaechler@linagora.com",
					"COMMENT:This is a random comment");
		
	}
	
	@Test
	public void testCancelation() throws AddressException, MessagingException, UnsupportedEncodingException, IOException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
			"Ronan LANORE <rlanore@linagora.com>, Guillaume ALAUX " +
			"<galaux@linagora.com>, Matthieu BAECHLER <mbaechler@linagora.com>, Blandine " +
			"DESCAMPS <blandine.descamps@linagora.com>");
		
		Event event = buildTestEvent();
		event.setSequence(2);
		String ics = ical4jHelper.buildIcsInvitationCancel(ServicesToolBox.getIcal4jUser(), event, accessToken);
		eventChangeMailer.notifyRemovedUsers(ServicesToolBox.getDefaultObmUser(), event.getAttendees(), event, Locale.FRENCH, TIMEZONE, ics, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, 
				"From: Obm User <user@test>",
				"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
				"Subject: =?UTF-8?Q?Annulation_d'un_=C3=A9v=C3=A9nement_de_Raphael");
		checkPlainMessage(parts, 
				"RENDEZ-VOUS ANNULÉ",
				"du              : 8 nov. 2010 11:00", 
				"au              : 8 nov. 2010 11:45",
				"sujet           : Sprint planning OBM", 
				"lieu            : ",
				"organisateur    : Raphael ROUGERON");
		checkHtmlMessage(parts, "Annulation d'un événement",
					"Du 8 nov. 2010 11:00", 
					"Au 8 nov. 2010 11:45", 
					"Sujet Sprint planning OBM", 
					"Lieu", 
					"Organisateur Raphael ROUGERON",
					"Créé par Emmanuel SURLEAU");
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=CANCEL;");
		checkIcs(parts, "BEGIN:VCALENDAR",
					"CALSCALE:GREGORIAN",
					"VERSION:2.0",
					"METHOD:CANCEL",
					"BEGIN:VEVENT",
					"DTSTART:20101108T100000Z",
					"SUMMARY:Sprint planning OBM",
					"ORGANIZER;CN=Raphael ROUGERON:mailto:rrougeron@linagora.com",
					"UID:f1514f44bf39311568d64072c1fec10f47fe",
					"X-OBM-DOMAIN:test.tlse.lng",
					"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
					"CREATED:20090608T142253Z",
					"LAST-MODIFIED:20090608T142315Z",
					"SEQUENCE:2");
	}
	
	@Test
	public void testCancelationRecurrentEvent() throws AddressException, MessagingException, UnsupportedEncodingException, IOException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond <pdupond@obm.linagora.com>");
		Event event = buildTestRecurrentEvent();
		event.setSequence(2);
		String ics = ical4jHelper.buildIcsInvitationCancel(ServicesToolBox.getIcal4jUser(), event, accessToken);
		eventChangeMailer.notifyRemovedUsers(ServicesToolBox.getDefaultObmUser(), event.getAttendees(), event, Locale.FRENCH, TIMEZONE, ics, accessToken);

		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, "From: Obm User <user@test>",
					"To: Jean Dupont <jdupont@obm.linagora.com>",
					"Subject: =?UTF-8?Q?Annulation_d'un_=C3=A9v=C3=A9nement_r=C3=A9current_de_Jack_d?=\r\n =?UTF-8?Q?e_Linagora");
		checkPlainMessage(parts, "RENDEZ-VOUS RÉCURRENT ANNULÉ",
					"du           : 23 janv. 2012", 
					"au           : 23 nov. 2012",
					"heure        : 12:00:00 - 13:00:00",
					"recurrence   : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
					"sujet        : A random recurrent event", 
					"lieu         : A random location",
					"organisateur : Jack de Linagora");
		checkHtmlMessage(parts, "Annulation d'un événement récurrent",
					"Du 23 janv. 2012", 
					"Au 23 nov. 2012", 
					"Sujet A random recurrent event", 
					"Lieu A random location", 
					"Organisateur Jack de Linagora",
					"Heure 12:00:00 - 13:00:00",
					"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]");
		checkIcs(parts, "BEGIN:VCALENDAR",
					"CALSCALE:GREGORIAN",
					"VERSION:2.0",
					"METHOD:CANCEL",
					"BEGIN:VEVENT",
					"DTSTART:20120123T110000Z",
					"SUMMARY:A random recurrent event",
					"ORGANIZER;CN=Jack de Linagora:mailto:jdlinagora@obm.linagora.com",
					"UID:1234567890",
					"X-OBM-DOMAIN:test.tlse.lng",
					"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
					"CREATED:20090608T142253Z",
					"LAST-MODIFIED:20090608T142315Z",
					"SEQUENCE:2",
					"RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE");
	}

	
	@Test
	public void testNeedActionCreation() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Ronan LANORE <rlanore@linagora.com>, Guillaume ALAUX " +
				"<galaux@linagora.com>, Matthieu BAECHLER <mbaechler@linagora.com>, Blandine " +
				"DESCAMPS <blandine.descamps@linagora.com>");
		Event event = buildTestEvent();
		event.setSequence(5);
		String ics  = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), event, accessToken);
		eventChangeMailer.notifyNeedActionNewUsers(ServicesToolBox.getDefaultObmUser(), event.getAttendees(), event, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REQUEST;");
		checkRawMessage(parts, 
				"From: Obm User <user@test>",
				"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
				"Subject: =?UTF-8?Q?Nouvel_=C3=A9v=C3=A9nement_de_Raphael_R?=\r\n =?UTF-8?Q?OUGERON_:_Sprint_planning_OBM");
		checkPlainMessage(parts, "NOUVEAU RENDEZ-VOUS",
				"du              : 8 nov. 2010 11:00:00", 
				"au              : 8 nov. 2010 11:45",
				"sujet           : Sprint planning OBM", 
				"lieu            : ",
				"organisateur    : Raphael ROUGERON",
				"créé par        : Emmanuel SURLEAU");
		checkHtmlMessage(parts, "Invitation à un événement",
				"Du 8 nov. 2010 11:00", 
				"Au 8 nov. 2010 11:45", 
				"Sujet Sprint planning OBM", 
				"Lieu", 
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU");
		checkIcs(parts, 
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"METHOD:REQUEST",
				"BEGIN:VEVENT",
				"DTSTART:20101108T100000Z",
				"DURATION:PT45M",
				"SUMMARY:Sprint planning OBM",
				"ORGANIZER;CN=Raphael ROUGERON:mailto:rrougeron@linagora.com",
				"UID:f1514f44bf39311568d64072c1fec10f47fe",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"SEQUENCE:5");
	}
	
	@Test
	public void testNeedActionCreationRecurrentEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond <pdupond@obm.linagora.com>"
				);
		Event event = buildTestRecurrentEvent();
		event.setSequence(5);
		String ics  = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), event, accessToken);
		eventChangeMailer.notifyNeedActionNewUsers(ServicesToolBox.getDefaultObmUser(), event.getAttendees(), event, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, 
				"From: Obm User <user@test>",
				"To: Jean Dupont <jdupont@obm.linagora.com>",
				"Subject: =?UTF-8?Q?Nouvel_=C3=A9v=C3=A9nement_r=C3=A9current_de_Jack_d?=\r\n =?UTF-8?Q?e_Linagora_:_A_random_recurrent_event");
		checkPlainMessage(parts, 
				"NOUVEAU RENDEZ-VOUS RÉCURRENT",
				"du           : 23 janv. 2012", 
				"au           : 23 nov. 2012",
				"heure        : 12:00:00 - 13:00:00",
				"recurrence   : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet        : A random recurrent event", 
				"lieu         : A random location",
				"organisateur : Jack de Linagora");
		checkHtmlMessage(parts, "Invitation à un événement récurrent",
				"Du 23 janv. 2012", 
				"Au 23 nov. 2012", 
				"Sujet A random recurrent event", 
				"Lieu A random location", 
				"Organisateur Jack de Linagora",
				"Heure 12:00:00 - 13:00:00",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]");
		checkIcs(parts, "BEGIN:VCALENDAR",
					"CALSCALE:GREGORIAN",
					"VERSION:2.0",
					"METHOD:REQUEST",
					"BEGIN:VEVENT",
					"DTSTART:20120123T110000Z",
					"DURATION:PT1H",
					"SUMMARY:A random recurrent event",
					"ORGANIZER;CN=Jack de Linagora:mailto:jdlinagora@obm.linagora.com",
					"UID:1234567890",
					"X-OBM-DOMAIN:test.tlse.lng",
					"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
					"CREATED:20090608T142253Z",
					"LAST-MODIFIED:20090608T142315Z",
					"SEQUENCE:5",
					"RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE");
	}
	
	@Test
	public void testNeedActionUpdate() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Ronan LANORE <rlanore@linagora.com>, Guillaume ALAUX " +
				"<galaux@linagora.com>, Matthieu BAECHLER <mbaechler@linagora.com>, Blandine " +
				"DESCAMPS <blandine.descamps@linagora.com>");
		Event before = buildTestEvent();
		Event after = before.clone();
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att : before.getAttendees()) {
			att.setParticipation(Participation.needsAction());
		}
		after.setSequence(4);
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(ServicesToolBox.getDefaultObmUser(), before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REQUEST;");
		checkRawMessage(parts, "From: Obm User <user@test>",
					"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
					"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9nement_de_Raphael");
		checkPlainMessage(parts, 
				"RENDEZ-VOUS MODIFIÉ !",
				"du 8 nov. 2010 11:00", 
				"au 8 nov. 2010 11:45",
				"Le rendez-vous Sprint planning OBM", 
				"lieu : ");
		checkHtmlMessage(parts, 
				"Invitation à un évènement : mise à jour",
				"du 8 nov. 2010 11:00", 
				"au 8 nov. 2010 11:45",
				"Du 8 nov. 2010 12:00", 
				"Au 8 nov. 2010 13:00",
				"Sujet Sprint planning OBM", 
				"Lieu", 
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU");
		checkIcs(parts, 
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"METHOD:REQUEST",
				"BEGIN:VEVENT",
				"DTSTART:20101108T110000Z",
				"DURATION:PT1H",
				"SUMMARY:Sprint planning OBM",
				"ORGANIZER;CN=Raphael ROUGERON:mailto:rrougeron@linagora.com",
				"UID:f1514f44bf39311568d64072c1fec10f47fe",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"SEQUENCE:4");
	}
	
	@Test
	public void testNeedActionUpdateRecurrentEvent() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond <pdupond@obm.linagora.com>"
				);
		Event before = buildTestRecurrentEvent();
		Event after = before.clone();
		after.setStartDate(date("2012-02-15T13:00:00"));
		after.setDuration(7200);
		for (Attendee att : before.getAttendees()) {
			att.setParticipation(Participation.needsAction());
		}
		after.setSequence(4);
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(ServicesToolBox.getDefaultObmUser(), before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		assertThat(parts.textCalendar.getContentType()).isEqualTo("text/calendar; charset=UTF-8; method=REQUEST;");
		checkRawMessage(parts, "From: Obm User <user@test>",
					"To: Jean Dupont <jdupont@obm.linagora.com>",
					"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9ne?=\r\n =?UTF-8?Q?ment_r=C3=A9current_de_Jack_?=\r\n =?UTF-8?Q?de_Linagora_sur_OBM_:_A_random_recurrent_event");
		checkPlainMessage(parts, "RENDEZ-VOUS RÉCURRENT MODIFIÉ !",
				"du 15 févr. 2012", 
				"au 23 nov. 2012",
				"de 13:00:00 à 15:00:00",
				"type de récurrence : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"lieu : A random location");
		checkHtmlMessage(parts, 
				"Invitation à un évènement récurrent : mise à jour",
				"Du 15 févr. 2012", 
				"Au 23 nov. 2012", 
				"Sujet A random recurrent event", 
				"Lieu A random location", 
				"Organisateur Jack de Linagora",
				"Heure 13:00:00 - 15:00:00",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]");
		checkIcs(parts, 
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"METHOD:REQUEST",
				"BEGIN:VEVENT",
				"DTSTART:20120215T120000Z",
				"DURATION:PT2H",
				"SUMMARY:A random recurrent event",
				"ORGANIZER;CN=Jack de Linagora:mailto:jdlinagora@obm.linagora.com",
				"UID:1234567890",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"SEQUENCE:4");
	}
	
	@Test
	public void testNotifyAcceptedUpdateUsers() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Ronan LANORE <rlanore@linagora.com>, Guillaume ALAUX " +
				"<galaux@linagora.com>, Matthieu BAECHLER <mbaechler@linagora.com>, Blandine " +
				"DESCAMPS <blandine.descamps@linagora.com>");
		

		Event before = buildTestEvent();
		Event after = before.clone();
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att: before.getAttendees()) {
			att.setParticipation(Participation.accepted());
		}
		
		eventChangeMailer.notifyAcceptedUpdateUsers(ServicesToolBox.getDefaultObmUser(), before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, "", accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, 
				"From: Obm User <user@test>",
				"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
				"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9nement_de_Raphael");
		checkPlainMessage(parts, 
				"RENDEZ-VOUS MODIFIÉ !",
				"du 8 nov. 2010 11:00", 
				"au 8 nov. 2010 11:45",
				"Le rendez-vous Sprint planning OBM", 
				"lieu : ",
				"::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ces modifications.");
		checkHtmlMessage(parts, 
				"Invitation à un évènement : mise à jour",
				"du 8 nov. 2010 11:00", 
				"au 8 nov. 2010 11:45",
				"Du 8 nov. 2010 12:00", 
				"Au 8 nov. 2010 13:00",
				"Sujet Sprint planning OBM", 
				"Lieu", 
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU",
				"Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ces modifications.");
	}
	
	@Test
	public void testNotifyAcceptedUpdateUsersCanWriteOnCalendar() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Ronan LANORE <rlanore@linagora.com>, Guillaume ALAUX " +
				"<galaux@linagora.com>, Matthieu BAECHLER <mbaechler@linagora.com>, Blandine " +
				"DESCAMPS <blandine.descamps@linagora.com>");
		

		Event before = buildTestEvent();
		Event after = before.clone();
		after.setStartDate(date("2010-11-08T12:00:00"));
		after.setDuration(3600);
		for (Attendee att: before.getAttendees()) {
			att.setParticipation(Participation.accepted());
		}
		eventChangeMailer.notifyAcceptedUpdateUsersCanWriteOnCalendar(obmUser, before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, accessToken);
		
		verify(mailService);
		
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkNotificationStructure(mimeMessage);
		checkRawMessage(parts, 
				"From: Obm User <user@test>",
				"To: Ronan LANORE <rlanore@linagora.com>, Guillaume",
				"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9nement_de_Raphael");
		checkPlainMessage(parts, 
				"RENDEZ-VOUS MODIFIÉ !",
				"du 8 nov. 2010 11:00", 
				"au 8 nov. 2010 11:45",
				"Le rendez-vous Sprint planning OBM", 
				"lieu : ",
				"::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ces modifications.");
		checkHtmlMessage(parts, 
				"Invitation à un évènement : mise à jour",
				"du 8 nov. 2010 11:00", 
				"au 8 nov. 2010 11:45",
				"Du 8 nov. 2010 12:00", 
				"Au 8 nov. 2010 13:00",
				"Sujet Sprint planning OBM", 
				"Lieu", 
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU",
				"Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ces modifications.");
	}
	
	@Test
		public void testBuildUpdateParticipationDatamodel() {
			Event event = new Event();
			event.setStartDate(new Date());
			ObmUser obmUser = new ObmUser();
			Participation status = Participation.accepted();
			status.setComment(new Comment(null));

		ObmSyncConfigurationService constantService = EasyMock.createMock(ObmSyncConfigurationService.class);
		expect(constantService.getObmUIBaseUrl()).andReturn("baseUrl").once();
		expect(constantService.getResourceBundle(Locale.FRENCH)).andReturn(ResourceBundle.getBundle("Messages", Locale.FRENCH)).atLeastOnce();
		expect(constantService.getEmailCalendarEncoding()).andReturn(null).atLeastOnce();

		replay(constantService);

			EventChangeMailer eventChangeMailer = new EventChangeMailer(null, constantService, null, logger);

			eventChangeMailer.buildUpdateParticipationDatamodel(event, obmUser, status, Locale.FRENCH);
		}
	
	@Test
	public void testNonRecurrentToRecurrentNotification() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond <pdupond@obm.linagora.com>");
		Event after = buildTestRecurrentEvent();
		Event before = after.clone();
		before.setRecurrence(new EventRecurrence());
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyAcceptedUpdateUsers(obmUser, before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, "From: Obm User <user@test>",
				"To: Jean Dupont <jdupont@obm.linagora.com>",
				"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9ne?=\r\n =?UTF-8?Q?ment_r=C3=A9current_de_Jack_?=\r\n =?UTF-8?Q?de_Linagora_sur_OBM_:_A_random_recurrent_event"
				);
		checkPlainMessage(parts, "RENDEZ-VOUS RÉCURRENT MODIFIÉ !",
				"du 23 janv. 2012", 
				"au 23 janv. 2012",
				"de 12:00:00 à 13:00:00",
				"au 23 nov. 2012",
				"type de récurrence : Pas de récurrence",
				"type de récurrence : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"lieu : A random location"
				);
		checkHtmlMessage(parts, 
				"Invitation à un évènement récurrent : mise à jour",
				"du 23 janv. 2012", 
				"au 23 janv. 2012",
				"Heure 12:00:00 - 13:00:00",
				"Au 23 nov. 2012",
				"Sujet A random recurrent event", 
				"Lieu A random location", 
				"Organisateur Jack de Linagora",
				"type de récurrence : Pas de récurrence",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]");
		checkIcs(parts, 
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"METHOD:REQUEST",
				"BEGIN:VEVENT",
				"DTSTART:20120123T110000Z",
				"DURATION:PT1H",
				"SUMMARY:A random recurrent event",
				"ORGANIZER;CN=Jack de Linagora:mailto:jdlinagora@obm.linagora.com",
				"UID:1234567890",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE",
				"SEQUENCE:0");
	}
	
	@Test
	public void testRecurrentToNonRecurrentNotification() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond <pdupond@obm.linagora.com>");
		Event before = buildTestRecurrentEvent();
		Event after = before.clone();
		after.setRecurrence(new EventRecurrence());
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyAcceptedUpdateUsers(obmUser, before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, "From: Obm User <user@test>",
				"To: Jean Dupont <jdupont@obm.linagora.com>",
				"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9nement_de_Jack_de_Li?=\r\n =?UTF-8?Q?nagora_sur_OBM_:_A_random_recurrent_event?="
				);
		checkPlainMessage(parts, 
				"RENDEZ-VOUS MODIFIÉ !",
				"du 23 janv. 2012 12:00",
				"au 23 janv. 2012 13:00",
				"Le rendez-vous A random recurrent event", 
				"lieu : A random location",
				"::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ces modifications."
				);
		checkHtmlMessage(parts, 
				"Invitation à un évènement : mise à jour",
				"du 23 janv. 2012 12:00",
				"au 23 janv. 2012 13:00",
				"lieu : A random location",
				"Du 23 janv. 2012 12:00",
				"Au 23 janv. 2012 13:00",
				"Sujet A random recurrent event", 
				"Lieu A random location", 
				"Organisateur Jack de Linagora"
				);
		checkIcs(parts, 
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"METHOD:REQUEST",
				"BEGIN:VEVENT",
				"DTSTART:20120123T110000Z",
				"DURATION:PT1H",
				"SUMMARY:A random recurrent event",
				"ORGANIZER;CN=Jack de Linagora:mailto:jdlinagora@obm.linagora.com",
				"UID:1234567890",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"SEQUENCE:0");
	}
	

	@Test
	public void testNonRecurrentToRecurrentNotifyNeedActionUpdateUsers() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond <pdupond@obm.linagora.com>");
		Event after = buildTestRecurrentEvent();
		Event before = after.clone();
		before.setRecurrence(new EventRecurrence());
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(obmUser, before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, "From: Obm User <user@test>",
				"To: Jean Dupont <jdupont@obm.linagora.com>",
				"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9ne?=\r\n =?UTF-8?Q?ment_r=C3=A9current_de_Jack_?=\r\n =?UTF-8?Q?de_Linagora_sur_OBM_:_A_random_recurrent_event"
				);
		checkPlainMessage(parts, "RENDEZ-VOUS RÉCURRENT MODIFIÉ !",
				"du 23 janv. 2012", 
				"au 23 janv. 2012",
				"de 12:00:00 à 13:00:00",
				"au 23 nov. 2012",
				"type de récurrence : Pas de récurrence",
				"type de récurrence : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"lieu : A random location"
				);
		checkHtmlMessage(parts, 
				"Invitation à un évènement récurrent : mise à jour",
				"du 23 janv. 2012", 
				"au 23 janv. 2012",
				"Heure 12:00:00 - 13:00:00",
				"Au 23 nov. 2012",
				"Sujet A random recurrent event", 
				"Lieu A random location", 
				"Organisateur Jack de Linagora",
				"type de récurrence : Pas de récurrence",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]");
		checkIcs(parts, 
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"METHOD:REQUEST",
				"BEGIN:VEVENT",
				"DTSTART:20120123T110000Z",
				"DURATION:PT1H",
				"SUMMARY:A random recurrent event",
				"ORGANIZER;CN=Jack de Linagora:mailto:jdlinagora@obm.linagora.com",
				"UID:1234567890",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"RRULE:FREQ=WEEKLY;UNTIL=20121123T120000;INTERVAL=2;BYDAY=TH,MO,WE",
				"SEQUENCE:0");
	}
	
	@Test
	public void testRecurrentToNonRecurrentNotifyNeedActionUpdateUsers() throws UnsupportedEncodingException, IOException, MessagingException {
		Capture<MimeMessage> capturedMessage = expectMailServiceSendMessageWithRecipients(
				"Jean Dupont <jdupont@obm.linagora.com>, Pierre Dupond <pdupond@obm.linagora.com>");
		Event before = buildTestRecurrentEvent();
		Event after = before.clone();
		after.setRecurrence(new EventRecurrence());
		String ics = ical4jHelper.buildIcsInvitationRequest(ServicesToolBox.getIcal4jUser(), after, accessToken);
		eventChangeMailer.notifyNeedActionUpdateUsers(obmUser, before.getAttendees(), before, after, Locale.FRENCH, TIMEZONE, ics, accessToken);
		verify(mailService);
		MimeMessage mimeMessage = capturedMessage.getValue();
		InvitationParts parts = checkInvitationStructure(mimeMessage);
		checkRawMessage(parts, "From: Obm User <user@test>",
				"To: Jean Dupont <jdupont@obm.linagora.com>",
				"Subject: =?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9nement_de_Jack_de_Li?=\r\n =?UTF-8?Q?nagora_sur_OBM_:_A_random_recurrent_event?="
				);
		checkPlainMessage(parts, 
				"RENDEZ-VOUS MODIFIÉ !",
				"du 23 janv. 2012 12:00",
				"au 23 janv. 2012 13:00",
				"Le rendez-vous A random recurrent event", 
				"lieu : A random location"
				);
		checkHtmlMessage(parts, 
				"Invitation à un évènement : mise à jour",
				"du 23 janv. 2012 12:00",
				"au 23 janv. 2012 13:00",
				"lieu : A random location",
				"Du 23 janv. 2012 12:00",
				"Au 23 janv. 2012 13:00",
				"Sujet A random recurrent event", 
				"Lieu A random location", 
				"Organisateur Jack de Linagora"
				);
		checkIcs(parts, 
				"BEGIN:VCALENDAR",
				"CALSCALE:GREGORIAN",
				"VERSION:2.0",
				"METHOD:REQUEST",
				"BEGIN:VEVENT",
				"DTSTART:20120123T110000Z",
				"DURATION:PT1H",
				"SUMMARY:A random recurrent event",
				"ORGANIZER;CN=Jack de Linagora:mailto:jdlinagora@obm.linagora.com",
				"UID:1234567890",
				"X-OBM-DOMAIN:test.tlse.lng",
				"X-OBM-DOMAIN-UUID:ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6",
				"CREATED:20090608T142253Z",
				"LAST-MODIFIED:20090608T142315Z",
				"SEQUENCE:0");
	}

}
