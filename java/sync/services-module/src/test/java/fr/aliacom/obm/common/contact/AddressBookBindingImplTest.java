/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package fr.aliacom.obm.common.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.naming.NoPermissionException;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.utils.DateUtils;
import org.obm.sync.addition.CommitedElement;
import org.obm.sync.addition.Kind;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.dao.EntityId;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.utils.ObmHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.addition.CommitedOperationDao;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;

@RunWith(GuiceRunner.class)
@GuiceModule(AddressBookBindingImplTest.Env.class)
public class AddressBookBindingImplTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(ObmHelper.class);
			bindWithMock(ContactDao.class);
			bindWithMock(UserDao.class);
			bindWithMock(ContactMerger.class);
			bindWithMock(ContactConfiguration.class);
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(CommitedOperationDao.class);
			bindWithMock(ObmSyncConfigurationService.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	private static final int USERS_ADDRESS_BOOK_ID = -1;
	
	@Inject
	private IMocksControl mocksControl;
	@Inject
	private AddressBookBindingImpl binding;
	@Inject
	private ContactConfiguration contactConfiguration;
	@Inject
	private ContactMerger contactMerger;
	@Inject
	private CommitedOperationDao commitedOperationDao;
	@Inject
	private ObmSyncConfigurationService configuration;
	@Inject
	private UserDao userDao;
	@Inject
	private ContactDao contactDao;
	@Inject
	private ObmHelper helper;

	private AccessToken token;

	@Before
	public void setUp() {
		token = ToolBox.mockAccessToken(mocksControl);
	}

	@After
	public void tearDown() {
		mocksControl.verify();
	}

	/**
	 * Tests that getSync() returns the updated contacts and address books,
	 * including the full list of users from the user DAO, when
	 * AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC is set to true.
	 */
	@Test
	public void testGetSyncGlobalAddressBookSync() throws Exception {
		Date timestamp = DateUtils.getEpochCalendar().getTime();

		Contact newContact = new Contact();
		newContact.setLastname("newContact");

		ContactUpdates contactUpdates = new ContactUpdates();
		contactUpdates.setContacts(ImmutableList.of(newContact));
		contactUpdates.setArchived(ImmutableSet.of(1, 2));

		Contact newUser = new Contact();
		newUser.setLastname("obmuser");

		ContactUpdates userUpdates = new ContactUpdates();
		userUpdates.setContacts(ImmutableList.of(newUser));
		userUpdates.setArchived(ImmutableSet.of(5, 7, 8));

		Folder updatedContactFolder1 = Folder.builder().uid(1).name("updatedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder updatedContactFolder2 = Folder.builder().uid(2).name("updatedContactFolder2").ownerLoginAtDomain("login@obm.org").build();
		Set<Folder> updatedContactFolders = Sets.newHashSet(updatedContactFolder1, updatedContactFolder2);

		Folder updatedUserFolder = Folder.builder().uid(USERS_ADDRESS_BOOK_ID).name("users").ownerLoginAtDomain("user@test.tlse.lng").build();

		Folder removedContactFolder1 = Folder.builder().uid(10).name("removedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder removedContactFolder2 = Folder.builder().uid(11).name("removedContactFolder2").ownerLoginAtDomain("login@obm.org").build();
		Set<Folder> removedContactFolders =  Sets.newHashSet(removedContactFolder1, removedContactFolder2);

		expect(contactDao.findUpdatedContacts(timestamp, token)).andReturn(contactUpdates).once();
		expect(contactDao.findRemovalCandidates(timestamp, token)).andReturn(ImmutableSet.of(3)).once();
		expect(contactDao.findUpdatedFolders(timestamp, token)).andReturn(updatedContactFolders).once();
		expect(contactDao.findRemovedFolders(timestamp, token)).andReturn(removedContactFolders).once();
		expect(userDao.findUpdatedUsers(timestamp, token)).andReturn(userUpdates).once();
		expect(configuration.syncUsersAsAddressBook()).andReturn(true).atLeastOnce();
		expect(contactConfiguration.getAddressBookUserId()).andReturn(-1);
		expect(contactConfiguration.getAddressBookUsersName()).andReturn("users");

		mockGetLastSyncSubtractByTransactionTimeout(timestamp, 0);
		mocksControl.replay();
		
		AddressBookChangesResponse changes = binding.getAddressBookSync(token, timestamp);

		assertThat(changes.getContactChanges().getUpdated()).containsOnly(newContact, newUser);
		assertThat(changes.getRemovedContacts()).containsOnly(1, 2, 3, 5, 7, 8);
		assertThat(changes.getUpdatedAddressBooks()).containsOnly(updatedContactFolder1, updatedContactFolder2, updatedUserFolder);
		assertThat(changes.getRemovedAddressBooks()).containsOnly(removedContactFolder1, removedContactFolder2);
	}

	/**
	 * Tests that getSync() returns the updated contacts and address book, but
	 * not the full list of users from the user DAO, when
	 * AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC is set to false.
	 */
	@Test
	public void testGetSyncNoGlobalAddressBookSync() throws Exception {
		Date timestamp = DateUtils.getEpochCalendar().getTime();

		Contact newContact = new Contact();
		newContact.setLastname("newContact");

		ContactUpdates contactUpdates = new ContactUpdates();
		contactUpdates.setContacts(ImmutableList.of(newContact));
		contactUpdates.setArchived(ImmutableSet.of(1, 2));

		Folder updatedContactFolder1 = Folder.builder().uid(1).name("updatedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder updatedContactFolder2 = Folder.builder().uid(2).name("updatedContactFolder2").ownerLoginAtDomain("login@obm.org").build();
		Set<Folder> updatedContactFolders = Sets.newHashSet(updatedContactFolder1, updatedContactFolder2);

		Folder removedContactFolder1 = Folder.builder().uid(10).name("removedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder removedContactFolder2 = Folder.builder().uid(11).name("removedContactFolder2").ownerLoginAtDomain("login@obm.org").build();
		Set<Folder> removedContactFolders =  Sets.newHashSet(removedContactFolder1, removedContactFolder2);

		expect(contactDao.findUpdatedContacts(timestamp, token)).andReturn(contactUpdates).once();
		expect(contactDao.findRemovalCandidates(timestamp, token)).andReturn(ImmutableSet.of(3)).once();
		expect(contactDao.findUpdatedFolders(timestamp, token)).andReturn(updatedContactFolders).once();
		expect(contactDao.findRemovedFolders(timestamp, token)).andReturn(removedContactFolders).once();
		expect(configuration.syncUsersAsAddressBook()).andReturn(false).atLeastOnce();

		mockGetLastSyncSubtractByTransactionTimeout(timestamp, 0);

		mocksControl.replay();
		
		AddressBookChangesResponse changes = binding.getAddressBookSync(token, timestamp);

		assertThat(changes.getContactChanges().getUpdated()).containsOnly(newContact);
		assertThat(changes.getRemovedContacts()).containsOnly(1, 2, 3);
		assertThat(changes.getUpdatedAddressBooks()).containsOnly(updatedContactFolder1, updatedContactFolder2);
		assertThat(changes.getRemovedAddressBooks()).containsOnly(removedContactFolder1, removedContactFolder2);
	}

	@Test
	public void testCreateContactWithCommitedOperation() throws Exception {
		Integer addressBookId = 1;
		EntityId entityId = EntityId.valueOf(984);
		Contact contact = new Contact(), expectedContact = new Contact();
		String clientId = "6547";

		expectedContact.setEntityId(entityId);

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.createContactInAddressBook(token, contact, addressBookId)).andReturn(expectedContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, addressBookId)).andReturn(true);
		expect(commitedOperationDao.findAsContact(token, clientId)).andReturn(null).once();
		commitedOperationDao.store(token,
				CommitedElement.builder()
					.clientId(clientId)
					.entityId(entityId)
					.kind(Kind.VCONTACT)
					.build());
		expectLastCall().once();
		mocksControl.replay();
		
		Contact createdContact = binding.createContact(token, addressBookId, contact, clientId);
		
		assertThat(createdContact).isEqualTo(expectedContact);
	}

	@Test
	public void testCreateContactAlreadyCommited() throws Exception {
		Integer addressBookId = 1;
		Contact contact = new Contact();
		String clientId = "6547";

		expect(contactDao.hasRightsOnAddressBook(token, addressBookId)).andReturn(true);
		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(commitedOperationDao.findAsContact(token, clientId)).andReturn(contact).once();
		mocksControl.replay();
		
		Contact createdContact = binding.createContact(token, addressBookId, contact, clientId);
		
		assertThat(createdContact).isEqualTo(contact);
	}

	@Test
	public void testCreateContactWhenNullClientId() throws Exception {
		Integer addressBookId = 1, entityId = 984;
		Contact contact = new Contact(), expectedContact = new Contact();

		expectedContact.setEntityId(EntityId.valueOf(entityId));
		
		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.hasRightsOnAddressBook(token, addressBookId)).andReturn(true);
		expect(contactDao.createContactInAddressBook(token, contact, addressBookId)).andReturn(expectedContact).once();
		expect(commitedOperationDao.findAsContact(token, null)).andReturn(null).once();
		mocksControl.replay();
		
		Contact createdContact = binding.createContact(token, addressBookId, contact, null);
		
		assertThat(createdContact).isEqualTo(expectedContact);
	}

	@Test
	public void testCreateContact() throws Exception {
		Contact contact = new Contact();
		int addressBookId = 1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.hasRightsOnAddressBook(token, addressBookId)).andReturn(true);
		expect(contactDao.createContactInAddressBook(token, contact, addressBookId)).andReturn(contact).once();
		expect(commitedOperationDao.findAsContact(token, null)).andReturn(null).once();
		mocksControl.replay();

		Contact createdContact = binding.createContact(token, addressBookId, contact, null);
		
		assertThat(createdContact).isNotNull();
	}

	@Test(expected = NoPermissionException.class)
	public void testCreateContactWithoutPermission() throws Exception {
		Contact contact = new Contact();
		int addressBookId = 1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.hasRightsOnAddressBook(token, addressBookId)).andReturn(false);
		mocksControl.replay();

		binding.createContact(token, addressBookId, contact, null);
	}

	@Test(expected = NoPermissionException.class)
	public void testCreateContactInAddressBookOfOBMUsers() throws Exception {
		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		mocksControl.replay();
		
		binding.createContact(token, USERS_ADDRESS_BOOK_ID, new Contact(), null);
	}

	@Test
	public void testModifyContact() throws Exception {
		int addressBookId = 1;
		Contact oldContact = new Contact(), newContact = new Contact();
		
		oldContact.setUid(1);
		oldContact.setFolderId(addressBookId);

		newContact.setUid(1);

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.findContact(token, oldContact.getUid())).andReturn(oldContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, oldContact.getFolderId())).andReturn(true).once();
		contactMerger.merge(oldContact, newContact);
		expectLastCall().once();
		expect(contactDao.modifyContact(token, newContact)).andReturn(newContact).once();
		mocksControl.replay();

		Contact modifiedContact = binding.modifyContact(token, addressBookId, newContact);
		
		assertThat(modifiedContact).isEqualTo(newContact);
	}

	@Test(expected = NoPermissionException.class)
	public void testModifyContactWithoutPermission() throws Exception {
		int addressBookId = 1;
		Contact oldContact = new Contact(), newContact = new Contact();
		
		oldContact.setUid(1);
		oldContact.setFolderId(addressBookId);

		newContact.setUid(1);

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.findContact(token, oldContact.getUid())).andReturn(oldContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, oldContact.getFolderId())).andReturn(false).once();
		mocksControl.replay();

		binding.modifyContact(token, addressBookId, newContact);
	}

	@Test(expected = NoPermissionException.class)
	public void testModifyContactInAddressBookOfOBMUsers() throws Exception {
		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		mocksControl.replay();
		
		binding.modifyContact(token, USERS_ADDRESS_BOOK_ID, new Contact());
	}

	@Test
	public void testUpdateContact() throws Exception {
		int addressBookId = 1;
		int entityId = 2;
		Contact oldContact = new Contact(), newContact = new Contact();
		
		oldContact.setUid(1);
		oldContact.setFolderId(addressBookId);
		oldContact.setEntityId(EntityId.valueOf(entityId));

		newContact.setUid(1);

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.findContact(token, oldContact.getUid())).andReturn(oldContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, oldContact.getFolderId())).andReturn(true).once();
		expect(contactDao.updateContact(token, oldContact)).andReturn(newContact).once();
		mocksControl.replay();

		Contact updatedContact = binding.updateContact(token, addressBookId, newContact);
		
		assertThat(updatedContact).isEqualTo(newContact);
		assertThat(newContact.getFolderId()).isEqualTo(1);
		assertThat(newContact.getEntityId().getId()).isEqualTo(2);
	}

	@Test(expected = NoPermissionException.class)
	public void testUpdateContactWithoutPermission() throws Exception {
		int addressBookId = 1;
		Contact oldContact = new Contact(), newContact = new Contact();
		
		oldContact.setUid(1);
		oldContact.setFolderId(addressBookId);

		newContact.setUid(1);

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.findContact(token, oldContact.getUid())).andReturn(oldContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, oldContact.getFolderId())).andReturn(false).once();
		mocksControl.replay();

		binding.updateContact(token, addressBookId, newContact);
	}

	@Test(expected = NoPermissionException.class)
	public void testUpdateContactInAddressBookOfOBMUsers() throws Exception {
		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		mocksControl.replay();
		
		binding.updateContact(token, USERS_ADDRESS_BOOK_ID, new Contact());
	}

	
	@Test
	public void testStoreContactCreation() throws Exception {
		Contact contact = new Contact();
		int addressBookId = 1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.hasRightsOnAddressBook(token, addressBookId)).andReturn(true);
		expect(contactDao.createContactInAddressBook(token, contact, addressBookId)).andReturn(contact).once();
		expect(commitedOperationDao.findAsContact(token, null)).andReturn(null).once();
		mocksControl.replay();
		Contact createdContact = binding.storeContact(token, addressBookId, contact, null);
		
		assertThat(createdContact).isNotNull();
	}
	
	@Test
	public void testStoreContactUpdate() throws Exception {
		int addressBookId = 1;
		Contact oldContact = new Contact(), newContact = new Contact();
		
		Map<String, EmailAddress> emails = ImmutableMap.of("work", EmailAddress.loginAtDomain("equeffelec@linagora.com"));
		
		oldContact.setUid(1);
		oldContact.setFolderId(addressBookId);
		oldContact.setFirstname("Erwan");
		oldContact.setEmails(emails);

		newContact.setUid(1);
		newContact.setFirstname("Michaël");
		newContact.setLastname("Bailly");

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.findContact(token, oldContact.getUid())).andReturn(oldContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, oldContact.getFolderId())).andReturn(true).once();
		expect(contactDao.updateContact(token, newContact)).andReturn(newContact).once();
		mocksControl.replay();

		Contact modifiedContact = binding.storeContact(token, addressBookId, newContact, null);
		
		assertThat(modifiedContact).isEqualTo(newContact);
		assertThat(modifiedContact.getFirstname()).isEqualTo("Michaël");
		assertThat(modifiedContact.getLastname()).isEqualTo("Bailly");
		assertThat(modifiedContact.getEmails()).isEmpty();
	}

	@Test
	public void testRemoveContact() throws Exception {
		int addressBookId = 1;
		Contact oldContact = new Contact(), newContact = new Contact();
		
		oldContact.setUid(1);
		oldContact.setFolderId(addressBookId);

		newContact.setUid(1);

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.findContact(token, oldContact.getUid())).andReturn(oldContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, oldContact.getFolderId())).andReturn(true).once();
		expect(contactDao.removeContact(token, oldContact)).andReturn(newContact).once();
		mocksControl.replay();

		Contact removedContact = binding.removeContact(token, addressBookId, newContact.getUid());
		
		assertThat(removedContact).isEqualTo(newContact);
	}

	@Test(expected = NoPermissionException.class)
	public void testRemoveContactWithoutPermission() throws Exception {
		int addressBookId = 1;
		Contact oldContact = new Contact(), newContact = new Contact();
		
		oldContact.setUid(1);
		oldContact.setFolderId(addressBookId);

		newContact.setUid(1);

		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		expect(contactDao.findContact(token, oldContact.getUid())).andReturn(oldContact).once();
		expect(contactDao.hasRightsOnAddressBook(token, oldContact.getFolderId())).andReturn(false).once();
		mocksControl.replay();

		binding.removeContact(token, addressBookId, newContact.getUid());
	}

	@Test(expected = NoPermissionException.class)
	public void testRemoveContactInAddressBookOfOBMUsers() throws Exception {
		expect(contactConfiguration.getAddressBookUserId()).andReturn(USERS_ADDRESS_BOOK_ID).once();
		mocksControl.replay();
		
		binding.removeContact(token, USERS_ADDRESS_BOOK_ID, 1);
	}
	
	private void mockGetLastSyncSubtractByTransactionTimeout(Date lastSync, int transactionTimeoutInSeconds)
			throws SQLException {
		expect(helper.getConnection()).andReturn(null).anyTimes();
		expect(helper.selectNow(null)).andReturn(lastSync).anyTimes();
		expect(configuration.transactionTimeoutInSeconds()).andReturn(transactionTimeoutInSeconds).anyTimes();
		helper.cleanup(null, null, null);
		expectLastCall().anyTimes();
	}

	private void mockSimpleGetContactsChanges(Date lastSync, int addressBookId)
			throws SQLException {
		Contact newContact = new Contact();
		newContact.setLastname("newContact");

		ContactUpdates contactUpdates = new ContactUpdates();
		contactUpdates.setContacts(ImmutableList.of(newContact));
		contactUpdates.setArchived(ImmutableSet.of(1, 2));

		expect(contactConfiguration.getAddressBookUserId()).andReturn(-1).once();
		expect(contactDao.findUpdatedContacts(lastSync, addressBookId, token)).andReturn(contactUpdates).once();
		expect(contactDao.findRemovalCandidates(lastSync, addressBookId, token)).andReturn(ImmutableSet.of(3)).once();
	}

	@Test
	public void testLastSyncOfListsContactsChanges()
			throws ServerFault, SQLException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		int transactionTimeoutInSeconds = 60;
		int addressBookId = 0;

		mockSimpleGetContactsChanges(lastSync, addressBookId);
		mockGetLastSyncSubtractByTransactionTimeout(lastSync, transactionTimeoutInSeconds);

		mocksControl.replay();
		ContactChanges contactChanges = binding.listContactsChanged(token, lastSync, addressBookId);
		mocksControl.verify();

		assertThat(contactChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:15:00"));
	}

	@Test
	public void testLastSyncOfListsContactsChangesWithAddressBookId()
			throws ServerFault, SQLException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		int transactionTimeoutInSeconds = 60;
		int addressBookId = 0;

		mockSimpleGetContactsChanges(lastSync, addressBookId);
		mockGetLastSyncSubtractByTransactionTimeout(lastSync, transactionTimeoutInSeconds);

		mocksControl.replay();
		ContactChanges contactChanges = binding.firstListContactsChanged(token, lastSync, addressBookId);
		mocksControl.verify();

		assertThat(contactChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:15:00"));
	}

	private void mockSimpleGetContactsChanges(Date lastSync)
			throws SQLException {
		Contact newContact = new Contact();
		newContact.setLastname("newContact");

		ContactUpdates contactUpdates = new ContactUpdates();
		contactUpdates.setContacts(ImmutableList.of(newContact));
		contactUpdates.setArchived(ImmutableSet.of(1, 2));

		expect(contactDao.findUpdatedContacts(lastSync, token)).andReturn(contactUpdates).once();
		expect(configuration.syncUsersAsAddressBook()).andReturn(false).once();
		expect(contactDao.findRemovalCandidates(lastSync, token)).andReturn(ImmutableSet.of(3)).once();
	}

	@Test
	public void testLastSyncOfListsContactsChangesWithoutAddressBookId()
			throws ServerFault, SQLException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		int transactionTimeoutInSeconds = 60;

		mockSimpleGetContactsChanges(lastSync);
		mockGetLastSyncSubtractByTransactionTimeout(lastSync, transactionTimeoutInSeconds);

		mocksControl.replay();
		ContactChanges contactChanges = binding.listContactsChanged(token, lastSync);
		mocksControl.verify();
		assertThat(contactChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:15:00"));
	}

	@Test
	public void testLastSyncOfFirstListContactsChangedWithoutAddressBookId()
			throws SQLException, ServerFault {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		int transactionTimeoutInSeconds = 60;

		mockSimpleGetContactsChanges(lastSync);
		mockGetLastSyncSubtractByTransactionTimeout(lastSync, transactionTimeoutInSeconds);

		mocksControl.replay();
		ContactChanges contactChanges = binding.firstListContactsChanged(token, lastSync);
		mocksControl.verify();
		assertThat(contactChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:15:00"));
	}

	private void mockListAddressBooksChanged(Date lastSync) throws SQLException {
		expect(contactDao.findUpdatedFolders(lastSync, token)).andReturn(null).once();
		expect(configuration.syncUsersAsAddressBook()).andReturn(false).once();
		expect(contactDao.findRemovedFolders(lastSync, token)).andReturn(null).once();
	}

	@Test
	public void testLastSyncOfGetAddressBookSync()
			throws ServerFault, SQLException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		int transactionTimeoutInSeconds = 60;

		mockSimpleGetContactsChanges(lastSync);
		mockListAddressBooksChanged(lastSync);
		mockGetLastSyncSubtractByTransactionTimeout(lastSync, transactionTimeoutInSeconds);

		mocksControl.replay();
		AddressBookChangesResponse contactChanges = binding.getAddressBookSync(token, lastSync);
		mocksControl.verify();

		assertThat(contactChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:15:00"));
	}

	@Test
	public void testLastSyncOfListAddressBooksChanged()
			throws ServerFault, SQLException {
		Date lastSync = DateUtils.date("2014-09-24T17:16:00");
		int transactionTimeoutInSeconds = 60;

		mockListAddressBooksChanged(lastSync);
		mockGetLastSyncSubtractByTransactionTimeout(lastSync, transactionTimeoutInSeconds);

		mocksControl.replay();
		FolderChanges contactChanges = binding.listAddressBooksChanged(token,lastSync);
		mocksControl.verify();

		assertThat(contactChanges.getLastSync()).isEqualTo(DateUtils.date("2014-09-24T17:15:00"));
	}

}
