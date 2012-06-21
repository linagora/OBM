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

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxFolder;
import org.obm.push.mail.MailboxFolders;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(SlowFilterRunner.class) @Slow
public class ImapMailboxServiceSubscribedFoldersTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	@Inject CollectionPathHelper collectionPathHelper;

	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private ImapTestUtils testUtils;
	private Date beforeTest;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		beforeTest = new Date();
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		testUtils = new ImapTestUtils(mailboxService, mailboxService, udr, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test
	public void testNewFolderIsCreatedUnsubscribed() throws MailException {
		OpushImapFolder newFolder = createUnsubscribedFolder("newFolder");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		
		assertThat(newFolder.isSubscribed()).isFalse();
		assertThat(subscribedFolders).isEmpty();
	}

	@Test
	public void testNoResultWhenNoSubscription() throws MailException {
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		
		assertThat(subscribedFolders).isEmpty();
	}

	@Test
	public void testNoResultWhenRegularFoldersExist() throws MailException {
		createUnsubscribedFolder(EmailConfiguration.IMAP_DRAFTS_NAME);
		createUnsubscribedFolder(EmailConfiguration.IMAP_SENT_NAME);
		createUnsubscribedFolder(EmailConfiguration.IMAP_TRASH_NAME);
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		
		assertThat(subscribedFolders).isEmpty();
	}

	@Test
	public void testNoResultWhenSubfolderExist() throws MailException {
		createUnsubscribedFolder(EmailConfiguration.IMAP_INBOX_NAME + ".SUBFOLDER");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);

		assertThat(subscribedFolders).isEmpty();
	}

	@Test(expected=MailException.class)
	public void testSubscribeToUnexistingFolder() throws MailException {
		mailboxService.subscribe(udr, "unexistingFolder");
	}
	
	@Test
	public void testSubscribeToFolder() throws MailException {
		OpushImapFolder sharedFolder = createUnsubscribedFolder("sharedFolder");
		mailboxService.subscribe(udr, "sharedFolder");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		MailboxFolders allFolders = mailboxService.listAllFolders(udr);

		assertThat(sharedFolder.isSubscribed()).isTrue();
		assertThat(subscribedFolders).containsOnly(testUtils.folder("sharedFolder"));
		assertThat(allFolders).containsOnly(
				testUtils.inbox(),
				testUtils.folder("sharedFolder"));
	}
	
	@Test
	public void testUnsubscribeToFolder() throws MailException {
		OpushImapFolder sharedFolder = createUnsubscribedFolder("sharedFolder");
		mailboxService.subscribe(udr, "sharedFolder");
		mailboxService.unsubscribe(udr, "sharedFolder");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		MailboxFolders allFolders = mailboxService.listAllFolders(udr);

		assertThat(sharedFolder.isSubscribed()).isFalse();
		assertThat(subscribedFolders).isEmpty();
		assertThat(allFolders).containsOnly(
				testUtils.inbox(),
				testUtils.folder("sharedFolder"));
	}
	
	@Test
	public void testSubscribeAffectOneFolder() throws MailException {
		OpushImapFolder sharedFolder = createUnsubscribedFolder("sharedFolder");
		OpushImapFolder otherSharedFolder = createUnsubscribedFolder("otherSharedFolder");
		mailboxService.subscribe(udr, "sharedFolder");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		MailboxFolders allFolders = mailboxService.listAllFolders(udr);

		assertThat(sharedFolder.isSubscribed()).isTrue();
		assertThat(otherSharedFolder.isSubscribed()).isFalse();
		assertThat(subscribedFolders).containsOnly(testUtils.folder("sharedFolder"));
		assertThat(allFolders).containsOnly(
				testUtils.inbox(),
				testUtils.folder("sharedFolder"),
				testUtils.folder("otherSharedFolder"));
	}
	
	@Test
	public void testUnsubscribeAffectOneFolder() throws MailException {
		OpushImapFolder sharedFolder = createUnsubscribedFolder("sharedFolder");
		OpushImapFolder otherSharedFolder = createUnsubscribedFolder("otherSharedFolder");
		mailboxService.subscribe(udr, "sharedFolder");
		mailboxService.subscribe(udr, "otherSharedFolder");
		mailboxService.unsubscribe(udr, "sharedFolder");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		MailboxFolders allFolders = mailboxService.listAllFolders(udr);

		assertThat(sharedFolder.isSubscribed()).isFalse();
		assertThat(otherSharedFolder.isSubscribed()).isTrue();
		assertThat(subscribedFolders).containsOnly(testUtils.folder("otherSharedFolder"));
		assertThat(allFolders).containsOnly(
				testUtils.inbox(),
				testUtils.folder("sharedFolder"),
				testUtils.folder("otherSharedFolder"));
	}
	
	@Test
	public void testSubscribeToSubFolder() throws MailException {
		OpushImapFolder parentFolder = createUnsubscribedFolder("PARENT");
		OpushImapFolder subFolder = createUnsubscribedFolder("PARENT.CHILD");
		mailboxService.subscribe(udr, "PARENT.CHILD");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		MailboxFolders allFolders = mailboxService.listAllFolders(udr);
		
		assertThat(parentFolder.isSubscribed()).isFalse();
		assertThat(subFolder.isSubscribed()).isTrue();
		assertThat(subscribedFolders).containsOnly(testUtils.folder("PARENT.CHILD"));
		assertThat(allFolders).containsOnly(
				testUtils.inbox(),
				testUtils.folder("PARENT"),
				testUtils.folder("PARENT.CHILD"));
	}
	
	@Test
	public void testSubscribeToParentFolder() throws MailException {
		OpushImapFolder parentFolder = createUnsubscribedFolder("PARENT.CHILD");
		OpushImapFolder subFolder = createUnsubscribedFolder("PARENT.CHILD.SUBCHILD");
		mailboxService.subscribe(udr, "PARENT.CHILD");
		
		MailboxFolders subscribedFolders = mailboxService.listSubscribedFolders(udr);
		MailboxFolders allFolders = mailboxService.listAllFolders(udr);
		
		assertThat(parentFolder.isSubscribed()).isTrue();
		assertThat(subFolder.isSubscribed()).isFalse();
		assertThat(subscribedFolders).containsOnly(testUtils.folder("PARENT.CHILD"));
		assertThat(allFolders).containsOnly(
				testUtils.inbox(),
				testUtils.folder("PARENT"),
				testUtils.folder("PARENT.CHILD"),
				testUtils.folder("PARENT.CHILD.SUBCHILD"));
	}

	private OpushImapFolder createUnsubscribedFolder(String folderName) throws MailException {
		MailboxFolder folder = testUtils.folder(folderName);
		return mailboxService.createFolder(udr, folder);
	}
}
