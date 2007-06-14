package fr.aliasource.funambol.engine.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;

import org.xml.sax.SAXException;

import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.ContactToSIFC;
import com.funambol.common.pim.converter.ContactToVcard;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.sif.SIFCParser;
import com.funambol.common.pim.vcard.VcardParser;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.tools.beans.LazyInitBean;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.Helper;
import fr.aliasource.obm.items.manager.ContactManager;

public final class ContactSyncSource extends ObmSyncSource 
	implements SyncSource, Serializable, LazyInitBean {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ContactManager manager;
	
	public ContactSyncSource() {
		super();
	}

	public void beginSync(SyncContext context) throws SyncSourceException {
		super.beginSync(context);

		log.info("- Begin an OBM Contact sync -");
		
		manager = new ContactManager(getObmAddress());
		
		try {
			manager.logIn(
					context.getPrincipal().getUser().getUsername(),
					context.getPrincipal().getUser().getPassword() );

		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		
		//manager.logIn("nicolasl","44669462809866fbfc6eab1f9fa93d4b");
		manager.setBook("contacts");
		manager.initRestriction(getRestrictions());
	}

	public void init() {
		super.init();
	}

	public void setOperationStatus(String operation, int statusCode, SyncItemKey[] keys) {
		super.setOperationStatus(operation, statusCode, keys);
	}

	public String toString() {
		return super.toString();
	}
	
	/**
	 * @see SyncSource
	 */
	public SyncItem addSyncItem(SyncItem syncItem) throws SyncSourceException {
	
		if (log.isDebugEnabled()) {
			log.debug("addSyncItem("                     +
	                       principal                          +
	                       " , "                              +
	                       syncItem.getKey().getKeyAsString() +
	                       ")");
		}
	    Contact contact = null;
	    Contact created = null;
		try {
			contact = getFoundationFromSyncItem(syncItem);
			contact.setUid(null);
			created = manager.addItem(contact, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    
	    log.debug(" created with id : "+created.getUid());
	    
	    return getSyncItemFromFoundation(created, SyncItemState.SYNCHRONIZED);
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getAllSyncItemKeys() throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("getAllSyncItemKeys(" +
	                       principal   +
	                       ")");
		}
	    String[] keys = null;
	    try {
			keys = manager.getAllItemKeys();
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    if (log.isDebugEnabled()) {
	    	log.debug(" returning "+ret.length+" key(s)");
	    }
	    
	    return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getDeletedSyncItemKeys(Timestamp since, Timestamp until)
	throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("getDeletedSyncItemKeys(" +
	                       principal          +
	                       " , "              +
	                       since              +
	                       " , "              +
	                       until              +
	                       ")");
		}
	    String[] keys = null;
	    try {
			keys = manager.getDeletedItemKeys(since);
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    if (log.isDebugEnabled()) {
	    	log.debug(" returning "+ret.length+" key(s)");
	    }
	    
	    return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getNewSyncItemKeys(Timestamp since, Timestamp until)
	throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("getNewSyncItemKeys(" +
	                       principal   +
	                       " , "       +
	                       since       +
	                       " , "       +
	                       until       +
	                       ")");
		}
	    
	    String[] keys = null;
	    try {
			keys = manager.getNewItemKeys(since);
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    if (log.isDebugEnabled()) {
	    	log.debug(" returning "+ret.length+" key(s)");
	    }
	    
	    return null;
	}

	/**
	 * @throws  
	 * @see SyncSource
	 */
	public SyncItemKey[] getSyncItemKeysFromTwin(SyncItem syncItem)
	throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("getSyncItemKeysFromTwin(" 	   +
                principal						   +
                ")"	);
		}
		
		Contact contact = null;
		
	    String[] keys = null;
	    try {
	    	contact = getFoundationFromSyncItem(syncItem);
			keys = manager.getContactTwinKeys(contact, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    if (log.isDebugEnabled()) {
	    	log.debug(" returning "+ret.length+" key(s)");
	    }
	    return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getUpdatedSyncItemKeys(Timestamp since, Timestamp until)
	throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("getUpdatedSyncItemKeys(" +
	                       principal          +
	                       " , "              +
	                       since              +
	                       " , "              +
	                       until              +
	                       ")");
		}
	
	    String[] keys = null;
	    try {
			keys = manager.getUpdatedItemKeys(since);
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    if (log.isDebugEnabled()) {
	    	log.debug(" returning "+ret.length+" key(s)");
	    }
	    return ret;
	}

	public void removeSyncItem(SyncItemKey syncItemKey, Timestamp time, boolean softDelete)
	throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("removeSyncItem(" +
	                       principal    +
	                       " , "        +
	                       syncItemKey  +
	                       " , "        +
	                       time         +
	                       ")");
		}
	    try {
			manager.removeItem(syncItemKey.getKeyAsString());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	}

	/*
	 * @see SyncSource
	 */
	public SyncItem updateSyncItem(SyncItem syncItem)
	throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("updateSyncItem("                     			+
	                       principal                         	+
	                       " , "                              	+
	                       syncItem.getKey().getKeyAsString() 	+
	                       ")");
		}
	    
	    Contact contact = null;
	    try {
	    	contact = getFoundationFromSyncItem(syncItem);
			contact = manager.updateItem(
					syncItem.getKey().getKeyAsString(), contact, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	
	    return getSyncItemFromFoundation(contact, SyncItemState.SYNCHRONIZED);
	}
	
	/*
	 * @see SyncSource
	 */
	public SyncItem getSyncItemFromId(SyncItemKey syncItemKey)
	throws SyncSourceException {
		if (log.isDebugEnabled()) {
			log.debug("getSyncItemFromId(" +
	                       principal             +
	                       ", "                  +
	                       syncItemKey           +
	                       ")");
		}
	
		String key = syncItemKey.getKeyAsString();
	
		com.funambol.common.pim.contact.Contact contact = null;
		try {
			contact = manager.getItemFromId(key, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		SyncItem ret = getSyncItemFromFoundation(contact, SyncItemState.UNKNOWN);
	
		return ret;
	}

	
	//  -------------------- Private methods ----------------------
	
	
	private SyncItem getSyncItemFromFoundation(Contact contact, char state) throws SyncSourceException {
	
		SyncItem syncItem = null;
        String content    = null;

        if (MSG_TYPE_VCARD.equals(getSourceType())) {
            content = getVCardFromFoundationContact(contact);
        } else {
            content = getXMLFromFoundationContact(contact);
        }

        syncItem = new SyncItemImpl(this, contact.getUid(), state);

        if (this.isEncode()) {
            syncItem.setContent(com.funambol.framework.tools.Base64.encode(content.getBytes()) );
            syncItem.setType(getSourceType());
            syncItem.setFormat("b64");
        } else {
            syncItem.setContent(content.getBytes() );
            syncItem.setType(getSourceType());
        }
        

        return syncItem;
	}

	private String getXMLFromFoundationContact(Contact contact) throws SyncSourceException {
		String xml = null;
	    ContactToSIFC c2xml = new ContactToSIFC(deviceTimezone, deviceCharset);
	    try {
			xml = c2xml.convert(contact);
		} catch (ConverterException e) {
			throw new SyncSourceException("Error converting to XML",e);
		}
	    
	    return xml;
	}

	private String getVCardFromFoundationContact(Contact contact) {
        String vcard = null;

        try {
            ContactToVcard c2vcard = new ContactToVcard(deviceTimezone, deviceCharset);
            vcard = c2vcard.convert(contact);
        } catch (ConverterException ex) {
            ex.printStackTrace();
        }
        return vcard;
	}

	private Contact getFoundationFromSyncItem(SyncItem item) throws OBMException {
		
		Contact contact = null;
		String content = null;
		
		content = Helper.getContentOfSyncItem(item, this.isEncode());
		
		if (MSG_TYPE_VCARD.equals(getSourceType())) {
            contact = getFoundationContactFromVCard(content);
        } else {
            contact = getFoundationContactFromXML(content);
        }
		
	    contact.setUid(item.getKey().getKeyAsString());
	    
	    return contact;
	}

	private Contact getFoundationContactFromXML(String content) throws OBMException {
		
		Contact result = new Contact();
		
		ByteArrayInputStream buffer = null;
	    SIFCParser parser = null;
	
	    buffer  = new ByteArrayInputStream(content.getBytes());
	    if ((content.getBytes()).length > 0) {
	        try {
				parser = new SIFCParser(buffer);
				result = (Contact) parser.parse();
			} catch (SAXException e) {
				throw new OBMException("Error converting from XML", e);
			} catch (IOException e) {
				throw new OBMException("Error converting from XML", e);
			}
	    }
		return result;
	}

	private Contact getFoundationContactFromVCard(String content) throws OBMException {
		 
		ByteArrayInputStream buffer = null;
        VcardParser parser = null;
        Contact contact = null;

        //content = SourceUtils.handleLineDelimiting(content);

        try {
            contact = new Contact();
            buffer = new ByteArrayInputStream(content.getBytes());
            if ((content.getBytes()).length > 0) {
                parser  = new VcardParser(buffer, deviceTimezoneDescr, deviceCharset);
                contact = parser.vCard();
            }
        } catch (Exception e) {
            throw new OBMException("Error converting from Vcard",e);
        }

        return contact;
	}


}
