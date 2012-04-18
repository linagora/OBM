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
package org.obm.push.bean.msmeetingrequest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.google.common.base.Objects;


public class MSMeetingRequest implements Serializable {

	private final boolean allDayEvent;
	private final Date startTime;
	private final Date dtStamp;
	private final Date endTime;
	private final MSMeetingRequestInstanceType msInstanceType;
	private final String location;
	private final String organizer;
	private final Date recurrenceId;
	private final Long reminder;
	private final boolean reponseRequested;
	private final List<MSMeetingRequestRecurrence> recurrences;
	private final MSMeetingRequestSensitivity sensitivity;
	private final MSMeetingRequestIntDBusyStatus intDBusyStatus;
	private final TimeZone timeZone;
	private final String globalObjId;
	private final List<MSMeetingRequestCategory> categories;
	
	public MSMeetingRequest(boolean allDayEvent, Date startTime, Date dtStamp, Date endTime, MSMeetingRequestInstanceType msInstanceType, 
			String location, String organizer, Date recurrenceId, Long reminder, boolean reponseRequested,
			List<MSMeetingRequestRecurrence> recurrences, MSMeetingRequestSensitivity sensitivity, 
			MSMeetingRequestIntDBusyStatus intDBusyStatus, TimeZone timeZone, String globalObjId, 
			List<MSMeetingRequestCategory> categories) {
		
		super();
		this.allDayEvent = allDayEvent;
		this.startTime = startTime;
		this.dtStamp = dtStamp;
		this.endTime = endTime;
		this.msInstanceType = msInstanceType;
		this.location = location;
		this.organizer = organizer;
		this.recurrenceId = recurrenceId;
		this.reminder = reminder;
		this.reponseRequested = reponseRequested;
		this.recurrences = recurrences;
		this.sensitivity = sensitivity;
		this.intDBusyStatus = intDBusyStatus;
		this.timeZone = timeZone;
		this.globalObjId = globalObjId;
		this.categories = categories;
	}

	public boolean isAllDayEvent() {
		return allDayEvent;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getDtStamp() {
		return dtStamp;
	}

	public Date getEndTime() {
		return endTime;
	}

	public MSMeetingRequestInstanceType getMsInstanceType() {
		return msInstanceType;
	}

	public String getLocation() {
		return location;
	}

	public String getOrganizer() {
		return organizer;
	}

	public Date getRecurrenceId() {
		return recurrenceId;
	}

	public Long getReminder() {
		return reminder;
	}

	public boolean isReponseRequested() {
		return reponseRequested;
	}

	public List<MSMeetingRequestRecurrence> getRecurrences() {
		return recurrences;
	}

	public MSMeetingRequestSensitivity getSensitivity() {
		return sensitivity;
	}

	public MSMeetingRequestIntDBusyStatus getIntDBusyStatus() {
		return intDBusyStatus;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public String getGlobalObjId() {
		return globalObjId;
	}

	public List<MSMeetingRequestCategory> getCategories() {
		return categories;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(allDayEvent, startTime, dtStamp, endTime, 
				msInstanceType, location, organizer, recurrenceId, reminder, 
				reponseRequested, recurrences, sensitivity, intDBusyStatus, timeZone, globalObjId, categories);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSMeetingRequest) {
			MSMeetingRequest that = (MSMeetingRequest) object;
			return Objects.equal(this.allDayEvent, that.allDayEvent)
				&& Objects.equal(this.startTime, that.startTime)
				&& Objects.equal(this.dtStamp, that.dtStamp)
				&& Objects.equal(this.endTime, that.endTime)
				&& Objects.equal(this.msInstanceType, that.msInstanceType)
				&& Objects.equal(this.location, that.location)
				&& Objects.equal(this.organizer, that.organizer)
				&& Objects.equal(this.recurrenceId, that.recurrenceId)
				&& Objects.equal(this.reminder, that.reminder)
				&& Objects.equal(this.reponseRequested, that.reponseRequested)
				&& Objects.equal(this.recurrences, that.recurrences)
				&& Objects.equal(this.sensitivity, that.sensitivity)
				&& Objects.equal(this.intDBusyStatus, that.intDBusyStatus)
				&& Objects.equal(this.timeZone, that.timeZone)
				&& Objects.equal(this.globalObjId, that.globalObjId)
				&& Objects.equal(this.categories, that.categories);
		}
		return false;
	}
}
