package fr.aliasource.obm.items.manager;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import com.funambol.framework.logging.Sync4jLogger;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.CalendarHelper;
import fr.aliasource.funambol.utils.Helper;
import fr.aliasource.obm.CalendarServiceLocator;
import fr.aliasource.obm.ecalendar.CalendarBindingStub;
import fr.aliasource.obm.fault.AuthFault;
import fr.aliasource.obm.fault.ServerFault;
import fr.aliasource.obm.wauth.AccessToken;
import fr.aliasource.obm.wcalendar.Attendee;
import fr.aliasource.obm.wcalendar.CalendarSync;
import fr.aliasource.obm.wcalendar.Event;
import fr.aliasource.obm.wcalendar.EventRecurrence;
import fr.aliasource.obm.wcontact.BookSync;
import fr.aliasource.obm.wcontact.Contact;

public class CalendarManager extends ObmManager {

	private CalendarBindingStub binding;
	private AccessToken token;
	private String calendar;
	private Logger log = null;
	private String userEmail;
	
	public CalendarManager() {
		
		log = Sync4jLogger.getLogger("server");
		
		CalendarBindingStub calendarBinding = null;
		try {
			CalendarServiceLocator calendarLocator = new CalendarServiceLocator();
			calendarBinding = (CalendarBindingStub)calendarLocator.getCalendar();
			
		} catch (ServiceException e) {
			log.info(e.getMessage());
		}
		binding = calendarBinding;
	}
	
	public void initRestriction(int restrictions) {
		this.restrictions = restrictions | Helper.RESTRICT_REFUSED;
	}
	
	public String getCalendar() {
		return calendar;
	}

	public void setCalendar(String calendar) {
		this.calendar = calendar;
	}

	public CalendarBindingStub getBinding() {
		return binding;
	}

	public AccessToken getToken() {
		return token;
	}
	
	//
	
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

	public void initUserEmail() throws OBMException {
		//userEmail = "nicolas.lascombes@aliasource.fr";
		
		try {
			userEmail = binding.getUserEmail(token, calendar, token.getUser());
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
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

	public String[] getRefusedItemKeys(Timestamp since) throws OBMException {

		Calendar d = Calendar.getInstance();
		d.setTime(since);
		
		String[] keys = null;
		
		try {
			keys = binding.getRefusedKeys(token,calendar,d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
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

	public com.funambol.foundation.pdi.event.Calendar getItemFromId(String key, String type) 
		throws OBMException {
		
		Event event = null;
		
		event = (Event) updatedRest.get(key);
		
		if (event == null) {
			log.info(" item "+key+" not found in updated -> get from sever");
			try {
				event = binding.getEventFromId(token, calendar, key);
			} catch (AuthFault e) {
				throw new OBMException(e.getMessage());
			} catch (ServerFault e) {
				throw new OBMException(e.getMessage());
			} catch (RemoteException e) {
				throw new OBMException(e.getMessage());
			}
		}
		
		com.funambol.foundation.pdi.event.Calendar ret = obmEventToFoundationCalendar(event, type);
		
		return ret;
	}

	public void removeItem(String key) throws OBMException {
		
		Event event = null;
		try {
			event = binding.getEventFromId(token, calendar, key);
			//log.info(" attendees size : "+event.getAttendees().length );
			//log.info(" owner : "+event.getOwner()+" calendar : "+calendar);
			if (event.getAttendees() == null || event.getAttendees().length == 1) {
				//no attendee (only the owner)
				binding.removeEvent(token, calendar, key);
			} else {
				CalendarHelper.refuseEvent(event, userEmail);
				//event = binding.refuseEvent(token, calendar, event);
				binding.modifyEvent(token, calendar, event, true);
			}
			
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
	}

	public com.funambol.foundation.pdi.event.Calendar updateItem(String key,
			com.funambol.foundation.pdi.event.Calendar event, String type)
				throws OBMException {
		
		Event c = null;
		try {
			c = binding.modifyEvent(token,calendar,foundationCalendarToObmEvent(event, type),false);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
	
		return obmEventToFoundationCalendar(c, type);
	}

	public com.funambol.foundation.pdi.event.Calendar addItem(
			com.funambol.foundation.pdi.event.Calendar event, String type) 
				throws OBMException {
		
		String uid = null;
		Event evt = null;
		
		try {
			uid = binding.createEvent(token,calendar,foundationCalendarToObmEvent(event, type));
			evt = binding.findEvent(token, calendar, uid);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		return obmEventToFoundationCalendar(evt, type);
	}
	
	public String[] getEventTwinKeys(com.funambol.foundation.pdi.event.Calendar event, String type) 
		throws OBMException {
		
		String[] keys = null;
		
		Event evt = foundationCalendarToObmEvent(event, type);
		
		//log.info(" look twin of : "+c.getFirstName()+","+c.getLastName()+","+c.getCompany());
		
		try {
			keys = binding.getEventTwinKeys(token,calendar,evt);
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
		
		CalendarSync sync = null;
		//get modified items
		try {
			 sync = binding.getSync(token, calendar, d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}
		
		Event[] updated = new Event[0];
		if (sync.getUpdated() != null) updated = sync.getUpdated();
		String[] deleted = new String[0];
		if (sync.getRemoved() != null) deleted = sync.getRemoved(); 
		
		//apply restriction(s)
		updatedRest = new HashMap();
		deletedRest = new ArrayList();
		String owner = "";
		String user = token.getUser();
		
		for (int i=0 ; i < updated.length ; i++) {
			owner = Helper.nullToEmptyString(updated[i].getOwner());
			if ( ( ((restrictions & Helper.RESTRICT_PRIVATE) == Helper.RESTRICT_PRIVATE)
				    && (updated[i].getClassification() == 1 && !calendar.equals(user)) )
			  || ( ((restrictions & Helper.RESTRICT_OWNER) == Helper.RESTRICT_OWNER)
					&& (!owner.equals(user)) )
			  || ( ((restrictions & Helper.RESTRICT_REFUSED) == Helper.RESTRICT_REFUSED)
			  		&& (CalendarHelper.isUserRefused(userEmail,updated[i].getAttendees()))) )
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
	
	
	/**
	 * Convert an OBM event in a calendar of type com.funambol.foundation.pdi.event.Calendar
	 * 
	 * @param obmevent
	 * @param type
	 * @return
	 */
	private com.funambol.foundation.pdi.event.Calendar obmEventToFoundationCalendar(Event obmevent, String type) {
    	
		com.funambol.foundation.pdi.event.Calendar calendar = new com.funambol.foundation.pdi.event.Calendar();
    	com.funambol.foundation.pdi.event.Event event = calendar.getEvent();
    	
    	event.getUid().setPropertyValue(obmevent.getUid());
/* this.uid = uid;
   this.owner = owner;
   this.priority = priority;
   this.classification = classification;
   this.allday = allday;
   this.date = date;
   this.duration = duration;
   this.title = title;
   this.description = description;
   this.category = category;
   this.location = location;
   this.attendees = attendees;
   this.recurrence = recurrence;*/
    	
    	event.setAllDay( new Boolean( obmevent.isAllday() ) );
    	
    	Date dstart = obmevent.getDate().getTime();
 
    	event.getDtStart().setPropertyValue( CalendarHelper.getUTCFormat(dstart) );
    	
    	java.util.Calendar temp = java.util.Calendar.getInstance();
        temp.setTime(dstart);
        temp.add( java.util.Calendar.SECOND, obmevent.getDuration() );	
        Date dend = temp.getTime();
    	
    	event.getDtEnd().setPropertyValue( CalendarHelper.getUTCFormat(dend) );
    	
    	event.getSummary().setPropertyValue( obmevent.getTitle() );
    	event.getDescription().setPropertyValue( obmevent.getDescription() );
 
    	event.getCategories().setPropertyValue( obmevent.getCategory() );
    	event.getLocation().setPropertyValue( obmevent.getLocation() );
    	
    	if (obmevent.getClassification() == 1) {
    		event.getClassEvent().setPropertyValue("2"); //olPrivate
    	} else {
    		event.getClassEvent().setPropertyValue("0"); //olNormal
    	}
    	event.setBusyStatus( new Short((short) 2) ); //olBusy
    
    	event.getPriority().setPropertyValue( 
    			Helper.getPriority(obmevent.getPriority().intValue()) );
    	event.getStatus().setPropertyValue("Tentative");
    	
    	/*XTag classification = new XTag();
    	classification.setXTagValue("Classification");
    	classification.getXTag().setPropertyValue("2");
    	event.addXTag(classification);*/
    	
    	EventRecurrence obmrec = obmevent.getRecurrence();
    	if ( !obmrec.getKind().equals("none") ) {
	    	event.setRecurrencePattern( CalendarHelper.getRecurrence(dstart, dend, obmrec) );
    	}
    	
    	return calendar;
    }
    
    /**
     * Convert a calendar of type com.funambol.foundation.pdi.event.Calendar in an OBM event
     * 
     * @param calendar
     * @param type
     * @return
     */
    private Event foundationCalendarToObmEvent(com.funambol.foundation.pdi.event.Calendar calendar, String type) {

    	Event event = new Event();
    	com.funambol.foundation.pdi.event.Event foundation = calendar.getEvent();
    	
    	
    	if (foundation.getUid() != null && foundation.getUid().getPropertyValueAsString() != "") {
    		event.setUid( foundation.getUid().getPropertyValueAsString() );
    	}
    	
    	event.setAllday( foundation.getAllDay().booleanValue() );
    	
    	
    	Date dstart = CalendarHelper.getDateFromUTCString(
    			foundation.getDtStart().getPropertyValueAsString() );
    	java.util.Calendar cstart = java.util.Calendar.getInstance();
    	cstart.setTime( dstart );
    	
    	event.setDate(cstart);
    	
    	Date dend = CalendarHelper.getDateFromUTCString(
    			foundation.getDtEnd().getPropertyValueAsString() );
    	if (dend.getTime() != dstart.getTime()) {
    		event.setDuration( (int)( (dend.getTime() - dstart.getTime()) / 1000) );
    	} else {
    		event.setDuration(0);
    	}
    	event.setTitle( foundation.getSummary().getPropertyValueAsString() );
    	event.setDescription( foundation.getDescription().getPropertyValueAsString() );
    	
    	event.setCategory( CalendarHelper.getOneCategory(
    			foundation.getCategories().getPropertyValueAsString()) );
    	event.setLocation( foundation.getLocation().getPropertyValueAsString() );
    	
    	event.setPriority( Helper.getPriorityFromFoundation(
    			foundation.getPriority().getPropertyValueAsString()) );
    	
    	if ( Helper.nullToEmptyString(
    			foundation.getClassEvent().getPropertyValueAsString()).equals("2") ) {
    		event.setClassification(1); //private
    	} else {
    		event.setClassification(0);
    	}
    	
    	EventRecurrence recurrence = null;
    	if (foundation.isRecurrent()) {
    		recurrence = CalendarHelper.getRecurrenceFromFoundation(
    				foundation.getRecurrencePattern(), dend );
    	} else {
    		recurrence = new EventRecurrence();
    		recurrence.setKind("none");
        	recurrence.setDays("");
        	recurrence.setFrequence(1);
    	}
    	event.setRecurrence(recurrence);
    	
    	return event;
    }

}
