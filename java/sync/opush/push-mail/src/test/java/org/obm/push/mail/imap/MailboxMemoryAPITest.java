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

import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import javax.mail.util.SharedFileInputStream;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.RandomGeneratedInputStream;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.greenmail.ClosableProcess;
import org.obm.push.mail.greenmail.ExternalProcessException;
import org.obm.push.mail.greenmail.GreenMailExternalProcess;
import org.obm.push.mail.greenmail.GreenMailServerUtil;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.service.OpushLocatorService;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModule(MailEnvModule.class)
public class MailboxMemoryAPITest {
	
	@Inject MailboxService mailboxService;
	@Inject EmailConfiguration emailConfiguration;
	@Inject OpushLocatorService locatorService;
	@Inject ICollectionPathHelper collectionPathHelper;
	
	private String mailbox;
	private String password;
	private UserDataRequest udr;
	private long maxHeapSize;
	private String inboxPath;
	
	@Inject GreenMailExternalProcess greenMailExternalProcess;
	private ClosableProcess greenMailProcess;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Before
	public void setUp() throws ExternalProcessException, InterruptedException {
		mailbox = "to@localhost.com";
		password = "password";
		maxHeapSize = getTwiceThisHeapSize();
		greenMailExternalProcess.setHeapMaxSize(maxHeapSize);
		greenMailProcess = greenMailExternalProcess.startGreenMail(mailbox, password);
		
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
		String imapLocation = locatorService.getServiceLocation("mail/imap_frontend", udr.getUser().getLoginAtDomain());
		GreenMailServerUtil.waitForGreenmailAvailability(imapLocation, greenMailExternalProcess.getImapPort());
		GreenMailServerUtil.waitForGreenmailAvailability(imapLocation, greenMailExternalProcess.getSmtpPort());
		inboxPath = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, IMAP_INBOX_NAME);
	}

	@After
	public void tearDown() throws InterruptedException {
		greenMailProcess.closeProcess();
	}
	
	private File generateBigEmail(long maxHeapSize) throws IOException, FileNotFoundException {
		File data = folder.newFile("test-data");
		FileOutputStream fileOutputStream = new FileOutputStream(data);
		ByteStreams.copy(StreamMailTestsUtils.getHeaders(), fileOutputStream);
		ByteStreams.copy(new RandomGeneratedInputStream(maxHeapSize), fileOutputStream);
		fileOutputStream.close();
		return data;
	}
	
	private long getTwiceThisHeapSize() {
		long thisHeapSizeInByte = Runtime.getRuntime().maxMemory();
		return thisHeapSizeInByte * 2;
	}

	@Test(expected=OutOfMemoryError.class)
	public void testBigMailTriggerOutOfMemory() throws Exception {
		File data = generateBigEmail(maxHeapSize);
		ByteStreams.toByteArray(new FileInputStream(data));
	}

	@Ignore("StoreInInbox isn't streamed at all with the 'imap' lib")
	@Test
	public void testStoreInInboxMoreThanMemorySize() throws Exception {
		Date before = new Date();
		File data = generateBigEmail(getTwiceThisHeapSize());
		InputStream heavyInputStream = new SharedFileInputStream(data);
		
		MailboxTestUtils.storeInInbox(udr, mailboxService, heavyInputStream);
		
		Set<Email> emails = mailboxService.fetchEmails(udr, inboxPath, before);
		Assertions.assertThat(emails).hasSize(1);
	}

	@Ignore("This test is too long to be executed during the development phase")
	@Test
	public void testFetchPartMoreThanMemorySize() throws Exception {
		File data = generateBigEmail(maxHeapSize);
		InputStream heavyInputStream = new SharedFileInputStream(data);
		MailboxTestUtils.storeInInbox(udr, mailboxService, heavyInputStream);
		
		InputStream fetchPart = uidFetchPart(1, "1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(new RandomGeneratedInputStream(maxHeapSize));
	}

	private InputStream uidFetchPart(long uid, String partToFetch) throws Exception {
		return mailboxService.findAttachment(udr, EmailConfiguration.IMAP_INBOX_NAME, uid, 
						new MimeAddress(partToFetch));
	}
	
}
