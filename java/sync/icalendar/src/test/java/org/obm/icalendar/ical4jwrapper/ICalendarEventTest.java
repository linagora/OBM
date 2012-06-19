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

import static org.fest.assertions.api.Assertions.assertThat;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class ICalendarEventTest {

	@Test
	public void testLocationNull() {
		VEvent vevent = vEventWithProperty(new Location(null));
		
		String location = new ICalendarEvent(vevent).location();
		
		assertThat(location).isNull();
	}
	
	@Test
	public void testLocationEmpty() {
		VEvent vevent = vEventWithProperty(new Location(""));
		
		String location = new ICalendarEvent(vevent).location();
		
		assertThat(location).isNull();
	}
	
	@Test
	public void testLocationValue() {
		VEvent vevent = vEventWithProperty(new Location("aValue"));
		
		String location = new ICalendarEvent(vevent).location();
		
		assertThat(location).isEqualTo("aValue");
	}
	
	@Test
	public void testUidNull() {
		VEvent vevent = vEventWithProperty(new Uid(null));
		
		String uid = new ICalendarEvent(vevent).uid();
		
		assertThat(uid).isNull();
	}
	
	@Test
	public void testUidEmpty() {
		VEvent vevent = vEventWithProperty(new Uid(""));
		
		String uid = new ICalendarEvent(vevent).uid();
		
		assertThat(uid).isNull();
	}
	
	@Test
	public void testUidValue() {
		VEvent vevent = vEventWithProperty(new Uid("aValue"));
		
		String uid = new ICalendarEvent(vevent).uid();
		
		assertThat(uid).isEqualTo("aValue");
	}

	@Test
	public void testTransparencyNull() {
		VEvent vevent = vEventWithProperty(new Transp(null));
		
		String transparency = new ICalendarEvent(vevent).transparency();
		
		assertThat(transparency).isNull();
	}
	
	@Test
	public void testTransparencyEmpty() {
		VEvent vevent = vEventWithProperty(new Transp(""));
		
		String transparency = new ICalendarEvent(vevent).transparency();
		
		assertThat(transparency).isNull();
	}
	
	@Test
	public void testTransparencyValue() {
		VEvent vevent = vEventWithProperty(new Transp("aValue"));
		
		String transparency = new ICalendarEvent(vevent).transparency();
		
		assertThat(transparency).isEqualTo("aValue");
	}

	@Test
	public void testPropertyNull() {
		VEvent vevent = vEventWithProperty(new XProperty("aName", null));
		
		String property = new ICalendarEvent(vevent).property("aName");
		
		assertThat(property).isNull();
	}
	
	@Test
	public void testPropertyEmpty() {
		VEvent vevent = vEventWithProperty(new XProperty("aName", ""));
		
		String property = new ICalendarEvent(vevent).property("aName");
		
		assertThat(property).isNull();
	}
	
	@Test
	public void testPropertyValue() {
		VEvent vevent = vEventWithProperty(new XProperty("aName", "aValue"));
		
		String property = new ICalendarEvent(vevent).property("aName");
		
		assertThat(property).isEqualTo("aValue");
	}

	private VEvent vEventWithProperty(Property property) {
		PropertyList properties = new PropertyList();
		properties.add(property);
		return new VEvent(properties);
	}
	
}
