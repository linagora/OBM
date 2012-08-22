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

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;
import static org.obm.push.mail.MailTestsUtils.loadEmail;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.Address;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.Mime4jUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.ICalendar;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

@RunWith(SlowFilterRunner.class)
public class MailBackendTest {

	private User user;
	private Device device;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		udr = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);
	}
	
	@Test
	public void testSendEmailWithBigMail()
			throws ProcessingEmailException, ServerFault, StoreEmailException, SendEmailException, SmtpInvalidRcptException, IOException, AuthFault {
		final String password = "pass";
		final AccessToken at = new AccessToken(1, "o-push");
		
		MailboxService emailManager = createMock(MailboxService.class);
		ICalendar calendarClient = createMock(ICalendar.class);
		UserDataRequest userDataRequest = createMock(UserDataRequest.class);
		LoginService login = createMock(LoginService.class);
		
		expect(userDataRequest.getUser()).andReturn(user).once();
		expect(userDataRequest.getPassword()).andReturn(password).once();

		expect(login.login(user.getLoginAtDomain(), password)).andReturn(at).once();
		expect(calendarClient.getUserEmail(at)).andReturn(user.getLoginAtDomain()).once();
		login.logout(at);
		expectLastCall().once();
		Set<Address> addrs = Sets.newHashSet();
		emailManager.sendEmail(anyObject(UserDataRequest.class), anyObject(Address.class), 
				anyObject(addrs.getClass()), anyObject(addrs.getClass()), anyObject(addrs.getClass()), 
				anyObject(InputStream.class), anyBoolean());
		
		expectLastCall().once();
				
		MailBackend mailBackend = new MailBackendImpl(
				emailManager, calendarClient, null, null, 
				login, new Mime4jUtils(), mockOpushConfigurationService(), null, null);

		replay(emailManager, calendarClient, userDataRequest, login);

		InputStream emailStream = loadEmail("bigEml.eml");
		mailBackend.sendEmail(userDataRequest, ByteStreams.toByteArray(emailStream), true);
		
		verify(emailManager, calendarClient, userDataRequest, login);
	}
	
	@Test
	public void initialHierarchyContainsBaseFolders() throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException {

		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "INBOX")).andReturn("pathForInbox");
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "Drafts")).andReturn("pathForDraft");
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "Sent")).andReturn("pathForSent");
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "Trash")).andReturn("pathForTrash");

		MappingService mappingService = createMock(MappingService.class);

		expect(mappingService.getCollectionIdFor(device, "pathForInbox")).andReturn(1);
		expect(mappingService.getCollectionIdFor(device, "pathForDraft")).andReturn(2);
		expect(mappingService.getCollectionIdFor(device, "pathForSent")).andReturn(3);
		expect(mappingService.getCollectionIdFor(device, "pathForTrash")).andReturn(4);
		
		expect(mappingService.collectionIdToString(1)).andReturn("collection1");
		expect(mappingService.collectionIdToString(2)).andReturn("collection2");
		expect(mappingService.collectionIdToString(3)).andReturn("collection3");
		expect(mappingService.collectionIdToString(4)).andReturn("collection4");

		MailboxService mailboxService = createMock(MailboxService.class);
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());

		
		expect(mappingService.listCollections(device)).andReturn(ImmutableList.<String>of());
		
		replay(collectionPathHelper, mappingService, mailboxService);
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathHelper);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, DateUtils.getEpochCalendar().getTime());
		
		verify(collectionPathHelper, mappingService, mailboxService);
		
		ItemChange inboxItemChange = new ItemChangeBuilder().serverId("collection1")
			.parentId("0").itemType(FolderType.DEFAULT_INBOX_FOLDER)
			.displayName("INBOX").build();
		
		ItemChange draftsItemChange = new ItemChangeBuilder().serverId("collection2")
			.parentId("0").itemType(FolderType.DEFAULT_DRAFTS_FOLDER)
			.displayName("Drafts").build();
		
		ItemChange sentItemChange = new ItemChangeBuilder().serverId("collection3")
			.parentId("0").itemType(FolderType.DEFAULT_SENT_EMAIL_FOLDER)
			.displayName("Sent").build();
		
		ItemChange trashItemChange = new ItemChangeBuilder().serverId("collection4")
			.parentId("0").itemType(FolderType.DEFAULT_DELETED_ITEMS_FOLDER)
			.displayName("Trash").build();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).contains(
				inboxItemChange, draftsItemChange, sentItemChange, trashItemChange);

		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(DateUtils.getEpochCalendar().getTime());
	}
	
	@Test
	public void emptyHierarchyChanges() throws DaoException, UnexpectedObmSyncServerException {

		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(anyObject(String.class))).andReturn(PIMDataType.EMAIL).anyTimes();
		
		MappingService mappingService = createMock(MappingService.class);
		expect(mappingService.listCollections(device)).andReturn(ImmutableList.of("INBOX", "Drafts", "Sent", "Trash"));

		MailboxService mailboxService = createMock(MailboxService.class);
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		
		replay(collectionPathHelper, mappingService, mailboxService);
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathHelper);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, date("20120101"));
		verify(collectionPathHelper, mappingService, mailboxService);
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(DateUtils.getEpochCalendar().getTime());
	}

	@Test
	public void filterContactsHierarchyChanges() throws DaoException, UnexpectedObmSyncServerException {

		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType("INBOX")).andReturn(PIMDataType.EMAIL);
		expect(collectionPathHelper.recognizePIMDataType("Drafts")).andReturn(PIMDataType.EMAIL);
		expect(collectionPathHelper.recognizePIMDataType("Sent")).andReturn(PIMDataType.EMAIL);
		expect(collectionPathHelper.recognizePIMDataType("Trash")).andReturn(PIMDataType.EMAIL);
		expect(collectionPathHelper.recognizePIMDataType("contact")).andReturn(PIMDataType.CONTACTS);
		
		MappingService mappingService = createMock(MappingService.class);
		expect(mappingService.listCollections(device)).andReturn(ImmutableList.of("INBOX", "Drafts", "Sent", "Trash", "contact"));

		MailboxService mailboxService = createMock(MailboxService.class);
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("INBOX", "Drafts", "Sent", "Trash"));
		
		replay(collectionPathHelper, mappingService, mailboxService);
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathHelper);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, date("20120101"));
		verify(collectionPathHelper, mappingService, mailboxService);
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(DateUtils.getEpochCalendar().getTime());
	}
	
	@Test
	public void newImapFolder() throws DaoException, UnexpectedObmSyncServerException, CollectionNotFoundException {

		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(anyObject(String.class))).andReturn(PIMDataType.EMAIL).anyTimes();
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "NewFolder")).andReturn("pathForNewFolder");
		
		MappingService mappingService = createMock(MappingService.class);
		expect(mappingService.listCollections(device)).andReturn(ImmutableList.of("INBOX", "Drafts", "Sent", "Trash"));
		expect(mappingService.getCollectionIdFor(device, "pathForNewFolder")).andReturn(5);
		expect(mappingService.collectionIdToString(5)).andReturn("newFolderCollection");
		
		MailboxService mailboxService = createMock(MailboxService.class);
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("NewFolder"));
		
		replay(collectionPathHelper, mappingService, mailboxService);
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathHelper);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, date("2012-01-01"));
		verify(collectionPathHelper, mappingService, mailboxService);
		
		ItemChange newFolderItemChange = new ItemChangeBuilder().serverId("newFolderCollection")
				.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
				.displayName("NewFolder").build();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(newFolderItemChange);
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(date("2012-01-01"));
	}

	
	@Test
	public void deletedImapFolder() throws DaoException, UnexpectedObmSyncServerException, CollectionNotFoundException {

		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(anyObject(String.class))).andReturn(PIMDataType.EMAIL).anyTimes();
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "deletedFolder")).andReturn("pathForDeletedFolder");
		
		MappingService mappingService = createMock(MappingService.class);
		expect(mappingService.listCollections(device)).andReturn(ImmutableList.of("INBOX", "Drafts", "Sent", "Trash", "deletedFolder"));
		expect(mappingService.getCollectionIdFor(device, "pathForDeletedFolder")).andReturn(5);
		expect(mappingService.collectionIdToString(5)).andReturn("deletedFolderCollection");
		
		MailboxService mailboxService = createMock(MailboxService.class);
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		
		replay(collectionPathHelper, mappingService, mailboxService);
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathHelper);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, date("2012-01-01"));
		verify(collectionPathHelper, mappingService, mailboxService);
		
		ItemChange deletedFolderItemChange = new ItemChangeBuilder().serverId("deletedFolderCollection")
			.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
			.displayName("deletedFolder").build();
		
		assertThat(hierarchyItemsChanges.getDeletedItems()).containsOnly(deletedFolderItemChange);
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(date("2012-01-01"));
	}
	
	@Test
	public void deletedAndAddedImapFolders() throws DaoException, UnexpectedObmSyncServerException, CollectionNotFoundException {

		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		MappingService mappingService = createMock(MappingService.class);

		expect(collectionPathHelper.recognizePIMDataType(anyObject(String.class))).andReturn(PIMDataType.EMAIL).anyTimes();
		
		expect(mappingService.listCollections(device)).andReturn(ImmutableList.of("INBOX", "Drafts", "Sent", "Trash", "OldFolder"));
		
		expect(mappingService.getCollectionIdFor(device, "pathForOldFolder")).andReturn(5);
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "OldFolder")).andReturn("pathForOldFolder");
		expect(mappingService.collectionIdToString(5)).andReturn("oldFolderCollection");
		
		expect(mappingService.getCollectionIdFor(device, "pathForNewFolder")).andReturn(6);
		expect(collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, "NewFolder")).andReturn("pathForNewFolder");
		expect(mappingService.collectionIdToString(6)).andReturn("newFolderCollection");
		
		MailboxService mailboxService = createMock(MailboxService.class);
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("NewFolder"));
		
		replay(collectionPathHelper, mappingService, mailboxService);
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathHelper);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, date("2012-01-01"));
		verify(collectionPathHelper, mappingService, mailboxService);
		
		ItemChange newFolderItemChange = new ItemChangeBuilder().serverId("newFolderCollection")
			.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
			.displayName("NewFolder").build();
		
		ItemChange oldFolderItemChange = new ItemChangeBuilder().serverId("oldFolderCollection")
			.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
			.displayName("OldFolder").build();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(newFolderItemChange);
		assertThat(hierarchyItemsChanges.getDeletedItems()).containsOnly(oldFolderItemChange);
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(date("2012-01-01"));
	}
	
	private MailboxFolders mailboxFolders(String... folders) {
		return new MailboxFolders(
				FluentIterable.from(ImmutableList.copyOf(folders))
					.transform(new Function<String, MailboxFolder>() {
							@Override
							public MailboxFolder apply(String input) {
								return new MailboxFolder(input);
							}
						})
					.toImmutableList());
	}
}
