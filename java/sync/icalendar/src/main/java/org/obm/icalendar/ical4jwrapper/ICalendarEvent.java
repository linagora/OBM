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

import com.google.common.base.Strings;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;


public class ICalendarEvent {

	private final VEvent vEvent;
	private final ICalendarRule iCalendarRule;
	
	public ICalendarEvent(Calendar calendar) {
		this.vEvent = (VEvent)calendar.getComponent(Component.VEVENT);
		this.iCalendarRule = new ICalendarRule(vEvent);
	}
	
	public Date endDate(Date startDate) {
		if (vEvent != null) {
			DtEnd dtEnd = vEvent.getEndDate();
			Duration duration = vEvent.getDuration();
			if (dtEnd != null) {
				return dtEnd.getDate();
			}
			if (duration != null) {			
				Dur dur = duration.getDuration();
				return dur.getTime(startDate);
			}
		}
		return null;
	}
	
	public Date startDate() {
		if (vEvent != null) {
			DtStart startDate = vEvent.getStartDate();
			if (startDate != null) {
				return startDate.getDate();
			}
		}
		return null;
	}

	public Date dtStamp() {
		if (vEvent != null) {
			DtStamp dateStamp = vEvent.getDateStamp();
			if (dateStamp != null) {
				return dateStamp.getDate();
			}
		}
		return null;
	}

	public String location() {
		if (vEvent != null) {
			Location location = vEvent.getLocation();
			if (location != null) {
				return Strings.emptyToNull(location.getValue());
			}
		}
		return null;
	}
	
	public String organizer() {
		if (vEvent != null) {
			Organizer organizer = vEvent.getOrganizer();
			if (organizer != null) {
				return organizer.getCalAddress().getSchemeSpecificPart();
			}
		}
		return null;
	}
	
	public Long firstAlarmDateTime(Date startDate) {
		if (vEvent != null) {
			VAlarm vAlarm = getFirstVAlarm(vEvent);
			if (vAlarm != null) {
				Dur duration = vAlarm.getTrigger().getDuration();
				return duration.getTime(startDate).getTime();
			}
		}
		return null;
	}
	
	private VAlarm getFirstVAlarm(VEvent vEvent) {
		Collection<VAlarm> alarms = vEvent.getAlarms();
		if (alarms != null) {
			for (VAlarm vAlarm: alarms) {
				return vAlarm;
			}
		}
		return null;
	}
	
	public String uid() {
		if (vEvent != null) {
			Uid uid = vEvent.getUid();
			if (uid != null) {
				return Strings.emptyToNull(uid.getValue());
			}
		}
		return null;
	}

	public Date reccurenceId() {
		if (vEvent != null) {
			RecurrenceId recurrenceId = vEvent.getRecurrenceId();
			if (recurrenceId != null) {
				return recurrenceId.getDate();
			}
		}
		return null;
	}
	
	public String transparency() {
		if (vEvent != null) {
			Transp transp = vEvent.getTransparency();
			if (transp != null) {
				return Strings.emptyToNull(transp.getValue());
			}
		}
		return null;
	}
	
	public String property(String name) {
		if (vEvent != null) {
			Property property = vEvent.getProperties().getProperty(name);
			if (property != null) {
				return Strings.emptyToNull(property.getValue());
			}
		}
		return null;
	}

	public Clazz getClassification() {
		if (vEvent != null) {
			return vEvent.getClassification();
		}
		return null;
	}
	
	public boolean isVEvent() {
		return vEvent != null;
	}
	
	public ICalendarRule getICalendarRule() {
		return iCalendarRule;
	}
}
