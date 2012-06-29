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
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.CollectionPath.Builder;
import org.obm.push.backend.CollectionPathUtils;
import org.obm.push.bean.Address;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
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
import com.google.inject.Provider;

@RunWith(SlowFilterRunner.class)
public class MailBackendTest {

	private User user;
	private Device device;
	private UserDataRequest udr;
	private MailboxService mailboxService;
	private MappingService mappingService;
	private Provider<Builder> collectionPathBuilderProvider;
	private CollectionPath.Builder collectionPathBuilder;

	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		udr = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);
		collectionPathBuilder = createMock(Builder.class);
		expect(collectionPathBuilder.userDataRequest(udr)).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.pimType(PIMDataType.EMAIL)).andReturn(collectionPathBuilder).anyTimes();
		collectionPathBuilderProvider = createMock(Provider.class);
		expect(collectionPathBuilderProvider.get()).andReturn(collectionPathBuilder).anyTimes();
		mailboxService = createMock(MailboxService.class);
		mappingService = createMock(MappingService.class);
	}
	
	private void replayCommonMocks() {
		replay(mailboxService, mappingService, collectionPathBuilderProvider, collectionPathBuilder);
	}
	
	private void verifyCommonMocks() {
		verify(mailboxService, mappingService, collectionPathBuilderProvider, collectionPathBuilder);
	}
	
	@Test
	public void testSendEmailWithBigMail()
			throws ProcessingEmailException, ServerFault, StoreEmailException, SendEmailException, SmtpInvalidRcptException, IOException, AuthFault {
		final String password = "pass";
		final AccessToken at = new AccessToken(1, "o-push");
		
		MailboxService mailboxService = createMock(MailboxService.class);
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
		mailboxService.sendEmail(anyObject(UserDataRequest.class), anyObject(Address.class), 
				anyObject(addrs.getClass()), anyObject(addrs.getClass()), anyObject(addrs.getClass()), 
				anyObject(InputStream.class), anyBoolean());
		
		expectLastCall().once();
				
		MailBackend mailBackend = new MailBackendImpl(
				mailboxService, calendarClient, null, null, 
				login, new Mime4jUtils(), mockOpushConfigurationService(), mappingService, collectionPathBuilderProvider);

		replay(mailboxService, calendarClient, userDataRequest, login);
		replayCommonMocks();
		
		InputStream emailStream = loadEmail("bigEml.eml");
		mailBackend.sendEmail(userDataRequest, ByteStreams.toByteArray(emailStream), true);
		
		verify(mailboxService, calendarClient, userDataRequest, login);
		verifyCommonMocks();
	}
	
	private void expectTestToBuildSpecialFoldersCollectionPaths() {
		CollectionPath inboxCollectionPath = CollectionPathUtils.emailCollectionPath("INBOX");
		expect(collectionPathBuilder.displayName("INBOX")).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.build()).andReturn(inboxCollectionPath).once();
		
		CollectionPath draftsCollectionPath = CollectionPathUtils.emailCollectionPath("Drafts");
		expect(collectionPathBuilder.displayName("Drafts")).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.build()).andReturn(draftsCollectionPath).once();
		
		CollectionPath sentCollectionPath = CollectionPathUtils.emailCollectionPath("Sent");
		expect(collectionPathBuilder.displayName("Sent")).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.build()).andReturn(sentCollectionPath).once();
		
		CollectionPath trashCollectionPath = CollectionPathUtils.emailCollectionPath("Trash");
		expect(collectionPathBuilder.displayName("Trash")).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.build()).andReturn(trashCollectionPath).once();
	}
	
	@Test
	public void initialHierarchyContainsBaseFolders() throws Exception {
		FolderSyncState incomingSyncState = new FolderSyncState("0");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234");

		expectTestToBuildSpecialFoldersCollectionPaths();
		
		expect(mappingService.getCollectionIdFor(device, "collectionpath/INBOX")).andReturn(1);
		expect(mappingService.getCollectionIdFor(device, "collectionpath/Drafts")).andReturn(2);
		expect(mappingService.getCollectionIdFor(device, "collectionpath/Sent")).andReturn(3);
		expect(mappingService.getCollectionIdFor(device, "collectionpath/Trash")).andReturn(4);
		
		expect(mappingService.collectionIdToString(1)).andReturn("collection1");
		expect(mappingService.collectionIdToString(2)).andReturn("collection2");
		expect(mappingService.collectionIdToString(3)).andReturn("collection3");
		expect(mappingService.collectionIdToString(4)).andReturn("collection4");
		
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		expect(mappingService.listCollections(udr, incomingSyncState)).andReturn(ImmutableList.<CollectionPath>of());
		
		replayCommonMocks();
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathBuilderProvider);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);
		
		verifyCommonMocks();
		
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
	public void emptyHierarchyChanges() throws Exception {
		FolderSyncState incomingSyncState = new FolderSyncState("1234a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234b");
		
		expectTestToBuildSpecialFoldersCollectionPaths();
		
		expect(mappingService.listCollections(udr, incomingSyncState)).andReturn(CollectionPathUtils.emailCollectionPaths("INBOX", "Drafts", "Sent", "Trash"));
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		replayCommonMocks();
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathBuilderProvider);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);
		
		verifyCommonMocks();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(DateUtils.getEpochCalendar().getTime());
	}

	@Test
	public void filterContactsHierarchyChanges() throws Exception {
		FolderSyncState incomingSyncState = new FolderSyncState("1234a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234b");
		
		expectTestToBuildSpecialFoldersCollectionPaths();
		
		expect(mappingService.listCollections(udr, incomingSyncState)).andReturn(
				ImmutableList.<CollectionPath>builder()
					.add(CollectionPathUtils.contactCollectionPath("contact"))
					.addAll(CollectionPathUtils.emailCollectionPaths("INBOX", "Drafts", "Sent", "Trash"))
					.build());
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		replayCommonMocks();
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathBuilderProvider);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);
		
		verifyCommonMocks();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(DateUtils.getEpochCalendar().getTime());
	}
	
	@Test
	public void newImapFolder() throws Exception {
		FolderSyncState incomingSyncState = new FolderSyncState("1234a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234b");
		
		expectTestToBuildSpecialFoldersCollectionPaths();
		
		CollectionPath newFolderCollectionPath = CollectionPathUtils.emailCollectionPath("NewFolder");
		expect(collectionPathBuilder.displayName("NewFolder")).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.build()).andReturn(newFolderCollectionPath).once();
		
		expect(mappingService.listCollections(udr, incomingSyncState)).andReturn(CollectionPathUtils.emailCollectionPaths("INBOX", "Drafts", "Sent", "Trash"));
		expect(mappingService.getCollectionIdFor(device, newFolderCollectionPath.collectionPath())).andReturn(5);
		expect(mappingService.collectionIdToString(5)).andReturn("newFolderCollection");
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("NewFolder"));
		
		replayCommonMocks();
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathBuilderProvider);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);
		
		verifyCommonMocks();

		ItemChange newFolderItemChange = new ItemChangeBuilder().serverId("newFolderCollection")
				.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
				.displayName("NewFolder").build();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(newFolderItemChange);
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(date("2012-01-01"));
	}

	
	@Test
	public void deletedImapFolder() throws Exception {
		FolderSyncState incomingSyncState = new FolderSyncState("1234a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234b");
		
		expectTestToBuildSpecialFoldersCollectionPaths();
		
		CollectionPath deletedFolderCollection = CollectionPathUtils.emailCollectionPath("deletedFolder");
		
		expect(mappingService.listCollections(udr, incomingSyncState)).andReturn(CollectionPathUtils.emailCollectionPaths("INBOX", "Drafts", "Sent", "Trash", "deletedFolder"));
		expect(mappingService.getCollectionIdFor(device, deletedFolderCollection.collectionPath())).andReturn(5);
		expect(mappingService.collectionIdToString(5)).andReturn("deletedFolderCollection");
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		
		replayCommonMocks();
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathBuilderProvider);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);

		verifyCommonMocks();
		
		ItemChange deletedFolderItemChange = new ItemChangeBuilder().serverId("deletedFolderCollection")
			.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
			.displayName("deletedFolder").build();
		
		assertThat(hierarchyItemsChanges.getDeletedItems()).containsOnly(deletedFolderItemChange);
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getLastSync()).isAfter(date("2012-01-01"));
	}
	
	@Test
	public void deletedAndAddedImapFolders() throws Exception {
		FolderSyncState incomingSyncState = new FolderSyncState("1234a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234b");
		
		expectTestToBuildSpecialFoldersCollectionPaths();
		
		CollectionPath newFolderCollectionPath = CollectionPathUtils.emailCollectionPath("NewFolder");
		expect(collectionPathBuilder.displayName("NewFolder")).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.build()).andReturn(newFolderCollectionPath).once();
		
		CollectionPath deletedFolderCollection = CollectionPathUtils.emailCollectionPath("deletedFolder");
		expect(collectionPathBuilder.displayName("deletedFolder")).andReturn(collectionPathBuilder).anyTimes();
		
		expect(mappingService.getCollectionIdFor(device, deletedFolderCollection.collectionPath())).andReturn(5);
		expect(mappingService.collectionIdToString(5)).andReturn("deletedFolderCollection");
		expect(mappingService.getCollectionIdFor(device, newFolderCollectionPath.collectionPath())).andReturn(6);
		expect(mappingService.collectionIdToString(6)).andReturn("newFolderCollection");
		
		expect(mappingService.listCollections(udr, incomingSyncState)).andReturn(CollectionPathUtils.emailCollectionPaths("INBOX", "Drafts", "Sent", "Trash", "deletedFolder"));
		
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("NewFolder"));
		
		replayCommonMocks();
		
		MailBackend mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null, mappingService, collectionPathBuilderProvider);
		HierarchyItemsChanges hierarchyItemsChanges = mailBackend.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);

		verifyCommonMocks();
		
		ItemChange newFolderItemChange = new ItemChangeBuilder().serverId("newFolderCollection")
			.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
			.displayName("NewFolder").build();
		
		ItemChange oldFolderItemChange = new ItemChangeBuilder().serverId("deletedFolderCollection")
			.parentId("0").itemType(FolderType.USER_CREATED_EMAIL_FOLDER)
			.displayName("deletedFolder").build();
		
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
