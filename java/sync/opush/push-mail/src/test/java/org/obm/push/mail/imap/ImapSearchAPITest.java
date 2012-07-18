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

import java.util.Collection;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.SearchQuery;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;

import com.google.inject.Inject;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(SlowFilterRunner.class) @Slow
public class ImapSearchAPITest {
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject MailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	@Inject CollectionPathHelper collectionPathHelper;
	
	@Inject ImapMailBoxUtils mailboxUtils;
	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private UserDataRequest udr;

	private Date beforeTest;
	private ImapTestUtils testUtils;
	private GreenMailUser greenMailUser;

	@Before
	public void setUp() {
		beforeTest = new Date();
	    greenMail.start();
	    mailbox = "to@localhost.com";
	    password = "password";
	    greenMailUser = greenMail.setUser(mailbox, password);
	    udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	    testUtils = new ImapTestUtils(mailboxService, privateMailboxService, udr, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test(expected=CollectionNotFoundException.class)
	public void testSearchWrongMailbox() throws Exception {
		try {
			privateMailboxService.uidSearch(udr, testUtils.mailboxPath("wrong"), SearchQuery.MATCH_ALL);	
		} catch (MailException e) {
			Assertions.assertThat(e).hasMessageContaining("Cannot find IMAP folder");
			throw e;
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void testSearchNullQuery() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		privateMailboxService.uidSearch(udr, inbox, null);	
	}
	
	@Test
	public void testSearchWithMatchAllQuery() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		Collection<Long> result = privateMailboxService.uidSearch(udr, inbox, SearchQuery.MATCH_ALL);
		Assertions.assertThat(result).isEmpty();
	}

	@Test
	public void testSearchOneMailWithMatchAllQuery() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		MimeMessage message = buildSimpleMessage();
		testUtils.deliverToUserInbox(greenMailUser, message, new Date());
		Collection<Long> result = privateMailboxService.uidSearch(udr, inbox, SearchQuery.MATCH_ALL);
		Assertions.assertThat(result).containsOnly(1L);
	}
	
	@Test
	public void testSearchTwoMailsWithMatchAllQuery() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		MimeMessage message = buildSimpleMessage();
		testUtils.deliverToUserInbox(greenMailUser, message, new Date());
		testUtils.deliverToUserInbox(greenMailUser, message, new Date());
		Collection<Long> result = privateMailboxService.uidSearch(udr, inbox, SearchQuery.MATCH_ALL);
		Assertions.assertThat(result).containsOnly(1L, 2L);
	}
	
	@Test
	public void testSearchSinceOneDayWindowInTwoMails() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		MimeMessage message = buildSimpleMessage();
		Date lastWeek = new DateTime(beforeTest).minusWeeks(1).toDate();
		Date yesterday = new DateTime(beforeTest).minusDays(1).toDate();
		
		testUtils.deliverToUserInbox(greenMailUser, message, beforeTest);
		testUtils.deliverToUserInbox(greenMailUser, message, lastWeek);
		
		SearchQuery query = new SearchQuery.Builder().after(yesterday).build();
		Collection<Long> result = privateMailboxService.uidSearch(udr, inbox, query);
		Assertions.assertThat(result).containsOnly(1L);
	}

	@Test
	public void testSearchBeforeOneDayWindowInTwoMails() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		MimeMessage message = buildSimpleMessage();
		Date lastWeek = new DateTime(beforeTest).minusWeeks(1).toDate();
		Date yesterday = new DateTime(beforeTest).minusDays(1).toDate();
		
		testUtils.deliverToUserInbox(greenMailUser, message, beforeTest);
		testUtils.deliverToUserInbox(greenMailUser, message, lastWeek);
		
		SearchQuery query = new SearchQuery.Builder().before(yesterday).build();
		Collection<Long> result = privateMailboxService.uidSearch(udr, inbox, query);
		Assertions.assertThat(result).containsOnly(2L);
	}

	@Test
	public void testSearchComplexWindow() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		MimeMessage message = buildSimpleMessage();
		Date lastMonth = new DateTime(beforeTest).minusMonths(1).toDate();
		Date lastWeek = new DateTime(beforeTest).minusWeeks(1).toDate();
		Date yesterday = new DateTime(beforeTest).minusDays(1).toDate();
		
		testUtils.deliverToUserInbox(greenMailUser, message, beforeTest);
		testUtils.deliverToUserInbox(greenMailUser, message, lastWeek);
		testUtils.deliverToUserInbox(greenMailUser, message, lastMonth);
		
		SearchQuery query = new SearchQuery.Builder().after(lastWeek).before(yesterday).build();
		Collection<Long> result = privateMailboxService.uidSearch(udr, inbox, query);
		Assertions.assertThat(result).containsOnly(2L);
	}

	@Test
	public void testSearchVoidRangeWindow() throws Exception {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		MimeMessage message = buildSimpleMessage();
		Date lastMonth = new DateTime(beforeTest).minusMonths(1).toDate();
		Date lastWeek = new DateTime(beforeTest).minusWeeks(1).toDate();
		Date yesterday = new DateTime(beforeTest).minusDays(1).toDate();
		
		testUtils.deliverToUserInbox(greenMailUser, message, beforeTest);
		testUtils.deliverToUserInbox(greenMailUser, message, lastWeek);
		testUtils.deliverToUserInbox(greenMailUser, message, lastMonth);
		
		SearchQuery query = new SearchQuery.Builder().after(yesterday).before(lastWeek).build();
		Collection<Long> result = privateMailboxService.uidSearch(udr, inbox, query);
		Assertions.assertThat(result).isEmpty();
	}
	
	private MimeMessage buildSimpleMessage() throws AddressException, MessagingException {
		return GreenMailUtil.buildSimpleMessage("from@localhost", "subject", "message content", ServerSetup.SMTP);
	}
}
