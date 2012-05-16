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
package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadEmail;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.Mime4jUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.ICalendar;

import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

@RunWith(SlowFilterRunner.class)
public class MailBackendTest {

	private User user;
	private Device device;
	private BackendSession bs;

	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		bs = new BackendSession(new Credentials(user, "password"), "noCommand", device, null);
	}
	
	@Test
	public void testSendEmailWithBigMail()
			throws ProcessingEmailException, ServerFault, StoreEmailException, SendEmailException, SmtpInvalidRcptException, IOException, AuthFault {
		final String password = "pass";
		final AccessToken at = new AccessToken(1, "o-push");
		
		MailboxService emailManager = createMock(MailboxService.class);
		ICalendar calendarClient = createMock(ICalendar.class);
		BackendSession backendSession = createMock(BackendSession.class);
		LoginService login = createMock(LoginService.class);
		
		expect(backendSession.getUser()).andReturn(user).once();
		expect(backendSession.getPassword()).andReturn(password).once();

		expect(login.login(user.getLoginAtDomain(), password)).andReturn(at).once();
		expect(calendarClient.getUserEmail(at)).andReturn(user.getLoginAtDomain()).once();
		login.logout(at);
		expectLastCall().once();
		Set<Address> addrs = Sets.newHashSet();
		emailManager.sendEmail(anyObject(BackendSession.class), anyObject(Address.class), 
				anyObject(addrs.getClass()), anyObject(addrs.getClass()), anyObject(addrs.getClass()), 
				anyObject(InputStream.class), anyBoolean());
		
		expectLastCall().once();
				
		MailBackend mailBackend = new MailBackendImpl(
				emailManager, calendarClient, null, null, 
				login, new Mime4jUtils(), mockOpushConfigurationService(), null, null);

		replay(emailManager, calendarClient, backendSession, login);

		InputStream emailStream = loadEmail("bigEml.eml");
		mailBackend.sendEmail(backendSession, ByteStreams.toByteArray(emailStream), true);
		
		verify(emailManager, calendarClient, backendSession, login);
	}
	
	@Test
	public void hierarchyAlwaysContainsBaseFolders() throws DaoException, CollectionNotFoundException {

		MappingService mappingService = createStrictMock(MappingService.class);
		expect(mappingService.getCollectionIdFor(device, "pathForInbox")).andReturn(1);
		expect(mappingService.collectionIdToString(1)).andReturn("collection1");
		expect(mappingService.getCollectionIdFor(device, "pathForDraft")).andReturn(2);
		expect(mappingService.collectionIdToString(2)).andReturn("collection2");
		expect(mappingService.getCollectionIdFor(device, "pathForSent")).andReturn(3);
		expect(mappingService.collectionIdToString(3)).andReturn("collection3");
		expect(mappingService.getCollectionIdFor(device, "pathForTrash")).andReturn(4);
		expect(mappingService.collectionIdToString(4)).andReturn("collection4");
		
		CollectionPathHelper collectionPathHelper = createStrictMock(CollectionPathHelper.class);
		expect(collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, "INBOX")).andReturn("pathForInbox");
		expect(collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, "Drafts")).andReturn("pathForDraft");
		expect(collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, "Sent")).andReturn("pathForSent");
		expect(collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, "Trash")).andReturn("pathForTrash");
		
		replay(collectionPathHelper, mappingService);
		
		MailBackend mailBackend = new MailBackendImpl(null, null, null, null, null, null, null, mappingService, collectionPathHelper);
		List<ItemChange> hierarchyChanges = mailBackend.getHierarchyChanges(bs);
		
		verify(collectionPathHelper, mappingService);
		
		Assertions.assertThat(hierarchyChanges).contains(
				new ItemChangeBuilder().serverId("collection1")
					.parentId("0").itemType(FolderType.DEFAULT_INBOX_FOLDER)
					.displayName("INBOX").build(),
					
				new ItemChangeBuilder().serverId("collection2")
					.parentId("0").itemType(FolderType.DEFAULT_DRAFTS_FOLDERS)
					.displayName("Drafts").build(),
					
				new ItemChangeBuilder().serverId("collection3")
					.parentId("0").itemType(FolderType.DEFAULT_SENT_EMAIL_FOLDER)
					.displayName("Sent").build(),
					
				new ItemChangeBuilder().serverId("collection4")
					.parentId("0").itemType(FolderType.DEFAULT_DELETED_ITEMS_FOLDERS)
					.displayName("Trash").build()
				);
	}
}
