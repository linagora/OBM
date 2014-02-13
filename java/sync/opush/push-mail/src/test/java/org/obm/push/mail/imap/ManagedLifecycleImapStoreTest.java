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
package org.obm.push.mail.imap;

import org.junit.Ignore;

@Ignore("OBMFULL-4182")
public class ManagedLifecycleImapStoreTest {

//	@Rule
//	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);
//
//	@Inject MailboxService mailboxService;
//	@Inject PrivateMailboxService privateMailboxService;
//	@Inject EmailConfiguration emailConfig;
//	@Inject GreenMail greenMail;
//	@Inject CollectionPathHelper collectionPathHelper;
//	@Inject ImapClientProvider clientProvider;
//	
//	private String mailbox;
//	private String password;
//	private UserDataRequest udr;
//	private Date beforeTest;
//	private ImapTestUtils testUtils;
//
//	@Before
//	public void setUp() {
//		beforeTest = DateUtils.date("1970-01-01T12:00:00");
//		greenMail.start();
//		mailbox = "to@localhost.com";
//		password = "password";
//		greenMail.setUser(mailbox, password);
//		udr = new UserDataRequest(
//				new Credentials(User.Factory.create()
//						.createUser(mailbox, mailbox, null), password), null, null, null);
//		testUtils = new ImapTestUtils(mailboxService, privateMailboxService, udr, mailbox, beforeTest, collectionPathHelper);
//	}
//	
//	@After
//	public void tearDown() {
//		greenMail.stop();
//	}
//
//	@Test
//	public void testIsConnected() throws Exception {
//		ImapStore imapStore = clientProvider.getImapClientWithJM(udr);
//		
//		imapStore.login();
//		
//		Assertions.assertThat(imapStore.isConnected()).isTrue();
//	}
//
//	@Test
//	public void testCloseWhenNoOperation() throws Exception {
//		ImapStore imapStore = clientProvider.getImapClientWithJM(udr);
//		
//		imapStore.login();
//		imapStore.logout();
//		
//		Assertions.assertThat(imapStore.isConnected()).isFalse();
//	}
//
//	@Test
//	public void testCloseAfterNoStreamedOperations() throws Exception {
//		ImapStore imapStore = clientProvider.getImapClientWithJM(udr);
//		
//		String intoFolder = "afolder";
//		imapStore.login();
//		imapStore.create(folder(intoFolder), Folder.HOLDS_MESSAGES|Folder.HOLDS_FOLDERS);
//		imapStore.select(intoFolder);
//		Message message = imapStore.createMessage(loadEmail("plainText.eml"));
//		imapStore.appendMessage(intoFolder, message);
//		imapStore.logout();
//		
//		Assertions.assertThat(imapStore.isConnected()).isFalse();
//	}
//
//	@Test
//	public void testCloseAfterStreamedOperation() throws Exception {
//		Email sentEmail = testUtils.sendEmailToInbox();
//		ImapStore imapStore = clientProvider.getImapClientWithJM(udr);
//		
//		imapStore.login();
//		OpushImapFolder imapFolder = imapStore.select(EmailConfiguration.IMAP_INBOX_NAME);
//		imapFolder.getMessageInputStream(sentEmail.getUid());
//		imapStore.logout();
//		
//		Assertions.assertThat(imapStore.isConnected()).isTrue();
//	}
//
//	@Test
//	public void testCloseAfterStreamedOperationButConsumed() throws Exception {
//		Email sentEmail = testUtils.sendEmailToInbox();
//		ImapStore imapStore = clientProvider.getImapClientWithJM(udr);
//		
//		imapStore.login();
//		OpushImapFolder imapFolder = imapStore.select(EmailConfiguration.IMAP_INBOX_NAME);
//		InputStream messageAsStream = imapFolder.getMessageInputStream(sentEmail.getUid());
//		imapStore.logout();
//		messageAsStream.close();
//		
//		Assertions.assertThat(imapStore.isConnected()).isFalse();
//	}
//
//	@Test
//	public void testCloseAfterStreamedOperationWhenCloseOnStreamCalledAfter() throws Exception {
//		Email sentEmail = testUtils.sendEmailToInbox();
//		ImapStore imapStore = clientProvider.getImapClientWithJM(udr);
//		
//		imapStore.login();
//		OpushImapFolder imapFolder = imapStore.select(EmailConfiguration.IMAP_INBOX_NAME);
//		InputStream messageAsStream = imapFolder.getMessageInputStream(sentEmail.getUid());
//		imapStore.logout();
//		messageAsStream.close();
//		
//		Assertions.assertThat(imapStore.isConnected()).isFalse();
//	}
//
//	public MailboxFolder folder(String name) {
//		return new MailboxFolder(name);
//	}
}
