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

import java.io.InputStream;
import java.util.Set;

import org.columba.ristretto.message.Address;
import org.easymock.EasyMock;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.StoreClient;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.store.LocatorService;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathUtils;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.smtp.SmtpSender;

import com.google.common.collect.Sets;

public class EmailManagerTest {

	private BackendSession bs;
	
	@Before
	public void setUp() {
		User user = Factory.create().createUser("user@domain", "user@domain");
		bs = new BackendSession(new Credentials(user, "test"),
				null, null, null);
	}
	
	@Test
	public void testSendEmailWithBigInputStream() throws ProcessingEmailException, StoreEmailException, SendEmailException, SmtpInvalidRcptException {
		
		LocatorService locatorService = EasyMock.createMock(LocatorService.class);
		SmtpSender smtpSender = EasyMock.createMock(SmtpSender.class);
		
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		Set<Address> addrs = Sets.newHashSet();
		smtpSender.sendEmail(EasyMock.anyObject(BackendSession.class), EasyMock.anyObject(Address.class),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(InputStream.class));
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(emailConfiguration, smtpSender);
		
		EmailManager emailManager = new EmailManager(null, emailConfiguration, smtpSender, null, locatorService, null);

		emailManager.sendEmail(bs,
				new Address("test@test.fr"),
				addrs,
				addrs,
				addrs,
				loadDataFile("bigEml.eml"), false);
		
		EasyMock.verify(emailConfiguration, smtpSender);
	}

	@Test
	public void testParseSentMailBox() throws Exception {
		StoreClient storeClient = newStoreClientMock("Sent");
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		EasyMock.replay(emailConfiguration, storeClient);
		
		EmailManager emailManager = new EmailManager(null, emailConfiguration, null, null, null, null);

		String userSentFolder = 
				CollectionPathUtils.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = emailManager.parseMailBoxName(bs, storeClient, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_SENT_NAME);
	}

	@Test
	public void testParseSentMailBoxSentIsInsensitive() throws Exception {
		StoreClient storeClient = newStoreClientMock("SeNt");
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		EasyMock.replay(emailConfiguration, storeClient);
		
		EmailManager emailManager = new EmailManager(null, emailConfiguration, null, null, null, null);

		String userSentFolder = 
				CollectionPathUtils.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = emailManager.parseMailBoxName(bs, storeClient, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo("SeNt");
	}
	
	@Test
	public void testParseSentMailBoxWhenManyNamedSentBox() throws Exception {
		StoreClient storeClient = newStoreClientMock("AnyFolderSent", "Sent", "SENT", "AnotherSentfolder");
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		EasyMock.replay(emailConfiguration, storeClient);
		
		EmailManager emailManager = new EmailManager(null, emailConfiguration, null, null, null, null);

		String userSentFolder = 
				CollectionPathUtils.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = emailManager.parseMailBoxName(bs, storeClient, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_SENT_NAME);
	}
	
	@Test
	public void testParseSentMailBox_OBMFULL3133() throws Exception {
		StoreClient storeClient = newStoreClientMock(
				"Bo&AO4-tes partag&AOk-es/696846/Sent", "Sent", "Bo&AO4-tes partag&AOk-es/696846/Sent");
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		EasyMock.replay(emailConfiguration, storeClient);
		
		EmailManager emailManager = new EmailManager(null, emailConfiguration, null, null, null, null);

		String userSentFolder = 
				CollectionPathUtils.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		String parsedMailbox = emailManager.parseMailBoxName(bs, storeClient, userSentFolder);
		Assertions.assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_SENT_NAME);
	}

	private StoreClient newStoreClientMock(String...allUserFolders) {
		StoreClient storeClient = EasyMock.createMock(StoreClient.class);
		ListResult existingFolder = newListResult(allUserFolders);
		EasyMock.expect(storeClient.listAll()).andReturn(existingFolder);
		return storeClient;
	}
	
	private ListResult newListResult(String...itemsName) {
		ListResult list = new ListResult(itemsName.length);
		for (String itemName : itemsName) {
			list.add(new ListInfo(itemName, true, false));
		}
		return list;
	}

	private EmailConfiguration newEmailConfigurationMock() {
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfiguration.class);
		EasyMock.expect(emailConfiguration.loginWithDomain()).andReturn(true).once();
		EasyMock.expect(emailConfiguration.activateTls()).andReturn(false).once();
		return emailConfiguration;
	}

	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"eml/" + name);
	}
}
