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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.push.mail.MSMailTestsUtils.loadEmail;

import java.io.InputStream;
import java.util.Set;

import org.columba.ristretto.smtp.SMTPException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.Address;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ResourcesHolder;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.smtp.SmtpSender;
import org.obm.push.minig.imap.StoreClient;

import com.google.common.collect.Sets;

@RunWith(SlowFilterRunner.class)
public class MockBasedImapMailboxServiceTest {

	private UserDataRequest udr;
	
	@Before
	public void setUp() {
		String mailbox = "user@domain";
		String password = "password";
	    udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, new ResourcesHolder());
	}
	
	@Test
	public void testSendEmailWithBigInputStream() throws ProcessingEmailException, StoreEmailException, SendEmailException, SmtpInvalidRcptException, SMTPException {
		
		SmtpSender smtpSender = createMock(SmtpSender.class);
		
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		Set<Address> addrs = Sets.newHashSet();
		smtpSender.sendEmail(anyObject(UserDataRequest.class), anyObject(Address.class),
				anyObject(addrs.getClass()),
				anyObject(addrs.getClass()),
				anyObject(addrs.getClass()), anyObject(InputStream.class));
		expectLastCall().once();
		
		replay(emailConfiguration, smtpSender);
		
		LinagoraMailboxService emailManager = 
				new LinagoraMailboxService(emailConfiguration, smtpSender, null, null);

		emailManager.sendEmail(udr,
				new Address("test@test.fr"),
				addrs,
				addrs,
				addrs,
				loadEmail("bigEml.eml"), false);
		
		verify(emailConfiguration, smtpSender);
	}

 	@Test
	public void testParseSpecificINBOXCase() throws Exception {
		String userINBOXFolder = "INBOX";
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperExtractFolder(userINBOXFolder);
		
		LinagoraImapClientProvider imapClientProvider = createMock(LinagoraImapClientProvider.class);
		StoreClient storeClient = createMock(StoreClient.class);
		expect(imapClientProvider.getImapClient(udr)).andReturn(storeClient);
		expect(storeClient.findMailboxNameWithServerCase(userINBOXFolder)).andReturn(userINBOXFolder);
		
		replay(emailConfiguration, collectionPathHelper, imapClientProvider, storeClient);
		LinagoraMailboxService emailManager = new LinagoraMailboxService(
				emailConfiguration, null, imapClientProvider, collectionPathHelper);

		String parsedMailbox = emailManager.parseMailBoxName(udr, collectionPath(userINBOXFolder));
		verify(emailConfiguration, collectionPathHelper, imapClientProvider, storeClient);
		
		assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_INBOX_NAME);
	}

	@Test
	public void testParseSpecificINBOXCaseIsntCaseSensitive() throws Exception {
		String userINBOXFolder = "InBoX";
		String serverINBOXFolder = "INBOX";
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperExtractFolder(userINBOXFolder);
		
		LinagoraImapClientProvider imapClientProvider = createMock(LinagoraImapClientProvider.class);
		StoreClient storeClient = createMock(StoreClient.class);
		expect(imapClientProvider.getImapClient(udr)).andReturn(storeClient);
		expect(storeClient.findMailboxNameWithServerCase(userINBOXFolder)).andReturn(serverINBOXFolder);

		replay(emailConfiguration, collectionPathHelper, imapClientProvider, storeClient);
		LinagoraMailboxService emailManager = new LinagoraMailboxService(
				emailConfiguration, null, imapClientProvider, collectionPathHelper);

		String parsedMailbox = emailManager.parseMailBoxName(udr, collectionPath(userINBOXFolder));
		verify(emailConfiguration, collectionPathHelper, imapClientProvider, storeClient);
		
		assertThat(parsedMailbox).isEqualTo(EmailConfiguration.IMAP_INBOX_NAME);
	}

	@Test @Slow
	public void testParseINBOXWithOtherFolderEndingByINBOX() throws Exception {
		String folderEndingByINBOX = "userFolder" + EmailConfiguration.IMAP_INBOX_NAME;

		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperExtractFolder(folderEndingByINBOX);
		EmailConfiguration emailConfiguration = newEmailConfigurationMock();

		LinagoraImapClientProvider imapClientProvider = createMock(LinagoraImapClientProvider.class);
		StoreClient storeClient = createMock(StoreClient.class);
		expect(imapClientProvider.getImapClient(udr)).andReturn(storeClient);
		expect(storeClient.findMailboxNameWithServerCase(folderEndingByINBOX)).andReturn(folderEndingByINBOX);

		replay(emailConfiguration, collectionPathHelper, imapClientProvider, storeClient);
		LinagoraMailboxService emailManager = new LinagoraMailboxService(
				emailConfiguration, null, imapClientProvider, collectionPathHelper);

		String parsedMailbox = emailManager.parseMailBoxName(udr, collectionPath(folderEndingByINBOX));
		verify(emailConfiguration, collectionPathHelper, imapClientProvider, storeClient);
		
		assertThat(parsedMailbox).isEqualTo(folderEndingByINBOX);
	}

	private EmailConfiguration newEmailConfigurationMock() {
		EmailConfiguration emailConfiguration = createMock(EmailConfiguration.class);
		expect(emailConfiguration.loginWithDomain()).andReturn(true).once();
		expect(emailConfiguration.activateTls()).andReturn(false).once();
		return emailConfiguration;
	}
	
	private CollectionPathHelper mockCollectionPathHelperExtractFolder(String expectedFolder) throws CollectionPathException {
		CollectionPathHelper helper = createMock(CollectionPathHelper.class);
		expect(helper.extractFolder(udr, collectionPath(expectedFolder), PIMDataType.EMAIL))
			.andReturn(expectedFolder).anyTimes();
		return helper;
	}

	private String collectionPath(String expectedFolder) {
		return "obm:\\\\user@domain\\email\\" + expectedFolder;
	}
}
