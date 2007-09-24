package fr.aliasource.funambol.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.calendar.Event;
import com.funambol.common.pim.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.calendar.RecurrencePattern;
import com.funambol.common.pim.calendar.RecurrencePatternException;
import com.funambol.common.pim.calendar.Reminder;
import com.funambol.common.pim.calendar.Task;
import com.funambol.common.pim.common.Converter;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.common.XTag;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.model.VCalendarContent;
import com.funambol.common.pim.model.VEvent;
import com.funambol.common.pim.model.VTodo;
import com.funambol.common.pim.utility.TimeUtils;

/**
 * This object is a converter from CalendarContent (Event or Task) to VCalendar
 * and from VCalendar to CalendarContent.
 * 
 * @see Converter
 * @version $Id: VCalendarContentConverter.java,v 1.1.1.1 2007/08/14 21:43:48
 *          stefano_fornari Exp $
 */
public class MyCalContentConverter extends VCalendarConverter {

	// ----------------------------------------------------------------
	// Constants

	private static final short SENSITIVITY_NORMAL = 0;
	private static final short SENSITIVITY_PERSONAL = 1;
	private static final short SENSITIVITY_PRIVATE = 2;
	private static final short SENSITIVITY_CONFIDENTIAL = 3;
	private static final String CLASS_PUBLIC = "PUBLIC";
	private static final String CLASS_PRIVATE = "PRIVATE";
	private static final String CLASS_CONFIDENTIAL = "CONFIDENTIAL";
	private static final String CLASS_CUSTOM = "X-PERSONAL";

	private static final String[] WEEK = { "SU", // NB: Sunday needs be the
			// first day of the week for
			// the mask
			"MO", // composition algorithm to work
			"TU", "WE", "TH", "FR", "SA" };

	private static final String DAILY = "DAILY";
	private static final String WEEKLY = "WEEKLY";
	private static final String MONTHLY = "MONTHLY";
	private static final String YEARLY = "YEARLY";
	private static final String BYMONTH = "BYMONTH";
	private static final String BYDAY = "BYDAY";
	private static final String BYMONTHDAY = "BYMONTHDAY";
	private static final String INTERVAL = "INTERVAL";
	private static final String COUNT = "COUNT";
	private static final String UNTIL = "UNTIL";
	private static final String BYSETPOS = "BYSETPOS";
	private static final String FREQ = "FREQ";

	private final String ZERO = String.valueOf(RecurrencePattern.UNSPECIFIED);

	private Log logger = LogFactory.getLog(getClass());
	private String storedDtStart;
	private String storedDtEnd;

	// --------------------------------------------------------------
	// Constructor

	public MyCalContentConverter(TimeZone timezone, String charset) {
		super(timezone, charset);
	}

	// ----------------------------------------------------------- Public
	// Methods

	public VCalendarContent cc2vcc(CalendarContent cc)
			throws ConverterException {
		return cc2vcc(cc, false); // default: text/calendar (2.0)
	}

	/**
	 * Performs the CalendarContent-to-VCalendarContent conversion.
	 * 
	 * @param cc
	 *            the CalendarContent object to be converted
	 * @param xv
	 *            true if the VCalendarContent must be in text/x-vcalendar
	 *            format
	 * @return a VCalendarContent object
	 */
	public VCalendarContent cc2vcc(CalendarContent cc, boolean xv)
			throws ConverterException {

		VCalendarContent vcc;
		if (cc instanceof Event) {
			vcc = new VEvent();
		} else {
			vcc = new VTodo();
		}

		ArrayList properties = new ArrayList(15);

		properties.add(composeField("UID", cc.getUid()));
		// Shouldn't be necessary: the UID is already known at Calendar level

		properties.add(composeField("SUMMARY", cc.getSummary()));
		properties.add(composeField("DESCRIPTION", cc.getDescription()));
		properties.add(composeField("LOCATION", cc.getLocation()));
		properties.add(composeField("CATEGORIES", cc.getCategories()));

		Property pAC = cc.getAccessClass();
		if (pAC != null) {
			Object savedPropertyValue = pAC.getPropertyValue();
			String accessClass = accessClassFrom03((Short) savedPropertyValue);
			pAC.setPropertyValue(accessClass);
			properties.add(composeField("CLASS", cc.getAccessClass()));
			pAC.setPropertyValue(savedPropertyValue); // Restores the value
		}

		Property dtStart = cc.getDtStart();
		properties.add(composeDateTimeField("DTSTART", dtStart));
		if (cc instanceof Event) {
			Property pE = cc.getDtEnd();
			Object savedPropertyValue = null;
			if (pE != null) {
				savedPropertyValue = pE.getPropertyValue();
				String due = pE.getPropertyValueAsString();
				if (TimeUtils.isInAllDayFormat(due)) {
					due = TimeUtils.rollOneDay(due, true); // Rolls on
					pE.setPropertyValue(due);
				}
			}
			properties.add(composeDateTimeField("DTEND", pE));
			if (savedPropertyValue != null) {
				pE.setPropertyValue(savedPropertyValue); // Restores the
				// value
			}
		} else {
			properties.add(composeDateTimeField("DUE", cc.getDtEnd()));
		}
		// NB: We decided not to store the duration but only Start and End (Due)

		properties.add(composeField("PRIORITY", cc.getPriority()));
		properties.add(composeField("CONTACT", cc.getContact()));
		properties.add(composeField("URL", cc.getUrl()));
		properties.add(composeField("SEQUENCE", cc.getSequence()));
		properties.add(composeField("PALARM", cc.getPAlarm()));
		properties.add(composeField("DALARM", cc.getDAlarm()));
		properties.add(composeField("ORGANIZER", cc.getOrganizer()));
		properties.add(composeDateTimeField("DTSTAMP", cc.getDtStamp()));

		if (cc instanceof Event) {
			properties.add(composeField("TRANSP", ((Event) cc).getTransp()));
			properties.add(composeField("STATUS", cc.getStatus()));

		} else if (cc instanceof Task) {
			properties.add(composeField("PERCENT-COMPLETE", ((Task) cc)
					.getPercentComplete()));
			properties.add(composeDateTimeField("COMPLETED", ((Task) cc)
					.getDateCompleted()));
			if (("100".equals(((Task) cc).getPercentComplete()
					.getPropertyValueAsString()))
					|| ("1".equals(((Task) cc).getComplete()
							.getPropertyValueAsString()))) {
				properties
						.add(composeField("STATUS", new Property("COMPLETED")));
			} else {
				properties.add(composeField("STATUS", cc.getStatus()));
			}
		}

		properties.add(composeField("LAST-MODIFIED", cc.getLastModified()));
		properties.add(composeDateTimeField("DCREATED", cc.getCreated()));

		Reminder reminder = cc.getReminder();
		if (reminder != null && reminder.isActive()) {
			Object savedPropertyValue = reminder.getPropertyValue(); // null?
			reminder.setPropertyValue(extractAAlarmPropertyValue(dtStart,
					reminder)); // Temporarily changes the value
			properties.add(composeField("AALARM", reminder)); // A Reminder is
			// also a
			// Property
			reminder.setPropertyValue(savedPropertyValue); // Restores the
			// value
		}

		RecurrencePattern rp = cc.getRecurrencePattern();
		if (rp != null) {
			Object savedPropertyValue = rp.getPropertyValue(); // null?
			rp.setPropertyValue( // Temporarily changes the value
					extractRRulePropertyValue(rp, xv));
			properties.add(composeField("RRULE", rp)); // A RecurrencePattern
			// is also a Property
			rp.setPropertyValue(savedPropertyValue); // Restores the value
			properties.add(composeField("EXDATE", new Property(
					extractExDatePropertyValue(rp))));
			properties.add(composeField("RDATE", new Property(
					extractRDatePropertyValue(rp))));
		}

		if ((cc.getLatitude() != null) && (cc.getLongitude() != null)) {
			if ((cc.getLatitude().getPropertyValueAsString() != null)
					&& (cc.getLongitude().getPropertyValueAsString() != null)) {
				String geo = cc.getLatitude().getPropertyValueAsString() + ";"
						+ cc.getLongitude().getPropertyValueAsString();
				if (geo.length() > 1) { // If it's not just a semicolon
					Property tmp = cc.getLatitude();
					Object savedPropertyValue = tmp.getPropertyValue();
					tmp.setPropertyValue(geo); // Temporarily changes the value
					properties.add(composeField("GEO", tmp));
					tmp.setPropertyValue(savedPropertyValue); // Restores the
				} // value
			}
		}

		properties.add(composeField("X-FUNAMBOL-FOLDER", cc.getFolder()));

		for (int i = 0; i < properties.size(); i++) {
			if (properties.get(i) != null) {
				vcc
						.addProperty((com.funambol.common.pim.model.Property) properties
								.get(i));
			}
		}

		if (xv) {
			try {
				String priority19 = vcc.getProperty("PRIORITY").getValue();
				String priority13 = String.valueOf(importance19To13(Integer
						.parseInt(priority19)));
				vcc.getProperty("PRIORITY").setValue(priority13);
			} catch (Exception e) { // NumberFormatException,
				// NullPointerException
				// Goes on
			}
		}

		if (cc.getBusyStatus() != null) {
			vcc.addProperty("X-MICROSOFT-CDO-BUSYSTATUS", cc.getBusyStatus()
					.toString());
		}

		if (cc.getAllDay().booleanValue()) {
			vcc.addProperty("X-FUNAMBOL-ALLDAY", "1");
		} else {
			vcc.addProperty("X-FUNAMBOL-ALLDAY", "0");
		}

		if (cc.getMeetingStatus() != null) {
			vcc.addProperty("PARTSTAT", cc.getMeetingStatus().toString());
		}

		/*
		 * // @todo List ccentXTag = cc.getXTags(); for (int i=0; i<ccentXTag.size();
		 * i++){ //vcc.addProperty(composeFieldXTag(ccentXTag)); }
		 */

//		if (cc.getAllDay()) {
//			logger.info("setting dtend to dtstart for all-day compat with pda");
//			vcc.addProperty("DTEND", vcc.getProperty("DTSTART").getValue());
//		}
		
		// @todo Add Task-specific properties
		return vcc;

	}

	/**
	 * Performs the VCalendarContent-to-CalendarContent conversion.
	 * 
	 * @param cc
	 *            the VCalendarContent object to be converted
	 * @param xv
	 *            true if the text/x-vcalendar format must be used while
	 *            generating some properties of the VCalendar output object
	 * @return a CalendarContent object
	 */
	public CalendarContent vcc2cc(VCalendarContent vcc, boolean xv)
			throws ConverterException {

		CalendarContent cc;
		if (vcc instanceof VEvent) {
			cc = new Event();
		} else {
			cc = new Task();
		}

		// Property storedDtStart = decodeField(vcc.getProperty("DTSTART"));
		// Property storedDtEnd = decodeField(vcc.getProperty("DTEND"));

		List prop = vcc.getAllProperties();

		for (Object o : prop) {
			com.funambol.common.pim.model.Property p = (com.funambol.common.pim.model.Property) o;
			if ("DTSTART".equals(p.getName())) {
				storedDtStart = p.getValue();
			}
			if ("DTEND".equals(p.getName())) {
				storedDtEnd = p.getValue();
			}
		}

		cc.setDtStart(decodeField(vcc.getProperty("DTSTART")));
		cc.setDuration(decodeField(vcc.getProperty("DURATION")));

		Property pS;
		Property pE;
		if (vcc instanceof VEvent) {
			pS = decodeField(vcc.getProperty("DTSTART"));
			pE = decodeField(vcc.getProperty("DTEND"));
			if (pE != null) {
				String start = null;
				if (pS != null) {
					start = pS.getPropertyValueAsString();
				}
				String due = pE.getPropertyValueAsString();
				if (!(due.equals(start)) // This is for robustness's sake
						&& TimeUtils.isInAllDayFormat(due)) {
					due = TimeUtils.rollOneDay(due, false); // Rolls back
					pE.setPropertyValue(due);
				}
			}
		} else {
			pE = decodeField(vcc.getProperty("DUE"));
		}
		cc.setDtEnd(pE);

		cc.setDtStamp(decodeField(vcc.getProperty("DTSTAMP")));
		cc.setLastModified(decodeField(vcc.getProperty("LAST-MODIFIED")));
		cc.setCreated(decodeField(vcc.getProperty("DCREATED")));

		if (cc instanceof Task) {
			((Task) cc).setDateCompleted(decodeField(vcc
					.getProperty("COMPLETED")));
		}

		try {

			fixDateProperty(cc.getLastModified());
			fixDateProperty(cc.getCreated());

			if (cc instanceof Event) {

				fixEventDates(cc); // must be called before the RRULE and the
				// AALARM
				// properties are parsed

			} else if (cc instanceof Task) {

				fixTaskDates(cc); // must be called before the RRULE and the
				// AALARM
				// properties are parsed

			}
		} catch (Exception e) {
			throw new ConverterException("Error while fixing the dates", e);
		}

		cc.setUid(decodeField(vcc.getProperty("UID")));
		// Shouldn't be necessary: the UID is already known at Calendar level

		if (cc instanceof Event) {
			((Event) cc).setTransp(decodeField(vcc.getProperty("TRANSP")));
		}

		cc.setSummary(decodeField(vcc.getProperty("SUMMARY")));
		cc.setDescription(decodeField(vcc.getProperty("DESCRIPTION")));
		cc.setLocation(decodeField(vcc.getProperty("LOCATION")));
		cc.setStatus(decodeField(vcc.getProperty("STATUS")));
		cc.setCategories(decodeField(vcc.getProperty("CATEGORIES")));

		String accessClass;
		com.funambol.common.pim.model.Property tmpClass = vcc
				.getProperty("CLASS");
		if (tmpClass == null) {
			accessClass = null;
		} else {
			accessClass = tmpClass.getValue();
		}
		Property accessClassProperty = new Property();
		accessClassProperty.setPropertyValue(new Short(
				accessClassTo03(accessClass)));

		cc.setAccessClass(accessClassProperty);

		cc.setPriority(decodeField(vcc.getProperty("PRIORITY")));
		if (xv) {
			try {
				String priority13 = cc.getPriority().getPropertyValueAsString();
				String priority19 = String.valueOf(importance13To19(Integer
						.parseInt(priority13)));
				cc.getPriority().setPropertyValue(priority19);
			} catch (Exception e) { // NumberFormatException,
				// NullPointerException
				// Goes on
			}
		}
		cc.setContact(decodeField(vcc.getProperty("CONTACT")));
		cc.setUrl(decodeField(vcc.getProperty("URL")));
		cc.setSequence(decodeField(vcc.getProperty("SEQUENCE")));
		cc.setPAlarm(decodeField(vcc.getProperty("PALARM")));
		cc.setDAlarm(decodeField(vcc.getProperty("DALARM")));
		cc.setOrganizer(decodeField(vcc.getProperty("ORGANIZER")));

		Short busyStatus = decodeShortField(vcc
				.getProperty("X-MICROSOFT-CDO-BUSYSTATUS"));
		if (busyStatus != null) {
			cc.setBusyStatus(busyStatus);
		}

		Short meetingStatus = decodeShortField(vcc.getProperty("PARTSTAT"));
		if (meetingStatus != null) {
			cc.setMeetingStatus(meetingStatus);
		}

		Property geo1 = decodeField(vcc.getProperty("GEO"));
		Property geo2 = decodeField(vcc.getProperty("GEO"));
		if (geo1 != null) {
			StringTokenizer st = new StringTokenizer(geo1
					.getPropertyValueAsString(), ";");
			if (st.countTokens() == 2) {
				geo1.setPropertyValue(st.nextToken());
				cc.setLatitude(geo1);
				geo2.setPropertyValue(st.nextToken());
				cc.setLongitude(geo2);
			}
		}

		//
		// If the calendar is an all-day event or task but no device timezone
		// is set and there is a start date, the time interval between the start
		// date time and the aalarm time is computed and this difference is
		// applied to the corrected start time of the all-day event or task
		// (ie, midnight UTC) irrespective of any time zone consideration.
		// This modified aalarm time is saved in the DB as a UTC time, but
		// it's interpreted as a local time when it's retrieved (exactly as
		// start and end dates when the all-day flag is on).
		//
		Property aalarm = decodeField(vcc.getProperty("AALARM"));
		// @todo Implement AALARM for iCalendar (2.0)
		if (aalarm != null && aalarm.getPropertyValueAsString() != null) {

			Reminder reminder = null;
			Property dtstart = decodeField(vcc.getProperty("DTSTART"));

			if (cc.isAllDay() && timezone == null && dtstart != null) {
				reminder = convertAAlarmToReminderBasedOnMinutes(dtstart
						.getPropertyValueAsString(), cc.getDtStart()
						.getPropertyValueAsString(), aalarm
						.getPropertyValueAsString());

			} else {
				reminder = convertAAlarmToReminder(cc.isAllDay(), cc
						.getDtStart(), aalarm.getPropertyValueAsString());
			}
			cc.setReminder(reminder);
		}

		Property rrule = decodeField(vcc.getProperty("RRULE"));
		if (rrule != null && rrule.getPropertyValueAsString().length() != 0) {
			try {
				cc.setRecurrencePattern(getRecurrencePattern(cc.getDtStart()
						.getPropertyValueAsString(), cc.getDtEnd()
						.getPropertyValueAsString(), rrule
						.getPropertyValueAsString(), xv));

				Property rdate = decodeField(vcc.getProperty("RDATE"));
				if (rdate != null) {
					cc.getRecurrencePattern().getExceptions().addAll(
							getRDates(rdate.getPropertyValueAsString()));
				}

				Property exdate = decodeField(vcc.getProperty("EXDATE"));
				if (exdate != null) {
					cc.getRecurrencePattern().getExceptions().addAll(
							getExDates(exdate.getPropertyValueAsString()));
				}

			} catch (ConverterException ce) {
				cc.setRecurrencePattern(null); // Ignore parsing errors
			}
		}

		/*
		 * // @todo List ccentXTag = cc.getXTags(); for (int i=0; i<ccentXTag.size();
		 * i++){ //vcc.addProperty(composeFieldXTag(ccentXTag)); }
		 */

		if (cc instanceof Task) {
			((Task) cc).setPercentComplete(decodeField(vcc
					.getProperty("PERCENT-COMPLETE")));

			String status = null;
			if ((cc.getStatus() != null)) {
				status = cc.getStatus().getPropertyValueAsString();
			}
			if ((status != null) && (status.length() != 0)) {
				if ("COMPLETED".equalsIgnoreCase(status)) {
					((Task) cc).setComplete(new Property("1"));
				} else {
					((Task) cc).setComplete(new Property("0"));
				}
			}
		}

		cc.setFolder(decodeField(vcc.getProperty("X-FUNAMBOL-FOLDER")));
		logger.info("restoring dtstart & end : " + storedDtStart + " & "
				+ storedDtEnd);
		cc.setDtStart(new Property(storedDtStart));
		if (cc.isAllDay()) {
			cc.setDtEnd(new Property(storedDtStart));
		} else {
			cc.setDtEnd(new Property(storedDtEnd));
		}
		return cc;
	}

	// ---------------------------------------------------------- Private
	// Methods

	/**
	 * @return a representation of the iCalendar field X-PROP:
	 */
	private StringBuffer composeFieldXTag(List xTags) throws ConverterException {
		StringBuffer output = new StringBuffer();

		if ((xTags == null) || xTags.isEmpty()) {
			return output;
		}

		Property xtag = null;
		String value = null;

		int size = xTags.size();
		for (int i = 0; i < size; i++) {

			XTag xtagObj = (XTag) xTags.get(i);

			xtag = xtagObj.getXTag();
			value = (String) xtag.getPropertyValue();

			output.append(composeICalTextComponent(xtag, (String) xtagObj
					.getXTagValue()));
		}
		return output;
	}

	/**
	 * @return a representation of the field RRULE (version 1.0)
	 */
	private String composeFieldRrule(RecurrencePattern rrule) {

		StringBuffer result = new StringBuffer(60); // Estimate 60 is needed

		if (rrule != null) {

			addXParams(result, rrule);

			result.append(rrule.getTypeDesc()).append(rrule.getInterval());

			if (rrule.getInstance() < 0) {
				result.append(" " + (-rrule.getInstance()) + "-");
			} else if (rrule.getInstance() > 0) {
				result.append(" " + rrule.getInstance() + "+");
			} // else, it's zero and nothing's to be done

			for (int i = 0; i < rrule.getDayOfWeek().size(); i++) {
				result.append(' ').append(rrule.getDayOfWeek().get(i));
			}
			if (rrule.getDayOfMonth() != 0 && !"YM".equals(rrule.getTypeDesc())) {
				result.append(' ').append(rrule.getDayOfMonth());
			}
			if (rrule.getMonthOfYear() != 0) {
				result.append(' ').append(rrule.getMonthOfYear());
			}
			if (rrule.getOccurrences() != -1 && rrule.isNoEndDate()) {
				result.append(" #").append(rrule.getOccurrences());
			} else {
				if (rrule.isNoEndDate()) {
					result.append(" #0"); // forever
				}
			}
			if (!rrule.isNoEndDate() && rrule.getEndDatePattern() != null
					&& !rrule.getEndDatePattern().equals("")) {
				try {
					result.append(' ').append(
							handleConversionToLocalDate(rrule
									.getEndDatePattern(), timezone));
				} catch (ConverterException ce) {
					// This should never happen!
				}

			}
		}
		return result.toString();
	}

	/**
	 * @return a representation of the event field AALARM
	 */
	private String extractAAlarmPropertyValue(Property dtStart,
			Reminder reminder) throws ConverterException {

		StringBuffer result = new StringBuffer(60); // 60 has been estimated OK

		/*
		 * String typeParam = reminder.getType(); if (typeParam != null) {
		 * result.append(";TYPE=").append(typeParam); } String
		 * valueParam=reminder.getValue(); if (valueParam!=null) {
		 * result.append(";VALUE=").append(valueParam); } addXParams(result,
		 * reminder);
		 */

		java.util.Date dateStart = null;
		SimpleDateFormat formatter = new SimpleDateFormat();

		String dtStartVal = (String) dtStart.getPropertyValue();
		String dtAlarmVal = reminder.getTime();
		if (dtAlarmVal == null || dtAlarmVal.length() == 0) {
			if (dtStartVal == null || dtStartVal.length() == 0) {
				return null;
			}
			try {
				dtStartVal = handleConversionToUTCDate(dtStartVal, timezone);
				formatter.applyPattern(TimeUtils.getDateFormat(dtStartVal));
				dateStart = formatter.parse(dtStartVal);
			} catch (Exception e) {
				throw new ConverterException("Error while parsing start date "
						+ "during AALARM property conversion.", e);
			}

			java.util.Calendar calAlarm = java.util.Calendar.getInstance();
			calAlarm.setTime(dateStart);
			calAlarm.add(java.util.Calendar.MINUTE, -reminder.getMinutes());

			Date dtAlarm = calAlarm.getTime();
			formatter.applyPattern("yyyyMMdd'T'HHmmss'Z'");
			dtAlarmVal = formatter.format(dtAlarm);
		}

		result.append(handleConversionToLocalDate(dtAlarmVal, timezone))
				.append(';');
		if (reminder.getInterval() != 0) {
			result.append(TimeUtils.getIso8601Duration(String.valueOf(reminder
					.getInterval())));
		}
		result.append(';').append(reminder.getRepeatCount());

		result.append(';');
		if (reminder.getSoundFile() != null) {
			result.append(reminder.getSoundFile());
		}

		return result.toString();
	}

	/**
	 * @return a representation of the ccent field LAST-MODIFIED:
	 */
	private String composeFieldLastModified(Property lastModified)
			throws ConverterException {

		StringBuffer result = new StringBuffer(60); // Estimate 60 is needed

		if (lastModified.getPropertyValue() != null) {

			addXParams(result, lastModified);

			String lastModifiedVal = (String) lastModified.getPropertyValue();

			lastModifiedVal = handleConversionToLocalDate(lastModifiedVal,
					timezone);

			result.append(lastModifiedVal);
		}
		return result.toString();
	}

	private String extractRRulePropertyValue(RecurrencePattern rp, boolean xv) {

		if (xv) {
			return composeFieldRrule(rp);
		}

		StringBuffer result = new StringBuffer(99); // 99 has been estimated OK

		String type = null;
		switch (rp.getTypeId()) {
		case RecurrencePattern.TYPE_DAYLY: // sic
			type = DAILY;
			break;
		case RecurrencePattern.TYPE_WEEKLY:
			type = WEEKLY;
			break;
		case RecurrencePattern.TYPE_MONTHLY:
		case RecurrencePattern.TYPE_MONTH_NTH:
			type = MONTHLY;
			break;
		case RecurrencePattern.TYPE_YEARLY:
		case RecurrencePattern.TYPE_YEAR_NTH:
			type = YEARLY;
			break;
		default:
			return null;
		}
		appendToStringBuffer(result, FREQ, type);

		appendToStringBuffer(result, INTERVAL, String.valueOf(rp.getInterval()));

		appendToStringBuffer(result, BYMONTH, String.valueOf(rp
				.getMonthOfYear()));

		appendToStringBuffer(result, BYMONTHDAY, String.valueOf(rp
				.getDayOfMonth()));

		appendToStringBuffer(result, BYSETPOS, String.valueOf(rp.getInstance()));

		int occurrences = rp.getOccurrences();
		if (occurrences != -1) { // means unspecified: see RecurrencePattern
			appendToStringBuffer(result, COUNT, String.valueOf(occurrences));
		} else {
			if (!rp.isNoEndDate()) {
				try {
					appendToStringBuffer(result, UNTIL,
							handleConversionToLocalDate(String.valueOf(rp
									.getEndDatePattern()), timezone));
				} catch (ConverterException ce) {
					// This should nccer happen!

				}
			}
		}

		String weekDays = "";
		for (short mask = rp.getDayOfWeekMask(), j = 0; mask > 0; // Until the
		// mask has
		// been
		// eaten up
		mask /= 2, j++) { // Shifts the mask, looks for the next weekday

			if (mask % 2 == 1) { // Bingo!
				weekDays += "," + WEEK[j]; // Adds the correct weekday symbol
			}
		}
		if (weekDays.length() > 0) {
			appendToStringBuffer(result, BYDAY, weekDays.substring(1)); // Discards
			// the
			// first
			// ","
		}

		return result.toString();

	}

	/**
	 * @see extractExceptionsAsSCSVs(RecurrencePattern, boolean)
	 */
	private String extractExDatePropertyValue(RecurrencePattern rp) {
		return extractExceptionsAsSCSVs(rp, false);
	}

	/**
	 * @see extractExceptionsAsSCSVs(RecurrencePattern, boolean)
	 */
	private String extractRDatePropertyValue(RecurrencePattern rp) {
		return extractExceptionsAsSCSVs(rp, true);
	}

	/**
	 * Extracts the recurrence exceptions from a given RecurrencePattern object
	 * and returns them as a string of semicolon-separated-values.
	 * 
	 * @param rp
	 *            the recurrence pattern that contains the exception list
	 * @param rdate
	 *            true if only the addition exceptions are to be extracted,
	 *            false if only the deletion exceptions are to be extracted
	 * @return the list in the proper vCalendar/iCalendar format
	 */
	private String extractExceptionsAsSCSVs(RecurrencePattern rp, boolean rdate) {

		List exceptions = rp.getExceptions();
		String result = "";

		for (int i = 0; i < exceptions.size(); i++) {
			ExceptionToRecurrenceRule etrr = (ExceptionToRecurrenceRule) exceptions
					.get(i);
			if (etrr.isAddition() == rdate) {
				if (result.length() > 0) { // always but the first time
					result += ';';
				}
				try {
					result += handleConversionToLocalDate(etrr.getDate(),
							timezone);
				} catch (ConverterException ce) {
					result += etrr.getDate(); // Keeps it as it is
				}
			}
		}

		if (result.length() == 0) {
			return null;
		}
		return result;
	}

	private void appendToStringBuffer(StringBuffer sb, String key, String value) {

		if (value != null && !ZERO.equals(value)) {
			if (!key.equals(FREQ)) { // FREQ is the first piece of the RRULE
				sb.append(';');
			}
			sb.append(key).append('=').append(value);
		}
	}

	private boolean isEndDateOrDuration(String token) { // Just for RRULE 1.0
		if (token.startsWith("#")) { // it's a duration
			return true;
		}
		if (token.length() >= 8) { // it's an end date
			return true;
		}
		return false;
	}

	private short instanceModifierToInt(String modifier) { // Irrespective of
		// the
		if (modifier.endsWith("-")) { // version
			return (short) -Short.parseShort(modifier.substring(0, modifier
					.length() - 1)); // Cuts last char
		} else if (modifier.startsWith("-")) {
			return (short) -Short.parseShort(modifier.substring(1)); // Cuts
			// first
			// char
		} else if (modifier.endsWith("+")) {
			return (short) Short.parseShort(modifier.substring(0, modifier
					.length() - 1)); // Cuts last char
		} else {
			return Short.parseShort(modifier); // Takes it all
		}
	}

	private boolean isAnInstanceModifier(String s) {
		switch (s.charAt(0)) {
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return true;
		default:
			return false;
		}
	}

	private RecurrencePattern getRecurrencePattern(String startDate,
			String endDate, String rrule, boolean xv) throws ConverterException {

		if (rrule == null || rrule.length() == 0) {
			return null;
		}

		String startDatePattern;

		if (startDate == null || startDate.length() == 0) {
			if (endDate == null) {
				return null;
			}
			startDatePattern = endDate;
		} else {
			startDatePattern = startDate;
		}

		HashMap map = new HashMap(7);
		ArrayList week = null;
		short type = -1;
		int occurences = -1; // means unspecified: see RecurrencePattern
		short instance = 0;
		int interval;
		short dayOfWeekMask = 0;
		short dayOfMonth = 0;
		short monthOfYear = 0;
		String endDatePattern = null;

		if (xv) {
			try {
				StringTokenizer st = new StringTokenizer(rrule, " ");
				String frequencyInterval = st.nextToken();
				String durationOrEndDate = null;
				int c = 2; // Default: frequency is 2-character long

				if (frequencyInterval.startsWith("D")) {
					type = RecurrencePattern.TYPE_DAYLY; // sic
					c = 1; // Shorter than default
					// No modifier... moves on to end date or duration

				} else if (frequencyInterval.startsWith("W")) {
					type = RecurrencePattern.TYPE_WEEKLY;
					c = 1; // Shorter than default
					week = new ArrayList(7); // Big enough!
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (isEndDateOrDuration(token)) {
							durationOrEndDate = token;
							break;
						} else {
							week.add(token);
						}
					}

				} else if (frequencyInterval.startsWith("MD")) {
					type = RecurrencePattern.TYPE_MONTHLY;
					String monthDay = null;
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (isEndDateOrDuration(token)) {
							durationOrEndDate = token;
							break;
						} else if (monthDay == null) { // Just the 1st one
							// found
							monthDay = token;
						}
					}
					if (monthDay != null) {
						dayOfMonth = instanceModifierToInt(monthDay);
					}

				} else if (frequencyInterval.startsWith("MP")) {
					type = RecurrencePattern.TYPE_MONTH_NTH;
					week = new ArrayList(7); // Big enough!
					String instanceModifier = "";
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (isEndDateOrDuration(token)) {
							durationOrEndDate = token;
							break;
						} else if (isAnInstanceModifier(token)) {
							instanceModifier = token;
						} else {
							week.add(new String(instanceModifier + token));
						}
					}

				} else if (frequencyInterval.startsWith("YD")) {
					type = RecurrencePattern.TYPE_YEAR_NTH;
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (isEndDateOrDuration(token)) {
							durationOrEndDate = token;
							break;
						}
					}
					// Month etc. are ignored and set below with rp.fix()

				} else if (frequencyInterval.startsWith("YM")) {
					type = RecurrencePattern.TYPE_YEARLY;
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (isEndDateOrDuration(token)) {
							durationOrEndDate = token;
							break;
						}
					}
					// Month etc. are ignored and set below with rp.fix()

				} else {
					throw new ConverterException("Error while parsing RRULE "
							+ "(1.0): frequency not recognized");
				}
				interval = Short.parseShort(frequencyInterval.substring(c));

				if (durationOrEndDate == null) { // If it's not been
					// retricced,
					if (!st.hasMoreTokens()) { // yet
						durationOrEndDate = "#2"; // Default: repeat twice
					} else {
						durationOrEndDate = st.nextToken();
					}
				}
				if (durationOrEndDate.startsWith("#")) { // it's a duration
					occurences = Short.parseShort(durationOrEndDate
							.substring(1));
					if (st.hasMoreTokens()) {
						// This is possible in theory. We ignore this case but
						// the specification prescribes to use whichccer
						// requirement is stricter among the duration and the
						// end date. We just use the first one we find.
					}
				} else { // it's an end date
					endDatePattern = handleConversionToUTCDate(
							durationOrEndDate, timezone);
					if (st.hasMoreTokens()) {
						// This is possible in theory. We ignore this case but
						// the specification prescribes to use whichccer
						// requirement is stricter among the duration and the
						// end date. We just use the first one we find.
					}
				}

			} catch (Exception e) {
				throw new ConverterException(
						"Error while parsing RRULE (1.0): " + e);
			}

		} else {
			try {
				StringTokenizer stSemiColon = new StringTokenizer(rrule, ";");
				while (stSemiColon.hasMoreTokens()) {
					StringTokenizer stEquals = new StringTokenizer(stSemiColon
							.nextToken(), "=");
					String key = stEquals.nextToken();
					StringTokenizer stComma = new StringTokenizer(stEquals
							.nextToken(), ",");
					ArrayList list = new ArrayList();
					while (stComma.hasMoreTokens()) {
						list.add(stComma.nextToken());
					}
					map.put(key, list);
				}
			} catch (Exception e) {
				throw new ConverterException(
						"Error while parsing RRULE (2.0): " + e);
			}

			interval = Short.parseShort(find(map, INTERVAL, true));
			monthOfYear = Short.parseShort(find(map, BYMONTH, true));
			dayOfMonth = Short.parseShort(find(map, BYMONTHDAY, true));
			instance = Short.parseShort(find(map, BYSETPOS, true));
			occurences = Short.parseShort(find(map, COUNT, true));
			endDatePattern = find(map, UNTIL, false);
			try {
				endDatePattern = handleConversionToUTCDate(endDatePattern,
						timezone);
			} catch (Exception e) {
				throw new ConverterException("Error while parsing RRULE (2.0):"
						+ " timezone-based conversion failed.");
			}
			Object obj = map.get(BYDAY);
			if (obj != null) {
				week = (ArrayList) obj;
			}

			String freq = find(map, FREQ, false);
			if (freq == null) {
				throw new ConverterException("Error while parsing RRULE (2.0):"
						+ " frequency not found");
			}
			if (freq.equals(DAILY)) {
				type = RecurrencePattern.TYPE_DAYLY; // sic

			} else if (freq.equals(WEEKLY)) {
				type = RecurrencePattern.TYPE_WEEKLY;

			} else if (freq.equals(MONTHLY)) {
				if (week == null) {
					type = RecurrencePattern.TYPE_MONTHLY;

				} else {
					type = RecurrencePattern.TYPE_MONTH_NTH;
				}

			} else if (freq.equals(YEARLY)) {
				if (week == null) {
					type = RecurrencePattern.TYPE_YEARLY;
				} else {
					type = RecurrencePattern.TYPE_YEAR_NTH;
				}
			}
			if (type == -1) { // is the default value
				throw new ConverterException("Error while parsing RRULE (2.0):"
						+ " frequency not found");
			}
		}

		if (occurences == 0) {
			occurences = -1; // means unspecified: see RecurrencePattern
		}

		boolean noEndDate = (endDatePattern == null);

		// Duplicates will be counted just once
		if (week != null) {
			short maskElement = 1;
			for (int j = 0; j < 7; // 7, because a week has 7 days
			j++, maskElement *= 2 // Sunday = 1, Monday = 2, Tuesday = 4 etc.
			) {
				for (int i = 0; i < week.size(); i++) {
					int pos = ((String) week.get(i)).indexOf(WEEK[j]);
					if (pos != -1) { // Bingo!
						dayOfWeekMask += maskElement;
						if (pos > 0) { // There's an instance marker, too
							if (instance == 0) { // it's not been set yet
								try { // Takes just the 1st modifier it finds
									instance = instanceModifierToInt(((String) week
											.get(i)).substring(0, pos));
								} catch (Exception e) {
									throw new ConverterException("Error while "
											+ "parsing RRULE's instance "
											+ "modifier.", e);
								}
							} else {
								// Currently, multi-instance recurrence patterns
								// are not supported.
							}
						}
						week.remove(i); // It won't be used any more
						break; // Time to look for the next week day
					}
				}
			}
		}

		RecurrencePattern rp = new RecurrencePattern(type, interval,
				monthOfYear, dayOfMonth, dayOfWeekMask, instance,
				startDatePattern, endDatePattern, noEndDate, occurences);

		try {
			rp.fix(); // If it lacks some information, it's extracted from the
			// start date
		} catch (RecurrencePatternException rpe) {
			throw new ConverterException("Error while fixing a newly parsed "
					+ "recurrence pattern that lacks some information.", rpe);
		}

		return rp;
	}

	/**
	 * @see getExceptionsAsList(String,boolean)
	 */
	private List getRDates(String rdate) {
		return getExceptionsAsList(rdate, true);
	}

	/**
	 * @see getExceptionsAsList(String,boolean)
	 */
	private List getExDates(String exdate) {
		return getExceptionsAsList(exdate, false);
	}

	/**
	 * Parses a string of semicolon-separated-values taken from the content of
	 * an EXDATE or RDATE vCalendar/iCalendar property and uses the parsed
	 * values to fill a list of ExceptionToRecurrenceRule istances.
	 * 
	 * @param scsv
	 *            the property value, as a String object
	 * @param rdate
	 *            true if the string comes from an RDATE property, false if it
	 *            comes from an EXDATE property
	 * @return an ArrayList of ExceptionToRecurrenceRule objects
	 */
	private List getExceptionsAsList(String scsv, boolean rdate) {

		List exceptions = new ArrayList();

		if (scsv == null || scsv.length() == 0) {
			return exceptions;
		}
		String[] token = scsv.split(";");

		for (int i = 0; token != null && i < token.length; i++) {
			try {
				//
				// Trimming the given date because some devices send it with a
				// space at the beginning
				//
				exceptions.add(new ExceptionToRecurrenceRule(rdate,
						handleConversionToUTCDate(token[i].trim(), timezone)));
			} catch (ConverterException ce) {
				// Skips this one
			}
		}
		return exceptions;
	}

	private String find(HashMap where, String what, boolean zeroIfTrouble) {
		try {
			return (String) ((ArrayList) where.get(what)).get(0);
		} catch (Exception e) {
			if (zeroIfTrouble)
				return ZERO;
			else
				return null;
		}
	}

	private void fixDateProperty(Property property) throws Exception {
		if (property == null || property.getPropertyValue() == null
				|| "".equals(property.getPropertyValueAsString())) {
			return;
		}
		String value = property.getPropertyValueAsString();
		property.setPropertyValue(handleConversionToUTCDate(value, timezone));
	}

	private void fixEventDates(CalendarContent cc) throws Exception {

		fixDateProperty(((Event) cc).getReplyTime());

		if (cc.getDtStart() == null) {
			cc.setDtStart(new Property());
		}
		if (cc.getDtEnd() == null) {
			cc.setDtEnd(new Property());
		}
		if (cc.getDuration() == null) {
			cc.setDuration(new Property());
		}

		String dtstart = cc.getDtStart().getPropertyValueAsString();
		String dtend = cc.getDtEnd().getPropertyValueAsString();
		String duration = cc.getDuration().getPropertyValueAsString();

		try {
			cc.setAllDay(Boolean.FALSE);

			//
			// Check if the event is an AllDay event
			// (yyyyMMdd or yyyy-MM-dd)
			//
			if (TimeUtils.isInAllDayFormat(dtstart)) {
				try {
					dtstart = TimeUtils.convertDateFromTo(dtstart,
							TimeUtils.PATTERN_YYYY_MM_DD);
				} catch (java.text.ParseException e) {
					throw new ConverterException("Error parsing date: "
							+ e.getMessage());
				}
				cc.getDtStart().setPropertyValue(dtstart);
				cc.setAllDay(Boolean.TRUE);
			} else {
				dtstart = handleConversionToUTCDate(dtstart, timezone);
				cc.getDtStart().setPropertyValue(dtstart);
			}

			//
			// Check if the event is an AllDay event
			//
			if (TimeUtils.isInAllDayFormat(dtend)) {
				dtend = TimeUtils.convertDateFromTo(dtend,
						TimeUtils.PATTERN_YYYY_MM_DD);
				cc.setAllDay(Boolean.TRUE);
			} else {
				dtend = handleConversionToUTCDate(dtend, timezone);
			}

			//
			// Compute End Date by Start Date and Duration
			//
			dtend = TimeUtils.getDTEnd(dtstart, duration, dtend, null);
			cc.getDtEnd().setPropertyValue(dtend);

			//
			// If the event is an all day check if there is the end date:
			// 1) if end date is null then set it with start date value.
			// 2) if end date is not into yyyy-MM-dd or yyyyMMdd format then
			// normalize it in yyyy-MM-dd format.
			//
			boolean startAllDay = TimeUtils.isInAllDayFormat(dtstart);
			boolean endAllDay = TimeUtils.isInAllDayFormat(dtend);

			if (startAllDay) {
				if (dtend == null) {
					dtend = dtstart;
				} else {
					if (!endAllDay) {
						try {
							dtend = TimeUtils.convertDateFromTo(dtend,
									TimeUtils.PATTERN_YYYY_MM_DD);
						} catch (java.text.ParseException e) {
							throw new ConverterException("Error parsing date: "
									+ e.getMessage());
						}
					}
				}
			}

			//
			// We have to check if the dates are not in the DayFormat but are
			// however
			// relative to an all day event.
			//
			if (!cc.getAllDay().booleanValue()) {
				boolean isAllDayEvent = false;

				//
				// Before to check the dates, we have to convert them in local
				// format
				// in order to have 00:00:00 as time for the middle night
				//
				String tmpDateStart = TimeUtils.convertUTCDateToLocal(dtstart,
						timezone);
				String tmpDateEnd = TimeUtils.convertUTCDateToLocal(dtend,
						timezone);

				isAllDayEvent = TimeUtils.isAllDayEvent(tmpDateStart,
						tmpDateEnd);

				if (isAllDayEvent) {

					//
					// Convert the dates in DayFormat
					//
					try {
						dtstart = TimeUtils.convertDateFromTo(tmpDateStart,
								TimeUtils.PATTERN_YYYY_MM_DD);

						dtend = TimeUtils.convertDateFromTo(tmpDateEnd,
								TimeUtils.PATTERN_YYYY_MM_DD);
					} catch (java.text.ParseException e) {
						throw new ParseException("Error parsing date: "
								+ e.getMessage(), e.getErrorOffset());
					}

					cc.getDtStart().setPropertyValue(dtstart);
					cc.getDtEnd().setPropertyValue(dtend);

					cc.setAllDay(Boolean.TRUE);
				} else {
					isAllDayCheckingDuration(cc);
				}
			}
		} catch (java.text.ParseException e) {
			throw new ConverterException("Error parsing date: "
					+ e.getMessage());
		}
	}

	private void fixTaskDates(CalendarContent cc) throws Exception {

		fixDateProperty(((Task) cc).getDateCompleted());

		if (cc.getDtStart() == null) {
			cc.setDtStart(new Property());
		}
		if (cc.getDtEnd() == null) {
			cc.setDtEnd(new Property());
		}
		if (cc.getDuration() == null) {
			cc.setDuration(new Property());
		}

		String dtstart = cc.getDtStart().getPropertyValueAsString();
		String dtend = cc.getDtEnd().getPropertyValueAsString();

		cc.setAllDay(Boolean.FALSE);

		if (dtend != null) {
			if (dtend.endsWith("T000000") || dtend.endsWith("T235900")) {
				if (dtstart == null || dtstart.endsWith("T000000")) {
					cc.setAllDay(Boolean.TRUE);
					dtend += 'Z';
					if (dtstart != null) {
						dtstart += 'Z';
					}
					return;
				}
			}
		} else if (dtstart != null) {
			//
			// dtend == null and dtstart != null ----> What to do ?
			//
			if (dtstart.endsWith("T000000")) {
				cc.setAllDay(Boolean.TRUE);
				dtstart += 'Z';
				return;
			}
		}

		//
		// No all day task
		//
		dtstart = handleConversionToUTCDate(dtstart, timezone);
		cc.getDtStart().setPropertyValue(dtstart);

		dtend = handleConversionToUTCDate(dtend, timezone);
		cc.getDtEnd().setPropertyValue(dtend);
	}

	/**
	 * Checks if the given dates are of an all day event checking if the
	 * duration is a multiple of 24 hour. The main problem is the end time is
	 * something like 23:59:59 so the difference is not 24 hour but 24 hour - 1
	 * second. Another problem is about the day of the event because the date
	 * can be shifted because we may not have the timezone of the device. BTW,
	 * in order to find the day, we don't need the timezone but we need to know
	 * just if the timezone is with an offset positive or negative. And to know
	 * it, we check the time of the dtEnd. <br/>If an all day event is detected,
	 * the properties are set in the given calendar.
	 * 
	 * KNOW ISSUE: this method fails with the timezone with an offset greater
	 * than 12 hours
	 * 
	 * @param dtStart
	 *            the date start
	 * @param dtEnd
	 *            the date start
	 * @throws Exception
	 *             if an error occurs
	 */
	public static void isAllDayCheckingDuration(CalendarContent cc)
			throws Exception {

		String dtStart = cc.getDtStart().getPropertyValueAsString();
		String dtEnd = cc.getDtEnd().getPropertyValueAsString();

		if (dtStart == null || dtStart.length() == 0) {
			cc.setAllDay(Boolean.FALSE);
			return;

		}

		if (dtEnd == null || dtEnd.length() == 0) {
			cc.setAllDay(Boolean.FALSE);
			return;

		}
		//
		// We replace end time 5900Z with 5959Z in order to check just 5959Z
		//
		dtEnd = dtEnd.replaceAll("5900Z", "5959Z");

		//
		// The date start must end with 00Z
		//
		if (!dtStart.endsWith("00Z")) {
			cc.setAllDay(Boolean.FALSE);
			return;
		}

		SimpleDateFormat formatter = new SimpleDateFormat(TimeUtils.PATTERN_UTC);
		TimeZone tz = TimeZone.getTimeZone("UTC");
		formatter.setLenient(false);
		formatter.setTimeZone(tz);

		Date dateStart = formatter.parse(dtStart);
		Date dateEnd = formatter.parse(dtEnd);

		long timeStart = dateStart.getTime();
		long timeEnd = dateEnd.getTime();

		if (dtEnd.endsWith("5959Z")) {
			timeEnd = timeEnd + 1000; // we add a second because
			// we'll check
			// if the difference is 24H
			// (we have already checked if
			// the end time finished with
			// 5959Z)
		}

		long diff = timeEnd - timeStart;

		long sec = diff / 1000L;

		if (sec == 0) {
			cc.setAllDay(Boolean.FALSE);
			return;
		}

		if (sec % TimeUtils.SECOND_IN_A_DAY != 0) {
			cc.setAllDay(Boolean.FALSE);
			return;
		}

		cc.setAllDay(Boolean.TRUE);

		boolean isGMTPositive = false;

		java.util.Calendar calStart = java.util.Calendar
				.getInstance(TimeUtils.TIMEZONE_UTC);
		calStart.setTime(dateStart);
		calStart.setTimeZone(TimeUtils.TIMEZONE_UTC);

		java.util.Calendar calEnd = java.util.Calendar
				.getInstance(TimeUtils.TIMEZONE_UTC);
		calEnd.setTime(dateEnd);
		calEnd.setTimeZone(TimeUtils.TIMEZONE_UTC);

		int hourEnd = calEnd.get(java.util.Calendar.HOUR_OF_DAY);
		int minuteEnd = calEnd.get(java.util.Calendar.MINUTE);

		int hourMinuteEnd = Integer.parseInt(String.valueOf(hourEnd)
				+ String.valueOf(minuteEnd));

		if (hourMinuteEnd >= 1200 && hourMinuteEnd <= 2350) {
			//
			// Positive
			//
			isGMTPositive = true;
		} else {
			//
			// Negative
			//
			isGMTPositive = false;
		}

		String allDayStart = null;
		String allDayEnd = null;

		if (isGMTPositive) {
			//
			// If the gmt is with an offset positive, the dtStart of the event
			// is
			// a day before so we have to add 1 day
			//
			calStart.add(java.util.Calendar.DATE, 1);

			allDayStart = TimeUtils.convertDateTo(calStart.getTime(),
					TimeUtils.TIMEZONE_UTC, TimeUtils.PATTERN_YYYY_MM_DD);
			allDayEnd = TimeUtils.convertDateTo(calEnd.getTime(),
					TimeUtils.TIMEZONE_UTC, TimeUtils.PATTERN_YYYY_MM_DD);

		} else {
			//
			// If the gmt is with an offset negative, the end of the event is
			// a day after so we have to subtract 1 day
			//
			calEnd.add(java.util.Calendar.DATE, -1);
			//
			// We have also to add 1 minute otherwise with the timezones with
			// offset = 0 we fail
			//
			calEnd.add(java.util.Calendar.MINUTE, 1);

			allDayStart = TimeUtils.convertDateTo(calStart.getTime(),
					TimeUtils.TIMEZONE_UTC, TimeUtils.PATTERN_YYYY_MM_DD);
			allDayEnd = TimeUtils.convertDateTo(calEnd.getTime(),
					TimeUtils.TIMEZONE_UTC, TimeUtils.PATTERN_YYYY_MM_DD);
		}

		cc.setDtStart(new Property(allDayStart));
		cc.setDtEnd(new Property(allDayEnd));
	}

	/**
	 * Converts the importance in the vCalendar scale (one to three, where three
	 * is the lowest priority) to the iCalendar-like scale (one to nine, where
	 * nine is the lowest priority) according to RFC 2445. NB: 3 is the lowest
	 * priority in many implementations of the vCalendar standard, that in
	 * itself does not prescibe a fixed value for the lowest priority level.
	 * 
	 * @param oneToThree
	 *            an int being 1 or 2 or 3
	 * @return an int in the [1; 9] range
	 * @throws NumberFormatException
	 *             if the argument is out of range
	 */
	private int importance13To19(int oneToThree) throws NumberFormatException {
		switch (oneToThree) {
		case 1:
			return 1;
		case 2:
			return 5;
		case 3:
			return 9;
		default:
			throw new NumberFormatException(); // will be caught
		}
	}

	/**
	 * Converts the importance in the iCalendar scale (one to nine, where nine
	 * is the lowest priority) to the vCalendar scale (one to three, where three
	 * is the lowest priority) according to RFC 2445. NB: 3 is the lowest
	 * priority in many implementations of the vCalendar standard, that in
	 * itself does not prescibe a fixed value for the lowest priority level.
	 * 
	 * @param oneToThree
	 *            an int in the [1; 9] range
	 * @return an int being 1 or 2 or 3
	 * @throws NumberFormatException
	 *             if the argument is out of range
	 */
	private int importance19To13(int oneToThree) throws NumberFormatException {
		switch (oneToThree) {
		case 1:
		case 2:
		case 3:
		case 4:
			return 1;
		case 5:
			return 2;
		case 6:
		case 7:
		case 8:
		case 9:
			return 3;
		default:
			throw new NumberFormatException(); // will be caught
		}
	}

	private short accessClassTo03(String accessClass) {
		if (accessClass == null) {
			return SENSITIVITY_NORMAL; // default
		}

		if (accessClass.equals(CLASS_PUBLIC)) {
			return SENSITIVITY_NORMAL;
		}

		if (accessClass.equals(CLASS_PRIVATE)) {
			return SENSITIVITY_PRIVATE;
		}

		if (accessClass.equals(CLASS_CONFIDENTIAL)) {
			return SENSITIVITY_CONFIDENTIAL;
		}

		return SENSITIVITY_PERSONAL; // custom value
	}

	private String accessClassFrom03(Short zeroToThree) {

		if (zeroToThree == null) {
			return CLASS_PUBLIC;
		}

		short s = zeroToThree.shortValue();
		switch (s) {
		case SENSITIVITY_PRIVATE:
			return CLASS_PRIVATE;

		case SENSITIVITY_CONFIDENTIAL:
			return CLASS_CONFIDENTIAL;

		case SENSITIVITY_PERSONAL:
			return CLASS_CUSTOM;

		case SENSITIVITY_NORMAL:
		default:
			return CLASS_PUBLIC;
		}
	}

	/**
	 * Converts the given aalarm string in a Reminder object
	 * 
	 * @param dtStart
	 *            the event's start date
	 * @param aalarm
	 *            the aalarm string
	 * @return the Reminder object built according to the given params
	 */
	private Reminder convertAAlarmToReminder(boolean isAllday,
			Property dtStart, String aalarm) {

		if (aalarm == null) {
			return null;
		}
		Reminder reminder = new Reminder();
		reminder.setActive(false);

		//
		// Splits aalarm considering the eventual spaces before or after ';'
		// like part of the token to search: this because some phones send the
		// values of the AALARM property with space at the beginning of the
		// value.
		// For example
		// AALARM;TYPE=WAVE;VALUE=URL:20070415T235900; ; ;
		// file:///mmedia/taps.wav
		//
		String[] values = aalarm.split("( )*;( )*");
		int cont = 0;
		for (String value : values) {

			switch (cont++) {
			case 0:
				//
				// The first token is the date
				//
				if (value == null || "".equals(value)) {
					// The date is empty
					break;
				}
				try {

					//
					// If the calendar is an all day event (or task) then
					// the aalarm time is considered as a local time
					// information consistently with the way start and end
					// dates are processed.
					//
					String alarmStart = null;
					if (isAllday) {
						//
						// Converts aalarm in local date and time to preserve
						// the distance from aalarm time to midnigth of the
						// start date.
						//
						alarmStart = handleConversionToLocalDate(value,
								timezone);
					} else {
						//
						// Converts aalarm in UTC date and time to preserve
						// the absolute moment of the aalarm.
						//
						alarmStart = handleConversionToUTCDate(value, timezone);
					}

					reminder.setTime(alarmStart);

					if (dtStart != null) {
						reminder.setMinutes(TimeUtils.getAlarmMinutes(dtStart
								.getPropertyValueAsString(), alarmStart, null));
					} else {
						reminder.setMinutes(0);
					}
					reminder.setActive(true);
				} catch (Exception e) {
					//
					// Something went wrong
					//
					reminder.setActive(false);
					return reminder;
				}
				break;

			case 1:
				//
				// The second token is the duration
				//
				if (value == null || "".equals(value)) {
					// The duration is empty
					break;
				}
				reminder.setInterval(TimeUtils.getAlarmInterval(value));

				break;

			case 2:
				//
				// The third token is the repeat count
				//
				if (value == null || "".equals(value)) {
					// The repeat count is empty
					break;
				}
				reminder.setRepeatCount(Integer.parseInt(value));

				break;
			case 3:
				//
				// The fourth token is the sound file
				//
				if (value == null || "".equals(value)) {
					// The sound file is empty
					break;
				}
				reminder.setSoundFile(value);

				break;
			default:
				return reminder;
			}
		}
		return reminder;
	}

	/**
	 * Convert AALARM to Reminder object when the calendar is an all day event
	 * and Timezone is not set and the start date is not null.
	 * 
	 * @param originalDtstart
	 *            start date before convertion
	 * @param convertedDtstart
	 *            start date after convertion
	 * @param aalarm
	 *            the aalarm string
	 */
	private Reminder convertAAlarmToReminderBasedOnMinutes(
			String originalDtstart, String convertedDtstart, String aalarm) {

		if (aalarm == null) {
			return null;
		}
		Reminder reminder = new Reminder();
		reminder.setActive(false);

		//
		// Splits aalarm considering the eventual spaces before or after ';'
		// like part of the token to search: this because some phones send the
		// values of the AALARM property with space at the beginning of the
		// value.
		// For example
		// AALARM;TYPE=WAVE;VALUE=URL:20070415T235900; ; ;
		// file:///mmedia/taps.wav
		//
		String[] values = aalarm.split("( )*;( )*");
		int cont = 0;
		for (String value : values) {

			switch (cont++) {
			case 0:
				//
				// The first token is the date
				//
				if (value == null || "".equals(value)) {
					// The date is empty
					break;
				}
				try {

					//
					// Uses start date converted to local time to calculate
					// minutes
					//
					reminder.setMinutes(TimeUtils.getAlarmMinutes(
							originalDtstart, value, null));

					//
					// Uses original start date minus reminder minutes
					// (calculated previuosly) to set the reminder time.
					// In this way, the reminder time is calculated like
					// relative moment to the timezone.
					//
					java.util.Calendar calAlarm = java.util.Calendar
							.getInstance();
					SimpleDateFormat formatter = new SimpleDateFormat(
							TimeUtils.PATTERN_YYYY_MM_DD_HH_MM_SS);
					calAlarm.setTime(formatter.parse(convertedDtstart
							+ " 00:00:00"));
					calAlarm.add(java.util.Calendar.MINUTE, -reminder
							.getMinutes());

					formatter = new SimpleDateFormat(TimeUtils.PATTERN_UTC_WOZ);
					Date dtAlarm = calAlarm.getTime();
					reminder.setTime(formatter.format(dtAlarm));

					reminder.setActive(true);

				} catch (Exception e) {
					//
					// Something went wrong
					//
					reminder.setActive(false);
					return reminder;
				}
				break;

			case 1:
				//
				// The second token is the duration
				//
				if (value == null || "".equals(value)) {
					// The duration is empty
					break;
				}
				reminder.setInterval(TimeUtils.getAlarmInterval(value));

				break;

			case 2:
				//
				// The third token is the repeat count
				//
				if (value == null || "".equals(value)) {
					// The repeat count is empty
					break;
				}
				reminder.setRepeatCount(Integer.parseInt(value));

				break;
			case 3:
				//
				// The fourth token is the sound file
				//
				if (value == null || "".equals(value)) {
					// The sound file is empty
					break;
				}
				reminder.setSoundFile(value);

				break;
			default:
				return reminder;
			}
		}
		return reminder;
	}
}
