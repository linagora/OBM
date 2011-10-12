package org.obm.push.protocol.data;

import java.util.ArrayList;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.Recurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.tnefconverter.RTFUtils;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.calendar.EventExtId;
import org.w3c.dom.Element;

public class CalendarDecoder extends Decoder implements IDataDecoder {

	@Override
	public IApplicationData decode(Element syncData) {
		Element containerNode;
		MSEvent calendar = new MSEvent();

		// Main attributes
		calendar.setOrganizerName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OrganizerName")));
		calendar.setOrganizerEmail(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OrganizerEmail")));
		Element uidElement = DOMUtils.getUniqueElement(syncData, "UID");
		EventExtId extId = getExtIdFromElement(uidElement);
		if (extId != null) {
			calendar.setExtId(extId);
		}
		calendar.setTimeZone(parseDOMTimeZone(DOMUtils.getUniqueElement(
				syncData, "TimeZone")));

		setEventCalendar(calendar, syncData);

		// Components attributes

		// Attendees

		containerNode = DOMUtils.getUniqueElement(syncData, "Attendees");
		if (containerNode != null) {
			// <Attendee>
			// <AttendeeName>noIn TheDatabase</AttendeeName>
			// <AttendeeEmail>notin@mydb.com</AttendeeEmail>
			// <AttendeeType>1</AttendeeType>
			// </Attendee>
			// <Attendee>
			// <AttendeeName>Fff Tt</AttendeeName>
			// <AttendeeEmail>ff@tt.com</AttendeeEmail>
			// <AttendeeType>2</AttendeeType>
			// </Attendee>

			for (int i = 0, n = containerNode.getChildNodes().getLength(); i < n; i += 1) {
				Element subnode = (Element) containerNode.getChildNodes().item(
						i);
				MSAttendee attendee = new MSAttendee();

				String email = parseDOMString(DOMUtils.getUniqueElement(
						subnode, "Email"));
				if (email == null || "".equals(email)) {
					email = parseDOMString(DOMUtils.getUniqueElement(subnode,
							"AttendeeEmail"));
				}
				attendee.setEmail(email);

				String name = parseDOMString(DOMUtils.getUniqueElement(subnode,
						"Name"));
				if (name == null || "".equals(name)) {
					name = parseDOMString(DOMUtils.getUniqueElement(subnode,
							"AttendeeName"));
				}
				attendee.setName(name);

				switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(subnode,
						"AttendeeStatus"))) {
				case 0:
					attendee.setAttendeeStatus(AttendeeStatus.RESPONSE_UNKNOWN);
					break;
				case 2:
					attendee.setAttendeeStatus(AttendeeStatus.TENTATIVE);
					break;
				case 3:
					attendee.setAttendeeStatus(AttendeeStatus.ACCEPT);
					break;
				case 4:
					attendee.setAttendeeStatus(AttendeeStatus.DECLINE);
					break;
				case 5:
					attendee.setAttendeeStatus(AttendeeStatus.NOT_RESPONDED);
					break;
				}

				if (attendee.getAttendeeStatus() != null) {
					logger.info("parse attendeeStatus: "
							+ attendee.getAttendeeStatus());
				}

				switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(subnode,
						"AttendeeType"))) {
				case 1:
					attendee.setAttendeeType(AttendeeType.REQUIRED);
					break;
				case 2:
					attendee.setAttendeeType(AttendeeType.OPTIONAL);
					break;
				case 3:
					attendee.setAttendeeType(AttendeeType.RESOURCE);
					break;
				}

				if (attendee.getAttendeeType() != null) {
					logger.info("parse attendeeType: "
							+ attendee.getAttendeeType());
				}
				calendar.addAttendee(attendee);
			}
		}

		// Exceptions
		containerNode = DOMUtils.getUniqueElement(syncData, "Exceptions");
		if (containerNode != null) {
			ArrayList<MSEvent> exceptions = new ArrayList<MSEvent>();
			for (int i = 0, n = containerNode.getChildNodes().getLength(); i < n; i += 1) {
				Element subnode = (Element) containerNode.getChildNodes().item(
						i);
				MSEvent exception = new MSEvent();

				setEventCalendar(exception, subnode);

				exception.setDeleted(parseDOMInt2Boolean(DOMUtils
						.getUniqueElement(subnode, "ExceptionIsDeleted")));
				exception.setExceptionStartTime(parseDOMDate(DOMUtils
						.getUniqueElement(subnode, "ExceptionStartTime")));

				exception.setMeetingStatus(getMeetingStatus(subnode));

				exception.setSensitivity(getCalendarSensitivity(subnode));
				exception.setBusyStatus(getCalendarBusyStatus(subnode));
				exceptions.add(exception);
			}
			calendar.setExceptions(exceptions);
		}

		// Recurrence
		containerNode = DOMUtils.getUniqueElement(syncData, "Recurrence");
		if (containerNode != null) {
			logger.info("decode recurrence");
			Recurrence recurrence = new Recurrence();

			recurrence.setUntil(parseDOMDate(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceUntil")));
			recurrence.setWeekOfMonth(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceWeekOfMonth")));
			recurrence.setMonthOfYear(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceMonthOfYear")));
			recurrence.setDayOfMonth(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceDayOfMonth")));
			recurrence.setOccurrences(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceOccurrences")));
			recurrence.setInterval(parseDOMInt(DOMUtils.getUniqueElement(
					containerNode, "RecurrenceInterval")));
			Integer i = parseDOMInt(DOMUtils.getUniqueElement(containerNode,
					"RecurrenceDayOfWeek"));
			if (i != null) {
				recurrence.setDayOfWeek(RecurrenceDayOfWeek.fromInt(i));
			}

			switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(containerNode,
					"RecurrenceType"))) {
			case 0:
				recurrence.setType(RecurrenceType.DAILY);
				break;
			case 1:
				recurrence.setType(RecurrenceType.WEEKLY);
				break;
			case 2:
				recurrence.setType(RecurrenceType.MONTHLY);
				break;
			case 3:
				recurrence.setType(RecurrenceType.MONTHLY_NDAY);
				break;
			case 5:
				recurrence.setType(RecurrenceType.YEARLY);
				break;
			case 6:
				recurrence.setType(RecurrenceType.YEARLY_NDAY);
				break;
			}

			if (recurrence.getType() != null) {
				logger.info("parse type: " + recurrence.getType());
			}

			calendar.setRecurrence(recurrence);
		}

		return calendar;
	}

	private EventExtId getExtIdFromElement(Element uidElement) {
		try {
			String uidAsString = parseDOMString(uidElement);
			if (uidAsString != null) {
				String decodedUid = new String(Hex.decodeHex(uidAsString.toCharArray()));
				return new EventExtId(decodedUid);
			}
		} catch (DecoderException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	void setEventCalendar(MSEvent calendar, Element domSource) {
		calendar.setLocation(parseDOMString(DOMUtils.getUniqueElement(
				domSource, "Location")));

		// description
		Element body = DOMUtils.getUniqueElement(domSource, "Body");
		if (body != null) {
			Element data = DOMUtils.getUniqueElement(body, "Data");
			if (data != null) {
				Type bodyType = Type.fromInt(Integer.parseInt(DOMUtils
						.getUniqueElement(body, "Type").getTextContent()));
				String txt = data.getTextContent();
				if (bodyType == Type.PLAIN_TEXT) {
					calendar.setDescription(data.getTextContent());
				} else if (bodyType == Type.RTF) {
					calendar.setDescription(RTFUtils.extractB64CompressedRTF(txt));
				} else {
					logger.warn("Unsupported body type: " + bodyType + "\n"
							+ txt);
				}
			}
		}
		Element rtf = DOMUtils.getUniqueElement(domSource, "Compressed_RTF");
		if (rtf != null) {
			String txt = rtf.getTextContent();
			calendar.setDescription(RTFUtils.extractB64CompressedRTF(txt));
		}

		calendar.setDtStamp(parseDOMDate(DOMUtils.getUniqueElement(domSource,
				"DTStamp")));
		calendar.setSubject(parseDOMString(DOMUtils.getUniqueElement(domSource,
				"Subject")));
		calendar.setEndTime(parseDOMDate(DOMUtils.getUniqueElement(domSource,
				"EndTime")));
		calendar.setStartTime(parseDOMDate(DOMUtils.getUniqueElement(domSource,
				"StartTime")));
		calendar.setAllDayEvent(parseDOMInt2Boolean(DOMUtils.getUniqueElement(
				domSource, "AllDayEvent")));
		calendar.setReminder(parseDOMInt(DOMUtils.getUniqueElement(domSource,
				"ReminderMinsBefore")));
		calendar.setCategories(parseDOMStringCollection(DOMUtils
				.getUniqueElement(domSource, "Categories"), "Category"));

		calendar.setBusyStatus(getCalendarBusyStatus(domSource));
		if (calendar.getBusyStatus() != null) {
			logger.info("parse busyStatus: " + calendar.getBusyStatus());
		}

		calendar.setSensitivity(getCalendarSensitivity(domSource));
		if (calendar.getSensitivity() != null) {
			logger.info("parse sensitivity: " + calendar.getSensitivity());
		}

		calendar.setMeetingStatus(getMeetingStatus(domSource));
		if (calendar.getMeetingStatus() != null) {
			logger.info("parse meetingStatus: " + calendar.getMeetingStatus());
		}
	}

	private CalendarBusyStatus getCalendarBusyStatus(Element domSource) {
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource,
				"BusyStatus"))) {
		case 0:
			return CalendarBusyStatus.FREE;
		case 1:
			return CalendarBusyStatus.TENTATIVE;
		case 2:
			return CalendarBusyStatus.BUSY;
		case 3:
			return CalendarBusyStatus.OUT_OF_OFFICE;
		}
		return null;
	}

	private CalendarSensitivity getCalendarSensitivity(Element domSource) {
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource,
				"Sensitivity"))) {
		case 0:
			return CalendarSensitivity.NORMAL;
		case 1:
			return CalendarSensitivity.PERSONAL;
		case 2:
			return CalendarSensitivity.PRIVATE;
		case 3:
			return CalendarSensitivity.CONFIDENTIAL;
		}
		return null;
	}

	private CalendarMeetingStatus getMeetingStatus(Element domSource) {
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource,
				"MeetingStatus"))) {
		case 0:
			return CalendarMeetingStatus.IS_NOT_IN_MEETING;
		case 1:
			return CalendarMeetingStatus.IS_IN_MEETING;
		case 3:
			return CalendarMeetingStatus.MEETING_RECEIVED;
		case 5:
			return CalendarMeetingStatus.MEETING_IS_CANCELED;
		case 7:
			return CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED;
		}
		return null;
	}
}
