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


public enum ASEmail implements ActiveSyncFields {

	TO("To"),
	REPLY_TO("ReplyTo"),
	DISPLAY_TO("DisplayTo"),
	CC("CC"),
	FROM("From"),
	SUBJECT("Subject"),
	DATE_RECEIVED("DateReceived"),
	MEETING_REQUEST("MeetingRequest"),
	ALL_DAY_EVENT("AllDayEvent"),
	START_TIME("StartTime"),
	END_TIME("EndTime"),
	DTSTAMP("DTStamp"),
	LOCATION("Location"),
	INSTANCE_TYPE("InstanceType"),
	ORGANIZER("Organizer"),
	REMINDER("Reminder"),
	RECURRENCE_ID("RecurrenceId"),
	RESPONSE_REQUESTED("ResponseRequested"),
	SENSITIVITY("Sensitivity"),
	INT_DB_BUSY_STATUS("IntDBusyStatus"),
	TIME_ZONE("TimeZone"),
	GLOBAL_OBJ_ID("GlobalObjId"),
	CATEGORIES("Categories"),
	RECURRENCES("Recurrences"),
	RECURRENCE("Recurrence"),
	INTERVAL("Recurrence_Interval"),
	UNTIL("Recurrence_Until"),
	OCCURRENCES("Recurrence_Occurrences"),
	TYPE("Recurrence_Type"),
	DAY_OF_MONTH("Recurrence_DayOfMonth"),
	WEEK_OF_MONTH("Recurrence_WeekOfMonth"),
	DAY_OF_WEEK("Recurrence_DayOfWeek"),
	MONTH_OF_YEAR("Recurrence_MonthOfYear"),
	READ("Read"),
	IMPORTANCE("Importance"),
	MESSAGE_CLASS("MessageClass"),
	CPID("InternetCPID"),
	CONTENT_CLASS("ContentClass");
	
	private final String name;

	private ASEmail(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String asASValue() {
		return "Email:".concat(getName());
	}
}
