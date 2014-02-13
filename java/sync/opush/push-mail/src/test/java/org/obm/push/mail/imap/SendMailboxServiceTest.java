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

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.MailboxFolder;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(GuiceRunner.class)
@GuiceModule(MailEnvModule.class)
public class SendMailboxServiceTest {

	@Inject MailboxService mailboxService;

	@Inject GreenMail greenMail;
	@Inject ICollectionPathHelper collectionPathHelper;
	private String mailbox;
	private String password;
	private UserDataRequest udr;


	@Before
	public void setUp() {
	    greenMail.start();
	    mailbox = "to@localhost.com";
	    password = "password";
	    greenMail.setUser(mailbox, password);
	    udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test
	public void testParseSentMailBox() throws Exception {
		mailboxService.createFolder(udr, folder("Sent"));

		String userSentFolder = 
				collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = mailboxService.parseMailBoxName(udr, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_SENT_NAME);
	}

	@Test
	public void testParseSentMailBoxSentIsInsensitive() throws Exception {
		mailboxService.createFolder(udr, folder("SeNt"));

		String userSentFolder = 
				collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = mailboxService.parseMailBoxName(udr, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo("SeNt");
	}

	@Test
	public void testParseSentMailBoxWhenManyNamedSentBox() throws Exception {
		mailboxService.createFolder(udr, folder("AnyFolderSent"));
		mailboxService.createFolder(udr, folder("Sent"));
		mailboxService.createFolder(udr, folder("AnotherSentfolder"));

		String userSentFolder = 
				collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = mailboxService.parseMailBoxName(udr, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_SENT_NAME);
	}
	
	@Test
	public void testParseSentMailBox_OBMFULL3133() throws Exception {
		mailboxService.createFolder(udr, folder("Bo&AO4-tes partag&AOk-es.696846.Sent"));
		mailboxService.createFolder(udr, folder("Sent"));
		mailboxService.createFolder(udr, folder("Bo&AO4-tes partag&AOk-es.6968426.Sent"));

		String userSentFolder = 
				collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = mailboxService.parseMailBoxName(udr, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_SENT_NAME);
	}
	
	private MailboxFolder folder(String name) {
		return new MailboxFolder(name);
	}
}
