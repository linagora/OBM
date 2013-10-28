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

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.net.URISyntaxException;
import java.util.Date;

import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ICalendarEventTest {

	private Organizer organizer;
	private String organizerEmail;

	@Before
	public void setUp() throws URISyntaxException {
		organizer = new Organizer("login@domain.org");
		organizerEmail = organizer.getCalAddress().getSchemeSpecificPart();
	}
	
	@Test
	public void testStatusValueNull() {
		VEvent vevent = vEventWithProperty(new Status(null));
		assertThat(new ICalendarEvent(vevent, organizer).status()).isNull();
	}
	
	@Test
	public void testStatusValueEmpty() {
		VEvent vevent = vEventWithProperty(new Status(""));
		assertThat(new ICalendarEvent(vevent, organizer).status()).isNull();
	}
	
	@Test
	public void testStatusValueCancelled() {
		VEvent vevent = vEventWithProperty(new Status("CANCELLED"));
		assertThat(new ICalendarEvent(vevent, organizer).status()).isEqualTo("CANCELLED");
	}

	@Test
	public void testLocationNull() {
		VEvent vevent = vEventWithProperty(new Location(null));
		
		String location = new ICalendarEvent(vevent, organizer).location();
		
		assertThat(location).isNull();
	}
	
	@Test
	public void testLocationEmpty() {
		VEvent vevent = vEventWithProperty(new Location(""));
		
		String location = new ICalendarEvent(vevent, organizer).location();
		
		assertThat(location).isNull();
	}
	
	@Test
	public void testLocationValue() {
		VEvent vevent = vEventWithProperty(new Location("aValue"));
		
		String location = new ICalendarEvent(vevent, organizer).location();
		
		assertThat(location).isEqualTo("aValue");
	}
	
	@Test
	public void testUidNull() {
		VEvent vevent = vEventWithProperty(new Uid(null));
		
		String uid = new ICalendarEvent(vevent, organizer).uid();
		
		assertThat(uid).isNull();
	}
	
	@Test
	public void testUidEmpty() {
		VEvent vevent = vEventWithProperty(new Uid(""));
		
		String uid = new ICalendarEvent(vevent, organizer).uid();
		
		assertThat(uid).isNull();
	}
	
	@Test
	public void testUidValue() {
		VEvent vevent = vEventWithProperty(new Uid("aValue"));
		
		String uid = new ICalendarEvent(vevent, organizer).uid();
		
		assertThat(uid).isEqualTo("aValue");
	}

	@Test
	public void testTransparencyNull() {
		VEvent vevent = vEventWithProperty(new Transp(null));
		
		String transparency = new ICalendarEvent(vevent, organizer).transparency();
		
		assertThat(transparency).isNull();
	}
	
	@Test
	public void testTransparencyEmpty() {
		VEvent vevent = vEventWithProperty(new Transp(""));
		
		String transparency = new ICalendarEvent(vevent, organizer).transparency();
		
		assertThat(transparency).isNull();
	}
	
	@Test
	public void testTransparencyValue() {
		VEvent vevent = vEventWithProperty(new Transp("aValue"));
		
		String transparency = new ICalendarEvent(vevent, organizer).transparency();
		
		assertThat(transparency).isEqualTo("aValue");
	}

	@Test
	public void testPropertyNull() {
		VEvent vevent = vEventWithProperty(new XProperty("aName", null));
		
		String property = new ICalendarEvent(vevent, organizer).property("aName");
		
		assertThat(property).isNull();
	}
	
	@Test
	public void testPropertyEmpty() {
		VEvent vevent = vEventWithProperty(new XProperty("aName", ""));
		
		String property = new ICalendarEvent(vevent, organizer).property("aName");
		
		assertThat(property).isNull();
	}
	
	@Test
	public void testPropertyValue() {
		VEvent vevent = vEventWithProperty(new XProperty("aName", "aValue"));
		
		String property = new ICalendarEvent(vevent, organizer).property("aName");
		
		assertThat(property).isEqualTo("aValue");
	}

	private VEvent vEventWithProperty(Property property) {
		PropertyList properties = new PropertyList();
		properties.add(property);
		return new VEvent(properties);
	}
	
	private PropertyList properties(Property... properties) {
		PropertyList propertyList = new PropertyList(properties.length);
		for (Property property: properties) {
			propertyList.add(property);
		}
		return propertyList;
	}

	private ParameterList parameters(Parameter... parameters) {
		ParameterList parameterList = new ParameterList();
		for (Parameter parameter: parameters) {
			parameterList.add(parameter);
		}
		return parameterList;
	}
	
	private ComponentList alarms(VAlarm... alarms) {
		ComponentList componentList = new ComponentList(alarms.length);
		for (VAlarm alarm: alarms) {
			componentList.add(alarm);
		}
		return componentList;
	}
	
	@Test(expected=NullPointerException.class)
	public void nullEvent() {
		@SuppressWarnings("unused")
		ICalendarEvent iCalendarEvent = new ICalendarEvent(null, organizer);
	}
	
	@Test
	public void nullDtStamp() {
		VEvent vEvent = new VEvent();
		vEvent.getDateStamp().setDate(null);
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.dtStamp()).isNull();
	}
	
	@Test
	public void dtStamp() {
		java.util.Date expectedDate = date("2012-01-01T11:22:33");
		VEvent vEvent = new VEvent(
				properties(
						new DtStamp(new DateTime(expectedDate))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.dtStamp()).isEqualTo(expectedDate);
	}

	@Test
	public void unsetProperties() {
		VEvent vEvent = new VEvent();
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent);
		assertThat(iCalendarEvent.uid()).isNull();
		assertThat(iCalendarEvent.classification()).isNull();
		assertThat(iCalendarEvent.hasRecur()).isFalse();
		assertThat(iCalendarEvent.recur()).isNull();
		assertThat(iCalendarEvent.location()).isNull();
		assertThat(iCalendarEvent.organizer()).isNull();
		assertThat(iCalendarEvent.recurrenceId()).isNull();
		assertThat(iCalendarEvent.startDate()).isNull();
		assertThat(iCalendarEvent.transparency()).isNull();
		assertThat(iCalendarEvent.endDate()).isNull();
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isNull();
	}
	
	@Test
	public void nullUidValue() {
		VEvent vEvent = new VEvent(
				properties(new Uid(null)));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.uid()).isNull();
	}
	
	@Test
	public void uid() {
		String uid = "UIDXX111222333FEAFEAZ";
		VEvent vEvent = new VEvent(
				properties(new Uid(uid)));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.uid()).isEqualTo(uid);
	}
	
	@Test
	public void confidentialClassification() {
		VEvent vEvent = new VEvent(
				properties(Clazz.CONFIDENTIAL));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.classification()).isEqualTo(Clazz.CONFIDENTIAL);
	}

	@Test
	public void hasOneRecur() {
		VEvent vEvent = new VEvent(
				properties(new RRule(new Recur(Recur.WEEKLY, 3))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.hasRecur()).isTrue();
	}

	@Test
	public void nullLocationValue() {
		VEvent vEvent = new VEvent(
				properties(new Location(null)));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.location()).isNull();
	}
	
	@Test
	public void location() {
		String expectedLocation = "at home";
		VEvent vEvent = new VEvent(
				properties(new Location(expectedLocation)));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.location()).isEqualTo(expectedLocation);
	}
	
	@Test
	public void nullOrganizerValue() {
		VEvent vEvent = new VEvent(properties(new Organizer()));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.organizer()).isEqualTo(organizerEmail);
	}
	
	@Test
	public void organizer() throws URISyntaxException {
		String expectedOrganizer = "karl.marx@ussr";
		VEvent vEvent = new VEvent(properties(new Organizer(expectedOrganizer)));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.organizer()).isEqualTo(expectedOrganizer);
	}
	
	@Test
	public void organizerWithMailto() throws URISyntaxException {
		String expectedOrganizer = "karl.marx@ussr";
		VEvent vEvent = new VEvent(properties(new Organizer("MAILTO:" + expectedOrganizer)));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.organizer()).isEqualTo(expectedOrganizer);
	}
	
	@Test
	public void organizerFallback() {
		String expectedOrganizer = organizerEmail;
		VEvent vEvent = new VEvent(properties());
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.organizer()).isEqualTo(expectedOrganizer);
	}
	
	@Test
	public void nullOrganizerAndFallback() {
		VEvent vEvent = new VEvent(properties(new Organizer()));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent);
		assertThat(iCalendarEvent.organizer()).isNull();
	}
	
	@Test
	public void nullOrganizerFallbackIsSameAsAbsent() throws URISyntaxException {
		String expectedOrganizer = "karl.marx@ussr";
		VEvent vEvent = new VEvent(properties(new Organizer(expectedOrganizer)));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, null);
		assertThat(iCalendarEvent.organizer()).isEqualTo(expectedOrganizer);
	}
	
	@Test
	public void recurrenceId() {
		Date expectedRecurrenceId = date("2012-01-01T11:22:33");
		VEvent vEvent = new VEvent(
				properties(new RecurrenceId(new DateTime(expectedRecurrenceId))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.recurrenceId()).isEqualTo(expectedRecurrenceId);
	}
	
	@Test
	public void startDate() {
		Date expectedStartDate = date("2012-01-01T11:22:33");
		VEvent vEvent = new VEvent(
				properties(new DtStart(new DateTime(expectedStartDate))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.startDate()).isEqualTo(expectedStartDate);
	}
	
	@Test
	public void transparencyOpaque() {
		VEvent vEvent = new VEvent(
				properties(Transp.OPAQUE));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.transparency()).isEqualTo("OPAQUE");
	}
	
	@Test
	public void endDate() {
		Date expectedEndDate = date("2012-01-01T11:22:33");
		VEvent vEvent = new VEvent(
				properties(new DtEnd(new DateTime(expectedEndDate))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.endDate()).isEqualTo(expectedEndDate);
	}
	
	@Test
	public void endDateWithStartAndDuration() {
		Date startDate = date("2012-01-01T10:22:33");
		Date expectedEndDate = date("2012-01-01T11:22:33");
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(startDate)),
						new Duration(new Dur("1H"))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.endDate()).isEqualTo(expectedEndDate);
	}
	
	@Test
	public void endDatePriority() {
		Date startDate = date("2012-01-01T10:22:33");
		Date expectedEndDate = date("2012-01-01T11:22:33");
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(startDate)),
						new Duration(new Dur("2H")),
						new DtEnd(new DateTime(expectedEndDate))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.endDate()).isEqualTo(expectedEndDate);
	}
	

	@Test
	public void endDateWithStartButNoDuration() {
		Date startDate = date("2012-01-01T10:22:33");
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(startDate))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.endDate()).isNull();
	}
	
	@Ignore("ical4j bug")
	@Test
	public void endDateWithDurationButNoStart() {
		VEvent vEvent = new VEvent(
				properties(
						new Duration(new Dur("2H"))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.endDate()).isNull();
	}

	@Test
	public void firstAlarmInSecondsNoAlarm() {
		VEvent vEvent = new VEvent(properties());
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isNull();
	}
	
	@Test
	public void firstAlarmInSecondsNoTriggers() {
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33")))),
				alarms(
						new VAlarm()));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isNull();
	}
	
	@Test
	public void firstAlarmInSecondsDateTimeAtBeginning() {
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33")))),
				alarms(
						new VAlarm(new DateTime(date("2012-01-01T20:22:33")))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isZero();
	}
	
	@Test
	public void firstAlarmInSecondsDateTime() {
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33")))),
				alarms(
						new VAlarm(new DateTime(date("2012-01-01T10:22:33")))));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isEqualTo(-36000);
	}

	@Test
	public void firstAlarmDefaultRelatedIsStartDate() {
		int durDays = 0;
		int durHours = -10;
		int durMinutes = 0;
		int durSeconds = 0;
		VAlarm vAlarm = new VAlarm(new Dur(durDays, durHours, durMinutes, durSeconds));
		
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33")))),
				alarms(vAlarm));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isEqualTo(-36000);
	}

	@Test
	public void firstAlarmBeforeRelatedStartTime() {
		int durDays = -1;
		int durHours = -2;
		int durMinutes = -30;
		int durSeconds = 0;
		VAlarm vAlarm = new VAlarm(properties(new Trigger(parameters(Related.START), new Dur(durDays, durHours, durMinutes, durSeconds))));
		
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33")))),
				alarms(vAlarm));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isEqualTo(-95400);
	}

	@Test
	public void firstAlarmAfterRelatedStartTime() {
		int durDays = 0;
		int durHours = 2;
		int durMinutes = 0;
		int durSeconds = 0;
		VAlarm vAlarm = new VAlarm(properties(new Trigger(parameters(Related.START), new Dur(durDays, durHours, durMinutes, durSeconds))));
		
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33")))),
				alarms(vAlarm));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isEqualTo(7200);
	}

	@Test
	public void firstAlarmBeforeRelatedToEndDate() {
		int durDays = 0;
		int durHours = -1;
		int durMinutes = 0;
		int durSeconds = 0;
		VAlarm vAlarm = new VAlarm(properties(new Trigger(parameters(Related.END), new Dur(durDays, durHours, durMinutes, durSeconds))));
		
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33"))),
						new DtEnd(new DateTime(date("2012-01-01T23:22:33")))),
				alarms(vAlarm));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isEqualTo(7200);
	}

	@Test
	public void firstAlarmAfterRelatedToEndDate() {
		int durDays = 0;
		int durHours = 10;
		int durMinutes = 0;
		int durSeconds = 0;
		VAlarm vAlarm = new VAlarm(properties(new Trigger(parameters(Related.END), new Dur(durDays, durHours, durMinutes, durSeconds))));
		
		VEvent vEvent = new VEvent(
				properties(
						new DtStart(new DateTime(date("2012-01-01T20:22:33"))),
						new DtEnd(new DateTime(date("2012-01-01T23:22:33")))),
				alarms(vAlarm));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.firstAlarmInSeconds()).isEqualTo(46800);
	}
	
	@Test
	public void summaryNull() {
		VEvent vEvent = new VEvent(new DateTime(date("2012-01-01T20:22:33")), null);
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.summary()).isNull();
	}
	
	@Test
	public void summaryEmpty() {
		VEvent vEvent = new VEvent(new DateTime(date("2012-01-01T20:22:33")), "");
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.summary()).isEmpty();
	}

	@Test
	public void summary() {
		VEvent vEvent = new VEvent(new DateTime(date("2012-01-01T20:22:33")), "I'm the summary");
		ICalendarEvent iCalendarEvent = new ICalendarEvent(vEvent, organizer);
		
		assertThat(iCalendarEvent.summary()).isEqualTo("I'm the summary");
	}
}
