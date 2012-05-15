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
package org.obm.push.mail;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.EmailView;
import org.minig.imap.Flag;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.mail.imap.ImapMailboxService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@RunWith(SlowFilterRunner.class)
public class EmailViewPartsFetcherImplTest {

	public static class MessageFixture {
		boolean answered = false;
		boolean read = false;
		boolean starred = false;
	}
	
	private MessageFixture messageFixture;
	private String messageCollectionName;
	private long messageUid;
	private String mailbox;
	private String password;
	private BackendSession bs;

	@Before
	public void setUp() {
		mailbox = "to@localhost.com";
		password = "password";
		bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		
		messageFixture = new MessageFixture();
		messageCollectionName = IMAP_INBOX_NAME;
		messageUid = 1l;
	}
	
	@Test
	public void testFlagAnsweredTrue() throws Exception {
		messageFixture.answered = true;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch();
		
		assertThat(emailView.getFlags()).contains(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagAnsweredFalse() throws Exception {
		messageFixture.answered = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).doesNotContain(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagReadTrue() throws Exception {
		messageFixture.read = true;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).contains(Flag.SEEN);
	}
	
	@Test
	public void testFlagReadFalse() throws Exception {
		messageFixture.read = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).doesNotContain(Flag.SEEN);
	}
	
	@Test
	public void testFlagStarredTrue() throws Exception {
		messageFixture.starred = true;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).contains(Flag.FLAGGED);
	}
	
	@Test
	public void testFlagStarredFalse() throws Exception {
		messageFixture.starred = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch();

		assertThat(emailView.getFlags()).doesNotContain(Flag.FLAGGED);
	}

	private ImapMailboxService messageFixtureToMailboxServiceMock() throws Exception {
		ImapMailboxService mailboxService = createStrictMock(ImapMailboxService.class);
		mockMailboxServiceFlags(mailboxService);
		replay(mailboxService);
		return mailboxService;
	}

	private void mockMailboxServiceFlags(ImapMailboxService mailboxService) throws MailException {
		Builder<Flag> flagsListBuilder = ImmutableList.builder();
		if (messageFixture.answered) {
			flagsListBuilder.add(Flag.ANSWERED);
		}
		if (messageFixture.read) {
			flagsListBuilder.add(Flag.SEEN);
		}
		if (messageFixture.starred) {
			flagsListBuilder.add(Flag.FLAGGED);
		}
		expect(mailboxService.fetchFlags(bs, messageCollectionName, messageUid))
				.andReturn(flagsListBuilder.build()).once();
	}
	
	private EmailViewPartsFetcherImpl newFetcherFromExpectedFixture() throws Exception {
		return new EmailViewPartsFetcherImpl(
				messageFixtureToMailboxServiceMock(), 
				bs, messageCollectionName, messageUid);
	}
}
