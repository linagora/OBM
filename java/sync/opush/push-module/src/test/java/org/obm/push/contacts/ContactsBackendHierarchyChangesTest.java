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
import org.obm.push.bean.ItemDeletion;
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
	private String contactParentName;
	private int contactParentId;
	private String contactParentIdAsString;

	private IMocksControl mocks;
	private MappingService mappingService;
	private BookClient bookClient;
	private LoginService loginService;
	private ContactConfiguration contactConfiguration;
	private Provider<CollectionPath.Builder> collectionPathBuilderProvider;
	
	private ContactsBackend contactsBackend;


	@Before
	public void setUp() throws Exception {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);
		accessToken = new AccessToken(0, "OBM");
		contactParentId = 0;
		contactParentIdAsString = String.valueOf(contactParentId);
		contactParentName = "contacts";

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
		
		expectLoginBehavior();
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
		
		Folder change = newFolderObject(contactParentName, contactParentId);
		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.of(change), ImmutableSet.<Folder>of());
		
		List<CollectionPath> knownCollections = ImmutableList.of(); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceSearchThenCreateCollection(contactParentName, contactParentId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(contactParentId));
		expectMappingServiceLookupCollection(contactParentName, contactParentId);
		
		expectBuildCollectionPath(contactParentName, contactParentId);

		mocks.replay();
		
		HierarchyItemsChanges hierarchyItemsChanges = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		mocks.verify();
		
		ItemChange expectedItemChange = new ItemChangeBuilder()
				.serverId(String.valueOf(contactParentId))
				.parentId("0")
				.displayName(contactParentName)
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

		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.<Folder>of(), ImmutableSet.<Folder>of());

		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath(contactParentName, contactParentId)); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceFindCollection(contactParentName, contactParentId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(contactParentId));
		
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
		
		int otherCollectionMappingId = 203;
		String otherCollectionDisplayName = "no default address book";
		
		Folder change = newFolderObject(otherCollectionDisplayName, Integer.valueOf(otherCollectionMappingId));
		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.of(change), ImmutableSet.<Folder>of());

		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath(contactParentName, contactParentId)); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceFindCollection(contactParentName, contactParentId);
		expectMappingServiceSearchThenCreateCollection(otherCollectionDisplayName, otherCollectionMappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(contactParentId, otherCollectionMappingId));
		expectMappingServiceLookupCollection(otherCollectionDisplayName, otherCollectionMappingId);

		expectBuildCollectionPath(otherCollectionDisplayName, otherCollectionMappingId);
		
		mocks.replay();
		
		HierarchyItemsChanges hierarchyItemsChanges = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		mocks.verify();

		ItemChange expectedItemChange = new ItemChangeBuilder()
				.serverId(String.valueOf(otherCollectionMappingId))
				.parentId(contactParentIdAsString)
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
		
		int otherCollectionMappingId = 203;
		String otherCollectionDisplayName = "no default address book";

		Folder contactDeletion = newFolderObject(contactParentName, contactParentId);
		Folder otherCollectionDeletion = newFolderObject(otherCollectionDisplayName, otherCollectionMappingId);
		expectBookClientListBooksChanged(lastKnownState, ImmutableSet.<Folder>of(), ImmutableSet.of(contactDeletion, otherCollectionDeletion));

		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath(contactParentName, contactParentId),
				new ContactCollectionPath(otherCollectionDisplayName, otherCollectionMappingId)); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceLookupCollection(contactParentName, contactParentId);
		expectMappingServiceLookupCollection(otherCollectionDisplayName, otherCollectionMappingId);

		expectBuildCollectionPath(contactParentName, contactParentId);
		expectBuildCollectionPath(otherCollectionDisplayName, otherCollectionMappingId);
		
		mocks.replay();
		
		HierarchyItemsChanges hierarchyItemsChanges = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		mocks.verify();

		ItemDeletion expectedItemDeletion = ItemDeletion.builder()
				.serverId(String.valueOf(contactParentId))
				.build();
		ItemDeletion expectedItem2Deletion = ItemDeletion.builder()
				.serverId(String.valueOf(otherCollectionMappingId))
				.build();
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).hasSize(2);
		assertThat(hierarchyItemsChanges.getDeletedItems()).containsOnly(expectedItemDeletion, expectedItem2Deletion);
	}
	
	@Test
	public void testSameAddAndDeleteDiscardDelete() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));
		int targetCollectionId = 2;
			
		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of();
		Set<Folder> updated = ImmutableSet.of(newFolderObject("both", targetCollectionId));
		Set<Folder> removed = ImmutableSet.of(newFolderObject("both", targetCollectionId));
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceSearchThenCreateCollection("both", targetCollectionId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(targetCollectionId));
		expectMappingServiceLookupCollection("both", targetCollectionId);

		expectBuildCollectionPath("both", targetCollectionId);
		expectBuildCollectionPath("both", targetCollectionId);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).containsOnly(new ItemChange(String.valueOf(targetCollectionId),
				contactParentIdAsString, "both", FolderType.USER_CREATED_CONTACTS_FOLDER, true));
		assertThat(changes.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testSameLastKnownAndAdd() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));
		int targetCollectionId = 2;
		
		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of(new ContactCollectionPath("both", targetCollectionId));
		Set<Folder> updated = ImmutableSet.of(newFolderObject("both", targetCollectionId));
		Set<Folder> removed = ImmutableSet.of();
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceFindCollection("both", targetCollectionId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(targetCollectionId));

		expectBuildCollectionPath("both", targetCollectionId);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).isEmpty();
		assertThat(changes.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testTwoSameLastKnownAndAdd() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath("both", 2), new ContactCollectionPath("both2", 3));
		Set<Folder> updated = ImmutableSet.of(newFolderObject("both", 2), newFolderObject("both2", 3));
		Set<Folder> removed = ImmutableSet.of();
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceFindCollection("both", 2);
		expectMappingServiceFindCollection("both2", 3);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(2, 3));

		expectBuildCollectionPath("both", 2);
		expectBuildCollectionPath("both2", 3);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).isEmpty();
		assertThat(changes.getDeletedItems()).isEmpty();
	}

	@Test
	public void testOneLastKnownInTwoAdd() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of(new ContactCollectionPath("known", 2));
		Set<Folder> updated = ImmutableSet.of(newFolderObject("known", 2), newFolderObject("add", 3));
		Set<Folder> removed = ImmutableSet.of();
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceSearchThenCreateCollection("add", 3);
		expectMappingServiceFindCollection("known", 2);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(2, 3));
		expectMappingServiceLookupCollection("add", 3);

		expectBuildCollectionPath("add", 3);
		expectBuildCollectionPath("known", 2);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).containsOnly(
				new ItemChange("3", contactParentIdAsString, "add", FolderType.USER_CREATED_CONTACTS_FOLDER, true));
		assertThat(changes.getDeletedItems()).isEmpty();
	}

	@Test
	public void testOneAddOneKnownDelete() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of(new ContactCollectionPath("known", 2));
		Set<Folder> updated = ImmutableSet.of(newFolderObject("add", 3));
		Set<Folder> removed = ImmutableSet.of(newFolderObject("known", 2));
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceSearchThenCreateCollection("add", 3);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(3));
		expectMappingServiceLookupCollection("add", 3);
		expectMappingServiceLookupCollection("known", 2);

		expectBuildCollectionPath("add", 3);
		expectBuildCollectionPath("known", 2);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).containsOnly(
				new ItemChange("3", contactParentIdAsString, "add", FolderType.USER_CREATED_CONTACTS_FOLDER, true));
		assertThat(changes.getDeletedItems()).containsOnly(
				ItemDeletion.builder().serverId("2").build());
	}
	
	@Test
	public void testOneAddOneUnknownDelete() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of();
		Set<Folder> updated = ImmutableSet.of(newFolderObject("add", 3));
		Set<Folder> removed = ImmutableSet.of(newFolderObject("unknown", 2));
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceSearchThenCreateCollection("add", 3);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(3));
		expectMappingServiceLookupCollection("add", 3);

		expectBuildCollectionPath("add", 3);
		expectBuildCollectionPath("unknown", 2);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).containsOnly(
				new ItemChange("3", contactParentIdAsString, "add", FolderType.USER_CREATED_CONTACTS_FOLDER, true));
		assertThat(changes.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testTwoAddWithSameNamesAndDifferentUidsKeepBoth() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of();
		Set<Folder> updated = ImmutableSet.of(newFolderObject("both", 2), newFolderObject("both", 3));
		Set<Folder> removed = ImmutableSet.of();
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceSearchThenCreateCollection("both", 2);
		expectMappingServiceSearchThenCreateCollection("both", 3);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(2, 3));
		expectMappingServiceLookupCollection("both", 2);
		expectMappingServiceLookupCollection("both", 3);

		expectBuildCollectionPath("both", 3);
		expectBuildCollectionPath("both", 2);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).containsOnly(
				new ItemChange("2", contactParentIdAsString, "both", FolderType.USER_CREATED_CONTACTS_FOLDER, true),
				new ItemChange("3", contactParentIdAsString, "both", FolderType.USER_CREATED_CONTACTS_FOLDER, true));
		assertThat(changes.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testTwoAddWithSameNamesAndSameUidsDiscardsOne() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of();
		Set<Folder> updated = ImmutableSet.of(newFolderObject("both", 2), newFolderObject("both", 2));
		Set<Folder> removed = ImmutableSet.of();
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceSearchThenCreateCollection("both", 2);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(2));
		expectMappingServiceLookupCollection("both", 2);

		expectBuildCollectionPath("both", 2);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).containsOnly(
				new ItemChange("2", contactParentIdAsString, "both", FolderType.USER_CREATED_CONTACTS_FOLDER, true));
		assertThat(changes.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testTwoDeleteWithSameNamesAndDifferentUidsKeepBoth() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath("both", 2),
				new ContactCollectionPath("both", 3));
		
		Set<Folder> updated = ImmutableSet.of();
		Set<Folder> removed = ImmutableSet.of(newFolderObject("both", 2), newFolderObject("both", 3));
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceLookupCollection("both", 2);
		expectMappingServiceLookupCollection("both", 3);
		
		expectBuildCollectionPath("both", 2);
		expectBuildCollectionPath("both", 3);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).isEmpty();
		assertThat(changes.getDeletedItems()).containsOnly(
				ItemDeletion.builder().serverId("2").build(),
				ItemDeletion.builder().serverId("3").build());
	}
	
	@Test
	public void testTwoDeleteWithSameNamesAndSameUidsDiscardsOne() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("key1", org.obm.DateUtils.date("2012-05-04T11:02:03"));
		FolderSyncState outgoingSyncState=  new FolderSyncState("key2", org.obm.DateUtils.date("2012-05-04T12:15:05"));

		List<CollectionPath> lastKnown = ImmutableList.<CollectionPath>of(
				new ContactCollectionPath("both", 2),
				new ContactCollectionPath("both", 3));
		
		Set<Folder> updated = ImmutableSet.of();
		Set<Folder> removed = ImmutableSet.of(newFolderObject("both", 2), newFolderObject("both", 2));
		
		expectBookClientListBooksChanged(lastKnownState, updated, removed);
		expectMappingServiceListLastKnowCollection(lastKnownState, lastKnown);
		expectMappingServiceFindCollection("both", 3);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(3));
		expectMappingServiceLookupCollection("both", 2);
		
		expectBuildCollectionPath("both", 2);
		
		mocks.replay();
		HierarchyItemsChanges changes = contactsBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		mocks.verify();
		
		assertThat(changes.getChangedItems()).isEmpty();
		assertThat(changes.getDeletedItems()).containsOnly(
				ItemDeletion.builder().serverId("2").build());
	}
	
	private Builder expectBuildCollectionPath(String displayName, int folderUid) {
		CollectionPath collectionPath = new ContactCollectionPath(displayName, folderUid);
		CollectionPath.Builder collectionPathBuilder = expectCollectionPathBuilder(collectionPath, displayName, folderUid);
		expectCollectionPathBuilderPovider(collectionPathBuilder);
		return collectionPathBuilder;
	}

	private CollectionPath.Builder expectCollectionPathBuilder(CollectionPath collectionPath, 
			String displayName, int folderUid) {
		
		CollectionPath.Builder collectionPathBuilder = mocks.createMock(CollectionPath.Builder.class);
		expect(collectionPathBuilder.userDataRequest(userDataRequest))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.pimType(PIMDataType.CONTACTS))
			.andReturn(collectionPathBuilder).once();

		expect(collectionPathBuilder.backendName(ContactCollectionPath.backendName(displayName, folderUid)))
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

	private void expectMappingServiceFindCollection(String collectionName, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		String collectionPath = 
				COLLECTION_CONTACT_PREFIX + ContactCollectionPath.backendName(collectionName, collectionId);
		
		expect(mappingService.getCollectionIdFor(device, collectionPath))
			.andReturn(collectionId).once();
	}
	
	private void expectMappingServiceSearchThenCreateCollection(String collectionName, Integer collectionId)
		throws CollectionNotFoundException, DaoException {

		String collectionPath = 
				COLLECTION_CONTACT_PREFIX + ContactCollectionPath.backendName(collectionName, collectionId);
		
		expect(mappingService.getCollectionIdFor(device, collectionPath))
			.andThrow(new CollectionNotFoundException()).once();
		
		expect(mappingService.createCollectionMapping(device, collectionPath))
			.andReturn(collectionId).once();
	}
	
	private void expectMappingServiceLookupCollection(String collectionName, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expectMappingServiceFindCollection(collectionName, collectionId);
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

		public ContactCollectionPath(String displayName, int folderUid) {
			super(String.format("%s%s", COLLECTION_CONTACT_PREFIX, backendName(displayName, folderUid)),
					PIMDataType.CONTACTS, displayName);
		}
		
		public static String backendName(String displayName, int folderUid) {
			return String.format("%d-%s", folderUid, displayName);
		}
	}
}
