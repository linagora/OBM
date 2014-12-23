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

package org.obm.imap.archive.services;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapDeleteException;
import org.obm.imap.archive.mailbox.MailboxImpl;
import org.obm.imap.archive.mailbox.TemporaryMailbox;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;

import fr.aliacom.obm.common.system.ObmSystemUser;


public class CyrusServiceTest {

	private IMocksControl control;
	
	private ImapArchiveConfigurationService imapArchiveConfigurationService;
	private StoreClientFactory storeClientFactory;
	
	private CyrusService testee;
	
	@Before
	public void setup() {
		control = createControl();
		
		imapArchiveConfigurationService = control.createMock(ImapArchiveConfigurationService.class);
		storeClientFactory = control.createMock(StoreClientFactory.class);
		
		testee = new CyrusService(imapArchiveConfigurationService, storeClientFactory);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkLoginShouldThrowWhenNullDomainName() throws Exception {
		try {
			control.replay();
			testee.checkLogin(null);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void checkLogin() throws Exception {
		DomainName domainName = new DomainName("mydomain.org");
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(domainName.get()))
			.andReturn(storeClient);
		storeClient.login(false);
		expectLastCall();
		storeClient.close();
		expectLastCall();
		
		control.replay();
		testee.checkLogin(domainName);
		control.verify();
	}
	
	@Test(expected=IMAPException.class)
	public void checkLoginShouldThrowWhenLoginFails() throws Exception {
		DomainName domainName = new DomainName("mydomain.org");
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(domainName.get()))
			.andReturn(storeClient);
		storeClient.login(false);
		expectLastCall().andThrow(new IMAPException("Could not login"));
		storeClient.close();
		expectLastCall();
		
		control.replay();
		testee.checkLogin(domainName);
		control.verify();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkCyrusPartitionFordomainShouldThrowWhenNullDomainName() throws Exception {
		String testUser = "usera";
		
		try {
			control.replay();
			testee.checkCyrusPartitionFordomain(null, testUser);
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkCyrusPartitionFordomainShouldThrowWhenNullTestUser() throws Exception {
		DomainName domainName = new DomainName("mydomain.org");
		
		try {
			control.replay();
			testee.checkCyrusPartitionFordomain(domainName, null);
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkCyrusPartitionFordomainShouldThrowWhenEmptyTestUser() throws Exception {
		DomainName domainName = new DomainName("mydomain.org");
		
		try {
			control.replay();
			testee.checkCyrusPartitionFordomain(domainName, "");
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void checkCyrusPartitionFordomain() throws Exception {
		DomainName domainName = new DomainName("mydomain.org");
		String testUser = "usera";
		
		expect(imapArchiveConfigurationService.getCyrusPartitionSuffix())
			.andReturn("suffix");
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(domainName.get()))
			.andReturn(storeClient);
		storeClient.login(false);
		expectLastCall();
		storeClient.close();
		expectLastCall();
		
		String temporaryMailbox = "user/usera/" + TemporaryMailbox.TEMPORARY_FOLDER + "/INBOX@mydomain.org";
		expect(storeClient.create(temporaryMailbox, "mydomain_org_suffix"))
			.andReturn(true);
		expect(storeClient.setAcl(temporaryMailbox, ObmSystemUser.CYRUS, MailboxImpl.ALL_IMAP_RIGHTS))
			.andReturn(true);
		expect(storeClient.delete(temporaryMailbox))
			.andReturn(true);
		
		control.replay();
		testee.checkCyrusPartitionFordomain(domainName, testUser);
		control.verify();
	}
	
	@Test(expected=ImapDeleteException.class)
	public void checkCyrusPartitionFordomainShouldThrowWhenExceptionOccuredOnDelete() throws Exception {
		DomainName domainName = new DomainName("mydomain.org");
		String testUser = "usera";
		
		expect(imapArchiveConfigurationService.getCyrusPartitionSuffix())
			.andReturn("suffix");
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(domainName.get()))
			.andReturn(storeClient);
		storeClient.login(false);
		expectLastCall();
		storeClient.close();
		expectLastCall();
		
		String temporaryMailbox = "user/usera/" + TemporaryMailbox.TEMPORARY_FOLDER + "/INBOX@mydomain.org";
		expect(storeClient.create(temporaryMailbox, "mydomain_org_suffix"))
			.andReturn(true);
		expect(storeClient.setAcl(temporaryMailbox, ObmSystemUser.CYRUS, MailboxImpl.ALL_IMAP_RIGHTS))
			.andReturn(true);
		expect(storeClient.delete(temporaryMailbox))
			.andReturn(false);
		
		control.replay();
		testee.checkCyrusPartitionFordomain(domainName, testUser);
		control.verify();
	}
	
	@Test(expected=ImapCreateException.class)
	public void checkCyrusPartitionFordomainShouldThrowWhenExceptionOccuredOnCreate() throws Exception {
		DomainName domainName = new DomainName("mydomain.org");
		String testUser = "usera";
		
		expect(imapArchiveConfigurationService.getCyrusPartitionSuffix())
			.andReturn("suffix");
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(domainName.get()))
			.andReturn(storeClient);
		storeClient.login(false);
		expectLastCall();
		storeClient.close();
		expectLastCall();
		
		String temporaryMailbox = "user/usera/" + TemporaryMailbox.TEMPORARY_FOLDER + "/INBOX@mydomain.org";
		expect(storeClient.create(temporaryMailbox, "mydomain_org_suffix"))
			.andReturn(false);
		
		control.replay();
		testee.checkCyrusPartitionFordomain(domainName, testUser);
		control.verify();
	}
}
