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
package org.obm.icalendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;

import org.obm.icalendar.ical4jwrapper.ICalendarEvent;
import org.obm.icalendar.ical4jwrapper.ICalendarMethod;
import org.obm.icalendar.ical4jwrapper.ICalendarTimeZone;
import org.obm.push.utils.FileUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ICalendar {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private InputStream inputStream;
		private String iCalendar;
		private Organizer organizerFallback;

		private Builder() {
		}
		
		public Builder inputStream(InputStream inputStream) {
			this.inputStream = inputStream;
			return this;
		}
		
		public Builder iCalendar(String iCalendar) {
			this.iCalendar = iCalendar;
			return this;
		}
		
		public Builder organizerFallback(Organizer organizerFallback) {
			this.organizerFallback = organizerFallback;
			return this;
		}
		
		public ICalendar build() throws IOException, ParserException {
			Preconditions.checkState(inputStream != null || iCalendar != null, 
					"Either the inputStream or the iCalendar field must be present");
			
			Preconditions.checkState(! (inputStream != null && iCalendar != null), 
					"Only one of the inputStream field and the iCalendar field may be present");
			
			if (inputStream != null) {
				iCalendar = FileUtils.streamString(inputStream, true);
			}
			validateICalendar();
			return new ICalendar(iCalendar, organizerFallback);
		}

		private void validateICalendar() {
			Preconditions.checkNotNull(iCalendar);
			Preconditions.checkArgument(iCalendar.startsWith("BEGIN"), "ICalendar lacks BEGIN");
		}
	}
	
	private final Calendar calendar;
	private final ICalendarEvent iCalendarEvent;
	private final ICalendarTimeZone iCalendarTimeZone;
	private final String iCalendar;
	
	private ICalendar(String iCalendar, Organizer organizerFallback) throws IOException, ParserException {
		this.calendar = new CalendarBuilder()
			.build(new UnfoldingReader(new StringReader(iCalendar), true));
		
		this.iCalendarEvent = buildCalendarEvent(organizerFallback);
		this.iCalendarTimeZone = new ICalendarTimeZone(calendar);

		this.iCalendar = iCalendar;
	}

	private ICalendarEvent buildCalendarEvent(Organizer organizerFallback) {
		VEvent vEvent = (VEvent)calendar.getComponent(Component.VEVENT);
		if (vEvent != null) {
			return new ICalendarEvent(vEvent, organizerFallback);
		} else {
			return null;
		}
	}
	
	public String getICalendar() {
		return iCalendar;
	}

	public boolean hasEvent() {
		return iCalendarEvent != null;
	}
	
	public ICalendarEvent getICalendarEvent() {
		return iCalendarEvent;
	}
	
	public ICalendarTimeZone getICalendarTimeZone() {
		return iCalendarTimeZone;
	}

	public ICalendarMethod getICalendarMethod() {
		Method method = calendar.getMethod();
		if (method != null) {
			return ICalendarMethod.fromSpecificationValue(method.getValue());
		}
		return null;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(iCalendar);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ICalendar) {
			ICalendar that = (ICalendar) object;
			return Objects.equal(this.iCalendar, that.iCalendar);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("iCalendar", iCalendar)
			.toString();
	}
}
