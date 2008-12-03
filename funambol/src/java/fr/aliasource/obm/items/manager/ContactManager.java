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
import org.obm.sync.book.Address;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.book.Website;
import org.obm.sync.client.ISyncClient;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.locators.AddressBookLocator;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.contact.BusinessDetail;
import com.funambol.common.pim.contact.PersonalDetail;
import com.funambol.common.pim.contact.Phone;
import com.funambol.common.pim.contact.WebPage;

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
		binding = addressbookLocator.locate(obmAddress.replace("/AddressBook",
				""));
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

		com.funambol.common.pim.contact.Contact ret = obmContactTofoundation(contact);

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

		BusinessDetail bd = contact.getBusinessDetail();

		for (String label : obmcontact.getEmails().keySet()) {
			Email e = obmcontact.getEmails().get(label);
			com.funambol.common.pim.contact.Email funisMail = new com.funambol.common.pim.contact.Email(
					e.getEmail());
			funisMail.setEmailType(label);
			bd.addEmail(funisMail);
		}

		obmToFunis(bd.getAddress(), "work", obmcontact.getAddresses().get(
				"work"));

		for (String label : obmcontact.getPhones().keySet()) {
			Phone p = new Phone(obmcontact.getPhones().get(label).getNumber());
			p.setPhoneType(label);
			bd.addPhone(p);
		}

		for (String label : obmcontact.getWebsites().keySet()) {
			Website ws = obmcontact.getWebsites().get(label);
			WebPage wp = new WebPage(ws.getUrl());
			wp.setWebPageType(label);
			bd.addWebPage(wp);
		}

		PersonalDetail pd = contact.getPersonalDetail();
		obmToFunis(pd.getAddress(), "home", obmcontact.getAddresses().get(
				"home"));
		obmToFunis(pd.getOtherAddress(), "other", obmcontact.getAddresses()
				.get("other"));

		ContactHelper.setFoundationNote(contact, obmcontact.getComment(),
				ContactHelper.COMMENT);

		contact.setSensitivity(new Short((short) 2)); // olPrivate

		return contact;
	}

	private void obmToFunis(com.funambol.common.pim.contact.Address target,
			String label, Address source) {
		if (target == null) {
			logger.warn("target addr is null");
			return;
		}
		if (source != null) {
			target.getStreet().setValue(source.getStreet());
			target.getCity().setValue(source.getTown());
			target.getCountry().setValue(source.getCountry());
			target.getLabel().setValue(label);
		}
	}

	private String s(Property p) {
		return p.getPropertyValueAsString();
	}

	private Address funisToObm(com.funambol.common.pim.contact.Address funis) {
		org.obm.sync.book.Address obm = new org.obm.sync.book.Address(s(funis
				.getStreet()), s(funis.getPostalCode()), s(funis
				.getPostOfficeAddress()), s(funis.getCity()), s(funis
				.getCountry()));
		return obm;
	}

	@SuppressWarnings("unchecked")
	private Contact foundationContactToObm(
			com.funambol.common.pim.contact.Contact funis, String type) {

		Contact contact = new Contact();

		// if (funis.getUid() != null && funis.getUid() != "") {
		// contact.setUid(new Integer(funis.getUid()));
		// }

		contact.setFirstname(ContactHelper.nullToEmptyString(funis.getName()
				.getFirstName().getPropertyValueAsString()));
		contact.setLastname(ContactHelper.getLastName(funis));

		contact.setAka(ContactHelper.nullToEmptyString(s(funis.getName()
				.getNickname())));

		BusinessDetail bd = funis.getBusinessDetail();
		PersonalDetail pd = funis.getPersonalDetail();

		// addresses
		if (bd.getAddress() != null) {
			contact.addAddress("work", funisToObm(bd.getAddress()));
		}

		if (pd.getAddress() != null) {
			contact.addAddress("home", funisToObm(pd.getAddress()));
		}
		if (pd.getOtherAddress() != null) {
			contact.addAddress("other", funisToObm(pd.getOtherAddress()));
		}

		// phones
		List<Phone> lph = new LinkedList<Phone>();
		lph.addAll(bd.getPhones());
		lph.addAll(pd.getPhones());
		for (Phone p : lph) {
			if (p != null && s(p) != null && s(p).length() > 0)
				contact.addPhone(p.getPhoneType(), new org.obm.sync.book.Phone(
						s(p)));
		}

		// emails
		List<com.funambol.common.pim.contact.Email> lem = new LinkedList<com.funambol.common.pim.contact.Email>();
		lem.addAll(bd.getEmails());
		lem.addAll(pd.getEmails());
		for (com.funambol.common.pim.contact.Email em : lem) {
			if (em != null && s(em) != null && s(em).length() > 0) {
				contact.addEmail(em.getEmailType(), new Email(s(em)));
			}
		}

		// websites
		List<WebPage> lwp = new LinkedList<WebPage>();
		lwp.addAll(bd.getWebPages());
		lwp.addAll(pd.getWebPages());
		for (WebPage wp : lwp) {
			if (wp != null && s(wp) != null && s(wp).length() > 0) {
				contact.addWebsite(wp.getWebPageType(), new Website(s(wp)));
			}
		}

		contact.setComment(ContactHelper.nullToEmptyString(ContactHelper
				.getNote(funis.getNotes(), ContactHelper.COMMENT)));

		return contact;
	}

	@Override
	protected ISyncClient getSyncClient() {
		return binding;
	}

}
