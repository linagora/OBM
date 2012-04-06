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

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.rules.TemporaryFolder;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.store.LocatorService;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailTestsUtils;
import org.obm.push.mail.MimeAddress;
import org.obm.push.mail.RandomGeneratedInputStream;
import org.obm.push.mail.greenmail.ClosableProcess;
import org.obm.push.mail.greenmail.ExternalProcessException;
import org.obm.push.mail.greenmail.GreenMailExternalProcess;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;

import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class) @Slow
public class ImapMemoryAPITest {
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	@Inject EmailConfiguration emailConfiguration;
	@Inject LocatorService locatorService;
	@Inject ImapClientProvider clientProvider;

	@Inject CollectionPathHelper collectionPathHelper;
	private String mailbox;
	private String password;
	private BackendSession bs;
	private long maxHeapSize;
	private String inboxPath;
	
	private ClosableProcess greenMailProcess;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();


	
	@Before
	public void setUp() throws ExternalProcessException, InterruptedException {
		mailbox = "to@localhost.com";
		password = "password";
		maxHeapSize = getTwiceThisHeapSize();
		greenMailProcess = new GreenMailExternalProcess(mailbox, password, false, maxHeapSize).execute();
		bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		String imapLocation = locatorService.getServiceLocation("mail/imap_frontend", bs.getUser().getLoginAtDomain());
		MailTestsUtils.waitForGreenmailAvailability(imapLocation, emailConfiguration.imapPort());
		inboxPath = collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, IMAP_INBOX_NAME);
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

	@Test
	public void testStoreInInboxMoreThanMemorySize() throws Exception {
		Date before = new Date();
		File data = generateBigEmail(getTwiceThisHeapSize());
		final InputStream heavyInputStream = new SharedFileInputStream(data);
		mailboxService.storeInInbox(bs, heavyInputStream, true);
		Set<Email> emails = mailboxService.fetchEmails(bs, inboxPath, before);
		Assertions.assertThat(emails).hasSize(1);
	}

	@Test
	public void testStoreInInboxMoreThanMemorySizeStream() throws Exception {
		Date before = new Date();
		long size = getTwiceThisHeapSize();
		final InputStream heavyInputStream = new RandomGeneratedInputStream(size);
		mailboxService.storeInInbox(bs, heavyInputStream, Ints.checkedCast(size), true);
		Set<Email> emails = mailboxService.fetchEmails(bs, inboxPath, before);
		Assertions.assertThat(emails).hasSize(1);
	}
	
	@Test
	public void testFetchMailStream() throws Exception {
		long size = getTwiceThisHeapSize();
		final InputStream heavyInputStream = new RandomGeneratedInputStream(size);
		mailboxService.storeInInbox(bs, heavyInputStream, Ints.checkedCast(size), true);
		InputStream stream = mailboxService.fetchMailStream(bs, inboxPath, 1L);
		Assertions.assertThat(stream).hasContentEqualTo(new RandomGeneratedInputStream(size));
	}

	@Ignore("This test is too long to be executed during the development phase")
	@Test
	public void testFetchPartMoreThanMemorySize() throws Exception {
		File data = generateBigEmail(maxHeapSize);
		final InputStream heavyInputStream = new SharedFileInputStream(data);
		mailboxService.storeInInbox(bs, heavyInputStream, true);

		InputStream fetchPart = uidFetchPart(1, "1");
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(new RandomGeneratedInputStream(maxHeapSize));
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
