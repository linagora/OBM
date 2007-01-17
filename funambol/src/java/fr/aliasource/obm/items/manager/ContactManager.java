package fr.aliasource.obm.items.manager;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import com.funambol.foundation.pdi.contact.Address;
import com.funambol.foundation.pdi.contact.BusinessDetail;
import com.funambol.framework.logging.Sync4jLogger;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.engine.source.ObmSyncSource;
import fr.aliasource.funambol.utils.ContactHelper;
import fr.aliasource.funambol.utils.Helper;
import fr.aliasource.obm.AddressBookLocator;
import fr.aliasource.obm.ebook.AddressBookBindingStub;
import fr.aliasource.obm.fault.AuthFault;
import fr.aliasource.obm.fault.ServerFault;
import fr.aliasource.obm.wcontact.BookSync;
import fr.aliasource.obm.wcontact.Contact;

public class ContactManager extends ObmManager {

	private AddressBookBindingStub binding;
	private String book;
	private Logger log = null;
	
	
	public ContactManager() {
		
		log = Sync4jLogger.getLogger("server");
		
		AddressBookBindingStub addressBinding = null;
		try {
			AddressBookLocator addressbookLocator = new AddressBookLocator();
			addressBinding = (AddressBookBindingStub)addressbookLocator.getAddressBook();
			
		} catch (ServiceException e) {
			log.info(e.getMessage());
		}
		binding = addressBinding;
	}

	
	public void initRestriction(int restrictions) {
		this.restrictions = restrictions;
		log.info(" init restrictions: "+restrictions);
	}
	
	public void logIn(String user, String pass) throws OBMException {
		token = null;
		try {
			token = binding.logUserIn(user, pass);
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		if (token == null) {
			throw new OBMException("OBM Login refused for user : "+user);
		}
	}

	public String[] getAllItemKeys() throws OBMException {

		String[] keys = null;
		
		if (!syncReceived) {
			getSync(null);
		}
		
		keys = extractKeys(updatedRest);
		
		return keys;
		
		/*
		try {
			keys = binding.getAllKeys(token, book);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		return keys;*/
	}	
	
	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public AddressBookBindingStub getBinding() {
		return binding;
	}


	public String[] getNewItemKeys(Timestamp since) throws OBMException {

		Calendar d = Calendar.getInstance();
		d.setTime(since);
		String[] keys = null;
		/*
		try {
			keys = binding.getNewKeys(token,book,d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}*/
		
		return keys;
	}

	public String[] getDeletedItemKeys(Timestamp since) throws OBMException {

		Calendar d = Calendar.getInstance();
		d.setTime(since);
		
		String[] keys = null;
		
		if (!syncReceived) {
			getSync(since);
		}
		
		keys = Helper.listToTab(deletedRest);
		
		return keys;
		/*
		try {
			keys = binding.getDeletedKeys(token,book,d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		return keys;*/
	}

	public String[] getUpdatedItemKeys(Timestamp since) throws OBMException {
		
		Calendar d = Calendar.getInstance();
		d.setTime(since);
		
		String[] keys = null;
		
		if (!syncReceived) {
			getSync(since);
		}
		
		keys = extractKeys(updatedRest);
		
		return keys;
		/*
		try {
			keys = binding.getUpdatedKeys(token,book,d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		return keys;*/
	}

	public com.funambol.foundation.pdi.contact.Contact getItemFromId(String key, String type) 
		throws OBMException {
		
		Contact contact = null;
		
		contact = (Contact) updatedRest.get(key);
		
		if (contact == null) {
			log.info(" item "+key+" not found in updated -> get from sever");
			try {
				contact = binding.getContactFromId(token, book, key);
			} catch (AuthFault e) {
				throw new OBMException(e.getMessage());
			} catch (ServerFault e) {
				throw new OBMException(e.getMessage());
			} catch (RemoteException e) {
				throw new OBMException(e.getMessage());
			}
		}
		
		com.funambol.foundation.pdi.contact.Contact ret = obmContactTofoundation(contact, type);
		
		return ret;
	}

	public void removeItem(String key) throws OBMException {
		
		try {
			binding.removeContact( token,book,key );
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
	}

	public com.funambol.foundation.pdi.contact.Contact updateItem(String key,
			com.funambol.foundation.pdi.contact.Contact contact, String type)
				throws OBMException {
		
		Contact c = null;
		try {
			c = binding.modifyContact(token,book,foundationContactToObm(contact, type));
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
	
		return obmContactTofoundation(c, type);
	}

	public com.funambol.foundation.pdi.contact.Contact addItem(
			com.funambol.foundation.pdi.contact.Contact contact, String type) 
				throws OBMException {
		
		Contact c = null;
		
		try {
			c = binding.createContact(token,book,foundationContactToObm(contact, type));
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		return obmContactTofoundation(c, type);
	}
	
	public String[] getContactTwinKeys(com.funambol.foundation.pdi.contact.Contact contact, String type) 
		throws OBMException {
		
		String[] keys = null;
		
		Contact c = foundationContactToObm(contact, type);
		
		log.info(" look twin of : "+c.getFirstName()+","+c.getLastName()+","+c.getCompany());
		
		try {
			keys = binding.getContactTwinKeys(token,book,c);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		return keys;
	}
	
	
	//---------------- Private methods ----------------------------------
	
	private void getSync(Timestamp since) throws OBMException {
		Calendar d = null;
		if (since != null) {
			d = Calendar.getInstance();
			d.setTime(since);
		}
		
		BookSync sync = null;
		//get modified items
		try {
			 sync = binding.getSync(token, book, d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		Contact[] updated = new Contact[0];
		if (sync.getUpdated() != null) updated = sync.getUpdated();
		int[] deleted = new int[0];
		if (sync.getRemoved() != null) deleted = sync.getRemoved(); 
		
		//apply restriction(s)
		updatedRest = new HashMap();
		deletedRest = new ArrayList();
		String owner = "";
		String user = token.getUser();
		for (int i=0 ; i < updated.length ; i++) {
			owner = Helper.nullToEmptyString(updated[i].getOwner());
			if ( ( ((restrictions & Helper.RESTRICT_PRIVATE) == Helper.RESTRICT_PRIVATE)
				    && (updated[i].getClassification() == 1 && !owner.equals(user)) )
			  || ( ((restrictions & Helper.RESTRICT_OWNER) == Helper.RESTRICT_OWNER)
					&& (!owner.equals(user)) ) )
			{
				if (d != null) {
					deletedRest.add(  (""+updated[i].getUid()) );
				}	
			} else {
				updatedRest.put( ""+updated[i].getUid(), updated[i] );
			}
		}
		
		for (int j=0 ; j < deleted.length ; j++) {
			deletedRest.add( (String) (""+deleted[j]) );
		}
		
		syncReceived = true;
	}
	
	
	private com.funambol.foundation.pdi.contact.Contact obmContactTofoundation(Contact obmcontact, String type) {
    	com.funambol.foundation.pdi.contact.Contact contact = new com.funambol.foundation.pdi.contact.Contact();
    	
    	contact.setUid(""+obmcontact.getUid());
    	
    	contact.getName().getFirstName().setPropertyValue(obmcontact.getFirstName());
    	contact.getName().getLastName().setPropertyValue(obmcontact.getLastName());
    	contact.getName().getDisplayName().setPropertyValue(obmcontact.getFirstName()+","+obmcontact.getLastName());
    	
    	
    	BusinessDetail bus = contact.getBusinessDetail();
    	/*bus.addEmail(
    			ContactHelper.getFoundationEmail(
    					obmcontact.getEmail(),ContactHelper.WORK_EMAIL) );*/
    	ContactHelper.setFoundationPhone(
    			bus, obmcontact.getWorkPhone(), ContactHelper.WORK_PHONE );
    	ContactHelper.setFoundationPhone(
    			bus, obmcontact.getWorkFax(), ContactHelper.WORK_FAX );
    	ContactHelper.setFoundationTitle(
    			bus, obmcontact.getTitle(), ContactHelper.WORK_TITLE );
    	bus.getCompany().setPropertyValue(obmcontact.getCompany());
    	
    	
    	Address addr = bus.getAddress();
    	addr.getCity().setPropertyValue(obmcontact.getTown());
    	addr.getCountry().setPropertyValue(obmcontact.getCountry());
    	addr.getStreet().setPropertyValue( ContactHelper.getStreetFromObm(obmcontact) );
    	addr.getPostalCode().setPropertyValue(obmcontact.getZipCode());
    	addr.getPostOfficeAddress().setPropertyValue(obmcontact.getExpressPostal());
    	
    	
    	ContactHelper.setFoundationEmail(
    			contact.getPersonalDetail(), obmcontact.getEmail(), ContactHelper.WORK_EMAIL );
    	
    	if (type.equals(ObmSyncSource.MSG_TYPE_VCARD)) {
    		ContactHelper.setFoundationPhone(
    				contact.getPersonalDetail(),
    	   			obmcontact.getHomePhone(), ContactHelper.OTHER_PHONE );
    	} else {
    		ContactHelper.setFoundationPhone(
    				contact.getPersonalDetail(),
    	   			obmcontact.getHomePhone(), ContactHelper.HOME_PHONE );
    	}
    	ContactHelper.setFoundationPhone(
    			contact.getPersonalDetail(),
    			obmcontact.getMobilePhone(), ContactHelper.HOME_MOBILE );
    	
    	ContactHelper.setFoundationNote(
    			contact,
    			obmcontact.getComment(), ContactHelper.COMMENT );
    	
    	
    	//Classification  	
    	if (obmcontact.getClassification() == 1 ) {
    		contact.setSensitivity(new Short((short)2) ); //olPrivate
    	} else {
    		contact.setSensitivity(new Short((short)0) ); //olNormal
    	}
    	
    	
    	return contact;
    }
    
    private Contact foundationContactToObm(com.funambol.foundation.pdi.contact.Contact foundation, String type) {

    	Contact contact = new Contact();
    	
    	if (foundation.getUid() != null && foundation.getUid() != "") {
    		contact.setUid( new Integer(foundation.getUid()).intValue());
    	}
    	
    	contact.setFirstName(
    			ContactHelper.nullToEmptyString(
    					foundation.getName().getFirstName().getPropertyValueAsString()) );
    	contact.setLastName(
    			ContactHelper.nullToEmptyString(
    					foundation.getName().getLastName().getPropertyValueAsString()) );
    	
    	
    	BusinessDetail bus = foundation.getBusinessDetail();	
    	/*contact.setEmail(
    			ContactHelper.nullToEmptyString(
    					ContactHelper.getEmail(bus.getEmails(),ContactHelper.WORK_EMAIL)) );*/
    	contact.setWorkPhone( 
    			ContactHelper.nullToEmptyString(
    					ContactHelper.getPhone(bus.getPhones(),ContactHelper.WORK_PHONE)) );
    	contact.setTitle(
    			ContactHelper.nullToEmptyString(
    					ContactHelper.getTitle(bus.getTitles(),ContactHelper.WORK_TITLE)) );
    	contact.setCompany(
    			ContactHelper.nullToEmptyString(
    					bus.getCompany().getPropertyValueAsString()) );
    	
    	
    	Address addr = bus.getAddress();
    	contact.setTown(
    			ContactHelper.nullToEmptyString(
    					addr.getCity().getPropertyValueAsString()) );
    	contact.setCountry(
    			ContactHelper.nullToEmptyString(
    					addr.getCountry().getPropertyValueAsString()) );
    	ContactHelper.constructObmStreet(
    			contact,
    			ContactHelper.nullToEmptyString(
    					addr.getStreet().getPropertyValueAsString()) );
    	contact.setZipCode(
    			ContactHelper.nullToEmptyString(
    					addr.getPostalCode().getPropertyValueAsString()) );
    	contact.setExpressPostal(
    			ContactHelper.nullToEmptyString(
    					addr.getPostOfficeAddress().getPropertyValueAsString()) );
 
    	//email 1
    	contact.setEmail(
    			ContactHelper.nullToEmptyString(
    					ContactHelper.getEmail(
    							foundation.getPersonalDetail().getEmails(),ContactHelper.WORK_EMAIL)) );
 
    	// different in vcard
    	if (type.equals(ObmSyncSource.MSG_TYPE_VCARD)) {
    	   	contact.setHomePhone(
	    			ContactHelper.nullToEmptyString(
	    					ContactHelper.getPhone(
	    							foundation.getPersonalDetail().getPhones(),ContactHelper.OTHER_PHONE)) );
    	} else {
	    	contact.setHomePhone(
	    			ContactHelper.nullToEmptyString(
	    					ContactHelper.getPhone(
	    							foundation.getPersonalDetail().getPhones(),ContactHelper.HOME_PHONE)) );
    	}
    	contact.setMobilePhone(
    			ContactHelper.nullToEmptyString(
    					ContactHelper.getPhone(
    							foundation.getPersonalDetail().getPhones(),ContactHelper.HOME_MOBILE)) );
    	
    	//comment
    	contact.setComment(
    			ContactHelper.nullToEmptyString(
    					ContactHelper.getNote(
    							foundation.getNotes(), ContactHelper.COMMENT)) );
    	
    	//private
    	if ( Helper.nullToZero(
    			foundation.getSensitivity() ).shortValue() == 2 ) {
    		contact.setClassification(1); //private
    	} else {
    		contact.setClassification(0); 
    	}
    	
    	return contact;
    }


}
