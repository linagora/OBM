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
package org.obm.push.mail.imap;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.Address;
import org.minig.imap.Envelope;
import org.minig.imap.FastFetch;
import org.minig.imap.UIDEnvelope;
import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.IMimePart;
import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailTestsUtils;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@Ignore("Waiting for mail backend testing module")
@RunWith(SlowFilterRunner.class) @Slow
public class MailboxFetchAPITest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject MailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	
	@Inject EmailConfiguration emailConfig;
	@Inject GreenMail greenMail;
	@Inject ImapMailBoxUtils mailboxUtils;
	@Inject CollectionPathHelper collectionPathHelper;
	
	private String mailbox;
	private String password;
	private UserDataRequest udr;
	private MailboxTestUtils testUtils;
	private Date beforeTest;
	private GreenMailUser greenMailUser;
	
	@Before
	public void setUp() {
		this.beforeTest = new Date();
		this.greenMail.start();
		this.mailbox = "to@localhost.com";
	    this.password = "password";
	    this.greenMailUser = this.greenMail.setUser(mailbox, password);
	    this.udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	    this.testUtils = new MailboxTestUtils(mailboxService, privateMailboxService, udr, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Ignore("AppendCommand should send optional message's internal date-time in command")
	@Test
	public void testFetchEnvelope() throws MailException {
		Envelope envelope = Envelope.builder().date(DateUtils.date("2010-09-17T17:12:26")).
		messageID("<20100917151246.2A9384BA1@lenny>").
		subject("my subject").
		from(Lists.newArrayList(new Address("Ad Min", "admin@opush.test"))).
		replyTo(Lists.newArrayList(new Address("Ad Min", "admin@opush.test"))).
		to(Lists.newArrayList(new Address("a@test"), new Address("B", "b@test"))).
		cc(Lists.newArrayList(new Address("c@test"))).
		bcc(Lists.newArrayList(new Address("d@test"))).build();
		
		InputStream inputStream = MailTestsUtils.loadEmail("plainText.eml");
		mailboxService.storeInInbox(udr, inputStream, true);
		
		UIDEnvelope uidEnvelope = privateMailboxService.fetchEnvelope(udr, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), 1l);

		Assertions.assertThat(uidEnvelope).isNotNull();
		Assertions.assertThat(uidEnvelope).isEqualTo(new UIDEnvelope(1l, envelope));
	}
	
	@Test(expected=MailException.class)
	public void testFetchEnvelopeWithWrongUID() throws MailException {
		InputStream inputStream = MailTestsUtils.loadEmail("plainText.eml");
		mailboxService.storeInInbox(udr, inputStream, true);
		
		privateMailboxService.fetchEnvelope(udr, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), 2l);
	}
	
	@Test
	public void testFetchEnvelopeMsgnoDifferentThanUID() throws MailException, ImapMessageNotFoundException {
		testUtils.sendEmailToInbox();
		Email emailWillBeDeleted = testUtils.sendEmailToInbox();
		Email email3 = testUtils.sendEmailToInbox();
		
		String mailboxPath = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		mailboxService.delete(udr, mailboxPath, emailWillBeDeleted.getUid());
		
		UIDEnvelope uidEnvelope = privateMailboxService.fetchEnvelope(udr, mailboxPath, email3.getUid());
		Assertions.assertThat(uidEnvelope).isNotNull();
		Assertions.assertThat(uidEnvelope.getUid()).isEqualTo(email3.getUid());
	}
	
	@Test
	public void testFetchFastNoUid() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = privateMailboxService.fetchFast(udr, inbox, ImmutableList.<Long>of());
		Assertions.assertThat(result).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testFetchFastNullUids() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		privateMailboxService.fetchFast(udr, inbox, null);
	}

	@Test
	public void testFetchFastOneMessage() throws MailException, AddressException, MessagingException, UserException {
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		String messageContent = "message content";
		MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, ServerSetup.SMTP);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = privateMailboxService.fetchFast(udr, inbox, ImmutableList.<Long>of(1L));
		Assertions.assertThat(result).containsOnly(FastFetch.builder().internalDate(truncatedInternalDate).uid(1).
				size(messageContent.length()).build());
	}
	
	@Test
	public void testFetchFastDuplicateMessage() throws MailException, AddressException, MessagingException, UserException {
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		String messageContent = "message content";
		MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, ServerSetup.SMTP);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = privateMailboxService.fetchFast(udr, inbox, ImmutableList.<Long>of(1L, 1L));
		Assertions.assertThat(result).containsOnly(FastFetch.builder().internalDate(truncatedInternalDate).uid(1).
				size(messageContent.length()).build());
	}
	
	@Test
	public void testFetchFastAnsweredMessage() throws MailException, AddressException, MessagingException, UserException, ImapMessageNotFoundException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		String messageContent = "message content";
		MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, ServerSetup.SMTP);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		mailboxService.setAnsweredFlag(udr, inbox, 1);
		Collection<FastFetch> result = privateMailboxService.fetchFast(udr, inbox, ImmutableList.<Long>of(1L));
		Assertions.assertThat(result).containsOnly(FastFetch.builder().internalDate(truncatedInternalDate).uid(1).answered().
				size(messageContent.length()).build());
	}
	
	@Test
	public void testFetchBodyStructureNoUid() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<org.minig.imap.mime.MimeMessage> result = privateMailboxService.fetchBodyStructure(udr, inbox, ImmutableList.<Long>of());
		Assertions.assertThat(result).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testFetchBodyStructureNullUids() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		privateMailboxService.fetchBodyStructure(udr, inbox, null);
	}
	
	@Test
	public void testFetchBodyStructureOneSimpleTextPlainMessage() throws MailException, AddressException, MessagingException, UserException {
		String messageContent = "message content";
		MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, ServerSetup.SMTP);
		testUtils.deliverToUserInbox(greenMailUser, message, new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<org.minig.imap.mime.MimeMessage> collections = privateMailboxService.fetchBodyStructure(udr, inbox, ImmutableList.<Long>of(1L));
		org.minig.imap.mime.MimeMessage onlyElement = Iterables.getOnlyElement(collections);

		Assertions.assertThat(collections).hasSize(1);
		Assertions.assertThat(onlyElement.getUid()).isEqualTo(1L);

		Assertions.assertThat(onlyElement.getMimePart().isMultipart()).isFalse();
		Assertions.assertThat(onlyElement.getMimePart().getChildren()).isEmpty();
		Assertions.assertThat(onlyElement.getMimePart().getFullMimeType()).isEqualTo("text/plain");
	}
	
	@Test
	public void testFetchBodyStructureOneComplexMultipartMixedMessage() throws MailException, UserException {
		InputStream messageInputStream = MailTestsUtils.loadEmail("multipartMixed.eml");
		testUtils.deliverToUserInbox(greenMailUser, 
				GreenMailUtil.newMimeMessage(messageInputStream), new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<org.minig.imap.mime.MimeMessage> collections = privateMailboxService.fetchBodyStructure(udr, inbox, ImmutableList.<Long>of(1L));
		org.minig.imap.mime.MimeMessage onlyElement = Iterables.getOnlyElement(collections);
		
		IMimePart multiPartMixed = onlyElement.getMimePart();
		IMimePart multiPartAlternative = multiPartMixed.getChildren().get(0);
		IMimePart attachment = multiPartMixed.getChildren().get(1);
		IMimePart textPlain = multiPartAlternative.getChildren().get(0);
		IMimePart textHtml = multiPartAlternative.getChildren().get(1);
		
		Assertions.assertThat(collections).hasSize(1);
		Assertions.assertThat(onlyElement.getUid()).isEqualTo(1L);
		
		Assertions.assertThat(multiPartMixed.isMultipart()).isTrue();
		
		Assertions.assertThat(multiPartMixed.getFullMimeType()).isEqualTo("multipart/mixed");
		Assertions.assertThat(multiPartMixed.getBodyParam("boundary")).
			isEqualTo(new BodyParam("boundary", "----=_Part_0_1330682067197"));
		
		Assertions.assertThat(multiPartAlternative.getFullMimeType()).isEqualTo("multipart/alternative");
		
		Assertions.assertThat(textPlain.getFullMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(textPlain.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		Assertions.assertThat(textPlain.getContentTransfertEncoding()).isEqualTo("8bit");
		
		Assertions.assertThat(textHtml.getFullMimeType()).isEqualTo("text/html");
		Assertions.assertThat(textHtml.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		Assertions.assertThat(textHtml.getContentTransfertEncoding()).isEqualTo("8bit");
		
		Assertions.assertThat(attachment.getFullMimeType()).isEqualTo("application/octet-stream");
		Assertions.assertThat(attachment.getContentTransfertEncoding()).isEqualTo("base64");
		Assertions.assertThat(attachment.isInvitation()).isFalse();
	}
	
	@Test
	public void testFetchBodyStructureOneComplexMultipartAlternativeMessage() throws MailException, UserException {
		InputStream messageInputStream = MailTestsUtils.loadEmail("multipartAlternative.eml");
		testUtils.deliverToUserInbox(greenMailUser, 
				GreenMailUtil.newMimeMessage(messageInputStream), new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<org.minig.imap.mime.MimeMessage> collections = privateMailboxService.fetchBodyStructure(udr, inbox, ImmutableList.<Long>of(1L));
		org.minig.imap.mime.MimeMessage onlyElement = Iterables.getOnlyElement(collections);
		
		IMimePart multiPartAlternative = onlyElement.getMimePart();
		IMimePart textPlain = multiPartAlternative.getChildren().get(0);
		IMimePart textHtml = multiPartAlternative.getChildren().get(1);
		IMimePart textCalendar = multiPartAlternative.getChildren().get(2);
		
		Assertions.assertThat(collections.size()).isEqualTo(1);
		Assertions.assertThat(onlyElement.getUid()).isEqualTo(1L);
		
		Assertions.assertThat(multiPartAlternative.isMultipart()).isTrue();
		
		Assertions.assertThat(multiPartAlternative.getFullMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(multiPartAlternative.getBodyParam("boundary")).
			isEqualTo(new BodyParam("boundary", "----=_Part_2_1320656625672"));
		
		Assertions.assertThat(textPlain.getFullMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(textPlain.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		
		Assertions.assertThat(textHtml.getFullMimeType()).isEqualTo("text/html");
		Assertions.assertThat(textHtml.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		
		Assertions.assertThat(textCalendar.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(textCalendar.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		Assertions.assertThat(textCalendar.getBodyParam("method")).isEqualTo(new BodyParam("method", "REPLY"));
		Assertions.assertThat(textCalendar.getContentTransfertEncoding()).isEqualTo("base64");
		Assertions.assertThat(textCalendar.isInvitation()).isFalse();
	}
	
	@Ignore("The parsing of a message complex rfc822 is not implemented in GreenMail")
	@Test
	public void testFetchBodyStructureOneComplexRFC822Message() throws MailException, UserException {
		InputStream messageInputStream = MailTestsUtils.loadEmail("messageRfc822ContentType.eml");
		testUtils.deliverToUserInbox(greenMailUser, 
				GreenMailUtil.newMimeMessage(messageInputStream), new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		privateMailboxService.fetchBodyStructure(udr, inbox, ImmutableList.<Long>of(1L));
	}
}