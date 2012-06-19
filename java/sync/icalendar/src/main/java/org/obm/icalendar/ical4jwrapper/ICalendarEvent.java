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
package org.obm.icalendar.ical4jwrapper;

import java.util.Collection;
import java.util.Date;

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class ICalendarEvent {

	private final VEvent vEvent;
	private final ICalendarRecur iCalendarRecur;
	
	public ICalendarEvent(VEvent vEvent) {
		Preconditions.checkNotNull(vEvent);
		this.vEvent = vEvent;
		this.iCalendarRecur = createRRule();
	}
	
	private ICalendarRecur createRRule() {
		RRule rRule = (RRule) vEvent.getProperties().getProperty(Property.RRULE);
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				return new ICalendarRecur(recur);
			}
		}
		return null;
	}
	
	public Date endDate(Date startDate) {
		DtEnd dtEnd = vEvent.getEndDate();
		Duration duration = vEvent.getDuration();
		if (dtEnd != null) {
			return dtEnd.getDate();
		}
		if (duration != null) {			
			Dur dur = duration.getDuration();
			return dur.getTime(startDate);
		}
		return null;
	}
	
	public Date startDate() {
		DtStart startDate = vEvent.getStartDate();
		if (startDate != null) {
			return startDate.getDate();
		}
		return null;
	}

	public Date dtStamp() {
		DtStamp dateStamp = vEvent.getDateStamp();
		if (dateStamp != null) {
			return dateStamp.getDate();
		}
		return null;
	}

	public String location() {
		Location location = vEvent.getLocation();
		if (location != null) {
			return Strings.emptyToNull(location.getValue());
		}
		return null;
	}
	
	public String organizer() {
		Organizer organizer = vEvent.getOrganizer();
		if (organizer != null) {
			return organizer.getCalAddress().getSchemeSpecificPart();
		}
		return null;
	}
	
	public Long firstAlarmDateTime(Date startDate) {
		VAlarm vAlarm = firstVAlarm(vEvent);
		if (vAlarm != null) {
			Dur duration = vAlarm.getTrigger().getDuration();
			return duration.getTime(startDate).getTime();
		}
		return null;
	}
	
	private VAlarm firstVAlarm(VEvent vEvent) {
		Collection<VAlarm> alarms = vEvent.getAlarms();
		if (alarms != null) {
			for (VAlarm vAlarm: alarms) {
				return vAlarm;
			}
		}
		return null;
	}
	
	public String uid() {
		Uid uid = vEvent.getUid();
		if (uid != null) {
			return Strings.emptyToNull(uid.getValue());
		}
		return null;
	}

	public Date reccurenceId() {
		RecurrenceId recurrenceId = vEvent.getRecurrenceId();
		if (recurrenceId != null) {
			return recurrenceId.getDate();
		}
		return null;
	}
	
	public String transparency() {
		Transp transp = vEvent.getTransparency();
		if (transp != null) {
			return Strings.emptyToNull(transp.getValue());
		}
		return null;
	}
	
	public String property(String name) {
		Property property = vEvent.getProperties().getProperty(name);
		if (property != null) {
			return Strings.emptyToNull(property.getValue());
		}
		return null;
	}

	public Clazz classification() {
		return vEvent.getClassification();
	}
	
	public boolean hasRecur() {
		return iCalendarRecur != null;
	}
	
	public ICalendarRecur recur() {
		return iCalendarRecur;
	}
}
