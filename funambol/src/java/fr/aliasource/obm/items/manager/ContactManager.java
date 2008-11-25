package fr.aliasource.obm.items.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.locators.AddressBookLocator;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.ContactHelper;

public class ContactManager extends ObmManager {

	protected Map<String, Contact> updatedRest = null;
	protected List<String> deletedRest = null;

	private BookClient binding;
	private BookType book;

	private Log logger = LogFactory.getLog(getClass());

	public ContactManager(String obmAddress) {

		AddressBookLocator addressbookLocator = new AddressBookLocator();
		binding = addressbookLocator.locate(obmAddress);
	}

	public void initRestriction(int restrictions) {
		this.restrictions = restrictions;
		if (logger.isDebugEnabled()) {
			logger.debug(" init restrictions: " + restrictions);
		}
	}

	public void logIn(String user, String pass) throws OBMException {
		token = binding.login(user, pass);
		if (token == null) {
			throw new OBMException("OBM Login refused for user : " + user);
		}
	}

	public List<String> getAllItemKeys() throws OBMException {

		if (!syncReceived) {
			getSync(null);
		}

		List<String> keys = new LinkedList<String>();
		keys.addAll(updatedRest.keySet());

		return keys;
	}

	public BookType getBook() {
		return book;
	}

	public void setBook(BookType book) {
		this.book = book;
	}

	public BookClient getBinding() {
		return binding;
	}

	public String[] getNewItemKeys(Timestamp since) throws OBMException {

		Calendar d = Calendar.getInstance();
		d.setTime(since);
		String[] keys = null;

		return keys;
	}

	public List<String> getDeletedItemKeys(Timestamp since) throws OBMException {
		Calendar d = Calendar.getInstance();
		d.setTime(since);
		if (!syncReceived) {
			getSync(since);
		}
		return deletedRest;
	}

	public List<String> getUpdatedItemKeys(Timestamp since) throws OBMException {

		Calendar d = Calendar.getInstance();
		d.setTime(since);

		if (!syncReceived) {
			getSync(since);
		}

		List<String> keys = new LinkedList<String>();
		keys.addAll(updatedRest.keySet());

		return keys;
	}

	public com.funambol.common.pim.contact.Contact getItemFromId(String key,
			String type) throws OBMException {

		Contact contact = null;

		contact = (Contact) updatedRest.get(key);

		if (contact == null) {
			logger.info(" item " + key
					+ " not found in updated -> get from sever");
			try {
				contact = binding.getContactFromId(token, book, key);
			} catch (AuthFault e) {
				throw new OBMException(e.getMessage());
			} catch (ServerFault e) {
				throw new OBMException(e.getMessage());
			}
		}

		com.funambol.common.pim.contact.Contact ret = obmContactTofoundation(
				contact);

		return ret;
	}

	public void removeItem(String key) throws OBMException {

		try {
			binding.removeContact(token, book, key);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		}
	}

	public com.funambol.common.pim.contact.Contact updateItem(String key,
			com.funambol.common.pim.contact.Contact contact, String type)
			throws OBMException {

		Contact c = null;
		try {
			c = binding.modifyContact(token, book, foundationContactToObm(
					contact, type));
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		}

		return obmContactTofoundation(c);
	}

	public com.funambol.common.pim.contact.Contact addItem(
			com.funambol.common.pim.contact.Contact contact, String type)
			throws OBMException {

		Contact c = null;

		try {
			c = binding.createContactWithoutDuplicate(token, book,
					foundationContactToObm(contact, type));
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		}

		return obmContactTofoundation(c);
	}

	public List<String> getContactTwinKeys(
			com.funambol.common.pim.contact.Contact contact, String type)
			throws OBMException {

		Contact c = foundationContactToObm(contact, type);

		if (logger.isDebugEnabled()) {
			logger.debug(" look twin of : " + c.getFirstname() + ","
					+ c.getLastname() + "," + c.getCompany());
		}

		try {
			c.setUid(null);
			return binding.getContactTwinKeys(token, book, c).getKeys();
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		}
	}

	// ---------------- Private methods ----------------------------------

	private void getSync(Timestamp since) throws OBMException {
		Date d = null;
		if (since != null) {
			d = new Date(since.getTime());
		}

		ContactChanges sync = null;
		// get modified items
		try {
			sync = binding.getSync(token, book, d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		}

		List<Contact> updated = new LinkedList<Contact>();
		if (sync.getUpdated() != null) {
			updated = sync.getUpdated();
		}
		Set<Integer> deleted = new HashSet<Integer>();
		if (sync.getRemoved() != null) {
			deleted = sync.getRemoved();
		}
		// apply restriction(s)
		updatedRest = new HashMap<String, Contact>();
		deletedRest = new ArrayList<String>();
		// String owner = "";
		// String user = token.getUser();
		for (Contact c : updated) {
			updatedRest.put("" + c.getUid(), c);
		}

		for (Integer i : deleted) {
			deletedRest.add(i.toString());
		}

		syncReceived = true;
	}

	private com.funambol.common.pim.contact.Contact obmContactTofoundation(
			Contact obmcontact) {
		com.funambol.common.pim.contact.Contact contact = new com.funambol.common.pim.contact.Contact();

		contact.setUid("" + obmcontact.getUid());

		contact.getName().getFirstName().setPropertyValue(
				obmcontact.getFirstname());
		contact.getName().getLastName().setPropertyValue(
				obmcontact.getLastname());
		contact.getName().getDisplayName().setPropertyValue(
				ContactHelper.constructDisplayName(obmcontact.getFirstname(),
						obmcontact.getLastname()));
		contact.getName().getNickname().setPropertyValue(obmcontact.getAka());

//		BusinessDetail bd = contact.getBusinessDetail();
//		PersonalDetail pd = contact.getPersonalDetail();
		// FIXME email, address, phones
		
		ContactHelper.setFoundationNote(contact, obmcontact.getComment(),
				ContactHelper.COMMENT);

		contact.setSensitivity(new Short((short) 2)); // olPrivate

		return contact;
	}

//	private org.obm.sync.book.Address updateAddress(Address funis, String type) {
//		org.obm.sync.book.Address obm = new org.obm.sync.book.Address(s(funis
//				.getStreet()), s(funis.getPostalCode()), s(funis
//				.getPostOfficeAddress()), s(funis.getCity()), s(funis
//				.getCountry()));
//		return obm;
//	}

//	private String s(Property p) {
//		return p.getPropertyValueAsString();
//	}
//
	private Contact foundationContactToObm(
			com.funambol.common.pim.contact.Contact funis, String type) {

		Contact contact = new Contact();

		if (funis.getUid() != null && funis.getUid() != "") {
			contact.setUid(new Integer(funis.getUid()));
		}

		contact.setFirstname(ContactHelper.nullToEmptyString(funis.getName()
				.getFirstName().getPropertyValueAsString()));
		contact.setLastname(ContactHelper.getLastName(funis));

		if (ContactHelper.nullToEmptyString(
				funis.getName().getNickname().getPropertyValueAsString())
				.equalsIgnoreCase("")) {
			contact.setAka(null);
		} else {
			contact.setAka(ContactHelper.nullToEmptyString(funis.getName()
					.getNickname().getPropertyValueAsString()));
		}

//		BusinessDetail bus = funis.getBusinessDetail();

		// TODO phones, email, contact
		
		// comment
//		contact.setComment(ContactHelper.nullToEmptyString(ContactHelper
//				.getNote(funis.getNotes(), ContactHelper.COMMENT)));

		return contact;
	}
	
	public void logout() {
		binding.logout(token);
	}


}
