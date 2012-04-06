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

import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.columba.ristretto.smtp.SMTPException;
import org.easymock.EasyMock;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.smtp.SmtpSender;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class MockBasedImapMailboxServiceTest {

	private BackendSession bs;
	
	@Before
	public void setUp() {
		String mailbox = "user@domain";
		String password = "password";
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	}
	
	@Test
	public void testSendEmailWithBigInputStream() throws ProcessingEmailException, StoreEmailException, SendEmailException, SmtpInvalidRcptException, SMTPException {
		
		SmtpSender smtpSender = EasyMock.createMock(SmtpSender.class);
		
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		Set<Address> addrs = Sets.newHashSet();
		smtpSender.sendEmail(EasyMock.anyObject(BackendSession.class), EasyMock.anyObject(Address.class),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(InputStream.class));
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(smtpSender);
		
		ImapMailboxService emailManager = 
				new ImapMailboxService(emailConfiguration, smtpSender, null, null, null, null);

		emailManager.sendEmail(bs,
				new Address("test@test.fr"),
				addrs,
				addrs,
				addrs,
				loadEmail("bigEml.eml"), false);
		
		EasyMock.verify(emailConfiguration, smtpSender);
	}

 	@Test
	public void testParseSpecificINBOXCase() throws Exception {
		String userINBOXFolder = "INBOX";
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperExtractFolder(userINBOXFolder);
		
		ImapMailboxService emailManager = new ImapMailboxService(
				emailConfiguration, null, null, null, null, collectionPathHelper);

		String parsedMailbox = emailManager.parseMailBoxName(bs, collectionPath(userINBOXFolder));
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_INBOX_NAME);
	}

	@Test
	public void testParseSpecificINBOXCaseIsntCaseSensitive() throws Exception {
		String userINBOXFolder = "InBoX";
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperExtractFolder(userINBOXFolder);

		ImapMailboxService emailManager = new ImapMailboxService(
				emailConfiguration, null, null, null, null, collectionPathHelper);

		String parsedMailbox = emailManager.parseMailBoxName(bs, collectionPath(userINBOXFolder));
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_INBOX_NAME);
	}

	@Test
	public void testParseINBOXWithOtherFolderEndingByINBOX() throws Exception {
		String folderEndingByINBOX = "userFolder" + EmailConfiguration.IMAP_INBOX_NAME;

		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperExtractFolder(folderEndingByINBOX);
		ImapClientProvider imapClientProvider = newImapClientProviderMock(folderEndingByINBOX);
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();

		ImapMailboxService emailManager = new ImapMailboxService(
				emailConfiguration, null, null, imapClientProvider, null, collectionPathHelper);

		String parsedMailbox = emailManager.parseMailBoxName(bs, collectionPath(folderEndingByINBOX));
		Assertions.assertThat(parsedMailbox).isEqualTo(folderEndingByINBOX);
	}

	private ImapClientProvider newImapClientProviderMock(String...allUserFolders) throws Exception {
		OpushImapFolder imapFolder = newImapFolderMock(allUserFolders);
		ImapStore imapStore = newImapStoreMock(imapFolder);
		ImapClientProvider imapClientProvider = EasyMock.createMock(ImapClientProvider.class);
		EasyMock.expect(imapClientProvider.getImapClientWithJM(bs)).andReturn(imapStore);
		
		EasyMock.replay(imapFolder, imapStore, imapClientProvider);
		return imapClientProvider;
	}

	private ImapStore newImapStoreMock(OpushImapFolder imapFolder) throws Exception {
		ImapStore imapStore = EasyMock.createMock(ImapStore.class);
		EasyMock.expect(imapStore.getDefaultFolder()).andReturn(imapFolder);
		imapStore.login();
		EasyMock.expectLastCall();
		imapStore.logout();
		EasyMock.expectLastCall();
		return imapStore;
	}

	private OpushImapFolder newImapFolderMock(String... allUserFolders) throws MessagingException {
		List<Folder> expectedFolders = Lists.newArrayList();
		for (String userFolder : allUserFolders) {
			expectedFolders.add(folder(userFolder));
		}
		OpushImapFolder imapFolder = EasyMock.createMock(OpushImapFolder.class);
		EasyMock.expect(imapFolder.list("*")).andReturn(expectedFolders.toArray(new Folder[expectedFolders.size()]));
		return imapFolder;
	}

	private Folder folder(String userFolder) throws MessagingException {
		Folder folder = EasyMock.createMock(Folder.class);
		EasyMock.expect(folder.getName()).andReturn(userFolder);
		EasyMock.expect(folder.getFullName()).andReturn(userFolder);
		EasyMock.expect(folder.getSeparator()).andReturn('/');
		EasyMock.replay(folder);
		return folder;
	}
	
	private EmailConfiguration newEmailConfigurationMock() {
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfiguration.class);
		EasyMock.expect(emailConfiguration.loginWithDomain()).andReturn(true).once();
		EasyMock.expect(emailConfiguration.activateTls()).andReturn(false).once();
		EasyMock.replay(emailConfiguration);
		return emailConfiguration;
	}
	
	private CollectionPathHelper mockCollectionPathHelperExtractFolder(String expectedFolder) throws CollectionPathException {
		CollectionPathHelper helper = EasyMock.createMock(CollectionPathHelper.class);
		EasyMock.expect(helper.extractFolder(bs, collectionPath(expectedFolder), PIMDataType.EMAIL))
			.andReturn(expectedFolder).anyTimes();
		EasyMock.replay(helper);
		return helper;
	}

	private String collectionPath(String expectedFolder) {
		return "obm:\\\\user@domain\\email\\" + expectedFolder;
	}
}
