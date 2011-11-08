package fr.aliacom.obm.common.contact;

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.Assertions.assertThat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.ContactConfiguration;
import org.obm.push.utils.DateUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.items.AddressBookChangesResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.services.constant.ConstantService;
import fr.aliacom.obm.utils.ObmHelper;

public class AddressBookBindingImplTest {
	private ObmHelper mockHelper() throws SQLException {
		ObmHelper helper = EasyMock.createMock(ObmHelper.class);
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
		Date timestamp = DateUtils.getEpochPlusOneSecondCalendar().getTime();

		AccessToken token = new AccessToken(1, 1, "");

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

		Set<Integer> allRemovedContacts = new HashSet<Integer>();
		allRemovedContacts.addAll(archivedContactIds);
		allRemovedContacts.addAll(removalCandidates);
		allRemovedContacts.addAll(archivedUserIds);

		Folder updatedContactFolder1 = new Folder();
		updatedContactFolder1.setUid(1);
		updatedContactFolder1.setName("updatedContactFolder1");

		Folder updatedContactFolder2 = new Folder();
		updatedContactFolder2.setUid(2);
		updatedContactFolder2.setName("updatedContactFolder2");

		List<Folder> updatedContactFolders = new ArrayList<Folder>();
		updatedContactFolders.add(updatedContactFolder1);
		updatedContactFolders.add(updatedContactFolder2);

		Folder updatedUserFolder = new Folder();
		int defautUsersIdFolder = -1;
		String defaultUsersNameFolder = "users";
		updatedUserFolder.setUid(defautUsersIdFolder);
		updatedUserFolder.setName(defaultUsersNameFolder);

		List<Folder> updatedUserFolders = ImmutableList.of(updatedUserFolder);

		Set<Folder> allUpdatedFolders = new HashSet<Folder>();
		allUpdatedFolders.addAll(updatedContactFolders);
		allUpdatedFolders.addAll(updatedUserFolders);

		Set<Integer> removedContactFolders = ImmutableSet.of(10, 11);

		ObmHelper helper = mockHelper();

		ContactDao contactDao = EasyMock.createMock(ContactDao.class);
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
		
		UserDao userDao = EasyMock.createMock(UserDao.class);
		expect(userDao.findUpdatedUsers(timestamp, token)).andReturn(userUpdates).once();

		ConstantService configuration = EasyMock.createMock(ConstantService.class);
		expect(configuration.getBooleanValue(AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC,
						AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC_DEFAULT_VALUE)).andReturn(true).atLeastOnce();

		ContactConfiguration contactConfiguration = EasyMock.createMock(ContactConfiguration.class);
		expect(contactConfiguration.getAddressBookUserId()).andReturn(defautUsersIdFolder);
		expect(contactConfiguration.getAddressBookUsersName()).andReturn(defaultUsersNameFolder);
		
		Object[] mocks = { helper, contactDao, userDao, configuration, contactConfiguration };
		EasyMock.replay(mocks);

		AddressBookBindingImpl binding = new AddressBookBindingImpl(contactDao, userDao, null, helper, 
				configuration, contactConfiguration);
		AddressBookChangesResponse changes = binding.getAddressBookSync(token, timestamp);

		EasyMock.verify(mocks);

		assertThat(changes.getContactChanges().getUpdated()).containsOnly(allUpdatedContacts.toArray());
		assertThat(changes.getRemovedContacts()).containsOnly(allRemovedContacts.toArray());
		assertThat(changes.getUpdatedAddressBooks()).containsOnly(allUpdatedFolders.toArray());
		assertThat(changes.getRemovedAddressBooks()).containsOnly(removedContactFolders.toArray());
	}

	/**
	 * Tests that getSync() returns the updated contacts and address book, but
	 * not the full list of users from the user DAO, when
	 * AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC is set to false.
	 */
	@Test
	public void testGetSyncNoGlobalAddressBookSync() throws ServerFault, SQLException {
		Date timestamp = new Date();

		AccessToken token = new AccessToken(1, 1, "");

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

		Set<Integer> allRemovedContacts = new HashSet<Integer>();
		allRemovedContacts.addAll(archivedContactIds);
		allRemovedContacts.addAll(removalCandidates);

		Folder updatedContactFolder1 = new Folder();
		updatedContactFolder1.setUid(1);
		updatedContactFolder1.setName("updatedContactFolder1");

		Folder updatedContactFolder2 = new Folder();
		updatedContactFolder2.setUid(2);
		updatedContactFolder2.setName("updatedContactFolder2");

		List<Folder> updatedContactFolders = new ArrayList<Folder>();
		updatedContactFolders.add(updatedContactFolder1);
		updatedContactFolders.add(updatedContactFolder2);

		Set<Folder> allUpdatedFolders = new HashSet<Folder>(updatedContactFolders);

		Set<Integer> removedContactFolders = ImmutableSet.of(10, 11);

		ObmHelper helper = mockHelper();

		ContactDao contactDao = EasyMock.createMock(ContactDao.class);
		
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
		
		ConstantService configuration = EasyMock.createMock(ConstantService.class);
		expect(
				configuration.getBooleanValue(AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC,
						AddressBookBindingImpl.GLOBAL_ADDRESS_BOOK_SYNC_DEFAULT_VALUE)).andReturn(
				false).atLeastOnce();

		Object[] mocks = { helper, contactDao, configuration };
		EasyMock.replay(mocks);

		AddressBookBindingImpl binding = new AddressBookBindingImpl(contactDao, null, null, helper,
				configuration, null);
		AddressBookChangesResponse changes = binding.getAddressBookSync(token, timestamp);

		EasyMock.verify(mocks);

		assertThat(changes.getContactChanges().getUpdated()).containsOnly(allUpdatedContacts.toArray());
		assertThat(changes.getRemovedContacts()).containsOnly(allRemovedContacts.toArray());
		assertThat(changes.getUpdatedAddressBooks()).containsOnly(allUpdatedFolders.toArray());
		assertThat(changes.getRemovedAddressBooks()).containsOnly(removedContactFolders.toArray());
	}
}