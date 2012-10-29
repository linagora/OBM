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
package org.obm.push.contacts;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.configuration.ContactConfiguration;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.CollectionPath.Builder;
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
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.service.impl.MappingService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Folder;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.items.FolderChanges;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;

@RunWith(SlowFilterRunner.class)
public class ContactsBackendHierarchyChangesTest {

	private static final String COLLECTION_CONTACT_PREFIX = "obm:\\\\test@test\\contacts\\";
	
	private User user;
	private Device device;
	private UserDataRequest userDataRequest;
	private AccessToken accessToken;
	
	private MappingService mappingService;
	private BookClient bookClient;
	private LoginService loginService;
	private ContactConfiguration contactConfiguration;
	private Provider<CollectionPath.Builder> collectionPathBuilderProvider;
	
	private ContactsBackend contactsBackend;

	private IMocksControl mocks;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);
		accessToken = new AccessToken(0, "OBM");

		mocks = createControl();
		mappingService = mocks.createMock(MappingService.class);
		bookClient = mocks.createMock(BookClient.class);
		loginService = mocks.createMock(LoginService.class);
		contactConfiguration = publicContactConfiguration();
		collectionPathBuilderProvider = mocks.createMock(Provider.class);
		
		contactsBackend = new ContactsBackend(mappingService, 
				bookClient, 
				loginService, 
				contactConfiguration, 
				collectionPathBuilderProvider);
	}

	private ContactConfiguration publicContactConfiguration() {
		return new ContactConfiguration() {
			@Override
			public String getDefaultAddressBookName() {
				return super.getDefaultAddressBookName();
			}
		};
	}

	@Test
	public void testDefaultContactChanges() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a", DateUtils.date("2012-12-15T20:30:45Z"));
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b", DateUtils.date("2012-12-16T20:30:45Z"));
		String contactDisplayName = "contacts";
		String contactCollectionPath = COLLECTION_CONTACT_PREFIX + contactDisplayName;
		
		int collectionMappingId = 1;

		expectLoginBehavior();
		Folder change = newFolderObject(contactDisplayName, collectionMappingId);
		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.of(change), ImmutableSet.<Folder>of());
		
		List<CollectionPath> knownCollections = ImmutableList.of(); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceSearchThenCreateCollection(contactCollectionPath, collectionMappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(collectionMappingId));
		expectMappingServiceLookupCollection(contactCollectionPath, collectionMappingId);
		
		expectBuildCollectionPath(contactDisplayName);

		mocks.replay();
		
		HierarchyItemsChanges hierarchyItemsChanges = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		mocks.verify();
		
		ItemChange expectedItemChange = new ItemChangeBuilder()
				.serverId(String.valueOf(collectionMappingId))
				.parentId("0")
				.displayName(contactDisplayName)
				.itemType(FolderType.DEFAULT_CONTACTS_FOLDER)
				.withNewFlag(true)
				.build();
		assertThat(hierarchyItemsChanges.getChangedItems()).hasSize(1);
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(expectedItemChange);
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testNoContactsChanges() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a", DateUtils.date("2012-12-15T20:30:45Z"));
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b", DateUtils.date("2012-12-16T20:30:45Z"));
		String contactDisplayName = "contacts";
		String contactCollectionPath = COLLECTION_CONTACT_PREFIX + contactDisplayName;
		
		int collectionMappingId = 1;

		expectLoginBehavior();
		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.<Folder>of(), ImmutableSet.<Folder>of());

		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath(contactDisplayName)); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceFindCollection(contactCollectionPath, collectionMappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(collectionMappingId));
		
		mocks.replay();
		
		HierarchyItemsChanges hierarchyItemsChanges = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		mocks.verify();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testOnlyChanges() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a", DateUtils.date("2012-12-15T20:30:45Z"));
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b", DateUtils.date("2012-12-16T20:30:45Z"));
		
		int contactMappingId = 115;
		String contactDisplayName = "contacts";
		String contactCollectionPath = COLLECTION_CONTACT_PREFIX + contactDisplayName;
		int otherCollectionMappingId = 203;
		String otherCollectionDisplayName = "no default address book";
		String otherCollectionCollectionPath = COLLECTION_CONTACT_PREFIX + otherCollectionDisplayName;
		

		expectLoginBehavior();
		Folder change = newFolderObject(otherCollectionDisplayName, Integer.valueOf(contactMappingId));
		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.of(change), ImmutableSet.<Folder>of());

		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath(contactDisplayName)); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceFindCollection(contactCollectionPath, contactMappingId);
		expectMappingServiceSearchThenCreateCollection(otherCollectionCollectionPath, otherCollectionMappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(contactMappingId, otherCollectionMappingId));
		expectMappingServiceLookupCollection(contactCollectionPath, contactMappingId);
		expectMappingServiceLookupCollection(otherCollectionCollectionPath, otherCollectionMappingId);

		expectBuildCollectionPath(otherCollectionDisplayName);
		expectBuildCollectionPath(contactDisplayName);
		
		mocks.replay();
		
		HierarchyItemsChanges hierarchyItemsChanges = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		mocks.verify();

		ItemChange expectedItemChange = new ItemChangeBuilder()
				.serverId(String.valueOf(otherCollectionMappingId))
				.parentId(String.valueOf(contactMappingId))
				.displayName(otherCollectionDisplayName)
				.itemType(FolderType.USER_CREATED_CONTACTS_FOLDER)
				.withNewFlag(true)
				.build();
		assertThat(hierarchyItemsChanges.getChangedItems()).hasSize(1);
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(expectedItemChange);
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testOnlyDeletions() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a", DateUtils.date("2012-12-15T20:30:45Z"));
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b", DateUtils.date("2012-12-16T20:30:45Z"));
		
		int contactMappingId = 115;
		String contactDisplayName = "contacts";
		String contactCollectionPath = COLLECTION_CONTACT_PREFIX + contactDisplayName;
		int otherCollectionMappingId = 203;
		String otherCollectionDisplayName = "no default address book";
		String otherCollectionCollectionPath = COLLECTION_CONTACT_PREFIX + otherCollectionDisplayName;
		
		expectLoginBehavior();
		Folder contactDeletion = newFolderObject(contactDisplayName, contactMappingId);
		Folder otherCollectionDeletion = newFolderObject(otherCollectionDisplayName, otherCollectionMappingId);
		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.<Folder>of(), ImmutableSet.of(contactDeletion, otherCollectionDeletion));

		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath(contactDisplayName),
				new ContactCollectionPath(otherCollectionDisplayName)); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceLookupCollection(contactCollectionPath, contactMappingId);
		expectMappingServiceLookupCollection(otherCollectionCollectionPath, otherCollectionMappingId);
		expectMappingServiceLookupCollection(contactCollectionPath, contactMappingId);

		expectBuildCollectionPath(contactDisplayName);
		expectBuildCollectionPath(otherCollectionDisplayName);
		expectBuildCollectionPath(contactDisplayName);
		
		mocks.replay();
		
		HierarchyItemsChanges hierarchyItemsChanges = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		mocks.verify();

		ItemChange expectedItemDeletion = new ItemChangeBuilder()
				.serverId(String.valueOf(contactMappingId))
				.parentId("0")
				.displayName(contactDisplayName)
				.itemType(FolderType.DEFAULT_CONTACTS_FOLDER)
				.withNewFlag(false)
				.build();
		ItemChange expectedItem2Deletion = new ItemChangeBuilder()
				.serverId(String.valueOf(otherCollectionMappingId))
				.parentId(String.valueOf(contactMappingId))
				.displayName(otherCollectionDisplayName)
				.itemType(FolderType.USER_CREATED_CONTACTS_FOLDER)
				.withNewFlag(false)
				.build();
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).hasSize(2);
		assertThat(hierarchyItemsChanges.getDeletedItems()).containsOnly(expectedItemDeletion, expectedItem2Deletion);
	}

	private Builder expectBuildCollectionPath(String displayName) {
		CollectionPath collectionPath = new ContactCollectionPath(displayName);
		CollectionPath.Builder collectionPathBuilder = expectCollectionPathBuilder(collectionPath, displayName);
		expectCollectionPathBuilderPovider(collectionPathBuilder);
		return collectionPathBuilder;
	}

	private CollectionPath.Builder expectCollectionPathBuilder(CollectionPath collectionPath, String displayName) {
		CollectionPath.Builder collectionPathBuilder = mocks.createMock(CollectionPath.Builder.class);
		expect(collectionPathBuilder.userDataRequest(userDataRequest))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.pimType(PIMDataType.CONTACTS))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.displayName(displayName))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.build())
			.andReturn(collectionPath).once();
		
		return collectionPathBuilder;
	}

	private void expectBookClientListBooksChanged(FolderSyncState lastKnownState,
			Set<Folder> changes, Set<Folder> deletions) throws ServerFault {
		expect(bookClient.listAddressBooksChanged(accessToken, lastKnownState.getLastSync()))
			.andReturn(new FolderChanges(changes, deletions, lastKnownState.getLastSync())).once();
	}

	private void expectCollectionPathBuilderPovider(CollectionPath.Builder collectionPathBuilder) {
			expect(collectionPathBuilderProvider.get())
				.andReturn(collectionPathBuilder).once();
	}

	private void expectMappingServiceSnapshot(FolderSyncState outgoingSyncState, Set<Integer> collectionIds)
			throws DaoException {

		mappingService.snapshotCollections(outgoingSyncState, collectionIds);
		expectLastCall();
	}

	private void expectMappingServiceListLastKnowCollection(FolderSyncState incomingSyncState,
			List<CollectionPath> collectionPaths) throws DaoException {
		
		expect(mappingService.getLastBackendMapping(PIMDataType.CONTACTS, incomingSyncState))
			.andReturn(incomingSyncState.getLastSync()).once();
		
		expect(mappingService.listCollections(userDataRequest, incomingSyncState))
			.andReturn(collectionPaths).once();
	}

	private void expectMappingServiceFindCollection(String collectionPath, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expect(mappingService.getCollectionIdFor(device, collectionPath))
			.andReturn(collectionId).once();
	}
	
	private void expectMappingServiceSearchThenCreateCollection(String collectionPath, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expect(mappingService.getCollectionIdFor(device, collectionPath))
			.andThrow(new CollectionNotFoundException()).once();
		
		expect(mappingService.createCollectionMapping(device, collectionPath))
			.andReturn(collectionId).once();
	}
	
	private void expectMappingServiceLookupCollection(String collectionPath, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expectMappingServiceFindCollection(collectionPath, collectionId);
		expect(mappingService.collectionIdToString(collectionId))
			.andReturn(String.valueOf(collectionId)).once();
	}

	private Folder newFolderObject(String displayName, int folderId) {
		return Folder.builder()
			.uid(folderId)
			.ownerDisplayName(displayName)
			.name(displayName)
			.build();
	}

	private void expectLoginBehavior() throws AuthFault {
		expect(loginService.login(userDataRequest.getUser().getLoginAtDomain(), userDataRequest.getPassword()))
			.andReturn(accessToken).once();
		
		loginService.logout(accessToken);
		expectLastCall().once();
	}
	
	private static class ContactCollectionPath extends CollectionPath {

		public ContactCollectionPath(String displayName) {
			super(COLLECTION_CONTACT_PREFIX + displayName, PIMDataType.CONTACTS, displayName);
		}
	}
}
