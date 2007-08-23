package fr.aliasource.obm.items.manager;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.rpc.ServiceException;

import com.funambol.framework.logging.FunambolLogger;
import com.funambol.framework.logging.FunambolLoggerFactory;

import fr.aliacom.obm.CalendarServiceLocator;
import fr.aliacom.obm.ecalendar.CalendarBindingStub;
import fr.aliacom.obm.fault.AuthFault;
import fr.aliacom.obm.fault.ServerFault;
import fr.aliacom.obm.wauth.AccessToken;
import fr.aliacom.obm.wcalendar.CalendarSync;
import fr.aliacom.obm.wcalendar.Event;
import fr.aliacom.obm.wcalendar.EventRecurrence;
import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.CalendarHelper;
import fr.aliasource.funambol.utils.Helper;

public class CalendarManager extends ObmManager {

	private CalendarBindingStub binding;
	private AccessToken token;
	private String calendar;
	protected FunambolLogger log = FunambolLoggerFactory.getLogger("funambol");
	private String userEmail;

	public CalendarManager(String obmAddress) {

		try {
			CalendarServiceLocator calendarLocator = new CalendarServiceLocator();
			calendarLocator.setCalendarEndpointAddress(obmAddress);
			CalendarBindingStub calendarBinding = (CalendarBindingStub) calendarLocator
					.getCalendar();
			binding = calendarBinding;
		} catch (ServiceException e) {
			log.error(e.getMessage());
		}
	}

	public void initRestriction(int restrictions) {
		this.restrictions = restrictions;
		if (log.isTraceEnabled()) {
			log.trace(" init restrictions: " + restrictions);
		}
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
			throw new OBMException("OBM Login refused for user : " + user);
		}
	}

	public void initUserEmail() throws OBMException {
		// userEmail = "nicolas.lascombes@aliasource.fr";

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

		if (!syncReceived) {
			getSync(null);
		}

		return extractKeys(updatedRest);
	}

	public String[] getDeletedItemKeys(Timestamp since) throws OBMException {

		if (!syncReceived) {
			getSync(since);
		}

		return Helper.listToTab(deletedRest);
	}

	public String[] getRefusedItemKeys(Timestamp since) throws OBMException {

		Calendar d = Calendar.getInstance();
		d.setTime(since);

		String[] keys = null;

		try {
			keys = binding.getRefusedKeys(token, calendar, d).getKey();
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
		if (!syncReceived) {
			getSync(since);
		}

		return extractKeys(updatedRest);
	}

	public com.funambol.common.pim.calendar.Calendar getItemFromId(String key,
			String type) throws OBMException {

		Event event = null;

		event = (Event) updatedRest.get(key);

		if (event == null) {
			log
					.info(" item " + key
							+ " not found in updated -> get from sever");
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

		com.funambol.common.pim.calendar.Calendar ret = obmEventToFoundationCalendar(
				event, type);

		return ret;
	}

	public void removeItem(String key) throws OBMException {

		Event event = null;
		try {
			event = binding.getEventFromId(token, calendar, key);
			// log.info(" attendees size : "+event.getAttendees().length );
			// log.info(" owner : "+event.getOwner()+" calendar : "+calendar);
			if (event.getAttendees() == null
					|| event.getAttendees().length == 1) {
				// no attendee (only the owner)
				binding.removeEvent(token, calendar, key);
			} else {
				CalendarHelper.refuseEvent(event, userEmail);
				// event = binding.refuseEvent(token, calendar, event);
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

	public com.funambol.common.pim.calendar.Calendar updateItem(String key,
			com.funambol.common.pim.calendar.Calendar event, String type)
			throws OBMException {

		Event c = null;
		try {
			c = binding.modifyEvent(token, calendar,
					foundationCalendarToObmEvent(event, type), false);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}

		return obmEventToFoundationCalendar(c, type);
	}

	public com.funambol.common.pim.calendar.Calendar addItem(
			com.funambol.common.pim.calendar.Calendar event, String type)
			throws OBMException {

		String uid = null;
		Event evt = null;

		try {
			uid = binding.createEvent(token, calendar,
					foundationCalendarToObmEvent(event, type));
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

	public String[] getEventTwinKeys(
			com.funambol.common.pim.calendar.Calendar event, String type)
			throws OBMException {

		String[] keys = null;

		Event evt = foundationCalendarToObmEvent(event, type);

		// log.info(" look twin of :
		// "+c.getFirstName()+","+c.getLastName()+","+c.getCompany());

		try {
			evt.setUid(null);
			keys = binding.getEventTwinKeys(token, calendar, evt).getKey();
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		} catch (RemoteException e) {
			throw new OBMException(e.getMessage());
		}

		return keys;
	}

	// ---------------- Private methods ----------------------------------

	private void getSync(Timestamp since) throws OBMException {
		Calendar d = null;
		if (since != null) {
			d = Calendar.getInstance();
			d.setTime(since);
		}

		CalendarSync sync = null;
		// get modified items
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
		if (sync.getUpdated() != null) {
			updated = sync.getUpdated();
		}
		String[] deleted = new String[0];
		if (sync.getRemoved() != null) {
			deleted = sync.getRemoved();
		}

		// remove refused events and private events
		updatedRest = new HashMap();
		deletedRest = new ArrayList();
		String user = token.getUser();

		for (Event e : updated) {
			if (e.getClassification() == 1
					&& !calendar.equals(user)
					|| (CalendarHelper.isUserRefused(userEmail, e
							.getAttendees()))) {
				if (d != null) {
					deletedRest.add(("" + e.getUid()));
				}
			} else {
				updatedRest.put("" + e.getUid(), e);
			}
		}

		for (String del : deleted) {
			deletedRest.add("" + del);
		}

		syncReceived = true;
	}

	/**
	 * Convert an OBM event in a calendar of type
	 * com.funambol.common.pim.calendar.Calendar
	 * 
	 * @param obmevent
	 * @param type
	 * @return
	 */
	private com.funambol.common.pim.calendar.Calendar obmEventToFoundationCalendar(
			Event obmevent, String type) {

		com.funambol.common.pim.calendar.Calendar calendar = new com.funambol.common.pim.calendar.Calendar();
		com.funambol.common.pim.calendar.CalendarContent event = null;
		event = new com.funambol.common.pim.calendar.Event();
		calendar.setEvent((com.funambol.common.pim.calendar.Event) event);

		event.getUid().setPropertyValue(obmevent.getUid());
		/*
		 * this.uid = uid; this.owner = owner; this.priority = priority;
		 * this.classification = classification; this.allday = allday; this.date =
		 * date; this.duration = duration; this.title = title; this.description =
		 * description; this.category = category; this.location = location;
		 * this.attendees = attendees; this.recurrence = recurrence;
		 */

		event.setAllDay(new Boolean(obmevent.isAllday()));

		Date dstart = obmevent.getDate().getTime();

		event.getDtStart()
				.setPropertyValue(CalendarHelper.getUTCFormat(dstart));

		java.util.Calendar temp = java.util.Calendar.getInstance();
		temp.setTime(dstart);
		temp.add(java.util.Calendar.SECOND, obmevent.getDuration());
		Date dend = temp.getTime();

		event.getDtEnd().setPropertyValue(CalendarHelper.getUTCFormat(dend));

		event.getSummary().setPropertyValue(obmevent.getTitle());
		event.getDescription().setPropertyValue(obmevent.getDescription());

		event.getCategories().setPropertyValue(obmevent.getCategory());
		event.getLocation().setPropertyValue(obmevent.getLocation());

		if (obmevent.getClassification() == 1) {
			event.getAccessClass().setPropertyValue("2"); // olPrivate
		} else {
			event.getAccessClass().setPropertyValue("0"); // olNormal
		}
		event.setBusyStatus(new Short((short) 2)); // olBusy

		event.getPriority().setPropertyValue(
				Helper.getPriority(obmevent.getPriority().intValue()));
		event.getStatus().setPropertyValue("Tentative");

		/*
		 * XTag classification = new XTag();
		 * classification.setXTagValue("Classification");
		 * classification.getXTag().setPropertyValue("2");
		 * event.addXTag(classification);
		 */

		EventRecurrence obmrec = obmevent.getRecurrence();
		if (!obmrec.getKind().equals("none")) {
			event.setRecurrencePattern(CalendarHelper.getRecurrence(dstart,
					dend, obmrec));
		}

		return calendar;
	}

	/**
	 * Convert a calendar of type com.funambol.common.pim.calendar.Calendar in
	 * an OBM event
	 * 
	 * @param calendar
	 * @param type
	 * @return
	 */
	private Event foundationCalendarToObmEvent(
			com.funambol.common.pim.calendar.Calendar calendar, String type) {

		Event event = new Event();
		com.funambol.common.pim.calendar.CalendarContent foundation = calendar
				.getCalendarContent();

		if (foundation.getUid() != null
				&& foundation.getUid().getPropertyValueAsString() != "") {
			event.setUid(foundation.getUid().getPropertyValueAsString());
		}

		event.setAllday(foundation.getAllDay().booleanValue());

		Date dstart = CalendarHelper.getDateFromUTCString(foundation
				.getDtStart().getPropertyValueAsString());
		java.util.Calendar cstart = java.util.Calendar.getInstance();
		cstart.setTime(dstart);

		event.setDate(cstart);

		Date dend = CalendarHelper.getDateFromUTCString(foundation.getDtEnd()
				.getPropertyValueAsString());
		if (dend.getTime() != dstart.getTime()) {
			event
					.setDuration((int) ((dend.getTime() - dstart.getTime()) / 1000));
		} else {
			event.setDuration(0);
		}
		event.setTitle(foundation.getSummary().getPropertyValueAsString());
		event.setDescription(foundation.getDescription()
				.getPropertyValueAsString());

		event.setCategory(CalendarHelper.getOneCategory(foundation
				.getCategories().getPropertyValueAsString()));
		event.setLocation(foundation.getLocation().getPropertyValueAsString());

		event.setPriority(Helper.getPriorityFromFoundation(foundation
				.getPriority().getPropertyValueAsString()));

		if (Helper.nullToEmptyString(
				foundation.getAccessClass().getPropertyValueAsString()).equals(
				"0")) { // olNormal
			event.setClassification(0); // public
		} else {
			event.setClassification(1); // private
		}

		EventRecurrence recurrence = null;
		if (foundation.isRecurrent()) {
			recurrence = CalendarHelper.getRecurrenceFromFoundation(foundation
					.getRecurrencePattern(), dend);
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
