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
package org.obm.push.mail.imap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;
import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.exception.MailException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.mail.AnyMatchBodyPreferencePolicy;
import org.obm.push.mail.FetchInstruction;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailTestsUtils;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.MimePartSelector;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.EmailReader;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.FastFetch;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.mime.BodyParam;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(GuiceRunner.class)
@GuiceModule(MailEnvModule.class)
public class MailboxFetchAPITest {

	@Inject MailboxService mailboxService;
	
	@Inject EmailConfiguration emailConfig;
	@Inject GreenMail greenMail;
	@Inject ICollectionPathHelper collectionPathHelper;
	
	private ServerSetup smtpServerSetup;
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
		this.smtpServerSetup = greenMail.getSmtp().getServerSetup();
		this.mailbox = "to@localhost.com";
	    this.password = "password";
	    this.greenMailUser = this.greenMail.setUser(mailbox, password);
	    this.udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
	    this.testUtils = new MailboxTestUtils(mailboxService, udr, mailbox, beforeTest, collectionPathHelper, smtpServerSetup);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Ignore("AppendCommand should send optional message's internal date-time in command")
	@Test
	public void testFetchEnvelope() throws MailException {
		Envelope envelope = Envelope.builder().date(date("2010-09-17T17:12:26")).
		messageID("<20100917151246.2A9384BA1@lenny>").
		subject("my subject").
		from(Lists.newArrayList(new Address("Ad Min", "admin@opush.test"))).
		replyTo(Lists.newArrayList(new Address("Ad Min", "admin@opush.test"))).
		to(Lists.newArrayList(new Address("a@test"), new Address("B", "b@test"))).
		cc(Lists.newArrayList(new Address("c@test"))).
		bcc(Lists.newArrayList(new Address("d@test"))).build();
		
		EmailReader readableEmail = MailTestsUtils.readableEmail("plainText.eml");
		mailboxService.storeInInbox(udr, readableEmail, true);
		
		Collection<UIDEnvelope> uidEnvelopes = mailboxService.fetchEnvelope(udr, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), MessageSet.singleton(1l));

		assertThat(uidEnvelopes).isNotNull().containsExactly(new UIDEnvelope(1l, envelope));
	}
	
	@Test(expected=MailException.class)
	public void testFetchEnvelopeWithWrongUID() throws MailException {
		EmailReader readableEmail = MailTestsUtils.readableEmail("plainText.eml");
		mailboxService.storeInInbox(udr, readableEmail, true);
		
		mailboxService.fetchEnvelope(udr, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), MessageSet.singleton(2l));
	}
	
	@Test
	public void testFetchEnvelopeMsgnoDifferentThanUID() throws MailException, ImapMessageNotFoundException {
		testUtils.sendEmailToInbox();
		Email emailWillBeDeleted = testUtils.sendEmailToInbox();
		Email email3 = testUtils.sendEmailToInbox();
		
		String mailboxPath = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		mailboxService.delete(udr, mailboxPath, MessageSet.singleton(emailWillBeDeleted.getUid()));
		
		Collection<UIDEnvelope> uidEnvelopes = mailboxService.fetchEnvelope(udr, mailboxPath, MessageSet.singleton(email3.getUid()));
		assertThat(uidEnvelopes).isNotNull().hasSize(1);
		assertThat(Iterables.getOnlyElement(uidEnvelopes).getUid()).isEqualTo(email3.getUid());
	}
	
	@Test
	public void testFetchFastNoUid() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = mailboxService.fetchFast(udr, inbox, MessageSet.empty());
		assertThat(result).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testFetchFastNullUids() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		mailboxService.fetchFast(udr, inbox, null);
	}

	@Test
	public void testFetchFastOneMessage() throws MailException, AddressException, MessagingException, UserException {
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		String messageContent = "message content";
		javax.mail.internet.MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, smtpServerSetup);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = mailboxService.fetchFast(udr, inbox, MessageSet.singleton(1L));
		assertThat(result).containsOnly(FastFetch.builder().internalDate(truncatedInternalDate).uid(1).
				size(messageContent.length()).build());
	}
		
	@Test
	public void testFetchFastAnsweredMessage() throws MailException, AddressException, MessagingException, UserException, ImapMessageNotFoundException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		String messageContent = "message content";
		javax.mail.internet.MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, smtpServerSetup);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		mailboxService.setAnsweredFlag(udr, inbox, MessageSet.singleton(1l));
		Collection<FastFetch> result = mailboxService.fetchFast(udr, inbox, MessageSet.singleton(1L));
		assertThat(result).containsOnly(FastFetch.builder().internalDate(truncatedInternalDate).uid(1).answered().
				size(messageContent.length()).build());
	}

	@Test
	public void testFetchFastDeletedMessage() throws MailException, AddressException, MessagingException, UserException, ImapMessageNotFoundException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Date internalDate = new Date(1234);
		String messageContent = "message content";
		javax.mail.internet.MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, smtpServerSetup);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		mailboxService.setDeletedFlag(udr, inbox, MessageSet.singleton(1l));
		Collection<FastFetch> result = mailboxService.fetchFast(udr, inbox, MessageSet.singleton(1L));
		assertThat(result).isEmpty();
	}
	
	@Test
	public void testFetchBodyStructureNoUid() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<org.obm.push.mail.mime.MimeMessage> result = mailboxService.fetchBodyStructure(udr, inbox, MessageSet.empty());
		assertThat(result).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testFetchBodyStructureNullUids() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		mailboxService.fetchBodyStructure(udr, inbox, null);
	}
	
	@Test
	public void testFetchBodyStructureOneSimpleTextPlainMessage() throws MailException, AddressException, MessagingException, UserException {
		String messageContent = "message content";
		javax.mail.internet.MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", messageContent, smtpServerSetup);
		testUtils.deliverToUserInbox(greenMailUser, message, new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<org.obm.push.mail.mime.MimeMessage> collections = mailboxService.fetchBodyStructure(udr, inbox, MessageSet.singleton(1l));
		org.obm.push.mail.mime.MimeMessage onlyElement = Iterables.getOnlyElement(collections);

		assertThat(collections).hasSize(1);
		assertThat(onlyElement.getUid()).isEqualTo(1L);

		assertThat(onlyElement.getMimePart().isMultipart()).isFalse();
		assertThat(onlyElement.getMimePart().getChildren()).isEmpty();
		assertThat(onlyElement.getMimePart().getFullMimeType()).isEqualTo("text/plain");
	}
	
	@Test
	public void testFetchBodyStructureOneComplexMultipartMixedMessage() throws MailException, UserException, IOException {
		InputStream messageInputStream = MailTestsUtils.loadEmail("multipartMixed.eml");
		testUtils.deliverToUserInbox(greenMailUser, 
				GreenMailUtil.newMimeMessage(messageInputStream), new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<org.obm.push.mail.mime.MimeMessage> collections = mailboxService.fetchBodyStructure(udr, inbox, MessageSet.singleton(1l));
		org.obm.push.mail.mime.MimeMessage onlyElement = Iterables.getOnlyElement(collections);
		
		MimePart multiPartMixed = onlyElement.getMimePart();
		MimePart multiPartAlternative = multiPartMixed.getChildren().get(0);
		MimePart attachment = multiPartMixed.getChildren().get(1);
		MimePart textPlain = multiPartAlternative.getChildren().get(0);
		MimePart textHtml = multiPartAlternative.getChildren().get(1);
		
		assertThat(collections).hasSize(1);
		assertThat(onlyElement.getUid()).isEqualTo(1L);
		
		assertThat(multiPartMixed.isMultipart()).isTrue();
		
		assertThat(multiPartMixed.getFullMimeType()).isEqualTo("multipart/mixed");
		assertThat(multiPartMixed.getBodyParam("boundary")).
			isEqualTo(new BodyParam("boundary", "----=_Part_0_1330682067197"));
		
		assertThat(multiPartAlternative.getFullMimeType()).isEqualTo("multipart/alternative");
		
		assertThat(textPlain.getFullMimeType()).isEqualTo("text/plain");
		assertThat(textPlain.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		assertThat(textPlain.getContentTransfertEncoding()).isEqualTo("8bit");
		
		assertThat(textHtml.getFullMimeType()).isEqualTo("text/html");
		assertThat(textHtml.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		assertThat(textHtml.getContentTransfertEncoding()).isEqualTo("8bit");
		
		assertThat(attachment.getFullMimeType()).isEqualTo("application/octet-stream");
		assertThat(attachment.getContentTransfertEncoding()).isEqualTo("base64");
		assertThat(attachment.isInvitation()).isFalse();
	}
	
	@Test
	public void testFetchBodyStructureOneComplexMultipartAlternativeMessage() throws MailException, UserException, IOException {
		InputStream messageInputStream = MailTestsUtils.loadEmail("multipartAlternative.eml");
		testUtils.deliverToUserInbox(greenMailUser, 
				GreenMailUtil.newMimeMessage(messageInputStream), new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<org.obm.push.mail.mime.MimeMessage> collections = mailboxService.fetchBodyStructure(udr, inbox, MessageSet.singleton(1l));
		org.obm.push.mail.mime.MimeMessage onlyElement = Iterables.getOnlyElement(collections);
		
		MimePart multiPartAlternative = onlyElement.getMimePart();
		MimePart textPlain = multiPartAlternative.getChildren().get(0);
		MimePart textHtml = multiPartAlternative.getChildren().get(1);
		MimePart textCalendar = multiPartAlternative.getChildren().get(2);
		
		assertThat(collections.size()).isEqualTo(1);
		assertThat(onlyElement.getUid()).isEqualTo(1L);
		
		assertThat(multiPartAlternative.isMultipart()).isTrue();
		
		assertThat(multiPartAlternative.getFullMimeType()).isEqualTo("multipart/alternative");
		assertThat(multiPartAlternative.getBodyParam("boundary")).
			isEqualTo(new BodyParam("boundary", "----=_Part_2_1320656625672"));
		
		assertThat(textPlain.getFullMimeType()).isEqualTo("text/plain");
		assertThat(textPlain.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		
		assertThat(textHtml.getFullMimeType()).isEqualTo("text/html");
		assertThat(textHtml.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		
		assertThat(textCalendar.getFullMimeType()).isEqualTo("text/calendar");
		assertThat(textCalendar.getBodyParam("charset")).isEqualTo(new BodyParam("charset", "utf-8"));
		assertThat(textCalendar.getBodyParam("method")).isEqualTo(new BodyParam("method", "REPLY"));
		assertThat(textCalendar.getContentTransfertEncoding()).isEqualTo("base64");
		assertThat(textCalendar.isInvitation()).isFalse();
	}
	
	@Ignore("The parsing of a message complex rfc822 is not implemented in GreenMail")
	@Test
	public void testFetchBodyStructureOneComplexRFC822Message() throws MailException, UserException, IOException {
		InputStream messageInputStream = MailTestsUtils.loadEmail("messageRfc822ContentType.eml");
		testUtils.deliverToUserInbox(greenMailUser, 
				GreenMailUtil.newMimeMessage(messageInputStream), new Date());
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		mailboxService.fetchBodyStructure(udr, inbox, MessageSet.singleton(1l));
	}
	
	@Test
	public void testFetchUIDNextEmptyMailbox() {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		long uIDNext = mailboxService.fetchUIDNext(udr, inbox);
		assertThat(uIDNext).isEqualTo(1);
	}
	
	@Test
	public void testFetchUIDNext() {
		testUtils.sendEmailToInbox();
		testUtils.sendEmailToInbox();
		testUtils.sendEmailToInbox();
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		long uIDNext = mailboxService.fetchUIDNext(udr, inbox);
		assertThat(uIDNext).isEqualTo(4);
	}
	
	@Test
	public void testFetchUIDNextMailboxInUTF7() {
		testUtils.createFolders("JIRA INBOX");
		String mailbox = testUtils.mailboxPath("JIRA INBOX");
		
		long uIDNext = mailboxService.fetchUIDNext(udr, mailbox);
		assertThat(uIDNext).isEqualTo(1);
	}
	
	@Test
	public void testFetchUIDNextMailboxInUTF7WithAccent() {
		testUtils.createFolders("déplacements");
		String mailbox = testUtils.mailboxPath("déplacements");
		
		long uIDNext = mailboxService.fetchUIDNext(udr, mailbox);
		assertThat(uIDNext).isEqualTo(1);
	}
	
	@Test
	public void testFetchUIDValidity() {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		long uIDValidity = mailboxService.fetchUIDValidity(udr, inbox);
		assertThat(uIDValidity).isGreaterThan(-1);
	}
	
	@Test(expected=ItemNotFoundException.class)
	public void testFetchEmailMetadataOfWrongUid() throws Exception {
		Date internalDate = date("2004-12-14T22:00:00");
		String content = "content";
		String from = "user@thilaire.lng.org";
		javax.mail.internet.MimeMessage message = GreenMailUtil.buildSimpleMessage(from, "subject", content, smtpServerSetup);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		mailboxService.fetchEmailMetadata(udr, inbox, 15l);
	}
	
	@Test
	public void testFetchEmailMetadata() throws Exception {
		Date internalDate = date("2004-12-14T22:00:00");
		String content = "content";
		String from = "user@thilaire.lng.org";
		javax.mail.internet.MimeMessage message = GreenMailUtil.buildSimpleMessage(from, "subject", content, smtpServerSetup);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		EmailMetadata emailMetadata = mailboxService.fetchEmailMetadata(udr, inbox, 1l);

		assertThat(emailMetadata.getUid()).isEqualTo(1L);
		assertThat(emailMetadata.getSize()).isEqualTo(content.length());
		assertThat(emailMetadata.getFlags()).isEmpty();
		
		Envelope envelope = emailMetadata.getEnvelope();
		assertThat(envelope.getDate()).isEqualTo(internalDate);
		assertThat(envelope.getSubject()).isEqualTo("subject");
		assertThat(envelope.getMessageId()).isNotEmpty();
		assertThat(envelope.getFrom()).containsOnly(new Address(null, from));
		assertThat(envelope.getTo()).isEmpty();
		assertThat(envelope.getCc()).isEmpty();
		assertThat(envelope.getBcc()).isEmpty();
		assertThat(envelope.getReplyTo()).containsOnly(new Address(null, from));
		
		org.obm.push.mail.mime.MimeMessage mimeMessage = emailMetadata.getMimeMessage();
		assertThat(mimeMessage.getUid()).isEqualTo(1l);
		assertThat(mimeMessage.getMimePart().isMultipart()).isFalse();
		assertThat(mimeMessage.getMimePart().getChildren()).isEmpty();
		assertThat(mimeMessage.getMimePart().getFullMimeType()).isEqualTo("text/plain");
	}

	@Test
	public void testFetchMimePartData() throws MailException, IOException {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));
		String inbox = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<MimeMessage> mimeMessages = 
				mailboxService.fetchBodyStructure(udr, inbox, MessageSet.singleton(sentEmail.getUid()));
		
		BodyPreference bodyPreference = BodyPreference.builder().bodyType(MSEmailBodyType.HTML).build();
		List<BodyPreference> bodyPreferences = Lists.newArrayList(bodyPreference);
		
		MimePartSelector mimeMessageSelector = new MimePartSelector();
		FetchInstruction fetchInstruction = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), bodyPreferences, Iterables.getOnlyElement(mimeMessages));
		
		InputStream mimePartData = mailboxService.fetchMimePartStream(udr, inbox, sentEmail.getUid(), fetchInstruction.getMimePart().getAddress());
		String data = CharStreams.toString(new InputStreamReader(mimePartData));
		
		assertThat(data).hasSize(fetchInstruction.getMimePart().getSize());
		assertThat(data).isEqualTo("<b>bodydata</b>");
	}
	
	@Test
	public void testFetchMimePartTruncationData() throws MailException, IOException {
		final int truncationSize = 5;
		
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));
		String inbox = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<MimeMessage> mimeMessages = 
				mailboxService.fetchBodyStructure(udr, inbox, MessageSet.singleton(sentEmail.getUid()));
		
		BodyPreference bodyPreference = BodyPreference.builder().bodyType(MSEmailBodyType.HTML).truncationSize(truncationSize).build();
		List<BodyPreference> bodyPreferences = Lists.newArrayList(bodyPreference);
		
		MimePartSelector mimeMessageSelector = new MimePartSelector();
		FetchInstruction fetchInstruction = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), bodyPreferences, Iterables.getOnlyElement(mimeMessages));
		
		InputStream mimePartData = mailboxService.fetchPartialMimePartStream(udr, inbox, sentEmail.getUid(), 
				fetchInstruction.getMimePart().getAddress(), fetchInstruction.getTruncation());
		String data = CharStreams.toString(new InputStreamReader(mimePartData));
		
		assertThat(data).hasSize(truncationSize);
		assertThat(data).isEqualTo("<b>bo");
	}
	
	@Test
	public void testFetchMimePartDataWithNullMimePart() throws MailException, IOException {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));
		String inbox = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<MimeMessage> mimeMessages = 
				mailboxService.fetchBodyStructure(udr, inbox, MessageSet.singleton(sentEmail.getUid()));
		
		BodyPreference bodyPreference = BodyPreference.builder().bodyType(MSEmailBodyType.RTF).build();
		List<BodyPreference> bodyPreferences = Lists.newArrayList(bodyPreference);
		
		MimePartSelector mimeMessageSelector = new MimePartSelector();
		FetchInstruction fetchInstruction = mimeMessageSelector.select(new AnyMatchBodyPreferencePolicy(), bodyPreferences, Iterables.getOnlyElement(mimeMessages));
		
		InputStream mimePartData = mailboxService.fetchMimePartStream(udr, inbox, sentEmail.getUid(), fetchInstruction.getMimePart().getAddress());
		String data = CharStreams.toString(new InputStreamReader(mimePartData));
		
		assertThat(data).isEqualTo("bodydata");
	}
}