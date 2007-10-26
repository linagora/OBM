package fr.aliasource.obm.items.manager;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.funambol.common.pim.contact.Address;
import com.funambol.common.pim.contact.BusinessDetail;
import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

import fr.aliacom.obm.AddressBookLocator;
import fr.aliacom.obm.ebook.AddressBookBindingStub;
import fr.aliacom.obm.fault.AuthFault;
import fr.aliacom.obm.fault.ServerFault;
import fr.aliacom.obm.wcontact.BookSync;
import fr.aliacom.obm.wcontact.Contact;
import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.engine.source.ObmSyncSource;
import fr.aliasource.funambol.utils.ContactHelper;
import fr.aliasource.funambol.utils.Helper;

public class ContactManager extends ObmManager {

	private AddressBookBindingStub binding;
	private String book;
	protected FunambolLogger log = FunambolLoggerFactory.getLogger("funambol");
	
	private Log logger = LogFactory.getLog(getClass());
	
	public ContactManager(String obmAddress) {
		
		AddressBookBindingStub addressBinding = null;
		try {
			AddressBookLocator addressbookLocator = new AddressBookLocator();
			addressbookLocator.setAddressBookEndpointAddress(obmAddress);
			addressBinding = (AddressBookBindingStub)addressbookLocator.getAddressBook();
			
		} catch (ServiceException e) {
			log.error(e.getMessage());
		}
		binding = addressBinding;
	}

	
	public void initRestriction(int restrictions) {
		this.restrictions = restrictions;
		if (log.isTraceEnabled()) {
			log.trace(" init restrictions: "+restrictions);
		}
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
	}

	public com.funambol.common.pim.contact.Contact getItemFromId(String key, String type) 
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
		
		com.funambol.common.pim.contact.Contact ret = obmContactTofoundation(contact, type);
		
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

	public com.funambol.common.pim.contact.Contact updateItem(String key,
			com.funambol.common.pim.contact.Contact contact, String type)
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

	public com.funambol.common.pim.contact.Contact addItem(
			com.funambol.common.pim.contact.Contact contact, String type) 
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
	
	public String[] getContactTwinKeys(com.funambol.common.pim.contact.Contact contact, String type) 
		throws OBMException {
		
		String[] keys = null;
		
		Contact c = foundationContactToObm(contact, type);
		
		if (log.isDebugEnabled()) {
			log.debug(" look twin of : "+c.getFirstName()+","+c.getLastName()+","+c.getCompany());
		}
		
		try {
			c.setUid(null);
			keys = binding.getContactTwinKeys(token,book,c).getKey();
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
		//String owner = "";
		//String user = token.getUser();
		for (int i=0 ; i < updated.length ; i++) {
			//owner = Helper.nullToEmptyString(updated[i].getOwner());
			//if ( ( ((restrictions & Helper.RESTRICT_PRIVATE) == Helper.RESTRICT_PRIVATE)
			//	    && (updated[i].getClassification() == 1 && !owner.equals(user)) )
			//  || ( ((restrictions & Helper.RESTRICT_OWNER  ) == Helper.RESTRICT_OWNER)
			//		&& (!owner.equals(user)) ) )
			//{
			//	if (d != null) {
			//		deletedRest.add(  (""+updated[i].getUid()) );
			//	}	
			//} else {
				updatedRest.put( ""+updated[i].getUid(), updated[i] );
			//}
		}
		
		for (int j=0 ; j < deleted.length ; j++) {
			deletedRest.add( (String) (""+deleted[j]) );
		}
		
		syncReceived = true;
	}
	
	
	private com.funambol.common.pim.contact.Contact obmContactTofoundation(Contact obmcontact, String type) {
    	com.funambol.common.pim.contact.Contact contact = new com.funambol.common.pim.contact.Contact();
    	
    	contact.setUid(""+obmcontact.getUid());
    	
    	contact.getName().getFirstName().setPropertyValue(obmcontact.getFirstName());
    	contact.getName().getLastName().setPropertyValue(obmcontact.getLastName());
    	contact.getName().getDisplayName().setPropertyValue(
				ContactHelper.constructDisplayName(obmcontact.getFirstName(),
						obmcontact.getLastName()));
    	contact.getName().getNickname().setPropertyValue(obmcontact.getAka());
    	
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
    	bus.getDepartment().setPropertyValue(obmcontact.getService());
    	
    	Address addr = bus.getAddress();
    	addr.getCity().setPropertyValue(obmcontact.getTown());
    	if (obmcontact.getCountry() != null) {
    		addr.getCountry().setPropertyValue(obmcontact.getCountry());
    	}
    	addr.getStreet().setPropertyValue( ContactHelper.getStreetFromObm(obmcontact) );
    	addr.getPostalCode().setPropertyValue(obmcontact.getZipCode());
    	addr.getPostOfficeAddress().setPropertyValue(obmcontact.getExpressPostal());
    	
    	//email 1
    	ContactHelper.setFoundationEmail(
    			contact.getPersonalDetail(), obmcontact.getEmail(), ContactHelper.WORK_EMAIL );
    	//email 2
    	ContactHelper.setFoundationEmail(
    			contact.getPersonalDetail(), obmcontact.getEmail2(), ContactHelper.HOME_EMAIL );
    	
    	if (type.equals(ObmSyncSource.MSG_TYPE_VCARD)) {
    		ContactHelper.setFoundationPhone(
    				contact.getPersonalDetail(),
    	   			obmcontact.getHomePhone(), ContactHelper.HOME_PHONE );//OTHER_PHONE );
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
    
    private Contact foundationContactToObm(com.funambol.common.pim.contact.Contact foundation, String type) {

    	Contact contact = new Contact();
    	
    	if (foundation.getUid() != null && foundation.getUid() != "") {
    		contact.setUid( new Integer(foundation.getUid()) );
    	}
    	
    	contact.setFirstName(
    			ContactHelper.nullToEmptyString(
    					foundation.getName().getFirstName().getPropertyValueAsString()) );
    	contact.setLastName(ContactHelper.getLastName(foundation));
    	
    	if (ContactHelper.nullToEmptyString(
				foundation.getName().getNickname().getPropertyValueAsString())
				.equalsIgnoreCase("")) {
			contact.setAka(null);
		} else {
	    	contact.setAka(
	    			ContactHelper.nullToEmptyString(
	    					foundation.getName().getNickname().getPropertyValueAsString()) );
		}
    	
    	
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
    	contact.setService(
    			ContactHelper.nullToEmptyString(
    					bus.getDepartment().getPropertyValueAsString()) );
    	
    	Address addr = bus.getAddress();
    	contact.setTown(
    			ContactHelper.nullToEmptyString(
    					addr.getCity().getPropertyValueAsString()) );
    	//country_iso_iso3166 char 2 in obm
    	contact.setCountry(
    			ContactHelper.getCountry(addr)
    			);
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
    	//email 2
    	contact.setEmail2(
    			ContactHelper.nullToEmptyString(
    					ContactHelper.getEmail(
    							foundation.getPersonalDetail().getEmails(),ContactHelper.HOME_EMAIL)) );
    	
    	// different in vcard
    	if (type.equals(ObmSyncSource.MSG_TYPE_VCARD)) {
    	   	contact.setHomePhone(
	    			ContactHelper.nullToEmptyString(
	    					ContactHelper.getPhone(
	    							foundation.getPersonalDetail().getPhones(),ContactHelper.HOME_PHONE)) );//OTHER_PHONE)) );
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
    			foundation.getSensitivity() ).shortValue() == 0 ) { //olNormal
    		contact.setClassification(0); //public
    	} else {
    		contact.setClassification(1); //private
    	}
    	
    	return contact;
    }


}
