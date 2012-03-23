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
import org.junit.Rule;
import org.junit.Test;
import org.minig.imap.Address;
import org.minig.imap.Envelope;
import org.minig.imap.FastFetch;
import org.minig.imap.UIDEnvelope;
import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailTestsUtils;
import org.obm.push.mail.PrivateMailboxService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class ImapFetchAPITest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	
	@Inject EmailConfiguration emailConfig;
	@Inject GreenMail greenMail;
	@Inject ImapMailBoxUtils mailboxUtils;
	@Inject CollectionPathHelper collectionPathHelper;
	
	private String mailbox;
	private String password;
	private BackendSession bs;
	private ImapTestUtils testUtils;
	private Date beforeTest;
	private GreenMailUser greenMailUser;
	
	@Before
	public void setUp() {
		this.beforeTest = new Date();
		this.greenMail.start();
		this.mailbox = "to@localhost.com";
	    this.password = "password";
	    this.greenMailUser = this.greenMail.setUser(mailbox, password);
	    this.bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	    this.testUtils = new ImapTestUtils(mailboxService, privateMailboxService, bs, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test
	public void testFetchEnvelope() throws MailException {
		Envelope envelope = Envelope.createBuilder().date(DateUtils.date("2010-09-17T17:12:26")).
		messageNumber(1).
		messageID("<20100917151246.2A9384BA1@lenny>").
		subject("my subject").
		from(new Address("Ad Min admin@opush.test")).
		to(Lists.newArrayList(new Address("a@test"), new Address("B b@test"))).
		cc(Lists.newArrayList(new Address("c@test"))).
		bcc(Lists.newArrayList(new Address("d@test"))).build();
		
		InputStream inputStream = MailTestsUtils.loadEmail("plainText.eml");
		mailboxService.storeInInbox(bs, inputStream, true);
		
		UIDEnvelope uidEnvelope = mailboxService.fetchEnvelope(bs, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), 1l);

		Assertions.assertThat(uidEnvelope).isNotNull();
		Assertions.assertThat(uidEnvelope).isEqualTo(new UIDEnvelope(1l, envelope));
	}
	
	@Test(expected=MailException.class)
	public void testFetchEnvelopeWithWrongUID() throws MailException {
		InputStream inputStream = MailTestsUtils.loadEmail("plainText.eml");
		mailboxService.storeInInbox(bs, inputStream, true);
		
		mailboxService.fetchEnvelope(bs, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), 2l);
	}
	
	@Test
	public void testFetchEnvelopeMsgnoDifferentThanUID() throws MailException, ImapMessageNotFoundException {
		testUtils.sendEmailToInbox();
		Email emailWillBeDeleted = testUtils.sendEmailToInbox();
		Email email3 = testUtils.sendEmailToInbox();
		
		String mailboxPath = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		mailboxService.delete(bs, mailboxPath, emailWillBeDeleted.getUid());
		
		UIDEnvelope uidEnvelope = mailboxService.fetchEnvelope(bs, mailboxPath, email3.getUid());
		Assertions.assertThat(uidEnvelope).isNotNull();
		Assertions.assertThat(uidEnvelope.getUid()).isEqualTo(email3.getUid());
		Assertions.assertThat(uidEnvelope.getEnvelope().getMsgno()).isEqualTo(2);
	}
	
	@Test
	public void testFetchFastNoUid() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = privateMailboxService.fetchFast(bs, inbox, ImmutableList.<Long>of());
		Assertions.assertThat(result).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testFetchFastNullUids() throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		privateMailboxService.fetchFast(bs, inbox, null);
	}

	@Test
	public void testFetchFastOneMessage() throws MailException, AddressException, MessagingException, UserException {
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", "message content", ServerSetup.SMTP);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = privateMailboxService.fetchFast(bs, inbox, ImmutableList.<Long>of(1L));
		Assertions.assertThat(result).containsOnly(new FastFetch.Builder().internalDate(truncatedInternalDate).uid(1).build());
	}
	
	@Test
	public void testFetchFastDuplicateMessage() throws MailException, AddressException, MessagingException, UserException {
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", "message content", ServerSetup.SMTP);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<FastFetch> result = privateMailboxService.fetchFast(bs, inbox, ImmutableList.<Long>of(1L, 1L));
		Assertions.assertThat(result).containsOnly(new FastFetch.Builder().internalDate(truncatedInternalDate).uid(1).build());
	}
	
	@Test
	public void testFetchFastAnsweredMessage() throws MailException, AddressException, MessagingException, UserException, ImapMessageNotFoundException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Date internalDate = new Date(1234);
		Date truncatedInternalDate = new Date(1000);
		MimeMessage message = GreenMailUtil.buildSimpleMessage(mailbox, "subject", "message content", ServerSetup.SMTP);
		testUtils.deliverToUserInbox(greenMailUser, message, internalDate);
		mailboxService.setAnsweredFlag(bs, inbox, 1);
		Collection<FastFetch> result = privateMailboxService.fetchFast(bs, inbox, ImmutableList.<Long>of(1L));
		Assertions.assertThat(result).containsOnly(new FastFetch.Builder().internalDate(truncatedInternalDate).uid(1).answered().build());
	}
	
}
