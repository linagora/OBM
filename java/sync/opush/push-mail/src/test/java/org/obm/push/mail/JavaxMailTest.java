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

import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.store.LocatorService;
import org.obm.opush.env.JUnitGuiceRule;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

public class JavaxMailTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject LocatorService locatorService;
	@Inject EmailConfiguration emailConfig;
	
	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private String emailHost;

	@Before
	public void setUp() {
	    greenMail.start();
	    mailbox = "to@localhost.com";
	    password = "password";
	    greenMail.setUser(mailbox, password);
	    emailHost = locatorService.getServiceLocation("mail/imap_frontend", mailbox);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test
	public void testReceiveMailUsingJavaMail() throws InterruptedException, NoSuchProviderException, MessagingException {
		sendOneEmail();
		greenMail.waitForIncomingEmail(1);

		Collection<Message> emails = getUserMails(emailHost, emailConfig.imapPort());
		Assertions.assertThat(emails).isNotNull().hasSize(1);
	}

	private Collection<Message> getUserMails(String host, int port)
			throws MessagingException {
		Store userStore = getConnectedUserImapStore(host, port);
		Folder userInboxFolder = getUserInboxFolder(userStore);
		return openFolderAndGetMessages(userInboxFolder);
	}

	private Store getConnectedUserImapStore(String host, int port)
			throws NoSuchProviderException, MessagingException {
		Session session = Session.getDefaultInstance(new Properties());
		Store imapStore = session.getStore("imap");
		imapStore.connect(host, port, mailbox, password);
		return imapStore;
	}

	private Folder getUserInboxFolder(Store userStore) throws MessagingException {
		Folder userRootFolder = userStore.getDefaultFolder();
		Folder userInboxFolder = userRootFolder.getFolder(IMAP_INBOX_NAME);
		return userInboxFolder;
	}
	
	private Collection<Message> openFolderAndGetMessages(Folder userInboxFolder)
			throws MessagingException {
		userInboxFolder.open(Folder.READ_ONLY);
		return Arrays.asList(userInboxFolder.getMessages());
	}

	private void sendOneEmail() {
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
	}
}
