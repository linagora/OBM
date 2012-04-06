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
package org.obm.push.mail.imap.command;

import static org.obm.DateUtils.date;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.Address;
import org.minig.imap.IMAPHeaders;
import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.EmailHeader;
import org.obm.push.bean.EmailHeaders;
import org.obm.push.bean.User;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;
import org.obm.push.mail.imap.ImapClientProvider;
import org.obm.push.mail.imap.ImapTestUtils;

import com.google.inject.Inject;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(SlowFilterRunner.class)
public class UIDFetchHeadersTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapClientProvider clientProvider;

	@Inject CollectionPathHelper collectionPathHelper;
	@Inject MailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	@Inject GreenMail greenMail;
	
	private String mailbox;
	private String password;
	private BackendSession bs;
	private ImapTestUtils testUtils;
	private Date beforeTest;
	private GreenMailUser greenmailUser;

	@Before
	public void setUp() {
		beforeTest = DateUtils.date("1970-01-01T12:00:00");
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenmailUser = greenMail.setUser(mailbox, password);
		bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		testUtils = new ImapTestUtils(mailboxService, privateMailboxService, bs, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test(expected=NullPointerException.class)
	public void nullEmailHeaderSet() throws Exception {
		sendMessage(message());
		uidFetchHeaders(1, null);
	}
	
	@Test
	public void emptyEmailHeaderSet() throws Exception {
		sendMessage(message());
		IMAPHeaders headers = uidFetchHeaders(1, new EmailHeaders.Builder().build());
		Assertions.assertThat(headers.getBcc()).isEmpty();
		Assertions.assertThat(headers.getCc()).isEmpty();
		Assertions.assertThat(headers.getDate()).isNull();
		Assertions.assertThat(headers.getDispositionNotification()).isEmpty();
		Assertions.assertThat(headers.getRawHeaders()).isEmpty();
		Assertions.assertThat(headers.getRecipients()).isEmpty();
		Assertions.assertThat(headers.getSubject()).isNull();
		Assertions.assertThat(headers.getTo()).isEmpty();
		Assertions.assertThat(headers.getFrom()).isNull();
	}
	
	@Test(expected=ImapMessageNotFoundException.class) @Slow
	public void unknownEmailUid() throws Exception {
		uidFetchHeaders(1, new EmailHeaders.Builder().header(EmailHeader.Common.FROM.getHeader()).build());
	}
	
	@Test @Slow
	public void retrievingUndefinedHeader() throws Exception {
		sendMessage(message());
		String header = "X-UNDEFINED";
		IMAPHeaders headers = uidFetchHeaders(1, new EmailHeaders.Builder().header(new EmailHeader(header)).build());
		Assertions.assertThat(headers.getRawHeader(header)).isNull();
	}
	
	@Test @Slow
	public void testUidFetchHeaders() throws Exception {
		String fromAddress = "from@adress";
		String toAddress = "to@adress";
		String ccAddress = "cc@adress";
		String bccAddress = "bcc@adress";
		String subject = "hey dude";
		Date date = date("2012-05-01T11:00:00");
		
		MimeMessage messageToSend = message();
		messageToSend.setFrom(inetAddr(fromAddress));
		messageToSend.setRecipient(RecipientType.TO, inetAddr(toAddress));
		messageToSend.setRecipient(RecipientType.CC, inetAddr(ccAddress));
		messageToSend.setRecipient(RecipientType.BCC, inetAddr(bccAddress));
		messageToSend.setSubject(subject);
		messageToSend.setSentDate(date);
		
		sendMessage(messageToSend);

		EmailHeaders headersToFetch = new EmailHeaders.Builder()
			.header(EmailHeader.Common.FROM.getHeader())
			.header(EmailHeader.Common.TO.getHeader())
			.header(EmailHeader.Common.CC.getHeader())
			.header(EmailHeader.Common.BCC.getHeader())
			.header(EmailHeader.Common.SUBJECT.getHeader())
			.header(EmailHeader.Common.DATE.getHeader())
			.build();
		
		IMAPHeaders fetchHeaders = uidFetchHeaders(1, headersToFetch);

		Assertions.assertThat(fetchHeaders.getFrom()).isEqualTo(addr(fromAddress));
		Assertions.assertThat(fetchHeaders.getTo()).containsOnly(addr(toAddress));
		Assertions.assertThat(fetchHeaders.getCc()).containsOnly(addr(ccAddress));
		Assertions.assertThat(fetchHeaders.getBcc()).containsOnly(addr(bccAddress));
		Assertions.assertThat(fetchHeaders.getSubject()).isEqualTo(subject);
		Assertions.assertThat(fetchHeaders.getDate()).isEqualTo(date);
	}
	
	@Test @Slow
	public void testCustomHeader() throws Exception {
		String headerName = "X-MyCustomHeader";
		String headerValue = "value of custom header";
		MimeMessage messageToSend = message();
		messageToSend.addHeader(headerName, headerValue);
		sendMessage(messageToSend);

		EmailHeaders headersToFetch = 
				new EmailHeaders.Builder()
					.header(new EmailHeader(headerName))
					.build();
		
		IMAPHeaders fetchHeaders = uidFetchHeaders(1, headersToFetch);

		Assertions.assertThat(fetchHeaders.getRawHeader(headerName)).isEqualTo(headerValue);
	}
	
	@Test @Slow
	public void testCustomCaseInsensitiveHeader() throws Exception {
		String headerName = "X-MyCustomHeader";
		String headerValue = "value of custom header";
		MimeMessage messageToSend = message();
		messageToSend.addHeader(headerName, headerValue);
		sendMessage(messageToSend);

		EmailHeaders headersToFetch = 
				new EmailHeaders.Builder()
					.header(new EmailHeader(headerName))
					.build();
		
		IMAPHeaders fetchHeaders = uidFetchHeaders(1, headersToFetch);

		Assertions.assertThat(fetchHeaders.getRawHeader(headerName.toUpperCase())).isEqualTo(headerValue);
	}
	
	private void sendMessage(MimeMessage message) throws UserException {
		testUtils.deliverToUserInbox(greenmailUser, message, DateUtils.date("2012-01-01T12:00:00"));
	}

	private MimeMessage message() throws MessagingException {
		return GreenMailUtil.buildSimpleMessage(mailbox, "subject", "message content", ServerSetup.SMTP);
	}

	private IMAPHeaders uidFetchHeaders(long uid, EmailHeaders headersToFetch) throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		return privateMailboxService.uidFetchHeaders(bs, inbox, uid, headersToFetch);
	}

	private Address addr(String address) {
		return new Address(address);
	}
	
	private InternetAddress inetAddr(String addr) throws AddressException {
		return new InternetAddress(addr);
	}
}
