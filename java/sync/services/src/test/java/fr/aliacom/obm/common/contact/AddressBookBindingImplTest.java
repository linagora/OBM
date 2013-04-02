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
package fr.aliacom.obm.common.contact;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NoPermissionException;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.utils.DateUtils;
import org.obm.sync.addition.CommitedElement;
import org.obm.sync.addition.Kind;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Folder;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.sync.exception.InvalidContactException;
import org.obm.sync.items.AddressBookChangesResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.addition.CommitedOperationDao;
import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import fr.aliacom.obm.utils.ObmHelper;

@RunWith(SlowFilterRunner.class)
public class AddressBookBindingImplTest {

	private static class Env extends AbstractModule {
		private IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(ContactDao.class);
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

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(Env.class);

	@Inject
	private IMocksControl mocksControl;
	@Inject
	private AddressBookBindingImpl binding;
	@Inject
	private ContactConfiguration contactConfiguration;
	@Inject
	private ContactMerger contactMerger;
	@Inject
	private ContactDao contactDao;
	@Inject
	private CommitedOperationDao commitedOperationDao;

	private static final int ADDRESS_BOOK_USER_ID = -1;

	private AccessToken token;

	@Before
	public void setUp() {
		ObmDomain domain = ObmDomain
							.builder()
							.id(123)
							.name("obm.org")
							.uuid("01324-56789")
							.build();
		
		token = new AccessToken(1, "");
		token.setUserLogin("login");
		token.setDomain(domain);
	}
	
	private ObmHelper mockHelper() throws SQLException {
		ObmHelper helper = createMock(ObmHelper.class);
		expect(helper.getConnection()).andReturn(null);
		helper.cleanup(null, null, null);
		expect(helper.selectNow(null)).andReturn(new Date());
		return helper;
	}

	/**
	 * Tests that getSync() returns the updated contacts and address books,
	 * including the full list of users from the user DAO, when
	 * AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC is set to true.
	 */
	@Test
	public void testGetSyncGlobalAddressBookSync() throws ServerFault, SQLException {
		Date timestamp = DateUtils.getEpochCalendar().getTime();

		Contact newContact = new Contact();
		newContact.setLastname("newContact");

		List<Contact> updatedContacts = ImmutableList.of(newContact);

		Set<Integer> archivedContactIds = ImmutableSet.of(1, 2);

		ContactUpdates contactUpdates = new ContactUpdates();
		contactUpdates.setContacts(updatedContacts);
		contactUpdates.setArchived(archivedContactIds);

		Set<Integer> removalCandidates = ImmutableSet.of(3);

		Contact newUser = new Contact();
		newUser.setLastname("obmuser");

		List<Contact> updatedUsers = ImmutableList.of(newUser);

		Set<Integer> archivedUserIds = ImmutableSet.of(5, 7, 8);

		ContactUpdates userUpdates = new ContactUpdates();
		userUpdates.setContacts(updatedUsers);
		userUpdates.setArchived(archivedUserIds);

		Set<Contact> allUpdatedContacts = new HashSet<Contact>();
		allUpdatedContacts.addAll(updatedContacts);
		allUpdatedContacts.addAll(updatedUsers);

		Folder updatedContactFolder1 = Folder.builder().uid(1).name("updatedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder updatedContactFolder2 = Folder.builder().uid(2).name("updatedContactFolder2").ownerLoginAtDomain("login@obm.org").build();

		Set<Folder> updatedContactFolders = new HashSet<Folder>();
		updatedContactFolders.add(updatedContactFolder1);
		updatedContactFolders.add(updatedContactFolder2);

		int defautUsersIdFolder = -1;
		String defaultUsersNameFolder = "users";
		Folder updatedUserFolder = Folder.builder().uid(defautUsersIdFolder).name(defaultUsersNameFolder).ownerLoginAtDomain("login@obm.org").build();

		Folder removedContactFolder1 = Folder.builder().uid(10).name("removedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder removedContactFolder2 = Folder.builder().uid(11).name("removedContactFolder2").ownerLoginAtDomain("login@obm.org").build();
		Set<Folder> removedContactFolders =  Sets.newHashSet(removedContactFolder1, removedContactFolder2);
		
		ObmHelper helper = mockHelper();

		ContactDao contactDao = createMock(ContactDao.class);
		expect(contactDao.findUpdatedContacts(timestamp, token)).andReturn(contactUpdates).once();
		expect(contactDao.findRemovalCandidates(timestamp, token)).andReturn(removalCandidates).once();
		
		expect(helper.getConnection()).andReturn(null);
		helper.cleanup(null, null, null);
		expect(helper.selectNow(null)).andReturn(new Date());
		
		expect(contactDao.findUpdatedFolders(timestamp, token)).andReturn(updatedContactFolders).once();
		expect(contactDao.findRemovedFolders(timestamp, token)).andReturn(removedContactFolders).once();

		expect(helper.getConnection()).andReturn(null);
		helper.cleanup(null, null, null);
		expect(helper.selectNow(null)).andReturn(new Date());
		
		UserDao userDao = createMock(UserDao.class);
		expect(userDao.findUpdatedUsers(timestamp, token)).andReturn(userUpdates).once();

		ObmSyncConfigurationService configuration = createMock(ObmSyncConfigurationService.class);
		expect(configuration.syncUsersAsAddressBook()).andReturn(true).atLeastOnce();

		ContactConfiguration contactConfiguration = createMock(ContactConfiguration.class);
		expect(contactConfiguration.getAddressBookUserId()).andReturn(-1);
		expect(contactConfiguration.getAddressBookUsersName()).andReturn("users");
		
		Object[] mocks = { helper, contactDao, userDao, configuration, contactConfiguration };
		replay(mocks);

		AddressBookBindingImpl binding = new AddressBookBindingImpl(contactDao, userDao, null, helper, 
				configuration, contactConfiguration, null);
		AddressBookChangesResponse changes = binding.getAddressBookSync(token, timestamp);

		verify(mocks);

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
	public void testGetSyncNoGlobalAddressBookSync() throws ServerFault, SQLException {
		Date timestamp = new Date();

		Contact newContact = new Contact();
		newContact.setLastname("newContact");

		List<Contact> updatedContacts = ImmutableList.of(newContact);

		Set<Integer> archivedContactIds = ImmutableSet.of(1, 2);

		ContactUpdates contactUpdates = new ContactUpdates();
		contactUpdates.setContacts(updatedContacts);
		contactUpdates.setArchived(archivedContactIds);

		Set<Integer> removalCandidates = ImmutableSet.of(3);

		Set<Contact> allUpdatedContacts = new HashSet<Contact>();
		allUpdatedContacts.addAll(updatedContacts);

		Folder updatedContactFolder1 = Folder.builder().uid(1).name("updatedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder updatedContactFolder2 = Folder.builder().uid(2).name("updatedContactFolder2").ownerLoginAtDomain("login@obm.org").build();
		Set<Folder> updatedContactFolders = Sets.newHashSet(updatedContactFolder1, updatedContactFolder2);

		Folder removedContactFolder1 = Folder.builder().uid(10).name("removedContactFolder1").ownerLoginAtDomain("login@obm.org").build();
		Folder removedContactFolder2 = Folder.builder().uid(11).name("removedContactFolder2").ownerLoginAtDomain("login@obm.org").build();
		Set<Folder> removedContactFolders =  Sets.newHashSet(removedContactFolder1, removedContactFolder2);
		
		removedContactFolders.add(removedContactFolder1);
		removedContactFolders.add(removedContactFolder2);

		ObmHelper helper = mockHelper();

		ContactDao contactDao = createMock(ContactDao.class);
		
		expect(contactDao.findUpdatedContacts(timestamp, token)).andReturn(contactUpdates).once();
		expect(contactDao.findRemovalCandidates(timestamp, token)).andReturn(removalCandidates).once();
		expect(helper.getConnection()).andReturn(null);
		helper.cleanup(null, null, null);
		expect(helper.selectNow(null)).andReturn(new Date());

		expect(contactDao.findUpdatedFolders(timestamp, token)).andReturn(updatedContactFolders).once();
		expect(contactDao.findRemovedFolders(timestamp, token)).andReturn(removedContactFolders).once();
		expect(helper.getConnection()).andReturn(null);
		helper.cleanup(null, null, null);
		expect(helper.selectNow(null)).andReturn(new Date());
		
		ObmSyncConfigurationService configuration = createMock(ObmSyncConfigurationService.class);
		expect(
				configuration.syncUsersAsAddressBook()).andReturn(false).atLeastOnce();

		Object[] mocks = { helper, contactDao, configuration };
		replay(mocks);

		AddressBookBindingImpl binding = new AddressBookBindingImpl(contactDao, null, null, helper,
				configuration, null, null);
		AddressBookChangesResponse changes = binding.getAddressBookSync(token, timestamp);

		verify(mocks);

		assertThat(changes.getContactChanges().getUpdated()).containsOnly(newContact);
		assertThat(changes.getRemovedContacts()).containsOnly(1, 2, 3);
		assertThat(changes.getUpdatedAddressBooks()).containsOnly(updatedContactFolder1, updatedContactFolder2);
		assertThat(changes.getRemovedAddressBooks()).containsOnly(removedContactFolder1, removedContactFolder2);
	}
	
	@Test
	public void testCreateContactWithCommitedOperation() throws Exception {
		Integer addressBookId = 1;
		Contact contact = new Contact();
		String clientId = "6547";

		IMocksControl control = createControl();
		
		ContactConfiguration contactConfiguration = control.createMock(ContactConfiguration.class);
		expect(contactConfiguration.getAddressBookUserId())
			.andReturn(2).once();
		
		Integer entityId = 984;
		Contact expectedContact = new Contact();
		expectedContact.setEntityId(entityId);
		
		ContactDao contactDao = control.createMock(ContactDao.class);
		expect(contactDao.createContactInAddressBook(token, contact, addressBookId))
			.andReturn(expectedContact).once();
		
		CommitedOperationDao commitedOperationDao = control.createMock(CommitedOperationDao.class);
		expect(commitedOperationDao.findAsContact(token, clientId))
			.andReturn(null).once();
		commitedOperationDao.store(token,
				CommitedElement.builder()
					.clientId(clientId)
					.entityId(entityId)
					.kind(Kind.VCONTACT)
					.build());
		expectLastCall().once();
		
		control.replay();
		
		AddressBookBindingImpl binding = new AddressBookBindingImpl(contactDao, null, null, null,
				null, contactConfiguration , commitedOperationDao);
		Contact createdContact = binding.createContact(token, addressBookId, contact, clientId);
		
		control.verify();
		assertThat(createdContact).isEqualTo(expectedContact);
	}
	
	@Test
	public void testCreateContactAlreadyCommited() throws Exception {
		Integer addressBookId = 1;
		Contact contact = new Contact();
		String clientId = "6547";

		IMocksControl control = createControl();
		
		ContactConfiguration contactConfiguration = control.createMock(ContactConfiguration.class);
		expect(contactConfiguration.getAddressBookUserId())
			.andReturn(2).once();
		
		CommitedOperationDao commitedOperationDao = control.createMock(CommitedOperationDao.class);
		expect(commitedOperationDao.findAsContact(token, clientId))
			.andReturn(contact).once();
		
		control.replay();
		
		AddressBookBindingImpl binding = new AddressBookBindingImpl(null, null, null, null,
				null, contactConfiguration , commitedOperationDao);
		Contact createdContact = binding.createContact(token, addressBookId, contact, clientId);
		
		control.verify();
		assertThat(createdContact).isEqualTo(contact);
	}
	
	@Test
	public void testCreateContactWhenNullClientId() throws Exception {
		Integer addressBookId = 1;
		Contact contact = new Contact();

		IMocksControl control = createControl();
		
		ContactConfiguration contactConfiguration = control.createMock(ContactConfiguration.class);
		expect(contactConfiguration.getAddressBookUserId())
			.andReturn(2).once();
		
		Integer entityId = 984;
		Contact expectedContact = new Contact();
		expectedContact.setEntityId(entityId);
		
		ContactDao contactDao = control.createMock(ContactDao.class);
		expect(contactDao.createContactInAddressBook(token, contact, addressBookId))
			.andReturn(expectedContact).once();
		
		CommitedOperationDao commitedOperationDao = control.createMock(CommitedOperationDao.class);
		expect(commitedOperationDao.findAsContact(token, null))
			.andReturn(null).once();
		
		control.replay();
		
		AddressBookBindingImpl binding = new AddressBookBindingImpl(contactDao, null, null, null,
				null, contactConfiguration , commitedOperationDao);
		Contact createdContact = binding.createContact(token, addressBookId, contact, null);
		
		control.verify();
		assertThat(createdContact).isEqualTo(expectedContact);
  }

	@Test
	public void testCreateContact()
			throws NoPermissionException, ServerFault, InvalidContactException,
			SQLException {
		Contact newContact = new Contact();
		int addressBookId = 1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(-1).once();
		expect(contactDao.createContactInAddressBook(token, newContact, addressBookId)).andReturn(newContact).once();
		expect(commitedOperationDao.findAsContact(token, "6547")).andReturn(null).once();

		verifyCreateContact(newContact, addressBookId, "6547");
	}

	@Test(expected = NoPermissionException.class)
	public void testCreateContactInAddressBookOfOBMUsers()
			throws ServerFault, NoPermissionException, InvalidContactException {
		Contact newContact = new Contact();
		int addressBookId = -1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(ADDRESS_BOOK_USER_ID).once();

		verifyCreateContact(newContact, addressBookId, "6547");
	}

	@Test(expected = InvalidContactException.class)
	public void testCreateContactWithInvalidEmail()
			throws ServerFault, NoPermissionException, InvalidContactException {
		Contact newContact = new Contact();
		int addressBookId = 1;

		newContact.addEmail("test", EmailAddress.loginAtDomain("test test @ foo.fr"));

		expect(contactConfiguration.getAddressBookUserId()).andReturn(ADDRESS_BOOK_USER_ID).once();

		verifyCreateContact(newContact, addressBookId, "6547");
	}

	private void verifyCreateContact(Contact newContact, int addressBookId, String clientId)
			throws ServerFault, NoPermissionException, InvalidContactException {
		mocksControl.replay();

		binding.createContact(token, addressBookId, newContact, clientId);

		mocksControl.verify();
	}

	@Test
	public void testModifyContact()
			throws NoPermissionException, ServerFault, InvalidContactException,
			SQLException, ContactNotFoundException, FindException, EventNotFoundException {
		Contact oldContact = new Contact();
		oldContact.setUid(1);
		Contact newContact = new Contact();
		newContact.setUid(1);
		int addressBookId = 1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(ADDRESS_BOOK_USER_ID).once();

		Integer oldContactUid = oldContact.getUid();
		expect(contactDao.findContact(token, oldContactUid)).andReturn(oldContact).once();
		expect(contactDao.hasRightsOn(token, oldContactUid)).andReturn(true).once();

		contactMerger.merge(oldContact, newContact);
		expectLastCall().once();

		expect(contactDao.modifyContact(token, newContact)).andReturn(newContact).once();

		verifyModifyContact(newContact, addressBookId);
	}

	@Test(expected = NoPermissionException.class)
	public void testModifyContactWithoutDaoPermission()
			throws ContactNotFoundException, SQLException, ServerFault,
			NoPermissionException, InvalidContactException {
		Contact oldContact = new Contact();
		oldContact.setUid(1);
		Contact newContact = new Contact();
		newContact.setUid(1);
		int addressBookId = 1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(ADDRESS_BOOK_USER_ID).once();

		Integer oldContactUid = oldContact.getUid();
		expect(contactDao.findContact(token, oldContactUid)).andReturn(oldContact).once();
		expect(contactDao.hasRightsOn(token, oldContactUid)).andReturn(false).once();

		verifyModifyContact(newContact, addressBookId);
	}

	@Test(expected = NoPermissionException.class)
	public void testModifyContactInAddressBookOfOBMUsers()
			throws ContactNotFoundException,ServerFault, NoPermissionException, InvalidContactException {
		Contact oldContact = new Contact();
		oldContact.setUid(1);
		Contact newContact = new Contact();
		newContact.setUid(1);
		int addressBookId = -1;

		expect(contactConfiguration.getAddressBookUserId()).andReturn(ADDRESS_BOOK_USER_ID).once();

		verifyModifyContact(newContact, addressBookId);
	}

	@Test(expected = InvalidContactException.class)
	public void testModifyContactWithInvalidEmail()
			throws ContactNotFoundException, ServerFault, NoPermissionException, InvalidContactException {
		Contact oldContact = new Contact();
		oldContact.setUid(1);
		Contact newContact = new Contact();
		newContact.setUid(1);
		int addressBookId = 1;

		newContact.addEmail("test", EmailAddress.loginAtDomain("test test @ foo.fr"));
		expect(contactConfiguration.getAddressBookUserId()).andReturn(ADDRESS_BOOK_USER_ID).once();

		verifyModifyContact(newContact, addressBookId);
	}

	private void verifyModifyContact(Contact newContact, int addressBookId)
			throws ServerFault, NoPermissionException,
			ContactNotFoundException, InvalidContactException {
		mocksControl.replay();

		binding.modifyContact(token, addressBookId, newContact);

		mocksControl.verify();
	}
}
