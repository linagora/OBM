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
package org.obm.push.mail.imap.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.minig.imap.ImapTestUtils;
import org.obm.push.minig.imap.StoreClient;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@GuiceModule(org.obm.push.minig.imap.MailEnvModule.class)
@RunWith(GuiceRunner.class)
public class UIDFetchMessageTest {

	@Inject StoreClient.Factory storeClientFactory;
	@Inject GreenMail greenMail;
	
	private String mailbox;
	private String password;
	private StoreClient client;

	@Before
	public void setUp() throws Exception {
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		client = loggedClient();
	}
	
	private StoreClient loggedClient() throws Exception  {
		StoreClient storeClient = storeClientFactory.create(greenMail.getImap().getBindTo(), mailbox, password);
		storeClient.login(false);
		return storeClient;
	}

	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testUidFetchPartSimple() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("plainText.eml"));

		InputStream fetchPart = uidFetchMessage(emailUid);
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("plainText.eml"));
	}
	
	@Test
	public void testUidFetchPartAlternative() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchMessage(emailUid);
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative.eml"));
	}
	
	@Test
	public void testUidFetchPartForwarded() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchMessage(emailUid);
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded.eml"));
	}
	
	@Test
	public void testUidFetchPartMixed() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchMessage(emailUid);
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed.eml"));
	}
	
	@Test
	public void testUidFetchPartRelated() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchMessage(emailUid);
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated.eml"));
	}

	@Test
	public void testUidFetchPartWrongUid() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("plainText.eml"));
		long wrongUid = emailUid + 1;
		
		InputStream uidFetchMessage = uidFetchMessage(wrongUid);
		assertThat(IOUtils.toByteArray(uidFetchMessage)).isEmpty();
	}
	
	@Test
	public void testUidFetchPartWrongSelectedMailbox() throws Exception {
		String mailbox = "wrongmailbox";
		client.create(mailbox);
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("plainText.eml"));

		client.select(mailbox);
		InputStream uidFetchMessage = client.uidFetchMessage(emailUid);
		assertThat(IOUtils.toByteArray(uidFetchMessage)).isEmpty();
	}

	private InputStream uidFetchMessage(long uid) throws Exception {
		StoreClient client = loggedClient();
		client.select(EmailConfiguration.IMAP_INBOX_NAME);
		return client.uidFetchMessage(uid);
	}
}
