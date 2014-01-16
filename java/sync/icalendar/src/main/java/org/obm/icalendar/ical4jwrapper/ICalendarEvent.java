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
package org.obm.icalendar.ical4jwrapper;

import java.net.URI;
import java.util.Collection;
import java.util.Date;

import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;


public class ICalendarEvent {

	private final VEvent vEvent;
	private final ICalendarRecur iCalendarRecur;
	private final Organizer organizerFallback;

	public ICalendarEvent(VEvent vEvent) {
		this(vEvent, null);
	}
	
	public ICalendarEvent(VEvent vEvent, Organizer organizerFallback) {
		Preconditions.checkNotNull(vEvent);
		this.organizerFallback = organizerFallback;
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
	
	public Date endDate() {
		DtEnd dtEnd = vEvent.getEndDate();
		if (dtEnd != null) {
			return dtEnd.getDate();
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
	
	public Organizer organizerFallback() {
		return organizerFallback;
	}

	public String organizer() {
		String icsOrganizer = organizerEmailOrNull(vEvent.getOrganizer());
		if (!Strings.isNullOrEmpty(icsOrganizer)) {
			return icsOrganizer;
		}
		String fallbackOrganizer = organizerEmailOrNull(organizerFallback);
		if (!Strings.isNullOrEmpty(fallbackOrganizer)) {
			return fallbackOrganizer;
		}
		return null;
	}
	
	private String organizerEmailOrNull(Organizer organizer) {
		if (organizer != null) {
			URI calAddress = organizer.getCalAddress();
			if (calAddress != null) {
				return calAddress.getSchemeSpecificPart();
			}
		}
		return null;
	}

	public Long firstAlarmInSeconds() {
		VAlarm vAlarm = firstVAlarm(vEvent);
		if (vAlarm != null && vAlarm.getTrigger() != null) {
			Trigger trigger = vAlarm.getTrigger();
			if (trigger.getDuration() != null) {
				return alarmFromRelatedDateTime(trigger);
			} else {
				return alarmFromSpecificDateTime(trigger);
			}
		}
		return null;
	}

	private long alarmFromRelatedDateTime(Trigger trigger) {
		Date relatedDate = isRelatedEndDate(trigger) ? endDate() : startDate();
		return alarmFromStartDateToDate(trigger.getDuration().getTime(relatedDate));
	}

	private boolean isRelatedEndDate(Trigger trigger) {
		return Related.END.equals(trigger.getParameter(Parameter.RELATED));
	}

	private long alarmFromSpecificDateTime(Trigger trigger) {
		return alarmFromStartDateToDate(trigger.getDateTime());
	}

	private long alarmFromStartDateToDate(Date toDate) {
		return new Duration(
				new DateTime(startDate()),
				new DateTime(toDate))
			.getStandardSeconds();
	}
	
	private VAlarm firstVAlarm(VEvent vEvent) {
		Collection<VAlarm> alarms = vEvent.getAlarms();
		if (alarms != null) {
			return Iterables.getFirst(alarms, null);
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

	public Date recurrenceId() {
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
	
	public String summary() {
		Summary summary = vEvent.getSummary();
		if (summary != null) {
			return summary.getValue();
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

	public String status() {
		Status status = vEvent.getStatus();
		if (status != null) {
			return Strings.emptyToNull(status.getValue());
		}
		return null;
	}
}
