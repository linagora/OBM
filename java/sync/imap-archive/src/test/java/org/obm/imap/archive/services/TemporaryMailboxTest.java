/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.imap.archive.beans.Year;
import org.obm.imap.archive.exception.ImapDeleteException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import pl.wkr.fluentrule.api.FluentExpectedException;


public class TemporaryMailboxTest {

	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	
	private IMocksControl control;
	
	@Before
	public void setup() {
		control = createControl();
	}
	
	@Test
	public void shouldBuildWhenEveryThingProvided() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		TemporaryMailbox.from(Mailbox.from("user/usera@mydomain.org", logger, storeClient), Year.from(2015), new DomainName("mydomain.org"));
		control.verify();
	}
	
	@Test
	public void temporaryMailboxShouldWorkWhenMailboxIsINBOX() throws Exception {
		String mailbox = "user/usera@mydomain.org";
		
		MailboxPaths temporaryMailbox = TemporaryMailbox.temporaryMailbox(mailbox);
		assertThat(temporaryMailbox.getName()).isEqualTo("user/usera/TEMP/INBOX@mydomain.org");
	}
	
	@Test
	public void temporaryMailboxShouldWorkWhenMailboxIsAFolder() throws Exception {
		String mailbox = "user/usera/Test@mydomain.org";
		
		MailboxPaths temporaryMailbox = TemporaryMailbox.temporaryMailbox(mailbox);
		assertThat(temporaryMailbox.getName()).isEqualTo("user/usera/TEMP/Test@mydomain.org");
	}
	
	@Test
	public void temporaryMailboxShouldWorkWhenMailboxIsASubFolder() throws Exception {
		String mailbox = "user/usera/Test/subfolder@mydomain.org";
		
		MailboxPaths temporaryMailbox = TemporaryMailbox.temporaryMailbox(mailbox);
		assertThat(temporaryMailbox.getName()).isEqualTo("user/usera/TEMP/Test/subfolder@mydomain.org");
	}
	
	@Test
	public void temporaryMailboxShouldThrowWhenBadMailbox() throws Exception {
		String mailbox = "user";
		
		expectedException.expect(MailboxFormatException.class);
		
		TemporaryMailbox.temporaryMailbox(mailbox);
	}
	
	@Test
	public void deleteShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.delete("user/usera/TEMP/INBOX@mydomain.org"))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject(String.class));
		expectLastCall().anyTimes();
		
		control.replay();
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.from(Mailbox.from("user/usera@mydomain.org", logger, storeClient), new DomainName("mydomain.org"));
		temporaryMailbox.delete();
		control.verify();
	}
	
	@Test
	public void deleteShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		expect(storeClient.delete("user/usera/TEMP/INBOX@mydomain.org"))
			.andReturn(false);
		logger.error(anyObject(String.class));
		expectLastCall();
		
		expectedException.expect(ImapDeleteException.class);
		
		control.replay();
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.from(Mailbox.from("user/usera@mydomain.org", logger, storeClient), new DomainName("mydomain.org"));
		temporaryMailbox.delete();
		control.verify();
	}
}
