package fr.aliasource.obm.items.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.client.ISyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.CalendarHelper;
import fr.aliasource.funambol.utils.Helper;

/**
 * Maintains a connection to obm-sync through a {@link CalendarClient}.
 * 
 * Stores the result of the getSync call to provide updates and deletions
 * informations "on demand".
 * 
 * @author tom
 * 
 */
public class CalendarManager extends ObmManager {

	private CalendarClient binding;
	private String calendar;
	private String userEmail;
	private Map<String, Event> updatedRest = null;
	private List<String> deletedRest = null;

	private Log logger = LogFactory.getLog(getClass());

	public CalendarManager(String obmAddress) {

		CalendarLocator calendarLocator = new CalendarLocator();
		binding = calendarLocator.locate(obmAddress.replace("/Calendar", ""));
	}

	public String getCalendar() {
		return calendar;
	}

	public void setCalendar(String calendar) {
		this.calendar = calendar;
	}

	public CalendarClient getBinding() {
		return binding;
	}

	public void initUserEmail() throws OBMException {
		try {
			userEmail = binding.getUserEmail(token);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
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

	public List<String> getDeletedItemKeys(Timestamp since) throws OBMException {

		if (!syncReceived) {
			getSync(since);
		}

		ArrayList<String> ret = new ArrayList<String>(deletedRest.size());
		ret.addAll(deletedRest);
		return ret;
	}

	public List<String> getUpdatedItemKeys(Timestamp since) throws OBMException {
		if (!syncReceived) {
			getSync(since);
		}
		List<String> keys = new LinkedList<String>();
		keys.addAll(updatedRest.keySet());
		return keys;
	}

	public com.funambol.common.pim.calendar.Calendar getItemFromId(String key,
			String type) throws OBMException {

		Event event = null;

		event = (Event) updatedRest.get(key);

		if (event == null) {
			logger.info(" item " + key
					+ " not found in updated -> get from sever");
			try {
				event = binding.getEventFromId(token, calendar, key);
			} catch (AuthFault e) {
				throw new OBMException(e.getMessage());
			} catch (ServerFault e) {
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
					|| event.getAttendees().size() == 1) {
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
		}

		if (c == null) {
			return null;
		} else {
			return obmEventToFoundationCalendar(c, type);
		}
	}

	public com.funambol.common.pim.calendar.Calendar addItem(
			com.funambol.common.pim.calendar.Calendar event, String type)
			throws OBMException {

		Event evt = null;

		try {
			Event forCreate = foundationCalendarToObmEvent(event, type);
			String uid = binding.createEvent(token, calendar, forCreate);
			evt = binding.getEventFromId(token, calendar, uid);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		}

		if (evt == null) {
			return null;
		} else {
			return obmEventToFoundationCalendar(evt, type);
		}
	}

	public List<String> getEventTwinKeys(
			com.funambol.common.pim.calendar.Calendar event, String type)
			throws OBMException {

		Event evt = foundationCalendarToObmEvent(event, type);

		if (evt == null) {
			return new LinkedList<String>();
		}

		try {
			evt.setUid(null);
			return binding.getEventTwinKeys(token, calendar, evt).getKeys();
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

		EventChanges sync = null;
		// get modified items
		try {
			sync = binding.getSync(token, calendar, d);
		} catch (AuthFault e) {
			throw new OBMException(e.getMessage());
		} catch (ServerFault e) {
			throw new OBMException(e.getMessage());
		}
		logger.info("getSync(" + calendar + ", " + d + " (since == " + since
				+ ")) => upd: " + sync.getUpdated().length + " del: "
				+ sync.getRemoved().length);
		Event[] updated = new Event[0];
		if (sync.getUpdated() != null) {
			updated = sync.getUpdated();
		}
		String[] deleted = new String[0];
		if (sync.getRemoved() != null) {
			deleted = sync.getRemoved();
		}

		// remove refused events and private events
		updatedRest = new HashMap<String, Event>();
		deletedRest = new ArrayList<String>();
		String user = token.getUser();

		for (Event e : updated) {
			logger.info("getSync: " + e.getTitle() + ", d: " + e.getDate());
			if (e.getPrivacy() == 1
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

		logger
				.info("bd -> pda - obmToFound: " + obmevent.getTitle()
						+ " date: " + obmevent.getDate() + " "
						+ obmevent.getDuration());
		Date dstart = obmevent.getDate();

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
		logger.info("computed dt end: " + dend);

		if (obmevent.getAlert() != -1 && obmevent.getAlert() != 0) {
			com.funambol.common.pim.calendar.Reminder remind = new com.funambol.common.pim.calendar.Reminder();

			remind.setMinutes(obmevent.getAlert() / 60);
			remind.setActive(true);
			event.setReminder(remind);

		} else {
			com.funambol.common.pim.calendar.Reminder remind = new com.funambol.common.pim.calendar.Reminder();
			remind.setActive(false);
			event.setReminder(remind);
		}
		/*
		 * logger.info("alert import:"+event.getReminder()); logger.info("alert
		 * import:"+obmevent.getAlert());
		 */
		event.setAllDay(new Boolean(obmevent.isAllday()));

		event.getSummary().setPropertyValue(obmevent.getTitle());
		event.getDescription().setPropertyValue(obmevent.getDescription());

		event.getCategories().setPropertyValue(obmevent.getCategory());
		event.getLocation().setPropertyValue(obmevent.getLocation());

		if (obmevent.getPrivacy() == 1) {
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

		com.funambol.common.pim.calendar.Event foundation = calendar.getEvent();

		if (foundation != null) {
			Event event = fillObmEventWithVEvent(calendar, foundation);
			return event;
		} else {
			logger
					.warn("Received ICalendar does not contain a VEVENT, VTODO ?");
			return null;
		}

	}

	private Event fillObmEventWithVEvent(
			com.funambol.common.pim.calendar.Calendar calendar,
			com.funambol.common.pim.calendar.Event foundation) {
		Event event = new Event();
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

		String prodId = "";
		if (calendar.getProdId() != null) {
			prodId = calendar.getProdId().getPropertyValueAsString();
			logger.info("prodId: " + prodId);
		}

		Date dstart = parseStart(prodId, foundation, event);
		Date dend = parseEnd(prodId, foundation, event);

		if (dend.getTime() != dstart.getTime()) {
			int fix = 0;
			// le rdv s'affiche sur 1 jour de plus dans obm si la duration
			// fait
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

		if (foundation.getReminder() != null
				&& foundation.getReminder().getMinutes() != 0) {
			event.setAlert(foundation.getReminder().getMinutes() * 60);
		} else {
			event.setAlert(0);
		}

		logger.info("alert export : " + event.getAlert());

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
			event.setPrivacy(0); // public
		} else {
			event.setPrivacy(1); // private
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

		if (foundation.getReminder() != null)
			logger.info("alert Reminder:" + foundation.getReminder());

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
		event.setDate(utcDate);
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

	@Override
	protected ISyncClient getSyncClient() {
		return binding;
	}
}
