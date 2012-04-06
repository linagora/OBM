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

import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.mime.MimeMessage;
import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.mail.FetchInstructions;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.MimeAddress;
import org.obm.push.mail.MimePartSelector;
import org.obm.push.mail.PrivateMailboxService;
import org.obm.push.mail.imap.ImapClientProvider;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.ImapTestUtils;
import org.obm.push.mail.imap.OpushImapFolder;
import org.obm.push.utils.Mime4jUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(SlowFilterRunner.class) @Slow
public class UIDFetchPartTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapClientProvider clientProvider;

	@Inject CollectionPathHelper collectionPathHelper;
	@Inject MailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	@Inject GreenMail greenMail;
	@Inject Mime4jUtils mime4jUtils;
	private String mailbox;
	private String password;
	private BackendSession bs;
	private ImapTestUtils testUtils;
	private Date beforeTest;

	@Before
	public void setUp() {
		beforeTest = DateUtils.date("1970-01-01T12:00:00");
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		testUtils = new ImapTestUtils(mailboxService, privateMailboxService, bs, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testUidFetchPartSimple() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("plainText.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("plainText-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartAlternativeText() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartAlternativeHtml() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartAlternativeInvitation() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "3");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative-part3.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedNestedAlternative() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedText() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "1.1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part1-1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedHtml() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "1.2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part1-2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartMixedAttachment() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedFirstPart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedSecondPart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedHtml() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2.1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part2-1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartRelatedFile() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2.2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated-part2-2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedFirstPart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousEmailPart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousFirstPart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2.1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-1.txt"));
	}

	@Test
	public void testUidFetchPartMultipartForwardedPreviousSecondPart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2.2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-2.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousHtmlPart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2.2.1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-2-1.txt"));
	}
	
	@Test
	public void testUidFetchPartMultipartForwardedPreviousFilePart() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchPart(sentEmail.getUid(), "2.2.2");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded-part2-2-2.txt"));
	}
	
	@Test
	public void testUidFetchPartFindAttachment() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));
		String inbox = collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		InputStream attachment = mailboxService.findAttachment(bs, inbox, sentEmail.getUid(), new MimeAddress("3"));
		
		Assertions.assertThat(attachment).hasContentEqualTo(loadEmail("multipartAlternative-part3.txt"));
	}

	@Test
	public void testFetchMimePartData() throws MailException, IOException {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));
		String inbox = collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<MimeMessage> mimeMessages = 
				privateMailboxService.fetchBodyStructure(bs, inbox, Lists.newArrayList(sentEmail.getUid()));
		
		BodyPreference bodyPreference = new BodyPreference.Builder().bodyType(MSEmailBodyType.HTML).build();
		List<BodyPreference> bodyPreferences = Lists.newArrayList(bodyPreference);
		
		MimePartSelector mimeMessageSelector = new MimePartSelector();
		FetchInstructions fetchInstructions = mimeMessageSelector.select(bodyPreferences, Iterables.getOnlyElement(mimeMessages));
		
		InputStream mimePartData = privateMailboxService.fetchMimePartData(bs, inbox, sentEmail.getUid(), fetchInstructions);
		String data = CharStreams.toString(new InputStreamReader(mimePartData));
		
		Assertions.assertThat(data).hasSize(fetchInstructions.getMimePart().getSize());
		Assertions.assertThat(data).isEqualTo("<b>bodydata</b>");
	}
	
	@Test
	public void testFetchMimePartTruncationData() throws MailException, IOException {
		final int truncationSize = 5;
		
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));
		String inbox = collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<MimeMessage> mimeMessages = 
				privateMailboxService.fetchBodyStructure(bs, inbox, Lists.newArrayList(sentEmail.getUid()));
		
		BodyPreference bodyPreference = new BodyPreference.Builder().bodyType(MSEmailBodyType.HTML).truncationSize(truncationSize).build();
		List<BodyPreference> bodyPreferences = Lists.newArrayList(bodyPreference);
		
		MimePartSelector mimeMessageSelector = new MimePartSelector();
		FetchInstructions fetchInstructions = mimeMessageSelector.select(bodyPreferences, Iterables.getOnlyElement(mimeMessages));
		
		InputStream mimePartData = privateMailboxService.fetchMimePartData(bs, inbox, sentEmail.getUid(), fetchInstructions);
		String data = CharStreams.toString(new InputStreamReader(mimePartData));
		
		Assertions.assertThat(data).hasSize(truncationSize);
		Assertions.assertThat(data).isEqualTo("<b>bo");
	}
	
	@Test(expected=NullPointerException.class)
	public void testFetchMimePartDataWithNullMimePart() throws MailException {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));
		String inbox = collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		Collection<MimeMessage> mimeMessages = 
				privateMailboxService.fetchBodyStructure(bs, inbox, Lists.newArrayList(sentEmail.getUid()));
		
		BodyPreference bodyPreference = new BodyPreference.Builder().bodyType(MSEmailBodyType.RTF).build();
		List<BodyPreference> bodyPreferences = Lists.newArrayList(bodyPreference);
		
		MimePartSelector mimeMessageSelector = new MimePartSelector();
		FetchInstructions fetchInstructions = mimeMessageSelector.select(bodyPreferences, Iterables.getOnlyElement(mimeMessages));
		
		privateMailboxService.fetchMimePartData(bs, inbox, sentEmail.getUid(), fetchInstructions);
	}
	
	private InputStream uidFetchPart(long uid, String partToFetch) throws Exception {
		ImapStore client = loggedClient();
		OpushImapFolder folder = client.select(EmailConfiguration.IMAP_INBOX_NAME);
		return folder.uidFetchPart(uid, new MimeAddress(partToFetch));
	}
	
	private ImapStore loggedClient() throws Exception {
		ImapStore client = clientProvider.getImapClientWithJM(bs);
		client.login();
		return client;
	}
}
