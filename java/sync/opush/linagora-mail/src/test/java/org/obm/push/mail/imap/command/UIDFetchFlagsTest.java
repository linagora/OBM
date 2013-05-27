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

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.OpushLocatorException;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.mail.imap.LinagoraImapClientProvider;
import org.obm.push.mail.imap.MailboxTestUtils;
import org.obm.push.minig.imap.StoreClient;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(SlowFilterRunner.class) @Slow
public class UIDFetchFlagsTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(org.obm.push.minig.imap.MailEnvModule.class);

	@Inject LinagoraImapClientProvider clientProvider;
	@Inject MailboxService mailboxService;
	@Inject CollectionPathHelper collectionPathHelper;

	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private UserDataRequest udr;
	private Date beforeTest;
	private MailboxTestUtils testUtils;

	@Before
	public void setUp() {
		beforeTest = DateUtils.date("1970-01-01T12:00:00");
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
	    testUtils = new MailboxTestUtils(mailboxService, udr, mailbox, beforeTest, collectionPathHelper,
	    		greenMail.getSmtp().getServerSetup());
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testNoFlags() throws Exception {
		long sentEmail = messageWithFlagsToInbox(); // no flags

		Collection<Flag> fetchFlags = uidFetchFlags(sentEmail);
		
		assertThat(fetchFlags).isEmpty();
	}
	
	@Test
	public void testFlagSeen() throws Exception {
		long sentEmail = messageWithFlagsToInbox(Flag.SEEN);

		Collection<Flag> fetchFlags = uidFetchFlags(sentEmail);
		
		assertContainsOnlyFlags(fetchFlags, Flag.SEEN);
	}
	
	@Test
	public void testFlagAnswered() throws Exception {
		long sentEmail = messageWithFlagsToInbox(Flag.ANSWERED);

		Collection<Flag> fetchFlags = uidFetchFlags(sentEmail);
		
		assertContainsOnlyFlags(fetchFlags, Flag.ANSWERED);
	}
	
	@Test
	public void testFlagDeleted() throws Exception {
		long sentEmail = messageWithFlagsToInbox(Flag.DELETED);

		Collection<Flag> fetchFlags = uidFetchFlags(sentEmail);
		
		assertContainsOnlyFlags(fetchFlags, Flag.DELETED);
	}
	
	@Test
	public void testFlagDraft() throws Exception {
		long sentEmail = messageWithFlagsToInbox(Flag.DRAFT);

		Collection<Flag> fetchFlags = uidFetchFlags(sentEmail);
		
		assertContainsOnlyFlags(fetchFlags, Flag.DRAFT);
	}
	
	@Test
	public void testFlagFlagged() throws Exception {
		long sentEmail = messageWithFlagsToInbox(Flag.FLAGGED);

		Collection<Flag> fetchFlags = uidFetchFlags(sentEmail);
		
		assertContainsOnlyFlags(fetchFlags, Flag.FLAGGED);
	}
	
	@Test
	public void testAllFlags() throws Exception {
		Flag[] allFlags = Flag.values();
		long sentEmail = messageWithFlagsToInbox(allFlags);

		Collection<Flag> fetchFlags = uidFetchFlags(sentEmail);
		
		assertContainsOnlyFlags(fetchFlags, allFlags);
	}

	private void assertContainsOnlyFlags(Collection<Flag> fetchFlags, Flag...expectedFlags) {
		assertThat(fetchFlags).containsOnly(expectedFlags);
	}
	
	private FlagsList list(Flag... expectedFlags) {
		FlagsList expectedFlagsList = new FlagsList();
		expectedFlagsList.addAll(Arrays.asList(expectedFlags));
		return expectedFlagsList;
	}

	private InputStream emailStream() {
		return StreamMailTestsUtils.newInputStreamFromString("data");
	}

	private long messageWithFlagsToInbox(Flag... flags) throws OpushLocatorException, IMAPException {
		StoreClient client = loggedClient();
		client.select(EmailConfiguration.IMAP_INBOX_NAME);
		client.append(EmailConfiguration.IMAP_INBOX_NAME, emailStream(), list(flags));
		MessageSet uidSearch = client.uidSearch(SearchQuery.MATCH_ALL_EVEN_DELETED);
		long newEmailUid = Iterables.getOnlyElement(uidSearch);
		return newEmailUid;
	}
	
	private Collection<Flag> uidFetchFlags(long uid) throws MailException {
		String inbox = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		return mailboxService.fetchFlags(udr, inbox, MessageSet.singleton(uid)).get(uid);
	}
	
	private StoreClient loggedClient() throws OpushLocatorException, IMAPException  {
		return clientProvider.getImapClient(udr);
	}
}
