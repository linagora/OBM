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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.domain.dao.SharedMailboxDao;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserSystemDao;
import org.obm.imap.archive.exception.NoBackendDefineForSharedMailboxException;
import org.obm.imap.archive.exception.SharedMailboxNotFoundException;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.provisioning.dao.exceptions.SystemUserNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.host.ObmHost;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.mailshare.SharedMailbox;
import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserPassword;

public class StoreClientFactoryTest {

	private IMocksControl control;
	private LocatorService locatorService;
	private UserSystemDao userSystemDao;
	private UserDao userDao;
	private SharedMailboxDao sharedMailboxDao;
	private StoreClient.Factory storeClientFactory;
	private StoreClientFactory testee;
	
	@Before
	public void setup() {
		control = createControl();
		
		locatorService = control.createMock(LocatorService.class);
		userSystemDao = control.createMock(UserSystemDao.class);
		userDao = control.createMock(UserDao.class);
		sharedMailboxDao = control.createMock(SharedMailboxDao.class);
		storeClientFactory = control.createMock(StoreClient.Factory.class);
		
		testee = new StoreClientFactory(locatorService, userSystemDao, userDao, sharedMailboxDao, storeClientFactory);
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createShouldThrowWhenDomainNameIsNull() throws Exception {
		testee.create(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createShouldThrowWhenDomainNameIsEmpty() throws Exception {
		testee.create("");
	}
	
	@Test(expected=SystemUserNotFoundException.class)
	public void createShouldThrowWhenCyrusUserNotFound() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andThrow(new SystemUserNotFoundException());
		
		control.replay();
		try {
			testee.create("mydomain.org");
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=LocatorClientException.class)
	public void createShouldThrowWhenServiceNotFound() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andReturn(ObmSystemUser.builder()
					.id(1)
					.login("cyrus")
					.password(UserPassword.valueOf("cyrus"))
					.build());
		
		String domainName = "mydomain.org";
		expect(locatorService.getServiceLocation("mail/imap_frontend", domainName))
			.andThrow(new LocatorClientException("not found"));
		
		control.replay();
		try {
			testee.create(domainName);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void createShouldWork() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andReturn(ObmSystemUser.builder()
					.id(1)
					.login("cyrus")
					.password(UserPassword.valueOf("cyrus"))
					.build());
		
		String domainName = "mydomain.org";
		String hostname = "myhost.lyon.lan";
		expect(locatorService.getServiceLocation("mail/imap_frontend", domainName))
			.andReturn(hostname);
		
		StoreClient expectedStoreClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(eq(hostname), eq("cyrus"), aryEq("cyrus".toCharArray())))
			.andReturn(expectedStoreClient);
		
		control.replay();
		StoreClient storeClient = testee.create(domainName);
		control.verify();
		
		assertThat(storeClient).isEqualTo(expectedStoreClient);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createOnUserBackendShouldThrowWhenUserIsNull() throws Exception {
		testee.createOnUserBackend(null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createOnUserBackendShouldThrowWhenUserIsEmpty() throws Exception {
		testee.createOnUserBackend("", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createOnUserBackendShouldThrowWhenDomainIsNull() throws Exception {
		testee.createOnUserBackend("user", null);
	}
	
	@Test(expected=SystemUserNotFoundException.class)
	public void createOnUserBackendShouldThrowWhenCyrusUserNotFound() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andThrow(new SystemUserNotFoundException());
		
		control.replay();
		try {
			testee.createOnUserBackend("user", ObmDomain.builder()
					.name("mydomain.org")
					.build());
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=UserNotFoundException.class)
	public void createOnUserBackendShouldThrowWhenUserNotFound() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andReturn(ObmSystemUser.builder()
					.id(1)
					.login("cyrus")
					.password(UserPassword.valueOf("cyrus"))
					.build());
		
		String domainName = "mydomain.org";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.build();
		
		String user = "user";
		expect(userDao.findUserByLogin(user, obmDomain))
			.andReturn(null);
		
		control.replay();
		try {
			testee.createOnUserBackend(user, obmDomain);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void createOnUserBackendShouldWork() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andReturn(ObmSystemUser.builder()
					.id(1)
					.login("cyrus")
					.password(UserPassword.valueOf("cyrus"))
					.build());
		
		String domainName = "mydomain.org";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.build();
		
		String user = "user";
		String hostIp = "10.69.43.33";
		ObmUser userObm = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf(user))
				.domain(obmDomain)
				.emails(UserEmails.builder()
						.domain(obmDomain)
						.server(ObmHost.builder()
								.ip(hostIp)
								.build())
						.build())
				.build();
		expect(userDao.findUserByLogin(user, obmDomain))
			.andReturn(userObm);
		
		StoreClient expectedStoreClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(eq(hostIp), eq("cyrus"), aryEq("cyrus".toCharArray())))
			.andReturn(expectedStoreClient);
		
		control.replay();
		StoreClient storeClient = testee.createOnUserBackend(user, obmDomain);
		control.verify();
		
		assertThat(storeClient).isEqualTo(expectedStoreClient);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createOnSharedMailboxBackendShouldThrowWhenUserIsNull() throws Exception {
		testee.createOnSharedMailboxBackend(null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createOnSharedMailboxBackendShouldThrowWhenUserIsEmpty() throws Exception {
		testee.createOnSharedMailboxBackend("", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createOnSharedMailboxBackendShouldThrowWhenDomainIsNull() throws Exception {
		testee.createOnSharedMailboxBackend("user", null);
	}
	
	@Test(expected=SystemUserNotFoundException.class)
	public void createOnSharedMailboxBackendShouldThrowWhenCyrusUserNotFound() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andThrow(new SystemUserNotFoundException());
		
		control.replay();
		try {
			testee.createOnSharedMailboxBackend("user", ObmDomain.builder()
					.name("mydomain.org")
					.build());
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=SharedMailboxNotFoundException.class)
	public void createOnSharedMailboxBackendShouldThrowWhenSharedMailboxNotFound() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andReturn(ObmSystemUser.builder()
					.id(1)
					.login("cyrus")
					.password(UserPassword.valueOf("cyrus"))
					.build());
		
		String domainName = "mydomain.org";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.build();
		
		String name = "name";
		expect(sharedMailboxDao.findSharedMailboxByName(name, obmDomain))
			.andReturn(null);
		
		control.replay();
		try {
			testee.createOnSharedMailboxBackend(name, obmDomain);
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=NoBackendDefineForSharedMailboxException.class)
	public void createOnSharedMailboxBackendShouldThrowWhenSharedMailboxHasNoServer() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andReturn(ObmSystemUser.builder()
					.id(1)
					.login("cyrus")
					.password(UserPassword.valueOf("cyrus"))
					.build());
		
		String domainName = "mydomain.org";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.build();
		
		String name = "name";
		expect(sharedMailboxDao.findSharedMailboxByName(name, obmDomain))
			.andReturn(SharedMailbox.builder()
					.id(1)
					.domain(obmDomain)
					.build());
		
		control.replay();
		try {
			testee.createOnSharedMailboxBackend(name, obmDomain);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void createOnSharedMailboxBackendShouldWork() throws Exception {
		expect(userSystemDao.getByLogin(ObmSystemUser.CYRUS))
			.andReturn(ObmSystemUser.builder()
					.id(1)
					.login("cyrus")
					.password(UserPassword.valueOf("cyrus"))
					.build());
		
		String domainName = "mydomain.org";
		ObmDomain obmDomain = ObmDomain.builder()
				.name(domainName)
				.build();
		
		String hostIp = "10.69.43.33";
		String name = "name";
		expect(sharedMailboxDao.findSharedMailboxByName(name, obmDomain))
			.andReturn(SharedMailbox.builder()
					.id(1)
					.domain(obmDomain)
					.server(ObmHost.builder()
							.ip(hostIp)
							.build())
					.build());
		
		StoreClient expectedStoreClient = control.createMock(StoreClient.class);
		expect(storeClientFactory.create(eq(hostIp), eq("cyrus"), aryEq("cyrus".toCharArray())))
			.andReturn(expectedStoreClient);
		
		control.replay();
		StoreClient storeClient = testee.createOnSharedMailboxBackend(name, obmDomain);
		control.verify();
		
		assertThat(storeClient).isEqualTo(expectedStoreClient);
	}
}
