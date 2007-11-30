package fr.aliasource.obm.items.manager;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private Log logger = LogFactory.getLog(getClass());

	public CalendarManager(String obmAddress) {

		try {
			CalendarServiceLocator calendarLocator = new CalendarServiceLocator();
			calendarLocator.setCalendarEndpointAddress(obmAddress);
			CalendarBindingStub calendarBinding = (CalendarBindingStub) calendarLocator
					.getCalendar();
			binding = calendarBinding;
		} catch (ServiceException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void initRestriction(int restrictions) {
		this.restrictions = restrictions;
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
			logger.info("getSync: " + e.getTitle() + ", d: "
					+ e.getDate().getTime());
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
		com.funambol.common.pim.calendar.CalendarContent event = new com.funambol.common.pim.calendar.Event();
		calendar.setEvent((com.funambol.common.pim.calendar.Event) event);

		event.getUid().setPropertyValue(obmevent.getUid());

		logger.info("bd -> pda - obmToFound: " + obmevent.getTitle()
				+ " date: " + obmevent.getDate().getTime());
		Date dstart = obmevent.getDate().getTime();

		logger.info("bd -> pda - utcFormat : "
				+ CalendarHelper.getUTCFormat(dstart));

		Date dend = null;
		if (!obmevent.isAllday()) {
			event.getDtStart().setPropertyValue(
					CalendarHelper.getUTCFormat(dstart));

			java.util.Calendar temp = java.util.Calendar.getInstance();
			temp.setTime(dstart);
			temp.add(java.util.Calendar.SECOND, obmevent.getDuration());
			dend = temp.getTime();

			event.getDtEnd()
					.setPropertyValue(CalendarHelper.getUTCFormat(dend));
		} else {
			java.util.Calendar temp = java.util.Calendar.getInstance();
			temp.setTime(dstart);
			temp.set(Calendar.HOUR_OF_DAY, 0);
			temp.set(Calendar.MINUTE, 0);
			temp.set(Calendar.SECOND, 0);

			event.getDtStart().setPropertyValue(
					CalendarHelper.getUTCFormat(temp.getTime()));

			temp.add(java.util.Calendar.SECOND, (int) (86400 * Math
					.ceil(((float) obmevent.getDuration()) / 86400)));
			dend = temp.getTime();

			event.getDtEnd()
					.setPropertyValue(CalendarHelper.getUTCFormat(dend));
		}

		event.setAllDay(new Boolean(obmevent.isAllday()));

		event.getSummary().setPropertyValue(obmevent.getTitle());
		event.getDescription().setPropertyValue(obmevent.getDescription());

		event.getCategories().setPropertyValue(obmevent.getCategory());
		event.getLocation().setPropertyValue(obmevent.getLocation());

		if (obmevent.getClassification() == 1) {
			event.getAccessClass().setPropertyValue(new Short((short) 2)); // olPrivate
		} else {
			event.getAccessClass().setPropertyValue(new Short((short) 0)); // olNormal
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
		event.setMileage(new Integer(0));

		return calendar;
	}

	/**
	 * Convert a calendar of type com.funambol.common.pim.calendar.Calendar in
	 * an OBM event
	 * 
	 * @param calendar
	 * @param type
	 * @param allDay
	 * @return
	 */
	private Event foundationCalendarToObmEvent(
			com.funambol.common.pim.calendar.Calendar calendar, String type) {

		Event event = new Event();
		com.funambol.common.pim.calendar.Event foundation = calendar.getEvent();

		if (foundation.getUid() != null
				&& foundation.getUid().getPropertyValueAsString() != "") {
			event.setUid(foundation.getUid().getPropertyValueAsString());
		}

		event.setAllday(foundation.getAllDay().booleanValue());

		if (foundation.getDtStamp() != null) {
			logger.info("dtstamp: "
					+ foundation.getDtStamp().getPropertyValue());
		}
		if (foundation.getDuration() != null) {
			logger.info("duration: "
					+ foundation.getDuration().getPropertyValue());
		}

		List xtags = foundation.getXTags();
		if (xtags != null) {
			for (Object o : xtags) {
				logger.info("     - xtag: '" + o + "'");
			}
		}

		String prodId = "";
		if (calendar.getProdId() != null) {
			prodId = calendar.getProdId().getPropertyValueAsString();
		}
		logger.info("prodId: " + prodId);
		Date dstart = parseStart(prodId, foundation, event);
		Date dend = parseEnd(prodId, foundation, event);

		if (dend.getTime() != dstart.getTime()) {
			int fix = 0;
			// le rdv s'affiche sur 1 jour de plus dans obm si la duration fait
			// tomber la date de fin sur minuit
			if (foundation.isAllDay()
					&& ((dend.getTime() - dstart.getTime()) % 86400) == 0) {
				fix = 1;
			}
			event
					.setDuration((int) ((dend.getTime() - dstart.getTime()) / 1000)
							- fix);
		} else {
			event.setDuration(3600);
		}
		if (foundation.getSummary() != null) {
			event.setTitle(foundation.getSummary().getPropertyValueAsString());
		} else {
			event.setTitle("[Sans titre]");
		}

		if (foundation.getDescription() != null) {
			event.setDescription(foundation.getDescription()
					.getPropertyValueAsString());
		}

		if (foundation.getCategories() != null) {
			event.setCategory(CalendarHelper.getOneCategory(foundation
					.getCategories().getPropertyValueAsString()));
		}

		if (foundation.getLocation() != null) {
			event.setLocation(foundation.getLocation()
					.getPropertyValueAsString());
		}

		if (foundation.getPriority() != null) {
			event.setPriority(Helper.getPriorityFromFoundation(foundation
					.getPriority().getPropertyValueAsString()));
		} else {
			event.setPriority(new Integer(1));
		}

		if (foundation.getAccessClass() != null
				&& Helper.nullToEmptyString(
						foundation.getAccessClass().getPropertyValueAsString())
						.equals("0")) { // olNormal
			event.setClassification(0); // public
		} else {
			event.setClassification(1); // private
		}

		EventRecurrence recurrence = null;
		if (foundation.isRecurrent()) {
			recurrence = CalendarHelper.getRecurrenceFromFoundation(foundation
					.getRecurrencePattern(), dend, foundation.isAllDay());
		} else {
			recurrence = new EventRecurrence();
			recurrence.setKind("none");
			recurrence.setDays("");
			recurrence.setFrequence(1);
		}
		event.setRecurrence(recurrence);

		return event;
	}

	/**
	 * bb hack : on ajoute 1j pour les blackberry. "le 19 à minuit GMT" est
	 * converti en "le 18" par les classes funambol.
	 * 
	 * @param prodId
	 * @param foundation
	 * @param event
	 * @return
	 */
	private Date parseStart(String prodId,
			com.funambol.common.pim.calendar.Event foundation, Event event) {
		String dtStart = foundation.getDtStart().getPropertyValueAsString();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Date utcDate = CalendarHelper.getDateFromUTCString(dtStart);
		cal.setTime(utcDate);

		if (foundation.getAllDay() && "Blackberry".equals(prodId)) {

			// logger.info("bb detected, adding 1 day to dtstart");
			// cal.add(Calendar.DAY_OF_MONTH, 1);
			// logger.info("utcDate: " + utcDate + " prev dtstart: " + dtStart
			// + " new dtstart: " + cal.getTime());
		}
		event.setDate(cal);
		return cal.getTime();
	}

	private Date parseEnd(String prodId,
			com.funambol.common.pim.calendar.Event foundation, Event event) {
		String dtEnd = foundation.getDtEnd().getPropertyValueAsString();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Date utcDate = CalendarHelper.getDateFromUTCString(dtEnd);
		cal.setTime(utcDate);

		if (foundation.getAllDay() && "Blackberry".equals(prodId)) {
			//
			// logger.info("bb detected, adding 1 day to dtend");
			// cal.add(Calendar.DAY_OF_MONTH, 1);
			// logger.info("utcDate: " + utcDate + " prev dtend: " + dtEnd
			// + " new dtend: " + cal.getTime());
		}
		return cal.getTime();
	}
}
