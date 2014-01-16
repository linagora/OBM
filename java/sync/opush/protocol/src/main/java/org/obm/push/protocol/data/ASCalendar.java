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

public enum ASCalendar {
	
	TIME_ZONE("TimeZone"),
	DTSTAMP("DTStamp"),
	START_TIME("StartTime"),
	SUBJECT("Subject"),
	UID("UID"),
	ORGANIZER_NAME("OrganizerName"),
	ORGANIZER_EMAIL("OrganizerEmail"),
	ATTENDEES("Attendees"),
	ATTENDEE("Attendee"),
	ATTENDEE_EMAIL("AttendeeEmail"),
	ATTENDEE_NAME("AttendeeName"),
	ATTENDEE_STATUS("AttendeeStatus"),
	ATTENDEE_TYPE("AttendeeType"),
	LOCATION("Location"),
	END_TIME("EndTime"),
	SENSITIVITY("Sensitivity"),
	BUSY_STATUS("BusyStatus"),
	ALL_DAY_EVENT("AllDayEvent"),
	MEETING_STATUS("MeetingStatus"),
	REMINDER_MINS_BEFORE("ReminderMinsBefore"),
	EXCEPTIONS("Exceptions"),
	EXCEPTION("Exception"),
	EXCEPTION_IS_DELETED("ExceptionIsDeleted"),
	CATEGORIES("Categories"),
	CATEGORY("Category"),
	EXCEPTION_START_TIME("ExceptionStartTime"),
	RECURRENCE("Recurrence"),
	RECURRENCE_TYPE("RecurrenceType"),
	RECURRENCE_INTERVAL("RecurrenceInterval"),
	RECURRENCE_OCCURRENCES("RecurrenceOccurrences"),
	RECURRENCE_UNTIL("RecurrenceUntil"),
	RECURRENCE_DAY_OF_MONTH("RecurrenceDayOfMonth"),
	RECURRENCE_WEEK_OF_MONTH("RecurrenceWeekOfMonth"),
	RECURRENCE_DAY_OF_WEEK("RecurrenceDayOfWeek"),
	RECURRENCE_MONTH_OF_YEAR("RecurrenceMonthOfYear");
	
	private final String name;

	private ASCalendar(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String asASValue() {
		return "Calendar:".concat(getName());
	}
}
