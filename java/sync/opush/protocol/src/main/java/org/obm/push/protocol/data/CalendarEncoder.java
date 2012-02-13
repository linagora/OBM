/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.SyncCollection;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.DateUtils;
import org.w3c.dom.Element;

import com.google.common.base.Strings;
import com.google.inject.Inject;

public class CalendarEncoder extends Encoder implements IDataEncoder {

	private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
	private SimpleDateFormat sdf;
	
	@Inject
	/* package */ CalendarEncoder() {
		super();
		this.sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
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
	@Override
	public void encode(BackendSession bs, Element p, IApplicationData data,
			SyncCollection c, boolean isReponse) {

		MSEvent ev = (MSEvent) data;

		Element tz = DOMUtils.createElement(p, "Calendar:TimeZone");
		// taken from exchange 2k7 : eastern greenland, gmt+0, no dst
		tz
				.setTextContent("xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==");

		s(p, "Calendar:DTStamp", ev.getDtStamp() != null ? ev.getDtStamp()
				: new Date(),sdf);

		s(p, "Calendar:StartTime", ev.getStartTime(),sdf);
		s(p, "Calendar:Subject", ev.getSubject());

		MSEventUid eventUid = ev.getUid();
		if (eventUid != null) {
			s(p, "Calendar:UID", eventUid.serializeToString());
		} else {
			throw new InvalidParameterException("a MSEvent must have an UID");
		}
		if (ev.getOrganizerEmail() != null) {
			s(p, "Calendar:OrganizerName", ev.getOrganizerName());
			s(p, "Calendar:OrganizerEmail", ev.getOrganizerEmail());
		}

		if (bs.checkHint("hint.loadAttendees", true)) {
			if(ev.getAttendees().size()>0){
				Element at = DOMUtils.createElement(p, "Calendar:Attendees");
				for (MSAttendee ma : ev.getAttendees()) {
					Element ae = DOMUtils
						.createElement(at, "Calendar:Attendee");
					s(ae, "Calendar:AttendeeEmail", ma.getEmail());
					
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
					
					s(ae, "Calendar:AttendeeName", ma.getName());

					if (bs.getProtocolVersion().compareTo(TWELVE) >= 0) {
						s(ae, "Calendar:AttendeeStatus", ma.getAttendeeStatus()
							.asIntString());
						s(ae, "Calendar:AttendeeType", ma.getAttendeeType()
							.asIntString());
					}
				}
			}
		}

		s(p, "Calendar:Location", ev.getLocation());
		s(p, "Calendar:EndTime", ev.getEndTime(),sdf);

		encodeBody(bs, p, ev.getDescription());

		if (ev.getRecurrence() != null) {
			encodeRecurrence(p, ev);
			encodeExceptions(bs, ev, p, ev.getExceptions());
		}

		s(p, "Calendar:Sensitivity", ev.getSensitivity().asIntString());
		s(p, "Calendar:BusyStatus", ev.getBusyStatus().asIntString());

		if (ev.getAllDayEvent()) {
			s(p, "Calendar:AllDayEvent", (ev.getAllDayEvent() ? "1" : "0"));
		} else {
			s(p, "Calendar:AllDayEvent", "0");
		}

		if (bs.checkHint("hint.loadAttendees", true)
				&& ev.getAttendees().size() > 1) {
			s(p, "Calendar:MeetingStatus", CalendarMeetingStatus.IS_A_MEETING
					.asIntString());
		} else {
			s(p, "Calendar:MeetingStatus",
					CalendarMeetingStatus.IS_NOT_A_MEETING.asIntString());
		}

		if (isReponse && bs.getProtocolVersion().compareTo(TWELVE) > 0) {
			s(p, "AirSyncBase:NativeBodyType", Type.PLAIN_TEXT.toString());
		}

		if (ev.getReminder() != null) {
			s(p, "Calendar:ReminderMinsBefore", ev.getReminder().toString());
		}

		// DOMUtils.createElement(p, "Calendar:Compressed_RTF");

	}

	private void encodeExceptions(BackendSession bs,
			MSEvent parent,
			Element p, List<MSEventException> excepts) {
		// Exceptions.Exception
		if(excepts.size()>0){
			Element es = DOMUtils.createElement(p, "Calendar:Exceptions");
			for (MSEventException ex : excepts) {
				Element e = DOMUtils.createElement(es, "Calendar:Exception");
				if (ex.isDeletedException()) {
				
					s(e, "Calendar:ExceptionIsDeleted", "1");
					s(e, "Calendar:MeetingStatus",
						CalendarMeetingStatus.MEETING_IS_CANCELED.asIntString());

				} else {
					if (bs.checkHint("hint.loadAttendees", true)
						&& parent.getAttendees().size() > 1) {
						s(e, "Calendar:MeetingStatus",
							CalendarMeetingStatus.IS_A_MEETING.asIntString());
					} else {
						s(e, "Calendar:MeetingStatus",
							CalendarMeetingStatus.IS_NOT_A_MEETING
							.asIntString());
					}

					encodeBody(bs, e, ex.getDescription());

					s(e, "Calendar:Location", ex.getLocation());
					s(e, "Calendar:Sensitivity", ex.getSensitivity().asIntString());
					s(e, "Calendar:BusyStatus", ex.getBusyStatus().asIntString());
					s(e, "Calendar:AllDayEvent", (ex.getAllDayEvent() ? "1" : "0"));
					s(e, "Calendar:ReminderMinsBefore", ex.getReminder());
					DOMUtils.createElement(e, "Calendar:Categories");
				}
				s(e, "Calendar:Subject", ex.getSubject());

				s(e, "Calendar:ExceptionStartTime", ex.getExceptionStartTime(),sdf);

				s(e, "Calendar:StartTime", ex.getStartTime(),sdf);
				s(e, "Calendar:EndTime", ex.getEndTime(),sdf);
				s(e, "Calendar:DTStamp", ex.getDtStamp(),sdf);
			}
		}
	}

	private void encodeBody(BackendSession bs, Element p,
			String description) {
		String body = Strings.nullToEmpty(description).trim();
		if (bs.getProtocolVersion().compareTo(TWELVE) >= 0) {
			Element d = DOMUtils.createElement(p, "AirSyncBase:Body");
			s(d, "AirSyncBase:Type", Type.PLAIN_TEXT.toString());
			s(d, "AirSyncBase:EstimatedDataSize", body.length());
			if (body.length() > 0) {
				DOMUtils.createElementAndText(d, "AirSyncBase:Data", body);
			}
		}
	}

	private void encodeRecurrence(Element p, MSEvent ev) {
		Element r = DOMUtils.createElement(p, "Calendar:Recurrence");
		DOMUtils.createElementAndText(r, "Calendar:RecurrenceType", rec(ev)
				.getType().asIntString());
		s(r, "Calendar:RecurrenceInterval", rec(ev).getInterval());
		s(r, "Calendar:RecurrenceUntil", rec(ev).getUntil(),sdf);

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(ev.getStartTime().getTime());
		switch (rec(ev).getType()) {
		case DAILY:
			break;
		case MONTHLY:
			s(r, "Calendar:RecurrenceDayOfMonth",
					"" + cal.get(Calendar.DAY_OF_MONTH));
			break;
		case MONTHLY_NDAY:
			int weekOfMonth = DateUtils.getWeekOfCurrentDayWithoutStartShift(cal);
			s(r, "Calendar:RecurrenceWeekOfMonth", String.valueOf(weekOfMonth));
			s(r, "Calendar:RecurrenceDayOfWeek", ""
					+ RecurrenceDayOfWeek.dayOfWeekToInt(cal
							.get(Calendar.DAY_OF_WEEK)));
			break;
		case WEEKLY:
			s(r, "Calendar:RecurrenceDayOfWeek", ""
					+ RecurrenceDayOfWeek.asInt(rec(ev).getDayOfWeek()));
			break;
		case YEARLY:
			s(r, "Calendar:RecurrenceDayOfMonth",
					"" + cal.get(Calendar.DAY_OF_MONTH));
			s(r, "Calendar:RecurrenceMonthOfYear",
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
