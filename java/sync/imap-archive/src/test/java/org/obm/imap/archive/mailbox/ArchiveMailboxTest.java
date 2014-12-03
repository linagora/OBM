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
import org.obm.imap.archive.beans.Year;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapQuotaException;
import org.obm.imap.archive.exception.ImapStoreException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;


public class ArchiveMailboxTest {

	private IMocksControl control;
	
	@Before
	public void setup() {
		control = createControl();
	}
	
	@Test(expected=NullPointerException.class)
	public void mailboxShouldNotBeNull() {
		ArchiveMailbox.builder().from(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void yearShouldNotBeNull() {
		ArchiveMailbox.builder().year(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void domainNameShouldNotBeNull() {
		ArchiveMailbox.builder().domainName(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void archiveMainFolderShouldNotBeNull() {
		ArchiveMailbox.builder().archiveMainFolder(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void cyrusPartitionSuffixShouldNotBeNull() {
		ArchiveMailbox.builder().cyrusPartitionSuffix(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void mailboxShouldBeProvided() throws Exception {
		ArchiveMailbox.builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void yearShouldBeProvided() throws Exception {
		Mailbox mailbox = control.createMock(Mailbox.class);
		
		try {
			control.replay();
			ArchiveMailbox.builder().from(mailbox).build();
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void domainNameShouldBeProvided() throws Exception {
		Mailbox mailbox = control.createMock(Mailbox.class);
		
		try {
			control.replay();
			ArchiveMailbox.builder().from(mailbox).year(Year.from(2015)).build();
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void archiveMainFolderShouldBeProvided() throws Exception {
		Mailbox mailbox = control.createMock(Mailbox.class);
		
		try {
			control.replay();
			ArchiveMailbox.builder()
				.from(mailbox)
				.year(Year.from(2015))
				.domainName(new DomainName("mydomain.org"))
				.build();
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void cyrusPartitionSuffixShouldBeProvided() throws Exception {
		Mailbox mailbox = control.createMock(Mailbox.class);
		
		try {
			control.replay();
			ArchiveMailbox.builder()
				.from(mailbox)
				.year(Year.from(2015))
				.domainName(new DomainName("mydomain.org"))
				.archiveMainFolder("ARCHIVE")
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
		ArchiveMailbox.builder()
			.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
			.year(Year.from(2015))
			.domainName(new DomainName("mydomain.org"))
			.archiveMainFolder("ARCHIVE")
			.cyrusPartitionSuffix("archive")
			.build();
		control.verify();
	}
	
	@Test
	public void getYearShouldWork() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		Year expectedYear = Year.from(2015);
		
		control.replay();
		ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
			.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
			.year(expectedYear)
			.domainName(new DomainName("mydomain.org"))
			.archiveMainFolder("ARCHIVE")
			.cyrusPartitionSuffix("archive")
			.build();
		control.verify();
		assertThat(archiveMailbox.getYear()).isEqualTo(expectedYear);
	}
	
	@Test
	public void archiveMailboxShouldWorkWhenMailboxIsINBOX() throws Exception {
		String mailbox = "user/usera@mydomain.org";
		
		MailboxPaths archiveMailbox = ArchiveMailbox.Builder.archiveMailbox(mailbox, Year.from(2014), "ARCHIVE");
		assertThat(archiveMailbox.getName()).isEqualTo("user/usera/ARCHIVE/2014/INBOX@mydomain.org");
	}
	
	@Test
	public void archiveMailboxShouldWorkWhenMailboxIsAFolder() throws Exception {
		String mailbox = "user/usera/Test@mydomain.org";
		
		MailboxPaths archiveMailbox = ArchiveMailbox.Builder.archiveMailbox(mailbox, Year.from(2014), "ARCHIVE");
		assertThat(archiveMailbox.getName()).isEqualTo("user/usera/ARCHIVE/2014/Test@mydomain.org");
	}
	
	@Test
	public void archiveMailboxShouldWorkWhenMailboxIsASubFolder() throws Exception {
		String mailbox = "user/usera/Test/subfolder@mydomain.org";
		
		MailboxPaths archiveMailbox = ArchiveMailbox.Builder.archiveMailbox(mailbox, Year.from(2014), "ARCHIVE");
		assertThat(archiveMailbox.getName()).isEqualTo("user/usera/ARCHIVE/2014/Test/subfolder@mydomain.org");
	}

	@Test(expected=MailboxFormatException.class)
	public void archiveMailboxShouldThrowWhenBadMailbox() throws Exception {
		String mailbox = "user";
		
		ArchiveMailbox.Builder.archiveMailbox(mailbox, Year.from(2014), "ARCHIVE");
	}
	
	@Test
	public void fromShouldBuild() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
			.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
			.year(Year.from(2015))
			.domainName(new DomainName("mydomain.org"))
			.archiveMainFolder("ARCHIVE")
			.cyrusPartitionSuffix("archive")
			.build();
		control.verify();
		
		assertThat(archiveMailbox.getName()).isEqualTo("user/usera/ARCHIVE/2015/INBOX@mydomain.org");
		assertThat(archiveMailbox.getUserAtDomain()).isEqualTo("usera@mydomain.org");
	}
	
	@Test
	public void createShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.create("user/usera/ARCHIVE/2015/INBOX@mydomain.org", "mydomain_org_archive"))
			.andReturn(true);
		logger.debug(anyObject(String.class));
		expectLastCall().anyTimes();
		
		control.replay();
		ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
				.year(Year.from(2015))
				.domainName(new DomainName("mydomain.org"))
				.archiveMainFolder("ARCHIVE")
				.cyrusPartitionSuffix("archive")
				.build();
		archiveMailbox.create();
		control.verify();
	}
	
	@Test(expected=ImapCreateException.class)
	public void createShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.create("user/usera/ARCHIVE/2015/INBOX@mydomain.org", "mydomain_org_archive"))
			.andReturn(false);
		
		try {
			control.replay();
			ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
					.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
					.year(Year.from(2015))
					.domainName(new DomainName("mydomain.org"))
					.archiveMainFolder("ARCHIVE")
					.cyrusPartitionSuffix("archive")
					.build();
			archiveMailbox.create();
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void uidStoreSeenShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		MessageSet messageSet = MessageSet.builder().add(12).add(13).add(14).build();
		expect(storeClient.uidStore(messageSet, new FlagsList(ImmutableSet.of(Flag.SEEN)), true))
			.andReturn(true);
		logger.debug(anyObject(String.class));
		expectLastCall().anyTimes();
		
		control.replay();
		ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
				.year(Year.from(2015))
				.domainName(new DomainName("mydomain.org"))
				.archiveMainFolder("ARCHIVE")
				.cyrusPartitionSuffix("archive")
				.build();
		archiveMailbox.uidStoreSeen(messageSet);
		control.verify();
	}
	
	@Test(expected=ImapStoreException.class)
	public void uidStoreSeenShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		MessageSet messageSet = MessageSet.builder().add(12).add(13).add(14).build();
		expect(storeClient.uidStore(messageSet, new FlagsList(ImmutableSet.of(Flag.SEEN)), true))
			.andReturn(false);
		
		try {
			control.replay();
			ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
					.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
					.year(Year.from(2015))
					.domainName(new DomainName("mydomain.org"))
					.archiveMainFolder("ARCHIVE")
					.cyrusPartitionSuffix("archive")
					.build();
			archiveMailbox.uidStoreSeen(messageSet);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void setMaxQuotaShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
				.year(Year.from(2015))
				.domainName(new DomainName("mydomain.org"))
				.archiveMainFolder("ARCHIVE")
				.cyrusPartitionSuffix("archive")
				.build();
		int quotaInKo = 1234;
		expect(storeClient.setQuota(archiveMailbox.getName(), quotaInKo))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject(String.class));
		expectLastCall().anyTimes();
		
		control.replay();
		archiveMailbox.setMaxQuota(1234);
		control.verify();
	}
	
	@Test(expected=ImapQuotaException.class)
	public void setMaxQuotaShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
				.from(MailboxImpl.from("user/usera@mydomain.org", logger, storeClient))
				.year(Year.from(2015))
				.domainName(new DomainName("mydomain.org"))
				.archiveMainFolder("ARCHIVE")
				.cyrusPartitionSuffix("archive")
				.build();
		int quotaInKo = 1234;
		expect(storeClient.setQuota(archiveMailbox.getName(), quotaInKo))
			.andReturn(false);
		
		try {
			control.replay();
			archiveMailbox.setMaxQuota(1234);
		} finally {
			control.verify();
		}
	}
}
