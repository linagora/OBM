/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.mailbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.exception.ImapAnnotationException;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapDeleteException;
import org.obm.imap.archive.exception.ImapQuotaException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.mail.bean.AnnotationEntry;
import org.obm.push.mail.bean.AttributeValue;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;


public class TemporaryMailboxTest {

	private IMocksControl control;
	
	@Before
	public void setup() {
		control = createControl();
	}
	
	@Test(expected=NullPointerException.class)
	public void mailboxShouldNotBeNull() {
		TemporaryMailbox.builder().from(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void domainNameShouldNotBeNull() {
		TemporaryMailbox.builder().domainName(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void cyrusPartitionSuffixShouldNotBeNull() {
		TemporaryMailbox.builder().cyrusPartitionSuffix(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void mailboxShouldBeProvided() throws Exception {
		TemporaryMailbox.builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void domainNameShouldBeProvided() throws Exception {
		Mailbox mailbox = control.createMock(Mailbox.class);
		
		try {
			control.replay();
			TemporaryMailbox.builder().from(mailbox).build();
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void cyrusPartitionSuffixShouldBeProvided() throws Exception {
		Mailbox mailbox = control.createMock(Mailbox.class);
		
		try {
			control.replay();
			TemporaryMailbox.builder()
				.from(mailbox)
				.domainName(new DomainName("mydomain.org"))
				.build();
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void shouldBuildWhenEveryThingProvided() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		TemporaryMailbox.builder()
			.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
			.domainName(new DomainName("mydomain.org"))
			.cyrusPartitionSuffix("archive")
			.build();
		control.verify();
	}
	
	@Test
	public void temporaryMailboxShouldWorkWhenMailboxIsINBOX() throws Exception {
		TestMailbox mailbox = new TestMailbox("user/usera@mydomain.org", null, null, false);

		MailboxPaths temporaryMailbox = TemporaryMailbox.Builder.temporaryMailbox(mailbox);
		assertThat(temporaryMailbox.getName()).isEqualTo("user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org");
	}
	
	@Test
	public void temporaryMailboxShouldWorkWhenMailboxIsAFolder() throws Exception {
		TestMailbox mailbox = new TestMailbox("user/usera/Test@mydomain.org", null, null, false);
		
		MailboxPaths temporaryMailbox = TemporaryMailbox.Builder.temporaryMailbox(mailbox);
		assertThat(temporaryMailbox.getName()).isEqualTo("user/usera/TEMPORARY_ARCHIVE_FOLDER/Test@mydomain.org");
	}
	
	@Test
	public void temporaryMailboxShouldWorkWhenMailboxIsASubFolder() throws Exception {
		TestMailbox mailbox = new TestMailbox("user/usera/Test/subfolder@mydomain.org", null, null, false);
		
		MailboxPaths temporaryMailbox = TemporaryMailbox.Builder.temporaryMailbox(mailbox);
		assertThat(temporaryMailbox.getName()).isEqualTo("user/usera/TEMPORARY_ARCHIVE_FOLDER/Test/subfolder@mydomain.org");
	}
	
	@Test(expected=MailboxFormatException.class)
	public void temporaryMailboxShouldThrowWhenBadMailbox() throws Exception {
		TestMailbox mailbox = new TestMailbox("user", null, null, false);
		
		TemporaryMailbox.Builder.temporaryMailbox(mailbox);
	}
	
	private static class TestMailbox extends MailboxImpl {

		protected TestMailbox(String name, Logger logger, StoreClient storeClient, boolean sharedMailbox) {
			super(name, logger, storeClient, sharedMailbox);
		}
	}
	@Test
	public void deleteShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.delete("user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org"))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject(String.class));
		expectLastCall().anyTimes();
		
		control.replay();
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
			.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
			.domainName(new DomainName("mydomain.org"))
			.cyrusPartitionSuffix("archive")
			.build();
		temporaryMailbox.delete();
		control.verify();
	}
	
	@Test(expected=ImapDeleteException.class)
	public void deleteShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.delete("user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org"))
			.andReturn(false);
		
		try {
			control.replay();
			TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
					.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
					.domainName(new DomainName("mydomain.org"))
					.cyrusPartitionSuffix("archive")
					.build();
			temporaryMailbox.delete();
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void createShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.create("user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org", "mydomain_org_archive"))
			.andReturn(true);
		logger.debug(anyObject(String.class));
		expectLastCall().anyTimes();
		
		control.replay();
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
				.domainName(new DomainName("mydomain.org"))
				.cyrusPartitionSuffix("archive")
				.build();
		temporaryMailbox.create();
		control.verify();
	}
	
	@Test(expected=ImapCreateException.class)
	public void createShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.create("user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org", "mydomain_org_archive"))
			.andReturn(false);

		try {
			control.replay();
			TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
					.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
					.domainName(new DomainName("mydomain.org"))
					.cyrusPartitionSuffix("archive")
					.build();
			temporaryMailbox.create();
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void setMaxQuotaShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
				.domainName(new DomainName("mydomain.org"))
				.cyrusPartitionSuffix("archive")
				.build();
		int quotaInKo = 1234;
		expect(storeClient.setQuota(temporaryMailbox.getName(), quotaInKo))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject(String.class));
		expectLastCall().anyTimes();
		
		control.replay();
		temporaryMailbox.setMaxQuota(1234);
		control.verify();
	}
	
	@Test(expected=ImapQuotaException.class)
	public void setMaxQuotaShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
				.domainName(new DomainName("mydomain.org"))
				.cyrusPartitionSuffix("archive")
				.build();
		int quotaInKo = 1234;
		expect(storeClient.setQuota(temporaryMailbox.getName(), quotaInKo))
			.andReturn(false);
		
		try {
			control.replay();
			temporaryMailbox.setMaxQuota(1234);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void setSharedSeenAnnotationShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
				.domainName(new DomainName("mydomain.org"))
				.cyrusPartitionSuffix("archive")
				.build();
		expect(storeClient.setAnnotation(temporaryMailbox.getName(), AnnotationEntry.SHAREDSEEN, AttributeValue.sharedValue("true")))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject(String.class));
		expectLastCall().anyTimes();

		control.replay();
		temporaryMailbox.setSharedSeenAnnotation();
		control.verify();
	}
	
	@Test(expected=ImapAnnotationException.class)
	public void setSharedSeenAnnotationShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient, false))
				.domainName(new DomainName("mydomain.org"))
				.cyrusPartitionSuffix("archive")
				.build();
		expect(storeClient.setAnnotation(temporaryMailbox.getName(), AnnotationEntry.SHAREDSEEN, AttributeValue.sharedValue("true")))
			.andReturn(false);

		try {
			control.replay();
			temporaryMailbox.setSharedSeenAnnotation();
		} finally {
			control.verify();
		}
	}
}
