package fr.aliasource.funambol.engine.source;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.converter.CalendarToIcalendar;
import com.funambol.common.pim.converter.CalendarToSIFE;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.icalendar.ICalendarParser;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.sif.SIFCalendarParser;
import com.funambol.foundation.exception.EntityException;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.tools.Base64;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.Helper;
import fr.aliasource.obm.items.manager.CalendarManager;

public class CalendarSyncSource extends ObmSyncSource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8820543271150832304L;
	
	private CalendarManager manager;

	public void beginSync(SyncContext context) throws SyncSourceException {
		super.beginSync(context);

		log.info("- Begin an OBM Calendar sync -");
		
		manager = new CalendarManager(getObmAddress());
		
		try {
			manager.logIn(
					context.getPrincipal().getUser().getUsername(),
					context.getPrincipal().getUser().getPassword() );
			manager.initUserEmail();
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		
		manager.setCalendar( manager.getToken().getUser() );
		manager.initRestriction( getRestrictions() );
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
	    com.funambol.common.pim.calendar.Calendar created = null;
	    try {
		    com.funambol.common.pim.calendar.Calendar calendar = getFoundationFromSyncItem(syncItem);
		    
			created = manager.addItem(calendar, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	    
	    log.info(" created with id : "+created.getCalendarContent().getUid().getPropertyValueAsString());
	    
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
	    SyncItemKey[] ret = getSyncItemKeysFromKeys( keys );
	    
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
	    
	    String[] keys  = null;
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
		String[] keys = null;
		try {
			Calendar event = getFoundationFromSyncItem(syncItem);

			keys = manager.getEventTwinKeys(event, this.getSourceType());
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
	    SyncItemKey[] ret = getSyncItemKeysFromKeys( keys );
	    
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
	    Calendar event = null;
	    try {
		    Calendar calendar = getFoundationFromSyncItem(syncItem);
	    
			event = manager.updateItem(
					syncItem.getKey().getKeyAsString(), calendar, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	
	    return getSyncItemFromFoundation(event, SyncItemState.SYNCHRONIZED);
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
	
		Calendar calendar = null;
		try {
			calendar = manager.getItemFromId(key, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		SyncItem ret = getSyncItemFromFoundation(calendar, SyncItemState.UNKNOWN);
	
		return ret;
	}

	
//  -------------------- Private methods ----------------------
	

	/**
     * Get Data from com.funambol.foundation.pdi.event.Calendar
     * converting the Calendar object into a v-card item
     *
     * @param calendar Calendar
     * @return String
     * @throws EntityException
     */
    private String getICalFromFoundationCalendar(Calendar calendar) {

        String ical = null;

        try {
            CalendarToIcalendar c2ical = new CalendarToIcalendar(deviceTimezone, deviceCharset);
            ical = c2ical.convert(calendar);
        } catch (ConverterException ex) {
            //throw new EntityException("Error converting calendar in iCal", ex);
        }
        return ical;
    }
    
    /**
    *
    * Get Data from ICal message
    * converting the ical item into a Calendar object
    *
    * the calendar object is a com.funambol.foundation.pdi.event.Calendar
    *
    * @param content String
    * @return Calendar
    * @throws EntityException
    */
   private Calendar getFoundationCalendarFromICal(String content) {


       ByteArrayInputStream buffer = null;
       ICalendarParser parser = null;
       VCalendar vcalendar = null;
       Calendar calendar = null;

      // content = SourceUtils.handleLineDelimiting(content);

       try {
           vcalendar = new VCalendar();
           buffer = new ByteArrayInputStream(content.getBytes());
           if ((content.getBytes()).length > 0) {
               parser = new ICalendarParser(buffer, deviceCharset);
               vcalendar = (VCalendar) parser.ICalendar();
           }

       } catch (Exception e) {
    	   //throw new EntityException(e.getMessage());
       }

       VCalendarConverter vconvert = new VCalendarConverter(deviceTimezone, deviceCharset);
       try {
    	   calendar =  vconvert.vcalendar2calendar(vcalendar);
       } catch (ConverterException e) {
    	   //throw new EntityException(e.getMessage());
       }
       /*
       // convert in to XML
       String xml = this.getXMLFromFoundationCalendar(calendar);
       // get content from XML
       calendar = this.getFoundationCalendarFromXML(xml);*/

       return calendar;

   }

   /**
    *
    * Get Data from sync4j.foundation.pdi.event.Calendar
    * converting the Calendar object into an xml item
    *
    * @param calendar Calendar
    * @return String
    * @throws EntityException
    */
   private String getXMLFromFoundationCalendar(Calendar calendar) {

       String xml = null;
       try {
           CalendarToSIFE c2xml = new CalendarToSIFE(deviceTimezone, deviceCharset);
           xml = c2xml.convert(calendar);
       } catch (ConverterException ex) {
           //throw new EntityException("Error converting calendar in xml", ex);
       }
       
       log.info(" getXmlFromFoundation return :");
       log.info(xml);
       return xml;
   }

   /**
    * Get Data from XML message
    * converting the xml item into a Calendar object
    *
    * the calendar object is a sync4j.foundation.pdi.event.Calendar
    *
    * @param content String
    * @return Calendar
    * @throws OBMException 
    */
   private Calendar getFoundationCalendarFromXML(String content) throws OBMException {
	   
       StringBuilder sb = new StringBuilder(content.length() + 60);
       sb.append("Converting: ").append("SIFE").append(" => Calendar ")
         .append("\nINPUT = {").append(content).append('}');
       log.info(sb.toString());
     
	   
       ByteArrayInputStream buffer = null;
       Calendar calendar = null;
       try {
           calendar = new Calendar();
           buffer = new ByteArrayInputStream(content.getBytes());
           if ((content.getBytes()).length > 0) {
                   SIFCalendarParser parser = new SIFCalendarParser(buffer);
                   calendar = parser.parse();
           }
       } catch (Exception e){
    	   throw new OBMException("Error converting ", e);
       }
       return calendar;
   }

   private Calendar getFoundationFromSyncItem(SyncItem item) throws OBMException {
       Calendar foundationCalendar = null;

       String content = Helper.getContentOfSyncItem(item, this.isEncode());

       if (MSG_TYPE_ICAL.equals(getSourceType())) {
           foundationCalendar = getFoundationCalendarFromICal(content);
       } else {
           foundationCalendar = getFoundationCalendarFromXML(content);
       }
       log.info("foundation :" +foundationCalendar);
       log.info("getCalendarContent :" +foundationCalendar.getCalendarContent());
       log.info("getUid :" +foundationCalendar.getCalendarContent().getUid());
       foundationCalendar.getCalendarContent().getUid().setPropertyValue(item.getKey().getKeyAsString());
       return foundationCalendar;
   }

   private SyncItem getSyncItemFromFoundation(Calendar calendar, char status) {
       SyncItem syncItem = null;

       String content = null;

       if (MSG_TYPE_ICAL.equals(getSourceType())) {
           content = getICalFromFoundationCalendar(calendar);
       } else {
           content = getXMLFromFoundationCalendar(calendar);
       }

       syncItem = new SyncItemImpl(this,
                                   calendar.getCalendarContent().getUid().getPropertyValueAsString(),
                                   status);

       if (this.isEncode()) {
           syncItem.setContent( Base64.encode(content.getBytes()) );
           syncItem.setType(getSourceType());
           syncItem.setFormat("b64");
       } else {
    	   syncItem.setContent( content.getBytes() );
    	   syncItem.setType(getSourceType());
       }

       return syncItem;
   }
}