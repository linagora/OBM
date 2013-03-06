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
package org.obm.push.java.mail;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.OpushImapFolder;

@RunWith(SlowFilterRunner.class)
public class ImapClientProviderImplTest {
	
	private static final String IMAP_STORE_RESOURCE = "imap-store";
	
	private User user;
	private Device device;
	private UserDataRequest udr;
	private IMocksControl mocksControl;

	@Before
	public void setup() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		udr = new UserDataRequest(new Credentials(user, "password"), "noCommand", device);
		mocksControl = EasyMock.createControl();
	}
	
	@Test
	public void testCloseFolderOnlyRetrieveWorkingImapStore() throws Exception {
		EmailConfiguration emailConfiguration = mockEmailConfiguration();
		
		OpushImapFolder opushImapFolder = mocksControl.createMock(OpushImapFolder.class);
		opushImapFolder.close();
		expectLastCall();
		
		mocksControl.replay();
		
		ImapClientProviderImpl imapClientProviderImpl = new ImapClientProviderImpl(null, emailConfiguration, null, null);
		ImapStore imapStore = imapClientProviderImpl.retrieveWorkingImapStore(udr, opushImapFolder);
		
		mocksControl.verify();
		assertThat(imapStore).isNull();
	}
	
	@Test
	public void testImapStoreOnlyRetrieveWorkingImapStore() throws Exception {
		EmailConfiguration emailConfiguration = mockEmailConfiguration();

		ImapStore imapStore = mocksControl.createMock(ImapStore.class);
		imapStore.close();
		expectLastCall();
		
		mocksControl.replay();

		udr.putResource(IMAP_STORE_RESOURCE, imapStore);
		
		ImapClientProviderImpl imapClientProviderImpl = new ImapClientProviderImpl(null, emailConfiguration, null, null);
		ImapStore nullImapStore = imapClientProviderImpl.retrieveWorkingImapStore(udr, null);
		
		mocksControl.verify();
		assertThat(nullImapStore).isNull();
	}
	
	@Test
	public void testRetrieveWorkingImapStore() throws Exception {
		EmailConfiguration emailConfiguration = mockEmailConfiguration();

		OpushImapFolder opushImapFolder = mocksControl.createMock(OpushImapFolder.class);
		
		ImapStore expectedImapStore = mocksControl.createMock(ImapStore.class);
		expect(expectedImapStore.isConnected(opushImapFolder))
			.andReturn(true).once();
		
		mocksControl.replay();

		udr.putResource(IMAP_STORE_RESOURCE, expectedImapStore);
		
		ImapClientProviderImpl imapClientProviderImpl = new ImapClientProviderImpl(null, emailConfiguration, null, null);
		ImapStore imapStore = imapClientProviderImpl.retrieveWorkingImapStore(udr, opushImapFolder);
		
		mocksControl.verify();
		assertThat(imapStore).isEqualTo(expectedImapStore);
	}
	
	@Test
	public void testCloseBothRetrieveWorkingImapStore() throws Exception {
		EmailConfiguration emailConfiguration = mockEmailConfiguration();

		OpushImapFolder opushImapFolder = mocksControl.createMock(OpushImapFolder.class);
		opushImapFolder.close();
		expectLastCall();
		
		ImapStore expectedImapStore = mocksControl.createMock(ImapStore.class);
		expect(expectedImapStore.isConnected(opushImapFolder))
			.andReturn(false).once();
		expectedImapStore.close();
		expectLastCall();
		
		mocksControl.replay();

		udr.putResource(IMAP_STORE_RESOURCE, expectedImapStore);
		
		ImapClientProviderImpl imapClientProviderImpl = new ImapClientProviderImpl(null, emailConfiguration, null, null);
		ImapStore imapStore = imapClientProviderImpl.retrieveWorkingImapStore(udr, opushImapFolder);
		
		mocksControl.verify();
		assertThat(imapStore).isNull();
	}

	private EmailConfiguration mockEmailConfiguration() {
		EmailConfiguration emailConfiguration = mocksControl.createMock(EmailConfiguration.class);
		expect(emailConfiguration.loginWithDomain())
			.andReturn(true).once();
		expect(emailConfiguration.activateTls())
			.andReturn(false).once();
		expect(emailConfiguration.imapTimeoutInMilliseconds())
			.andReturn(5000).once();
		expect(emailConfiguration.getImapFetchBlockSize())
			.andReturn(10240).once();
		return emailConfiguration;
	}
}
