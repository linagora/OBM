package fr.aliasource.funambol.engine.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import org.xml.sax.SAXException;

import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.tools.beans.LazyInitBean;

import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.converter.ContactToVcard;
import com.funambol.common.pim.converter.ContactToSIFC;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.sif.SIFCParser;
import com.funambol.common.pim.vcard.VcardParser;
//import com.funambol.foundation.pdi.utils.SourceUtils;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSourceException;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.Helper;
import fr.aliasource.funambol.utils.MD5Helper;
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
		
		manager = new ContactManager();
		
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
	
	    log.info("addSyncItem("                     +
	                       principal                          +
	                       " , "                              +
	                       syncItem.getKey().getKeyAsString() +
	                       ")");
	    Contact contact = getFoundationFromSyncItem(syncItem);
	    contact.setUid(null);
	    Contact created = null;
		try {
			created = manager.addItem(contact, this.getType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    
	    log.info(" created with id : "+created.getUid());
	    
	    return getSyncItemFromFoundation(created, SyncItemState.SYNCHRONIZED);
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getAllSyncItemKeys() throws SyncSourceException {
	
	    log.info("getAllSyncItemKeys(" +
	                       principal   +
	                       ")");
	
	    String[] keys = null;
	    try {
			keys = manager.getAllItemKeys();
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    
	    log.info(" returning "+ret.length+" key(s)");
	    
	    return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getDeletedSyncItemKeys(Timestamp since, Timestamp until)
	throws SyncSourceException {
	
	    log.info("getDeletedSyncItemKeys(" +
	                       principal          +
	                       " , "              +
	                       since              +
	                       " , "              +
	                       until              +
	                       ")");
	    String[] keys = null;
	    try {
			keys = manager.getDeletedItemKeys(since);
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    
	    log.info(" returning "+ret.length+" key(s)");
	    
	    return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getNewSyncItemKeys(Timestamp since, Timestamp until)
	throws SyncSourceException {
	
	    log.info("getNewSyncItemKeys(" +
	                       principal   +
	                       " , "       +
	                       since       +
	                       " , "       +
	                       until       +
	                       ")");
	    
	    String[] keys = null;
	    try {
			keys = manager.getNewItemKeys(since);
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    
	    log.info(" returning "+ret.length+" key(s)");
	    
	    return null;//ret;
	}

	/**
	 * @see SyncSource
	 */
	public SyncItemKey[] getSyncItemKeysFromTwin(SyncItem syncItem)
	throws SyncSourceException {
		
		log.info("getSyncItemKeysFromTwin(" 	   +
                principal						   +
                ")"	);
		
		Contact contact = getFoundationFromSyncItem(syncItem);
		
	    String[] keys = null;
	    try {
			keys = manager.getContactTwinKeys(contact, this.getType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    
	    log.info(" returning "+ret.length+" key(s)");
	    
	    return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getUpdatedSyncItemKeys(Timestamp since, Timestamp until)
	throws SyncSourceException {
	
	    log.info("getUpdatedSyncItemKeys(" +
	                       principal          +
	                       " , "              +
	                       since              +
	                       " , "              +
	                       until              +
	                       ")");
	
	    String[] keys = null;
	    try {
			keys = manager.getUpdatedItemKeys(since);
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);
	    
	    log.info(" returning "+ret.length+" key(s)");
	    
	    return ret;
	}

	public void removeSyncItem(SyncItemKey syncItemKey, Timestamp time, boolean softDelete)
	throws SyncSourceException {
	
	    log.info("removeSyncItem(" +
	                       principal    +
	                       " , "        +
	                       syncItemKey  +
	                       " , "        +
	                       time         +
	                       ")");
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
	
	    log.info("updateSyncItem("                     			+
	                       principal                         	+
	                       " , "                              	+
	                       syncItem.getKey().getKeyAsString() 	+
	                       ")");
	    
	    Contact contact = getFoundationFromSyncItem(syncItem);
	    try {
			contact = manager.updateItem(
					syncItem.getKey().getKeyAsString(), contact, this.getType());
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
	
		log.info("getSyncItemFromId(" +
	                       principal             +
	                       ", "                  +
	                       syncItemKey           +
	                       ")");
	
		String key = syncItemKey.getKeyAsString();
	
		com.funambol.common.pim.contact.Contact contact = null;
		try {
			contact = manager.getItemFromId(key, this.getType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		SyncItem ret = getSyncItemFromFoundation(contact, SyncItemState.UNKNOWN);
	
		return ret;
	}

	
	//  -------------------- Private methods ----------------------
	
	
	private SyncItem getSyncItemFromFoundation(Contact contact, char state) {
	
		SyncItem syncItem = null;
        String content    = null;

        if (MSG_TYPE_VCARD.equals(this.getType())) {
            content = getVCardFromFoundationContact(contact);
        } else {
            content = getXMLFromFoundationContact(contact);
        }

        syncItem = new SyncItemImpl(this, contact.getUid(), state);

        if (this.isEncode()) {
            syncItem.setContent(com.funambol.framework.tools.Base64.encode(content.getBytes()) );
            syncItem.setType(this.getType());
            syncItem.setFormat("b64");
        } else {
            syncItem.setContent(content.getBytes() );
            syncItem.setType(this.getType());
        }
        

        return syncItem;
	}

	private String getXMLFromFoundationContact(Contact contact) {
		String xml = null;
	    ContactToSIFC c2xml = new ContactToSIFC(deviceTimezone, deviceCharset);
	    try {
			xml = c2xml.convert(contact);
		} catch (ConverterException e) {
			e.printStackTrace();
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

	private Contact getFoundationFromSyncItem(SyncItem item) {
		
		Contact contact = null;
		String content = null;
		
		content = Helper.getContentOfSyncItem(item, this.isEncode());
		
		if (MSG_TYPE_VCARD.equals(this.getType())) {
            contact = getFoundationContactFromVCard(content);
        } else {
            contact = getFoundationContactFromXML(content);
        }
		
	    contact.setUid(item.getKey().getKeyAsString());
	    
	    return contact;
	}

	private Contact getFoundationContactFromXML(String content) {
		
		Contact result = new Contact();
		
		ByteArrayInputStream buffer = null;
	    SIFCParser parser = null;
	
	    buffer  = new ByteArrayInputStream(content.getBytes());
	    if ((content.getBytes()).length > 0) {
	        try {
				parser = new SIFCParser(buffer);
				result = (Contact) parser.parse();
			} catch (SAXException e) {
				log.info(e.getMessage());
			} catch (IOException e) {
				log.info(e.getMessage());
			}
	    }
		return result;
	}

	private Contact getFoundationContactFromVCard(String content) {
		 
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
            e.printStackTrace();
        }

        /**
         * workaround
         *
         */
        // convert in to XML
        String xml = this.getXMLFromFoundationContact(contact);
        // get content from XML
        contact = this.getFoundationContactFromXML(xml);

        return contact;
	}


}
