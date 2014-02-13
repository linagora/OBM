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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.mail.Mime4jUtils;
import org.obm.push.mail.imap.LinagoraImapClientProvider;
import org.obm.push.minig.imap.ImapTestUtils;
import org.obm.push.minig.imap.StoreClient;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@GuiceModule(org.obm.push.minig.imap.MailEnvModule.class)
@RunWith(GuiceRunner.class)
public class UIDFetchPartTest {

	@Inject StoreClient.Factory storeClientFactory;
	@Inject LinagoraImapClientProvider clientProvider;
	@Inject GreenMail greenMail;
	@Inject Mime4jUtils mime4jUtils;
	
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
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	private StoreClient loggedClient() throws Exception  {
		StoreClient storeClient = storeClientFactory.create(greenMail.getImap().getBindTo(), mailbox, password);
		storeClient.login(false);
		return storeClient;
	}
	
	
	@Test
	public void testUidFetchPartSimple() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("plainText.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("plainText-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartAlternativeText() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartAlternativeHtml() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartAlternativeInvitation() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "3");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative-part3.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedNestedAlternative() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedText() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "1.1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part1-1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedHtml() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "1.2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part1-2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedAttachment() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedFirstPart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedSecondPart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedHtml() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2.1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part2-1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedFile() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2.2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part2-2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedFirstPart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousEmailPart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousFirstPart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2.1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-1.txt"));
	}

	@Test
	public void testUidFetchPartMultipartForwardedPreviousSecondPart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2.2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousHtmlPart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2.2.1");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-2-1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousFilePart() throws Exception {
		long emailUid = ImapTestUtils.storeEmailToInbox(client, loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(emailUid, "2.2.2");
		
		assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-2-2.txt"));
	}
	
	private InputStream uidFetchPart(long uid, String partToFetch) throws Exception {
		StoreClient client = loggedClient();
		client.select(EmailConfiguration.IMAP_INBOX_NAME);
		return client.uidFetchPart(uid, partToFetch);
	}
}
