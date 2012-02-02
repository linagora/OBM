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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.RandomGeneratedInputStream;
import org.obm.push.mail.StreamMailTestsUtils;
import org.obm.push.mail.greenmail.ClosableProcess;
import org.obm.push.mail.greenmail.ExternalProcessException;
import org.obm.push.mail.greenmail.GreenMailExternalProcess;
import org.obm.push.mail.imap.ImapMailboxService;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;

public class ImapStoreAPIMemoryTest {
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	
	private String mailbox;
	private String password;
	private BackendSession bs;
	private long maxHeapSize;
	
	private ClosableProcess greenMailProcess;
	
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

	
	@Before
	public void setUp() throws ExternalProcessException {
	    mailbox = "to@localhost.com";
	    password = "password";
	    maxHeapSize = getTwiceThisHeapSize();
	    greenMailProcess = new GreenMailExternalProcess(mailbox, password, false, maxHeapSize).execute();
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password, null), null, null, null);
	}

	@After
	public void tearDown() throws InterruptedException {
		greenMailProcess.closeProcess();
	}
	
	private File generateBigEmail(long maxHeapSize) throws IOException,
			FileNotFoundException {
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

	@Test
	public void testStoreInInbox() throws Exception {
		final InputStream tinyInputStream = StreamMailTestsUtils.newInputStreamFromString("test");

		mailboxService.storeInInbox(bs, tinyInputStream, true);

		InputStream fetchMailStream = mailboxService.fetchMailStream(bs, IMAP_INBOX_NAME, 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("test\r\n\r\n");
		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test
	public void testStoreInInboxStream() throws Exception {
		final InputStream tinyInputStream = StreamMailTestsUtils.newInputStreamFromString("test");

		mailboxService.storeInInbox(bs, tinyInputStream, 4, true);

		InputStream fetchMailStream = mailboxService.fetchMailStream(bs, IMAP_INBOX_NAME, 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("test\r\n\r\n");
		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
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
		Set<Email> emails = mailboxService.fetchEmails(bs, EmailConfiguration.IMAP_INBOX_NAME, before);
		Assertions.assertThat(emails).hasSize(1);
	}
	
	@Test
	public void testStoreInInboxMoreThanMemorySizeStream() throws Exception {
		Date before = new Date();
		long size = getTwiceThisHeapSize();
		final InputStream heavyInputStream = new RandomGeneratedInputStream(size);
		mailboxService.storeInInbox(bs, heavyInputStream, Ints.checkedCast(size), true);
		Set<Email> emails = mailboxService.fetchEmails(bs, EmailConfiguration.IMAP_INBOX_NAME, before);
		Assertions.assertThat(emails).hasSize(1);
	}
}
