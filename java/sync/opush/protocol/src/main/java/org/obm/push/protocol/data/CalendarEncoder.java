/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.data;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.Device;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class CalendarEncoder extends Encoder {

	private final static String DEFAULT_TIME_ZONE = 
			"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZw" +
			"BoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==";
	
	private final TimeZoneEncoder timeZoneEncoder;
	private final TimeZoneConverter timeZoneConverter;
	
	@Inject
	protected CalendarEncoder(TimeZoneEncoder timeZoneEncoder, TimeZoneConverter timeZoneConverter) {
		super();
		this.timeZoneEncoder = timeZoneEncoder;
		this.timeZoneConverter = timeZoneConverter;
	}
	
	// <TimeZone>xP///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==</TimeZone>
	// <AllDayEvent>0</AllDayEvent>
	// <BusyStatus>2</BusyStatus>
	// <DTStamp>20010101T000000Z</DTStamp>
	// <EndTime>20010101T000000Z</EndTime>
	// <Sensitivity>0</Sensitivity>
	// <StartTime>20010101T000000Z</StartTime>
	// <UID>74455CE0E49D486DBDBC7CB224C5212D00000000000000000000000000000000</UID>
	// <MeetingStatus>0</MeetingStatus>
	public void encode(Device device, Element p, IApplicationData data, boolean isResponse) {

		MSEvent ev = (MSEvent) data;

		TimeZone timeZone = ev.getTimeZone();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		
		if (null != timeZone) {
			s(p, ASCalendar.TIME_ZONE.asASValue(), encodedTimeZoneAsString(timeZone, Locale.getDefault()));
		}

		s(p, ASCalendar.DTSTAMP.asASValue(), 
				ev.getDtStamp() != null ? ev.getDtStamp() : new Date(), sdf);

		s(p, ASCalendar.START_TIME.asASValue(), 
				ev.getStartTime(), sdf);
		s(p, ASCalendar.SUBJECT.asASValue(), ev.getSubject());

		MSEventUid eventUid = ev.getUid();
		if (eventUid != null) {
			s(p, ASCalendar.UID.asASValue(), eventUid.serializeToString());
		} else {
			throw new InvalidParameterException("a MSEvent must have an UID");
		}
		if (ev.getOrganizerEmail() != null) {
			s(p, ASCalendar.ORGANIZER_NAME.asASValue(), ev.getOrganizerName());
			s(p, ASCalendar.ORGANIZER_EMAIL.asASValue(), ev.getOrganizerEmail());
		}

		if (device.checkHint("hint.loadAttendees", true)) {
			if(ev.getAttendees().size()>0){
				Element at = DOMUtils.createElement(p, ASCalendar.ATTENDEES.asASValue());
				for (MSAttendee ma : ev.getAttendees()) {
					Element ae = DOMUtils
						.createElement(at, ASCalendar.ATTENDEE.asASValue());
					s(ae, ASCalendar.ATTENDEE_EMAIL.asASValue(), ma.getEmail());
					
					if (ma.getName() == null
						|| ma.getName().trim().length() == 0) {
						String mail = ma.getEmail();
						int idx = mail.indexOf('@');
						if (idx > 0) {
							mail = mail.substring(0, mail.indexOf('@'))
								.replace(".", " ");
						}
						ma.setName(mail);
					}
					
					s(ae, ASCalendar.ATTENDEE_NAME.asASValue(), ma.getName());

					s(ae, ASCalendar.ATTENDEE_STATUS.asASValue(), ma.getAttendeeStatus().asSpecificationValue());
					s(ae, ASCalendar.ATTENDEE_TYPE.asASValue(), ma.getAttendeeType().getId());
				}
			}
		}

		s(p, ASCalendar.LOCATION.asASValue(), ev.getLocation());
		s(p, ASCalendar.END_TIME.asASValue(), 
				ev.getEndTime(), sdf);

		encodeBody(p, ev.getDescription());

		encodeCategories(p, ev.getCategories());
		
		if (ev.getRecurrence() != null) {
			encodeRecurrence(p, ev, sdf, timeZone);
		}

		s(p, ASCalendar.SENSITIVITY.asASValue(), ev.getSensitivity().asIntString());
		s(p, ASCalendar.BUSY_STATUS.asASValue(), ev.getBusyStatus().asIntString());
		if (ev.getAllDayEvent()) {
			s(p, ASCalendar.ALL_DAY_EVENT.asASValue(), (ev.getAllDayEvent() ? "1" : "0"));
		} else {
			s(p, ASCalendar.ALL_DAY_EVENT.asASValue(), "0");
		}
		
		List<MSEventException> exceptions = Objects.firstNonNull(ev.getExceptions(), ImmutableList.<MSEventException>of());
		encodeExceptions(device, ev, p, exceptions, sdf);
		
		if (device.checkHint("hint.loadAttendees", true)
				&& ev.getAttendees().size() > 1) {
			s(p, ASCalendar.MEETING_STATUS.asASValue(), CalendarMeetingStatus.IS_A_MEETING
					.asIntString());
		} else {
			s(p, ASCalendar.MEETING_STATUS.asASValue(),
					CalendarMeetingStatus.IS_NOT_A_MEETING.asIntString());
		}

		if (isResponse && device.getProtocolVersion().compareTo(ProtocolVersion.V120) > 0) {
			s(p, "AirSyncBase:NativeBodyType", Type.PLAIN_TEXT.toString());
		}

		if (ev.getReminder() != null) {
			s(p, ASCalendar.REMINDER_MINS_BEFORE.asASValue(), ev.getReminder().toString());
		}

		// DOMUtils.createElement(p, "Calendar:Compressed_RTF");

	}

	public Element encodedApplicationData(Device device, IApplicationData data, boolean isResponse) {
		Document doc = DOMUtils.createDoc(null, null);
		Element root = doc.getDocumentElement();
		encode(device, root, data, isResponse);
		return root;
	}
	
	private void encodeCategories(Element p, List<String> categories) {
		if (categories != null && !categories.isEmpty()) {
			Element ce = DOMUtils.createElement(p, ASCalendar.CATEGORIES.asASValue());
			
			for (String category : categories) {
				s(ce, ASCalendar.CATEGORY.asASValue(), category);
			}
		}
	}

	private String encodedTimeZoneAsString(TimeZone timeZone, Locale locale) {
		byte[] encodedTimeZone = encodedTimeZone(timeZone, locale);
		if (encodedTimeZone != null) {
			return Base64.encodeBase64String(encodedTimeZone);
		} else {
			return DEFAULT_TIME_ZONE;
		}
	}
	
	private byte[] encodedTimeZone(TimeZone timeZone, Locale locale) {
		ASTimeZone asTimeZone = timeZoneConverter.convert(timeZone, locale);
		return timeZoneEncoder.encode(asTimeZone);
	}
	
	private void encodeExceptions(Device device,
			MSEvent parent,
			Element p,
			List<MSEventException> excepts,
			SimpleDateFormat sdf) {
		
		// Exceptions.Exception
		if(excepts.size()>0){
			Element es = DOMUtils.createElement(p, ASCalendar.EXCEPTIONS.asASValue());
			for (MSEventException ex : excepts) {
				Element e = DOMUtils.createElement(es, ASCalendar.EXCEPTION.asASValue());
				if (ex.isDeleted()) {
				
					s(e, ASCalendar.EXCEPTION_IS_DELETED.asASValue(), "1");
					s(e, ASCalendar.MEETING_STATUS.asASValue(),
						CalendarMeetingStatus.MEETING_IS_CANCELED.asIntString());

				} else {
					if (device.checkHint("hint.loadAttendees", true)
						&& parent.getAttendees().size() > 1) {
						s(e, ASCalendar.MEETING_STATUS.asASValue(),
							CalendarMeetingStatus.IS_A_MEETING.asIntString());
					} else {
						s(e, ASCalendar.MEETING_STATUS.asASValue(),
							CalendarMeetingStatus.IS_NOT_A_MEETING
							.asIntString());
					}

					encodeBody(e, ex.getDescription());

					s(e, ASCalendar.LOCATION.asASValue(), ex.getLocation());
					s(e, ASCalendar.SENSITIVITY.asASValue(), ex.getSensitivity().asIntString());
					s(e, ASCalendar.BUSY_STATUS.asASValue(), ex.getBusyStatus().asIntString());
					s(e, ASCalendar.ALL_DAY_EVENT.asASValue(), (ex.getAllDayEvent() ? "1" : "0"));
					s(e, ASCalendar.REMINDER_MINS_BEFORE.asASValue(), ex.getReminder());
					encodeCategories(e, ex.getCategories());
				}
				s(e, ASCalendar.SUBJECT.asASValue(), ex.getSubject());

				s(e, ASCalendar.EXCEPTION_START_TIME.asASValue(), 
						ex.getExceptionStartTime(), sdf);

				s(e, ASCalendar.START_TIME.asASValue(), 
						ex.getStartTime(), sdf);
				s(e, ASCalendar.END_TIME.asASValue(), 
						ex.getEndTime(), sdf);
				s(e, ASCalendar.DTSTAMP.asASValue(), 
						ex.getDtStamp(), sdf);
			}
		}
	}

	private void encodeBody(Element p, String description) {
		String body = Strings.nullToEmpty(description).trim();
		Element d = DOMUtils.createElement(p, "AirSyncBase:Body");
		s(d, "AirSyncBase:Type", Type.PLAIN_TEXT.toString());
		s(d, "AirSyncBase:EstimatedDataSize", body.length());
		if (body.length() > 0) {
			DOMUtils.createElementAndText(d, "AirSyncBase:Data", body);
		}
	}

	private void encodeRecurrence(Element p,
			MSEvent ev,
			SimpleDateFormat sdf,
			TimeZone timeZone) {
		
		Element r = DOMUtils.createElement(p, ASCalendar.RECURRENCE.asASValue());
		DOMUtils.createElementAndText(r, ASCalendar.RECURRENCE_TYPE.asASValue(), rec(ev)
				.getType().asIntString());
		s(r, ASCalendar.RECURRENCE_INTERVAL.asASValue(), rec(ev).getInterval());
		s(r, ASCalendar.RECURRENCE_UNTIL.asASValue(), 
				rec(ev).getUntil(), sdf);

		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTimeInMillis(ev.getStartTime().getTime());
		switch (rec(ev).getType()) {
		case DAILY:
			break;
		case MONTHLY:
			s(r, ASCalendar.RECURRENCE_DAY_OF_MONTH.asASValue(),
					"" + cal.get(Calendar.DAY_OF_MONTH));
			break;
		case MONTHLY_NDAY:
			int weekOfMonth = DateUtils.getWeekOfCurrentDayWithoutStartShift(cal);
			s(r, ASCalendar.RECURRENCE_WEEK_OF_MONTH.asASValue(), String.valueOf(weekOfMonth));
			s(r, ASCalendar.RECURRENCE_DAY_OF_WEEK.asASValue(), ""
					+ RecurrenceDayOfWeek.dayOfWeekToInt(cal
							.get(Calendar.DAY_OF_WEEK)));
			break;
		case WEEKLY:
			s(r, ASCalendar.RECURRENCE_DAY_OF_WEEK.asASValue(), ""
					+ RecurrenceDayOfWeek.asInt(rec(ev).getDayOfWeek()));
			break;
		case YEARLY:
			s(r, ASCalendar.RECURRENCE_DAY_OF_MONTH.asASValue(),
					"" + cal.get(Calendar.DAY_OF_MONTH));
			s(r, ASCalendar.RECURRENCE_MONTH_OF_YEAR.asASValue(),
					"" + (cal.get(Calendar.MONTH) + 1));
			break;
		case YEARLY_NDAY:
			break;
		}
	}

	private MSRecurrence rec(MSEvent ev) {
		return ev.getRecurrence();
	}
}
