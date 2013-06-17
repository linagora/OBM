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

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NoPermissionException;

import net.fortuna.ical4j.util.Strings;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.BackendWindowingService;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.CollectionPath.Builder;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.OpushCollection;
import org.obm.push.backend.PathsToCollections;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.service.ClientIdService;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.DateUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;

@RunWith(SlowFilterRunner.class)
public class ContactsBackendTest {

	private static final String COLLECTION_CONTACT_PREFIX = "obm:\\\\test@test\\contacts\\";
	private static final String DEFAULT_PARENT_BOOK_ID = "0";
	private static final String DEFAULT_PARENT_BOOK_NAME = "contacts";
	
	private User user;
	private Device device;
	private UserDataRequest userDataRequest;
	
	private IMocksControl mocks;
	private MappingService mappingService;
	private BookClient bookClient;
	private LoginService loginService;
	private ContactConfiguration contactConfiguration;
	private Provider<CollectionPath.Builder> collectionPathBuilderProvider;
	private BackendWindowingService backendWindowingService;
	private ClientIdService clientIdService;
	private AccessToken token;
	private ContactsBackend contactsBackend;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device);
		token = new AccessToken(0, "OBM");
		
		mocks = createControl();
		mappingService = mocks.createMock(MappingService.class);
		bookClient = mocks.createMock(BookClient.class);
		loginService = mocks.createMock(LoginService.class);
		contactConfiguration = mocks.createMock(ContactConfiguration.class);
		collectionPathBuilderProvider = mocks.createMock(Provider.class);
		backendWindowingService = mocks.createMock(BackendWindowingService.class);
		clientIdService = mocks.createMock(ClientIdService.class);
		
		contactsBackend = new ContactsBackend(mappingService, bookClient, loginService, contactConfiguration,
				collectionPathBuilderProvider, backendWindowingService, clientIdService);
		
		expectDefaultAddressAndParentForContactConfiguration();
	}
	
	@Test
	public void sortedByDefaultFolderName() {
		final String defaultFolderName = DEFAULT_PARENT_BOOK_NAME;
		
		Folder f1 = Folder.builder().name("users").uid(-1).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder f2 = Folder.builder().name("collected_contacts").uid(2).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder f3 = Folder.builder().name(defaultFolderName).uid(3).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder f4 = Folder.builder().name("my address book").uid(4).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		
		TreeSet<Folder> treeset = new TreeSet<Folder>(new ComparatorUsingFolderName(defaultFolderName));
		treeset.addAll(ImmutableList.of(f1, f2, f3, f4));
		
		assertThat(treeset).hasSize(4);
		assertThat(treeset).contains(f1, f2, f3, f4);
		assertThat(treeset.first().getName()).isEqualTo(defaultFolderName);
		assertThat(treeset.last().getName()).isEqualTo("users");
	}
	
	@Test
	public void testGetPIMDataType() {
		ContactsBackend contactsBackend = new ContactsBackend(null, null, null, null, null, null, null);
		assertThat(contactsBackend.getPIMDataType()).isEqualTo(PIMDataType.CONTACTS);
	}

	@Test
	public void testGetChanges() throws Exception {
		int collectionId = 1;
		Date currentDate = DateUtils.getCurrentDate();
		SyncKey newSyncKey = new SyncKey("789");
		ItemSyncState lastKnownState = ItemSyncState.builder()
				.syncDate(currentDate)
				.syncKey(new SyncKey("1234567890a"))
				.build();

		List<AddressBook> books = ImmutableList.of(newAddressBookObject("folder", collectionId, false));
		
		expectLoginBehavior(token);
		expectListAllBooks(token, books);
		expectBuildCollectionPath("folder", collectionId);

		ContactChanges contactChanges = new ContactChanges(ImmutableList.<Contact> of(), ImmutableSet.<Integer> of(), currentDate);
		expect(bookClient.listContactsChanged(token, currentDate, collectionId)).andReturn(contactChanges).once();
		expectMappingServiceCollectionIdBehavior(books);
		
		mocks.replay();
		DataDelta dataDelta = contactsBackend.getAllChanges(userDataRequest, lastKnownState, collectionId, newSyncKey);
		mocks.verify();

		assertThat(dataDelta).isEqualTo(DataDelta.builder()
				.changes(ImmutableList.<ItemChange>of())
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(currentDate)
				.syncKey(newSyncKey)
				.build());
	}

	@Test
	public void testGetAllChangesOnFirstSync() throws Exception {
		int collectionId = 1;
		Date currentDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		SyncKey newSyncKey = new SyncKey("789");
		ItemSyncState lastKnownState = ItemSyncState.builder()
				.syncDate(currentDate)
				.syncKey(new SyncKey("1234567890a"))
				.build();

		List<AddressBook> books = ImmutableList.of(newAddressBookObject("folder", collectionId, false));
		
		expectLoginBehavior(token);
		expectListAllBooks(token, books);
		expectBuildCollectionPath("folder", collectionId);

		Contact contact = new Contact();
		contact.setFirstname("contact");
		contact.setUid(1);
		Contact contact2 = new Contact();
		contact2.setFirstname("contact2");
		contact2.setUid(2);
		
		ContactChanges contactChanges = new ContactChanges(ImmutableList.of(contact, contact2), ImmutableSet.<Integer> of(), currentDate);
		expect(bookClient.firstListContactsChanged(token, currentDate, collectionId)).andReturn(contactChanges).once();
		expectMappingServiceCollectionIdBehavior(books);
		
		expect(mappingService.getServerIdFor(collectionId, Strings.valueOf(contact.getUid())))
			.andReturn("1:" + contact.getUid()).once();
		expect(mappingService.getServerIdFor(collectionId, Strings.valueOf(contact2.getUid())))
			.andReturn("1:" + contact2.getUid()).once();
		
		mocks.replay();
		DataDelta dataDelta = contactsBackend.getAllChanges(userDataRequest, lastKnownState, collectionId, newSyncKey);
		mocks.verify();

		MSContact msContact = new MSContact();
		msContact.setFirstName(contact.getFirstname());
		msContact.setFileAs(contact.getFirstname());
		ItemChange expectedItemChange = new ItemChangeBuilder()
			.serverId("1:1")
			.withApplicationData(msContact)
			.build();
		MSContact msContact2 = new MSContact();
		msContact2.setFirstName(contact2.getFirstname());
		msContact2.setFileAs(contact2.getFirstname());
		ItemChange expectedItemChange2 = new ItemChangeBuilder()
			.serverId("1:2")
			.withApplicationData(msContact2)
			.build();
		
		assertThat(dataDelta).isEqualTo(DataDelta.builder()
				.changes(ImmutableList.of(expectedItemChange, expectedItemChange2))
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(currentDate)
				.syncKey(newSyncKey)
				.build());
	}

	@Test
	public void testGetItemEstimateSize() throws Exception {
		Date currentDate = DateUtils.getCurrentDate();
		ItemSyncState lastKnownState = ItemSyncState.builder()
				.syncDate(currentDate)
				.syncKey(new SyncKey("1234567890a"))
				.build();

		expectLoginBehavior(token);

		int otherContactCollectionUid = 1;
		int targetcontactCollectionUid = 2;
		List<AddressBook> books = ImmutableList.of(
				newAddressBookObject("folder", otherContactCollectionUid, false),
				newAddressBookObject("folder_1", targetcontactCollectionUid, false));

		expectListAllBooks(token, books);
		expectBuildCollectionPath("folder", otherContactCollectionUid);
		expectBuildCollectionPath("folder_1", targetcontactCollectionUid);

		int contactChangedUid = 215;
		Contact contactChanged = newContactObject(contactChangedUid);
		ContactChanges contactChanges = new ContactChanges(ImmutableList.<Contact> of(contactChanged), ImmutableSet.<Integer> of(), currentDate);
		expect(bookClient.listContactsChanged(token, currentDate, targetcontactCollectionUid))
			.andReturn(contactChanges).once();
		
		expectMappingServiceCollectionIdBehavior(books);
		
		expect(mappingService.getServerIdFor(targetcontactCollectionUid, String.valueOf(contactChangedUid)))
			.andReturn("1215");
		
		mocks.replay();
		int itemEstimateSize = contactsBackend.getItemEstimateSize(userDataRequest, lastKnownState, targetcontactCollectionUid, null);
		mocks.verify();
		
		assertThat(itemEstimateSize).isEqualTo(1);
	}
	
	@Test
	public void testCreateOrUpdate() throws Exception {
		int otherContactCollectionUid = 1;
		int targetcontactCollectionUid = 2;
		int serverId = 215;
		String serverIdAsString = String.valueOf(serverId);
		String clientId = "1";

		List<AddressBook> books = ImmutableList.of(
				newAddressBookObject("folder", otherContactCollectionUid, false),
				newAddressBookObject("folder_1", targetcontactCollectionUid, false));
		
		expectLoginBehavior(token);
		expectListAllBooks(token,books);
		expectBuildCollectionPath("folder", otherContactCollectionUid);
		expectBuildCollectionPath("folder_1", targetcontactCollectionUid);
		
		Contact contact = newContactObject(serverId);
		expect(bookClient.modifyContact(token, targetcontactCollectionUid, contact))
			.andReturn(contact).once();
		
		expectMappingServiceCollectionIdBehavior(books);
		
		expect(mappingService.getItemIdFromServerId(serverIdAsString)).andReturn(serverId).once();
		expect(mappingService.getServerIdFor(targetcontactCollectionUid, serverIdAsString))
			.andReturn(serverIdAsString);

		MSContact msContact = new MSContact();

		mocks.replay();
		String newServerId = contactsBackend.createOrUpdate(userDataRequest, targetcontactCollectionUid, serverIdAsString, clientId, msContact);
		mocks.verify();
		
		assertThat(newServerId).isEqualTo(serverIdAsString);
	}
	
	@Test
	public void testCreateOrUpdateCreatesContact() throws Exception {
		int otherContactCollectionUid = 1;
		int targetcontactCollectionUid = 2;
		int serverId = 215;
		String clientId = "1";
		String clientIdHash = "146565647814688";

		List<AddressBook> books = ImmutableList.of(
				newAddressBookObject("folder", otherContactCollectionUid, false),
				newAddressBookObject("folder_1", targetcontactCollectionUid, false));
		
		expectLoginBehavior(token);
		expectListAllBooks(token,books);
		expectBuildCollectionPath("folder", otherContactCollectionUid);
		expectBuildCollectionPath("folder_1", targetcontactCollectionUid);
		expect(clientIdService.hash(userDataRequest, clientId)).andReturn(clientIdHash);
		
		MSContact msContact = new MSContact();
		
		Contact contact = newContactObject(serverId);
		Contact convertedContact = new ContactConverter().contact(msContact);
		expect(bookClient.createContact(token, targetcontactCollectionUid, convertedContact, clientIdHash))
			.andReturn(contact).once();
		
		expectMappingServiceCollectionIdBehavior(books);
		
		String serverIdAsString = String.valueOf(serverId);
		expect(mappingService.getItemIdFromServerId(null))
			.andReturn(null).once();
		expect(mappingService.getServerIdFor(targetcontactCollectionUid, serverIdAsString))
			.andReturn(serverIdAsString);

		mocks.replay();
		
		String newServerId = contactsBackend.createOrUpdate(userDataRequest, targetcontactCollectionUid, null, clientId, msContact);
		
		mocks.verify();
		
		assertThat(newServerId).isEqualTo(serverIdAsString);
	}
	
	@Test(expected=NoPermissionException.class)
	public void testCreateNoPermissionLetsPropagateTheException() throws Exception {
		int contactCollectionUid = 2;
		int itemId = 215;
		String serverId = null;
		String clientId = "489654";
		String clientIdHash = "78481484087";
		MSContact msContact = new MSContact();

		List<AddressBook> books = ImmutableList.of(
				newAddressBookObject("folder", contactCollectionUid, false));
		
		expectLoginBehavior(token);
		expectListAllBooks(token,books);
		expectBuildCollectionPath("folder", contactCollectionUid);
		expectMappingServiceCollectionIdBehavior(books);
		expect(mappingService.getItemIdFromServerId(serverId)).andReturn(itemId).once();
		expect(clientIdService.hash(userDataRequest, clientId)).andReturn(clientIdHash);
		
		expect(bookClient.createContact(token, contactCollectionUid, new Contact(), clientIdHash)).andThrow(new NoPermissionException());

		mocks.replay();
		try {
			contactsBackend.createOrUpdate(userDataRequest, contactCollectionUid, serverId, clientId, msContact);
		} catch (NoPermissionException e) {
			mocks.verify();
			throw e;
		}
	}

	@Test(expected=NoPermissionException.class)
	public void testUpdateNoPermissionLetsPropagateTheException() throws Exception {
		int contactCollectionUid = 2;
		int itemId = 215;
		String serverId = "2:215";
		String clientId = null;
		MSContact msContact = new MSContact();

		List<AddressBook> books = ImmutableList.of(
				newAddressBookObject("folder", contactCollectionUid, false));
		
		expectLoginBehavior(token);
		expectListAllBooks(token,books);
		expectBuildCollectionPath("folder", contactCollectionUid);
		expectMappingServiceCollectionIdBehavior(books);
		expect(mappingService.getItemIdFromServerId(serverId)).andReturn(itemId).once();
		
		Contact contact = newContactObject(itemId);
		expect(bookClient.modifyContact(token, contactCollectionUid, contact)).andThrow(new NoPermissionException());

		mocks.replay();
		try {
			contactsBackend.createOrUpdate(userDataRequest, contactCollectionUid, serverId, clientId, msContact);
		} catch (NoPermissionException e) {
			mocks.verify();
			throw e;
		}
	}
	
	@Test
	public void testDelete() throws Exception {
		int otherContactCollectionUid = 1;
		int targetcontactCollectionUid = 2;
		int serverId = 2;
		String serverIdAsString = String.valueOf(serverId);
		
		List<AddressBook> books = ImmutableList.of(
				newAddressBookObject("folder", otherContactCollectionUid, false),
				newAddressBookObject("folder_1", targetcontactCollectionUid, false));

		expectLoginBehavior(token);
		expectListAllBooks(token, books);
		expectBuildCollectionPath("folder", otherContactCollectionUid);
		expectBuildCollectionPath("folder_1", targetcontactCollectionUid);
		
		expect(bookClient.removeContact(token, targetcontactCollectionUid, serverId))
			.andReturn(newContactObject(serverId)).once();

		expect(mappingService.getItemIdFromServerId(serverIdAsString)).andReturn(serverId).once();
		expectMappingServiceCollectionIdBehavior(books);

		mocks.replay();
		contactsBackend.delete(userDataRequest, serverId, serverIdAsString, true);
		mocks.verify();
	}

	@Test
	public void testFetch() throws Exception {
		int otherContactCollectionUid = 1;
		int targetcontactCollectionUid = 2;
		int itemId = 215;
		String serverId = targetcontactCollectionUid + ":" + itemId;
		
		List<AddressBook> books = ImmutableList.of(
				newAddressBookObject("folder", otherContactCollectionUid, false),
				newAddressBookObject("folder_1", targetcontactCollectionUid, false));

		expectLoginBehavior(token);
		expectListAllBooks(token, books);
		expectBuildCollectionPath("folder", otherContactCollectionUid);
		expectBuildCollectionPath("folder_1", targetcontactCollectionUid);
		
		Contact contact = newContactObject(itemId);
		expect(bookClient.getContactFromId(token, targetcontactCollectionUid, itemId)).andReturn(contact);

		expectMappingServiceCollectionIdBehavior(books);
		expect(mappingService.getItemIdFromServerId(serverId)).andReturn(itemId);
		expect(mappingService.getServerIdFor(targetcontactCollectionUid, String.valueOf(itemId))).andReturn(serverId);
	
		mocks.replay();
		List<ItemChange> itemChanges = contactsBackend.fetch(userDataRequest, targetcontactCollectionUid, ImmutableList.of(serverId), null, null);
		mocks.verify();
		
		ItemChange itemChange = new ItemChange(serverId, false);
		itemChange.setData(new ContactConverter().convert(contact));
		
		assertThat(itemChanges).hasSize(1);
		assertThat(itemChanges).containsOnly(itemChange);
	}

	private void expectListAllBooks(AccessToken token, List<AddressBook> addressbooks) throws ServerFault {
		expect(bookClient.listAllBooks(token))
			.andReturn(addressbooks)
			.once();
	}

	private Contact newContactObject(int contactUid) {
		Contact contact = new Contact();
		contact.setUid(contactUid);
		return contact;
	}

	private AddressBook newAddressBookObject(String name, Integer uid, boolean readOnly) {
		return new AddressBook(name, uid, readOnly);
	}
	
	private void expectLoginBehavior(AccessToken token) throws AuthFault {
		expect(loginService.login(userDataRequest.getUser().getLoginAtDomain(), userDataRequest.getPassword()))
			.andReturn(token).anyTimes();
		
		loginService.logout(token);
		expectLastCall().anyTimes();
	}

	private void expectDefaultAddressAndParentForContactConfiguration() {
		expect(contactConfiguration.getDefaultAddressBookName())
			.andReturn(DEFAULT_PARENT_BOOK_NAME).anyTimes();
		
		expect(contactConfiguration.getDefaultParentId())
			.andReturn(DEFAULT_PARENT_BOOK_ID).anyTimes();
	}

	private void expectMappingServiceCollectionIdBehavior(List<AddressBook> books) 
			throws CollectionNotFoundException, DaoException {
		
		for (AddressBook book : books) {
			expect(mappingService.getCollectionIdFor(userDataRequest.getDevice(),
					COLLECTION_CONTACT_PREFIX + backendName(book.getName(), book.getUid()))).andReturn(book.getUid()).anyTimes();

			expect(mappingService.collectionIdToString(book.getUid())).andReturn(String.valueOf(book.getUid())).anyTimes();
		}
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
		
		expect(collectionPathBuilder.backendName(backendName(displayName, folderUid)))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.build())
			.andReturn(collectionPath).once();
		
		return collectionPathBuilder;
	}

	private void expectCollectionPathBuilderPovider(CollectionPath.Builder collectionPathBuilder) {
			expect(collectionPathBuilderProvider.get())
				.andReturn(collectionPathBuilder).once();
	}
	
	private static class ContactCollectionPath extends CollectionPath {

		public ContactCollectionPath(String displayName, int folderUid) {
			super(String.format("%s%s", COLLECTION_CONTACT_PREFIX, ContactsBackendTest.backendName(displayName, folderUid)),
					PIMDataType.CONTACTS, ContactsBackendTest.backendName(displayName, folderUid));
		}
	}

	private static String backendName(String displayName, int folderUid) {
		return String.format("%d:%s", folderUid, displayName);
	}
	
	@Test
	public void changeDisplayNameAndOwnerLoginAtDomainAreTakenFromFolderForAdd() {
		String folder1Name = "f1";
		String folder2Name = "f2";
		FolderChanges changes = FolderChanges.builder()
			.updated(
				Folder.builder().name(folder1Name).uid(1).ownerLoginAtDomain(user.getLoginAtDomain()).build(),
				Folder.builder().name(folder2Name).uid(2).ownerLoginAtDomain("owner@domain").build())
			.build();

		expectBuildCollectionPath(folder1Name, 1);
		expectBuildCollectionPath(folder2Name, 2);

		mocks.replay();
		Iterable<OpushCollection> actual = contactsBackend.changedCollections(userDataRequest, changes).collections();
		mocks.verify();
		
		assertThat(actual).containsOnly(
				OpushCollection.builder()
					.collectionPath(new ContactCollectionPath(folder1Name, 1))
					.displayName(folder1Name)
					.ownerLoginAtDomain(user.getLoginAtDomain())
					.build(),
				OpushCollection.builder()
					.collectionPath(new ContactCollectionPath(folder2Name, 2))
					.displayName(folder2Name)
					.ownerLoginAtDomain("owner@domain")
					.build());
	}
	
	@Test
	public void changeDisplayNameIsTookFromFolderForDelete() {
		String folder1Name = "f1";
		String folder2Name = "f2";
		int folder1Uid = 1;
		int folder2Uid = 2;
		ContactCollectionPath f1CollectionPath = new ContactCollectionPath(folder1Name, folder1Uid);
		ContactCollectionPath f2CollectionPath = new ContactCollectionPath(folder2Name, folder2Uid);
		FolderChanges changes = FolderChanges.builder()
				.removed(
					Folder.builder().name(folder1Name).uid(folder1Uid).ownerLoginAtDomain(user.getLoginAtDomain()).build(),
					Folder.builder().name(folder2Name).uid(folder2Uid).ownerLoginAtDomain("owner@domain").build())
				.build();
		
		PathsToCollections adds = PathsToCollections.builder().build();
		Set<CollectionPath> lastKnown = ImmutableSet.<CollectionPath>of(f1CollectionPath, f2CollectionPath);

		expectBuildCollectionPath(folder1Name, folder1Uid);
		expectBuildCollectionPath(folder2Name, folder2Uid);

		mocks.replay();
		Iterable<CollectionPath> actual = contactsBackend.deletedCollections(userDataRequest, changes, lastKnown, adds);
		mocks.verify();
		
		assertThat(actual).containsOnly(f1CollectionPath, f2CollectionPath);
	}
	
	@Test
	public void createItemChangeGetsDisplayNameFromOpushCollection() throws Exception {
		AccessToken token = new AccessToken(0, "OBM");
		expectLoginBehavior(token);

		int folderUid = 3;
		expect(mappingService.collectionIdToString(folderUid)).andReturn(String.valueOf(folderUid)).anyTimes();
		expect(mappingService.getCollectionIdFor(userDataRequest.getDevice(), 
				COLLECTION_CONTACT_PREFIX + backendName("technicalName", folderUid)))
			.andReturn(folderUid).anyTimes();

		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new ContactCollectionPath("technicalName", folderUid))
				.displayName("great display name!")
				.build();
		
		mocks.replay();
		CollectionChange itemChange = contactsBackend.createCollectionChange(userDataRequest, collection);
		mocks.verify();
		
		assertThat(itemChange).isEqualTo(CollectionChange.builder()
				.displayName("great display name!")
				.parentCollectionId("0")
				.collectionId("3")
				.folderType(FolderType.USER_CREATED_CONTACTS_FOLDER)
				.isNew(true)
				.build());
	}
	
	@Test
	public void filterUnknownDeletedItemsFromAddressBooksChanged() {
		String folderOneName = "f1";
		String folderTwoName = "f2";
		Folder folder1 = Folder.builder().name(folderOneName).uid(1).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder folder2 = Folder.builder().name(folderTwoName).uid(2).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		FolderChanges changes = FolderChanges.builder().removed(folder1, folder2).build();
		
		ContactCollectionPath f1CollectionPath = new ContactCollectionPath(folderOneName, 1);
		ImmutableSet<CollectionPath> lastKnown = ImmutableSet.<CollectionPath>of(f1CollectionPath);
		PathsToCollections adds = PathsToCollections.builder().build();

		expectBuildCollectionPath(folderOneName, 1);
		expectBuildCollectionPath(folderTwoName, 2);

		mocks.replay();
		Iterable<CollectionPath> actual = contactsBackend.deletedCollections(userDataRequest, changes, lastKnown, adds);
		mocks.verify();
		
		assertThat(actual).containsOnly(f1CollectionPath);
	}
	
	@Test
	public void createItemChangeBuildsWithParentIdFromConfiguration() throws Exception {
		int folderUid = 3;
		expect(mappingService.collectionIdToString(folderUid)).andReturn(String.valueOf(folderUid)).anyTimes();
		expect(mappingService.getCollectionIdFor(userDataRequest.getDevice(), 
				COLLECTION_CONTACT_PREFIX + backendName("technicalName", folderUid)))
			.andReturn(folderUid).anyTimes();

		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new ContactCollectionPath("technicalName", folderUid))
				.displayName("displayName")
				.build();
		
		mocks.replay();
		CollectionChange itemChange = contactsBackend.createCollectionChange(userDataRequest, collection);
		mocks.verify();

		assertThat(itemChange.getParentCollectionId()).isEqualTo(DEFAULT_PARENT_BOOK_ID);
	}
	
	@Test
	public void testSortingKeepsFolderWithSameNames() {
		Folder folder1 = Folder.builder().name("users").uid(1).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder folder2 = Folder.builder().name("users").uid(2).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder folder3 = Folder.builder().name("users").uid(3).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		FolderChanges changes = FolderChanges.builder().updated(folder1, folder2, folder3).build();
		
		Iterable<Folder> result = contactsBackend.sortedFolderChangesByDefaultAddressBook(changes, "defaultName");
		
		assertThat(result).hasSize(3);
		assertThat(result).containsOnly(folder1, folder2, folder3);
	}
	
	@Test
	public void testSortingKeepsFolderWithSameNamesAndSameUid() {
		Folder folder1 = Folder.builder().name("users").uid(1).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder folder2 = Folder.builder().name("users").uid(2).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder folder3 = Folder.builder().name("users").uid(2).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder folder4 = Folder.builder().name("users").uid(3).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		Folder folder5 = Folder.builder().name("users").uid(1).ownerLoginAtDomain(user.getLoginAtDomain()).build();
		
		FolderChanges changes = FolderChanges.builder().updated(folder1, folder2, folder3, folder4, folder5).build();
		
		Iterable<Folder> result = contactsBackend.sortedFolderChangesByDefaultAddressBook(changes, "defaultName");
		
		assertThat(result).hasSize(3);
		assertThat(result).containsOnly(folder1, folder2, folder4);
	}
	
	@Test
	public void testIsDefaultFolderRightNameAndBadUser() {
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new ContactCollectionPath(DEFAULT_PARENT_BOOK_NAME, 5))
				.displayName("displayName")
				.ownerLoginAtDomain("owner@domain")
				.build();

		mocks.replay();
		boolean defaultFolder = contactsBackend.isDefaultFolder(userDataRequest, collection);
		mocks.verify();
		
		assertThat(defaultFolder).isFalse();
	}
	
	@Test
	public void testIsDefaultFolderBadNameAndRightUser() {
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new ContactCollectionPath("contacts book", 5))
				.displayName("displayName")
				.ownerLoginAtDomain(userDataRequest.getUser().getLoginAtDomain())
				.build();

		mocks.replay();
		boolean defaultFolder = contactsBackend.isDefaultFolder(userDataRequest, collection);
		mocks.verify();
		
		assertThat(defaultFolder).isFalse();
	}
	
	@Test
	public void testIsDefaultFolderRightNameAndUser() {
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new ContactCollectionPath(DEFAULT_PARENT_BOOK_NAME, 5))
				.displayName("displayName")
				.ownerLoginAtDomain(userDataRequest.getUser().getLoginAtDomain())
				.build();
		
		mocks.replay();
		boolean defaultFolder = contactsBackend.isDefaultFolder(userDataRequest, collection);
		mocks.verify();
		
		assertThat(defaultFolder).isTrue();
	}
}
