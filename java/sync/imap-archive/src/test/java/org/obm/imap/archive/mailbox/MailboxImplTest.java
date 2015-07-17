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

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.ImapSetAclException;
import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.google.common.collect.ImmutableList;


public class MailboxImplTest {

	private IMocksControl control;
	private Logger logger;
	private StoreClient storeClient;

	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	
	@Before
	public void setup() {
		control = createControl();
		logger = control.createMock(Logger.class);
		
		storeClient = control.createMock(StoreClient.class);
	}
	
	@Test(expected=NullPointerException.class)
	public void nameShouldNotBeNull() {
		MailboxImpl.from(null, null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void nameShouldNotBeEmpty() {
		MailboxImpl.from("", null, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void loggerShouldBeProvided() {
		MailboxImpl.from("mailbox", null, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void storeClientShouldBeProvided() {
		MailboxImpl.from("mailbox", logger, null);
	}
	
	@Test
	public void getName() {
		String expectedName = "mailbox";
		
		MailboxImpl mailbox = MailboxImpl.from("mailbox", logger, storeClient);
		String name = mailbox.getName();
		assertThat(name).isEqualTo(expectedName);
	}
	
	@Test
	public void getLogger() {
		Logger expectedlogger = logger;
		
		MailboxImpl mailbox = MailboxImpl.from("mailbox", logger, storeClient);
		Logger logger = mailbox.getLogger();
		assertThat(logger).isEqualTo(expectedlogger);
	}
	
	@Test
	public void getStoreClient() {
		StoreClient expectedStoreClient = storeClient;
		
		MailboxImpl mailbox = MailboxImpl.from("mailbox", logger, storeClient);
		StoreClient storeClient = mailbox.getStoreClient();
		assertThat(storeClient).isEqualTo(expectedStoreClient);
	}
	
	@Test
	public void selectShouldNotThrowWhenSuccess() throws Exception {
		expect(storeClient.select("mailbox"))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject());
		expectLastCall().anyTimes();
		
		control.replay();
		MailboxImpl mailbox = MailboxImpl.from("mailbox", logger, storeClient);
		mailbox.select();
		control.verify();
	}
	
	@Test
	public void selectShouldThrowWhenError() throws Exception {
		expect(storeClient.select("mailbox"))
			.andReturn(false);
		logger.error(anyObject(String.class));
		expectLastCall();
		
		expectedException.expect(ImapSelectException.class);
		
		control.replay();
		MailboxImpl mailbox = MailboxImpl.from("mailbox", logger, storeClient);
		mailbox.select();
		control.verify();
	}
	
	@Test
	public void setAclShouldReturnTrueWhenSuccess() throws Exception {
		String user = "user";
		String acl = "lr";
		expect(storeClient.setAcl("mailbox", user, acl))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject(), anyObject(), anyObject());
		expectLastCall().anyTimes();
		
		control.replay();
		MailboxImpl mailbox = MailboxImpl.from("mailbox", logger, storeClient);
		mailbox.setAcl(user, acl);
		control.verify();
	}
	
	@Test
	public void setAclShouldReturnFalseWhenError() throws Exception {
		String user = "user";
		String acl = "lr";
		expect(storeClient.setAcl("mailbox", user, acl))
			.andReturn(false);
		logger.error(anyObject(String.class));
		expectLastCall();
		
		expectedException.expect(ImapSetAclException.class);
		
		control.replay();
		MailboxImpl mailbox = MailboxImpl.from("mailbox", logger, storeClient);
		mailbox.setAcl(user, acl);
		control.verify();
	}
	
	@Test
	public void uidCopyShouldReturnMessageSet() throws Exception {
		MessageSet expectedMessageSet = MessageSet.builder().add(12).add(13).add(15).build();
		expect(storeClient.uidCopy(expectedMessageSet, "user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org"))
			.andReturn(expectedMessageSet);
		
		control.replay();
		MailboxImpl mailbox = MailboxImpl.from("user/usera@mydomain.org", logger, storeClient);
		MessageSet messageSet = mailbox.uidCopy(expectedMessageSet, TemporaryMailbox.builder()
				.from(mailbox)
				.domainName(new DomainName("mydomain.org"))
				.cyrusPartitionSuffix("archive")
				.build());
		control.verify();
		
		assertThat(messageSet).isEqualTo(expectedMessageSet);
	}
	
	@Test(expected=MailboxNotFoundException.class)
	public void uidCopyShouldThrow() throws Exception {
		MessageSet expectedMessageSet = MessageSet.builder().add(12).add(13).add(15).build();
		expect(storeClient.uidCopy(expectedMessageSet, "user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org"))
			.andThrow(new MailboxNotFoundException("Cannot find IMAP folder for collection [ user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org ]"));
		
		try {
			control.replay();
			MailboxImpl mailbox = MailboxImpl.from("user/usera@mydomain.org", logger, storeClient);
			mailbox.uidCopy(expectedMessageSet, TemporaryMailbox.builder()
					.from(mailbox)
					.domainName(new DomainName("mydomain.org"))
					.cyrusPartitionSuffix("archive")
					.build());
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=ImapMessageNotFoundException.class)
	public void fetchInternalDateShouldThrowWhenNotFound() throws Exception {
		MessageSet messageSet = MessageSet.singleton(1);
		expect(storeClient.uidFetchInternalDate(messageSet))
			.andReturn(ImmutableList.<InternalDate> of());
		
		try {
			control.replay();
			MailboxImpl mailbox = MailboxImpl.from("user/usera@mydomain.org", logger, storeClient);
			mailbox.fetchInternalDate(messageSet);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void fetchInternalDateShouldReturnInternalDate() throws Exception {
		int uid = 1;
		MessageSet messageSet = MessageSet.singleton(uid);
		InternalDate expectedInternalDate = new InternalDate(uid, "12-Feb-2014 14:26:00 +0000");
		expect(storeClient.uidFetchInternalDate(messageSet))
			.andReturn(ImmutableList.<InternalDate> of(expectedInternalDate));
		
		control.replay();
		MailboxImpl mailbox = MailboxImpl.from("user/usera@mydomain.org", logger, storeClient);
		List<InternalDate> internalDates = mailbox.fetchInternalDate(messageSet);
		control.verify();
		
		assertThat(internalDates).containsOnly(expectedInternalDate);
	}
	
	@Test
	public void fetchInternalDateShouldReturnEmptyWhenEmptyMessageSet() throws Exception {
		MessageSet messageSet = MessageSet.empty();
		
		control.replay();
		MailboxImpl mailbox = MailboxImpl.from("user/usera@mydomain.org", logger, storeClient);
		List<InternalDate> internalDates = mailbox.fetchInternalDate(messageSet);
		control.verify();
		
		assertThat(internalDates).isEmpty();
	}
}
